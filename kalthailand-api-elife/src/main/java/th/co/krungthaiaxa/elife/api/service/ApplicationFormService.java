package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.GenderCode;
import th.co.krungthaiaxa.elife.api.model.enums.MaritalStatus;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.model.enums.ProductIFinePackage;
import th.co.krungthaiaxa.elife.api.products.ProductType;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.*;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static th.co.krungthaiaxa.elife.api.model.enums.DividendOption.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.*;
import static th.co.krungthaiaxa.elife.api.model.enums.RegistrationTypeName.THAI_ID_NUMBER;

@Service
public class ApplicationFormService {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationFormService.class);

    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    private final Color FONT_COLOR = Color.BLACK;
    private final String MARK = "X";
    private final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public byte[] generateNotValidatedApplicationForm(Policy policy) throws Exception {
        return generateValidatedApplicationForm(policy, false);
    }

    public byte[] generateValidatedApplicationForm(Policy policy) throws Exception {
        return generateValidatedApplicationForm(policy, true);
    }

    private byte[] generateValidatedApplicationForm(Policy policy, boolean validatedPolicy) throws Exception {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, content);
        document.open();

        // page1
        PdfContentByte canvas1 = writer.getDirectContentUnder();
        canvas1.addImage(getPdfImage(getPage1(policy, validatedPolicy)));

        // page2
        document.newPage();
        PdfContentByte canvas2 = writer.getDirectContentUnder();
        canvas2.addImage(getPdfImage(getPage2(policy)));

        // page3
        document.newPage();
        PdfContentByte canvas3 = writer.getDirectContentUnder();
        canvas3.addImage(getPdfImage(getPage3(policy)));

        document.close();
        content.close();
        return content.toByteArray();
    }

    private byte[] getPage1(Policy pol, boolean validatedPolicy) throws Exception {
        InputStream is1 = getClass().getClassLoader().getResourceAsStream("application-form/application-form-1.png");
        BufferedImage bf1 = ImageIO.read(is1);
        Graphics g1 = bf1.getGraphics();
        g1 = setGraphicColorAndFontBigText(g1);

        Insured insured = pol.getInsureds().get(0);
        Person person = insured.getPerson();

        //Policy number
        g1.drawString(pol.getPolicyId(), 1940, 410);

        /*
        g1 = setBarcode3Of9Font(g1);
        //generate application barcode 3of9
        g1.drawString("*NTH1AFOL16*", 1800, 80);
        */

        //add barcode image
        InputStream isBarcode = getClass().getClassLoader().getResourceAsStream("application-form/application-barcode.png");
        java.awt.Image img = ImageIO.read(isBarcode);
        g1.drawImage(img, 1780, 50, 650, 100, null);

        g1 = setGraphicColorAndFont(g1);

        //add *eBiz App* below barcode
        g1.drawString("*eBiz App*", 2015, 190);

        //g1 = setBarcode3Of9Font(g1);

        //generate barcode 3of9
        //g1.drawString("*" + pol.getPolicyId() + "*", 1780, 190);

        g1 = setGraphicColorAndFont(g1);

        if (validatedPolicy && isNotEmpty(pol.getValidationAgentCode())) {
            //Validate TMC agent code
            g1.drawString(pol.getValidationAgentCode(), 2010, 480);
        }

        //Title
        if (person.getTitle().equals("MR")) {
            g1.drawString(MARK, 625, 1340);
        }
        if (person.getTitle().equals("MS")) {
            g1.drawString(MARK, 775, 1340);
        }
        if (person.getTitle().equals("MRS")) {
            g1.drawString(MARK, 915, 1340);
        }

        //Name
        g1.drawString(person.getGivenName() + " " + person.getSurName(), 1280, 1340);

        //gender
        if (person.getGenderCode().equals(GenderCode.MALE)) {
            //Gender mail
            g1.drawString(MARK, 210, 1460);
        } else {
            //Gender femail
            g1.drawString(MARK, 355, 1460);
        }

        //birthdate
        Map<String, String> birthDate = doSplitDateOfBirth(person.getBirthDate());
        //date of birthday
        g1.drawString(birthDate.get("date"), 660, 1460);
        //month of birthday
        g1.drawString(birthDate.get("month"), 1030, 1460);
        //year of birthday
        g1.drawString(birthDate.get("year"), 1530, 1460);

        //Nationality
        g1.drawString("ไทย", 2030, 1460);

        //marital status
        if (person.getMaritalStatus().equals(MaritalStatus.SINGLE)) {
            //Marital status 1
            g1.drawString(MARK, 245, 1535);
        } else if (person.getMaritalStatus().equals(MaritalStatus.MARRIED)) {
            //Marital status 2
            g1.drawString(MARK, 395, 1535);
        } else if (person.getMaritalStatus().equals(MaritalStatus.DIVORCED)) {
            //Marital status 3
            g1.drawString(MARK, 560, 1535);
        } else {
            //Marital status 4
            g1.drawString(MARK, 730, 1535);
        }

        //height
        g1.drawString(String.valueOf(pol.getInsureds().get(0).getHealthStatus().getHeightInCm()), 245, 1655);

        //weight
        g1.drawString(String.valueOf(pol.getInsureds().get(0).getHealthStatus().getWeightInKg()), 540, 1655);

        //weight change in last 6 months
        if (false == pol.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6Months()) {
            g1.drawString(MARK, 1630, 1650);
        } else {
            g1.drawString(MARK, 1760, 1650);
            g1.drawString(pol.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6MonthsReason(), 2230, 1650);
        }

        //document display
        if (person.getRegistrations().get(0).getTypeName().equals(THAI_ID_NUMBER)) {
            //document display id card
            g1.drawString(MARK, 395, 1780);
        }
        /*
        //document display house registeration
        g1.drawString(MARK, 800, 915);
        //document display others
        g1.drawString("บัตรประกันสังคม", 1125, 915);
        */

        //id card number or passport number
        g1.drawString(person.getRegistrations().get(0).getId(), 1110, 1785);

        //present address number
        g1.drawString(person.getCurrentAddress().getStreetAddress1(), 510, 2020);
        //present address road
        g1.drawString(solveNullValue(person.getCurrentAddress().getStreetAddress2()), 330, 2095);
        //present address sub district
        g1.drawString(person.getCurrentAddress().getSubdistrict(), 1490, 2095);
        //present address district
        g1.drawString(person.getCurrentAddress().getDistrict(), 345, 2165);
        //present address province
        g1.drawString(person.getCurrentAddress().getSubCountry(), 1170, 2165);
        //present address zipcode
        g1.drawString(person.getCurrentAddress().getPostCode(), 2075, 2165);

        //register address same present address check
        if (person.getRegistrationAddress() == null) {
            //register address same present address mark
            g1.drawString(MARK, 470, 2235);
        } else {
            //register address number
            g1.drawString(person.getRegistrationAddress().getStreetAddress1(), 500, 2300);
            //register address road
            g1.drawString(solveNullValue(person.getRegistrationAddress().getStreetAddress2()), 325, 2375);
            //register address sub district
            g1.drawString(person.getRegistrationAddress().getSubdistrict(), 1490, 2375);
            //register address district
            g1.drawString(person.getRegistrationAddress().getDistrict(), 345, 2450);
            //register address province
            g1.drawString(person.getRegistrationAddress().getSubCountry(), 1160, 2450);
            //register address zipcode
            g1.drawString(person.getRegistrationAddress().getPostCode(), 2075, 2450);
        }

        //contact telephone
        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            g1.drawString(person.getHomePhoneNumber().getNumber(), 310, 2590);
        }
        //contact mobile
        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            g1.drawString(person.getMobilePhoneNumber().getNumber(), 1490, 2590);
        }

        //contact email
        g1.drawString(person.getEmail(), 280, 2660);

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            if (pol.getInsureds().get(0).getProfessionName().equals("คนงานก่อสร้าง")) {
                g1.drawString(MARK, 155, 2950);
            } else if (pol.getInsureds().get(0).getProfessionName().equals("คนขับรถแท๊กซี่ / คนขับรถมอเตอร์ไซค์รับจ้าง")) {
                g1.drawString(MARK, 455, 2950);
            } else if (pol.getInsureds().get(0).getProfessionName().equals("คนงานเหมือง / สำรวจหาน้ำมัน")) {
                g1.drawString(MARK, 1150, 2960);
            } else {
                g1.drawString(MARK, 155, 3020);
                g1.drawString(pol.getInsureds().get(0).getProfessionName(), 510, 3020);
            }
        }

        //occupation position
        if (insured.getProfessionName() != null) {
            g1.drawString(insured.getProfessionName(), 290, 3140);
        }
        //occupation job description
        if (insured.getProfessionDescription() != null) {
            g1.drawString(insured.getProfessionDescription(), 380, 3215);
        }
        //annual income
        if (insured.getAnnualIncome() != null) {
            g1.drawString(MONEY_FORMAT.format(Integer.parseInt(insured.getAnnualIncome(), 10)), 320, 3280);
        }
        //source income
        if (insured.getIncomeSources() != null) {
            g1.drawString(insured.getIncomeSources().stream().collect(joining(",")), 1220, 3280);
        }
        //working location
        if (insured.getEmployerName() != null) {
            g1.drawString(insured.getEmployerName(), 340, 3355);
        }

        return getImageBytes(bf1);
    }

    private byte[] getPage2(Policy pol) throws Exception {
        InputStream is2 = getClass().getClassLoader().getResourceAsStream("application-form/application-form-2.png");
        BufferedImage bf2 = ImageIO.read(is2);
        Graphics g2 = bf2.getGraphics();
        g2 = setGraphicColorAndFont(g2);
        Insured insured = pol.getInsureds().get(0);
        Person person = insured.getPerson();

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {

            g2.drawString(MARK, 1450, 120);
            g2.drawString(MARK, 1465, 265);

            //Premium
            g2.drawString(MONEY_FORMAT.format(getYearlyPremium(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue(), pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode())), 1800, 265);

        } else if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {

            g2.drawString(MARK, 165, 175);

            //Plan
            if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE1)) {
                g2.drawString(MARK, 400, 320);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE2)) {
                g2.drawString(MARK, 605, 320);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE3)) {
                g2.drawString(MARK, 815, 320);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE4)) {
                g2.drawString(MARK, 1010, 320);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE5)) {
                g2.drawString(MARK, 1220, 320);
            }
        }

        //coverage period
        g2.drawString(String.valueOf(pol.getCommonData().getNbOfYearsOfCoverage()), 480, 1000);
        //premium period
        g2.drawString(String.valueOf(pol.getCommonData().getNbOfYearsOfPremium()), 1065, 1000);

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {

            //dividend option
            if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
                if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_CASH)) {
                    //divident option 1
                    g2.drawString(MARK, 140, 1175);
                    //divident option 1.1
                    g2.drawString(MARK, 290, 1175);
                } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_FOR_NEXT_PREMIUM)) {
                    //divident option 1
                    g2.drawString(MARK, 140, 1175);
                    //divident option 1.2
                    g2.drawString(MARK, 290, 1245);
                } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(IN_FINE)) {
                    //divident option 2
                    g2.drawString(MARK, 140, 1315);
                }
            }

        }

        //payment mode
        if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH)) {
            //payment mode 1 m
            g2.drawString(MARK, 525, 1430);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_HALF_YEAR)) {
            //payment mode 6 m
            g2.drawString(MARK, 995, 1430);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_QUARTER)) {
            //payment mode 3 m
            g2.drawString(MARK, 740, 1430);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_YEAR)) {
            //payment mode 12 m
            g2.drawString(MARK, 1245, 1430);
        }

        //nb premium
        g2.drawString(MONEY_FORMAT.format(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 660, 1535);

        //line payment channel
        g2.drawString(MARK, 325, 1795);

        //Benefit
        List<Integer> listY = getBenefitPositionY();
        List<CoverageBeneficiary> allBenefit = pol.getCoverages().get(0).getBeneficiaries();
        for (Integer a = 0; a < allBenefit.size(); a++) {
            CoverageBeneficiary benefit = pol.getCoverages().get(0).getBeneficiaries().get(a);
            //benefit name
            g2.drawString(benefit.getPerson().getGivenName() + " " + benefit.getPerson().getSurName(), 155, listY.get(a));
            //benefit age
            g2.drawString(String.valueOf(benefit.getAgeAtSubscription()), 730, listY.get(a));
            //benefit relation
            g2.drawString(messageSource.getMessage("relationship." + String.valueOf(benefit.getRelationship()), null, thLocale), 905, listY.get(a));
            //benefit id card number
            g2.drawString(benefit.getPerson().getRegistrations().get(0).getId(), 1135, listY.get(a));

            g2 = setGraphicColorAndFontSmall(g2);

            //benefit address
            g2.drawString(generateAddress1(benefit.getPerson().getCurrentAddress()), 1630, listY.get(a) - 25);

            g2.drawString(generateAddress2(benefit.getPerson().getCurrentAddress()), 1630, listY.get(a) + 5);

            g2 = setGraphicColorAndFont(g2);

            //benefit benefit percent
            g2.drawString(String.valueOf(benefit.getCoverageBenefitPercentage()), 2185, listY.get(a));
        }

        //health question 1
        g2.drawString(MARK, 710, 2985);

        //health question 2
        g2.drawString(MARK, 150, 3215);

        return getImageBytes(bf2);
    }

    private byte[] getPage3(Policy pol) throws Exception {
        InputStream is3 = getClass().getClassLoader().getResourceAsStream("application-form/application-form-3.png");
        BufferedImage bf3 = ImageIO.read(is3);
        Graphics g3 = bf3.getGraphics();
        g3 = setGraphicColorAndFont(g3);

        //health question 3
        g3.drawString(MARK, 150, 240);

        //fatca 1
        g3.drawString(MARK, 185, 600);

        //fatca 2
        g3.drawString(MARK, 185, 715);

        //fatca 3
        g3.drawString(MARK, 185, 835);

        //fatca 4
        g3.drawString(MARK, 185, 1015);

        //accept check
        g3.drawString(MARK, 145, 2775);

        //generate date
        Map<String, String> now = doSplitDateOfBirth(LocalDate.now());

        //date
        g3.drawString(now.get("date"), 1550, 2845);

        //month
        g3.drawString(now.get("month"), 1795, 2845);

        //year
        g3.drawString(now.get("year"), 2140, 2845);

        return getImageBytes(bf3);
    }

    private List<Integer> getBenefitPositionY() {
        List<Integer> listY = new ArrayList<>();
        listY.add(2280);
        listY.add(2360);
        listY.add(2435);
        listY.add(2515);
        listY.add(2595);
        listY.add(2670);
        return listY;
    }

    private String generateAddress1(GeographicalAddress g) {
        if (g == null) {
            return "";
        }

        String out = "";
        out += g.getStreetAddress1();
        out += " " + g.getStreetAddress2();
        return out;
    }

    private String generateAddress2(GeographicalAddress g) {
        if (g == null) {
            return "";
        }

        String out = "";
        if (g.getSubCountry().equals("กรุงเทพมหานคร")) {
            out += "แขวง" + g.getSubdistrict();
            out += " เขต" + g.getDistrict();
        } else {
            out += "ตำบล" + g.getSubdistrict();
            out += " อำเภอ" + g.getDistrict();
        }
        out += " " + g.getSubCountry();
        out += " " + g.getPostCode();
        return out;
    }

    private Double getYearlyPremium(Double premium, PeriodicityCode mode) {
        Double premiumPerYear = 0.0;
        if (mode.equals(EVERY_YEAR)) {
            premiumPerYear = premium;
        } else if (mode.equals(EVERY_HALF_YEAR)) {
            premiumPerYear = premium * 2;
        } else if (mode.equals(EVERY_QUARTER)) {
            premiumPerYear = premium * 4;
        } else if (mode.equals(EVERY_MONTH)) {
            premiumPerYear = premium * 12;
        }
        return premiumPerYear;
    }

    private String solveNullValue(String s) {
        return (StringUtils.isBlank(s) ? "" : s);
    }

    private Map<String, String> doSplitDateOfBirth(LocalDate birthDate) {
        ThaiBuddhistDate thaiBirthDate = ThaiBuddhistDate.from(birthDate);
        Map<String, String> m = new HashMap<>();
        m.put("date", thaiBirthDate.format(ofPattern("dd")));
        m.put("month", thaiBirthDate.format(ofPattern("MMMM", new Locale("th", "TH"))));
        m.put("year", thaiBirthDate.format(ofPattern("yyyy")));
        return m;
    }

    private Graphics setGraphicColorAndFontSmall(Graphics g) throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(35f);
            g.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return g;
    }

    private Graphics setGraphicColorAndFont(Graphics g) throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(50f);
            g.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return g;
    }

    private Graphics setGraphicColorAndFontBigText(Graphics g) throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(100f);
            g.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return g;
    }

    private Graphics setBarcode3Of9Font(Graphics g) throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("barcode3of9/3OF9_NEW.TTF")).deriveFont(80f);
            g.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return g;
    }

    private byte[] getImageBytes(BufferedImage bf1) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bf1, "png", baos);
        baos.flush();
        byte[] content = baos.toByteArray();
        baos.close();
        return content;
    }

    private Image getPdfImage(byte[] imageConent) throws IOException, BadElementException {
        Image image = Image.getInstance(imageConent);
        image.scaleAbsolute(PageSize.A4);
        image.setAbsolutePosition(0, 0);
        return image;
    }

}
