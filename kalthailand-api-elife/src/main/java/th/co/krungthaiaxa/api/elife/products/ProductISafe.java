package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.*;
import th.co.krungthaiaxa.api.elife.repository.ProductISafeRateRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.*;

@Component
public class ProductISafe implements Product {
    public final static int DURATION_COVERAGE_IN_YEAR = 10;
    public final static int DURATION_PAYMENT_IN_YEAR = 6;
    public final static String PRODUCT_ISAFE_NAME = "Product iSafe";
    public final static String PRODUCT_ISAFE_CURRENCY = "THB";
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 1000000.0;
    public static final Double PREMIUM_MIN = 2682.0;
    public static final Double PREMIUM_MAX = 308000.0;
    public static final int MAX_AGE = 70;
    public static final int MIN_AGE = 20;

    @Inject
    private ProductISafeRateRepository productISafeRateRepository;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation)  {
        if (productQuotation == null) {
            return;
        }

        Optional<Coverage> hasIFineCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_ISAFE_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!ProductUtils.hasEnoughTocalculateFor10ECOrISafe(productQuotation)) {
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
            quote.getPremiumsData().getProductISafePremium().setSumInsured(amount(productQuotation.getSumInsuredAmount().getValue()));
            quote.getPremiumsData().getProductISafePremium().setSumInsuredOption(TRUE);
        } else {
            quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount(productQuotation.getPremiumAmount().getValue()));
            quote.getPremiumsData().getProductISafePremium().setSumInsuredOption(FALSE);
        }

        // cannot be too young or too old
        ProductUtils.checkInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        LocalDate startDate = LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST")));
        insured.setStartDate(startDate);
        insured.setEndDate(startDate.plusYears(DURATION_COVERAGE_IN_YEAR));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(startDate.plusYears(DURATION_PAYMENT_IN_YEAR));

        // get rates from mongoDB
        ProductISafeRate productISafeRate = productISafeRateRepository.findByGender(insured.getPerson().getGenderCode().name());
        Double rate = productISafeRate.getRate().get(insured.getAgeAtSubscription() - MIN_AGE);

        PremiumsData premiumsData = quote.getPremiumsData();
        // calculates premium / sum insured
        if (premiumsData.getProductISafePremium().getSumInsured() != null) {
            premiumsData.getFinancialScheduler().setModalAmount(ProductUtils.getPremiumFromSumInsured(
                    quote.getPremiumsData().getProductISafePremium().getSumInsured(),
                    rate,
                    quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()));
        } else {
            premiumsData.getProductISafePremium().setSumInsured(ProductUtils.getSumInsuredFromPremium(
                    quote.getPremiumsData().getFinancialScheduler().getModalAmount(),
                    rate,
                    quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()));
        }

        // cannot insure too much or not enough
        checkSumInsured(premiumsData, PRODUCT_ISAFE_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        checkPremium(premiumsData);

        // calculates yearly cash backs
        premiumsData.getProductISafePremium().setYearlyCashBacks(calculateDatedAmount(quote, null, productISafeRate.getDvdRate()));

        // calculates yearly returns
        premiumsData.getProductISafePremium().setEndOfContractBenefitsMinimum(calculateDatedAmount(quote, 20, productISafeRate.getDvdRate()));
        premiumsData.getProductISafePremium().setEndOfContractBenefitsAverage(calculateDatedAmount(quote, 40, productISafeRate.getDvdRate()));
        premiumsData.getProductISafePremium().setEndOfContractBenefitsMaximum(calculateDatedAmount(quote, 45, productISafeRate.getDvdRate()));

        // calculates yearly returns
        premiumsData.getProductISafePremium().setYearlyCashBacksAverageDividende(calculateDatedAmount(quote, null, productISafeRate.getAverageExtraDvdRate()));
        premiumsData.getProductISafePremium().setYearlyCashBacksMaximumDividende(calculateDatedAmount(quote, null, productISafeRate.getMaximumExtraDvdRate()));

        // calculates yearly benefits
        premiumsData.getProductISafePremium().setYearlyCashBacksAverageBenefit(calculateDatedAmount(quote, 40, productISafeRate.getAverageExtraDvdRate()));
        premiumsData.getProductISafePremium().setYearlyCashBacksMaximumBenefit(calculateDatedAmount(quote, 45, productISafeRate.getMaximumExtraDvdRate()));

        // calculate tax deduction
        premiumsData.getProductISafePremium().setYearlyTaxDeduction(ProductUtils.calculateTaxReturnFor10ECOrISafe(quote, PRODUCT_ISAFE_CURRENCY));

        if (!hasIFineCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_ISAFE_NAME);
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
        checkMainInsuredISafeSpecific(insured);

        // Recalculate the quote
        calculateQuote(quote, null);

        // check for calculated data
        ProductUtils.checkCoverage(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        ProductUtils.checkBeneficiaries(insured, coverage.getBeneficiaries());
        checkISafePremiumsData(quote.getPremiumsData(), insured.getStartDate());

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
        commonData.setMaxPremium(amount(PREMIUM_MAX));
        commonData.setMaxSumInsured(amount(SUM_INSURED_MAX));
        commonData.setMinAge(MIN_AGE);
        commonData.setMinPremium(amount(PREMIUM_MIN));
        commonData.setMinSumInsured(amount(SUM_INSURED_MIN));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        commonData.setProductId(ProductType.PRODUCT_ISAFE.getName());
        commonData.setProductCurrency(PRODUCT_ISAFE_CURRENCY);
        commonData.setProductName(PRODUCT_ISAFE_NAME);
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
        ProductISafeRate productISafeRate = productISafeRateRepository.findByGender(productQuotation.getGenderCode().name());
        Double interestRate = productISafeRate.getRate().get(ProductUtils.getAge(productQuotation.getDateOfBirth()) - MIN_AGE);
        Double factor = ProductUtils.modalFactor.apply(productQuotation.getPeriodicityCode());
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

        ProductISafePremium productISafePremium = new ProductISafePremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductISafePremium(productISafePremium);

        return premiumsData;
    }

    private static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        if (quote.getPremiumsData().getProductISafePremium() != null) {
            quote.getPremiumsData().getProductISafePremium().setYearlyCashBacksAverageBenefit(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setYearlyCashBacksAverageDividende(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setEndOfContractBenefitsAverage(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setYearlyCashBacksMaximumBenefit(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setYearlyCashBacksMaximumDividende(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setEndOfContractBenefitsMaximum(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setEndOfContractBenefitsMinimum(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setYearlyCashBacks(new ArrayList<>());
            quote.getPremiumsData().getProductISafePremium().setYearlyTaxDeduction(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    public static void checkMainInsuredISafeSpecific(Insured insured) {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), PolicyValidationException.mainInsuredWithNoDeclaredTax);
    }

    private static void checkSumInsured(PremiumsData premiumsData, String currency, Double sumInsuredMin, Double sumInsuredMax) {
        if (premiumsData.getProductISafePremium().getSumInsured() == null || premiumsData.getProductISafePremium().getSumInsured().getValue() == null) {
            // no amount to check
            return;
        }
        isEqual(currency, premiumsData.getProductISafePremium().getSumInsured().getCurrencyCode(), QuoteCalculationException.sumInsuredCurrencyException.apply(currency));
        isFalse(premiumsData.getProductISafePremium().getSumInsured().getValue() > sumInsuredMax, QuoteCalculationException.sumInsuredTooHighException.apply(sumInsuredMax));
        isFalse(premiumsData.getProductISafePremium().getSumInsured().getValue() < sumInsuredMin, QuoteCalculationException.sumInsuredTooLowException.apply(sumInsuredMin));
    }

    private static void checkCommonData(CommonData commonData) {
        isEqual(commonData.getProductId(), ProductType.PRODUCT_ISAFE.getName(), PolicyValidationException.productISafeExpected);
        isEqual(commonData.getProductName(), PRODUCT_ISAFE_NAME, PolicyValidationException.productISafeExpected);
    }

    private static void checkISafePremiumsData(PremiumsData premiumsData, LocalDate startDate) {
        notNull(premiumsData, PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProductISafePremium(), PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProductISafePremium().getSumInsured(), PolicyValidationException.premiumnsDataNoSumInsured);
        notNull(premiumsData.getProductISafePremium().getSumInsured().getCurrencyCode(), PolicyValidationException.premiumnsSumInsuredNoCurrency);
        notNull(premiumsData.getProductISafePremium().getSumInsured().getValue(), PolicyValidationException.premiumnsSumInsuredNoAmount);

        checkSumInsured(premiumsData, PRODUCT_ISAFE_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
        ProductISafePremium productISafePremium = premiumsData.getProductISafePremium();
        ProductUtils.checkDatedAmounts(productISafePremium.getEndOfContractBenefitsAverage(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productISafePremium.getEndOfContractBenefitsMaximum(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productISafePremium.getEndOfContractBenefitsMinimum(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productISafePremium.getYearlyCashBacks(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productISafePremium.getYearlyCashBacksAverageBenefit(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productISafePremium.getYearlyCashBacksAverageDividende(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productISafePremium.getYearlyCashBacksMaximumBenefit(), startDate, DURATION_COVERAGE_IN_YEAR);
        ProductUtils.checkDatedAmounts(productISafePremium.getYearlyCashBacksMaximumDividende(), startDate, DURATION_COVERAGE_IN_YEAR);
    }

    private static void checkPremium(PremiumsData premiumsData) {
        if (premiumsData.getFinancialScheduler().getModalAmount() == null || premiumsData.getFinancialScheduler().getModalAmount().getValue() == null) {
            // no amount to check
            return;
        }

        isEqual(PRODUCT_ISAFE_CURRENCY, premiumsData.getFinancialScheduler().getModalAmount().getCurrencyCode(), QuoteCalculationException.premiumCurrencyException.apply(PRODUCT_ISAFE_CURRENCY));
        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() > PREMIUM_MAX, QuoteCalculationException.premiumTooHighException.apply(PREMIUM_MAX));
        isFalse(premiumsData.getFinancialScheduler().getModalAmount().getValue() < PREMIUM_MIN, QuoteCalculationException.premiumTooLowException.apply(PREMIUM_MIN));
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, List<Double> dividends) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getProductISafePremium().getSumInsured();
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

    private static Amount amount(Double value) {
        Amount amount = new Amount();
        amount.setCurrencyCode(PRODUCT_ISAFE_CURRENCY);
        amount.setValue(value);
        return amount;
    }
}
