package th.co.krungthaiaxa.elife.api.products;

import org.apache.commons.lang3.SerializationUtils;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Product10EC {
    public final static int DURATION_COVERAGE_IN_YEAR = 10;
    public final static int DURATION_PAYMENT_IN_YEAR = 6;
    public final static String PRODUCT_10_EC_NAME = "10 EC";
    public final static String PRODUCT_10_EC_CURRENCY = "THB";


    private static Function<Integer, Integer> dvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 1 && numberOfYearsOfContract <= 9) {
            return 20;
        } else if (numberOfYearsOfContract == DURATION_COVERAGE_IN_YEAR) {
            return 1820;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> minimumExtraDvdRate = numberOfYearsOfContract -> {
        return 0;
    };

    private static Function<Integer, Integer> averageExtraDvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 7 && numberOfYearsOfContract <= 9) {
            return 15;
        } else if (numberOfYearsOfContract == DURATION_COVERAGE_IN_YEAR) {
            return 165;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> maximumExtraDvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 7 && numberOfYearsOfContract <= 9) {
            return 18;
        } else if (numberOfYearsOfContract == DURATION_COVERAGE_IN_YEAR) {
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

    public static Quote calculateQuote(Quote quote) throws QuoteCalculationException {
        Optional<Coverage> has10ECCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
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
        checkMainInsuredAge(quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get());

        // Set dates based on current date and product duration
        LocalDate startDate = LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST")));
        LocalDate endDate = startDate.plus(DURATION_COVERAGE_IN_YEAR, YEARS);
        quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get().setStartDate(startDate);
        quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get().setEndDate(endDate);
        quote.getPremiumsData().getFinancialScheduler().setEndDate(endDate);

        // calculates premium / sum insured
        PremiumsDataLifeInsurance premiumsData = quote.getPremiumsData();
        if (premiumsData.getLifeInsuranceSumInsured() != null) {
            premiumsData.getFinancialScheduler().setModalAmount(getPremiumFromSumInsured(quote));
        } else {
            premiumsData.setLifeInsuranceSumInsured(getSumInsuredFromPremium(quote));
        }

        // cannot insure too much or not enough
        checkSumInsured(premiumsData);

        // calculates yearly cash backs
        premiumsData.setLifeInsuranceYearlyCashBacks(calculateDatedAmount(quote, null, dvdRate));

        // calculates yearly returns
        premiumsData.setLifeInsuranceMinimumYearlyReturns(calculateDatedAmount(quote, 20, dvdRate));
        premiumsData.setLifeInsuranceAverageYearlyReturns(calculateDatedAmount(quote, 40, dvdRate));
        premiumsData.setLifeInsuranceMaximumYearlyReturns(calculateDatedAmount(quote, 45, dvdRate));

        // calculates yearly returns
        premiumsData.setLifeInsuranceMinimumExtraDividende(calculateDatedAmount(quote, null, minimumExtraDvdRate));
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

    public static void getPolicyFromQuote(Policy policy, Quote quote) throws PolicyValidationException, QuoteCalculationException {
        // check for mandatory data
        checkCommonData(quote.getCommonData());
        checkInsured(quote);
        checkPerson(quote);

        // There is only one insured at this point
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        checkMainInsuredAge(insured);
        checkMainInsured(insured);

        // Recalculate the quote
        quote = calculateQuote(quote);

        // check for calculated data
        checkCoverage(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        checkBeneficiaries(coverage.getBeneficiaries());
        checkPremiumsData(quote.getPremiumsData(), insured.getStartDate());

        // Copy from quote to Policy
        policy.setQuoteId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
        policy.addCoverage(SerializationUtils.clone(coverage));
        policy.addInsured(SerializationUtils.clone(insured));

        // Add future payment schedule
        addPayments(policy);
    }

    private static void addPayments(Policy policy) {
        LocalDate startDate = policy.getInsureds().get(0).getStartDate();

        PeriodicityCode periodicityCode = policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        Double amountValue = policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        String amountCurrency = policy.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode();
        int nbOfPayments = (12 / periodicityCode.getNbOfMonths()) * DURATION_PAYMENT_IN_YEAR;

        IntStream.range(0, nbOfPayments).forEach(i -> policy.addPayment(new Payment(amountValue, amountCurrency, startDate.plus(i * periodicityCode.getNbOfMonths(), MONTHS))));
    }

    private static void checkCommonData(CommonData commonData) throws PolicyValidationException {
        if (!commonData.getProductId().equals(PRODUCT_10_EC_NAME)) {
            throw PolicyValidationException.product10ECExpected;
        } else if (!commonData.getProductName().equals(PRODUCT_10_EC_NAME)) {
            throw PolicyValidationException.product10ECExpected;
        }
    }

    private static void checkCoverage(List<Coverage> coverages) throws PolicyValidationException {
        if (coverages == null || coverages.size() == 0) {
            throw PolicyValidationException.coverageExpected;
        } else if (coverages.size() != 1) {
            throw PolicyValidationException.coverageMoreThanOne;
        }
    }

    private static void checkBeneficiaries(List<CoverageBeneficiary> beneficiaries) throws PolicyValidationException {
        if (beneficiaries == null || beneficiaries.size() == 0) {
            throw PolicyValidationException.beneficiariesNone;
        } else if (beneficiaries.size() > 6) {
            throw PolicyValidationException.beneficiariesTooMany;
        } else if (beneficiaries.stream().mapToDouble(CoverageBeneficiary::getCoverageBenefitPercentage).sum() != 100.0) {
            throw PolicyValidationException.beneficiariesPercentSumNot100;
        }
    }

    private static void checkPremiumsData(PremiumsDataLifeInsurance premiumsData, LocalDate startDate) throws PolicyValidationException, QuoteCalculationException {
        if (premiumsData == null) {
            throw PolicyValidationException.premiumnsDataNone;
        } else if (premiumsData.getLifeInsuranceSumInsured() == null) {
            throw PolicyValidationException.premiumnsDataNoSumInsured;
        } else if (premiumsData.getLifeInsuranceSumInsured().getCurrencyCode() == null) {
            throw PolicyValidationException.premiumnsSumInsuredNoCurrency;
        } else if (premiumsData.getLifeInsuranceSumInsured().getValue() == null) {
            throw PolicyValidationException.premiumnsSumInsuredNoAmount;
        }
        checkSumInsured(premiumsData);
        checkDatedAmounts(premiumsData.getLifeInsuranceAverageExtraDividende(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsuranceAverageYearlyReturns(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsuranceMaximumExtraDividende(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsuranceMaximumYearlyReturns(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsuranceMinimumExtraDividende(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsuranceMinimumYearlyReturns(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsuranceYearlyCashBacks(), startDate);
    }

    private static void checkSumInsured(PremiumsDataLifeInsurance premiumsData) throws QuoteCalculationException {
        if (premiumsData.getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase(PRODUCT_10_EC_CURRENCY)
                && premiumsData.getLifeInsuranceSumInsured().getValue() > 1000000.0) {
            throw sumInsuredTooHighException;
        } else if (premiumsData.getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase(PRODUCT_10_EC_CURRENCY)
                && premiumsData.getLifeInsuranceSumInsured().getValue() < 200000.0) {
            throw sumInsuredTooLowException;
        }
    }

    private static void checkDatedAmounts(List<DatedAmount> datedAmounts, LocalDate startDate) throws PolicyValidationException {
        List<LocalDate> allowedDates = new ArrayList<>();
        IntStream.range(0, 10).forEach(value -> allowedDates.add(startDate.plus(value + 1, YEARS)));
        List<LocalDate> filteredDates = datedAmounts.stream().map(DatedAmount::getDate).filter(date -> !allowedDates.contains(date)).collect(toList());
        if (datedAmounts == null) {
            throw PolicyValidationException.premiumnsCalculatedAmountEmpty;
        } else if (datedAmounts.size() != DURATION_COVERAGE_IN_YEAR) {
            throw PolicyValidationException.premiumnsCalculatedAmountNotFor10Years;
        } else if (datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getCurrencyCode() == null)) {
            throw PolicyValidationException.premiumnsCalculatedAmountNoCurrency;
        } else if (datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getDate() == null)) {
            throw PolicyValidationException.premiumnsCalculatedAmountNoDate;
        } else if (datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getDate().isBefore(LocalDate.now()))) {
            throw PolicyValidationException.premiumnsCalculatedAmountDateInThePast;
        } else if (datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getValue() == null)) {
            throw PolicyValidationException.premiumnsCalculatedAmountNoAmount;
        } else if (filteredDates.size() != 0) {
            throw PolicyValidationException.premiumnsCalculatedAmountInvalidDate.apply(filteredDates.stream().map(LocalDate::toString).collect(joining(", ")));
        }
    }

    private static void checkMainInsuredAge(Insured insured) throws QuoteCalculationException {
        if (insured.getAgeAtSubscription() == null) {
            throw ageIsEmptyException;
        } else if (insured.getAgeAtSubscription() > 70) {
            throw ageIsTooHighException;
        } else if (insured.getAgeAtSubscription() < 20) {
            throw ageIsTooLowException;
        }
    }

    private static void checkMainInsured(Insured insured) throws PolicyValidationException {
        if (insured.getDisableOrImmunoDeficient() == null) {
            throw PolicyValidationException.mainInsuredWithNoDisableStatus;
        } else if (insured.getHospitalizedInLast6Months() == null) {
            throw PolicyValidationException.mainInsuredWithNoHospitalizedStatus;
        } else if (insured.getDeclaredTaxPercentAtSubscription() == null) {
            throw PolicyValidationException.mainInsuredWithNoDeclaredTax;
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
        } else if (!insured.getStartDate().equals(LocalDate.now())) {
            throw PolicyValidationException.startDateNotServerDate;
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
        } else if (quote.getInsureds().size() != 1) {
            throw PolicyValidationException.insuredMoreThanOne;
        }

        Insured insured = quote.getInsureds().get(0);
        if (insured.getType() == null) {
            throw PolicyValidationException.insuredWithNoType;
        } else if (insured.getMainInsuredIndicator() == null) {
            throw PolicyValidationException.insuredWithNoMainInsured;
        } else if (!insured.getMainInsuredIndicator()) {
            throw PolicyValidationException.noMainInsured;
        } else if (insured.getFatca() == null) {
            throw PolicyValidationException.insuredNoFatca;
        } else if (insured.getFatca().isBornInUSA() == null) {
            throw PolicyValidationException.insuredFatcaInvalid1;
        } else if (insured.getFatca().isPayTaxInUSA() == null) {
            throw PolicyValidationException.insuredFatcaInvalid2;
        } else if (insured.getFatca().isPermanentResidentOfUSAForTax() == null) {
            throw PolicyValidationException.insuredFatcaInvalid3;
        } else if (insured.getFatca().getPermanentResidentOfUSA() == null) {
            throw PolicyValidationException.insuredFatcaInvalid4;
        }
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, Function<Integer, Integer> dvdFunction) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getLifeInsuranceSumInsured();
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= DURATION_COVERAGE_IN_YEAR; i++) {
            Double interest = sumInsured.getValue() * dvdFunction.apply(i) / 1000;
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setCurrencyCode(sumInsured.getCurrencyCode());
            datedAmount.setDate(endDate.minus(DURATION_COVERAGE_IN_YEAR - i, YEARS));
            if (percentRate != null) {
                latestAmout = (double) Math.round(interest + latestAmout + (latestAmout * percentRate) / 1000);
                datedAmount.setValue(latestAmout);
            } else {
                datedAmount.setValue(interest);
            }
            result.add(datedAmount);
        }
        Collections.sort(result);
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
