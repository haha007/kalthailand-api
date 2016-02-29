package th.co.krungthaiaxa.elife.api.products;

import org.apache.commons.lang3.SerializationUtils;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException.*;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.*;

public class Product10EC implements Product {
    public final static int DURATION_COVERAGE_IN_YEAR = 10;
    public final static int DURATION_PAYMENT_IN_YEAR = 6;
    public final static String PRODUCT_10_EC_ID = "10EC";
    public final static String PRODUCT_10_EC_NAME = "Product 10 EC";
    public final static String PRODUCT_10_EC_CURRENCY = "THB";
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 1000000.0;
    public static final Double PREMIUM_MIN = 2682.0;
    public static final Double PREMIUM_MAX = 308000.0;
    public static final int MAX_AGE = 70;
    public static final int MIN_AGE = 20;

    private static Function<Integer, Integer> dvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 1 && numberOfYearsOfContract <= 9) {
            return 20;
        } else if (numberOfYearsOfContract == DURATION_COVERAGE_IN_YEAR) {
            return 1820;
        } else {
            return 0;
        }
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

    private static Function<Integer, Double> rate = age -> {
        if (age >= MIN_AGE && age <= 45) {
            return 308.0;
        } else if (age >= 46 && age <= 50) {
            return 306.0;
        } else if (age >= 51 && age <= 55) {
            return 304.0;
        } else if (age >= 56 && age <= 60) {
            return 301.0;
        } else if (age >= 61 && age <= 65) {
            return 300.0;
        } else if (age >= 66 && age <= MAX_AGE) {
            return 298.0;
        } else {
            return 0.0;
        }
    };

    @Override
    public void calculateQuote(Quote quote) throws QuoteCalculationException {
        Optional<Coverage> has10ECCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_10_EC_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(quote)) {
            // we need to delete what might have been calculated before
            ProductUtils.resetCalculatedStuff(quote, has10ECCoverage);
            return;
        }

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // cannot be too young or too old
        checkMainInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        LocalDate startDate = LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST")));
        insured.setStartDate(startDate);
        insured.setEndDate(startDate.plus(DURATION_COVERAGE_IN_YEAR, YEARS));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(startDate.plus(DURATION_PAYMENT_IN_YEAR, YEARS));

        PremiumsData premiumsData = quote.getPremiumsData();
        // cannot insure too much or not enough
        checkSumInsured(premiumsData, PRODUCT_10_EC_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        checkPremium(premiumsData);

        // calculates premium / sum insured
        if (premiumsData.getLifeInsurance().getSumInsured() != null) {
            premiumsData.getFinancialScheduler().setModalAmount(getPremiumFromSumInsured(quote, rate.apply(quote.getInsureds().get(0).getAgeAtSubscription())));
        } else {
            premiumsData.getLifeInsurance().setSumInsured(getSumInsuredFromPremium(quote, rate.apply(quote.getInsureds().get(0).getAgeAtSubscription())));
        }

        // calculates yearly cash backs
        premiumsData.getLifeInsurance().setYearlyCashBacks(calculateDatedAmount(quote, null, dvdRate));

        // calculates yearly returns
        premiumsData.getLifeInsurance().setEndOfContractBenefitsMinimum(calculateDatedAmount(quote, 20, dvdRate));
        premiumsData.getLifeInsurance().setEndOfContractBenefitsAverage(calculateDatedAmount(quote, 40, dvdRate));
        premiumsData.getLifeInsurance().setEndOfContractBenefitsMaximum(calculateDatedAmount(quote, 45, dvdRate));

        // calculates yearly returns
        premiumsData.getLifeInsurance().setYearlyCashBacksAverageDividende(calculateDatedAmount(quote, null, averageExtraDvdRate));
        premiumsData.getLifeInsurance().setYearlyCashBacksMaximumDividende(calculateDatedAmount(quote, null, maximumExtraDvdRate));

        // calculates yearly benefits
        premiumsData.getLifeInsurance().setYearlyCashBacksAverageBenefit(calculateDatedAmount(quote, 40, averageExtraDvdRate));
        premiumsData.getLifeInsurance().setYearlyCashBacksMaximumBenefit(calculateDatedAmount(quote, 45, maximumExtraDvdRate));

        // calculate tax deduction
        premiumsData.getLifeInsurance().setYearlyTaxDeduction(calculateTaxReturn(quote));

        if (!has10ECCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_10_EC_NAME);
            quote.addCoverage(coverage);
        }
    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) throws PolicyValidationException, QuoteCalculationException {
        // check for mandatory data
        checkCommonData(quote.getCommonData());
        checkInsured(quote);

        // There is only one insured at this point
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        checkMainInsuredAge(insured, MIN_AGE, MAX_AGE);
        checkMainInsured(insured);

        // Recalculate the quote
        calculateQuote(quote);

        // check for calculated data
        checkCoverage(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        checkBeneficiaries(insured, coverage.getBeneficiaries());
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

    @Override
    public CommonData getCommonData() {
        CommonData commonData = new CommonData();
        commonData.setMaxAge(MAX_AGE);
        commonData.setMaxPremium(amount(PREMIUM_MAX));
        commonData.setMaxSumInsured(amount(SUM_INSURED_MAX));
        commonData.setMinAge(MIN_AGE);
        commonData.setMinPremium(amount(PREMIUM_MIN));
        commonData.setMinSumInsured(amount(SUM_INSURED_MIN));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        commonData.setProductId(PRODUCT_10_EC_ID);
        commonData.setProductName(PRODUCT_10_EC_NAME);
        return commonData;
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
        if (!commonData.getProductId().equals(PRODUCT_10_EC_ID)) {
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

    private static void checkBeneficiaries(Insured insured, List<CoverageBeneficiary> beneficiaries) throws PolicyValidationException {
        if (beneficiaries == null || beneficiaries.size() == 0) {
            throw PolicyValidationException.beneficiariesNone;
        } else if (beneficiaries.size() > 6) {
            throw PolicyValidationException.beneficiariesTooMany;
        } else if (beneficiaries.stream().mapToDouble(CoverageBeneficiary::getCoverageBenefitPercentage).sum() != 100.0) {
            throw PolicyValidationException.beneficiariesPercentSumNot100;
        } else if (beneficiaries.stream().filter(coverageBeneficiary -> coverageBeneficiary.getAgeAtSubscription() == null).findFirst().isPresent()) {
            throw PolicyValidationException.beneficiariesAgeAtSubscriptionEmpty;
        } else if (beneficiaries.stream().filter(coverageBeneficiary -> !checkThaiIDNumbers(coverageBeneficiary.getPerson())).findFirst().isPresent()) {
            throw PolicyValidationException.beneficiariesWithWrongIDNumber;
        }

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
        if (hasABeneficiaryWithSameIdAsInsured.isPresent()) {
            throw PolicyValidationException.beneficiariesIdIqualToInsuredId;
        }

        Boolean hasDifferentBeneficiaries = beneficiaryRegistrationIds.stream()
                .allMatch(new HashSet<>()::add);
        if (!hasDifferentBeneficiaries) {
            throw PolicyValidationException.beneficiariesWithSameId;
        }
    }

    private static boolean checkThaiIDNumbers(Person person) {
        boolean isValid = true;
        for (Registration registration : person.getRegistrations()) {
            isValid = isValid && checkThaiIDNumber(registration.getId());
        }
        return isValid;
    }

    private static boolean checkThaiIDNumber(String id) {
        if (id.length() != 13) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Integer.parseInt(String.valueOf(id.charAt(i))) * (13 - i);
        }

        return id.charAt(12) - '0' == ((11 - (sum % 11)) % 10);
    }

    private static void checkPremiumsData(PremiumsData premiumsData, LocalDate startDate) throws PolicyValidationException, QuoteCalculationException {
        if (premiumsData == null) {
            throw PolicyValidationException.premiumnsDataNone;
        } else if (premiumsData.getLifeInsurance().getSumInsured() == null) {
            throw PolicyValidationException.premiumnsDataNoSumInsured;
        } else if (premiumsData.getLifeInsurance().getSumInsured().getCurrencyCode() == null) {
            throw PolicyValidationException.premiumnsSumInsuredNoCurrency;
        } else if (premiumsData.getLifeInsurance().getSumInsured().getValue() == null) {
            throw PolicyValidationException.premiumnsSumInsuredNoAmount;
        }
        checkSumInsured(premiumsData, PRODUCT_10_EC_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        checkDatedAmounts(premiumsData.getLifeInsurance().getEndOfContractBenefitsAverage(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsurance().getEndOfContractBenefitsMaximum(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsurance().getEndOfContractBenefitsMinimum(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsurance().getYearlyCashBacks(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsurance().getYearlyCashBacksAverageBenefit(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsurance().getYearlyCashBacksAverageDividende(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsurance().getYearlyCashBacksMaximumBenefit(), startDate);
        checkDatedAmounts(premiumsData.getLifeInsurance().getYearlyCashBacksMaximumDividende(), startDate);
    }

    private static void checkPremium(PremiumsData premiumsData) throws QuoteCalculationException {
        if (premiumsData.getFinancialScheduler().getModalAmount() == null || premiumsData.getFinancialScheduler().getModalAmount().getValue() == null) {
            // no amount to check
            return;
        } else if (!PRODUCT_10_EC_CURRENCY.equalsIgnoreCase(premiumsData.getFinancialScheduler().getModalAmount().getCurrencyCode())) {
            throw premiumCurrencyException.apply(PRODUCT_10_EC_CURRENCY);
        }

        if (premiumsData.getFinancialScheduler().getModalAmount().getValue() > PREMIUM_MAX) {
            throw premiumTooHighException.apply(PREMIUM_MAX);
        } else if (premiumsData.getFinancialScheduler().getModalAmount().getValue() < PREMIUM_MIN) {
            throw premiumTooLowException.apply(PREMIUM_MIN);
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

    private static void checkMainInsured(Insured insured) throws PolicyValidationException {
        if (insured.getDeclaredTaxPercentAtSubscription() == null) {
            throw PolicyValidationException.mainInsuredWithNoDeclaredTax;
        } else if (insured.getStartDate() == null) {
            throw PolicyValidationException.mainInsuredWithNoStartDate;
        } else if (insured.getEndDate() == null) {
            throw PolicyValidationException.mainInsuredWithNoEndDate;
        } else if (insured.getProfessionName() == null) {
            throw PolicyValidationException.mainInsuredWithNoProfessionName;
        } else if (insured.getPerson().getGenderCode() == null) {
            throw PolicyValidationException.mainInsuredWithNoGenderCode;
        } else if (insured.getPerson().getMaritalStatus() == null) {
            throw PolicyValidationException.mainInsuredWithNoMaritalStatus;
        } else if (insured.getPerson().getBirthDate() == null) {
            throw PolicyValidationException.mainInsuredWithNoDOB;
        } else if (insured.getPerson().getEmail() == null) {
            throw PolicyValidationException.mainInsuredWithNoEmail;
        } else if (!insured.getStartDate().equals(LocalDate.now())) {
            throw PolicyValidationException.startDateNotServerDate;
        }

        if (insured.getPerson().getHomePhoneNumber() == null && insured.getPerson().getMobilePhoneNumber() == null) {
            throw PolicyValidationException.mainInsuredWithNoPhoneNumber;
        }
        if (!isValidEmailAddress(insured.getPerson().getEmail())) {
            throw PolicyValidationException.mainInsuredWithInvalidEmail;
        }

        if (insured.getHealthStatus().getDisableOrImmunoDeficient() == null) {
            throw PolicyValidationException.mainInsuredWithNoDisableStatus;
        } else if (insured.getHealthStatus().getHospitalizedInLast6Months() == null) {
            throw PolicyValidationException.mainInsuredWithNoHospitalizedStatus;
        } else if (insured.getHealthStatus().getHeightInCm() == null) {
            throw PolicyValidationException.mainInsuredWithNoHeight;
        } else if (insured.getHealthStatus().getWeightInKg() == null) {
            throw PolicyValidationException.mainInsuredWithNoWeight;
        }

        if (insured.getPerson().getCurrentAddress() == null && insured.getPerson().getDeliveryAddress() == null) {
            throw PolicyValidationException.mainInsuredWithNoGeographicalAddress;
        }
        checkGeographicalAddress(insured.getPerson().getCurrentAddress());
        checkGeographicalAddress(insured.getPerson().getDeliveryAddress());
        checkGeographicalAddress(insured.getPerson().getRegistrationAddress());
    }

    private static void checkGeographicalAddress(GeographicalAddress address) throws PolicyValidationException {
        if (address == null) {
            return;
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
        if (insured.getPerson() == null) {
            throw PolicyValidationException.insuredWithNoPerson;
        } else if (insured.getPerson().getGivenName() == null) {
            throw PolicyValidationException.personWithNoGivenName;
        } else if (insured.getPerson().getSurName() == null) {
            throw PolicyValidationException.personWithNoSurname;
        } else if (insured.getPerson().getTitle() == null) {
            throw PolicyValidationException.personWithNoTitle;
        } else if (!checkThaiIDNumbers(insured.getPerson())) {
            throw PolicyValidationException.personWithInvalidThaiIdNumber;
        }
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, Function<Integer, Integer> dvdFunction) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getLifeInsurance().getSumInsured();
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= DURATION_COVERAGE_IN_YEAR; i++) {
            Double interest = sumInsured.getValue() * dvdFunction.apply(i) / 1000;
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setCurrencyCode(sumInsured.getCurrencyCode());
            datedAmount.setDate(startDate.plus(i, YEARS));
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

    private Amount calculateTaxReturn(Quote quote) {
        // (min(100000, (premium * tax rate / 100 * numberOfPayments)
        Double premium = quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        Integer taxRate = quote.getInsureds().get(0).getDeclaredTaxPercentAtSubscription();
        PeriodicityCode periodicityCode = quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        int nbOfPaymentsPerYear = 12 / periodicityCode.getNbOfMonths();

        Amount amount = new Amount();
        amount.setCurrencyCode(PRODUCT_10_EC_CURRENCY);
        amount.setValue(Math.min(100000.0, Math.round(premium * taxRate / 100 * nbOfPaymentsPerYear)));
        return amount;
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
        boolean hasAmount = quote.getPremiumsData().getLifeInsurance().getSumInsured() != null
                || quote.getPremiumsData().getFinancialScheduler().getModalAmount() != null;
        if (!hasAmount) {
            return false;
        }

        // We need a periodicity
        return quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode() != null;
    }

    private static boolean isValidEmailAddress(String email) {
        String ePattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    private static Amount amount(Double value) {
        Amount amount = new Amount();
        amount.setCurrencyCode(PRODUCT_10_EC_CURRENCY);
        amount.setValue(value);
        return amount;
    }
}
