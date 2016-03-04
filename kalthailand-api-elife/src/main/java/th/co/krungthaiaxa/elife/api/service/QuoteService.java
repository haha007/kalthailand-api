package th.co.krungthaiaxa.elife.api.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.InsuredType;
import th.co.krungthaiaxa.elife.api.products.Product;
import th.co.krungthaiaxa.elife.api.products.ProductFactory;
import th.co.krungthaiaxa.elife.api.products.ProductQuotation;
import th.co.krungthaiaxa.elife.api.repository.QuoteRepository;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static th.co.krungthaiaxa.elife.api.products.ProductFactory.getProduct;


@Service
public class QuoteService {
    private final SessionQuoteRepository sessionQuoteRepository;
    private final QuoteRepository quoteRepository;

    @Inject
    public QuoteService(SessionQuoteRepository sessionQuoteRepository, QuoteRepository quoteRepository) {
        this.sessionQuoteRepository = sessionQuoteRepository;
        this.quoteRepository = quoteRepository;
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

    public Quote createQuote(String sessionId, ChannelType channelType, ProductQuotation productQuotation) throws QuoteCalculationException {
        Product product = getProduct(productQuotation.getProductType().getName());

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setLifeInsurance(new LifeInsurance());

        Insured insured = new Insured();
        insured.setMainInsuredIndicator(true);
        insured.setFatca(new Fatca());
        insured.setHealthStatus(new HealthStatus());
        insured.setPerson(new Person());
        insured.setType(InsuredType.Insured);

        Quote quote = new Quote();
        LocalDateTime now = now(of(SHORT_IDS.get("VST")));
        quote.setCreationDateTime(now);
        quote.setLastUpdateDateTime(now);
        quote.setQuoteId(randomNumeric(20));
        quote.setCommonData(product.getCommonData());
        quote.setPremiumsData(premiumsData);
        quote.addInsured(insured);

        // copy data already gathered in ProductQuotation
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        quote.getInsureds().get(0).getPerson().setBirthDate(productQuotation.getDateOfBirth());
        quote.getInsureds().get(0).setAgeAtSubscription(getAge(productQuotation.getDateOfBirth()));
        quote.getInsureds().get(0).getPerson().setGenderCode(productQuotation.getGenderCode());
        quote.getInsureds().get(0).setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            Amount amount = new Amount();
            amount.setCurrencyCode(productQuotation.getSumInsuredAmount().getCurrencyCode());
            amount.setValue(productQuotation.getSumInsuredAmount().getValue());
            quote.getPremiumsData().getLifeInsurance().setSumInsured(amount);
        } else {
            Amount amount = new Amount();
            amount.setCurrencyCode(productQuotation.getPremiumAmount().getCurrencyCode());
            amount.setValue(productQuotation.getPremiumAmount().getValue());
            quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);
        }

        //calculate
        product.calculateQuote(quote);

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

    public Quote updateQuote(Quote quote) throws QuoteCalculationException {
        // common calculation
        quote = basicCalculateQuote(quote);

        Product product = ProductFactory.getProduct(quote.getCommonData().getProductId());
        product.calculateQuote(quote);
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
