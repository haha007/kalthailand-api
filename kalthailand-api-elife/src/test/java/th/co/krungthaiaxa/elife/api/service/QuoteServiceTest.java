package th.co.krungthaiaxa.elife.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Amount;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.products.ProductQuotation;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;

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
    public void should_find_by_quote_id_and_session_id() throws QuoteCalculationException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isTrue();
        assertThat(savedQuote.get()).isNotNull();
    }

    @Test
    public void should_not_find_by_quote_id_when_session_id_has_no_access_to_quote() throws QuoteCalculationException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Optional<Quote> savedQuote = quoteService.findByQuoteId(quote.getQuoteId(), "something", LINE);
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.isPresent()).isFalse();
    }

    @Test
    public void should_add_one_quote_in_session() throws QuoteCalculationException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote);
    }

    @Test
    public void should_add_two_quotes_in_session_and_ordered_by_update_time() throws QuoteCalculationException {
        String sessionId = randomNumeric(20);

        Quote quote1 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote1, insured(35), beneficiary(100.0));
        quote1 = quoteService.updateQuote(quote1);
        Quote quote2 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote2, insured(35), beneficiary(100.0));
        quote2 = quoteService.updateQuote(quote2);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, LINE);
        assertThat(sessionQuote.getQuotes()).containsExactly(quote2, quote1);
    }

    @Test
    public void should_not_get_latest_quote() throws Exception {
        String sessionId = randomNumeric(20);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, LINE);
        assertThat(quote.isPresent()).isFalse();
    }

    @Test
    public void should_get_latest_quote() throws QuoteCalculationException {
        String sessionId = randomNumeric(20);

        Quote quote1 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote1, insured(35), beneficiary(100.0));
        quoteService.updateQuote(quote1);
        Quote quote2 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote2, insured(35), beneficiary(100.0));
        quote2 = quoteService.updateQuote(quote2);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, LINE);
        assertThat(quote.get()).isEqualTo(quote2);
    }

    @Test
    public void should_get_latest_quote_that_has_not_been_transformed_into_policy() throws QuoteCalculationException, PolicyValidationException {
        String sessionId = randomNumeric(20);

        Quote quote1 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote1, insured(35), beneficiary(100.0));
        quoteService.updateQuote(quote1);
        Quote quote2 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote2, insured(35), beneficiary(100.0));
        quote2 = quoteService.updateQuote(quote2);
        policyService.createPolicy(quote2);

        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, LINE);
        assertThat(quote.get()).isEqualTo(quote1);
    }

    @Test
    public void should_calculate_age_of_insured() throws QuoteCalculationException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote, insured(35), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().getLifeInsurance().setSumInsured(amount);

        quote = quoteService.updateQuote(quote);
        assertThat(quote.getInsureds().get(0).getAgeAtSubscription()).isEqualTo(35);
    }

    @Test
    public void should_return_empty_calculated_stuff_when_there_is_nothing_to_calculate_anymore() throws QuoteCalculationException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote, insured(35), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().getLifeInsurance().setSumInsured(amount);

        quote = quoteService.updateQuote(quote);
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacks()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMinimum()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsAverage()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMaximum()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageBenefit()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageDividende()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumBenefit()).hasSize(10);
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumDividende()).hasSize(10);
        assertThat(quote.getCoverages()).hasSize(1);
        assertThat(quote.getCommonData().getProductId()).isNotNull();
        assertThat(quote.getCommonData().getProductName()).isNotNull();

        quote.getPremiumsData().getLifeInsurance().setSumInsured(null);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(null);
        quote = quoteService.updateQuote(quote);
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacks()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMinimum()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsAverage()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMaximum()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageBenefit()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageDividende()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumBenefit()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumDividende()).isEmpty();
        assertThat(quote.getPremiumsData().getLifeInsurance().getYearlyCashBacks()).isEmpty();
        assertThat(quote.getCoverages()).isEmpty();
    }

    @Test
    public void should_return_quote_object_with_object_not_in_product_quotation_set_to_null() throws QuoteCalculationException {
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
    public void should_return_quote_object_with_object_in_product_quotation_with_default_values() throws QuoteCalculationException {
        ProductQuotation productQuotation = productQuotation();

        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation);
        assertThat(quote.getInsureds().get(0).getPerson().getBirthDate()).isEqualTo(productQuotation.getDateOfBirth());
        assertThat(quote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(productQuotation.getGenderCode());
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(productQuotation.getPeriodicityCode());
        assertThat(quote.getPremiumsData().getLifeInsurance().getSumInsured()).isEqualTo(productQuotation.getSumInsuredAmount());
    }
}
