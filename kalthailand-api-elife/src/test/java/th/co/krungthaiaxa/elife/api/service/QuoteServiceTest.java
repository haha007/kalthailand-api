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
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.model.Amount;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.products.Product10EC.getCommonData;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteServiceTest {

    @Inject
    private QuoteService quoteService;
    @Inject
    private SessionQuoteRepository sessionQuoteRepository;

    @Test
    public void should_find_by_quote_id_and_session_id() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 100000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isTrue();
        assertThat(savedQuote.get()).isNotNull();
    }

    @Test
    public void should_not_find_by_quote_id_when_session_id_has_no_access_to_quote() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 100000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), "something", LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isFalse();
    }

    @Test
    public void should_add_one_quote_in_session() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 100000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote);
    }

    @Test
    public void should_add_two_quotes_in_session_and_ordered_by_update_time() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote1 = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote(quote1, EVERY_YEAR, 100000.0, insured(35), beneficiary(100.0));
        quote1 = quoteService.updateQuote(quote1);
        Quote quote2 = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote(quote2, EVERY_MONTH, 200000.0, insured(35), beneficiary(100.0));
        quote2 = quoteService.updateQuote(quote2);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote2, quote1);
    }

    @Test
    public void should_not_get_latest_quote() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, LINE);
        assertThat(quote.isPresent()).isFalse();
    }

    @Test
    public void should_get_latest_quote() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote1 = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote(quote1, EVERY_YEAR, 100000.0, insured(35), beneficiary(100.0));
        quoteService.updateQuote(quote1);
        Quote quote2 = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote(quote2, EVERY_MONTH, 200000.0, insured(35), beneficiary(100.0));
        quote2 = quoteService.updateQuote(quote2);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, LINE);
        assertThat(quote.isPresent()).isTrue();
        assertThat(quote.get()).isEqualTo(quote2);
    }

    @Test
    public void should_calculate_age_of_insured() throws Exception {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote.getInsureds().get(0).getPerson().setBirthDate(now().minus(35, YEARS));
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(EVERY_YEAR);

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

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), LINE);
        quote.getInsureds().get(0).getPerson().setBirthDate(now().minus(35, YEARS));
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(EVERY_YEAR);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = quoteService.updateQuote(quote);
        assertThat(quote.getPremiumsData().getYearlyCashBacks()).hasSize(10);
        assertThat(quote.getPremiumsData().getEndOfContractBenefitsMinimum()).hasSize(10);
        assertThat(quote.getPremiumsData().getEndOfContractBenefitsAverage()).hasSize(10);
        assertThat(quote.getPremiumsData().getEndOfContractBenefitsMaximum()).hasSize(10);
        assertThat(quote.getPremiumsData().getYearlyCashBacksMinimumDividende()).hasSize(10);
        assertThat(quote.getPremiumsData().getYearlyCashBacksMaximumDividende()).hasSize(10);
        assertThat(quote.getPremiumsData().getYearlyCashBacks()).hasSize(10);
        Assertions.assertThat(quote.getCoverages()).hasSize(1);
        assertThat(quote.getCommonData().getProductId()).isNotNull();
        assertThat(quote.getCommonData().getProductName()).isNotNull();

        quote.getPremiumsData().setLifeInsuranceSumInsured(null);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(null);
        quote = quoteService.updateQuote(quote);
        assertThat(quote.getPremiumsData().getYearlyCashBacks()).isEmpty();
        assertThat(quote.getPremiumsData().getEndOfContractBenefitsMinimum()).isEmpty();
        assertThat(quote.getPremiumsData().getEndOfContractBenefitsAverage()).isEmpty();
        assertThat(quote.getPremiumsData().getEndOfContractBenefitsMaximum()).isEmpty();
        assertThat(quote.getPremiumsData().getYearlyCashBacksMinimumDividende()).isEmpty();
        assertThat(quote.getPremiumsData().getYearlyCashBacksMaximumDividende()).isEmpty();
        assertThat(quote.getPremiumsData().getYearlyCashBacks()).isEmpty();
        Assertions.assertThat(quote.getCoverages()).isEmpty();
    }

    @Test
    public void should_return_empty_quote_object_with_every_object_set_to_null() {
        String sessionId = RandomStringUtils.randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, getCommonData(), LINE);
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
