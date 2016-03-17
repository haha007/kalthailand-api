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
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.GenderCode;
import th.co.krungthaiaxa.elife.api.model.enums.MaritalStatus;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.products.ProductType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import static th.co.krungthaiaxa.elife.api.model.enums.DividendOption.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.*;

@Service
public class ApplicationFormService {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationFormService.class);

    private final Color FONT_COLOR = Color.BLACK;
    private final String ID_CARD_DOC = "Thai ID Card number";
    private final String MARK = "X";
    private final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public byte[] generatePdfForm(Policy pol) throws Exception {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, content);
        document.open();

        // page1
        PdfContentByte canvas1 = writer.getDirectContentUnder();
        canvas1.addImage(getPdfImage(getPage1(pol)));

        // page2
        document.newPage();
        PdfContentByte canvas2 = writer.getDirectContentUnder();
        canvas2.addImage(getPdfImage(getPage2(pol)));

        // page3
        document.newPage();
        PdfContentByte canvas3 = writer.getDirectContentUnder();
        canvas3.addImage(getPdfImage(getPage3()));

        document.close();
        content.close();
        return content.toByteArray();
    }

    private byte[] getPage1(Policy pol) throws Exception {
        InputStream is1 = getClass().getClassLoader().getResourceAsStream("application-form/application-form-1.png");
        BufferedImage bf1 = ImageIO.read(is1);
        Graphics g1 = bf1.getGraphics();
        g1 = setGraphicColorAndFontBigText(g1);

        Insured insured = pol.getInsureds().get(0);
        Person person = insured.getPerson();

        //Policy number
        g1.drawString(pol.getPolicyId(), 1930, 460);

        g1 = setGraphicColorAndFont(g1);

        //Name
        g1.drawString(person.getGivenName() + " " + person.getSurName(), 630, 775);

        //gender
        if (person.getGenderCode().equals(GenderCode.MALE)) {
            //Gender mail
            g1.drawString(MARK, 1580, 780);
        } else {
            //Gender femail
            g1.drawString(MARK, 1730, 780);
        }

        //Nationality
        g1.drawString("ไทย", 2070, 775);

        //marital status
        if (person.getMaritalStatus().equals(MaritalStatus.SINGLE)) {
            //Marital status 1
            g1.drawString(MARK, 290, 850);
        } else if (person.getMaritalStatus().equals(MaritalStatus.MARRIED)) {
            //Marital status 2
            g1.drawString(MARK, 435, 850);
        } else if (person.getMaritalStatus().equals(MaritalStatus.DIVORCED)) {
            //Marital status 3
            g1.drawString(MARK, 603, 850);
        } else {
            //Marital status 4
            g1.drawString(MARK, 773, 850);
        }

        //Age
        g1.drawString(String.valueOf(insured.getAgeAtSubscription()), 1063, 845);

        //birthdate
        Map<String, String> birthDate = doSplitDateOfBirth(person.getBirthDate());
        //date of birthday
        g1.drawString(birthDate.get("date"), 1430, 845);
        //month of birthday
        g1.drawString(birthDate.get("month"), 1730, 845);
        //year of birthday
        g1.drawString(birthDate.get("year"), 2100, 845);

        //document display
        if (person.getRegistrations().get(0).getTypeName().equals(ID_CARD_DOC)) {
            //document display id card
            g1.drawString(MARK, 395, 915);
        }
        /*
        //document display house registeration
        g1.drawString(MARK, 800, 915);
        //document display others
        g1.drawString("บัตรประกันสังคม", 1125, 915);
        */

        //id card number or passport number
        g1.drawString(person.getRegistrations().get(0).getId(), 750, 995);

        //present address number
        g1.drawString(person.getCurrentAddress().getStreetAddress1(), 485, 1160);
        //present address road
        g1.drawString(solveNullValue(person.getCurrentAddress().getStreetAddress2()), 330, 1235);
        //present address sub district
        g1.drawString(person.getCurrentAddress().getSubdistrict(), 1485, 1235);
        //present address district
        g1.drawString(person.getCurrentAddress().getDistrict(), 340, 1310);
        //present address province
        g1.drawString(person.getCurrentAddress().getSubCountry(), 1170, 1310);
        //present address zipcode
        g1.drawString(person.getCurrentAddress().getPostCode(), 2075, 1310);

        //register address same present address check
        if (person.getRegistrationAddress() == null) {
            //register address same present address mark
            g1.drawString(MARK, 480, 1365);
        } else {
            //register address number
            g1.drawString(person.getRegistrationAddress().getStreetAddress1(), 485, 1440);
            //register address road
            g1.drawString(solveNullValue(person.getRegistrationAddress().getStreetAddress2()), 320, 1515);
            //register address sub district
            g1.drawString(person.getRegistrationAddress().getSubdistrict(), 1480, 1515);
            //register address district
            g1.drawString(person.getRegistrationAddress().getDistrict(), 340, 1590);
            //register address province
            g1.drawString(person.getRegistrationAddress().getSubCountry(), 1165, 1590);
            //register address zipcode
            g1.drawString(person.getRegistrationAddress().getPostCode(), 2070, 1590);
        }

        //contact telephone
        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            g1.drawString(person.getHomePhoneNumber().getNumber(), 315, 1720);
        }
        //contact mobile
        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            g1.drawString(person.getMobilePhoneNumber().getNumber(), 1490, 1720);
        }

        //contact email
        g1.drawString(person.getEmail(), 270, 1795);

        /*
        //occupation constructor
        g1.drawString(MARK, 155, 2045);
        //occupation taxi
        g1.drawString(MARK, 460, 2045);
        //occupation motorcycle service
        g1.drawString(MARK, 1155, 2045);
        //occupation other
        g1.drawString(MARK, 155, 2120);
        //occupation other text
        g1.drawString("อาชีพสมมุติ", 500, 2125);
        */

        //occupation position
        if (insured.getProfessionName() != null) {
            g1.drawString(insured.getProfessionName(), 285, 2230);
        }
        //occupation job description
        if (insured.getProfessionDescription() != null) {
            g1.drawString(insured.getProfessionDescription(), 375, 2305);
        }
        //annual income
        if (insured.getAnnualIncome() != null) {
            g1.drawString(MONEY_FORMAT.format(Integer.parseInt(insured.getAnnualIncome(), 10)), 310, 2375);
        }
        //source income
        if (insured.getIncomeSource() != null) {
            g1.drawString(insured.getIncomeSource(), 1230, 2375);
        }
        //working location
        if (insured.getEmployerName() != null) {
            g1.drawString(insured.getEmployerName(), 345, 2455);
        }

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            //product ifine select mark
            g1.drawString(MARK, 155, 2620);
            //ifine plan 1
            g1.drawString(MARK, 390, 2765);
            //ifine plan 2
            g1.drawString(MARK, 600, 2765);
            //ifine plan 3
            g1.drawString(MARK, 805, 2765);
            //ifine plan 4
            g1.drawString(MARK, 1005, 2765);
            //ifine plan 5
            g1.drawString(MARK, 1210, 2765);
        } else if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            //other product mark
            g1.drawString(MARK, 1445, 2565);
            //product 10ec
            g1.drawString(MARK, 1455, 2705);
            //product 10ec sum insurance
            g1.drawString(MONEY_FORMAT.format(pol.getPremiumsData().getProduct10ECPremium().getSumInsured().getValue()), 1790, 2705);
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

        //coverage period
        g2.drawString(String.valueOf(pol.getCommonData().getNbOfYearsOfCoverage()), 435, 130);
        //premium period
        g2.drawString(String.valueOf(pol.getCommonData().getNbOfYearsOfPremium()), 1025, 130);

        //dividend option
        if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_CASH)) {
            //divident option 1
            g2.drawString(MARK, 105, 275);
            //divident option 1.1
            g2.drawString(MARK, 165, 345);
        } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_FOR_NEXT_PREMIUM)) {
            //divident option 1
            g2.drawString(MARK, 105, 275);
            //divident option 1.2
            g2.drawString(MARK, 165, 415);
        } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(IN_FINE)) {
            //divident option 2
            g2.drawString(MARK, 105, 555);
        }

        //payment mode
        if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH)) {
            //payment mode 1 m
            g2.drawString(MARK, 1440, 195);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_HALF_YEAR)) {
            //payment mode 6 m
            g2.drawString(MARK, 1650, 195);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_QUARTER)) {
            //payment mode 3 m
            g2.drawString(MARK, 1900, 195);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_YEAR)) {
            //payment mode 12 m
            g2.drawString(MARK, 2160, 195);
        }

        //premium
        g2.drawString(MONEY_FORMAT.format(getYearlyPremium(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue(), pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode())), 1705, 260);
        //nb premium
        g2.drawString(MONEY_FORMAT.format(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 1930, 330);

        //pay method check
        /*
        //pay cash
        g2.drawString(MARK, 1570, 400);
        //pay cheque
        g2.drawString(MARK, 1745, 400);
        //pay credit
        g2.drawString(MARK, 1870, 400);
        */
        //pay other
        g2.drawString(MARK, 2090, 400);
        //pay other text
        g2.drawString("LinePay", 2220, 400);

        //height
        g2.drawString(String.valueOf(pol.getInsureds().get(0).getHealthStatus().getHeightInCm()), 260, 730);
        //weight
        g2.drawString(String.valueOf(pol.getInsureds().get(0).getHealthStatus().getWeightInKg()), 740, 730);
        //insure name
        g2.drawString(person.getGivenName() + " " + person.getSurName(), 705, 920);

        //gender
        if (pol.getInsureds().get(0).getPerson().getGenderCode().equals(GenderCode.MALE)) {
            //insure gender mail
            g2.drawString(MARK, 1785, 925);
        } else if (pol.getInsureds().get(0).getPerson().getGenderCode().equals(GenderCode.FEMALE)) {
            //insure gender femail
            g2.drawString(MARK, 1930, 925);
        }

        //insure age
        g2.drawString(String.valueOf(pol.getInsureds().get(0).getAgeAtSubscription()), 2185, 925);
        //insure relation
        g2.drawString("N/A", 580, 1000);
        //insure professional
        g2.drawString(pol.getInsureds().get(0).getProfessionName(), 1110, 1000);

        List<Integer> listY = getBenefitPositionY();
        List<CoverageBeneficiary> allBenefit = pol.getCoverages().get(0).getBeneficiaries();
        for (Integer a = 0; a < allBenefit.size(); a++) {
            CoverageBeneficiary benefit = pol.getCoverages().get(0).getBeneficiaries().get(a);
            //benefit name
            g2.drawString(benefit.getPerson().getGivenName() + " " + benefit.getPerson().getSurName(), 170, listY.get(a));
            //benefit age
            g2.drawString(String.valueOf(benefit.getAgeAtSubscription()), 740, listY.get(a));
            //benefit relation
            g2.drawString(String.valueOf(benefit.getRelationship()), 910, listY.get(a));
            //benefit id card number
            g2.drawString(benefit.getPerson().getRegistrations().get(0).getId(), 1150, listY.get(a));
            //benefit address
            g2.drawString(generateAddress(benefit.getPerson().getCurrentAddress()), 1640, listY.get(a));
            //benefit benefit percent
            g2.drawString(String.valueOf(benefit.getCoverageBenefitPercentage()), 2195, listY.get(a));
        }

        //health question 1
        g2.drawString(MARK, 695, 2050);
        //health question 2
        g2.drawString(MARK, 150, 2285);
        //health question 3
        g2.drawString(MARK, 150, 2585);
        //fatca 1
        g2.drawString(MARK, 170, 3240);
        //fatca 2
        g2.drawString(MARK, 170, 3360);

        return getImageBytes(bf2);
    }

    private byte[] getPage3() throws Exception {
        InputStream is3 = getClass().getClassLoader().getResourceAsStream("application-form/application-form-3.png");
        BufferedImage bf3 = ImageIO.read(is3);
        Graphics g3 = bf3.getGraphics();
        g3 = setGraphicColorAndFont(g3);

        //fatca 3
        g3.drawString(MARK, 170, 190);
        //fatca 4
        g3.drawString(MARK, 170, 365);
        //confirm check
        g3.drawString(MARK, 145, 2155);
        //writing where
        g3.drawString("123 ทุ่งครุ ทุ่งครุ กรุงเทพฯลฯ", 260, 2225);
        //writing date
        g3.drawString("29", 1545, 2225);
        //writing month
        g3.drawString("มกราคม", 1785, 2225);
        //writing year
        g3.drawString("2522", 2150, 2225);
        //footer name
        g3.drawString("บริษัท ไลน์ บิซ พลัส จำกัด", 250, 2740);
        //agent code 1
        g3.drawString("0", 200, 2870);
        //agent code 2
        g3.drawString("4", 255, 2870);
        //agent code 3
        g3.drawString("0", 310, 2870);
        //agent code 4
        g3.drawString("0", 360, 2870);
        //agent code 5
        g3.drawString("0", 410, 2870);
        //agent code 6
        g3.drawString("2", 465, 2870);
        //agent code 7
        g3.drawString("0", 540, 2870);
        //agent code 8
        g3.drawString("1", 590, 2870);
        //agent code 9
        g3.drawString("0", 665, 2870);
        //agent code 10
        g3.drawString("7", 715, 2870);
        //agent code 11
        g3.drawString("5", 765, 2870);
        //agent code 11
        g3.drawString("5", 820, 2870);
        //agent code 12
        g3.drawString("9", 875, 2870);
        //agent code 13
        g3.drawString("4", 925, 2870);

        return getImageBytes(bf3);
    }

    private List<Integer> getBenefitPositionY() {
        List<Integer> listY = new ArrayList<>();
        listY.add(1385);
        listY.add(1465);
        listY.add(1540);
        listY.add(1620);
        listY.add(1695);
        listY.add(1775);
        return listY;
    }

    private String generateAddress(GeographicalAddress g) {
        if (g == null) {
            return "";
        }

        String out = "";
        out += g.getStreetAddress1();
        out += " " + g.getStreetAddress2();
        out += " " + g.getSubdistrict();
        out += " " + g.getDistrict();
        out += " " + g.getSubCountry();
        out += " " + g.getPostCode();
        out += " " + g.getCountry();
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
        Map<String, String> m = new HashMap<>();
        m.put("date", (new DecimalFormat("00")).format(birthDate.getDayOfMonth()));
        DateFormatSymbols dfs = new DateFormatSymbols(new Locale("th", "TH"));
        m.put("month", dfs.getMonths()[(birthDate.getMonthValue() - 1)]);
        m.put("year", String.valueOf(birthDate.getYear() + 543));
        return m;
    }

    private Graphics setGraphicColorAndFont(Graphics g)throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT,getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(50f);
            g.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return g;
    }

    private Graphics setGraphicColorAndFontBigText(Graphics g)throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT,getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(100f);
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
