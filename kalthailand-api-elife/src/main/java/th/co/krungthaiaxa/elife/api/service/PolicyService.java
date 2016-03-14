package th.co.krungthaiaxa.elife.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.PolicyNumber;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus;
import th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.elife.api.products.Product;
import th.co.krungthaiaxa.elife.api.products.ProductFactory;
import th.co.krungthaiaxa.elife.api.repository.*;
import th.co.krungthaiaxa.elife.api.utils.ThaiBahtUtil;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_IMAGE;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.SUCCESS;

@Service
public class PolicyService {

    private final static Logger logger = LoggerFactory.getLogger(PolicyService.class);

    private final static String ERECEIPT_MERGED_FILE_NAME = "ereceipts_merged.png";
    private final static String ERECEIPT_TEMPLATE_FILE_NAME = "AGENT-WHITE-FINAL.jpg";
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyNumberRepository policyNumberRepository;
    private final QuoteRepository quoteRepository;
    private final DocumentService documentService;

    @Inject
    public PolicyService(PaymentRepository paymentRepository, PolicyRepository policyRepository,
                         PolicyNumberRepository policyNumberRepository, QuoteRepository quoteRepository,
                         DocumentService documentService) {
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyNumberRepository = policyNumberRepository;
        this.quoteRepository = quoteRepository;
        this.documentService = documentService;
    }

    public Policy findPolicy(String policyId) {
        return policyRepository.findByPolicyId(policyId);
    }

    public Policy createPolicy(Quote quote) throws PolicyValidationException, QuoteCalculationException {
        if (quote == null) {
            throw PolicyValidationException.emptyQuote;
        } else if (quote.getId() == null || quoteRepository.findOne(quote.getId()) == null) {
            throw PolicyValidationException.noneExistingQuote;
        }

        Stream<PolicyNumber> availablePolicyNumbers;
        try {
            availablePolicyNumbers = policyNumberRepository.findByPolicyNull();
        } catch (RuntimeException e) {
            throw PolicyValidationException.noPolicyNumberAccessible;
        }

        Optional<PolicyNumber> policyNumber = availablePolicyNumbers.sorted((p1, p2) -> p1.getPolicyId().compareTo(p2.getPolicyId())).findFirst();
        if (!policyNumber.isPresent()) {
            throw PolicyValidationException.noPolicyNumberAvailable;
        }

        Policy policy = policyRepository.findByQuoteId(quote.getId());
        if (policy == null) {
            policy = new Policy();
            policy.setPolicyId(policyNumber.get().getPolicyId());

            Product product = ProductFactory.getProduct(quote.getCommonData().getProductId());
            product.getPolicyFromQuote(policy, quote);

            policy.getPayments().stream().forEach(paymentRepository::save);
            policy = policyRepository.save(policy);
            policyNumber.get().setPolicy(policy);
            policyNumberRepository.save(policyNumber.get());
            quote.setPolicyId(policy.getPolicyId());
            quoteRepository.save(quote);
        }

        return policy;
    }

    public Policy updatePayment(Policy policy, Payment payment, Double value, String currencyCode,
                                Optional<String> registrationKey, SuccessErrorStatus status, ChannelType channelType,
                                Optional<String> creditCardName, Optional<String> paymentMethod,
                                Optional<String> errorCode, Optional<String> errorMessage) throws IOException {
        if (!currencyCode.equals(payment.getAmount().getCurrencyCode())) {
            status = ERROR;
            errorMessage = Optional.of("Currencies are different");
        }

        Amount amount = new Amount();
        amount.setCurrencyCode(currencyCode);
        amount.setValue(value);

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setAmount(amount);
        paymentInformation.setChannel(channelType);
        paymentInformation.setCreditCardName(creditCardName.isPresent() ? creditCardName.get() : null);
        paymentInformation.setDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        paymentInformation.setMethod(paymentMethod.isPresent() ? paymentMethod.get() : null);
        paymentInformation.setRejectionErrorCode(errorCode.isPresent() ? errorCode.get() : null);
        paymentInformation.setRejectionErrorMessage(errorMessage.isPresent() ? errorMessage.get() : null);
        paymentInformation.setStatus(status);
        payment.getPaymentInformations().add(paymentInformation);
        if (registrationKey.isPresent() && !registrationKey.get().equals(payment.getRegistrationKey())) {
            payment.setRegistrationKey(registrationKey.get());
        }

        Double totalSuccesfulPayments = payment.getPaymentInformations()
                .stream()
                .filter(tmp -> tmp.getStatus() != null && tmp.getStatus().equals(SUCCESS))
                .mapToDouble(tmp -> tmp.getAmount().getValue())
                .sum();
        if (totalSuccesfulPayments < payment.getAmount().getValue()) {
            payment.setStatus(PaymentStatus.INCOMPLETE);
        } else if (Objects.equals(totalSuccesfulPayments, payment.getAmount().getValue())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setEffectiveDate(paymentInformation.getDate());
        } else if (totalSuccesfulPayments > payment.getAmount().getValue()) {
            payment.setStatus(PaymentStatus.OVERPAID);
            payment.setEffectiveDate(paymentInformation.getDate());
        }

        byte[] ereceiptImage;
        Optional<Document> documentImage = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_IMAGE)).findFirst();
        if (!documentImage.isPresent()) {
            ereceiptImage = createEreceipt(policy);
            byte[] encodedContent = Base64.getEncoder().encode(ereceiptImage);
            documentService.addDocument(policy, encodedContent, "image/png", ERECEIPT_IMAGE);
        } else {
            ereceiptImage = Base64.getDecoder().decode(documentService.downloadDocument(documentImage.get().getId()).getContent().getBytes());
        }

        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        if (!documentPdf.isPresent()) {
            byte[] encodedContent = Base64.getEncoder().encode(ereceiptImage);
            documentService.addDocument(policy, encodedContent, "application/pdf", ERECEIPT_PDF);
        }

        paymentRepository.save(payment);

        return policy;
    }

    public byte[] createEreceipt(Policy policy) throws IOException {
        logger.info("[createEReceipt] quoteId : " + policy.getQuoteId());
        logger.info("[createEReceipt] policyNumber : " + policy.getPolicyId());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ereceipt/" + ERECEIPT_TEMPLATE_FILE_NAME);

        DecimalFormat formatter = new DecimalFormat("#,##0.00");

        StringBuilder im = new StringBuilder();
        im.append(ERECEIPT_MERGED_FILE_NAME);
        im.insert(im.toString().indexOf("."), "_" + policy.getPolicyId());
        String resultFileName = im.toString();
        logger.info("[createEReceipt] eReceipt file name:" + resultFileName);

        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            logger.error("Unable to read the inputStream of template e-receipt", e);
            throw e;
        }

        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Angsana New", Font.BOLD, 30));

        //Name
        graphics.drawString(policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName(), 227, 305);
        logger.debug("Name Insure : " + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName());

        //Mobile Phone
        String mobilePhone = policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber();
        logger.debug("MobilePhone : " + mobilePhone);
        graphics.drawString(mobilePhone, 633, 305);

        //ProductName
        graphics.drawString(policy.getCommonData().getProductName(), 188, 353);
        logger.debug("ProductName : " + policy.getCommonData().getProductName());

        //SumInsured
        graphics.drawString(formatter.format(policy.getPremiumsData().getProduct10ECPremium().getSumInsured().getValue()), 553, 353);

        //PolicyNo
        char[] numberPNO = policy.getPolicyId().toCharArray();
        graphics.drawString(String.valueOf(numberPNO[0]), 950, 433);
        graphics.drawString(String.valueOf(numberPNO[1]), 977, 433);
        graphics.drawString(String.valueOf(numberPNO[2]), 1004, 433);
        graphics.drawString(String.valueOf(numberPNO[4]), 1060, 433);
        graphics.drawString(String.valueOf(numberPNO[5]), 1088, 433);
        graphics.drawString(String.valueOf(numberPNO[6]), 1114, 433);
        graphics.drawString(String.valueOf(numberPNO[7]), 1143, 433);
        graphics.drawString(String.valueOf(numberPNO[8]), 1170, 433);
        graphics.drawString(String.valueOf(numberPNO[9]), 1197, 433);
        graphics.drawString(String.valueOf(numberPNO[10]), 1225, 433);

        //PaymentMode
        switch (policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()) {
            case EVERY_YEAR:
                graphics.drawString("X", 286, 376);
                break;
            case EVERY_HALF_YEAR:
                graphics.drawString("X", 390, 376);
                break;
            case EVERY_QUARTER:
                graphics.drawString("X", 541, 376);
                break;
            case EVERY_MONTH:
                graphics.drawString("X", 692, 376);
                break;
            default:
                logger.error("Invalid PaymentMode");
        }
        //Memo
        graphics.drawString("X", 89, 439);
        //ID-Card
        char[] numberIds = policy.getInsureds().get(0).getPerson().getRegistrations().get(0).getId().toCharArray();
        graphics.drawString(String.valueOf(numberIds[0]), 896, 495);
        graphics.drawString(String.valueOf(numberIds[1]), 924, 495);
        graphics.drawString(String.valueOf(numberIds[2]), 951, 495);
        graphics.drawString(String.valueOf(numberIds[3]), 979, 495);
        graphics.drawString(String.valueOf(numberIds[4]), 1007, 495);
        graphics.drawString(String.valueOf(numberIds[5]), 1035, 495);
        graphics.drawString(String.valueOf(numberIds[6]), 1061, 495);
        graphics.drawString(String.valueOf(numberIds[7]), 1089, 495);
        graphics.drawString(String.valueOf(numberIds[8]), 1119, 495);
        graphics.drawString(String.valueOf(numberIds[9]), 1145, 495);
        graphics.drawString(String.valueOf(numberIds[10]), 1173, 495);
        graphics.drawString(String.valueOf(numberIds[11]), 1201, 495);
        graphics.drawString(String.valueOf(numberIds[12]), 1230, 495);
        //REF2
        graphics.drawString("M", 974, 576);
        graphics.drawString(String.valueOf(numberPNO[2]), 1002, 576);
        graphics.drawString(String.valueOf(numberPNO[4]), 1030, 576);
        graphics.drawString(String.valueOf(numberPNO[5]), 1057, 576);
        graphics.drawString(String.valueOf(numberPNO[6]), 1087, 576);
        graphics.drawString(String.valueOf(numberPNO[7]), 1113, 576);
        graphics.drawString(String.valueOf(numberPNO[8]), 1140, 576);
        graphics.drawString(String.valueOf(numberPNO[9]), 1196, 576);
        graphics.drawString(String.valueOf(numberPNO[10]), 1224, 576);
        //CreditCard
        graphics.drawString("X", 89, 596);
        //Number Premiums
        graphics.drawString(formatter.format(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 1026, 644);
        //Letter Premiums
        graphics.drawString(new ThaiBahtUtil().getText(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 296, 643);
        graphics.drawString("Krungthai-AXA life Online", 879, 714);
        graphics.drawString("Krungthai-AXA life Online", 879, 800);
        //Agent Code
        graphics.drawString("0", 851, 892);
        graphics.drawString("4", 879, 892);
        graphics.drawString("0", 905, 892);
        graphics.drawString("0", 933, 892);
        graphics.drawString("0", 962, 892);
        graphics.drawString("0", 989, 892);
        graphics.drawString("0", 1025, 892);
        graphics.drawString("4", 1052, 892);
        graphics.drawString("0", 1088, 892);
        graphics.drawString("0", 1116, 892);
        graphics.drawString("0", 1145, 892);
        graphics.drawString("0", 1172, 892);
        graphics.drawString("0", 1200, 892);
        graphics.drawString("1", 1229, 892);
        graphics.drawString("ไลน์เพย์ (LINE Pay)", 246, 598);

        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", baos);
            bytes = baos.toByteArray();
            logger.info("[createEReceipt] Generating Base64...");
        } catch (IOException e) {
            logger.error("Unable to write image e-receipt to byteArrayOutputStream : " + resultFileName, e);
            throw e;
        }

        return bytes;
    }
}
