package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class QuoteFactory {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    public Quote createDefaultQuoteForLine(int age, String email) {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation(age, PeriodicityCode.EVERY_MONTH));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        PersonFactory.setValuesToFirstInsuredPerson(quote, age, "DefaultName", email);
        quote = quoteService.updateQuote(quote, "token");
        return quote;
    }
}
