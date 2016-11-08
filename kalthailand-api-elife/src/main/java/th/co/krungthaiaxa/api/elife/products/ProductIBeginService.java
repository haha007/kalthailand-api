package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.product.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.product.ProductIBeginPremium;
import th.co.krungthaiaxa.api.elife.products.utils.ProductQuotationUtils;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.ProductIBeginRateRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.amount;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.amountTHB;

@Component
public class ProductIBeginService implements ProductService {
    public final static Integer DURATION_COVERAGE_IN_YEAR = null;
    public final static Integer DURATION_PAYMENT_IN_YEAR = 10;
    public final static String PRODUCT_IBEGIN_ID = "iBegin";
    public final static String PRODUCT_IBEGIN_NAME = "Product iBegin";
    public final static String PRODUCT_IBEGIN_CURRENCY = "THB";
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 300000.0;
    public static final Double PREMIUM_MIN = null;
    public static final Double PREMIUM_MAX = null;
    public static final int MAX_AGE = 70;
    public static final int MIN_AGE = 50;

    @Inject
    private ProductIBeginRateRepository productIBeginRateRepository;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        if (productQuotation == null) {
            return;
        }

        Optional<Coverage> hasIBeginCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_IBEGIN_ID))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(productQuotation)) {
            // we need to delete what might have been calculated before
            return;
        }

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();
        ProductQuotationUtils.setValidAtpModeToQuote(quote, productQuotation);

        // copy data already gathered in ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        insured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        insured.setAgeAtSubscription(ProductUtils.getAge(productQuotation.getDateOfBirth()));
        insured.getPerson().setGenderCode(productQuotation.getGenderCode());
        insured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            quote.getPremiumsData().getProductIBeginPremium().setSumInsured(
                    amount(
                            productQuotation.getSumInsuredAmount().getValue(),
                            productQuotation.getSumInsuredAmount().getCurrencyCode()
                    ));
        }

        // cannot be too young or too old
        ProductUtils.checkInsuredAgeInRange(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        insured.setStartDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        insured.setEndDate(insured.getPerson().getBirthDate().plusYears(90 - insured.getAgeAtSubscription()));
        //TODO this has to change and 5 has to become a parameter from ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().setEndDate(insured.getStartDate().plusYears(5));

        PremiumsData premiumsData = quote.getPremiumsData();
        // cannot insure too much or not enough
//        checkSumInsured(premiumsData, PRODUCT_IBEGIN_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);

        // calculates premium / sum insured
        //TODO this has to change and iBegin5 has to become a parameter from ProductQuotation
        ProductIBeginRate productIBeginRate = productIBeginRateRepository.findByNbOfYearsOfPaymentAndSumInsured(5, premiumsData.getProductIBeginPremium().getSumInsured().getValue());
        List<Double> rates;
        if (insured.getPerson().getGenderCode().equals(GenderCode.MALE)) {
            rates = productIBeginRate.getMaleRate();
        } else {
            rates = productIBeginRate.getFemaleRate();
        }
        premiumsData.getFinancialScheduler().setModalAmount(ProductUtils.getPremiumFromSumInsured(
                quote.getPremiumsData().getProductIBeginPremium().getSumInsured(),
                rates.get(quote.getInsureds().get(0).getAgeAtSubscription() - MIN_AGE),
                quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()));
    }

    @Override
    public void createPolicyFromQuote(Policy policy, Quote quote) {

    }

    @Override
    public CommonData initCommonData(ProductQuotation productQuotation) {
        CommonData commonData = new CommonData();
        ProductQuotationUtils.validateAtpMode(productQuotation);
        commonData.setMaxAge(MAX_AGE);
        commonData.setMaxPremium(amountTHB(PREMIUM_MAX));
        commonData.setMaxSumInsured(amountTHB(SUM_INSURED_MAX));
        commonData.setMinAge(MIN_AGE);
        commonData.setMinPremium(amountTHB(PREMIUM_MIN));
        commonData.setMinSumInsured(amountTHB(SUM_INSURED_MIN));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        commonData.setProductId(ProductType.PRODUCT_IBEGIN.name());
        commonData.setProductCurrency(PRODUCT_IBEGIN_CURRENCY);
        commonData.setProductName(PRODUCT_IBEGIN_NAME);
        return commonData;
    }

    @Override
    public PremiumsData initPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        ProductIBeginPremium productIBeginPremium = new ProductIBeginPremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductIBeginPremium(productIBeginPremium);

        return premiumsData;
    }

    @Override
    public ProductAmounts calculateProductAmounts(ProductQuotation productQuotation) {
        return null;
    }

    private static boolean hasEnoughTocalculate(ProductQuotation productQuotation) {
        // Do we have a birth date to calculate the age of insured
        boolean hasAnyDateOfBirth = productQuotation.getDateOfBirth() != null;
        if (!hasAnyDateOfBirth) {
            return false;
        }

        // we need a gender
        boolean hasGender = productQuotation.getGenderCode() != null;
        if (!hasGender) {
            return false;
        }

        // we need an amount
        boolean hasAmount = productQuotation.getSumInsuredAmount() != null;
        if (!hasAmount) {
            return false;
        }

        // We need a periodicity
        return productQuotation.getPeriodicityCode() != null;
    }
}
