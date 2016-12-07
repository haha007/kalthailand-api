package th.co.krungthaiaxa.api.elife.commission.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculation;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionResult;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionResultRepository;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

/**
 * @author khoi.tran on 9/5/16.
 */
@Service
public class CommissionCalculationSessionExportService {
    public final static Logger LOGGER = LoggerFactory.getLogger(CDBRepository.class);
    private final CommissionResultRepository commissionResultRepository;

    private final CommissionCalculationSessionService commissionCalculationSessionService;

    @Autowired
    public CommissionCalculationSessionExportService(CommissionResultRepository commissionResultRepository, CommissionCalculationSessionService commissionCalculationSessionService) {
        this.commissionResultRepository = commissionResultRepository;
        this.commissionCalculationSessionService = commissionCalculationSessionService;
    }

    /**
     * @param commissionCalculationSessionId
     * @return
     * @deprecated Not implemented
     */
    @Deprecated
    public byte[] exportExcel(String commissionCalculationSessionId) {
        ObjectId objectId = new ObjectId(commissionCalculationSessionId);
        CommissionCalculationSession commissionCalculationSession = commissionCalculationSessionService.validateExistCalculationSession(objectId);
        //TODO this method never return null.
        return null;
    }

    //santi : for download commission excel file
    public byte[] exportToExcel(String rowId, String now) {

        LOGGER.debug("Start process to export commission excel .....");

        byte[] content = null;

        CommissionResult commissionResult = commissionResultRepository.findByRowId(rowId);
        List<CommissionCalculation> commissionCalculated = commissionResult.getPolicies();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("CommissionExtract_" + now);

        ExcelUtils.appendRow(sheet,
                text("Month"),
                text("Policy Number"),
                text("Policy Status"),
                text("Plan Code"),
                text("Payment Code"),
                text("Agent Code"),
                text("Customer Category"),
                text("Previous Policy Number"),
                text("Existing Agent Code 1"),
                text("Existing Agent Code 1 Status"),
                text("Existing Agent Code 2"),
                text("Existing Agent Code 2 Status"),
                text("First Year Premium (RLS)"),
                text("First Year Commission (RLS)"),
                text("FY Affiliate Commission"),
                text("FY Distribution 1 Commission"),
                text("FY Distribution 2 Commission"),
                text("FY TSR Commission"),
                text("FY Marketing Commission"),
                text("FY Company Commission"),
                text("OV Affiliate Commission"),
                text("OV Distribution 1 Commission"),
                text("OV Distribution 2 Commission"),
                text("OV TSR Commission"),
                text("OV Marketing Commission"),
                text("OV Company Commission"),
                text("FY Affiliate Rate"),
                text("FY Distribution Rate"),
                text("FY TSR Rate"),
                text("FY Marketion Rate"),
                text("FY Company Rate"),
                text("OV Affiliate Rate"),
                text("OV Distribution Rate"),
                text("OV TSR Rate"),
                text("OV Marketing Rate"),
                text("OV Company Rate"),
                text("Calculate Date Time"));
        commissionCalculated.stream().forEach(tmp -> createCommissionResultExtractExcelFileLine(sheet, tmp, commissionResult));
        ExcelUtils.autoWidthAllColumns(workbook);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            content = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }

        LOGGER.debug("Stop process to export commission excel .....");

        return content;

    }

    private void createCommissionResultExtractExcelFileLine(Sheet sheet, CommissionCalculation commission, CommissionResult commissionResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
        ExcelUtils.appendRow(sheet,
                text(String.valueOf(commissionResult.getCommissionMonth())),
                text(commission.getPolicyNumber()),
                text(commission.getPolicyStatus()),
                text(commission.getPlanCode()),
                text(commission.getPaymentCode()),
                text(commission.getAgentCode()),
                text(commission.getCustomerCategory()),
                text(commission.getPreviousPolicyNo()),
                text(commission.getExistingAgentCode1()),
                text(commission.getExistingAgentCode1Status()),
                text(commission.getExistingAgentCode2()),
                text(commission.getExistingAgentCode2Status()),
                text(commission.getFirstYearPremium()),
                text(commission.getFirstYearCommission()),
                text(commission.getFyAffiliateCommission()),
                text(commission.getFyDistribution1Commission()),
                text(commission.getFyDistribution2Commission()),
                text(commission.getFyTsrCommission()),
                text(commission.getFyMarketingCommission()),
                text(commission.getFyCompanyCommission()),
                text(commission.getOvAffiliateCommission()),
                text(commission.getOvDistribution1Commission()),
                text(commission.getOvDistribution2Commission()),
                text(commission.getOvTsrCommission()),
                text(commission.getOvMarketingCommission()),
                text(commission.getOvCompanyCommission()),
                text(commission.getFyAffiliateRate()),
                text(commission.getFyDistributionRate()),
                text(commission.getFyTsrRate()),
                text(commission.getFyMarketingRate()),
                text(commission.getFyCompanyRate()),
                text(commission.getOvAffiliateRate()),
                text(commission.getOvDistributionRate()),
                text(commission.getOvTsrRate()),
                text(commission.getOvMarketingRate()),
                text(commission.getOvCompanyRate()),
                text(commissionResult.getUpdatedDateTime().format(formatter)));
    }

}
