package th.co.krungthaiaxa.ebiz.api.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType;
import th.co.krungthaiaxa.ebiz.api.products.Product10EC;
import th.co.krungthaiaxa.ebiz.api.repository.QuoteRepository;
import th.co.krungthaiaxa.ebiz.api.repository.SessionQuoteRepository;

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

    public Quote createQuote(String sessionId, ChannelType channelType) {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, channelType);

        boolean userHasSavedQuote = sessionQuote != null
                && sessionQuote.getQuoteTechId() != null
                && quoteRepository.findOne(sessionQuote.getQuoteTechId()) != null;
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

            quote = new Quote();
            quote.setCommonData(new CommonData());
            quote.setPremiumsData(premiumsData);
            quote.addInsured(insured);
            quote = quoteRepository.save(quote);

            sessionQuote = new SessionQuote();
            sessionQuote.setSessionId(sessionId);
            sessionQuote.setChannelType(channelType);
            sessionQuote.setQuoteTechId(quote.getTechnicalId());
            sessionQuoteRepository.save(sessionQuote);
        } else {
            quote = quoteRepository.findOne(sessionQuote.getQuoteTechId());
        }

        return quote;
    }

    public Quote updateQuote(Quote quote) throws Exception {
        // common calculation
        quote = basicCalculateQuote(quote);

        // product specific calculation
        // So far there is only one product
        quote = Product10EC.calculateQuote(quote);

        return quoteRepository.save(quote);
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