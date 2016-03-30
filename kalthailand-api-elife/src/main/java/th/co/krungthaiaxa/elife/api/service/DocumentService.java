package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.Document;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.enums.DocumentType;
import th.co.krungthaiaxa.elife.api.repository.DocumentDownloadRepository;
import th.co.krungthaiaxa.elife.api.repository.DocumentRepository;
import th.co.krungthaiaxa.elife.api.repository.PolicyRepository;
import th.co.krungthaiaxa.elife.api.utils.ThaiBahtUtil;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Base64;
import java.util.Optional;

import static com.itextpdf.text.PageSize.A4;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.*;

@Service
public class DocumentService {
    private final static Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final static String ERECEIPT_MERGED_FILE_NAME = "ereceipts_merged.png";
    private final static String ERECEIPT_TEMPLATE_FILE_NAME = "AGENT-WHITE-FINAL.jpg";

    private final DocumentRepository documentRepository;
    private final DocumentDownloadRepository documentDownloadRepository;
    private final PolicyRepository policyRepository;
    private final ApplicationFormService applicationFormService;
    private final DAFormService daFormService;

    @Inject
    public DocumentService(DocumentRepository documentRepository,
                           DocumentDownloadRepository documentDownloadRepository,
                           PolicyRepository policyRepository,
                           ApplicationFormService applicationFormService,
                           DAFormService daFormService) {
        this.documentRepository = documentRepository;
        this.documentDownloadRepository = documentDownloadRepository;
        this.policyRepository = policyRepository;
        this.applicationFormService = applicationFormService;
        this.daFormService = daFormService;
    }

    public DocumentDownload downloadDocument(String documentId) {
        return documentDownloadRepository.findByDocumentId(documentId);
    }

    /**
     * This moethods encodes the content in Base64 by default
     *
     * @param policy
     * @param decodedContent
     * @param mimeType
     * @param documentType
     * @return
     */
    public Document addDocument(Policy policy, byte[] decodedContent, String mimeType, DocumentType documentType) {
        LocalDateTime now = now(of(SHORT_IDS.get("VST")));

        Document document = new Document();
        document.setCreationDate(now);
        document.setPolicyId(policy.getPolicyId());
        document.setTypeName(documentType);
        document = documentRepository.save(document);

        DocumentDownload documentDownload = new DocumentDownload();
        documentDownload.setContent(new String(Base64.getEncoder().encode(decodedContent), Charset.forName("UTF-8")));
        documentDownload.setDocumentId(document.getId());
        documentDownload.setMimeType(mimeType);
        documentDownloadRepository.save(documentDownload);

        policy.addDocument(document);
        policyRepository.save(policy);
        return document;
    }

    public void generateNotValidatedPolicyDocuments(Policy policy) {
        // Generate Application Form
        Optional<Document> applicationForm = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM)).findFirst();
        if (!applicationForm.isPresent()) {
            try {
                byte[] content = applicationFormService.generatePdfForm(policy);
                addDocument(policy, content, "application/pdf", APPLICATION_FORM);
            } catch (Exception e) {
                logger.error("Application form for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
            }
        }

        // Generate DA Form
        Optional<Document> daForm = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(DA_FORM)).findFirst();
        if (!daForm.isPresent()) {
            try {
                byte[] content = daFormService.generateDAForm(policy);
                addDocument(policy, content, "application/pdf", DA_FORM);
            } catch (Exception e) {
                logger.error("DA form for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
            }
        }
        logger.info("Default documents for policy [" + policy.getPolicyId() + "] have been created.");
    }

    public void generateValidatedPolicyDocuments(Policy policy) {
        // In case previous documents were not generated
        generateNotValidatedPolicyDocuments(policy);

        // Generate Ereceipt as Image
        byte[] ereceiptImage = null;
        Optional<Document> documentImage = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_IMAGE)).findFirst();
        if (!documentImage.isPresent()) {
            try {
                ereceiptImage = createEreceipt(policy);
                addDocument(policy, ereceiptImage, "image/png", ERECEIPT_IMAGE);
            } catch (IOException e) {
                logger.error("Image Ereceipt for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
            }
        } else {
            ereceiptImage = Base64.getDecoder().decode(downloadDocument(documentImage.get().getId()).getContent().getBytes());
        }

        // Generate Ereceipt as PDF
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        if (ereceiptImage != null && !documentPdf.isPresent()) {
            try {
                addDocument(policy, createEreceiptPDF(ereceiptImage), "application/pdf", ERECEIPT_PDF);
            } catch (DocumentException | IOException e) {
                logger.error("PDF Ereceipt for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
            }
        }
        logger.info("Extra documents for policy [" + policy.getPolicyId() + "] have been created.");
    }

    private byte[] createEreceiptPDF(byte[] eReceiptImage) throws DocumentException, IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, content);
        document.open();

        PdfContentByte canvas1 = writer.getDirectContentUnder();
        Image image = Image.getInstance(eReceiptImage);
        image.scaleAbsolute(A4);
        image.scalePercent(50);
        image.setAbsolutePosition(120, 80);
        canvas1.addImage(image);

        document.close();
        content.close();
        return content.toByteArray();
    }

    private String getThaiDate(LocalDate localDate) {
        ThaiBuddhistDate tdate = ThaiBuddhistDate.from(localDate);
        return tdate.format(ofPattern("dd/MM/yyyy"));
    }

    private byte[] createEreceipt(Policy policy) throws IOException {
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
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(30f);
            graphics.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }

        //Name
        graphics.drawString(policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName(), 227, 305);
        logger.debug("Name Insure : " + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName());

        //payment date
        if (policy.getPayments().get(0).getEffectiveDate() != null) {
            graphics.drawString(getThaiDate(policy.getPayments().get(0).getEffectiveDate()), 980, 365);
            logger.debug("Payment Date : " + policy.getPayments().get(0).getEffectiveDate());
        }

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
