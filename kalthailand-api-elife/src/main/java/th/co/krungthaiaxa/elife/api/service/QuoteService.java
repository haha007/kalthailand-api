package th.co.krungthaiaxa.elife.api.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.InsuredType;
import th.co.krungthaiaxa.elife.api.products.Product;
import th.co.krungthaiaxa.elife.api.products.ProductFactory;
import th.co.krungthaiaxa.elife.api.products.ProductQuotation;
import th.co.krungthaiaxa.elife.api.repository.QuoteRepository;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;

@Service
public class QuoteService {
    private final SessionQuoteRepository sessionQuoteRepository;
    private final QuoteRepository quoteRepository;
    private final ProductFactory productFactory;

    @Inject
    public QuoteService(SessionQuoteRepository sessionQuoteRepository, QuoteRepository quoteRepository, ProductFactory productFactory) {
        this.sessionQuoteRepository = sessionQuoteRepository;
        this.quoteRepository = quoteRepository;
        this.productFactory = productFactory;
    }

    public Optional<Quote> getLatestQuote(String sessionId, ChannelType channelType) {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, channelType);
        if (sessionQuote == null) {
            return Optional.empty();
        }

        // should return latest quote that is not finished, meaning that hasn't been transformed into a Policy
        return sessionQuote.getQuotes().stream()
                .filter(quote1 -> quote1 != null)
                .filter(quote1 -> quote1.getPolicyId() == null)
                .findFirst();
    }

    public Quote createQuote(String sessionId, ChannelType channelType, ProductQuotation productQuotation) {
        Product product = productFactory.getProduct(productQuotation.getProductType().getName());

        Person person = new Person();
        if (LINE.equals(channelType)) {
            person.setLineId(sessionId);
        }

        Insured insured = new Insured();
        insured.setMainInsuredIndicator(true);
        insured.setFatca(new Fatca());
        insured.setHealthStatus(new HealthStatus());
        insured.setPerson(person);
        insured.setType(InsuredType.Insured);

        Quote quote = new Quote();
        LocalDateTime now = now(of(SHORT_IDS.get("VST")));
        quote.setCreationDateTime(now);
        quote.setLastUpdateDateTime(now);
        quote.setQuoteId(randomNumeric(20));
        quote.setCommonData(product.getCommonData());
        quote.setPremiumsData(product.getPremiumData());
        quote.addInsured(insured);

        //calculate
        product.calculateQuote(quote, productQuotation);

        quote = quoteRepository.save(quote);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, channelType);
        if (sessionQuote == null) {
            sessionQuote = new SessionQuote();
            sessionQuote.setSessionId(sessionId);
            sessionQuote.setChannelType(channelType);
        }
        sessionQuote.addQuote(quote);
        sessionQuoteRepository.save(sessionQuote);

        return quote;
    }

    public Quote updateQuote(Quote quote) {
        quote.setLastUpdateDateTime(now(of(SHORT_IDS.get("VST"))));
        return quoteRepository.save(quote);
    }

    public Optional<Quote> findByQuoteId(String quoteId, String sessionId, ChannelType channelType) {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, channelType);
        if (sessionQuote == null || sessionQuote.getQuotes() == null) {
            return Optional.empty();
        }
        return sessionQuote.getQuotes().stream()
                .filter(quote -> quote.getQuoteId().equals(quoteId))
                .findFirst();
    }
}
