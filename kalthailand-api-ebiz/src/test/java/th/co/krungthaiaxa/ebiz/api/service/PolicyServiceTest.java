package th.co.krungthaiaxa.ebiz.api.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.ebiz.api.KalApiApplication;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.Amount;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType;
import th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException.emptyQuote;
import static th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException.noneExistingQuote;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
public class PolicyServiceTest {

    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_return_error_when_create_policy_if_quote_not_provided() throws Exception {
        assertThatThrownBy(() -> policyService.createPolicy(null))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(emptyQuote.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_if_quote_does_not_exist() throws Exception {
        Quote quote = new Quote();
        quote.setTechnicalId("123");
        assertThatThrownBy(() -> policyService.createPolicy(quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noneExistingQuote.getMessage());
    }

    @Test
    public void should_add_generated_ids_when_saving_policy_for_first_time() throws Exception {
        Quote quote = quoteService.createQuote("123", ChannelType.LINE);
        updateQuote(quote);
        quote = quoteService.updateQuote(quote);

        Policy policy = policyService.createPolicy(quote);
        assertThat(policy.getPolicyId()).isNotNull();
        assertThat(policy.getTechnicalId()).isNotNull();
    }

    private static void updateQuote(Quote quote) {
        quote.getInsureds().get(0).getPerson().setBirthDate(LocalDate.now().minus(35, ChronoUnit.YEARS));
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(PeriodicityCode.EVERY_YEAR);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);
    }

}
