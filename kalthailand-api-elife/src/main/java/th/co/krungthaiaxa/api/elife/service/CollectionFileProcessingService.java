package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.action.ActionWithResult;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.ProfileHelper;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.CollectionFileLine;
import th.co.krungthaiaxa.api.elife.data.DeductionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptNumber;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptService;
import th.co.krungthaiaxa.api.elife.line.LineService;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponse;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.CollectionFileRepository;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static th.co.krungthaiaxa.api.elife.line.LineService.RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
import static th.co.krungthaiaxa.api.elife.line.LineService.RESPONSE_CODE_SUCCESS;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.appendRow;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

/**
 * This class contains methods for processing the collectionFile.
 * After processing collection files, the result (Excel) file will be updated into RLS system (manually).
 * Collection always process only the 'renewal' payments. The 'new business' payments were already handled by FE.
 */
@Service
public class CollectionFileProcessingService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CollectionFileProcessingService.class);
    /**
     * For processing collection file, it's always Renewal payment, so the NewBusiness is always false.
     */
    private static final boolean NEW_BUSINESS = false;

    public static final String FAIL_PROCESSING_EMAIL = "+kalthailand-api.test.fail";

    private final CollectionFileRepository collectionFileRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyService policyService;
    private final PaymentService paymentService;
    private final EreceiptService ereceiptService;

    //    private final DeductionFileRepository deductionFileRepository;
    /**
     * Don't put final here because we need inject mock test dependency
     */
    private LineService lineService;
    private final PaymentFailEmailService paymentRetryEmailService;
    private final PaymentFailLineNotificationService paymentRetryLineNotificationService;
    private final ProfileHelper profileHelper;

    @Inject
    public CollectionFileProcessingService(CollectionFileRepository collectionFileRepository, PaymentRepository paymentRepository, PolicyRepository policyRepository, PolicyService policyService, PaymentService paymentService, EreceiptService ereceiptService,
            LineService lineService,
            PaymentFailEmailService paymentRetryEmailService, PaymentFailLineNotificationService paymentRetryLineNotificationService, ProfileHelper profileHelper) {
        this.collectionFileRepository = collectionFileRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyService = policyService;
        this.paymentService = paymentService;
        this.ereceiptService = ereceiptService;
//        this.deductionFileRepository = deductionFileRepository;
        this.lineService = lineService;
        this.paymentRetryEmailService = paymentRetryEmailService;
        this.paymentRetryLineNotificationService = paymentRetryLineNotificationService;
        this.profileHelper = profileHelper;
    }

    public CollectionFile findOne(String collectionFileId) {
        return collectionFileRepository.findOne(collectionFileId);
    }

    public List<CollectionFile> getCollectionFiles() {
        return collectionFileRepository.findAll(new Sort(Sort.Direction.DESC, "receivedDate"));
    }

    @Scheduled(cron = "0 0 9-17 * * ?")
    public void processLatestCollectionFilesJob() {
        processLatestCollectionFiles();
    }

    /**
     * This method must be synchronized to avoid process one collectionFile many times by multi-submit.
     *
     * @return the collection files which has just processed.
     */
    public synchronized List<CollectionFile> processLatestCollectionFiles() {
        Instant startTime = LogUtil.logStarting("Process collection files [start]");
        List<CollectionFile> collectionFiles = collectionFileRepository.findByJobStartedDateNull();
        LOGGER.info("Found [" + collectionFiles.size() + "] collection files to process.");
        for (CollectionFile collectionFile : collectionFiles) {
            processCollectionFile(collectionFile);
        }
        LogUtil.logRuntime(startTime, "Process collection files [finished]");
        return collectionFiles;
    }

    private void processCollectionFile(CollectionFile collectionFile) {
        try {
            collectionFile.setJobStartedDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
            DeductionFile deductionFile = new DeductionFile();
            collectionFile.setDeductionFile(deductionFile);
            LOGGER.info("Found [" + collectionFile.getLines().size() + "] collection(s) line(s) to process.");
            for (CollectionFileLine collectionFileLine : collectionFile.getLines()) {
                processCollectionFileLine(deductionFile, collectionFileLine);
            }
            collectionFile.setJobEndedDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
            collectionFileRepository.save(collectionFile);
        } catch (Exception ex) {
            LOGGER.error("Error while process collection file: " + ex.getMessage() + " \nCollectionFile: " + ObjectMapperUtil.toStringMultiLine(collectionFile), ex);
        }
    }

    public byte[] createDeductionExcelFile(DeductionFile deductionFile) {
        notNull(deductionFile, "No deduction file has been created");
        notNull(deductionFile.getLines(), "No deduction file has been created");
        isTrue(deductionFile.getLines().size() != 0, "No deduction file has been created");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LFPATPTDR6");
        appendRow(sheet, //
                text("M93RPNO6"), // Policy ID
                text("M93RBKCD6"), // Bank Code
                text("M93RPMOD6"), // Payment Mode
                text("M93RPRM6"), // Deposit amount
                text("M93RDOC6"), // Process date
                text("M93RJCD6")); // Rejection Code
        deductionFile.getLines().stream().forEach(deductionFileLine -> createDeductionExcelFileLine(sheet, deductionFileLine));
        ExcelUtils.autoWidthAllColumns(workbook);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }
    }

//
//    private String findLastRegistrationKey(String policyNumber) {
//        Optional<Payment> paymentOptional = paymentService.findLastestPaymentByPolicyNumberAndRegKeyNotNull(policyNumber);
//        if (paymentOptional.isPresent()) {
//            return paymentOptional.get().getRegistrationKey();
//        } else {
//            return null;
//        }
//    }

    //TODO when processing successful, it only generate eReceipt number, not eReceipt Pdf, and also not send eReceipt Pdf to TMC?
    private void processCollectionFileLine(DeductionFile deductionFile, CollectionFileLine collectionFileLine) {
        boolean newBusiness = NEW_BUSINESS;
        LOGGER.info("Processing collectionFileLine [start]: policyNumber: {}", collectionFileLine.getPolicyNumber());

        String paymentId = collectionFileLine.getPaymentId();
        String policyId = collectionFileLine.getPolicyNumber();
        Double premiumAmount = collectionFileLine.getPremiumAmount();
        Policy policy = null;
        Payment payment = null;
        String currencyCode = null;
        String paymentModeString = null;
        String resultMessage = "CollectionFileLine is not processed completely yet: " + ObjectMapperUtil.toStringMultiLine(collectionFileLine);
        String resultCode = RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
        try {
            policy = policyRepository.findByPolicyId(policyId);
            if (policy == null) {
                throw new UnexpectedException("Not found policy " + policyId);
            }
            PeriodicityCode periodicityCode = policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
            paymentModeString = CollectionFileService.PAYMENT_MODE.apply(periodicityCode);
            String productId = policy.getCommonData().getProductId();

            payment = paymentRepository.findOne(paymentId);
            if (payment == null) {
                throw new UnexpectedException("Not found payment " + paymentId);
            }
            currencyCode = payment.getAmount().getCurrencyCode();
            String paymentIdStringSuffix = StringUtils.isNoneBlank(payment.getPaymentId()) ? "_" + payment.getPaymentId() : "";
            String orderId = "R-" + payment.getPolicyId() + "-" + (new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date())) + paymentIdStringSuffix;

            String lastRegistrationKey = paymentService.findLastRegistrationKey(payment.getPolicyId());
            if (StringUtils.isBlank(lastRegistrationKey)) {
                throw new UnexpectedException("Not found registrationKey for policy " + policyId + ", paymentId " + paymentId);
            }
            LinePayRecurringResponse linePayResponse;
            if (mockFailPayment(policy)) {
                linePayResponse = new LinePayRecurringResponse();
                linePayResponse.setReturnCode(LineService.RESPONSE_CODE_ERROR_NO_REGKEY);
                linePayResponse.setReturnMessage("MockFailTest");
            } else {
                linePayResponse = lineService.preApproved(lastRegistrationKey, premiumAmount, currencyCode, productId, orderId);
            }
            resultCode = linePayResponse.getReturnCode();
            resultMessage = linePayResponse.getReturnMessage();
            //We should allow to process any money because user may want to pay more or less.
//            if (Math.abs(premiumAmount - payment.getAmount().getValue()) >= 1) {
//                String msg = String.format("The money in collection file %s is not match with the predefined amount %s", premiumAmount, payment.getAmount());
//                throw new UnexpectedException(msg);
//            }
            payment.getAmount().setValue(premiumAmount);
            payment.setOrderId(orderId);
            if (LineService.RESPONSE_CODE_SUCCESS.equals(resultCode)) {
                //Only generate new ereceiptNumber when payment success.
                EreceiptNumber ereceiptNumber = ereceiptService.generateEreceiptFullNumber(newBusiness);
                payment.setReceiptNumber(ereceiptNumber);
                payment.setNewBusiness(newBusiness);
                payment.setReceiptNumberOldPattern(false);
            }
            paymentService.updateByLinePayResponse(payment, linePayResponse);
        } catch (Exception ex) {
            LOGGER.error("Error when process collection line: " + ex.getMessage() + ". Collection line:%n" + ObjectMapperUtil.toStringMultiLine(collectionFileLine), ex);
            resultCode = RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
            resultMessage = ex.getMessage();
            if (payment != null) {
                paymentService.updatePaymentWithErrorStatus(payment, premiumAmount, currencyCode, LINE, resultCode, resultMessage);
            }
        } finally {
            DeductionFileLine deductionFileLine = initDeductionFileLine(collectionFileLine, paymentModeString, resultCode, resultMessage);
            deductionFile.addLine(deductionFileLine);
            if (hasErrorButNotInternalErrorWhenCallLinePay(resultCode)) {
                if (policy == null || policy.getPolicyId() == null) {
                    LOGGER.warn("Cannot find policyId " + policyId + ", so cannot get information of insured person. Therefore we cannot send inform email to insured customer.");
                } else {
                    setResultOfFailPaymentNotificationToDeductionFileLine(deductionFileLine, policy, payment);
                }
            }
        }
        LOGGER.info("Process collectionFileLine [finished]: policyNumber: {}, paymentId: {}", collectionFileLine.getPolicyNumber(), collectionFileLine.getPaymentId());
    }

    private boolean mockFailPayment(Policy policy) {
        if (profileHelper.containProfile(ProfileHelper.PRODUCTION)) {
            return false;
        } else {
            String insuredEmail = ProductUtils.validateExistMainInsured(policy).getPerson().getEmail();
            if (insuredEmail.contains(FAIL_PROCESSING_EMAIL)) {
                return true;
            } else {
                return false;
            }
        }
    }

    //TODO need to update the list of internal error. E.g: 1106: our server cannot connect to LINE server: should not send email to client.
    private boolean hasErrorButNotInternalErrorWhenCallLinePay(String resultCode) {
        return !resultCode.equals(RESPONSE_CODE_SUCCESS) && !LineService.RESPONSE_CODES_ERROR_BY_INTERNAL_APP.contains(resultCode);
    }

    private void setResultOfFailPaymentNotificationToDeductionFileLine(DeductionFileLine deductionFileLine, Policy policy, Payment payment) {
        String informCustomerCode;
        String informCustomerMessage;
        List<ActionWithResult> notifyActions = new ArrayList<>();
        notifyActions.add(new ActionWithResult("Email") {
            @Override
            public void executeSuccess() {
                paymentRetryEmailService.sendEmail(policy, payment);
                this.resultCode = PaymentFailEmailService.RESPONSE_CODE_EMAIL_SENT_SUCCESS;
                this.resultMessage = "Sent inform email to insured person";
            }
        });
        notifyActions.add(new ActionWithResult("Line notification") {
            @Override
            public void executeSuccess() {
                paymentRetryLineNotificationService.sendNotification(policy, payment);
                this.resultCode = PaymentFailEmailService.RESPONSE_CODE_EMAIL_SENT_SUCCESS;
                this.resultMessage = "Sent inform line notification to insured person";
            }
        });
        notifyActions.stream().forEach(actionWithResult -> actionWithResult.execute());
        informCustomerCode = notifyActions.stream().map(notifyAction -> notifyAction.getActionName() + ": " + notifyAction.getResultCode()).collect(Collectors.joining(", "));
        informCustomerMessage = notifyActions.stream().map(notifyAction -> notifyAction.getResultMessage()).collect(Collectors.joining(", "));
        deductionFileLine.setInformCustomerCode(informCustomerCode);
        deductionFileLine.setInformCustomerMessage(informCustomerMessage);
    }

    private DeductionFileLine initDeductionFileLine(CollectionFileLine collectionFileLine, String paymentMode, String errorCode, String errorMessage) {

        DeductionFileLine deductionFileLine = new DeductionFileLine();
        deductionFileLine.setPaymentId(collectionFileLine.getPaymentId());
        deductionFileLine.setAmount(collectionFileLine.getPremiumAmount());
        deductionFileLine.setBankCode(collectionFileLine.getBankCode());
        deductionFileLine.setPaymentMode(paymentMode);
        deductionFileLine.setPolicyNumber(collectionFileLine.getPolicyNumber());
        deductionFileLine.setProcessDate(LocalDateTime.now());
        deductionFileLine.setRejectionCode(errorCode);
        deductionFileLine.setRejectionMessage(errorMessage);
        return deductionFileLine;
    }

    private void createDeductionExcelFileLine(Sheet sheet, DeductionFileLine deductionFileLine) {
        appendRow(sheet,
                text(deductionFileLine.getPolicyNumber()),
                text(deductionFileLine.getBankCode()),
                text(deductionFileLine.getPaymentMode()),
                text(deductionFileLine.getAmount().toString()),
                text(ofPattern("yyyyMMdd").format(deductionFileLine.getProcessDate())),
                text((deductionFileLine.getRejectionCode().equals("0000") ? "" : deductionFileLine.getRejectionCode())));
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }

}
