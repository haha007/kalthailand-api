package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;
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
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.repository.ProductIGenRateRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isFalse;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.products.ProductUtils.amountTHB;

@Component
public class ProductIProtect implements Product {
    public final static int DURATION_COVERAGE_IN_YEAR = 10;
    public final static int DURATION_PAYMENT_IN_YEAR = 6;
    public final static String PRODUCT_NAME = "Product iProtect";
    public final static String PRODUCT_CURRENCY = "THB";
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 1000000.0;
    public static final Double PREMIUM_MIN = 2682.0;
    public static final Double PREMIUM_MAX = 308000.0;
    public static final int MAX_AGE = 70;
    public static final int MIN_AGE = 20;

    @Inject
    private ProductIGenRateRepository productIGenRateRepository;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        if (productQuotation == null) {
            return;
        }

        Optional<Coverage> hasIFineCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!ProductUtils.hasEnoughTocalculateFor10ECOrIGen(productQuotation)) {
            // we need to delete what might have been calculated before
            resetCalculatedStuff(quote, hasIFineCoverage);
            return;
        }

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // copy data already gathered in ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        insured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        insured.setAgeAtSubscription(ProductUtils.getAge(productQuotation.getDateOfBirth()));
        insured.getPerson().setGenderCode(productQuotation.getGenderCode());
        insured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            quote.getPremiumsData().getProductIGenPremium().setSumInsured(amountTHB(productQuotation.getSumInsuredAmount().getValue()));
            quote.getPremiumsData().getProductIGenPremium().setSumInsuredOption(TRUE);
        } else {
            quote.getPremiumsData().getFinancialScheduler().setModalAmount(amountTHB(productQuotation.getPremiumAmount().getValue()));
            quote.getPremiumsData().getProductIGenPremium().setSumInsuredOption(FALSE);
        }

        // cannot be too young or too old
        ProductUtils.checkInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        LocalDate startDate = LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST")));
        insured.setStartDate(startDate);
        insured.setEndDate(startDate.plusYears(DURATION_COVERAGE_IN_YEAR));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(startDate.plusYears(DURATION_PAYMENT_IN_YEAR));

        // get rates from mongoDB
        ProductIGenRate productIGenRate = productIGenRateRepository.findByGender(insured.getPerson().getGenderCode().name());
        Double rate = productIGenRate.getRate().get(insured.getAgeAtSubscription() - MIN_AGE);

        PremiumsData premiumsData = quote.getPremiumsData();
        // calculates premium / sum insured
        if (premiumsData.getProductIGenPremium().getSumInsured() != null) {
            premiumsData.getFinancialScheduler().setModalAmount(ProductUtils.getPremiumFromSumInsured(
                    quote.getPremiumsData().getProductIGenPremium().getSumInsured(),
                    rate,
                    quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()));
        } else {
            premiumsData.getProductIGenPremium().setSumInsured(ProductUtils.getSumInsuredFromPremium(
                    quote.getPremiumsData().getFinancialScheduler().getModalAmount(),
                    rate,
                    quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()));
        }

        // cannot insure too much or not enough
        checkSumInsured(premiumsData, PRODUCT_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        checkPremium(premiumsData);

        // calculates yearly cash backs
        premiumsData.getProductIGenPremium().setYearlyCashBacks(calculateDatedAmount(quote, null, productIGenRate.getDvdRate()));

        // calculates yearly returns
        premiumsData.getProductIGenPremium().setEndOfContractBenefitsMinimum(calculateDatedAmount(quote, 20, productIGenRate.getDvdRate()));
        premiumsData.getProductIGenPremium().setEndOfContractBenefitsAverage(calculateDatedAmount(quote, 40, productIGenRate.getDvdRate()));
        premiumsData.getProductIGenPremium().setEndOfContractBenefitsMaximum(calculateDatedAmount(quote, 45, productIGenRate.getDvdRate()));

        // calculates yearly returns
        premiumsData.getProductIGenPremium().setYearlyCashBacksAverageDividende(calculateDatedAmount(quote, null, productIGenRate.getAverageExtraDvdRate()));
        premiumsData.getProductIGenPremium().setYearlyCashBacksMaximumDividende(calculateDatedAmount(quote, null, productIGenRate.getMaximumExtraDvdRate()));

        // calculates yearly benefits
        premiumsData.getProductIGenPremium().setYearlyCashBacksAverageBenefit(calculateDatedAmount(quote, 40, productIGenRate.getAverageExtraDvdRate()));
        premiumsData.getProductIGenPremium().setYearlyCashBacksMaximumBenefit(calculateDatedAmount(quote, 45, productIGenRate.getMaximumExtraDvdRate()));

        // calculate tax deduction
        premiumsData.getProductIGenPremium().setYearlyTaxDeduction(ProductUtils.calculateTaxReturnFor10ECOrIGen(quote, PRODUCT_CURRENCY));

        if (!hasIFineCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_NAME);
            quote.addCoverage(coverage);
        }
    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) {
        // check for mandatory data
        checkCommonData(getCommonData());
        ProductUtils.checkInsured(quote);

        // There is only one insured at this point
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        ProductUtils.checkInsuredAge(insured, MIN_AGE, MAX_AGE);
        ProductUtils.checkMainInsured(insured);
        checkMainInsuredIGenSpecific(insured);

        // Recalculate the quote
        calculateQuote(quote, null);

        // check for calculated data
        ProductUtils.checkCoverage(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        ProductUtils.checkBeneficiaries(insured, coverage.getBeneficiaries());
        checkIGenPremiumsData(quote.getPremiumsData(), insured.getStartDate());

        // Copy from quote to Policy
        policy.setQuoteId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
        policy.addCoverage(SerializationUtils.clone(coverage));
        policy.addInsured(SerializationUtils.clone(insured));

        // Add payment schedule
        ProductUtils.addPayments(policy, DURATION_PAYMENT_IN_YEAR);
    }

    @Override
    public CommonData getCommonData() {
        CommonData commonData = new CommonData();
        commonData.setMaxAge(MAX_AGE);
        commonData.setMaxPremium(amountTHB(PREMIUM_MAX));
        commonData.setMaxSumInsured(amountTHB(SUM_INSURED_MAX));
        commonData.setMinAge(MIN_AGE);
        commonData.setMinPremium(amountTHB(PREMIUM_MIN));
        commonData.setMinSumInsured(amountTHB(SUM_INSURED_MIN));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        commonData.setProductId(ProductType.PRODUCT_IGEN.getName());
        commonData.setProductCurrency(PRODUCT_CURRENCY);
        commonData.setProductName(PRODUCT_NAME);
        return commonData;
    }

    @Override
    public ProductAmounts getProductAmounts(ProductQuotation productQuotation) {
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(getCommonData());
        if (productQuotation.getDateOfBirth() == null || productQuotation.getPeriodicityCode() == null) {
            return productAmounts;
        }

        // get rates from mongoDB
        ProductIGenRate productIGenRate = productIGenRateRepository.findByGender(productQuotation.getGenderCode().name());
        Double interestRate = productIGenRate.getRate().get(ProductUtils.getAge(productQuotation.getDateOfBirth()) - MIN_AGE);
        Double factor = ProductUtils.modalFactor.apply(productQuotation.getPeriodicityCode());
        productAmounts.setMaxPremium(amountTHB(SUM_INSURED_MAX * factor * interestRate / 1000));
        productAmounts.setMaxSumInsured(amountTHB(SUM_INSURED_MAX));
        productAmounts.setMinPremium(amountTHB(SUM_INSURED_MIN * factor * interestRate / 1000));
        productAmounts.setMinSumInsured(amountTHB(SUM_INSURED_MIN));
        return productAmounts;
    }

    @Override
    public PremiumsData getPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        ProductIGenPremium productIGenPremium = new ProductIGenPremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductIGenPremium(productIGenPremium);

        return premiumsData;
    }

    private static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        if (quote.getPremiumsData().getProductIGenPremium() != null) {
            quote.getPremiumsData().getProductIGenPremium().setYearlyCashBacksAverageBenefit(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setYearlyCashBacksAverageDividende(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setEndOfContractBenefitsAverage(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setYearlyCashBacksMaximumBenefit(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setYearlyCashBacksMaximumDividende(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setEndOfContractBenefitsMaximum(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setEndOfContractBenefitsMinimum(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setYearlyCashBacks(new ArrayList<>());
            quote.getPremiumsData().getProductIGenPremium().setYearlyTaxDeduction(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    public static void checkMainInsuredIGenSpecific(Insured insured) {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), PolicyValidationException.mainInsuredWithNoDeclaredTax);
    }

    private static void checkSumInsured(PremiumsData premiumsData, String currency, Double sumInsuredMin, Double sumInsuredMax) {
        if (premiumsData.getProductIGenPremium().getSumInsured() == null || premiumsData.getProductIGenPremium().getSumInsured().getValue() == null) {
            // no amount to check
            return;
        }
        isEqual(currency, premiumsData.getProductIGenPremium().getSumInsured().getCurrencyCode(), QuoteCalculationException.sumInsuredCurrencyException.apply(currency));
        isFalse(premiumsData.getProductIGenPremium().getSumInsured().getValue() > sumInsuredMax, QuoteCalculationException.sumInsuredTooHighException.apply(sumInsuredMax));
        isFalse(premiumsData.getProductIGenPremium().getSumInsured().getValue() < sumInsuredMin, QuoteCalculationException.sumInsuredTooLowException.apply(sumInsuredMin));
    }

    private static void checkCommonData(CommonData commonData) {
        isEqual(commonData.getProductId(), ProductType.PRODUCT_IGEN.getName(), PolicyValidationException.productIGenExpected);
        isEqual(commonData.getProductName(), PRODUCT_NAME, PolicyValidationException.productIGenExpected);
    }

    private static void checkIGenPremiumsData(PremiumsData premiumsData, LocalDate startDate) {
        notNull(premiumsData, PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProductIGenPremium(), PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProductIGenPremium().getSumInsured(), PolicyValidationException.premiumnsDataNoSumInsured);
        notNull(premiumsData.getProductIGenPremium().getSumInsured().getCurrencyCode(), PolicyValidationException.premiumnsSumInsuredNoCurrency);
        notNull(premiumsData.getProductIGenPremium().getSumInsured().getValue(), PolicyValidationException.premiumnsSumInsuredNoAmount);

        checkSumInsured(premiumsData, PRODUCT_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        ProductIGenPremium productIGenPremium = premiumsData.getProductIGenPremium();
        ProductUtils.checkDatedAmounts(productIGenPremium.getEndOfContractBenefitsAverage(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productIGenPremium.getEndOfContractBenefitsMaximum(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productIGenPremium.getEndOfContractBenefitsMinimum(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productIGenPremium.getYearlyCashBacks(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productIGenPremium.getYearlyCashBacksAverageBenefit(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productIGenPremium.getYearlyCashBacksAverageDividende(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productIGenPremium.getYearlyCashBacksMaximumBenefit(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productIGenPremium.getYearlyCashBacksMaximumDividende(), startDate, DURATION_COVERAGE_IN_YEAR);
    }

    private static void checkPremium(PremiumsData premiumsData) {
        if (premiumsData.getFinancialScheduler().getModalAmount() == null || premiumsData.getFinancialScheduler().getModalAmount().getValue() == null) {
            // no amount to check
            return;
        }

        isEqual(PRODUCT_CURRENCY, premiumsData.getFinancialScheduler().getModalAmount().getCurrencyCode(), QuoteCalculationException.premiumCurrencyException.apply(PRODUCT_CURRENCY));
        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() > PREMIUM_MAX, QuoteCalculationException.premiumTooHighException.apply(PREMIUM_MAX));
        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() < PREMIUM_MIN, QuoteCalculationException.premiumTooLowException.apply(PREMIUM_MIN));
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, List<Double> dividends) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getProductIGenPremium().getSumInsured();
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= DURATION_COVERAGE_IN_YEAR; i++) {
            Double interest = sumInsured.getValue() * dividends.get(i - 1) / 1000;
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
