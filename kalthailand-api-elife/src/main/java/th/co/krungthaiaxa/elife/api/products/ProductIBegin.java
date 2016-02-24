package th.co.krungthaiaxa.elife.api.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.repository.ProductIBeginRateRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.YEARS;
import static th.co.krungthaiaxa.elife.api.model.enums.GenderCode.MALE;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.*;

@Component
public class ProductIBegin implements Product {
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
    public void calculateQuote(Quote quote) throws QuoteCalculationException {
        Optional<Coverage> hasIBeginCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_IBEGIN_ID))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(quote)) {
            // we need to delete what might have been calculated before
            ProductUtils.resetCalculatedStuff(quote, hasIBeginCoverage);
            return;
        }

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // cannot be too young or too old
        checkMainInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        insured.setStartDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        insured.setEndDate(insured.getPerson().getBirthDate().plus(90 - insured.getAgeAtSubscription(), YEARS));
        //TODO this has to change and 5 has to become a parameter from ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().setEndDate(insured.getStartDate().plus(5, YEARS));

        PremiumsData premiumsData = quote.getPremiumsData();
        // cannot insure too much or not enough
        checkSumInsured(premiumsData, PRODUCT_IBEGIN_CURRENCY, SUM_INSURED_MIN, SUM_INSURED_MAX);

        // calculates premium / sum insured
        //TODO this has to change and iBegin5 has to become a parameter from ProductQuotation
        ProductIBeginRate productIBeginRate = productIBeginRateRepository.findByPlanAndSumInsured("iBegin5", premiumsData.getLifeInsurance().getSumInsured().getValue());
        List<Double> rates;
        if (insured.getPerson().getGenderCode().equals(MALE)) {
            rates = productIBeginRate.getMaleRate();
        } else {
            rates = productIBeginRate.getFemaleRate();
        }
        premiumsData.getFinancialScheduler().setModalAmount(getPremiumFromSumInsured(quote, rates.get(quote.getInsureds().get(0).getAgeAtSubscription() - MIN_AGE)));
    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) throws PolicyValidationException, QuoteCalculationException {

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
        commonData.setProductId(PRODUCT_IBEGIN_ID);
        commonData.setProductName(PRODUCT_IBEGIN_NAME);
        return commonData;
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

        // we need a gender
        boolean hasGender = quote.getInsureds().stream()
                .filter(insured -> insured != null)
                .filter(insured -> insured.getPerson() != null)
                .filter(insured -> insured.getPerson().getGenderCode() != null)
                .findFirst()
                .isPresent();
        if (!hasGender) {
            return false;
        }

        // we need an amount
        boolean hasAmount = quote.getPremiumsData().getLifeInsurance().getSumInsured() != null;
        if (!hasAmount) {
            return false;
        }

        // We need a periodicity
        return quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode() != null;
    }

    private static Amount amount(Double value) {
        Amount amount = new Amount();
        amount.setCurrencyCode(PRODUCT_IBEGIN_CURRENCY);
        amount.setValue(value);
        return amount;
    }
}
