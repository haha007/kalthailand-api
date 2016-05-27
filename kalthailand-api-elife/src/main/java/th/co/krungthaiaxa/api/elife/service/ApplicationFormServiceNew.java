package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.*;
import th.co.krungthaiaxa.api.elife.model.enums.*;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static th.co.krungthaiaxa.api.elife.model.enums.DividendOption.*;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.*;

@Service
public class ApplicationFormServiceNew {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationFormServiceNew.class);
    private static final float SMALL_SIZE = 35f;
    private static final float MEDIUM_SIZE = 50f;

    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

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

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application-form/empty-application-form.pdf");
        PdfReader pdfReader = new PdfReader(inputStream);
        PdfStamper pdfStamper = new PdfStamper(pdfReader, content);

        // page1
        getPage1(pdfStamper.getOverContent(1), policy, validatedPolicy);

        // page2
        getPage2(pdfStamper.getOverContent(2), policy);

        // page3
        getPage3(pdfStamper.getOverContent(3));

        pdfStamper.close();
        content.close();
        return content.toByteArray();
    }

    private void getPage1(PdfContentByte pdfContentByte, Policy pol, boolean validatedPolicy) throws Exception {
        BaseFont font = getBaseFont();

        Insured insured = pol.getInsureds().get(0);
        Person person = insured.getPerson();

        //Policy number
        writeText(pdfContentByte, font, pol.getPolicyId(), 1940, 410, SMALL_SIZE);

        //add *eBiz App* below barcode
        writeText(pdfContentByte, font, "*eBiz App*", 2015, 190, SMALL_SIZE);

        if (validatedPolicy && isNotEmpty(pol.getValidationAgentCode())) {
            //Validate TMC agent code
            writeText(pdfContentByte, font, pol.getValidationAgentCode(), 2010, 480, MEDIUM_SIZE);
        }

        //Title
        if (person.getTitle().equals("MR")) {
            writeText(pdfContentByte, font, MARK, 625, 1340, MEDIUM_SIZE);
        }
        if (person.getTitle().equals("MS")) {
            writeText(pdfContentByte, font, MARK, 775, 1340, MEDIUM_SIZE);
        }
        if (person.getTitle().equals("MRS")) {
            writeText(pdfContentByte, font, MARK, 915, 1340, MEDIUM_SIZE);
        }

        //Name
        writeText(pdfContentByte, font, person.getGivenName() + " " + person.getSurName(), 1280, 1340, MEDIUM_SIZE);

        //gender
        if (person.getGenderCode().equals(GenderCode.MALE)) {
            //Gender mail
            writeText(pdfContentByte, font, MARK, 210, 1460, MEDIUM_SIZE);
        } else {
            //Gender female
            writeText(pdfContentByte, font, MARK, 355, 1460, MEDIUM_SIZE);
        }

        //birthdate
        Map<String, String> birthDate = doSplitDateOfBirth(person.getBirthDate());
        //date of birthday
        writeText(pdfContentByte, font, birthDate.get("date"), 660, 1460, MEDIUM_SIZE);
        //month of birthday
        writeText(pdfContentByte, font, birthDate.get("month"), 1030, 1460, MEDIUM_SIZE);
        //year of birthday
        writeText(pdfContentByte, font, birthDate.get("year"), 1530, 1460, MEDIUM_SIZE);

        //Nationality
        writeText(pdfContentByte, font, "ไทย", 2030, 1460, MEDIUM_SIZE);

        //marital status
        if (person.getMaritalStatus().equals(MaritalStatus.SINGLE)) {
            //Marital status 1
            writeText(pdfContentByte, font, MARK, 245, 1535, MEDIUM_SIZE);
        } else if (person.getMaritalStatus().equals(MaritalStatus.MARRIED)) {
            //Marital status 2
            writeText(pdfContentByte, font, MARK, 395, 1535, MEDIUM_SIZE);
        } else if (person.getMaritalStatus().equals(MaritalStatus.DIVORCED)) {
            //Marital status 3
            writeText(pdfContentByte, font, MARK, 560, 1535, MEDIUM_SIZE);
        } else {
            //Marital status 4
            writeText(pdfContentByte, font, MARK, 730, 1535, MEDIUM_SIZE);
        }

        //height
        writeText(pdfContentByte, font, String.valueOf(pol.getInsureds().get(0).getHealthStatus().getHeightInCm()), 245, 1655, MEDIUM_SIZE);

        //weight
        writeText(pdfContentByte, font, String.valueOf(pol.getInsureds().get(0).getHealthStatus().getWeightInKg()), 540, 1655, MEDIUM_SIZE);

        //weight change in last 6 months
        if (!pol.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6Months()) {
            writeText(pdfContentByte, font, MARK, 1630, 1650, MEDIUM_SIZE);
        } else {
            writeText(pdfContentByte, font, MARK, 1760, 1650, MEDIUM_SIZE);
            String weightChangeReason = pol.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6MonthsReason();
            if (weightChangeReason.equals("น้ำหนักเพิ่ม")) {
                writeText(pdfContentByte, font, weightChangeReason, 2220, 1650, MEDIUM_SIZE);
            } else {
                String w1 = "", w2 = "";
                if (weightChangeReason.equals("ออกกำลังกายหรือควบคุมอาหาร")) {
                    w1 = weightChangeReason.substring(0, 11);
                    w2 = weightChangeReason.substring(w1.length(), weightChangeReason.length());
                } else if (weightChangeReason.equals("ตั้งใจเพิ่ม/ลดน้ำหนัก")) {
                    w1 = weightChangeReason.substring(0, 11);
                    w2 = weightChangeReason.substring(w1.length(), weightChangeReason.length());
                } else if (weightChangeReason.equals("น้ำหนักเปลี่ยนแปลงมากโดยไม่ทราบสาเหตุ")) {
                    w1 = weightChangeReason.substring(0, 18);
                    w2 = weightChangeReason.substring(w1.length(), weightChangeReason.length());
                }
                writeText(pdfContentByte, font, w1, 2220, 1635, SMALL_SIZE);
                writeText(pdfContentByte, font, w2, 2220, 1660, SMALL_SIZE);
            }
        }

        //document display
        if (person.getRegistrations().get(0).getTypeName().equals(RegistrationTypeName.THAI_ID_NUMBER)) {
            //document display id card
            writeText(pdfContentByte, font, MARK, 395, 1780, MEDIUM_SIZE);
        }
        /*
        //document display house registeration
        g1.drawString(MARK, 800, 915);
        //document display others
        g1.drawString("บัตรประกันสังคม", 1125, 915);
        */

        //id card number or passport number
        writeText(pdfContentByte, font, person.getRegistrations().get(0).getId(), 1110, 1785, MEDIUM_SIZE);

        //present address number
        writeText(pdfContentByte, font, person.getCurrentAddress().getStreetAddress1(), 510, 2020, MEDIUM_SIZE);
        //present address road
        writeText(pdfContentByte, font, solveNullValue(person.getCurrentAddress().getStreetAddress2()), 330, 2095, MEDIUM_SIZE);
        //present address sub district
        writeText(pdfContentByte, font, person.getCurrentAddress().getSubdistrict(), 1490, 2095, MEDIUM_SIZE);
        //present address district
        writeText(pdfContentByte, font, person.getCurrentAddress().getDistrict(), 345, 2165, MEDIUM_SIZE);
        //present address province
        writeText(pdfContentByte, font, person.getCurrentAddress().getSubCountry(), 1170, 2165, MEDIUM_SIZE);
        //present address zipcode
        writeText(pdfContentByte, font, person.getCurrentAddress().getPostCode(), 2075, 2165, MEDIUM_SIZE);

        //register address same present address check
        if (person.getRegistrationAddress() == null) {
            //register address same present address mark
            writeText(pdfContentByte, font, MARK, 470, 2235, MEDIUM_SIZE);
        } else {
            //register address number
            writeText(pdfContentByte, font, person.getRegistrationAddress().getStreetAddress1(), 500, 2300, MEDIUM_SIZE);
            //register address road
            writeText(pdfContentByte, font, solveNullValue(person.getRegistrationAddress().getStreetAddress2()), 325, 2375, MEDIUM_SIZE);
            //register address sub district
            writeText(pdfContentByte, font, person.getRegistrationAddress().getSubdistrict(), 1490, 2375, MEDIUM_SIZE);
            //register address district
            writeText(pdfContentByte, font, person.getRegistrationAddress().getDistrict(), 345, 2450, MEDIUM_SIZE);
            //register address province
            writeText(pdfContentByte, font, person.getRegistrationAddress().getSubCountry(), 1160, 2450, MEDIUM_SIZE);
            //register address zipcode
            writeText(pdfContentByte, font, person.getRegistrationAddress().getPostCode(), 2075, 2450, MEDIUM_SIZE);
        }

        //contact telephone
        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            writeText(pdfContentByte, font, person.getHomePhoneNumber().getNumber(), 310, 2590, MEDIUM_SIZE);
        }
        //contact mobile
        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            writeText(pdfContentByte, font, person.getMobilePhoneNumber().getNumber(), 1490, 2590, MEDIUM_SIZE);
        }

        //contact email
        writeText(pdfContentByte, font, person.getEmail(), 280, 2660, MEDIUM_SIZE);

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            if (pol.getInsureds().get(0).getProfessionName().equals("คนงานก่อสร้าง")) {
                writeText(pdfContentByte, font, MARK, 155, 2950, MEDIUM_SIZE);
            } else if (pol.getInsureds().get(0).getProfessionName().equals("คนขับรถแท๊กซี่ / คนขับรถมอเตอร์ไซค์รับจ้าง")) {
                writeText(pdfContentByte, font, MARK, 455, 2950, MEDIUM_SIZE);
            } else if (pol.getInsureds().get(0).getProfessionName().equals("คนงานเหมือง / สำรวจหาน้ำมัน")) {
                writeText(pdfContentByte, font, MARK, 1150, 2960, MEDIUM_SIZE);
            } else {
                writeText(pdfContentByte, font, MARK, 155, 3020, MEDIUM_SIZE);
                writeText(pdfContentByte, font, pol.getInsureds().get(0).getProfessionName(), 510, 3020, MEDIUM_SIZE);
            }
        }

        //occupation position
        if (insured.getProfessionName() != null) {
            writeText(pdfContentByte, font, insured.getProfessionName(), 290, 3140, MEDIUM_SIZE);
        }
        //occupation job description
        if (insured.getProfessionDescription() != null) {
            writeText(pdfContentByte, font, insured.getProfessionDescription(), 380, 3215, MEDIUM_SIZE);
        }
        //annual income
        if (insured.getAnnualIncome() != null) {
            writeText(pdfContentByte, font, MONEY_FORMAT.format(Integer.parseInt(insured.getAnnualIncome(), 10)), 320, 3280, MEDIUM_SIZE);
        }
        //source income
        if (insured.getIncomeSources() != null) {
            writeText(pdfContentByte, font, insured.getIncomeSources().stream().collect(joining(",")), 1220, 3280, MEDIUM_SIZE);
        }
        //working location
        if (insured.getEmployerName() != null) {
            writeText(pdfContentByte, font, insured.getEmployerName(), 340, 3355, MEDIUM_SIZE);
        }
    }

    private void getPage2(PdfContentByte pdfContentByte, Policy pol) throws Exception {
        BaseFont font = getBaseFont();

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            writeText(pdfContentByte, font, MARK, 1450, 120, MEDIUM_SIZE);
            writeText(pdfContentByte, font, MARK, 1465, 265, MEDIUM_SIZE);

            //Premium
            writeText(pdfContentByte, font, MONEY_FORMAT.format(getYearlyPremium(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue(), pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode())), 1800, 265, MEDIUM_SIZE);
        } else if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            writeText(pdfContentByte, font, MARK, 165, 175, MEDIUM_SIZE);

            //Plan
            if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE1)) {
                writeText(pdfContentByte, font, MARK, 400, 320, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE2)) {
                writeText(pdfContentByte, font, MARK, 605, 320, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE3)) {
                writeText(pdfContentByte, font, MARK, 815, 320, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE4)) {
                writeText(pdfContentByte, font, MARK, 1010, 320, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE5)) {
                writeText(pdfContentByte, font, MARK, 1220, 320, MEDIUM_SIZE);
            }
        }

        //coverage period
        writeText(pdfContentByte, font, String.valueOf(pol.getCommonData().getNbOfYearsOfCoverage()), 480, 1000, MEDIUM_SIZE);
        //premium period
        writeText(pdfContentByte, font, String.valueOf(pol.getCommonData().getNbOfYearsOfPremium()), 1065, 1000, MEDIUM_SIZE);

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            //dividend option
            if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
                if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_CASH)) {
                    //divident option 1
                    writeText(pdfContentByte, font, MARK, 140, 1175, MEDIUM_SIZE);
                    //divident option 1.1
                    writeText(pdfContentByte, font, MARK, 290, 1175, MEDIUM_SIZE);
                } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_FOR_NEXT_PREMIUM)) {
                    //divident option 1
                    writeText(pdfContentByte, font, MARK, 140, 1175, MEDIUM_SIZE);
                    //divident option 1.2
                    writeText(pdfContentByte, font, MARK, 290, 1245, MEDIUM_SIZE);
                } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(IN_FINE)) {
                    //divident option 2
                    writeText(pdfContentByte, font, MARK, 140, 1315, MEDIUM_SIZE);
                }
            }

        }

        //payment mode
        if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH)) {
            //payment mode 1 m
            writeText(pdfContentByte, font, MARK, 525, 1430, MEDIUM_SIZE);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_HALF_YEAR)) {
            //payment mode 6 m
            writeText(pdfContentByte, font, MARK, 995, 1430, MEDIUM_SIZE);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_QUARTER)) {
            //payment mode 3 m
            writeText(pdfContentByte, font, MARK, 740, 1430, MEDIUM_SIZE);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_YEAR)) {
            //payment mode 12 m
            writeText(pdfContentByte, font, MARK, 1245, 1430, MEDIUM_SIZE);
        }

        //nb premium
        writeText(pdfContentByte, font, MONEY_FORMAT.format(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 660, 1535, MEDIUM_SIZE);

        //line payment channel
        writeText(pdfContentByte, font, MARK, 325, 1795, MEDIUM_SIZE);

        //Benefit
        List<Integer> listY = getBenefitPositionY();
        List<CoverageBeneficiary> allBenefit = pol.getCoverages().get(0).getBeneficiaries();
        for (Integer a = 0; a < allBenefit.size(); a++) {
            CoverageBeneficiary benefit = pol.getCoverages().get(0).getBeneficiaries().get(a);
            //benefit name
            writeText(pdfContentByte, font, benefit.getPerson().getGivenName() + " " + benefit.getPerson().getSurName(), 155, listY.get(a), MEDIUM_SIZE);
            //benefit age
            writeText(pdfContentByte, font, String.valueOf(benefit.getAgeAtSubscription()), 730, listY.get(a), MEDIUM_SIZE);
            //benefit relation
            writeText(pdfContentByte, font, messageSource.getMessage("relationship." + String.valueOf(benefit.getRelationship()), null, thLocale), 905, listY.get(a), MEDIUM_SIZE);
            //benefit id card number
            writeText(pdfContentByte, font, benefit.getPerson().getRegistrations().get(0).getId(), 1135, listY.get(a), MEDIUM_SIZE);

            //benefit address
            writeText(pdfContentByte, font, generateAddress1(benefit.getPerson().getCurrentAddress()), 1630, listY.get(a) - 25, SMALL_SIZE);

            writeText(pdfContentByte, font, generateAddress2(benefit.getPerson().getCurrentAddress()), 1630, listY.get(a) + 5, SMALL_SIZE);

            //benefit benefit percent
            writeText(pdfContentByte, font, String.valueOf(benefit.getCoverageBenefitPercentage()), 2185, listY.get(a), MEDIUM_SIZE);
        }

        //health question 1
        writeText(pdfContentByte, font, MARK, 710, 2985, MEDIUM_SIZE);

        //health question 2
        writeText(pdfContentByte, font, MARK, 150, 3215, MEDIUM_SIZE);
    }

    private void getPage3(PdfContentByte pdfContentByte) throws Exception {
        BaseFont font = getBaseFont();

        //health question 3
        writeText(pdfContentByte, font, MARK, 150, 240, MEDIUM_SIZE);

        //fatca 1
        writeText(pdfContentByte, font, MARK, 185, 600, MEDIUM_SIZE);

        //fatca 2
        writeText(pdfContentByte, font, MARK, 185, 715, MEDIUM_SIZE);

        //fatca 3
        writeText(pdfContentByte, font, MARK, 185, 835, MEDIUM_SIZE);

        //fatca 4
        writeText(pdfContentByte, font, MARK, 185, 1015, MEDIUM_SIZE);

        //accept check
        writeText(pdfContentByte, font, MARK, 145, 2775, MEDIUM_SIZE);

        //generate date
        Map<String, String> now = doSplitDateOfBirth(LocalDate.now());

        //date
        writeText(pdfContentByte, font, now.get("date"), 1550, 2845, MEDIUM_SIZE);

        //month
        writeText(pdfContentByte, font, now.get("month"), 1795, 2845, MEDIUM_SIZE);

        //year
        writeText(pdfContentByte, font, now.get("year"), 2140, 2845, MEDIUM_SIZE);
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

    private void writeText(PdfContentByte pdfContentByte, BaseFont font, String text, int x, int y, float fontSize) {
        pdfContentByte.beginText();
        pdfContentByte.setFontAndSize(font, fontSize);
        pdfContentByte.setTextMatrix(x, y);
        pdfContentByte.showText(text);
        pdfContentByte.endText();
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

    private BaseFont getBaseFont() throws IOException {
        BaseFont baseFont;
        try {
            byte[] bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF"));
            baseFont = BaseFont.createFont("ANGSAB_1.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        } catch (DocumentException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return baseFont;
    }

}
