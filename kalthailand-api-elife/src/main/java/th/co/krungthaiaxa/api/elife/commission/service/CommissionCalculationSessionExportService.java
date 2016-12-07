package th.co.krungthaiaxa.api.elife.commission.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculation;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionCalculationSessionRepository;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionResultRepository;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import java.io.IOException;
import java.util.List;

import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

/**
 * @author khoi.tran on 9/5/16.
 */
@Service
public class CommissionCalculationSessionExportService {
    public final static Logger LOGGER = LoggerFactory.getLogger(CDBRepository.class);
    private final CommissionResultRepository commissionResultRepository;
    private final CommissionCalculationSessionRepository commissionCalculationSessionRepository;

    private final CommissionCalculationSessionService commissionCalculationSessionService;

    @Autowired
    public CommissionCalculationSessionExportService(CommissionResultRepository commissionResultRepository, CommissionCalculationSessionRepository commissionCalculationSessionRepository, CommissionCalculationSessionService commissionCalculationSessionService) {
        this.commissionResultRepository = commissionResultRepository;
        this.commissionCalculationSessionRepository = commissionCalculationSessionRepository;
        this.commissionCalculationSessionService = commissionCalculationSessionService;
    }

    //santi : for download commission excel file
    public byte[] exportToExcel(String commissionCalculationSessionId) {

        LOGGER.debug("Start process to export commission excel .....");

        CommissionCalculationSession commissionResult = commissionCalculationSessionRepository.findOne(commissionCalculationSessionId);
        List<CommissionCalculation> commissionCalculated = commissionResult.getCommissionCalculations();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("CommissionExtract_" + DateTimeUtil.formatNowForFilePath());

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
                text("Calculate Date Time"),
                text("Result Code"),
                text("Result Message"));
        commissionCalculated.stream().forEach(tmp -> createCommissionResultExtractExcelFileLine(sheet, tmp, commissionResult));
        ExcelUtils.autoWidthAllColumns(workbook);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            byte[] content = byteArrayOutputStream.toByteArray();
            LOGGER.debug("Stop process to export commission excel .....");
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }
    }

    private void createCommissionResultExtractExcelFileLine(Sheet sheet, CommissionCalculation commission, CommissionCalculationSession commissionResult) {
        String commissionMonth = DateTimeUtil.format(commissionResult.getCommissionDate(), "MM/yyyy");
        String updateDateTime = DateTimeUtil.formatLocalDateTime(commissionResult.getUpdatedDateTime(), "dd/MM/yyyy HH:mm:ss.SSS");
        ExcelUtils.appendRow(sheet,
                text(commissionMonth),
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
                text(updateDateTime),
                text(commission.getResultCode()),
                text(commission.getResultMessage()));
    }

}
