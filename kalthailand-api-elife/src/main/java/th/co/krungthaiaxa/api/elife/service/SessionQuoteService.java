package th.co.krungthaiaxa.api.elife.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.SessionQuoteCount;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.SessionQuoteRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

@Service
public class SessionQuoteService {
    private final static Logger logger = LoggerFactory.getLogger(SessionQuoteService.class);
    private final SessionQuoteRepository sessionQuoteRepository;

    @Inject
    public SessionQuoteService(SessionQuoteRepository sessionQuoteRepository) {this.sessionQuoteRepository = sessionQuoteRepository;}

    public List<SessionQuoteCount> countSessionQuotesOfAllProducts(LocalDateTime startDate, LocalDateTime endDate) {
        List<SessionQuoteCount> result = Arrays.asList(ProductType.values()).stream()
                .map(productType -> countSessionQuotes(productType, startDate, endDate))
                .collect(Collectors.toList());
        return result;
    }

    public SessionQuoteCount countSessionQuotes(ProductType productType, LocalDateTime startDate, LocalDateTime endDate) {
        long countForEachProduct = sessionQuoteRepository.countByProductIdAndStartDateInRange(productType.getLogicName(), startDate, endDate);
        return new SessionQuoteCount(productType.getLogicName(), countForEachProduct);
    }

    //TODO not refactor yet.
    public byte[] exportTotalQuotesCountReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<SessionQuoteCount> listTotalQuoteCount = countSessionQuotesOfAllProducts(startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Session Quotes Counts");

        ExcelUtils.appendRow(sheet,
                text("Product"),
                text("Total Quotes"));
        listTotalQuoteCount.stream().forEach(sessionQuoteCount -> createTotalQuoteCountExtractExcelFileLine(sheet, sessionQuoteCount));
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

    private void createTotalQuoteCountExtractExcelFileLine(Sheet sheet, SessionQuoteCount sessionQuoteCount) {
        ExcelUtils.appendRow(sheet, text(sessionQuoteCount.getProductId()), ExcelUtils.integer(sessionQuoteCount.getQuoteCount()));
    }
}
