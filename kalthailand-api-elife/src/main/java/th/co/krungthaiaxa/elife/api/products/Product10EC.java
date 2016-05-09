package th.co.krungthaiaxa.elife.api.products;

import org.apache.commons.lang3.SerializationUtils;
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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static th.co.krungthaiaxa.elife.api.exception.ExceptionUtils.*;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException.*;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_10_EC;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.*;

public class Product10EC implements Product {
    public final static int DURATION_COVERAGE_IN_YEAR = 10;
    public final static int DURATION_PAYMENT_IN_YEAR = 6;
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
    public void calculateQuote(Quote quote, ProductQuotation productQuotation)  {
        if (productQuotation == null) {
            return;
        }

        Optional<Coverage> has10ECCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_10_EC_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(productQuotation)) {
            // we need to delete what might have been calculated before
            resetCalculatedStuff(quote, has10ECCoverage);
            return;
        }

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // copy data already gathered in ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        insured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        insured.setAgeAtSubscription(getAge(productQuotation.getDateOfBirth()));
        insured.getPerson().setGenderCode(productQuotation.getGenderCode());
        insured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            quote.getPremiumsData().getProduct10ECPremium().setSumInsured(amount(productQuotation.getSumInsuredAmount().getValue()));
            quote.getPremiumsData().getProduct10ECPremium().setSumInsuredOption(TRUE);
        } else {
            quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount(productQuotation.getPremiumAmount().getValue()));
            quote.getPremiumsData().getProduct10ECPremium().setSumInsuredOption(FALSE);
        }

        // cannot be too young or too old
        checkInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        LocalDate startDate = LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST")));
        insured.setStartDate(startDate);
        insured.setEndDate(startDate.plusYears(DURATION_COVERAGE_IN_YEAR));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(startDate.plusYears(DURATION_PAYMENT_IN_YEAR));

        PremiumsData premiumsData = quote.getPremiumsData();
        // calculates premium / sum insured
        if (premiumsData.getProduct10ECPremium().getSumInsured() != null) {
            premiumsData.getFinancialScheduler().setModalAmount(getPremiumFromSumInsured(
                    quote.getPremiumsData().getProduct10ECPremium().getSumInsured(),
                    rate.apply(insured.getAgeAtSubscription()),
                    quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()));
        } else {
            premiumsData.getProduct10ECPremium().setSumInsured(getSumInsuredFromPremium(
                    quote.getPremiumsData().getFinancialScheduler().getModalAmount(),
                    rate.apply(insured.getAgeAtSubscription()),
                    quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()));
        }

        // cannot insure too much or not enough
        checkSumInsured(premiumsData, PRODUCT_10_EC_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        checkPremium(premiumsData);

        // calculates yearly cash backs
        premiumsData.getProduct10ECPremium().setYearlyCashBacks(calculateDatedAmount(quote, null, dvdRate));

        // calculates yearly returns
        premiumsData.getProduct10ECPremium().setEndOfContractBenefitsMinimum(calculateDatedAmount(quote, 20, dvdRate));
        premiumsData.getProduct10ECPremium().setEndOfContractBenefitsAverage(calculateDatedAmount(quote, 40, dvdRate));
        premiumsData.getProduct10ECPremium().setEndOfContractBenefitsMaximum(calculateDatedAmount(quote, 45, dvdRate));

        // calculates yearly returns
        premiumsData.getProduct10ECPremium().setYearlyCashBacksAverageDividende(calculateDatedAmount(quote, null, averageExtraDvdRate));
        premiumsData.getProduct10ECPremium().setYearlyCashBacksMaximumDividende(calculateDatedAmount(quote, null, maximumExtraDvdRate));

        // calculates yearly benefits
        premiumsData.getProduct10ECPremium().setYearlyCashBacksAverageBenefit(calculateDatedAmount(quote, 40, averageExtraDvdRate));
        premiumsData.getProduct10ECPremium().setYearlyCashBacksMaximumBenefit(calculateDatedAmount(quote, 45, maximumExtraDvdRate));

        // calculate tax deduction
        premiumsData.getProduct10ECPremium().setYearlyTaxDeduction(calculateTaxReturn(quote));

        if (!has10ECCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_10_EC_NAME);
            quote.addCoverage(coverage);
        }
    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) {
        // check for mandatory data
        checkCommonData(getCommonData());
        checkInsured(quote);

        // There is only one insured at this point
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        checkInsuredAge(insured, MIN_AGE, MAX_AGE);
        checkMainInsured(insured);
        checkMainInsured10ECSpecific(insured);

        // Recalculate the quote
        calculateQuote(quote, null);

        // check for calculated data
        checkCoverage(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        checkBeneficiaries(insured, coverage.getBeneficiaries());
        check10ECPremiumsData(quote.getPremiumsData(), insured.getStartDate());

        // Copy from quote to Policy
        policy.setQuoteId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
        policy.addCoverage(SerializationUtils.clone(coverage));
        policy.addInsured(SerializationUtils.clone(insured));

        // Add payment schedule
        addPayments(policy, DURATION_PAYMENT_IN_YEAR);
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
        commonData.setProductId(PRODUCT_10_EC.getName());
        commonData.setProductCurrency(PRODUCT_10_EC_CURRENCY);
        commonData.setProductName(PRODUCT_10_EC_NAME);
        return commonData;
    }

    @Override
    public ProductAmounts getProductAmounts(ProductQuotation productQuotation) {
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(getCommonData());
        if (productQuotation.getDateOfBirth() == null || productQuotation.getPeriodicityCode() == null) {
            return productAmounts;
        }

        Double interestRate = rate.apply(getAge(productQuotation.getDateOfBirth()));
        Double factor = modalFactor.apply(productQuotation.getPeriodicityCode());
        productAmounts.setMaxPremium(amount(SUM_INSURED_MAX * factor * interestRate / 1000));
        productAmounts.setMaxSumInsured(amount(SUM_INSURED_MAX));
        productAmounts.setMinPremium(amount(SUM_INSURED_MIN * factor * interestRate / 1000));
        productAmounts.setMinSumInsured(amount(SUM_INSURED_MIN));
        return productAmounts;
    }

    @Override
    public PremiumsData getPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        Product10ECPremium product10ECPremium = new Product10ECPremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProduct10ECPremium(product10ECPremium);

        return premiumsData;
    }

    private static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        if (quote.getPremiumsData().getProduct10ECPremium() != null) {
            quote.getPremiumsData().getProduct10ECPremium().setYearlyCashBacksAverageBenefit(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setYearlyCashBacksAverageDividende(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setEndOfContractBenefitsAverage(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setYearlyCashBacksMaximumBenefit(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setYearlyCashBacksMaximumDividende(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setEndOfContractBenefitsMaximum(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setEndOfContractBenefitsMinimum(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setYearlyCashBacks(new ArrayList<>());
            quote.getPremiumsData().getProduct10ECPremium().setYearlyTaxDeduction(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    public static void checkMainInsured10ECSpecific(Insured insured) {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), mainInsuredWithNoDeclaredTax);
    }

    private static void checkSumInsured(PremiumsData premiumsData, String currency, Double sumInsuredMin, Double sumInsuredMax) {
        if (premiumsData.getProduct10ECPremium().getSumInsured() == null || premiumsData.getProduct10ECPremium().getSumInsured().getValue() == null) {
            // no amount to check
            return;
        }
        isEqual(currency, premiumsData.getProduct10ECPremium().getSumInsured().getCurrencyCode(), sumInsuredCurrencyException.apply(currency));
        isFalse(premiumsData.getProduct10ECPremium().getSumInsured().getValue() > sumInsuredMax, sumInsuredTooHighException.apply(sumInsuredMax));
        isFalse(premiumsData.getProduct10ECPremium().getSumInsured().getValue() < sumInsuredMin, sumInsuredTooLowException.apply(sumInsuredMin));
    }

    private static void checkCommonData(CommonData commonData) {
        isEqual(commonData.getProductId(), PRODUCT_10_EC.getName(), product10ECExpected);
        isEqual(commonData.getProductName(), PRODUCT_10_EC_NAME, product10ECExpected);
    }

    private static void check10ECPremiumsData(PremiumsData premiumsData, LocalDate startDate) {
        notNull(premiumsData, premiumnsDataNone);
        notNull(premiumsData.getProduct10ECPremium(), premiumnsDataNone);
        notNull(premiumsData.getProduct10ECPremium().getSumInsured(), premiumnsDataNoSumInsured);
        notNull(premiumsData.getProduct10ECPremium().getSumInsured().getCurrencyCode(), premiumnsSumInsuredNoCurrency);
        notNull(premiumsData.getProduct10ECPremium().getSumInsured().getValue(), premiumnsSumInsuredNoAmount);

        checkSumInsured(premiumsData, PRODUCT_10_EC_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getEndOfContractBenefitsAverage(), startDate);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getEndOfContractBenefitsMaximum(), startDate);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getEndOfContractBenefitsMinimum(), startDate);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getYearlyCashBacks(), startDate);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getYearlyCashBacksAverageBenefit(), startDate);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getYearlyCashBacksAverageDividende(), startDate);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getYearlyCashBacksMaximumBenefit(), startDate);
        checkDatedAmounts(premiumsData.getProduct10ECPremium().getYearlyCashBacksMaximumDividende(), startDate);
    }

    private static void checkPremium(PremiumsData premiumsData) {
        if (premiumsData.getFinancialScheduler().getModalAmount() == null || premiumsData.getFinancialScheduler().getModalAmount().getValue() == null) {
            // no amount to check
            return;
        }

        isEqual(PRODUCT_10_EC_CURRENCY, premiumsData.getFinancialScheduler().getModalAmount().getCurrencyCode(), premiumCurrencyException.apply(PRODUCT_10_EC_CURRENCY));
        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() > PREMIUM_MAX, premiumTooHighException.apply(PREMIUM_MAX));
        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() < PREMIUM_MIN, premiumTooLowException.apply(PREMIUM_MIN));
    }

    private static void checkDatedAmounts(List<DatedAmount> datedAmounts, LocalDate startDate) {
        List<LocalDate> allowedDates = new ArrayList<>();
        IntStream.range(0, 10).forEach(value -> allowedDates.add(startDate.plusYears(value + 1)));
        List<LocalDate> filteredDates = datedAmounts.stream().map(DatedAmount::getDate).filter(date -> !allowedDates.contains(date)).collect(toList());

        notNull(datedAmounts, premiumnsCalculatedAmountEmpty);
        isEqual(datedAmounts.size(), DURATION_COVERAGE_IN_YEAR, premiumnsCalculatedAmountNotFor10Years);
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getCurrencyCode() == null), premiumnsCalculatedAmountNoCurrency);
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getDate() == null), premiumnsCalculatedAmountNoDate);
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getDate().isBefore(LocalDate.now())), premiumnsCalculatedAmountDateInThePast);
        notNull(datedAmounts.stream().anyMatch(datedAmount -> datedAmount.getValue() == null), premiumnsCalculatedAmountNoAmount);
        isEqual(filteredDates.size(), 0, premiumnsCalculatedAmountInvalidDate.apply(filteredDates.stream().map(LocalDate::toString).collect(joining(", "))));
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, Function<Integer, Integer> dvdFunction) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getProduct10ECPremium().getSumInsured();
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= DURATION_COVERAGE_IN_YEAR; i++) {
            Double interest = sumInsured.getValue() * dvdFunction.apply(i) / 1000;
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setCurrencyCode(sumInsured.getCurrencyCode());
            datedAmount.setDate(startDate.plusYears(i));
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

        return amount(Math.min(100000.0, Math.round(premium * taxRate / 100 * nbOfPaymentsPerYear)));
    }

    private static boolean hasEnoughTocalculate(ProductQuotation productQuotation) {
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

    private static Amount amount(Double value) {
        Amount amount = new Amount();
        amount.setCurrencyCode(PRODUCT_10_EC_CURRENCY);
        amount.setValue(value);
        return amount;
    }
}
