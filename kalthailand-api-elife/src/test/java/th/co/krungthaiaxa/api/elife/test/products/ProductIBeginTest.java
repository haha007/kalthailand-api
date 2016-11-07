package th.co.krungthaiaxa.api.elife.test.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.products.ProductIBeginService;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.TestUtil.quote;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIBeginTest {
    @Inject
    private ProductIBeginService productIBegin;

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_male_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, TestUtil.productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_YEAR, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(62034.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_female_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_YEAR, 200000.0, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(55494.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_male_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_HALF_YEAR, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(32257.68);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_female_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_HALF_YEAR, 200000.0, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(28856.88);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarterly_periodicity_male_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_QUARTER, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(16749.18);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarterly_periodicity_female_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_QUARTER, 200000.0, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(14983.38);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_male_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_MONTH, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(5583.06);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_female_age_50() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 50, EVERY_MONTH, 200000.0, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(4994.46);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_55() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 55, EVERY_YEAR, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(67502.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_60() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 60, EVERY_YEAR, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(73014.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_65() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 65, EVERY_YEAR, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(78676.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_70() throws Exception {
        Quote quote = quote(TestUtil.productIBeginService());
        productIBegin.calculateQuote(quote, productQuotation(ProductType.PRODUCT_IBEGIN, 70, EVERY_YEAR, 200000.0, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(84732.0);
    }
}
