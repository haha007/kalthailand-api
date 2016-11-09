package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.SessionQuote;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.SessionQuoteRepository;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

import javax.inject.Inject;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteServiceTest extends ELifeTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private SessionQuoteRepository sessionQuoteRepository;
    @Inject
    private QuoteFactory quoteFactory;
    @Inject
    private PolicyFactory policyFactory;

    @Test
    public void should_be_able_to_update_a_10ec_quote() {
        String sessionId = randomNumeric(20);
        QuoteFactory.QuoteResult quoteResult = createQuote(sessionId, ProductType.PRODUCT_10_EC);

        quoteService.updateProfessionNameAndCheckBlackList(quoteResult.getQuote(), quoteResult.getAccessToken());
        assertThat(quoteResult.getQuote()).isNotNull();
    }

    @Test
    public void should_be_able_to_update_a_iBegin_quote() {
        String sessionId = randomNumeric(20);
        QuoteFactory.QuoteResult quoteResult = createQuote(sessionId, ProductType.PRODUCT_IBEGIN);

        quoteService.updateProfessionNameAndCheckBlackList(quoteResult.getQuote(), quoteResult.getAccessToken());
        assertThat(quoteResult.getQuote()).isNotNull();
    }

    @Test
    public void should_be_able_to_update_a_iFine_quote() {
        String sessionId = randomNumeric(20);
        QuoteFactory.QuoteResult quoteResult = createQuote(sessionId, ProductType.PRODUCT_IFINE);

        quoteService.updateProfessionNameAndCheckBlackList(quoteResult.getQuote(), quoteResult.getAccessToken());
        assertThat(quoteResult.getQuote()).isNotNull();
    }

    @Test
    public void should_assign_session_id_to_insured_person_line_id_when_channel_type_is_line() {
        String sessionId = randomNumeric(20);
        Quote quote = quoteService.createQuote(sessionId, ChannelType.LINE, ProductQuotationFactory.construct10ECDefault());

        assertThat(quote.getInsureds().get(0).getPerson().getLineId()).isEqualTo(sessionId);
    }

    @Test
    public void should_find_by_quote_id_and_session_id() {
        String sessionId = randomNumeric(20);
        QuoteFactory.QuoteResult quoteResult = createQuote(sessionId, ProductType.PRODUCT_IGEN);
        Quote quote = quoteResult.getQuote();
        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, ChannelType.LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isTrue();
        assertThat(savedQuote.get()).isNotNull();
    }

    @Test
    public void should_not_find_by_quote_id_when_session_id_has_no_access_to_quote() {
        QuoteFactory.QuoteResult quoteResult = createQuote(randomNumeric(20), ProductType.PRODUCT_IGEN);
        Quote quote = quoteResult.getQuote();
        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), "FAKE_SESSION", ChannelType.LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isFalse();
    }

    @Test
    public void should_add_one_quote_in_session() {
        String sessionId = randomNumeric(20);
        QuoteFactory.QuoteResult quoteResult = createQuote(sessionId, ProductType.PRODUCT_IGEN);
        Quote quote = quoteResult.getQuote();

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, ChannelType.LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote);
    }

    @Test
    public void should_add_two_quotes_in_session_and_ordered_by_update_time() {
        String sessionId = randomNumeric(20);
        Quote quote1 = createQuote(sessionId, ProductType.PRODUCT_IGEN).getQuote();
        Quote quote2 = createQuote(sessionId, ProductType.PRODUCT_IGEN).getQuote();

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
        createQuote(sessionId, ProductType.PRODUCT_IGEN);
        Quote quote2 = createQuote(sessionId, ProductType.PRODUCT_IGEN).getQuote();

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, ChannelType.LINE);
        assertThat(quote.get()).isEqualTo(quote2);
    }

    @Test
    public void should_get_latest_quote_that_has_not_been_transformed_into_policy() {
        String sessionId = randomNumeric(20);
        Quote quote1 = createQuote(sessionId, ProductType.PRODUCT_IGEN).getQuote();
        Quote quote2 = createQuote(sessionId, ProductType.PRODUCT_IGEN).getQuote();
        policyService.createPolicy(quote2);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, ChannelType.LINE);
        assertThat(quote.get()).isEqualTo(quote1);
    }

    @Test
    public void should_calculate_age_of_insured() {
        QuoteFactory.QuoteResult quoteResult = createQuote(randomNumeric(20), ProductType.PRODUCT_IGEN);
        Quote quote = quoteResult.getQuote();
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, quoteResult.getAccessToken());
        assertThat(quote.getInsureds().get(0).getAgeAtSubscription()).isGreaterThan(0);
    }

    @Test
    public void should_set_profession_name_from_profession_id() {
        QuoteFactory.QuoteResult quoteResult = createQuote(randomNumeric(20), ProductType.PRODUCT_IGEN);
        Quote quote = quoteResult.getQuote();
        quote.getInsureds().get(0).setProfessionId(1);

        quote = quoteService.updateProfessionNameAndCheckBlackList(quoteResult.getQuote(), quoteResult.getAccessToken());
        assertThat(quote.getInsureds().get(0).getProfessionName()).isNotNull();
    }

    @Test
    public void should_forbid_blacklisted_thai_id() {
        QuoteFactory.QuoteResult quoteResult = createQuote(randomNumeric(20), ProductType.PRODUCT_IGEN);
        Quote quote = quoteResult.getQuote();

        quote.getInsureds().get(0).getPerson().getRegistrations().get(0).setId("aMockedBlackListedThaiID");

        assertThatThrownBy(() -> quoteService.updateProfessionNameAndCheckBlackList(quote, "token"))
                .isInstanceOf(ElifeException.class);
    }

    @Test
    public void should_return_quote_object_with_object_not_in_product_quotation_set_to_null() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, ProductQuotationFactory.constructIGenDefault());
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

    private QuoteFactory.QuoteResult createQuote(String sessionId, ProductType productType) {
        return quoteFactory.createQuote(sessionId, ProductQuotationFactory.constructDefault(productType), TestUtil.DUMMY_EMAIL);
    }
}
