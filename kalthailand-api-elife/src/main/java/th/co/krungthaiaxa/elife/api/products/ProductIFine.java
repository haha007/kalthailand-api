package th.co.krungthaiaxa.elife.api.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.CommonData;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.repository.ProductIFineRateRepository;

import javax.inject.Inject;

@Component
public class ProductIFine implements Product {
    public final static Integer DURATION_COVERAGE_IN_YEAR = 10;
    public final static Integer DURATION_PAYMENT_IN_YEAR = 10;
    public final static String PRODUCT_IBEGIN_ID = "iFine";
    public final static String PRODUCT_IBEGIN_NAME = "Product iFine";
    public final static String PRODUCT_IBEGIN_CURRENCY = "THB";
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 300000.0;
    public static final Double PREMIUM_MIN = null;
    public static final Double PREMIUM_MAX = null;
    public static final int MAX_AGE = 59;
    public static final int MIN_AGE = 18;

    @Inject
    private ProductIFineRateRepository productIFineRateRepository;

    @Override
    public void calculateQuote(Quote quote) throws QuoteCalculationException {

    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) throws PolicyValidationException, QuoteCalculationException {

    }

    @Override
    public CommonData getCommonData() {
        return null;
    }

    @Override
    public ProductAmounts getProductAmounts(ProductQuotation productQuotation) {
        return null;
    }
}
