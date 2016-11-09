package th.co.krungthaiaxa.api.elife.test.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_HALF_YEAR;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_QUARTER;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIBeginTest {
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_male_age_50() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_YEAR, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(62034.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_female_age_50() throws Exception {
        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_YEAR, 200000.0, true, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(55494.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_male_age_50() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_HALF_YEAR, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(32257.68);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_female_age_50() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_HALF_YEAR, 200000.0, true, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(28856.88);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarterly_periodicity_male_age_50() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_QUARTER, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(16749.18);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarterly_periodicity_female_age_50() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_QUARTER, 200000.0, true, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(14983.38);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_male_age_50() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_MONTH, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(5583.06);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_female_age_50() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(50, EVERY_MONTH, 200000.0, true, GenderCode.FEMALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(4994.46);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_55() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(55, EVERY_YEAR, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(67502.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_60() throws Exception {

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(60, EVERY_YEAR, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(73014.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_65() throws Exception {
        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(65, EVERY_YEAR, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(78676.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_70() throws Exception {
        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, ProductQuotationFactory.constructIBeginDefault(70, EVERY_YEAR, 200000.0, true, GenderCode.MALE));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(84732.0);
    }
}
