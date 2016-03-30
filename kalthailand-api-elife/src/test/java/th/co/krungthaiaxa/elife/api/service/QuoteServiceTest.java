package th.co.krungthaiaxa.elife.api.service;

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
import th.co.krungthaiaxa.elife.api.products.ProductQuotation;
import th.co.krungthaiaxa.elife.api.products.ProductType;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_10_EC;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IBEGIN;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteServiceTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private SessionQuoteRepository sessionQuoteRepository;

    @Test
    public void should_be_able_to_update_a_10ec_quote() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, PRODUCT_10_EC);

        quoteService.updateQuote(quote);
        assertThat(quote).isNotNull();
    }

    @Test
    public void should_be_able_to_update_a_iBegin_quote() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, PRODUCT_IBEGIN);

        quoteService.updateQuote(quote);
        assertThat(quote).isNotNull();
    }

    @Test
    public void should_be_able_to_update_a_iFine_quote() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, PRODUCT_IFINE);

        quoteService.updateQuote(quote);
        assertThat(quote).isNotNull();
    }

    @Test
    public void should_assign_session_id_to_insured_person_line_id_when_channel_type_is_line() {
        String sessionId = randomNumeric(20);
        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation(PRODUCT_10_EC, 55, EVERY_MONTH, 100000.0));

        assertThat(quote.getInsureds().get(0).getPerson().getLineId()).isEqualTo(sessionId);
    }

    @Test
    public void should_find_by_quote_id_and_session_id() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, PRODUCT_10_EC);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isTrue();
        assertThat(savedQuote.get()).isNotNull();
    }

    @Test
    public void should_not_find_by_quote_id_when_session_id_has_no_access_to_quote() {
        Quote quote = getQuote(randomNumeric(20), PRODUCT_10_EC);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), "something", LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isFalse();
    }

    @Test
    public void should_add_one_quote_in_session() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, PRODUCT_10_EC);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote);
    }

    @Test
    public void should_add_two_quotes_in_session_and_ordered_by_update_time() {
        String sessionId = randomNumeric(20);
        Quote quote1 = getQuote(sessionId, PRODUCT_10_EC);
        Quote quote2 = getQuote(sessionId, PRODUCT_10_EC);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote2, quote1);
    }

    @Test
    public void should_not_get_latest_quote() {
        Optional<Quote> quote = quoteService.getLatestQuote(randomNumeric(20), LINE);
        assertThat(quote.isPresent()).isFalse();
    }

    @Test
    public void should_get_latest_quote() {
        String sessionId = randomNumeric(20);
        getQuote(sessionId, PRODUCT_10_EC);
        Quote quote2 = getQuote(sessionId, PRODUCT_10_EC);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, LINE);
        assertThat(quote.get()).isEqualTo(quote2);
    }

    @Test
    public void should_get_latest_quote_that_has_not_been_transformed_into_policy() {
        String sessionId = randomNumeric(20);
        Quote quote1 = getQuote(sessionId, PRODUCT_10_EC);
        Quote quote2 = getQuote(sessionId, PRODUCT_10_EC);
        policyService.createPolicy(quote2);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, LINE);
        assertThat(quote.get()).isEqualTo(quote1);
    }

    @Test
    public void should_calculate_age_of_insured() {
        Quote quote = getQuote(randomNumeric(20), PRODUCT_10_EC);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().getProduct10ECPremium().setSumInsured(amount);

        quote = quoteService.updateQuote(quote);
        assertThat(quote.getInsureds().get(0).getAgeAtSubscription()).isEqualTo(55);
    }

    @Test
    public void should_return_quote_object_with_object_not_in_product_quotation_set_to_null() {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        assertThat(quote).isNotNull();
        assertThat(quote.getCommonData()).isNotNull();
        assertThat(quote.getInsureds()).hasSize(1);
        assertThat(quote.getInsureds().get(0)).isNotNull();
        assertThat(quote.getInsureds().get(0).getFatca()).isNotNull();
        assertThat(quote.getInsureds().get(0).getPerson()).isNotNull();
        assertThat(quote.getPremiumsData()).isNotNull();
        assertThat(quote.getPremiumsData().getFinancialScheduler()).isNotNull();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity()).isNotNull();
    }

    @Test
    public void should_return_quote_object_with_object_in_product_quotation_with_default_values() {
        ProductQuotation productQuotation = productQuotation();

        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation);
        assertThat(quote.getInsureds().get(0).getPerson().getBirthDate()).isEqualTo(productQuotation.getDateOfBirth());
        assertThat(quote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(productQuotation.getGenderCode());
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(productQuotation.getPeriodicityCode());
        assertThat(quote.getPremiumsData().getProduct10ECPremium().getSumInsured()).isEqualTo(productQuotation.getSumInsuredAmount());
    }

    private Quote getQuote(String sessionId, ProductType productType) {
        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation(productType, 55, EVERY_MONTH, 100000.0));
        quote(quote, beneficiary(100.0));
        return quoteService.updateQuote(quote);
    }
}
