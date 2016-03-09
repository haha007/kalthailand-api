package th.co.krungthaiaxa.elife.api.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.repository.ProductIFineRateRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static th.co.krungthaiaxa.elife.api.products.ProductUtils.checkMainInsuredAge;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.checkSumInsured;

@Component
public class ProductIFine implements Product {
    public final static Integer DURATION_COVERAGE_IN_YEAR = 10;
    public final static Integer DURATION_PAYMENT_IN_YEAR = 10;
    public final static String PRODUCT_IFINE_ID = "iFine";
    public final static String PRODUCT_IFINE_NAME = "Product iFine";
    public final static String PRODUCT_IFINE_CURRENCY = "THB";
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 300000.0;
    public static final Double PREMIUM_MIN = null;
    public static final Double PREMIUM_MAX = null;
    public static final int MAX_AGE = 59;
    public static final int MIN_AGE = 18;

    @Inject
    private ProductIFineRateRepository productIFineRateRepository;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) throws QuoteCalculationException {
        Optional<Coverage> hasIBeginCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_IFINE_ID))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(productQuotation)) {
            // we need to delete what might have been calculated before
//            resetCalculatedStuff(quote, hasIBeginCoverage);
            return;
        }

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // cannot be too young or too old
        checkMainInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        insured.setStartDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        insured.setEndDate(insured.getPerson().getBirthDate().plusYears(90 - insured.getAgeAtSubscription()));
        //TODO this has to change and 5 has to become a parameter from ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().setEndDate(insured.getStartDate().plusYears(5));

        PremiumsData premiumsData = quote.getPremiumsData();
        // cannot insure too much or not enough
        checkSumInsured(premiumsData, PRODUCT_IFINE_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);
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

        // we need an occupation
        boolean hasOccupation = productQuotation.getOccupation() != null;
        if (!hasOccupation) {
            return false;
        }

        // we need the product name
        boolean hasPackageName = productQuotation.getPackageName() != null;
        if (!hasPackageName) {
            return false;
        }

        // We need a periodicity
        return productQuotation.getPeriodicityCode() != null;
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
