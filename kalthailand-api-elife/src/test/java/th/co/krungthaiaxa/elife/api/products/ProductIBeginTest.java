package th.co.krungthaiaxa.elife.api.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Amount;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.model.enums.GenderCode.FEMALE;
import static th.co.krungthaiaxa.elife.api.model.enums.GenderCode.MALE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.*;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IBEGIN;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIBeginTest {
    @Inject
    private ProductIBegin productIBegin;

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_male_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_YEAR, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(62034.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_female_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_YEAR, 200000.0, FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(55494.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_male_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_HALF_YEAR, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(32257.68);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_female_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_HALF_YEAR, 200000.0, FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(28856.88);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarterly_periodicity_male_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_QUARTER, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(16749.18);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarterly_periodicity_female_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_QUARTER, 200000.0, FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(14983.38);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_male_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_MONTH, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(5583.06);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_female_age_50() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 50, EVERY_MONTH, 200000.0, FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(4994.46);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_55() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 55, EVERY_YEAR, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(67502.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_60() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 60, EVERY_YEAR, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(73014.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_65() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 65, EVERY_YEAR, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(78676.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_70() throws Exception {
        Quote quote = quote(productIBegin());
        productIBegin.calculateQuote(quote, productQuotation(PRODUCT_IBEGIN, 70, EVERY_YEAR, 200000.0, MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(84732.0);
    }
}
