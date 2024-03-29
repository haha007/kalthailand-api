package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.common.model.DownloadFile;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.DownloadUtil;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PersonInfo;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PolicyCDB;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.model.PolicyPremiumNoticeRequest;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.model.PolicyPremiumNoticeSMSRequest;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.service.PolicyCDBService;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.service.PolicyPremiumNotificationService;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PaymentRetryService;
import th.co.krungthaiaxa.api.elife.service.PaymentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.PolicyValidatedProcessingService;
import th.co.krungthaiaxa.api.elife.service.PolicyValidatedProcessingService.PolicyValidationRequest;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static th.co.krungthaiaxa.api.common.utils.JsonUtil.getJson;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.VALIDATED;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

@RestController
@Api(value = "Policies")
public class PolicyResource {
    private final static Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private final DocumentService documentService;
    private final PolicyService policyService;
    private final QuoteService quoteService;
    private final PaymentService paymentService;
    private final PaymentRetryService paymentRetryService;
    private final PolicyValidatedProcessingService policyValidatedProcessingService;
    private final PolicyPremiumNotificationService policyPremiumNotificationService;
    private final PolicyCDBService policyCDBService;

    @Value("${environment.name}")
    private String environmentName;

    @Inject
    public PolicyResource(DocumentService documentService, PolicyService policyService, QuoteService quoteService, PaymentService paymentService, PaymentRetryService paymentRetryService, PolicyValidatedProcessingService policyValidatedProcessingService,
                          PolicyPremiumNotificationService policyPremiumNotificationService, PolicyCDBService policyCDBService) {
        this.documentService = documentService;
        this.policyService = policyService;
        this.quoteService = quoteService;
        this.paymentService = paymentService;
        this.paymentRetryService = paymentRetryService;
        this.policyValidatedProcessingService = policyValidatedProcessingService;
        this.policyPremiumNotificationService = policyPremiumNotificationService;
        this.policyCDBService = policyCDBService;
    }

    @ApiOperation(value = "List of policies", notes = "Gets a list of policies.", response = Policy.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 406, message = "If Excel file is not in invalid format", response = Error.class)
    })
    @RequestMapping(value = "/policies", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public Page<Policy> getAllPolicies(
            @ApiParam(required = true, value = "Page number (starts at 0)")
            @RequestParam Integer pageNumber,
            @ApiParam(required = true, value = "Number of elements per page")
            @RequestParam Integer pageSize,
            @ApiParam(value = "Part of policy Id to filter with")
            @RequestParam(required = false) String policyId,
            @ApiParam(value = "The product type to filter with")
            @RequestParam(required = false) ProductType productType,
            @ApiParam(value = "The policy status to filter with")
            @RequestParam(required = false) PolicyStatus status,
            @ApiParam(value = "True to return only Policies with previous agent code, false to return Policies with empty agent codes, empty to return all Policies")
            @RequestParam(required = false) Boolean nonEmptyAgentCode,
            @ApiParam(value = "To filter Policies starting after the given date")
            @RequestParam(required = false) String fromDate,
            @ApiParam(value = "To filter Policies ending before the given date")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) PeriodicityCode periodicityCode,
            @RequestParam(required = false) Integer atpModeId) {

        LocalDate startDate = null;
        if (StringUtils.isNoneEmpty(fromDate)) {
            startDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(fromDate));
        }

        LocalDate endDate = null;
        if (StringUtils.isNoneEmpty(toDate)) {
            endDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(toDate));
        }
        return policyService.findAll(policyId, productType, status, nonEmptyAgentCode, startDate, endDate, periodicityCode, atpModeId, pageNumber, pageSize);
    }

    @ApiOperation(value = "Policies extract", notes = "Gets the policy extract for commission calculation. Result is an Excel file", response = Policy.class, responseContainer = "List")
    @RequestMapping(value = "/policies/extract/download", method = GET)
    @ResponseBody
    public void getPoliciesExcelFile(
            @ApiParam(value = "Part of policy Id to filter with")
            @RequestParam(required = false) String policyId,
            @ApiParam(value = "The product type to filter with")
            @RequestParam(required = false) ProductType productType,
            @ApiParam(value = "The policy status to filter with")
            @RequestParam(required = false) PolicyStatus status,
            @ApiParam(value = "True to return only Policies with previous agent code, false to return Policies with empty agent codes, empty to return all Policies")
            @RequestParam(required = false) Boolean nonEmptyAgentCode,
            @ApiParam(value = "To filter Policies starting after the given date")
            @RequestParam(required = false) String fromDate,
            @ApiParam(value = "To filter Policies ending before the given date")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) PeriodicityCode periodicityCode,
            @RequestParam(required = false) Integer atpModeId,
            HttpServletResponse response) {
        LocalDate startDate = null;
        if (StringUtils.isNoneEmpty(fromDate)) {
            startDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(fromDate));
        }

        LocalDate endDate = null;
        if (StringUtils.isNoneEmpty(toDate)) {
            endDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(toDate));
        }

        List<Policy> policies = policyService.findAll(policyId, productType, status, nonEmptyAgentCode, startDate, endDate, periodicityCode, atpModeId);

        String now = ofPattern("yyyyMMdd_HHmmss").format(now());
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PolicyExtract_" + now);

        ExcelUtils.appendRow(sheet,
                text("Policy ID"),
                text("Product Type"),
                text("Line UserId"));
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

    @ApiOperation(value = "Policy details")
    @RequestMapping(value = "/policies/{policyNumber}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public Policy getPolicy(@ApiParam(value = "The policy number", required = true) @PathVariable String policyNumber) {
        return policyService.findPolicyWithFullDetailsByPolicyNumber(policyNumber);
    }

    @ApiOperation(value = "Send notifications", notes = "Sends pre-defined notifications to insured on different channels (SMS, eMail, Line Push notification). Content of notification and channels of notifications depends on reminder ID.", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy is not found", response = Error.class),
            @ApiResponse(code = 500, message = "If one of the notifications was not sent", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/reminder/{reminderId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> sendReminder(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The reminder ID. 1: reminder when user is not responding. 2: when phone number is wrong", required = true)
            @PathVariable Integer reminderId) {
        Optional<Policy> policy = policyService.findPolicyByPolicyNumber(policyId);
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
            return new ResponseEntity<>(getJson(ErrorCode.LINE_NOTIFICATION.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(getJson("Notifications have been sent"), OK);
    }

    @ApiOperation(value = "Download document", notes = "Download a document attached to a policy. Response's content-type is 'application/pdf'", response = String.class)
    @RequestMapping(value = "/policies/{policyId}/document/{documentType}/download", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public void downloadDocument(@PathVariable String policyId, @PathVariable String documentType, HttpServletResponse response) {
        Optional<Policy> policy = policyService.findPolicyByPolicyNumber(policyId);
        if (!policy.isPresent()) {
            return;
        }

        Optional<Document> document = policy.get().getDocuments().stream().filter(tmp -> tmp.getTypeName().name().equals(documentType)).findFirst();
        if (!document.isPresent()) {
            return;
        }

        DocumentDownload documentDownload = documentService.findDocumentDownload(document.get().getId());
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

    @ApiOperation(value = "Creates a policy", notes = "Creates a policy out of a quote. Policy will be created only " +
            "if it went through all product validation. Policy will also contain calculated payment schedule",
            response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If JSon of quote is invalid or if Policy could not be created",
                    response = Error.class)
    })
    @RequestMapping(value = "/policies", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> createPolicy(
            @ApiParam(value = "The session id the quote is in")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The json of the quote to create the policy from. This quote will go through maximum " +
                    "validations")
            @RequestBody String jsonQuote) {
        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(getJson(ErrorCode.INVALID_QUOTE_PROVIDED), NOT_ACCEPTABLE);
        }

        Optional<Quote> tmp = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, channelType);
        if (!tmp.isPresent()) {
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_DOES_NOT_EXIST), NOT_FOUND);
        }

        Policy policy;
        try {
            policy = policyService.createPolicy(quote);
        } catch (ElifeException e) {
            logger.error("Unable to create a policy from the validated quote [" + jsonQuote + "]", e);
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_CANNOT_BE_CREATED.apply(e.getMessage())), NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(getJson(policy), OK);
    }

    @ApiOperation(value = "Update main insured information", notes = "This API will help you to update person info of main insured",
            response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If JSon of quote is invalid or if Policy could not be created",
                    response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/main-insured/person", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public Policy updateMainInsuredPerson(
            @ApiParam(name = "policyId", value = "the policy number", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The Person object of mainInsured")
            @RequestBody PersonInfo mainInsuredPerson, HttpServletRequest httpServletRequest) {
//        String accessToken = RequestUtil.getAccessToken(httpServletRequest);

        return policyService.updateMainInsuredPerson(policyId, mainInsuredPerson);
    }

    @ApiOperation(value = "Policy payments", notes = "Get the payments of a policy", response = Payment.class,
            responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/payments", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public List<Payment> getPolicyPayments(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId) {
        return paymentService.findPaymentsByPolicyNumber(policyId);
//        Optional<Policy> policy = policyService.findPolicyByPolicyNumber(policyId);
//        if (!policy.isPresent()) {
//            logger.error("Unable to find the policy with ID [" + policyId + "]");
//            return new ResponseEntity<>(getJson(ErrorCode.POLICY_DOES_NOT_EXIST), NOT_FOUND);
//        }
//        return new ResponseEntity<>(getJson(policy.get().getPayments()), OK);
    }

    @ApiOperation(value = "Update Policy status", notes = "Updates the Policy status to PENDING_VALIDATION. If " +
            "susccessful, it also generates the DA form and Application form documents. Payment will be updated to " +
            "store the registration key which will be used later on for recurrent payments. Payment will also store " +
            "orderId and transactionId for trackign purprose.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment id is not found in the policy payment list", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking has failed and error details have not been provided", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking is successful and no registration key has been provided", response = Error.class),
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/update/status/pendingValidation", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity<byte[]> updatePolicyToPendingValidation(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The payment ID", required = true)
            @RequestParam String paymentId,//TODO this is for the first time, so this is the first payment in policy.
            @ApiParam(value = "The order id used to book the payment", required = true)
            @RequestParam String orderId,
            @ApiParam(value = "The transaction id to use to confirm the payment. Must be sent of status id SUCCESS", required = false)
            @RequestParam(required = false) Optional<String> transactionId,
            @ApiParam(value = "The RegKey is optional. If AtpMode is disabled, the regKey is null because there's no payment from user yet. But if AtpMode is enabled, the regKey must be not null.", required = false)
            @RequestParam(required = false) String regKey,
            HttpServletRequest httpServletRequest) {
        String msg = String.format("Policy update status to PendingValidation. policyId: %s, paymentId: %s, orderId: %s, transId: %s, regKey: %s", policyId, paymentId, orderId, transactionId, regKey);
        Instant start = LogUtil.logStarting(msg);
        if (isEmpty(orderId)) {
            logger.error("The order ID was not received");
            return new ResponseEntity<>(getJson(ErrorCode.ORDER_ID_NOT_PROVIDED), NOT_ACCEPTABLE);
        }
        Policy policy = policyService.validateExistPolicy(policyId);

        if (!policy.getStatus().equals(PolicyStatus.PENDING_PAYMENT)) {
            logger.error("The policy is in status [" + policy.getStatus().name() + "] and cannot be updated to " + PolicyStatus.PENDING_VALIDATION + " status.");
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_IS_NOT_PENDING_FOR_PAYMENT.apply(policyId)), NOT_ACCEPTABLE);
        }

        // If no transaction id, then in error, nothing else should be done since we don't have a status (error / success)
        if (!transactionId.isPresent() || isEmpty(transactionId.get())) {
            return new ResponseEntity<>(getJson(policy), OK);
        }

        //TODO need to UnitTest it.
        //TODO need to check is this the first payment in policy or not. If not, throw exception.
        Payment payment = paymentService.validateExistPaymentInPolicy(policyId, paymentId);
        if (StringUtils.isBlank(regKey)) {
            if (ProductUtils.isAtpModeEnable(policy)) {
                throw new BadArgumentException("The AtpMode is enabled, but regKey is blank. policyId:" + policyId, "regKey");
            } else if (PeriodicityCode.EVERY_MONTH.equals(ProductUtils.getPeriodicityCode(policy))) {
                throw new BadArgumentException("The AtpMode is enabled, but regKey is blank. policyId:" + policyId + ". Besides, payment is MONTHLY but ATP is disabled, that's wrong!", "regKey");
            }
        }
        paymentService.updatePayment(payment, orderId, transactionId.get(), (regKey == null ? "" : regKey));

        final String accessToken = RequestUtil.getAccessToken(httpServletRequest);
        // Update the policy status
        policyService.updatePolicyStatusToPendingValidation(policy, accessToken);
        //Return policy with updated payment
        policy = policyService.validateExistPolicy(policyId);
        LogUtil.logFinishing(start, msg);
        return new ResponseEntity<>(getJson(policy), OK);
    }

    @ApiOperation(value = "Update Policy status", notes = "Updates the Policy status to VALIDATED. If " +
            "successful, it also generates the DA form and Application form documents. Payment will be updated to " +
            "store the registration key which will be used later on for recurrent payments. Payment will also store " +
            "orderId and transactionId for tracking purpose.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment id is not found in the policy payment list", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking has failed and error details have not been provided", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking is successful and no registration key has been provided", response = Error.class),
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/update/status/completedValidation", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity<byte[]> completedValidationOnPolicy(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(name = "paymentId", value = "The payment ID", required = true)
            @RequestParam("paymentId") String paymentId,
            @ApiParam(value = "The order id used to book the payment", required = true)
            @RequestParam(required = true) String orderId,
            @ApiParam(value = "The transaction id to use to confirm the payment. Must be sent of status id SUCCESS", required = false)
            @RequestParam(required = false) String transactionId,
            @ApiParam(value = "The RegKey for Monthly Mode Payment Only", required = false)
            @RequestParam(required = false) String regKey,
            HttpServletRequest httpServletRequest) {
        String msg = String.format("[Retry payment] Retry to complete payment. policyId: %s, paymentId: %s, orderId: %s, transId: %s, regKey: %s", policyId, paymentId, orderId, transactionId, regKey);
        Instant start = LogUtil.logStarting(msg);

        Policy policy = policyService.validateExistPolicy(policyId);
        if (!policy.getStatus().equals(PolicyStatus.VALIDATED)) {
            logger.error("The policy is in status [" + policy.getStatus().name() + "], it must be " + VALIDATED + " status.");
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_IS_NOT_VALIDATED_FOR_PAYMENT.apply(policyId)), NOT_ACCEPTABLE);
        }
        String accessToken = RequestUtil.getAccessToken(httpServletRequest);
        paymentRetryService.retryFailedPayment(policyId, paymentId, orderId, transactionId, regKey, accessToken);
        LogUtil.logFinishing(start, msg);
        return new ResponseEntity<>(getJson(policy), OK);
    }

    @ApiOperation(value = "Change status of policy from PENDING_VALIDATION to VALIDATED.", notes = "Updates the Policy status to VALIDATED. If " +
            "susuccessful, it also generates the eReceipt form document (image and PDF) and eReceipt pdf is sent to " +
            "Tele sale API. LINE Pay API is called to confirm the payment booking made earlier using the " +
            "registration key. Payment will be updated with amount and effective date. Finally, it sends " +
            "notifications through email, SMS and LINE push notifications", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the agent code is not in format '123456-12-123456'", response = Error.class),
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/update/status/validated", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public Policy updatePolicyToValidated(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The code of validating agent", required = true)
            @RequestParam String agentCode,
            @ApiParam(value = "The name of validating agent", required = true)
            @RequestParam String agentName,
            @ApiParam(value = "The type of call to Line Pay Capture API", required = true)
            @RequestParam LinePayCaptureMode linePayCaptureMode,
            HttpServletRequest httpServletRequest) {
        String accessToken = RequestUtil.getAccessToken(httpServletRequest);
        PolicyValidationRequest policyValidationRequest = new PolicyValidationRequest(policyId, agentCode, agentName, linePayCaptureMode, accessToken);
        return policyValidatedProcessingService.updatePolicyStatusToValidated(policyValidationRequest);
    }

    @ApiOperation(value = "Notify policy's premium via SMS", notes = "Send the notification about premium information to client via SMS")
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/premium/sms", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public void sendSMSPolicyPremium(
            @ApiParam(value = "policyId", required = true)
            @PathVariable("policyId") String policyNumber,
            @RequestBody @Valid PolicyPremiumNoticeSMSRequest policyPremiumNoticeSMSRequest) {
        policyPremiumNoticeSMSRequest.setPolicyNumber(policyNumber);
        policyPremiumNotificationService.sendSMS(policyPremiumNoticeSMSRequest);
    }

    @ApiOperation(value = "Notify policy's premium via Email", notes = "Send the notification about premium information to client via email")
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/premium/email", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public void sendEmailPolicyPremium(
            @ApiParam(value = "policyId", required = true)
            @PathVariable("policyId") String policyNumber,
            @RequestBody @Valid PolicyPremiumNoticeRequest policyPremiumNoticeRequest) {
        policyPremiumNoticeRequest.setPolicyNumber(policyNumber);
        policyPremiumNotificationService.sendEmail(policyPremiumNoticeRequest);
    }

    @ApiOperation(value = "Download policy's premium notification pdf", response = DownloadFile.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/premium/pdf", produces = APPLICATION_JSON_VALUE, method = POST)
    public DownloadFile getPolicyPremiumNoticePdf(
            @ApiParam(value = "policyId", required = true)
            @PathVariable("policyId") String policyNumber,
            @RequestBody @Valid PolicyPremiumNoticeRequest policyPremiumNoticeRequest) {
        policyPremiumNoticeRequest.setPolicyNumber(policyNumber);
        byte[] pdf = policyPremiumNotificationService.exportPdf(policyPremiumNoticeRequest);
        DownloadFile result = new DownloadFile();
        String base64String = Base64.getEncoder().encodeToString(pdf);
        result.setContent(base64String);
        result.setFileName("premium-notice.pdf");
        result.setMimeType(DownloadUtil.CONTENT_TYPE_PDF);
        return result;
    }

    @ApiOperation(value = "Download policy's premium notification pdf")
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/premium/pdf/download", produces = APPLICATION_JSON_VALUE, method = POST)
    public void downloadPolicyPremiumPdf(
            @ApiParam(value = "policyId", required = true)
            @PathVariable("policyId") String policyNumber,
            @RequestBody @Valid PolicyPremiumNoticeRequest policyPremiumNoticeRequest,
            HttpServletResponse httpServletResponse) {
        policyPremiumNoticeRequest.setPolicyNumber(policyNumber);
        byte[] pdf = policyPremiumNotificationService.exportPdf(policyPremiumNoticeRequest);
        DownloadUtil.writeBytesToResponseWithFullFileName(httpServletResponse, pdf, "premium-notice.pdf", DownloadUtil.DownloadType.PDF);
    }

    @ApiOperation(value = "Get policy detail from Core DB (CDB) system.", notes = "This method doesn't get policy detail from eLife DB, it will get detail from CDB.", response = PolicyCDB.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/premium/cdb", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public PolicyCDB getPolicyDetailFromCDB(
            @ApiParam(value = "policyId", required = true)
            @PathVariable("policyId") String policyNumber,
            @ApiParam(value = "The dob of main insured. Pattern: 'yyyy-MM-dd'", required = true)
            @RequestParam(name = "insuredDob", required = true) String insuredDobString) {
        LocalDate insuredDob = DateTimeUtil.toLocalDate(insuredDobString, RequestUtil.PATTERN_LOCAL_DATE);
        return policyCDBService.findOneByPolicyNumberAndMainInsuredDOB(policyNumber, insuredDob);
    }

    private void createPolicyExtractExcelFileLine(Sheet sheet, Policy policy) {

        Insured firstInsured = ProductUtils.validateExistFirstInsured(policy);
        String productType = ProductUtils.getProductLogicName(policy);
        String lineUserId = firstInsured.getPerson().getLineUserId();
        ExcelUtils.appendRow(sheet,
                text(policy.getPolicyId()),
                text(productType),
                text(lineUserId));
    }

}
