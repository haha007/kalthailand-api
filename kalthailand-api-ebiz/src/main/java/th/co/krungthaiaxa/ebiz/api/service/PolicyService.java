package th.co.krungthaiaxa.ebiz.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType;
import th.co.krungthaiaxa.ebiz.api.model.enums.PaymentStatus;
import th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.ebiz.api.products.Product10EC;
import th.co.krungthaiaxa.ebiz.api.repository.PaymentRepository;
import th.co.krungthaiaxa.ebiz.api.repository.PolicyRepository;
import th.co.krungthaiaxa.ebiz.api.repository.QuoteRepository;
import th.co.krungthaiaxa.ebiz.api.utils.ThaiBahtUtil;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

import static th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus.SUCCESS;

@Service
public class PolicyService {

    private final static Logger logger = LoggerFactory.getLogger(PolicyService.class);
    @Value("${path.store.elife.ereceipt}")
    private String eReceiptStorePath;
    @Value("${path.store.elife.ereceipt.image}")
    private String eReceiptImageStorePath;
    @Value("${path.store.elife.ereceipt.pdf}")
    private String eReceiptPdfStorePath;
    @Value("${path.store.elife.ereceipt.mail.logo}")
    private String eReceiptMailLogoStorePath;

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final QuoteRepository quoteRepository;
    @Value("${policy.number.prefix}")
    private String policyNumberPrefix;

    @Inject
    public PolicyService(PaymentRepository paymentRepository, PolicyRepository policyRepository, QuoteRepository quoteRepository) {
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.quoteRepository = quoteRepository;
    }

    public Policy findPolicy(String policyId) {
        return policyRepository.findOne(policyId);
    }

    public Policy createPolicy(Quote quote) throws Exception {
        if (quote == null) {
            throw PolicyValidationException.emptyQuote;
        } else if (quote.getTechnicalId() == null || quoteRepository.findOne(quote.getTechnicalId()) == null) {
            throw PolicyValidationException.noneExistingQuote;
        }

        Policy policy = policyRepository.findByQuoteFunctionalId(quote.getTechnicalId());
        if (policy == null) {
            policy = new Policy();
            policy.setPolicyId(policyNumberPrefix + RandomStringUtils.randomNumeric(11));
            // Only one product so far
            Product10EC.getPolicyFromQuote(policy, quote);
            policy.getPayments().stream().forEach(paymentRepository::save);
            policy = policyRepository.save(policy);
        }

        return policy;
    }

    public Payment updatePayment(String paymentId, Double value, String currencyCode, String registrationKey,
                                 SuccessErrorStatus status, ChannelType channelType, String creditCardName,
                                 String paymentMethod, String errorMessage) {
        Payment payment = paymentRepository.findOne(paymentId);

        if (!currencyCode.equals(payment.getAmount().getCurrencyCode())) {
            status = ERROR;
            errorMessage = "Currencies are different";
        }

        Amount amount = new Amount();
        amount.setCurrencyCode(currencyCode);
        amount.setValue(value);

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setAmount(amount);
        paymentInformation.setChannel(channelType);
        paymentInformation.setCreditCardName(creditCardName);
        paymentInformation.setDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        paymentInformation.setMethod(paymentMethod);
        paymentInformation.setRejectionErrorMessage(errorMessage);
        paymentInformation.setStatus(status);
        payment.getPaymentInformations().add(paymentInformation);
        if (registrationKey != null && !registrationKey.equals(payment.getRegistrationKey())) {
            payment.setRegistrationKey(registrationKey);
        }

        Double totalSuccesfulPayments = payment.getPaymentInformations()
                .stream()
                .filter(tmp -> tmp.getStatus() != null && tmp.getStatus().equals(SUCCESS))
                .mapToDouble(tmp -> tmp.getAmount().getValue())
                .sum();
        if (totalSuccesfulPayments < payment.getAmount().getValue()) {
            payment.setStatus(PaymentStatus.INCOMLETE);
        } else if (Objects.equals(totalSuccesfulPayments, payment.getAmount().getValue())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setEffectiveDate(paymentInformation.getDate());
        } else if (totalSuccesfulPayments > payment.getAmount().getValue()) {
            payment.setStatus(PaymentStatus.OVERPAID);
            payment.setEffectiveDate(paymentInformation.getDate());
        }

        return paymentRepository.save(payment);
    }

    public byte[] createEreceipt(Policy policy) throws Exception {

        logger.info("[def] createEReceipt");
        logger.info("[createEReceipt] quoteId : "+ policy.getQuoteFunctionalId());
        logger.info("[createEReceipt] policyNumber : "+ policy.getPolicyId());
        logger.info("[createEReceipt] E-receipt template store name path : "+ eReceiptStorePath);
        logger.info("[createEReceipt] E-receipt image store name path : " + eReceiptImageStorePath);
        try {

            DecimalFormat formatter = new DecimalFormat("#,##0.00");

            StringBuilder im = new StringBuilder(eReceiptImageStorePath);
            im.insert(eReceiptImageStorePath.indexOf("."),policy.getPolicyId());
            eReceiptImageStorePath = im.toString();
            logger.info("[createEReceipt] Name Image File["+policy.getPolicyId()+"]:" + eReceiptImageStorePath);

            BufferedImage bufferedImage = ImageIO.read(new File(eReceiptStorePath));
            Graphics graphics = bufferedImage.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Angsana New", Font.BOLD, 30));

            graphics.drawString(policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName(), 227, 305);

            String phone = policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber().toString();

            graphics.drawString(phone, 633, 305);
            graphics.drawString(policy.getCommonData().getProductName(), 188, 353);
            graphics.drawString(formatter.format(policy.getPremiumsData().getLifeInsuranceSumInsured().getValue()), 553, 353);
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
            switch (policy.getPayments().get(0).getPaymentInformations().get(0).getMethod()) {
                case "12":
                    graphics.drawString("X", 286, 376);
                    break;
                case "6":
                    graphics.drawString("X", 390, 376);
                    break;
                case "3":
                    graphics.drawString("X", 541, 376);
                    break;
                case "1":
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

            graphics.drawString(formatter.format(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 1026, 644);
            graphics.drawString(new ThaiBahtUtil().getText(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 296, 643);
            graphics.drawString("Krungthai-AXA life Online", 879, 714);
            graphics.drawString("Krungthai-AXA life Online", 879, 800);
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
            logger.info("[createEReceipt] success for drawing graphics on image.");
            ImageIO.write(bufferedImage, "jpg", new File(eReceiptImageStorePath));
            logger.info("[createEReceipt] write image file success : " + eReceiptImageStorePath );

            ByteArrayOutputStream baos  = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] bytes   = baos.toByteArray();

            logger.info("[createEReceipt] Generating Base64...");

            baos.close();
            return bytes;
        }catch(Exception e){
            logger.error(null,e);
            throw e;
        }
    }


}
