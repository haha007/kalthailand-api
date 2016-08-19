package th.co.krungthaiaxa.api.elife.products;

import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.DatedAmount;
import th.co.krungthaiaxa.api.elife.model.GeographicalAddress;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isFalse;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isNotEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isTrue;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException.ageIsEmptyException;
import static th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException.ageIsTooHighException;
import static th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException.ageIsTooLowException;

public class ProductUtils {
    public static final String CURRENCY_THB = "THB";
    public static final int MAX_BENEFICIARIES = 6;
    /**
     * At this moment, this modalFactor is still the same for every products. If in the future it's different for one product, please customize it.
     */
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
            //TODO should create a specific Exception, using RuntimeException is not a good practices.
            throw new RuntimeException("The periodicity [" + periodicityCode.name() + "] is invalid to get modal factor");
        }
    };

    public static double getModalFactor(PeriodicityCode periodicityCode) {
        return modalFactor.apply(periodicityCode);
    }

    public static Amount getPremiumFromSumInsured(Amount sumInsured, Double rate, PeriodicityCode periodicityCode) {
        return getPremiumFromSumInsured(sumInsured, rate, 0.0, 0.0, periodicityCode);
    }

    /**
     * @param sumInsured
     * @param premiumRate     must be always greater than 0.
     * @param occupationRate  if the product doesn't affected by occupation, input 0 here.
     * @param discountRate    if there's no discount, input 0 here.
     * @param periodicityCode
     * @return
     */
    public static Amount getPremiumFromSumInsured(Amount sumInsured, double premiumRate, double occupationRate, double discountRate, PeriodicityCode periodicityCode) {

        Amount result = new Amount();
        double value = sumInsured.getValue();
        double rate = validateRates(premiumRate, occupationRate, discountRate);
        value = value * (rate);
        value = value * modalFactor.apply(periodicityCode);
        value = value / 1000;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(sumInsured.getCurrencyCode());
        return result;
    }

    public static Amount getSumInsuredFromPremium(Amount premium, double premiumRate, double occupationRate, double discountRate, PeriodicityCode periodicityCode) {
        Amount result = new Amount();
        double value = premium.getValue();
        double rate = validateRates(premiumRate, occupationRate, discountRate);
        value = value * 1000;
        value = value / modalFactor.apply(periodicityCode);
        value = value / rate;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(premium.getCurrencyCode());
        return result;
    }

    public static Amount getSumInsuredFromPremium(Amount premium, Double premiumRate, PeriodicityCode periodicityCode) {
        return getSumInsuredFromPremium(premium, premiumRate, 0.0, 0.0, periodicityCode);
    }

    private static double validateRates(double premiumRate, double occupationRate, double discountRate) {
        double rate = premiumRate + occupationRate - discountRate;
        isTrue(rate > 0, new ElifeException(String.format("PremiumRate (%s) + OccupationRate (%s) - DiscountRate (%s) must be > 0, while the result is %s", premiumRate, occupationRate, discountRate, rate)));
        return rate;
    }

    public static void addPayments(Policy policy, int durationPaymentInYears) {
        LocalDate startDate = policy.getInsureds().get(0).getStartDate();

        PeriodicityCode periodicityCode = policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        Double amountValue = policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        String amountCurrency = policy.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode();
        int nbOfPayments = (12 / periodicityCode.getNbOfMonths()) * durationPaymentInYears;

        IntStream.range(0, nbOfPayments).forEach(i -> policy.addPayment(new Payment(policy.getPolicyId(),
                amountValue,
                amountCurrency,
                startDate.plusMonths(i * periodicityCode.getNbOfMonths()))
        ));
    }

    public static void checkInsuredAgeInRange(Insured insured, int minAge, int maxAge) {
        notNull(insured.getAgeAtSubscription(), ageIsEmptyException);
        isFalse(insured.getAgeAtSubscription() > maxAge, ageIsTooHighException.apply(maxAge));
        isFalse(insured.getAgeAtSubscription() < minAge, ageIsTooLowException.apply(minAge));
    }

    public static Insured validateMainInsured(Quote quote) {
        notNull(quote.getInsureds(), noInsured);
        isNotEqual(quote.getInsureds().size(), 0, noInsured);
        isEqual(quote.getInsureds().size(), 1, insuredMoreThanOne);

        Insured mainInsured = validateExistMainInsured(quote);
        notNull(mainInsured.getType(), insuredWithNoType);
        notNull(mainInsured.getMainInsuredIndicator(), insuredWithNoMainInsured);
        isFalse(!mainInsured.getMainInsuredIndicator(), noMainInsured);
        notNull(mainInsured.getFatca(), insuredNoFatca);
        notNull(mainInsured.getFatca().isBornInUSA(), insuredFatcaInvalid1);
        notNull(mainInsured.getFatca().isPayTaxInUSA(), insuredFatcaInvalid2);
        notNull(mainInsured.getFatca().isPermanentResidentOfUSAForTax(), insuredFatcaInvalid3);
        notNull(mainInsured.getFatca().getPermanentResidentOfUSA(), insuredFatcaInvalid4);

        notNull(mainInsured.getPerson(), insuredWithNoPerson);
        notNull(mainInsured.getPerson().getGivenName(), personWithNoGivenName);
        notNull(mainInsured.getPerson().getSurName(), personWithNoSurname);
        notNull(mainInsured.getPerson().getTitle(), personWithNoTitle);
        isTrue(checkThaiIDNumbers("insure", mainInsured.getPerson()), personWithInvalidThaiIdNumber);
        return mainInsured;
    }

    /**
     * This method is applied only for mainInsured. For other insured, we don't need to check following data?
     *
     * @param insured
     */
    public static void checkMainInsured(Insured insured) {
        notNull(insured.getStartDate(), mainInsuredWithNoStartDate);
        notNull(insured.getEndDate(), mainInsuredWithNoEndDate);
        notNull(insured.getProfessionId(), mainInsuredWithNoProfessionId);
        notNull(insured.getProfessionName(), mainInsuredWithNoProfessionName);
        notNull(insured.getPerson().getGenderCode(), mainInsuredWithNoGenderCode);
        notNull(insured.getPerson().getMaritalStatus(), mainInsuredWithNoMaritalStatus);
        notNull(insured.getPerson().getBirthDate(), mainInsuredWithNoDOB);
        notNull(insured.getPerson().getEmail(), mainInsuredWithNoEmail);
        //TODO what if request come from another TimeZone? Recheck where was insured.startDate input?
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
        notNull(insured.getHealthStatus().getWeightChangeInLast6Months(), mainInsuredWithNoWeightChange);
        if (insured.getHealthStatus().getWeightChangeInLast6Months()) {
            notNull(insured.getHealthStatus().getWeightChangeInLast6MonthsReason(), mainInsuredWithNoWeightChangeReason);
        }

        if (insured.getPerson().getCurrentAddress() == null && insured.getPerson().getDeliveryAddress() == null) {
            throw PolicyValidationException.mainInsuredWithNoGeographicalAddress;
        }
        checkGeographicalAddress(insured.getPerson().getCurrentAddress());
        checkGeographicalAddress(insured.getPerson().getDeliveryAddress());
        checkGeographicalAddress(insured.getPerson().getRegistrationAddress());
    }

    public static void checkGeographicalAddress(GeographicalAddress address) {
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

    public static void validateNumberOfCoverages(List<Coverage> coverages) {
        notNull(coverages, coverageExpected);
        isFalse(coverages.size() == 0, coverageExpected);
        isTrue(coverages.size() == 1, coverageMoreThanOne);
    }

    public static void checkBeneficiaries(Insured insured, List<CoverageBeneficiary> beneficiaries) {
        notNull(beneficiaries, beneficiariesNone);
        isFalse(beneficiaries.size() == 0, beneficiariesNone);
        isFalse(beneficiaries.size() > MAX_BENEFICIARIES, beneficiariesTooMany);
        isEqual(beneficiaries.stream().mapToDouble(CoverageBeneficiary::getCoverageBenefitPercentage).sum(), 100.0, beneficiariesPercentSumNot100);
        isFalse(beneficiaries.stream().filter(coverageBeneficiary -> coverageBeneficiary.getAgeAtSubscription() == null).findFirst().isPresent(), beneficiariesAgeAtSubscriptionEmpty);

        isFalse(beneficiaries.stream().filter(coverageBeneficiary -> !checkThaiIDNumbers("benefit", coverageBeneficiary.getPerson())).findFirst().isPresent(), beneficiariesWithWrongIDNumber);

        List<String> insuredRegistrationIds = insured.getPerson().getRegistrations().stream()
                .map(Registration::getId)
                .collect(toList());

        //Edit by santi to ignore benefit id blank value ======================>
        
        /*
        List<String> beneficiaryRegistrationIds = beneficiaries.stream()
                .map(coverageBeneficiary -> coverageBeneficiary.getPerson().getRegistrations())
                .flatMap(Collection::stream)
                .map(Registration::getId)
                .collect(toList());
                */

        List<String> beneficiaryRegistrationIds = beneficiaries.stream()
                .filter(p -> !p.getPerson().getRegistrations().get(0).getId().equals(""))
                .map(p -> p.getPerson().getRegistrations())
                .flatMap(Collection::stream)
                .map(Registration::getId)
                .collect(toList());

        //Edit by santi to ignore benefit id blank value ======================>

        Optional<String> hasABeneficiaryWithSameIdAsInsured = beneficiaryRegistrationIds.stream()
                .filter(insuredRegistrationIds::contains)
                .findFirst();
        isFalse(hasABeneficiaryWithSameIdAsInsured.isPresent(), beneficiariesIdIqualToInsuredId);

        Boolean hasDifferentBeneficiaries = beneficiaryRegistrationIds.stream()
                .allMatch(new HashSet<>()::add);
        isTrue(hasDifferentBeneficiaries, beneficiariesWithSameId);
    }

    /**
     * @param type   either "benefit" (the person is a beneficiary person) or "insure" (the person is an insured person).
     * @param person
     * @return
     */
    public static boolean checkThaiIDNumbers(String type, Person person) {
        boolean isValid = true;
        for (Registration registration : person.getRegistrations()) {
            if (type.equals("benefit")) {
                if (!registration.getId().equals("")) {
                    isValid = isValid && checkThaiIDNumber(registration.getId());
                }
            } else {
                isValid = isValid && checkThaiIDNumber(registration.getId());
            }
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

    public static Amount calculateTaxReturnFor10ECOrIGen(Quote quote, String currencyCode) {
        // (min(100000, (premium * tax rate / 100 * numberOfPayments)
        Double premium = quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        Integer taxRate = quote.getInsureds().get(0).getDeclaredTaxPercentAtSubscription();
        PeriodicityCode periodicityCode = quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        int nbOfPaymentsPerYear = 12 / periodicityCode.getNbOfMonths();

        return amount(Math.min(100000.0, Math.round(premium * taxRate / 100 * nbOfPaymentsPerYear)), currencyCode);
    }

    public static boolean hasEnoughTocalculateFor10ECOrIGen(ProductQuotation productQuotation) {
        // Do we have a birth date to calculate the age of insured
        boolean hasAnyDateOfBirth = productQuotation.getDateOfBirth() != null;
        if (!hasAnyDateOfBirth) {
            return false;
        }

        // we need an amount
        boolean hasAmount = productQuotation.getSumInsuredAmount() != null
                || productQuotation.getPremiumAmount() != null;
        if (!hasAmount) {
            return false;
        }

        // We need a periodicity
        return productQuotation.getPeriodicityCode() != null;
    }

    public static Amount amountTHB(Double value) {
        return amount(value, CURRENCY_THB);
    }

    public static Amount exchangeCurrency(Amount amount, String toCurrencyCode) {
        if (amount == null) {
            return null;
        }
        if (!toCurrencyCode.equals(amount.getCurrencyCode())) {
            String msg = String.format("At this moment we don't really support exchange currency yet, so it can only work when two amounts have the same currency. Input: %s.", ObjectMapperUtil.toString(amount));
            throw new UnsupportedOperationException(msg);
        }
        return amount(amount.getValue(), amount.getCurrencyCode());
    }

    public static Amount amount(Double value, String currencyCode) {
        Amount amount = new Amount();
        amount.setCurrencyCode(currencyCode);
        amount.setValue(value);
        return amount;
    }

    public static void checkDatedAmounts(List<DatedAmount> datedAmounts, LocalDate startDate, Integer durationCoverageInYears) {
        List<LocalDate> allowedDates = new ArrayList<>();
        IntStream.range(0, durationCoverageInYears).forEach(value -> allowedDates.add(startDate.plusYears(value + 1)));
        List<LocalDate> filteredDates = datedAmounts.stream().map(DatedAmount::getDate).filter(date -> !allowedDates.contains(date)).collect(toList());

        notNull(datedAmounts, premiumnsCalculatedAmountEmpty);
        isEqual(datedAmounts.size(), durationCoverageInYears, premiumsCalculatedNotEnoughCoverageYears.apply(durationCoverageInYears));
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getCurrencyCode() == null), premiumnsCalculatedAmountNoCurrency);
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getDate() == null), premiumnsCalculatedAmountNoDate);
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getDate().isBefore(LocalDate.now())), premiumnsCalculatedAmountDateInThePast);
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getValue() == null), premiumnsCalculatedAmountNoAmount);
        isEqual(filteredDates.size(), 0, premiumnsCalculatedAmountInvalidDate.apply(filteredDates.stream().map(LocalDate::toString).collect(joining(", "))));
    }

    public static Insured validateExistMainInsured(Quote quote) {
        return quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator)
                .findFirst()
                .orElseThrow(() -> QuoteCalculationException.mainInsuredNotExistException.apply("Insured size: " + quote.getInsureds().size()));
    }

    private static boolean isValidEmailAddress(String email) {
        String ePattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public static double convertPeriodicity(double numberInSourcePeriodicity, PeriodicityCode sourcePeriodicity, PeriodicityCode destPeriodicity) {
        double sourceModalFactor = ProductUtils.getModalFactor(sourcePeriodicity);
        double destModalFactor = ProductUtils.getModalFactor(destPeriodicity);
        double numberInYear = numberInSourcePeriodicity / sourceModalFactor;
        double numberInDestPeriodicity = numberInYear * destModalFactor;
        return numberInDestPeriodicity;
    }

    public static <T extends Enum<T>> T validateExistPackageName(Class<T> packageClass, ProductQuotation productQuotation) {
        String packageName = productQuotation.getPackageName();
        T productPackage = Enum.valueOf(packageClass, packageName);
        notNull(productPackage, QuoteCalculationException.packageNameUnknown.apply(packageName));
        return productPackage;
    }
}
