package th.co.krungthaiaxa.api.elife.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import th.co.krungthaiaxa.api.elife.repository.QuoteCriteriaRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;
import th.co.krungthaiaxa.api.elife.repository.SessionQuoteRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

@Service
public class QuoteService {
    private final static Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private final SessionQuoteRepository sessionQuoteRepository;
    private final QuoteRepository quoteRepository;
    private final ProductServiceFactory productServiceFactory;
    private final OccupationTypeRepository occupationTypeRepository;
    private final BlackListClient blackListClient;
    private final QuoteCriteriaRepository quoteCriteriaRepository;

    @Inject
    public QuoteService(SessionQuoteRepository sessionQuoteRepository,
            QuoteRepository quoteRepository,
            ProductServiceFactory productServiceFactory,
            OccupationTypeRepository occupationTypeRepository,
            BlackListClient blackListClient,
            QuoteCriteriaRepository quoteCriteriaRepository
    ) {
        this.sessionQuoteRepository = sessionQuoteRepository;
        this.quoteRepository = quoteRepository;
        this.productServiceFactory = productServiceFactory;
        this.occupationTypeRepository = occupationTypeRepository;
        this.blackListClient = blackListClient;
        this.quoteCriteriaRepository = quoteCriteriaRepository;
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
        ProductService productService = productServiceFactory.getProduct(productQuotation.getProductType().getName());

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
        quote.setCommonData(productService.initCommonData());
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

        quote.setLastUpdateDateTime(now(of(SHORT_IDS.get("VST"))));
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

    public Map<String, Object> getTotalQuoteCount(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("content", quoteCriteriaRepository.quoteCount(startDate, endDate));
        return responseMap;
    }

    public byte[] downloadTotalQuoteCount(LocalDate startDate, LocalDate endDate, String nowString) {
        List<Map<String, Object>> listTotalQuoteCount = quoteCriteriaRepository.quoteCount(startDate, endDate);

        String now = nowString;
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("TotalQuoteCountExtract_" + now);

        ExcelUtils.appendRow(sheet,
                text("Product"),
                text("Total Quotes"));
        listTotalQuoteCount.stream().forEach(tmp -> createTotalQuoteCountExtractExcelFileLine(sheet, tmp));
        ExcelUtils.autoWidthAllColumns(workbook);

        byte[] content;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            content = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel total quote count file", e);
        }

        return content;
    }

    private static class TotalQuoteResult {
        private String content;
    }

    private void createTotalQuoteCountExtractExcelFileLine(Sheet sheet, Map<String, Object> m) {
        ExcelUtils.appendRow(sheet, text("" + m.get("productId")), text("" + m.get("quoteCount")));
    }
}
