package th.co.krungthaiaxa.elife.api.service;


import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Amount;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.products.Product10EC;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.products.Product10EC.getCommonData;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
public class QuoteServiceTest {

    @Inject
    private QuoteService quoteService;

    @Test
    public void should_calculate_age_of_insured() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), ChannelType.LINE);
        quote.getInsureds().get(0).getPerson().setBirthDate(LocalDate.now().minus(35, ChronoUnit.YEARS));
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(PeriodicityCode.EVERY_YEAR);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = quoteService.updateQuote(quote);
        assertThat(quote.getInsureds().get(0).getAgeAtSubscription()).isEqualTo(35);
    }

    @Test
    public void should_return_empty_calculated_stuff_when_there_is_nothing_to_calculate_anymore() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), ChannelType.LINE);
        quote.getInsureds().get(0).getPerson().setBirthDate(LocalDate.now().minus(35, ChronoUnit.YEARS));
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(PeriodicityCode.EVERY_YEAR);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = quoteService.updateQuote(quote);
        assertThat(quote.getPremiumsData().getLifeInsuranceYearlyCashBacks()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsuranceMinimumYearlyReturns()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsuranceAverageYearlyReturns()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsuranceMaximumYearlyReturns()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsuranceMinimumExtraDividende()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsuranceMaximumExtraDividende()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsuranceYearlyCashBacks()).hasSize(10);
        Assertions.assertThat(quote.getCoverages()).hasSize(1);
        assertThat(quote.getCommonData().getProductId()).isNotNull();
        assertThat(quote.getCommonData().getProductName()).isNotNull();

        quote.getPremiumsData().setLifeInsuranceSumInsured(null);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(null);
        quote = quoteService.updateQuote(quote);
        assertThat(quote.getPremiumsData().getLifeInsuranceYearlyCashBacks()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsuranceMinimumYearlyReturns()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsuranceAverageYearlyReturns()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsuranceMaximumYearlyReturns()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsuranceMinimumExtraDividende()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsuranceMaximumExtraDividende()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsuranceYearlyCashBacks()).isEmpty();
        Assertions.assertThat(quote.getCoverages()).isEmpty();
    }

    @Test
    public void should_return_empty_quote_object_with_every_object_set_to_null() {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), ChannelType.LINE);
        assertThat(quote).isNotNull();
        assertThat(quote.getCommonData()).isNotNull();
        Assertions.assertThat(quote.getInsureds()).hasSize(1);
        assertThat(quote.getInsureds().get(0)).isNotNull();
        assertThat(quote.getInsureds().get(0).getFatca()).isNotNull();
        assertThat(quote.getInsureds().get(0).getPerson()).isNotNull();
        assertThat(quote.getPremiumsData()).isNotNull();
        assertThat(quote.getPremiumsData().getFinancialScheduler()).isNotNull();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity()).isNotNull();
    }
}
