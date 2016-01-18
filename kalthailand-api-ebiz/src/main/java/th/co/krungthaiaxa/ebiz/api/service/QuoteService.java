package th.co.krungthaiaxa.ebiz.api.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.SessionType;
import th.co.krungthaiaxa.ebiz.api.repository.QuoteRepository;
import th.co.krungthaiaxa.ebiz.api.repository.SessionQuoteRepository;

import javax.inject.Inject;

@Service
public class QuoteService {

    private final SessionQuoteRepository sessionQuoteRepository;
    private final QuoteRepository quoteRepository;

    @Inject
    public QuoteService(SessionQuoteRepository sessionQuoteRepository, QuoteRepository quoteRepository) {
        this.sessionQuoteRepository = sessionQuoteRepository;
        this.quoteRepository = quoteRepository;
    }

    public Quote createQuote(String sessionId, SessionType sessionType) {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndSessionType(sessionId, sessionType);

        boolean userHasSavedQuote = sessionQuote != null
                        && sessionQuote.getQuoteId() != null
                        && quoteRepository.findOne(sessionQuote.getQuoteId()) !=null;
        Quote quote;
        if (!userHasSavedQuote) {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            financialScheduler.setPeriodicity(new Periodicity());

            PremiumsData premiumsData = new PremiumsData();
            premiumsData.setFinancialScheduler(financialScheduler);

            Insured insured = new Insured();
            insured.setMainInsuredIndicator(true);
            insured.setFatca(new Fatca());
            insured.setPerson(new Person());

            quote = new Quote();
            quote.setCommonData(new QuoteCommonData());
            quote.setPremiumsData(premiumsData);
            quote.addInsured(insured);
            quote = quoteRepository.save(quote);

            sessionQuote = new SessionQuote();
            sessionQuote.setSessionId(sessionId);
            sessionQuote.setSessionType(sessionType);
            sessionQuote.setQuoteId(quote.getQuoteId());
            sessionQuoteRepository.save(sessionQuote);
        }
        else {
            quote = quoteRepository.findOne(sessionQuote.getQuoteId());
        }

        return quote;
    }

    public Quote updateQuote(Quote quote) throws Exception {
        return quoteRepository.save(quote);
    }
}
