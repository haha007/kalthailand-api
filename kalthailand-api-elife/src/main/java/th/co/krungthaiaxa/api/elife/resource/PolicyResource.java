package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.*;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponseInfo;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponsePaymentInfo;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.LineService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.PENDING_VALIDATION;
import static th.co.krungthaiaxa.api.elife.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode.*;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

@RestController
@Api(value = "Policies")
public class PolicyResource {
    private final static Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private final DocumentService documentService;
    private final LineService lineService;
    private final PolicyService policyService;
    private final QuoteService quoteService;

    @Value("${environment.name}")
    private String environmentName;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;

    @Inject
    public PolicyResource(DocumentService documentService, LineService lineService, PolicyService policyService, QuoteService quoteService) {
        this.documentService = documentService;
        this.lineService = lineService;
        this.policyService = policyService;
        this.quoteService = quoteService;
    }

    @ApiOperation(value = "List of policies", notes = "Gets a list of policies.", response = Policy.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 406, message = "If Excel file is not in invalid format", response = Error.class)
    })
    @RequestMapping(value = "/policies", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getAllPolicies(
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
                text("Previous Policy ID"),
                text("Agent Code 1"),
                text("Agent Code 2"));
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

    @ApiOperation(value = "Policy details", notes = "Gets the details of a policy.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy is not found", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPolicy(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_DOES_NOT_EXIST), NOT_FOUND);
        } else {
            return new ResponseEntity<>(getJson(policy.get()), OK);
        }
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

    @ApiOperation(value = "Download document", notes = "Download a document attached to a policy. Response's content-type is 'application/pdf'", response = String.class)
    @RequestMapping(value = "/policies/{policyId}/document/{documentType}/download", produces = APPLICATION_JSON_VALUE, method = GET)
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
            return new ResponseEntity<>(getJson(INVALID_QUOTE_PROVIDED), NOT_ACCEPTABLE);
        }

        Optional<Quote> tmp = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, channelType);
        if (!tmp.isPresent()) {
            return new ResponseEntity<>(getJson(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED), NOT_FOUND);
        }

        Policy policy;
        try {
            policy = policyService.createPolicy(quote);
        } catch (ElifeException e) {
            logger.error("Unable to create a policy from the validated quote [" + jsonQuote + "]", e);
            return new ResponseEntity<>(getJson(POLICY_CANNOT_BE_CREATED.apply(e.getMessage())), NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(getJson(policy), OK);
    }

    @ApiOperation(value = "Policy payments", notes = "Get the payments of a policy", response = Payment.class,
            responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/payments", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPolicyPayments(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }
        return new ResponseEntity<>(getJson(policy.get().getPayments()), OK);
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
            @RequestParam String paymentId,
            @ApiParam(value = "The order id used to book the payment", required = true)
            @RequestParam String orderId,
            @ApiParam(value = "The transaction id to use to confirm the payment. Must be sent of status id SUCCESS", required = false)
            @RequestParam(required = false) Optional<String> transactionId,
            @ApiParam(value = "The RegKey for Monthly Mode Payment Only", required = false)
            @RequestParam(required = false) Optional<String> regKey) {
        if (isEmpty(orderId)) {
            logger.error("The order ID was not received");
            return new ResponseEntity<>(getJson(ORDER_ID_NOT_PROVIDED), NOT_ACCEPTABLE);
        }

        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        if (!policy.get().getStatus().equals(PolicyStatus.PENDING_PAYMENT)) {
            logger.error("The policy is in status [" + policy.get().getStatus().name() + "] and cannot be updated to " + PENDING_VALIDATION + " status.");
            return new ResponseEntity<>(getJson(POLICY_IS_NOT_PENDING_FOR_PAYMENT.apply(policyId)), NOT_ACCEPTABLE);
        }

        Optional<Payment> payment = policy.get().getPayments().stream().filter(tmp -> tmp.getPaymentId().equals(paymentId)).findFirst();
        if (!payment.isPresent()) {
            logger.error("Unable to find the payment with ID [" + paymentId + "] in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_CONTAIN_A_PAYMENT_WITH_TRANSACTION_ID), NOT_ACCEPTABLE);
        }

        // If no transaction id, then in error, nothing else should be done since we don't have a status (error / success)
        if (!transactionId.isPresent() || isEmpty(transactionId.get())) {
            return new ResponseEntity<>(getJson(policy.get()), OK);
        }

        // Update the payment
        if (regKey.isPresent()) {
            policyService.updatePayment(payment.get(), orderId, transactionId.get(), (!regKey.isPresent() ? "" : regKey.get()));
        } else {
            policyService.updatePayment(payment.get(), orderId, transactionId.get(), "");
        }

        // Update the policy status
        policyService.updatePolicyAfterFirstPaymentValidated(policy.get());

        return new ResponseEntity<>(getJson(policy.get()), OK);
    }

    @ApiOperation(value = "Update Policy status", notes = "Updates the Policy status to VALIDATED. If " +
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
    public ResponseEntity<byte[]> updatePolicyToValidated(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The code of validating agent", required = true)
            @RequestParam String agentCode,
            @ApiParam(value = "The name of validating agent", required = true)
            @RequestParam String agentName,
            @ApiParam(value = "The type of call to Line Pay Capture API", required = true)
            @RequestParam LinePayCaptureMode linePayCaptureMode,
            HttpServletRequest httpServletRequest) {

        Pattern pattern = Pattern.compile("[0-9]{6}-[0-9]{2}-[0-9]{6}$");
        Matcher matcher = pattern.matcher(agentCode);
        if (!matcher.find()) {
            logger.error("Agent code [" + policyId + "] is not following format '123456-12-123456'.");
            return new ResponseEntity<>(getJson(AGENT_CODE_FORMAT_ERROR), NOT_ACCEPTABLE);
        }

        if (environmentName.equals("PRD") && !linePayCaptureMode.equals(REAL)) {
            return new ResponseEntity<>(getJson(REAL_CAPTURE_API_HAS_TO_BE_USED), NOT_ACCEPTABLE);
        }

        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        Optional<Payment> paymentOptional = policy.get().getPayments()
                .stream()
                .filter(tmp -> tmp.getTransactionId() != null)
                .findFirst();
        if (!paymentOptional.isPresent()) {
            logger.error("Unable to find a payment with a transaction id pending for confirmation in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_CONTAIN_A_PAYMENT_WITH_TRANSACTION_ID), NOT_ACCEPTABLE);
        }

        Payment payment = paymentOptional.get();
        LinePayResponse linePayResponse = null;
        if (linePayCaptureMode.equals(REAL)) {
            logger.info("Will try to confirm payment with ID [" + payment.getPaymentId() + "] and transation ID [" + payment.getTransactionId() + "] on the policy with ID [" + policyId + "]");
            try {
                linePayResponse = lineService.capturePayment(payment.getTransactionId(), payment.getAmount().getValue(), payment.getAmount().getCurrencyCode());
            } catch (RuntimeException | IOException e) {
                logger.error("Unable to confirm the payment in the policy with ID [" + policyId + "]", e);
                return new ResponseEntity<>(getJson(UNABLE_TO_CAPTURE_PAYMENT.apply(e.getMessage())), NOT_ACCEPTABLE);
            }
        } else if (linePayCaptureMode.equals(FAKE_WITH_ERROR)) {
            linePayResponse = new LinePayResponse();
            linePayResponse.setReturnCode("9999");
            linePayResponse.setReturnMessage("This is a fake call to Line Pay API with an error as a result");
        } else if (linePayCaptureMode.equals(FAKE_WITH_SUCCESS)) {
            LinePayResponsePaymentInfo payResponsePaymentInfo = new LinePayResponsePaymentInfo();
            payResponsePaymentInfo.setMethod("someMethodAfterFakeCallToLinePayCaptureAPI");
            payResponsePaymentInfo.setCreditCardName("someCreditCardNameAfterFakeCallToLinePayCaptureAPI");

            LinePayResponseInfo info = new LinePayResponseInfo();
            info.setRegKey("someRegistrationKeyAfterFakeCallToLinePayCaptureAPI");
            info.addPayInfo(payResponsePaymentInfo);

            linePayResponse = new LinePayResponse();
            linePayResponse.setReturnCode("0000");
            linePayResponse.setReturnMessage("This is a fake call to Line Pay API with success");
            linePayResponse.setInfo(info);
        }

        if (linePayResponse == null) {
            return new ResponseEntity<>(getJson(UNABLE_TO_CAPTURE_PAYMENT.apply("No way to call Line Pay capture API has been provided")), NOT_ACCEPTABLE);
        } else if (!linePayResponse.getReturnCode().equals("0000")) {
            String msg = "Confirming payment didn't go through. Error code is [" + linePayResponse.getReturnCode() + "], error message is [" + linePayResponse.getReturnMessage() + "]";
            return new ResponseEntity<>(getJson(UNABLE_TO_CAPTURE_PAYMENT.apply(msg)), NOT_ACCEPTABLE);
        }

        // Update the payment if confirm is success
        policyService.updatePayment(payment, payment.getAmount().getValue(), payment.getAmount().getCurrencyCode(), LINE, linePayResponse);
        policyService.updateRegistrationForAllNotProcessedPayment(policy.get(), linePayResponse.getInfo().getRegKey());

        try {
            policyService.updatePolicyAfterPolicyHasBeenValidated(policy.get(), agentCode, agentName, httpServletRequest.getHeader(tokenHeader));
        } catch (ElifeException e) {
            logger.error("Payment is successful but there was an error whil trying to update policy status.", e);
            return new ResponseEntity<>(getJson(POLICY_VALIDATION_ERROR.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(getJson(policy.get()), OK);
    }

    private void createPolicyExtractExcelFileLine(Sheet sheet, Policy policy) {
        if (policy.getInsureds().get(0).getInsuredPreviousInformations().size() != 0) {
            ExcelUtils.appendRow(sheet,
                    text(policy.getPolicyId()),
                    text(policy.getInsureds().get(0).getInsuredPreviousInformations().get(0)),
                    text(policy.getInsureds().get(0).getInsuredPreviousInformations().get(1)),
                    text(policy.getInsureds().get(0).getInsuredPreviousInformations().get(2)));
        }
    }

}
