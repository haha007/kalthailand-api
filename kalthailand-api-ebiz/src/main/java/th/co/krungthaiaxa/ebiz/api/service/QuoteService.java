package th.co.krungthaiaxa.ebiz.api.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.SessionType;
import th.co.krungthaiaxa.ebiz.api.repository.SessionQuoteRepository;

import javax.inject.Inject;

@Service
public class QuoteService {

    private SessionQuoteRepository sessionQuoteRepository;

    @Inject
    public QuoteService(SessionQuoteRepository sessionQuoteRepository) {
        this.sessionQuoteRepository = sessionQuoteRepository;
    }

    public Quote createQuote(String sessionId, SessionType sessionType) {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndSessionType(sessionId, sessionType);

        if (sessionQuote == null) {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            financialScheduler.setPeriodicity(new Periodicity());

            PremiumsData premiumsData = new PremiumsData();
            premiumsData.setFinancialScheduler(financialScheduler);

            Insured insured = new Insured();
            insured.setMainInsuredIndicator(true);
            insured.setFatca(new Fatca());
            insured.setPerson(new Person());

            Quote quote = new Quote();
            quote.setCommonData(new QuoteCommonData());
            quote.setPremiumsData(premiumsData);
            quote.addInsured(insured);

            sessionQuote = new SessionQuote();
            sessionQuote.setSessionId(sessionId);
            sessionQuote.setSessionType(sessionType);
            sessionQuote.setQuote(quote);
        }

        sessionQuote = sessionQuoteRepository.save(sessionQuote);
        return sessionQuote.getQuote();
    }

    public Quote updateQuote(String sessionId, SessionType sessionType, Quote quote) throws Exception {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndSessionType(sessionId, sessionType);

        if (sessionQuote == null) {
            throw new Exception("There is no quote in this session");
        }

        sessionQuote.setQuote(quote);
        sessionQuote = sessionQuoteRepository.save(sessionQuote);

        return sessionQuote.getQuote();
    }
}
