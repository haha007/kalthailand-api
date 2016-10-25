package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.exception.EreceiptDocumentException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentReferenceType;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.utils.PersonUtil;
import th.co.krungthaiaxa.api.elife.utils.ThaiBahtUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

import static com.itextpdf.text.PageSize.A4;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_IMAGE;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;

/**
 * @author khoi.tran on 10/25/16.
 */
@Service
public class EreceiptService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);
    private final static String ERECEIPT_MERGED_FILE_NAME = "ereceipts_merged.png";
    private final static String ERECEIPT_TEMPLATE_FILE_NAME = "AGENT-WHITE-FINAL.jpg";
    private final static int LINE_POS_REF2 = 576;//px
    private static final int LINE_POS_PERSON_REGID = 495;//px
    private final static double CELL_WIDTH = 28.0;

    private final DocumentService documentService;
    private final PaymentRepository paymentRepository;
    private final SigningClient signingClient;

    @Autowired
    public EreceiptService(DocumentService documentService, PaymentRepository paymentRepository, SigningClient signingClient) {
        this.documentService = documentService;
        this.paymentRepository = paymentRepository;
        this.signingClient = signingClient;
    }

    /**
     * @param policy
     * @param payment
     * @param firstPayment
     * @param accessToken  it will be used for sign document
     * @return
     * @throws EreceiptDocumentException if there's something wrong while creating ereceipt pdf.
     */
    public Document addEreceiptPdf(Policy policy, Payment payment, boolean firstPayment, String accessToken) {
        try {
            byte[] ereceiptImage = createEreceiptImage(policy, payment, firstPayment);
            addEReceiptDocument(policy, payment, ereceiptImage, "image/png", ERECEIPT_IMAGE);
            byte[] decodedNonSignedPdf = createEreceiptPDF(ereceiptImage);
            byte[] encodedNonSignedPdf = Base64.getEncoder().encode(decodedNonSignedPdf);
            byte[] encodedSignedPdf = signingClient.getEncodedSignedPdfDocument(encodedNonSignedPdf, accessToken);
            byte[] decodedSignedPdf = Base64.getDecoder().decode(encodedSignedPdf);
            return addEReceiptDocument(policy, payment, decodedSignedPdf, "application/pdf", ERECEIPT_PDF);
        } catch (IOException | DocumentException e) {
            String msg = String.format("Error creating ereceipt pdf for policyId: %s, paymentId: %s, firstPayment: %s", policy.getPolicyId(), payment.getPaymentId(), firstPayment);
            throw new EreceiptDocumentException(msg);
        }
    }

    public Document addEReceiptDocument(Policy policy, Payment payment, byte[] decodedContent, String mimeType, DocumentType documentType) {
        Document document = documentService.addDocument(policy, decodedContent, mimeType, documentType, DocumentReferenceType.PAYMENT, payment.getPaymentId());
        if (documentType == DocumentType.ERECEIPT_IMAGE) {
            payment.setReceiptImageDocument(document);
        } else if (documentType == DocumentType.ERECEIPT_PDF) {
            payment.setReceiptPdfDocument(document);
        } else {
            throw new UnexpectedException("There's something really wrong here. You can only call this method if your documentType is either " + DocumentType.ERECEIPT_IMAGE + " or " + DocumentType.ERECEIPT_PDF);
        }
        paymentRepository.save(payment);
        return document;
    }

    /**
     * @param policy
     * @param payment
     * @param firstPayment is it the new business (true) or renewal (false)
     * @return
     * @throws IOException
     */
    public byte[] createEreceiptImage(Policy policy, Payment payment, boolean firstPayment) throws IOException {
        LOGGER.info("[createEReceipt] quoteId : " + policy.getQuoteId());
        LOGGER.info("[createEReceipt] policyNumber : " + policy.getPolicyId());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ereceipt/" + ERECEIPT_TEMPLATE_FILE_NAME);

        DecimalFormat formatter = new DecimalFormat("#,##0.00");

        StringBuilder im = new StringBuilder();
        im.append(ERECEIPT_MERGED_FILE_NAME);
        im.insert(im.toString().indexOf("."), "_" + policy.getPolicyId());
        String resultFileName = im.toString();
        LOGGER.info("[createEReceipt] eReceipt file name:" + resultFileName);

        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            LOGGER.error("Unable to read the inputStream of template e-receipt", e);
            throw e;
        }

        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(Color.BLACK);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(30f);
            graphics.setFont(f);
        } catch (FontFormatException e) {
            LOGGER.error("Unable to load embed font file", e);
            throw new IOException(e);
        }

        //Name
        Person mainInsuredPerson = ProductUtils.validateExistMainInsured(policy).getPerson();
        String mainInsuredName = PersonUtil.getFirstNameAndLastName(mainInsuredPerson);
        graphics.drawString(mainInsuredName, 227, 305);
        LOGGER.debug("Name Insure : " + mainInsuredName);

        //payment date
        if (payment.getEffectiveDate() != null) {
            graphics.drawString(getThaiDate(payment.getEffectiveDate()), 980, 365);
            LOGGER.debug("Payment Date : " + payment.getEffectiveDate());
        }

        //Mobile Phone
        String mobilePhone = policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber();
        LOGGER.debug("MobilePhone : " + mobilePhone);
        graphics.drawString(mobilePhone, 633, 305);

        //ProductName
        String productLogicName = policy.getCommonData().getProductId();
        ProductType productType = ProductUtils.validateExistProductTypeByLogicName(productLogicName);
        String productDisplayName = productType.getDisplayName();
        graphics.drawString(productDisplayName, 188, 353);
        LOGGER.debug("ProductName : " + productDisplayName);

        //SumInsured
        Double premium = 0.0;
        if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getLogicName())) {
            premium = policy.getPremiumsData().getProduct10ECPremium().getSumInsured().getValue();
        } else if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getLogicName())) {
            premium = policy.getPremiumsData().getProductIFinePremium().getSumInsured().getValue();
        } else if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_IPROTECT.getLogicName())) {
            premium = policy.getPremiumsData().getProductIProtectPremium().getSumInsured().getValue();
        } else {
            premium = policy.getPremiumsData().getPremiumDetail().getSumInsured().getValue();
        }
        graphics.drawString(formatter.format(premium), 553, 353);

        //PolicyNo
        String policyNumber = policy.getPolicyId();
        char[] policyNumberChars = policyNumber.toCharArray();
        graphics.drawString(String.valueOf(policyNumberChars[0]), 950, 433);
        graphics.drawString(String.valueOf(policyNumberChars[1]), 977, 433);
        graphics.drawString(String.valueOf(policyNumberChars[2]), 1004, 433);
        graphics.drawString(String.valueOf(policyNumberChars[4]), 1060, 433);
        graphics.drawString(String.valueOf(policyNumberChars[5]), 1088, 433);
        graphics.drawString(String.valueOf(policyNumberChars[6]), 1114, 433);
        graphics.drawString(String.valueOf(policyNumberChars[7]), 1143, 433);
        graphics.drawString(String.valueOf(policyNumberChars[8]), 1170, 433);
        graphics.drawString(String.valueOf(policyNumberChars[9]), 1197, 433);
        graphics.drawString(String.valueOf(policyNumberChars[10]), 1225, 433);

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
            LOGGER.error("Invalid PaymentMode");
        }
        //Memo
        if (firstPayment) {
            graphics.drawString("X", 89, 439);
        } else {
            graphics.drawString("X", 89, 465);
        }

        //ID-Card
        char[] personRegistrationIdChars = mainInsuredPerson.getRegistrations().get(0).getId().toCharArray();
        drawPersonRegistrationId(graphics, personRegistrationIdChars);

        //REF2
        drawRef2Prefix(graphics,
                'M',
                policyNumberChars[2],
                policyNumberChars[4],
                policyNumberChars[5],
                policyNumberChars[6],
                policyNumberChars[7],
                policyNumberChars[8]
        );
        char[] ref2SuffixChars = new char[] { policyNumberChars[9], policyNumberChars[10] };
        if (!firstPayment) {
            ref2SuffixChars = "02".toCharArray();
        }
        drawRef2Suffix(graphics, ref2SuffixChars);

        //CreditCard
        graphics.drawString("X", 89, 596);
        //Number Premiums
        graphics.drawString(formatter.format(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 1026, 644);
        //Letter Premiums
        graphics.drawString(new ThaiBahtUtil().getText(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 296, 643);

        //TMC Agent Code
        if (policy.getValidationAgentName() != null) {
            graphics.drawString(policy.getValidationAgentName(), 879, 714);
            graphics.drawString(policy.getValidationAgentName(), 879, 800);
        }

        //TMC Agent Code
        if (policy.getValidationAgentCode() != null) {
            char[] tmcAgentCode = policy.getValidationAgentCode().toCharArray();
            graphics.drawString(String.valueOf(tmcAgentCode[0]), 851, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[1]), 879, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[2]), 905, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[3]), 933, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[4]), 962, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[5]), 989, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[7]), 1025, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[8]), 1052, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[10]), 1088, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[11]), 1116, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[12]), 1145, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[13]), 1172, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[14]), 1200, 892);
            graphics.drawString(String.valueOf(tmcAgentCode[15]), 1229, 892);
        }

        graphics.drawString("ไลน์เพย์ (LINE Pay)", 246, 598);

        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", baos);
            bytes = baos.toByteArray();
            LOGGER.info("[createEReceipt] Generating Base64...");
        } catch (IOException e) {
            LOGGER.error("Unable to write image e-receipt to byteArrayOutputStream : " + resultFileName, e);
            throw e;
        }

        return bytes;
    }

    public byte[] createEreceiptPDF(byte[] eReceiptImage) throws DocumentException, IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, content);
        document.open();

        PdfContentByte canvas1 = writer.getDirectContentUnder();
        com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(eReceiptImage);
        image.scaleAbsolute(A4);
        image.scalePercent(50);
        image.setAbsolutePosition(120, 80);
        canvas1.addImage(image);

        document.close();
        content.close();
        return content.toByteArray();
    }

    private void drawPersonRegistrationId(Graphics graphics, char... chars) {
        drawCharsInLine(graphics, 892, LINE_POS_PERSON_REGID, CELL_WIDTH, 13, chars);
    }

    private void drawCharsInLine(Graphics graphics, int startCharPos, int linePos, Double charWidth, int maxChar, char... chars) {
        int length = Math.min(chars.length, maxChar);
        for (int i = 0; i < length; i++) {
            char ichar = chars[i];
            graphics.drawString(String.valueOf(ichar), startCharPos + (int) Math.round(charWidth * i), linePos);
        }
    }

    /**
     * @param graphics
     * @param prefixChars 7 chars
     */
    private void drawRef2Prefix(Graphics graphics, char... prefixChars) {
        drawCharsInLine(graphics, 973, LINE_POS_REF2, CELL_WIDTH, 7, prefixChars);
    }

    /**
     * @param graphics
     * @param suffixChars 2 chars. For new business, it should be 01, for renewal, it should be 02.
     */
    private void drawRef2Suffix(Graphics graphics, char... suffixChars) {
        drawCharsInLine(graphics, 1197, LINE_POS_REF2, CELL_WIDTH, 2, suffixChars);
    }

    private String getThaiDate(LocalDate localDate) {
        return DateTimeUtil.formatBuddhistThaiDate(localDate);
    }

    private String getThaiDate(LocalDateTime localDate) {
        return DateTimeUtil.formatBuddhistThaiDate(localDate);
    }
}
