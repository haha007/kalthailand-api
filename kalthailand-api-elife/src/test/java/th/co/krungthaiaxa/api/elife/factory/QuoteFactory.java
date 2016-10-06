package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;

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
        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, TestUtil.productQuotation(age, PeriodicityCode.EVERY_MONTH));
        TestUtil.quote(quote, BeneficiaryFactory.constructDefaultBeneficiary());
        PersonFactory.setValuesToFirstInsuredPerson(quote, age, "MockInsuredPerson", email);
        quote = quoteService.updateQuote(quote, RequestFactory.generateAccessToken());
        return quote;
    }

    public QuoteResult createDefaultIGen() {
        return createDefaultIGen(ProductQuotationFactory.DUMMY_EMAIL);
    }

    public QuoteResult createDefaultIGen(String email) {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGenDefault();
        return createQuote(productQuotation, email);
    }

    public QuoteResult createDefaultIProtect() {
        return createDefaultIProtect(ProductQuotationFactory.DUMMY_EMAIL);
    }

    public QuoteResult createDefaultIProtect(String email) {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIProtectDefault();
        return createQuote(productQuotation, email);
    }

    public QuoteResult createQuote(ProductQuotation productQuotation, String email) {
        String sessionId = RequestFactory.generateSession();
        return createQuote(sessionId, productQuotation, email);
    }

    public QuoteResult createQuote(String sessionId, ProductQuotation productQuotation, String email) {
        ChannelType channelType = ChannelType.LINE;
        Quote quote = quoteService.createQuote(sessionId, channelType, productQuotation);
        TestUtil.quote(quote, BeneficiaryFactory.constructDefaultBeneficiary());
        PersonFactory.setValuesToFirstInsuredPerson(quote, "MockInsuredPerson", email);
        quote = quoteService.updateQuote(quote, RequestFactory.generateAccessToken());
        return new QuoteResult(quote, sessionId, channelType);
    }

    public Quote createDefaultIProtectQuoteForLine(int age, String email) {
        PeriodicityCode periodicityCode = PeriodicityCode.EVERY_MONTH;
        int taxPercentage = 35;

        ProductQuotation productQuotation = TestUtil.productQuotation(
                ProductType.PRODUCT_IPROTECT,
                IProtectPackage.IPROTECT10.name(),
                age,
                periodicityCode,
                2000.0, false,
                taxPercentage,
                GenderCode.MALE);

        Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
        TestUtil.quote(quote, BeneficiaryFactory.constructDefaultBeneficiary());
        PersonFactory.setValuesToFirstInsuredPerson(quote, age, "MockInsuredPerson", email);
        quote = quoteService.updateQuote(quote, RequestFactory.generateAccessToken());
        return quote;
    }

    public static class QuoteResult {
        private Quote quote;
        private String sessionId;
        private ChannelType channelType;

        public QuoteResult() {
        }

        public QuoteResult(Quote quote, String sessionId, ChannelType channelType) {
            this.quote = quote;
            this.sessionId = sessionId;
            this.channelType = channelType;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public ChannelType getChannelType() {
            return channelType;
        }

        public void setChannelType(ChannelType channelType) {
            this.channelType = channelType;
        }

        public Quote getQuote() {
            return quote;
        }

        public void setQuote(Quote quote) {
            this.quote = quote;
        }
    }
}
