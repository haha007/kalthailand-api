package th.co.krungthaiaxa.ebiz.api.products;

import org.apache.commons.lang3.SerializationUtils;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException.*;

public class Product10EC {
    private final static String PRODUCT_10_EC_NAME = "10 EC";

    private static Function<Integer, Integer> dvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 1 && numberOfYearsOfContract <= 9) {
            return 20;
        } else if (numberOfYearsOfContract == 10) {
            return 1820;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> averageExtraDvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 7 && numberOfYearsOfContract <= 9) {
            return 15;
        } else if (numberOfYearsOfContract == 10) {
            return 165;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> maximumExtraDvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 7 && numberOfYearsOfContract <= 9) {
            return 18;
        } else if (numberOfYearsOfContract == 10) {
            return 198;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> rate = age -> {
        if (age >= 0 && age <= 45) {
            return 308;
        } else if (age >= 46 && age <= 50) {
            return 306;
        } else if (age >= 51 && age <= 55) {
            return 304;
        } else if (age >= 56 && age <= 60) {
            return 301;
        } else if (age >= 61 && age <= 65) {
            return 300;
        } else if (age >= 66 && age <= 70) {
            return 298;
        } else {
            return 0;
        }
    };

    private static Function<PeriodicityCode, Double> modalFactor = periodicityCode -> {
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

    public static Quote calculateQuote(Quote quote) throws Exception {
        Optional<Coverage> has10ECCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_10_EC_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(quote)) {
            // we need to delete what might have been calculated before
            quote.getPremiumsData().setLifeInsuranceAverageExtraDividende(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceAverageYearlyReturns(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMaximumExtraDividende(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMaximumYearlyReturns(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMinimumExtraDividende(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMinimumYearlyReturns(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceYearlyCashBacks(new ArrayList<>());
            quote.getCommonData().setProductId(null);
            quote.getCommonData().setProductName(null);
            if (has10ECCoverage.isPresent()) {
                quote.getCoverages().remove(has10ECCoverage.get());
            }
            return quote;
        }

        // cannot be too young or too old
        for (Insured insured : quote.getInsureds()) {
            if (insured.getMainInsuredIndicator()) {
                if (insured.getAgeAtSubscription() > 70) {
                    throw ageIsTooHighException;
                } else if (insured.getAgeAtSubscription() < 20) {
                    throw ageIsTooLowException;
                }
            }
        }

        // calculates premium / sum insured
        PremiumsDataLifeInsurance premiumsData = quote.getPremiumsData();
        if (premiumsData.getLifeInsuranceSumInsured() != null) {
            premiumsData.getFinancialScheduler().setModalAmount(getPremiumFromSumInsured(quote));
        } else {
            premiumsData.setLifeInsuranceSumInsured(getSumInsuredFromPremium(quote));
        }

        if (premiumsData.getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase("THB")
                && premiumsData.getLifeInsuranceSumInsured().getValue() > 1000000.0) {
            throw sumInsuredTooHighException;
        } else if (premiumsData.getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase("THB")
                && premiumsData.getLifeInsuranceSumInsured().getValue() < 200000.0) {
            throw sumInsuredTooLowException;
        }

        // calculates yearly cash backs
        premiumsData.setLifeInsuranceYearlyCashBacks(calculateDatedAmount(quote, null, dvdRate));

        // calculates yearly returns
        premiumsData.setLifeInsuranceMinimumYearlyReturns(calculateDatedAmount(quote, 20, dvdRate));
        premiumsData.setLifeInsuranceAverageYearlyReturns(calculateDatedAmount(quote, 40, dvdRate));
        premiumsData.setLifeInsuranceMaximumYearlyReturns(calculateDatedAmount(quote, 45, dvdRate));

        // calculates yearly returns
        premiumsData.setLifeInsuranceAverageExtraDividende(calculateDatedAmount(quote, 40, averageExtraDvdRate));
        premiumsData.setLifeInsuranceMaximumExtraDividende(calculateDatedAmount(quote, 45, maximumExtraDvdRate));

        if (!has10ECCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_10_EC_NAME);
            quote.getCommonData().setProductId(PRODUCT_10_EC_NAME);
            quote.getCommonData().setProductName(PRODUCT_10_EC_NAME);
            quote.addCoverage(coverage);
        }

        return quote;
    }

    public static void getPolicyFromQuote(Policy policy, Quote quote) throws PolicyValidationException {
        checkInsured(quote);
        checkPerson(quote);
        checkMainInsured(quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get());
        checkBeneficiaries(quote.getInsureds().stream().filter(insured -> !insured.getMainInsuredIndicator()).collect(Collectors.toList()));

        policy.setQuoteFunctionalId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        quote.getCoverages().stream().forEach(coverage -> policy.addCoverage(SerializationUtils.clone(coverage)));
        quote.getInsureds().stream().forEach(insured -> policy.addInsured(SerializationUtils.clone(insured)));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
    }

    private static void checkBeneficiaries(List<Insured> beneficiaries) throws PolicyValidationException {
        if (beneficiaries == null || beneficiaries.size() == 0) {
            throw PolicyValidationException.beneficiariesNone;
        } else if (beneficiaries.size() > 6) {
            throw PolicyValidationException.beneficiariesTooMany;
        }
    }

    private static void checkMainInsured(Insured insured) throws PolicyValidationException {
        if (insured.getDisableOrImmunoDeficient() == null) {
            throw PolicyValidationException.mainInsuredWithNoDisableStatus;
        } else if (insured.getHospitalizedInLast6Months() == null) {
            throw PolicyValidationException.mainInsuredWithNoHospitalizedStatus;
        } else if (insured.getDeclaredTaxPercentAtSubscription() == null) {
            throw PolicyValidationException.mainInsuredWithNoDeclaredTax;
        } else if (insured.getAgeAtSubscription() == null) {
            throw PolicyValidationException.mainInsuredWithNoAge;
        } else if (insured.getStartDate() == null) {
            throw PolicyValidationException.mainInsuredWithNoStartDate;
        } else if (insured.getEndDate() == null) {
            throw PolicyValidationException.mainInsuredWithNoEndDate;
        } else if (insured.getProfessionName() == null) {
            throw PolicyValidationException.mainInsuredWithNoProfessionName;
        } else if (insured.getPerson().getGenderCode() == null) {
            throw PolicyValidationException.mainInsuredWithNoGenderCode;
        } else if (insured.getPerson().getHeightInCm() == null) {
            throw PolicyValidationException.mainInsuredWithNoHeight;
        } else if (insured.getPerson().getMaritalStatus() == null) {
            throw PolicyValidationException.mainInsuredWithNoMaritalStatus;
        } else if (insured.getPerson().getWeightInKg() == null) {
            throw PolicyValidationException.mainInsuredWithNoWeight;
        } else if (insured.getPerson().getBirthDate() == null) {
            throw PolicyValidationException.mainInsuredWithNoDOB;
        } else if (insured.getPerson().getEmail() == null) {
            throw PolicyValidationException.mainInsuredWithNoEmail;
        } else if (insured.getPerson().getGeographicalAddress() == null) {
            throw PolicyValidationException.mainInsuredWithNoGeographicalAddress;
        }

        if (insured.getPerson().getHomePhoneNumber() == null && insured.getPerson().getMobilePhoneNumber() == null) {
            throw PolicyValidationException.mainInsuredWithNoPhoneNumber;
        }
        if (!isValidEmailAddress(insured.getPerson().getEmail())) {
            throw PolicyValidationException.mainInsuredWithInvalidEmail;
        }
        checkGeographicalAddress(insured.getPerson().getGeographicalAddress());
    }

    private static void checkGeographicalAddress(GeographicalAddress address) throws PolicyValidationException {
        if (address.getCountry() == null) {
            throw PolicyValidationException.addressWithNoCountry;
        } else if (address.getDistrict() == null) {
            throw PolicyValidationException.addressWithNoDistrict;
        } else if (address.getPostCode() == null) {
            throw PolicyValidationException.addressWithNoPostCode;
        } else if (address.getStreetAddress1() == null) {
            throw PolicyValidationException.addressWithNoStreetAddress1;
        } else if (address.getStreetAddress2() == null) {
            throw PolicyValidationException.addressWithNoStreetAddress2;
        } else if (address.getSubCountry() == null) {
            throw PolicyValidationException.addressWithNoSubCountry;
        } else if (address.getSubdistrict() == null) {
            throw PolicyValidationException.addressWithNoSubDistrict;
        }
    }

    private static void checkPerson(Quote quote) throws PolicyValidationException {
        if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson() == null)) {
            throw PolicyValidationException.insuredWithNoPerson;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getGivenName() == null)) {
            throw PolicyValidationException.personWithNoGivenName;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getMiddleName() == null)) {
            throw PolicyValidationException.personWithNoMiddleName;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getSurName() == null)) {
            throw PolicyValidationException.personWithNoSurname;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getTitle() == null)) {
            throw PolicyValidationException.personWithNoTitle;
        }
    }

    private static void checkInsured(Quote quote) throws PolicyValidationException {
        if (quote.getInsureds() == null || quote.getInsureds().size() == 0) {
            throw PolicyValidationException.noInsured;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getType() == null)) {
            throw PolicyValidationException.insuredWithNoType;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getMainInsuredIndicator() == null)) {
            throw PolicyValidationException.insuredWithNoMainInsured;
        } else if (!quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().isPresent()) {
            throw PolicyValidationException.noMainInsured;
        } else if (quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).count() != 1) {
            throw PolicyValidationException.moreThanOneMainInsured;
        }
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, Function<Integer, Integer> dvdFunction) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getLifeInsuranceSumInsured();
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= 10; i++) {
            Double interest = sumInsured.getValue() * dvdFunction.apply(i) / 1000;
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setCurrencyCode(sumInsured.getCurrencyCode());
            datedAmount.setDate(endDate.minus(10 - i, ChronoUnit.YEARS));
            if (percentRate != null) {
                latestAmout = (double) Math.round(interest + latestAmout + (latestAmout * percentRate) / 1000);
                datedAmount.setValue(latestAmout);
            } else {
                datedAmount.setValue(interest);
            }
            result.add(datedAmount);
        }
        return result;
    }

    private static boolean hasEnoughTocalculate(Quote quote) {
        // Do we have a birth date to calculate the age of insured
        boolean hasAnyDateOfBirth = quote.getInsureds().stream()
                .filter(insured -> insured != null)
                .filter(insured -> insured.getPerson() != null)
                .filter(insured -> insured.getPerson().getBirthDate() != null)
                .findFirst()
                .isPresent();
        if (!hasAnyDateOfBirth) {
            return false;
        }

        // we need an amount
        boolean hasAmount = quote.getPremiumsData().getLifeInsuranceSumInsured() != null
                || quote.getPremiumsData().getFinancialScheduler().getModalAmount() != null;
        if (!hasAmount) {
            return false;
        }

        // We need a periodicity
        return quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode() != null;
    }

    private static Amount getPremiumFromSumInsured(Quote quote) {
        Amount result = new Amount();
        Double value = quote.getPremiumsData().getLifeInsuranceSumInsured().getValue();
        value = value * rate.apply(quote.getInsureds().get(0).getAgeAtSubscription());
        value = value * modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        value = value / 1000;
        result.setValue(value);
        result.setCurrencyCode(quote.getPremiumsData().getLifeInsuranceSumInsured().getCurrencyCode());
        return result;
    }

    private static Amount getSumInsuredFromPremium(Quote quote) {
        Amount result = new Amount();
        Double value = quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        value = value * 1000;
        value = value / modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        value = value / rate.apply(quote.getInsureds().get(0).getAgeAtSubscription());
        result.setValue(value);
        result.setCurrencyCode(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode());
        return result;
    }

    private static boolean isValidEmailAddress(String email) {
        String ePattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
