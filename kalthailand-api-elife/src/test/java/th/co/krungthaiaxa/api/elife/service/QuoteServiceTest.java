package th.co.krungthaiaxa.api.elife.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.SessionQuote;
import th.co.krungthaiaxa.api.elife.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteServiceTest extends ELifeTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private SessionQuoteRepository sessionQuoteRepository;

    @Test
    public void should_be_able_to_update_a_10ec_quote() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, ProductType.PRODUCT_10_EC);

        quoteService.updateQuote(quote, "token");
        assertThat(quote).isNotNull();
    }

    @Test
    public void should_be_able_to_update_a_iBegin_quote() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, ProductType.PRODUCT_IBEGIN);

        quoteService.updateQuote(quote, "token");
        assertThat(quote).isNotNull();
    }

    @Test
    public void should_be_able_to_update_a_iFine_quote() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, ProductType.PRODUCT_IFINE);

        quoteService.updateQuote(quote, "token");
        assertThat(quote).isNotNull();
    }

    @Test
    public void should_assign_session_id_to_insured_person_line_id_when_channel_type_is_line() {
        String sessionId = randomNumeric(20);
        Quote quote = quoteService.createQuote(sessionId, ChannelType.LINE, productQuotation(ProductType.PRODUCT_10_EC, 55, EVERY_MONTH, 100000.0));

        assertThat(quote.getInsureds().get(0).getPerson().getLineId()).isEqualTo(sessionId);
    }

    @Test
    public void should_find_by_quote_id_and_session_id() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, ProductType.PRODUCT_10_EC);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, ChannelType.LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isTrue();
        assertThat(savedQuote.get()).isNotNull();
    }

    @Test
    public void should_not_find_by_quote_id_when_session_id_has_no_access_to_quote() {
        Quote quote = getQuote(randomNumeric(20), ProductType.PRODUCT_10_EC);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), "something", ChannelType.LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isFalse();
    }

    @Test
    public void should_add_one_quote_in_session() {
        String sessionId = randomNumeric(20);
        Quote quote = getQuote(sessionId, ProductType.PRODUCT_10_EC);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, ChannelType.LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote);
    }

    @Test
    public void should_add_two_quotes_in_session_and_ordered_by_update_time() {
        String sessionId = randomNumeric(20);
        Quote quote1 = getQuote(sessionId, ProductType.PRODUCT_10_EC);
        Quote quote2 = getQuote(sessionId, ProductType.PRODUCT_10_EC);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, ChannelType.LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote2, quote1);
    }

    @Test
    public void should_not_get_latest_quote() {
        Optional<Quote> quote = quoteService.getLatestQuote(randomNumeric(20), ChannelType.LINE);
        assertThat(quote.isPresent()).isFalse();
    }

    @Test
    public void should_get_latest_quote() {
        String sessionId = randomNumeric(20);
        getQuote(sessionId, ProductType.PRODUCT_10_EC);
        Quote quote2 = getQuote(sessionId, ProductType.PRODUCT_10_EC);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, ChannelType.LINE);
        assertThat(quote.get()).isEqualTo(quote2);
    }

    @Test
    public void should_get_latest_quote_that_has_not_been_transformed_into_policy() {
        String sessionId = randomNumeric(20);
        Quote quote1 = getQuote(sessionId, ProductType.PRODUCT_10_EC);
        Quote quote2 = getQuote(sessionId, ProductType.PRODUCT_10_EC);
        policyService.createPolicy(quote2);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, ChannelType.LINE);
        assertThat(quote.get()).isEqualTo(quote1);
    }

    @Test
    public void should_calculate_age_of_insured() {
        Quote quote = getQuote(randomNumeric(20), ProductType.PRODUCT_10_EC);

        quote = quoteService.updateQuote(quote, "token");
        assertThat(quote.getInsureds().get(0).getAgeAtSubscription()).isEqualTo(55);
    }

    @Test
    public void should_set_profession_name_from_profession_id() {
        Quote quote = getQuote(randomNumeric(20), ProductType.PRODUCT_10_EC);
        quote.getInsureds().get(0).setProfessionId(1);

        quote = quoteService.updateQuote(quote, "token");
        assertThat(quote.getInsureds().get(0).getProfessionName()).isNotNull();
    }

    @Test
    public void should_forbid_blacklisted_thai_id() {
        Quote quote = getQuote(randomNumeric(20), ProductType.PRODUCT_10_EC);
        quote.getInsureds().get(0).getPerson().getRegistrations().get(0).setId("aMockedBlackListedThaiID");

        assertThatThrownBy(() -> quoteService.updateQuote(quote, "token"))
                .isInstanceOf(ElifeException.class);
    }

    @Test
    public void should_return_quote_object_with_object_not_in_product_quotation_set_to_null() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation());
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
        ProductQuotation productQuotation = TestUtil.productQuotation();

        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation);
        assertThat(quote.getInsureds().get(0).getPerson().getBirthDate()).isEqualTo(productQuotation.getDateOfBirth());
        assertThat(quote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(productQuotation.getGenderCode());
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(productQuotation.getPeriodicityCode());
        assertThat(quote.getPremiumsData().getProduct10ECPremium().getSumInsured()).isEqualTo(productQuotation.getSumInsuredAmount());
    }

    private Quote getQuote(String sessionId, ProductType productType) {
        Quote quote = quoteService.createQuote(sessionId, ChannelType.LINE, productQuotation(productType, 55, EVERY_MONTH, 100000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        return quoteService.updateQuote(quote, "token");
    }
}
