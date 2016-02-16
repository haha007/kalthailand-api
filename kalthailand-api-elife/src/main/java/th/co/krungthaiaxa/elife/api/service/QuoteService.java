package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.InsuredType;
import th.co.krungthaiaxa.elife.api.products.Product;
import th.co.krungthaiaxa.elife.api.products.Product10EC;
import th.co.krungthaiaxa.elife.api.products.ProductFactory;
import th.co.krungthaiaxa.elife.api.repository.QuoteRepository;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class QuoteService {

    private final SessionQuoteRepository sessionQuoteRepository;
    private final QuoteRepository quoteRepository;

    @Inject
    public QuoteService(SessionQuoteRepository sessionQuoteRepository, QuoteRepository quoteRepository) {
        this.sessionQuoteRepository = sessionQuoteRepository;
        this.quoteRepository = quoteRepository;
    }

    public Quote createQuote(String sessionId, CommonData commonData, ChannelType channelType) {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, channelType);

        boolean userHasSavedQuote = sessionQuote != null
                && sessionQuote.getQuoteId() != null
                && quoteRepository.findOne(sessionQuote.getQuoteId()) != null;
        Quote quote;
        if (!userHasSavedQuote) {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            financialScheduler.setPeriodicity(new Periodicity());

            PremiumsDataLifeInsurance premiumsData = new PremiumsDataLifeInsurance();
            premiumsData.setFinancialScheduler(financialScheduler);

            Insured insured = new Insured();
            insured.setMainInsuredIndicator(true);
            insured.setFatca(new Fatca());
            insured.setPerson(new Person());
            insured.setType(InsuredType.Insured);


            quote = new Quote();
            quote.setQuoteId(RandomStringUtils.randomNumeric(20));
            quote.setCommonData(commonData);
            quote.setPremiumsData(premiumsData);
            quote.addInsured(insured);
            quote = quoteRepository.save(quote);

            sessionQuote = new SessionQuote();
            sessionQuote.setSessionId(sessionId);
            sessionQuote.setChannelType(channelType);
            sessionQuote.setQuoteId(quote.getQuoteId());
            sessionQuoteRepository.save(sessionQuote);
        } else {
            quote = quoteRepository.findOne(sessionQuote.getQuoteId());
        }

        return quote;
    }

    public Quote updateQuote(Quote quote) throws Exception {
        // common calculation
        quote = basicCalculateQuote(quote);

        Product product = ProductFactory.getProduct(quote.getCommonData().getProductId());
        quote = product.calculateQuote(quote);

        return quoteRepository.save(quote);
    }

    public Quote findByQuoteId(String quoteId) throws Exception {
        try {
            return quoteRepository.findByQuoteId(quoteId);
        } catch (RuntimeException e) {
            throw new Exception(e);
        }
    }

    private Quote basicCalculateQuote(Quote quote) {
        // calculate age
        quote.getInsureds().stream()
                .filter(insured -> insured != null)
                .filter(insured -> insured.getPerson() != null)
                .filter(insured -> insured.getPerson().getBirthDate() != null)
                .forEach(insured -> insured.setAgeAtSubscription(getAge(insured.getPerson().getBirthDate())));

        return quote;
    }

    private Integer getAge(LocalDate birthDate) {
        return ((Long) ChronoUnit.YEARS.between(birthDate, LocalDate.now())).intValue();
    }
}
