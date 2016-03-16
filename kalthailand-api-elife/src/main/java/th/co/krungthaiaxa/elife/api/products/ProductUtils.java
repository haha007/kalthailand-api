package th.co.krungthaiaxa.elife.api.products;

import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException.*;

public class ProductUtils {
    public static Function<PeriodicityCode, Double> modalFactor = periodicityCode -> {
        switch (periodicityCode) {
            case EVERY_MONTH:
                return 0.09;
            case EVERY_QUARTER:
                return 0.27;
            case EVERY_HALF_YEAR:
                return 0.52;
            case EVERY_YEAR:
                return 1.0;
            default:
                throw new RuntimeException("The periodicity [" + periodicityCode.name() + "] is invalid to get modal factor");
        }
    };

    public static Amount getPremiumFromSumInsured(Amount sumInsured, Double rate, PeriodicityCode periodicityCode) {
        Amount result = new Amount();
        Double value = sumInsured.getValue();
        value = value * rate;
        value = value * modalFactor.apply(periodicityCode);
        value = value / 1000;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(sumInsured.getCurrencyCode());
        return result;
    }

    public static Amount getSumInsuredFromPremium(Amount premium, Double rate, PeriodicityCode periodicityCode) {
        Amount result = new Amount();
        Double value = premium.getValue();
        value = value * 1000;
        value = value / modalFactor.apply(periodicityCode);
        value = value / rate;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(premium.getCurrencyCode());
        return result;
    }

    public static void addPayments(Policy policy, int durationPaymentInYears) {
        LocalDate startDate = policy.getInsureds().get(0).getStartDate();

        PeriodicityCode periodicityCode = policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        Double amountValue = policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        String amountCurrency = policy.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode();
        int nbOfPayments = (12 / periodicityCode.getNbOfMonths()) * durationPaymentInYears;

        IntStream.range(0, nbOfPayments).forEach(i -> policy.addPayment(new Payment(amountValue, amountCurrency, startDate.plusMonths(i * periodicityCode.getNbOfMonths()))));
    }

    public static void checkInsuredAge(Insured insured, int minAge, int maxAge) throws QuoteCalculationException {
        if (insured.getAgeAtSubscription() == null) {
            throw ageIsEmptyException;
        } else if (insured.getAgeAtSubscription() > maxAge) {
            throw ageIsTooHighException;
        } else if (insured.getAgeAtSubscription() < minAge) {
            throw ageIsTooLowException;
        }
    }

    public static void checkInsured(Quote quote) throws PolicyValidationException {
        notNull(quote.getInsureds(), noInsured);
        isNotEqual(quote.getInsureds().size(), 0, noInsured);
        isEqual(quote.getInsureds().size(), 1, insuredMoreThanOne);

        Insured insured = quote.getInsureds().get(0);
        notNull(insured.getType(), insuredWithNoType);
        notNull(insured.getMainInsuredIndicator(), insuredWithNoMainInsured);
        isFalse(!insured.getMainInsuredIndicator(), noMainInsured);
        notNull(insured.getFatca(), insuredNoFatca);
        notNull(insured.getFatca().isBornInUSA(), insuredFatcaInvalid1);
        notNull(insured.getFatca().isPayTaxInUSA(), insuredFatcaInvalid2);
        notNull(insured.getFatca().isPermanentResidentOfUSAForTax(), insuredFatcaInvalid3);
        notNull(insured.getFatca().getPermanentResidentOfUSA(), insuredFatcaInvalid4);

        notNull(insured.getPerson(), insuredWithNoPerson);
        notNull(insured.getPerson().getGivenName(), personWithNoGivenName);
        notNull(insured.getPerson().getSurName(), personWithNoSurname);
        notNull(insured.getPerson().getTitle(), personWithNoTitle);
        isTrue(checkThaiIDNumbers(insured.getPerson()), personWithInvalidThaiIdNumber);
    }

    public static void checkMainInsured(Insured insured) throws PolicyValidationException {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), mainInsuredWithNoDeclaredTax);
        notNull(insured.getStartDate(), mainInsuredWithNoStartDate);
        notNull(insured.getEndDate(), mainInsuredWithNoEndDate);
        notNull(insured.getProfessionName(), mainInsuredWithNoProfessionName);
        notNull(insured.getPerson().getGenderCode(), mainInsuredWithNoGenderCode);
        notNull(insured.getPerson().getMaritalStatus(), mainInsuredWithNoMaritalStatus);
        notNull(insured.getPerson().getBirthDate(), mainInsuredWithNoDOB);
        notNull(insured.getPerson().getEmail(), mainInsuredWithNoEmail);
        isEqual(insured.getStartDate(), LocalDate.now(), startDateNotServerDate);

        if (insured.getPerson().getHomePhoneNumber() == null && insured.getPerson().getMobilePhoneNumber() == null) {
            throw PolicyValidationException.mainInsuredWithNoPhoneNumber;
        }
        isTrue(isValidEmailAddress(insured.getPerson().getEmail()), mainInsuredWithInvalidEmail);

        notNull(insured.getHealthStatus().getDisableOrImmunoDeficient(), mainInsuredWithNoDisableStatus);
        notNull(insured.getHealthStatus().getHospitalizedInLast6Months(), mainInsuredWithNoHospitalizedStatus);
        notNull(insured.getHealthStatus().getDeniedOrCounterOffer(), mainInsuredWithNoDeniedOrCounterOfferStatus);
        notNull(insured.getHealthStatus().getHeightInCm(), mainInsuredWithNoHeight);
        notNull(insured.getHealthStatus().getWeightInKg(), mainInsuredWithNoWeight);

        if (insured.getPerson().getCurrentAddress() == null && insured.getPerson().getDeliveryAddress() == null) {
            throw PolicyValidationException.mainInsuredWithNoGeographicalAddress;
        }
        checkGeographicalAddress(insured.getPerson().getCurrentAddress());
        checkGeographicalAddress(insured.getPerson().getDeliveryAddress());
        checkGeographicalAddress(insured.getPerson().getRegistrationAddress());
    }

    public static void checkGeographicalAddress(GeographicalAddress address) throws PolicyValidationException {
        if (address == null) {
            return;
        }
        notNull(address.getDistrict(), addressWithNoDistrict);
        notNull(address.getPostCode(), addressWithNoPostCode);
        notNull(address.getStreetAddress1(), addressWithNoStreetAddress1);
        notNull(address.getStreetAddress2(), addressWithNoStreetAddress2);
        notNull(address.getSubCountry(), addressWithNoSubCountry);
        notNull(address.getSubdistrict(), addressWithNoSubDistrict);
    }

    public static void checkCoverage(List<Coverage> coverages) throws PolicyValidationException {
        notNull(coverages, coverageExpected);
        isFalse(coverages.size() == 0, coverageExpected);
        isTrue(coverages.size() == 1, coverageMoreThanOne);
    }

    public static void checkBeneficiaries(Insured insured, List<CoverageBeneficiary> beneficiaries) throws PolicyValidationException {
        notNull(beneficiaries, beneficiariesNone);
        isFalse(beneficiaries.size() == 0, beneficiariesNone);
        isFalse(beneficiaries.size() > 6, beneficiariesTooMany);
        isEqual(beneficiaries.stream().mapToDouble(CoverageBeneficiary::getCoverageBenefitPercentage).sum(), 100.0, beneficiariesPercentSumNot100);
        isFalse(beneficiaries.stream().filter(coverageBeneficiary -> coverageBeneficiary.getAgeAtSubscription() == null).findFirst().isPresent(), beneficiariesAgeAtSubscriptionEmpty);
        isFalse(beneficiaries.stream().filter(coverageBeneficiary -> !checkThaiIDNumbers(coverageBeneficiary.getPerson())).findFirst().isPresent(), beneficiariesWithWrongIDNumber);

        List<String> insuredRegistrationIds = insured.getPerson().getRegistrations().stream()
                .map(Registration::getId)
                .collect(toList());
        List<String> beneficiaryRegistrationIds = beneficiaries.stream()
                .map(coverageBeneficiary -> coverageBeneficiary.getPerson().getRegistrations())
                .flatMap(Collection::stream)
                .map(Registration::getId)
                .collect(toList());

        Optional<String> hasABeneficiaryWithSameIdAsInsured = beneficiaryRegistrationIds.stream()
                .filter(insuredRegistrationIds::contains)
                .findFirst();
        isFalse(hasABeneficiaryWithSameIdAsInsured.isPresent(), beneficiariesIdIqualToInsuredId);

        Boolean hasDifferentBeneficiaries = beneficiaryRegistrationIds.stream()
                .allMatch(new HashSet<>()::add);
        isTrue(hasDifferentBeneficiaries, beneficiariesWithSameId);
    }

    public static boolean checkThaiIDNumbers(Person person) {
        boolean isValid = true;
        for (Registration registration : person.getRegistrations()) {
            isValid = isValid && checkThaiIDNumber(registration.getId());
        }
        return isValid;
    }

    public static boolean checkThaiIDNumber(String id) {
        if (id.length() != 13) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Integer.parseInt(String.valueOf(id.charAt(i))) * (13 - i);
        }

        return id.charAt(12) - '0' == ((11 - (sum % 11)) % 10);
    }

    public static Integer getAge(LocalDate birthDate) {
        return ((Long) ChronoUnit.YEARS.between(birthDate, LocalDate.now())).intValue();
    }

    private static boolean isValidEmailAddress(String email) {
        String ePattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
