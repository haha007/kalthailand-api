package th.co.krungthaiaxa.api.elife.resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import springfox.documentation.annotations.ApiIgnore;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.BlackListedService;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.CANCELED;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.PENDING_PAYMENT;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

@ApiIgnore
@RestController
public class AdminResource {
    private final static Logger logger = LoggerFactory.getLogger(AdminResource.class);
    private final PolicyService policyService;
    private final DocumentService documentService;
    private final BlackListedService blackListedService;

    @Inject
    public AdminResource(PolicyService policyService, DocumentService documentService, BlackListedService blackListedService) {
        this.policyService = policyService;
        this.documentService = documentService;
        this.blackListedService = blackListedService;
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getAllPolicies(@RequestParam Integer pageNumber,
                                                 @RequestParam Integer pageSize,
                                                 @RequestParam(required = false) String policyId,
                                                 @RequestParam(required = false) ProductType productType,
                                                 @RequestParam(required = false) PolicyStatus status,
                                                 @RequestParam(required = false) Boolean nonEmptyAgentCode,
                                                 @RequestParam(required = false) String fromDate,
                                                 @RequestParam(required = false) String toDate) {
        LocalDate startDate = null;
        if (StringUtils.isNoneEmpty(fromDate)) {
            startDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(fromDate));
        }

        LocalDate endDate = null;
        if (StringUtils.isNoneEmpty(toDate)) {
            endDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(toDate));
        }
        return new ResponseEntity<>(getJson(policyService.findAll(policyId, productType, status, nonEmptyAgentCode, startDate, endDate, pageNumber, pageSize)), OK);
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies/extract/download", method = GET)
    @ResponseBody
    public void getPoliciesExcelFile(@RequestParam(required = false) String policyId,
                                     @RequestParam(required = false) ProductType productType,
                                     @RequestParam(required = false) PolicyStatus status,
                                     @RequestParam(required = false) Boolean nonEmptyAgentCode,
                                     @RequestParam(required = false) String fromDate,
                                     @RequestParam(required = false) String toDate,
                                     HttpServletResponse response) {
        LocalDate startDate = null;
        if (StringUtils.isNoneEmpty(fromDate)) {
            startDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(fromDate));
        }

        LocalDate endDate = null;
        if (StringUtils.isNoneEmpty(toDate)) {
            endDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(toDate));
        }

        List<Policy> policies = policyService.findAll(policyId, productType, status, nonEmptyAgentCode, startDate, endDate);

        String now = ofPattern("yyyyMMdd_HHmmss").format(now());
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PolicyExtract_" + now);
        ExcelUtils.appendRow(sheet,
                text("Policy ID"),
                text("Product Type"),
                text("Premium"),
                text("Status"),
                text("Start date"),
                text("Agent Code 1"),
                text("Agent Code 2"),
                text("Validation Agent Code"));
        policies.stream().forEach(tmp -> createPolicyExtractExcelFileLine(sheet, tmp));
        ExcelUtils.autoWidthAllColumns(workbook);

        byte[] content;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            content = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(content.length);

        String fileName = "eLife_PolicyExtract_" + now + ".xlsx";
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(content, outStream);
        } catch (IOException e) {
            logger.error("Unable to download the deduction file", e);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies/{policyId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPolicy(@PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_DOES_NOT_EXIST), NOT_FOUND);
        } else if (policy.get().getStatus().equals(CANCELED)) {
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_IS_CANCELED.apply(policyId)), NOT_ACCEPTABLE);
        } else if (policy.get().getStatus().equals(PENDING_PAYMENT)) {
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_IS_PENDING_PAYMENT.apply(policyId)), NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity<>(getJson(policy.get()), OK);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies/{policyId}/reminder/{reminderId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> sendReminder(@PathVariable String policyId, @PathVariable Integer reminderId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        try {
            switch (reminderId) {
                case 1:
                    policyService.sendNotificationsWhenUserNotRespondingToCalls(policy.get());
                    break;
                case 2:
                    policyService.sendNotificationsWhenPhoneNumberIsWrong(policy.get());
                    break;
            }
        } catch (MessagingException | IOException e) {
            logger.error("Notification has not been sent", e);
            return new ResponseEntity<>(getJson(ErrorCode.NOTIFICATION_NOT_SENT.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(getJson("Notifications have been sent"), OK);
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies/{policyId}/document/{documentType}/download", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public void downloadDocument(@PathVariable String policyId, @PathVariable String documentType, HttpServletResponse response) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return;
        }

        Optional<Document> document = policy.get().getDocuments().stream().filter(tmp -> tmp.getTypeName().name().equals(documentType)).findFirst();
        if (!document.isPresent()) {
            return;
        }

        DocumentDownload documentDownload = documentService.downloadDocument(document.get().getId());
        byte[] documentContent = Base64.getDecoder().decode(documentDownload.getContent());

        response.setContentType("application/pdf");
        response.setContentLength(documentContent.length);

        String fileName = policyId + "-" + documentType + "_" + ofPattern("yyyyMMdd_HHmmss").format(now()) + ".pdf";
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(documentContent, outStream);
        } catch (IOException e) {
            logger.error("Unable to download the document", e);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/blackList", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> blackList(@RequestParam Integer pageNumber, @RequestParam Integer pageSize, @RequestParam String searchContent) {
        return new ResponseEntity<>(getJson(blackListedService.findAll(pageNumber, pageSize, searchContent)), OK);
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/blackList/upload", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> uploadBlackListFile(@RequestParam("file") MultipartFile file) {
        try {
            return new ResponseEntity<>(getJson(blackListedService.readBlackListedExcelFile(file.getInputStream())), CREATED);
        } catch (IOException | SAXException | OpenXML4JException | ParserConfigurationException | IllegalArgumentException | ElifeException e) {
            return new ResponseEntity<>(getJson(ErrorCode.INVALID_BLACKLIST_FILE.apply(e.getMessage())), NOT_ACCEPTABLE);
        }
    }

    private void createPolicyExtractExcelFileLine(Sheet sheet, Policy policy) {
        // Extracted Excel file should contain 'NULL' by default
        String agentCode1 = "NULL";
        String agentCode2 = "NULL";
        String validationAgentCode = "NULL";

        if (policy.getInsureds().get(0).getInsuredPreviousAgents().size() >= 1) {
            agentCode1 = policy.getInsureds().get(0).getInsuredPreviousAgents().get(0);
        }
        if (policy.getInsureds().get(0).getInsuredPreviousAgents().size() >= 2) {
            agentCode2 = policy.getInsureds().get(0).getInsuredPreviousAgents().get(1);
        }
        if (policy.getValidationAgentCode() != null) {
            validationAgentCode = policy.getValidationAgentCode();
        }

        ExcelUtils.appendRow(sheet,
                text(policy.getPolicyId()),
                text(policy.getCommonData().getProductId()),
                text(policy.getPremiumsData().getFinancialScheduler().getModalAmount().toString()),
                text(policy.getStatus().name()),
                text(ofPattern("yyyy-MM-dd").format(policy.getInsureds().get(0).getStartDate())),
                text(agentCode1),
                text(agentCode2),
                text(validationAgentCode));
    }

}