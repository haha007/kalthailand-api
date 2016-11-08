package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.SerializationUtils;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.DatedAmount;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.product.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.product.Product10ECPremium;
import th.co.krungthaiaxa.api.elife.products.utils.ProductQuotationUtils;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isTrue;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.addPayments;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.amount;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.getPremiumFromSumInsured;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.getSumInsuredFromPremium;

public class Product10ECService implements ProductService {
    public final static int DURATION_COVERAGE_IN_YEAR = 10;
    public final static int DURATION_PAYMENT_IN_YEAR = 6;
    public final static String PRODUCT_10_EC_NAME = ProductType.PRODUCT_10_EC.getLogicDisplayName();
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
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        if (productQuotation == null) {
            return;
        }

        Optional<Coverage> has10ECCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_10_EC_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!ProductUtils.hasEnoughTocalculateFor10ECOrIGen(productQuotation)) {
            // we need to delete what might have been calculated before
            resetCalculatedStuff(quote, has10ECCoverage);
            return;
        }
        ProductQuotationUtils.validateAtpMode(productQuotation);

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // copy data already gathered in ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        insured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        insured.setAgeAtSubscription(ProductUtils.getAge(productQuotation.getDateOfBirth()));
        insured.getPerson().setGenderCode(productQuotation.getGenderCode());
        insured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            quote.getPremiumsData().getProduct10ECPremium().setSumInsured(amount(productQuotation.getSumInsuredAmount().getValue(), PRODUCT_10_EC_CURRENCY));
            quote.getPremiumsData().getProduct10ECPremium().setSumInsuredOption(TRUE);
        } else {
            quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount(productQuotation.getPremiumAmount().getValue(), PRODUCT_10_EC_CURRENCY));
            quote.getPremiumsData().getProduct10ECPremium().setSumInsuredOption(FALSE);
        }

        // cannot be too young or too old
        ProductUtils.checkInsuredAgeInRange(insured, MIN_AGE, MAX_AGE);

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
        ProductUtils.validatePremiumAmountInRange(premiumsData.getFinancialScheduler().getModalAmount(), PREMIUM_MIN, PREMIUM_MAX);
//        checkPremium(premiumsData);

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
        premiumsData.getProduct10ECPremium().setYearlyTaxDeduction(ProductUtils.calculateTaxReturnFor10ECOrIGen(quote, PRODUCT_10_EC_CURRENCY));

        if (!has10ECCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_10_EC_NAME);
            quote.addCoverage(coverage);
        }
    }

    @Override
    public void createPolicyFromQuote(Policy policy, Quote quote) {
        // check for mandatory data
        checkCommonData(quote.getCommonData());
        ProductUtils.validateMainInsured(quote);

        // There is only one insured at this point
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        ProductUtils.checkInsuredAgeInRange(insured, MIN_AGE, MAX_AGE);
        ProductUtils.checkMainInsured(insured);
        checkMainInsured10ECSpecific(insured);

        // Recalculate the quote
        //TODO this method do nothing!!!
        calculateQuote(quote, null);

        // check for calculated data
        ProductUtils.validateNumberOfCoverages(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        ProductUtils.checkBeneficiaries(insured, coverage.getBeneficiaries());
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
    public CommonData initCommonData(ProductQuotation productQuotation) {
        CommonData commonData = new CommonData();
        commonData.setMaxAge(MAX_AGE);
        commonData.setMaxPremium(amount(PREMIUM_MAX, PRODUCT_10_EC_CURRENCY));
        commonData.setMaxSumInsured(amount(SUM_INSURED_MAX, PRODUCT_10_EC_CURRENCY));
        commonData.setMinAge(MIN_AGE);
        commonData.setMinPremium(amount(PREMIUM_MIN, PRODUCT_10_EC_CURRENCY));
        commonData.setMinSumInsured(amount(SUM_INSURED_MIN, PRODUCT_10_EC_CURRENCY));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        commonData.setProductId(ProductType.PRODUCT_10_EC.getLogicName());
        commonData.setProductCurrency(PRODUCT_10_EC_CURRENCY);
        commonData.setProductName(PRODUCT_10_EC_NAME);
        return commonData;
    }

    @Override
    public ProductAmounts calculateProductAmounts(ProductQuotation productQuotation) {
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(initCommonData(productQuotation));
        if (productQuotation.getDateOfBirth() == null || productQuotation.getPeriodicityCode() == null) {
            return productAmounts;
        }

        Double interestRate = rate.apply(ProductUtils.getAge(productQuotation.getDateOfBirth()));
        Double factor = ProductUtils.modalFactor.apply(productQuotation.getPeriodicityCode());
        productAmounts.setMaxPremium(amount(SUM_INSURED_MAX * factor * interestRate / 1000, PRODUCT_10_EC_CURRENCY));
        productAmounts.setMaxSumInsured(amount(SUM_INSURED_MAX, PRODUCT_10_EC_CURRENCY));
        productAmounts.setMinPremium(amount(SUM_INSURED_MIN * factor * interestRate / 1000, PRODUCT_10_EC_CURRENCY));
        productAmounts.setMinSumInsured(amount(SUM_INSURED_MIN, PRODUCT_10_EC_CURRENCY));
        return productAmounts;
    }

    @Override
    public PremiumsData initPremiumData() {
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
        notNull(insured.getDeclaredTaxPercentAtSubscription(), PolicyValidationException.mainInsuredWithNoDeclaredTax);
    }

    private static void checkSumInsured(PremiumsData premiumsData, String currency, Double sumInsuredMin, Double sumInsuredMax) {
        if (premiumsData.getProduct10ECPremium().getSumInsured() == null || premiumsData.getProduct10ECPremium().getSumInsured().getValue() == null) {
            // no amount to check
            return;
        }
        isEqual(currency, premiumsData.getProduct10ECPremium().getSumInsured().getCurrencyCode(), QuoteCalculationException.sumInsuredCurrencyException.apply(currency));
        Amount sumInsured = premiumsData.getProduct10ECPremium().getSumInsured();
        isTrue(sumInsured.getValue() <= sumInsuredMax, QuoteCalculationException.sumInsuredTooHighException.apply("Maximum: " + sumInsuredMin + ", actual value: " + sumInsured));
        isTrue(sumInsured.getValue() >= sumInsuredMin, QuoteCalculationException.sumInsuredTooLowException.apply("Minimum: " + sumInsuredMin + ", actual value: " + sumInsured));
    }

    private static void checkCommonData(CommonData commonData) {
        isEqual(commonData.getProductId(), ProductType.PRODUCT_10_EC.getLogicName(), PolicyValidationException.product10ECExpected);
//        isTrue(StringUtils.isNotBlank(commonData.getProductName()), PolicyValidationException.product10ECExpected);
    }

    private static void check10ECPremiumsData(PremiumsData premiumsData, LocalDate startDate) {
        notNull(premiumsData, PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProduct10ECPremium(), PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProduct10ECPremium().getSumInsured(), PolicyValidationException.premiumnsDataNoSumInsured);
        notNull(premiumsData.getProduct10ECPremium().getSumInsured().getCurrencyCode(), PolicyValidationException.premiumnsSumInsuredNoCurrency);
        notNull(premiumsData.getProduct10ECPremium().getSumInsured().getValue(), PolicyValidationException.premiumnsSumInsuredNoAmount);

        checkSumInsured(premiumsData, PRODUCT_10_EC_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        Product10ECPremium product10ECPremium = premiumsData.getProduct10ECPremium();
        ProductUtils.checkDatedAmounts(product10ECPremium.getEndOfContractBenefitsAverage(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(product10ECPremium.getEndOfContractBenefitsMaximum(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(product10ECPremium.getEndOfContractBenefitsMinimum(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(product10ECPremium.getYearlyCashBacks(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(product10ECPremium.getYearlyCashBacksAverageBenefit(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(product10ECPremium.getYearlyCashBacksAverageDividende(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(product10ECPremium.getYearlyCashBacksMaximumBenefit(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(product10ECPremium.getYearlyCashBacksMaximumDividende(), startDate, DURATION_COVERAGE_IN_YEAR);
    }

//    private static void checkPremium(PremiumsData premiumsData) {
//        if (premiumsData.getFinancialScheduler().getModalAmount() == null || premiumsData.getFinancialScheduler().getModalAmount().getValue() == null) {
//            // no amount to check
//            return;
//        }
//
//        isEqual(PRODUCT_10_EC_CURRENCY, premiumsData.getFinancialScheduler().getModalAmount().getCurrencyCode(), QuoteCalculationException.premiumCurrencyException.apply(PRODUCT_10_EC_CURRENCY));
//        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() > PREMIUM_MAX, QuoteCalculationException.premiumTooHighException.apply(PREMIUM_MAX));
//        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() < PREMIUM_MIN, QuoteCalculationException.premiumTooLowException.apply(PREMIUM_MIN));
//    }

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
}
