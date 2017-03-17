package th.co.krungthaiaxa.api.elife.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.QuoteMid;
import th.co.krungthaiaxa.api.elife.model.QuoteCount;
import th.co.krungthaiaxa.api.elife.model.SessionQuoteCount;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepositoryExtends;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

@Service
public class QuoteCountForAllProductsService {
    private final static Logger logger = LoggerFactory.getLogger(QuoteCountForAllProductsService.class);
    private final QuoteRepository quoteRepository;
    private final SessionQuoteService sessionQuoteService;
    private final QuoteRepositoryExtends quoteRepositoryExtend;

    @Inject
    public QuoteCountForAllProductsService(QuoteRepository quoteRepository, SessionQuoteService sessionQuoteService,
                                           QuoteRepositoryExtends quoteRepositoryExtend) {
        this.quoteRepository = quoteRepository;
        this.sessionQuoteService = sessionQuoteService;
        this.quoteRepositoryExtend = quoteRepositoryExtend;
    }

    public List<QuoteCount> countQuotesOfAllProducts(LocalDateTime startDate, LocalDateTime endDate) {
        List<QuoteCount> result = Arrays.asList(ProductType.values()).stream()
                .map(productType -> countQuotes(productType, startDate, endDate))
                .collect(Collectors.toList());
        return result;
    }

    public QuoteCount countQuotes(ProductType productType, LocalDateTime startDate, LocalDateTime endDate) {
        QuoteCount result = new QuoteCount();
        SessionQuoteCount sessionQuoteCount = sessionQuoteService.countSessionQuotes(productType, startDate, endDate);
        long sessionQuoteCountNumber = sessionQuoteCount.getQuoteCount();
        long quoteCount = quoteRepository.countByProductIdAndStartDateInRange(productType.getLogicName(), startDate, endDate);
        result.setProductId(productType.getLogicName());
        result.setSessionQuoteCount(sessionQuoteCountNumber);
        result.setQuoteCount(quoteCount);
        return result;
    }

    //TODO not refactor yet.
    public byte[] exportTotalQuotesCountReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<QuoteCount> listTotalQuoteCount = countQuotesOfAllProducts(startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Session Quotes Counts");

        ExcelUtils.appendRow(sheet,
                text("Product"),
                text("Session Quotes"),
                text("Quotes"));
        listTotalQuoteCount.stream().forEach(quoteCount -> createTotalQuoteCountExtractExcelFileLine(sheet, quoteCount));
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

    //TODO not refactor yet.
    public byte[] exportTotalQuotesByProductTypeReport(ProductType productType, LocalDateTime startDate, LocalDateTime endDate) {
        List<QuoteCount> listTotalQuoteCount = Collections.singletonList(countQuotes(productType, startDate, endDate));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Session Quotes Counts");

        ExcelUtils.appendRow(sheet,
                text("Product"),
                text("Session Quotes"),
                text("Quotes"));
        listTotalQuoteCount.stream().forEach(quoteCount -> createTotalQuoteCountExtractExcelFileLine(sheet, quoteCount));
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

    private void createTotalQuoteCountExtractExcelFileLine(Sheet sheet, QuoteCount quoteCount) {
        ExcelUtils.appendRow(sheet,
                text(quoteCount.getProductId()),
                ExcelUtils.integer(quoteCount.getSessionQuoteCount()),
                ExcelUtils.integer(quoteCount.getQuoteCount())
        );
    }

    public byte[] exportSessionQuotesMIDReport(final List<ProductType> productTypes,
                                               final LocalDateTime startDate,
                                               final LocalDateTime endDate) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Session Quotes MID");

        ExcelUtils.appendRow(sheet,
                text("Product"),
                text("MID"),
                text("DateTime"));
        final List<QuoteMid> quotes = quoteRepositoryExtend.getDistinctQuoteMid(
                productTypes,
                startDate, endDate);

        for (QuoteMid quote : quotes) {
            ExcelUtils.appendRow(sheet,
                    text(quote.getProductId()),
                    text(quote.getMid()),
                    text(quote.getCreationDate())
            );
        }
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
}
