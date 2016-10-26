package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.NumberUtil;
import th.co.krungthaiaxa.api.common.utils.PdfIOUtil;
import th.co.krungthaiaxa.api.common.utils.PdfUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.PersonUtil;
import th.co.krungthaiaxa.api.elife.utils.ThaiBahtUtil;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author khoi.tran on 10/25/16.
 */
@Service
public class EreceiptPdfService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);
    private final static String ERECEIPT_TEMPLATE_FILE_NAME = "/ereceipt/EreceiptTemplate.pdf";

    /**
     * When you use invertY is false, this method will write the first line with position information on it.
     */
    private static final float Y_TOP_WHICH_DRAWN_IN_NORMAL_POSITION = 574.0f;
    /**
     * Open the template file, use inspect tool (Preview) to see the position of the first line.
     */
    private static final float Y_TOP_INSPECT = 97.48f;

    private final static int LINE_POS_REF2 = 576;//px
    private static final int LINE_POS_PERSON_REGID = 495;//px
    private final static double CELL_WIDTH = 28.0;

    //PAGE 01 ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //LEFT ...................................................................
    //line 1:
    private static final Point POS_MAIN_INSURED_FULL_NAME = new Point(156, 249);//line 1: 240
    private static final Point POS_MAIN_INSURED_MOBILE_PHONE = new Point(362, 249);

    //line 2:

    //line 3:
    private static final Point POS_PRODUCT_NAME = new Point(135, 274);
    private static final Point POS_SUM_INSURED = new Point(315, 274);

    //line 4:
    private static final Point POS_PAYMENT_PERIODICITY_YEARLY = new Point(193, 284);
    private static final Point POS_PAYMENT_PERIODICITY_HAFTYEAR = new Point(246, 284);
    private static final Point POS_PAYMENT_PERIODICITY_QUARTER = new Point(320, 284);
    private static final Point POS_PAYMENT_PERIODICITY_MONTHLY = new Point(396, 284);

    //line 6:
    private static final Point POS_PAYMENT_MODE_NEW_BUSINESS = new Point(95, 316);
    //line 7:
    private static final Point POS_PAYMENT_MODE_RENEWAL = new Point(95, 328);
    //line 8: don't use it yet
    private static final Point POS_PAYMENT_MODE_AUTO_PREMIUM_LOAN = new Point(95, 340);//Not used yet (2016-10-26)
    //line 9: don't use it yet
    private static final Point POS_PAYMENT_MODE_NEW_ISSUED_POLICY = new Point(95, 352);//Not used yet (2016-10-26)

    //line 10:
    private static final Point POS_PAYMENT_METHOD_CASH_OPTION = new Point(213, 371);//Not used yet (2016-10-26)
    private static final Point POS_PAYMENT_METHOD_OTHER_OPTION = new Point(304, 370);
    private static final Point POS_PAYMENT_METHOD_OTHER_TEXT = new Point(335, 371);

    //line 11:
    private static final Point POS_PAYMENT_DATE = new Point(155, 0);//Not sure what the date is. //Not used yet (2016-10-26)

    //line 12:
    private static final Point POS_PAYMENT_CREDIT_NUMBER_CHECKER = new Point(95, 0);//Not used yet (2016-10-26)
    private static final Point POS_PAYMENT_CREDIT_NUMBER = new Point(155, 0);//Not used yet (2016-10-26)
    private static final Point POS_PAYMENT_EXPIRED_DATE = new Point(155, 0);//Not used yet (2016-10-26)

    //line 13: Should on the same line of POS_PREMIUM_VALUE_IN_NUMBERS
    private static final Point POS_PREMIUM_VALUE_IN_THAI_LETTERS = new Point(180, 417);

    //RIGHT ...................................................................
    //line 1:
    private static final Point POS_PAYMENT_EFFECTIVE_DATE = new Point(547, 279);

    //line 2:
    private static final Point POS_REF1_POLICY_NUMBER_PREFIX = new Point(524, 312);
    private static final Point POS_REF1_POLICY_NUMBER_SUFFIX = new Point(580, 312);

    //line 3:
    private static final Point POS_REF1_ID_CARD = new Point(497, 343);

    //line 4:
    private static final Point POS_REF2_RECEIPT_NUMBER = new Point(560, 386);

    //line 5: Should on the same line of POS_PREMIUM_VALUE_IN_THAI_LETTERS
    private static final Point POS_PREMIUM_VALUE_IN_NUMBERS = new Point(560, 417);

    //line 6 & 7: 447 & 489
    private static final Point POS_TMC_AGENCY_NAME_01 = new Point(475, 456);
    private static final Point POS_TMC_AGENCY_NAME_02 = new Point(475, 498);

    //line 8:
    private static final Point POS_TMC_AGENCY_CODE_PART01 = new Point(475, 541);
    private static final Point POS_TMC_AGENCY_CODE_PART02 = new Point(563, 541);
    private static final Point POS_TMC_AGENCY_CODE_PART03 = new Point(595, 541);

    private final DocumentService documentService;
    private BaseFont baseFont = getBaseFont();

    @Autowired
    public EreceiptPdfService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * @param policy
     * @param payment
     * @param firstPayment If this is the new business payment, the value is "true". If this is the renewal payment, it's "false".
     * @return
     */
    public byte[] createEreceiptPdf(Policy policy, Payment payment, boolean firstPayment) {
        InputStream pdfTemplateInputStream = IOUtil.loadInputStreamFromClassPath(ERECEIPT_TEMPLATE_FILE_NAME);
        return createEreceiptPdf(pdfTemplateInputStream, policy, payment, firstPayment);
    }

    public byte[] createEreceiptPdf(InputStream pdfTemplateInputStream, Policy policy, Payment payment, boolean firstPayment) {
        LOGGER.debug("Generate eReceipt pdf [started]: %n\t policyId: {}%n\t payment: {}%n\t firstPayment: {}", policy.getPolicyId(), payment.getPaymentId(), firstPayment);
        try (ByteArrayOutputStream content = new ByteArrayOutputStream()) {
            PdfReader pdfReader = new PdfReader(pdfTemplateInputStream);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, content);
            writePage01(pdfStamper.getOverContent(1), policy, payment, firstPayment);
//            writePagePosition(pdfStamper.getOverContent(1), policy, payment, firstPayment);

            //page02 is the same as template file, we don't need to change anything.
            pdfStamper.close();
            content.close();
            byte[] result = content.toByteArray();
            LOGGER.debug("Generate eReceipt pdf [finished]: %n\t policyId: {}%n\t payment: {}%n\t firstPayment: {}", policy.getPolicyId(), payment.getPaymentId(), firstPayment);
            return result;
        } catch (DocumentException | IOException ex) {
            throw new FileIOException(String.format("Generate eReceipt pdf [error]: %n\t policyId: %s %n\t payment: %s %n\t firstPayment: %s %n\tError message: %s", policy.getPolicyId(), payment.getPaymentId(), firstPayment, ex.getMessage()), ex);
        }
    }

    private void writePage01(PdfContentByte page, Policy policy, Payment payment, boolean firstPayment) {
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        String mainInsuredFullName = PersonUtil.getFullName(mainInsured.getPerson());
        writeText(page, mainInsuredFullName, POS_MAIN_INSURED_FULL_NAME);

        String mainInsuredMobilePhone = PersonUtil.getMobilePhoneNumber(mainInsured.getPerson());
        writeText(page, mainInsuredMobilePhone, POS_MAIN_INSURED_MOBILE_PHONE);

        String productName = ProductUtils.validateExistProductName(policy);
        writeText(page, productName, POS_PRODUCT_NAME);

        Double sumInsuredValue = ProductUtils.getSumInsuredAmount(policy).getValue();
        writeCurrencyValue(page, sumInsuredValue, POS_SUM_INSURED);

        writePaymentPeriodicity(page, policy);

        writePaymentRenewalMode(page, firstPayment);

        writePaymentMethod(page);

        writePremiumValue(page, policy);

        writeThaiDateIfExist(page, payment.getEffectiveDate(), POS_PAYMENT_EFFECTIVE_DATE);

        writePolicyNumber(page, policy);

        String mainInsuredRegistrationIdCard = ProductUtils.getRegistrationId(mainInsured);
        writeChars(page, POS_REF1_ID_CARD, 13.94, 13, mainInsuredRegistrationIdCard.toCharArray());

//        writeReceiptNumber(page);

        String agentName = policy.getValidationAgentName();
        if (StringUtils.isNotBlank(agentName)) {
            writeText(page, agentName, POS_TMC_AGENCY_NAME_01);
            writeText(page, agentName, POS_TMC_AGENCY_NAME_02);
        }

        writeAgentCode(page, policy);
    }

    private void writeAgentCode(PdfContentByte page, Policy policy) {
        String agentCode = policy.getValidationAgentCode();
        String[] parts = agentCode.split("-");
        writeChars(page, POS_TMC_AGENCY_CODE_PART01, 13.94, 6, parts[0].toCharArray());
        writeChars(page, POS_TMC_AGENCY_CODE_PART02, 13.94, 2, parts[1].toCharArray());
        writeChars(page, POS_TMC_AGENCY_CODE_PART03, 13.94, 6, parts[2].toCharArray());
    }

    private void writePolicyNumber(PdfContentByte page, Policy policy) {
        String policyNumber = policy.getPolicyId();
        String[] policyNumberParts = policyNumber.split("-");
        char[] policyNumberPrefixChars = policyNumberParts[0].toCharArray();
        char[] policyNumberSuffixChars = policyNumberParts[1].toCharArray();
        writeChars(page, POS_REF1_POLICY_NUMBER_PREFIX, 13.94, 3, policyNumberPrefixChars);
        writeChars(page, POS_REF1_POLICY_NUMBER_SUFFIX, 13.94, 7, policyNumberSuffixChars);
    }

    private void writeThaiDateIfExist(PdfContentByte page, LocalDateTime localDateTime, Point point) {
        if (localDateTime == null) {
            return;
        }
        String thaiDateString = getThaiDate(localDateTime);
        writeText(page, thaiDateString, point);
    }

    private void writeChars(PdfContentByte page, Point startPosition, Double charWidth, int maxChar, char... chars) {
        int length = Math.min(chars.length, maxChar);
        for (int i = 0; i < length; i++) {
            char ichar = chars[i];
            Point ipoint = new Point(startPosition.x + (int) Math.round(charWidth * i), startPosition.y);
            writeText(page, String.valueOf(ichar), ipoint);
        }
    }

    private void writePremiumValue(PdfContentByte page, Policy policy) {
        Double premium = ProductUtils.getPremiumAmount(policy).getValue();
        String premiumInText = ThaiBahtUtil.getText(premium);
        String premiumInNum = NumberUtil.formatCurrencyValue(premium);
        writeText(page, premiumInText, POS_PREMIUM_VALUE_IN_THAI_LETTERS);
        writeText(page, premiumInNum, POS_PREMIUM_VALUE_IN_NUMBERS);
    }

    private void writePaymentMethod(PdfContentByte page) {
        //For now (2016-10-26), this method only support payment by linePay, so the "Other" option is always selected.
        Point posPaymentMethod = POS_PAYMENT_METHOD_OTHER_OPTION;
        writeText(page, "X", posPaymentMethod);
        writeText(page, "ไลน์เพย์(LINE Pay)", POS_PAYMENT_METHOD_OTHER_TEXT);
    }

    private void writePaymentPeriodicity(PdfContentByte page, Policy policy) {
        PeriodicityCode periodicityCode = ProductUtils.getPeriodicityCode(policy);
        Point posPeriodicity;
        switch (periodicityCode) {
        case EVERY_YEAR:
            posPeriodicity = POS_PAYMENT_PERIODICITY_YEARLY;
            break;
        case EVERY_HALF_YEAR:
            posPeriodicity = POS_PAYMENT_PERIODICITY_HAFTYEAR;
            break;
        case EVERY_QUARTER:
            posPeriodicity = POS_PAYMENT_PERIODICITY_QUARTER;
            break;
        case EVERY_MONTH:
            posPeriodicity = POS_PAYMENT_PERIODICITY_MONTHLY;
            break;
        default:
            throw new UnexpectedException("Unsupported periodicity code: " + periodicityCode);
        }
        writeText(page, "X", posPeriodicity);
    }

    private void writePaymentRenewalMode(PdfContentByte page, boolean firstPayment) {
        Point pos = firstPayment ? POS_PAYMENT_MODE_NEW_BUSINESS : POS_PAYMENT_MODE_RENEWAL;
        writeText(page, "X", pos);
    }

    private void writeCurrencyValue(PdfContentByte page, double currencyValue, Point point) {
        writeText(page, NumberUtil.formatCurrencyValue(currencyValue), point);
    }

    private void writeText(PdfContentByte page, String text, Point point) {
        float actualYInTemplate = Y_TOP_WHICH_DRAWN_IN_NORMAL_POSITION + Y_TOP_INSPECT - point.y;
        PdfUtil.writeText(page, baseFont, text, (float) point.getX(), actualYInTemplate, PdfUtil.MEDIUM_SIZE);
    }

    private BaseFont getBaseFont() {
        return PdfIOUtil.loadFontFromClassPath("ANGSAB_1.ttf", "/ereceipt/ANGSAB_1.TTF");
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
