package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.client.BlackListClient;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.data.SessionQuote;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Fatca;
import th.co.krungthaiaxa.api.elife.model.HealthStatus;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.InsuredType;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductServiceFactory;
import th.co.krungthaiaxa.api.elife.repository.OccupationTypeRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;
import th.co.krungthaiaxa.api.elife.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@Service
public class QuoteService {
    private final static Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private final SessionQuoteRepository sessionQuoteRepository;
    private final QuoteRepository quoteRepository;
    private final ProductServiceFactory productServiceFactory;
    private final OccupationTypeRepository occupationTypeRepository;
    private final BlackListClient blackListClient;

    @Inject
    public QuoteService(SessionQuoteRepository sessionQuoteRepository,
            QuoteRepository quoteRepository,
            ProductServiceFactory productServiceFactory,
            OccupationTypeRepository occupationTypeRepository,
            BlackListClient blackListClient
    ) {
        this.sessionQuoteRepository = sessionQuoteRepository;
        this.quoteRepository = quoteRepository;
        this.productServiceFactory = productServiceFactory;
        this.occupationTypeRepository = occupationTypeRepository;
        this.blackListClient = blackListClient;
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
        ProductService productService = productServiceFactory.getProduct(productQuotation.getProductType().getLogicName());

        Person person = new Person();
        if (ChannelType.LINE.equals(channelType)) {
            person.setLineId(sessionId);
        }

        Insured mainInsured = new Insured();
        mainInsured.setMainInsuredIndicator(true);
        mainInsured.setFatca(new Fatca());
        mainInsured.setHealthStatus(new HealthStatus());
        mainInsured.setPerson(person);
        mainInsured.setType(InsuredType.Insured);

        Quote quote = new Quote();
        LocalDateTime now = DateTimeUtil.nowLocalDateTimeInThaiZoneId();
        quote.setCreationDateTime(now);
        quote.setLastUpdateDateTime(now);
        quote.setQuoteId(randomNumeric(20));
        quote.setCommonData(productService.initCommonData(productQuotation));
        quote.setPremiumsData(productService.initPremiumData());
        quote.addInsured(mainInsured);

        //calculate
        productService.calculateQuote(quote, productQuotation);

        quote = quoteRepository.save(quote);

        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, channelType);
        if (sessionQuote == null) {
            sessionQuote = new SessionQuote();
            sessionQuote.setSessionId(sessionId);
            sessionQuote.setChannelType(channelType);
        }
        sessionQuote.addQuote(quote);
        sessionQuoteRepository.save(sessionQuote);

        logger.info("Quote has been successfully created with id [" + quote.getId() + "] and quoteId [" + quote.getQuoteId() + "]");

        return quote;
    }

    public Quote updateQuote(Quote quote, String token) {
        // For some products, professionId is given after quote calculation
        // In this case, profession name has to be calculated
        if (quote.getInsureds().get(0).getProfessionId() != null) {
            OccupationType occupationType = occupationTypeRepository.findByOccId(quote.getInsureds().get(0).getProfessionId());
            quote.getInsureds().get(0).setProfessionName(occupationType.getOccTextTh());
        }

        // Some Thai ID are black listed and Policy / Quote should be forbidden
        if (quote.getInsureds().get(0).getPerson().getRegistrations() != null) {
            Optional<String> insuredRegistrationId = quote.getInsureds().get(0).getPerson().getRegistrations().stream()
                    .filter(registration -> registration.getTypeName().equals(RegistrationTypeName.THAI_ID_NUMBER))
                    .map(Registration::getId)
                    .findFirst();
            if (insuredRegistrationId.isPresent()) {
                Boolean blackListed = blackListClient.getCheckingBlackListed(insuredRegistrationId.get(), token);
                if (blackListed) {
                    throw new ElifeException("The Thai ID [" + insuredRegistrationId.get() + "] is not allowed to purchase Policy.");
                }
            }
        }

        quote.setLastUpdateDateTime(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        logger.info("Quote with id [" + quote.getId() + "] and quoteId [" + quote.getQuoteId() + "] has been successfully updated");
        return quoteRepository.save(quote);
    }

    public Optional<Quote> findByQuoteId(String quoteId, String sessionId, ChannelType channelType) {
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, channelType);
        if (sessionQuote == null || sessionQuote.getQuotes() == null) {
            logger.error("There is no quote with id [" + quoteId + "] for the session id [" + sessionId + "]");
            return Optional.empty();
        }
        return sessionQuote.getQuotes().stream()
                .filter(quote -> quote.getQuoteId().equals(quoteId))
                .findFirst();
    }
}
