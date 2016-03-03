package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.PaymentInformation;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static th.co.krungthaiaxa.elife.api.utils.ExcelUtils.*;

@Service
public class ExportService {
    private final static Logger logger = LoggerFactory.getLogger(ExportService.class);

    private Function<PeriodicityCode, String> paymentMode = periodicityCode -> {
        if (periodicityCode.equals(PeriodicityCode.EVERY_YEAR)) {
            return "A";
        } else if (periodicityCode.equals(PeriodicityCode.EVERY_HALF_YEAR)) {
            return "S";
        } else if (periodicityCode.equals(PeriodicityCode.EVERY_QUARTER)) {
            return "Q";
        } else {
            return "M";
        }
    };

    public byte[] exportPayments(List<Pair<Policy, PaymentInformation>> payments) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LFPATPTDR6");
        appendRow(sheet, //
                text("M93RPNO6"), // Policy ID
                text("M93RBKCD6"), // Bank Code
                text("M93RPMOD6"), // Payment Mode
                text("M93RPRM6"), // Deposit amount
                text("M93RDOC6"), // Process date
                text("M93RJCD6")); // Rejection Code
        payments.stream().forEach(data -> exportPayment(sheet, data.getLeft(), data.getRight()));
        autoWidthAllColumns(workbook);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Unable to write content of excel file", e);
            throw new IllegalStateException("Unable to write content of excel file", e);
        }
    }

    private void exportPayment(Sheet sheet, Policy policy, PaymentInformation paymentInformation) {
        appendRow(sheet,
                text(policy.getPolicyId()),
                empty(),
                text(paymentMode.apply(policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode())),
                text(paymentInformation.getAmount().getValue().toString()),
                time(paymentInformation.getDate()),
                text(paymentInformation.getRejectionErrorMessage()));
    }

}
