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
import th.co.krungthaiaxa.api.elife.products.ProductIFinePackage;
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
public class ApplicationFormService {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationFormService.class);
    private static final float VERY_SMALL_SIZE = 7f;
    private static final float SMALL_SIZE = 10f;
    private static final float MEDIUM_SIZE = 13f;    
    private static final float BIG_SIZE = 25f;

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
        writeText(pdfContentByte, font, pol.getPolicyId(), 460, 745, BIG_SIZE);

        //add *eBiz App* below barcode
        writeText(pdfContentByte, font, "*eBiz App*", 490, 795, SMALL_SIZE);

        if (validatedPolicy && isNotEmpty(pol.getValidationAgentCode())) {
            //Validate TMC agent code
            writeText(pdfContentByte, font, pol.getValidationAgentCode(), 475, 727, MEDIUM_SIZE);
        }

        //Title
        if (person.getTitle().equals("MR")) {
            writeText(pdfContentByte, font, MARK, 148, 520, MEDIUM_SIZE);
        }
        if (person.getTitle().equals("MRS")) {
            writeText(pdfContentByte, font, MARK, 186, 520, MEDIUM_SIZE);
        }
        if (person.getTitle().equals("MS")) {
            writeText(pdfContentByte, font, MARK, 218, 520, MEDIUM_SIZE);
        }

        //Name
        writeText(pdfContentByte, font, person.getGivenName() + " " + person.getSurName(), 310, 520, MEDIUM_SIZE);

        //gender
        if (person.getGenderCode().equals(GenderCode.MALE)) {
            //Gender mail
            writeText(pdfContentByte, font, MARK, 50, 492, MEDIUM_SIZE);
        } else {
            //Gender fesmale
            writeText(pdfContentByte, font, MARK, 86, 492, MEDIUM_SIZE);
        }

        //birthdate
        Map<String, String> birthDate = doSplitDateOfBirth(person.getBirthDate());
        //date of birthday
        writeText(pdfContentByte, font, birthDate.get("date"), 160, 492, MEDIUM_SIZE);
        //month of birthday
        writeText(pdfContentByte, font, birthDate.get("month"), 250, 492, MEDIUM_SIZE);
        //year of birthday
        writeText(pdfContentByte, font, birthDate.get("year"), 370, 492, MEDIUM_SIZE);

        //Nationality
        writeText(pdfContentByte, font, "ไทย", 490, 492, MEDIUM_SIZE);

        //marital status
        if (person.getMaritalStatus().equals(MaritalStatus.SINGLE)) {
            //Marital status 1
            writeText(pdfContentByte, font, MARK, 58, 474, MEDIUM_SIZE);
        } else if (person.getMaritalStatus().equals(MaritalStatus.MARRIED)) {
            //Marital status 2
            writeText(pdfContentByte, font, MARK, 94, 474, MEDIUM_SIZE);
        } else if (person.getMaritalStatus().equals(MaritalStatus.DIVORCED)) {
            //Marital status 3
            writeText(pdfContentByte, font, MARK, 134, 474, MEDIUM_SIZE);
        } else {
            //Marital status 4
            writeText(pdfContentByte, font, MARK, 174, 474, MEDIUM_SIZE);
        }

        //height
        writeText(pdfContentByte, font, String.valueOf(pol.getInsureds().get(0).getHealthStatus().getHeightInCm()), 60, 446, MEDIUM_SIZE);

        //weight
        writeText(pdfContentByte, font, String.valueOf(pol.getInsureds().get(0).getHealthStatus().getWeightInKg()), 130, 446, MEDIUM_SIZE);

        //weight change in last 6 months
        if (!pol.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6Months()) {
            writeText(pdfContentByte, font, MARK, 390, 446, MEDIUM_SIZE);
        } else {
            writeText(pdfContentByte, font, MARK, 420, 446, MEDIUM_SIZE);
            String weightChangeReason = pol.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6MonthsReason();
            if (weightChangeReason.equals("น้ำหนักเพิ่ม")) {
                writeText(pdfContentByte, font, weightChangeReason, 533, 444, MEDIUM_SIZE);
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
                writeText(pdfContentByte, font, w1, 534, 450, VERY_SMALL_SIZE);
                writeText(pdfContentByte, font, w2, 534, 444, VERY_SMALL_SIZE);
            }
        }

        //document display
        if (person.getRegistrations().get(0).getTypeName().equals(RegistrationTypeName.THAI_ID_NUMBER)) {
            //document display id card
            writeText(pdfContentByte, font, MARK, 94, 414, MEDIUM_SIZE);
        }
        /*
        //document display house registeration
        g1.drawString(MARK, 800, 915);
        //document display others
        g1.drawString("บัตรประกันสังคม", 1125, 915);
        */

        //id card number or passport number
        writeText(pdfContentByte, font, person.getRegistrations().get(0).getId(), 268, 414, MEDIUM_SIZE);

        //present address number
        writeText(pdfContentByte, font, person.getCurrentAddress().getStreetAddress1(), 126, 356, MEDIUM_SIZE);
        //present address road
        writeText(pdfContentByte, font, solveNullValue(person.getCurrentAddress().getStreetAddress2()), 80, 338, MEDIUM_SIZE);
        //present address sub district
        writeText(pdfContentByte, font, person.getCurrentAddress().getSubdistrict(), 360, 338, MEDIUM_SIZE);
        //present address district
        writeText(pdfContentByte, font, person.getCurrentAddress().getDistrict(), 86, 322, MEDIUM_SIZE);
        //present address province
        writeText(pdfContentByte, font, person.getCurrentAddress().getSubCountry(), 284, 322, MEDIUM_SIZE);
        //present address zipcode
        writeText(pdfContentByte, font, person.getCurrentAddress().getPostCode(), 502, 322, MEDIUM_SIZE);

        //register address same present address check
        if (person.getRegistrationAddress() == null) {
            //register address same present address mark
            writeText(pdfContentByte, font, MARK, 112, 304, MEDIUM_SIZE);
        } else {
            //register address number
            writeText(pdfContentByte, font, person.getRegistrationAddress().getStreetAddress1(), 126, 288, MEDIUM_SIZE);
            //register address road
            writeText(pdfContentByte, font, solveNullValue(person.getRegistrationAddress().getStreetAddress2()), 80, 272, MEDIUM_SIZE);
            //register address sub district
            writeText(pdfContentByte, font, person.getRegistrationAddress().getSubdistrict(), 360, 272, MEDIUM_SIZE);
            //register address district
            writeText(pdfContentByte, font, person.getRegistrationAddress().getDistrict(), 86, 254, MEDIUM_SIZE);
            //register address province
            writeText(pdfContentByte, font, person.getRegistrationAddress().getSubCountry(), 284, 254, MEDIUM_SIZE);
            //register address zipcode
            writeText(pdfContentByte, font, person.getRegistrationAddress().getPostCode(), 502, 254, MEDIUM_SIZE);
        }

        //contact telephone
        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            writeText(pdfContentByte, font, person.getHomePhoneNumber().getNumber(), 80, 220, MEDIUM_SIZE);
        }
        //contact mobile
        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            writeText(pdfContentByte, font, person.getMobilePhoneNumber().getNumber(), 362, 220, MEDIUM_SIZE);
        }

        //contact email
        writeText(pdfContentByte, font, person.getEmail(), 74, 204, MEDIUM_SIZE);

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName()) ||
        		pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IPROTECT.getName())) {
            if (pol.getInsureds().get(0).getProfessionName().equals("คนงานก่อสร้าง")) {
                writeText(pdfContentByte, font, MARK, 36, 134, MEDIUM_SIZE);
            } else if (pol.getInsureds().get(0).getProfessionName().equals("คนขับรถแท๊กซี่ / คนขับรถมอเตอร์ไซค์รับจ้าง")) {
                writeText(pdfContentByte, font, MARK, 108, 134, MEDIUM_SIZE);
            } else if (pol.getInsureds().get(0).getProfessionName().equals("คนงานเหมือง / สำรวจหาน้ำมัน")) {
                writeText(pdfContentByte, font, MARK, 274, 130, MEDIUM_SIZE);
            } else {
                writeText(pdfContentByte, font, MARK, 36, 116, MEDIUM_SIZE);
                writeText(pdfContentByte, font, pol.getInsureds().get(0).getProfessionName(), 126, 116, MEDIUM_SIZE);
            }
        }

        //occupation position
        if (insured.getProfessionName() != null) {
            writeText(pdfContentByte, font, insured.getProfessionName(), 74, 88, MEDIUM_SIZE);
        }
        //occupation job description
        if (insured.getProfessionDescription() != null) {
            writeText(pdfContentByte, font, insured.getProfessionDescription(), 96, 70, MEDIUM_SIZE);
        }
        //annual income
        if (insured.getAnnualIncome() != null) {
            writeText(pdfContentByte, font, MONEY_FORMAT.format(Integer.parseInt(insured.getAnnualIncome(), 10)), 80, 54, MEDIUM_SIZE);
        }
        //source income
        if (insured.getIncomeSources() != null) {
            writeText(pdfContentByte, font, insured.getIncomeSources().stream().collect(joining(",")), 296, 54, MEDIUM_SIZE);
        }
        //working location
        if (insured.getEmployerName() != null) {
            writeText(pdfContentByte, font, insured.getEmployerName(), 86, 36, MEDIUM_SIZE);
        }
    }

    private void getPage2(PdfContentByte pdfContentByte, Policy pol) throws Exception {
        BaseFont font = getBaseFont();

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            writeText(pdfContentByte, font, MARK, 348, 814, MEDIUM_SIZE);
            writeText(pdfContentByte, font, MARK, 350, 780, MEDIUM_SIZE);
            //Premium
            writeText(pdfContentByte, font, MONEY_FORMAT.format(getYearlyPremium(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue(), pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode())), 434, 780, MEDIUM_SIZE);
        } else if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            writeText(pdfContentByte, font, MARK, 38, 802, MEDIUM_SIZE);
            //Plan
            if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE1)) {
                writeText(pdfContentByte, font, MARK, 96, 766, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE2)) {
                writeText(pdfContentByte, font, MARK, 146, 766, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE3)) {
                writeText(pdfContentByte, font, MARK, 196, 766, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE4)) {
                writeText(pdfContentByte, font, MARK, 242, 766, MEDIUM_SIZE);
            } else if (pol.getPremiumsData().getProductIFinePremium().getProductIFinePackage().equals(ProductIFinePackage.IFINE5)) {
                writeText(pdfContentByte, font, MARK, 292, 766, MEDIUM_SIZE);
            }
        } else if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_IPROTECT.getName())) {
        	writeText(pdfContentByte, font, MARK, 348, 814, MEDIUM_SIZE);        	
        	writeText(pdfContentByte, font, MARK, 350, 780, MEDIUM_SIZE);
        	//premium
        	writeText(pdfContentByte, font, MONEY_FORMAT.format(pol.getPremiumsData().getProductIProtectPremium().getSumInsured().getValue()), 430, 780, MEDIUM_SIZE);
        }        

        //coverage period
        writeText(pdfContentByte, font, String.valueOf(pol.getCommonData().getNbOfYearsOfCoverage()), 114, 604, MEDIUM_SIZE);
        //premium period
        writeText(pdfContentByte, font, String.valueOf(pol.getCommonData().getNbOfYearsOfPremium()), 256, 604, MEDIUM_SIZE);

        if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            //dividend option
            if (pol.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
                if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_CASH)) {
                    //divident option 1
                    writeText(pdfContentByte, font, MARK, 32, 560, MEDIUM_SIZE);
                    //divident option 1.1
                    writeText(pdfContentByte, font, MARK, 70, 560, MEDIUM_SIZE);
                } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_FOR_NEXT_PREMIUM)) {
                    //divident option 1
                    writeText(pdfContentByte, font, MARK, 32, 560, MEDIUM_SIZE);
                    //divident option 1.2
                    writeText(pdfContentByte, font, MARK, 70, 544, MEDIUM_SIZE);
                } else if (pol.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(IN_FINE)) {
                    //divident option 2
                    writeText(pdfContentByte, font, MARK, 32, 528, MEDIUM_SIZE);
                }
            }

        }

        //payment mode
        if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH)) {
            //payment mode 1 m
            writeText(pdfContentByte, font, MARK, 126, 498, MEDIUM_SIZE);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_HALF_YEAR)) {
            //payment mode 6 m
            writeText(pdfContentByte, font, MARK, 238, 498, MEDIUM_SIZE);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_QUARTER)) {
            //payment mode 3 m
            writeText(pdfContentByte, font, MARK, 178, 498, MEDIUM_SIZE);
        } else if (pol.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_YEAR)) {
            //payment mode 12 m
            writeText(pdfContentByte, font, MARK, 300, 498, MEDIUM_SIZE);
        }

        //nb premium
        writeText(pdfContentByte, font, MONEY_FORMAT.format(pol.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 162, 474, MEDIUM_SIZE);

        //line payment channel
        writeText(pdfContentByte, font, MARK, 78, 412, MEDIUM_SIZE);

        //Benefit
        List<Integer> listY = getBenefitPositionY();
        List<CoverageBeneficiary> allBenefit = pol.getCoverages().get(0).getBeneficiaries();
        for (Integer a = 0; a < allBenefit.size(); a++) {
            CoverageBeneficiary benefit = pol.getCoverages().get(0).getBeneficiaries().get(a);
            //benefit name
            writeText(pdfContentByte, font, benefit.getPerson().getGivenName() + " " + benefit.getPerson().getSurName(), 38, listY.get(a), MEDIUM_SIZE);
            //benefit age
            writeText(pdfContentByte, font, String.valueOf(benefit.getAgeAtSubscription()), 178, listY.get(a), MEDIUM_SIZE);
            //benefit relation
            writeText(pdfContentByte, font, messageSource.getMessage("relationship." + String.valueOf(benefit.getRelationship()), null, thLocale), 215, listY.get(a), MEDIUM_SIZE);
            //benefit id card number
            writeText(pdfContentByte, font, benefit.getPerson().getRegistrations().get(0).getId(), 274, listY.get(a), MEDIUM_SIZE);

            //benefit address
            writeText(pdfContentByte, font, generateAddress1(benefit.getPerson().getCurrentAddress()), 387, listY.get(a) + 6, SMALL_SIZE);

            writeText(pdfContentByte, font, generateAddress2(benefit.getPerson().getCurrentAddress()), 387, listY.get(a), SMALL_SIZE);

            //benefit benefit percent
            writeText(pdfContentByte, font, String.valueOf(benefit.getCoverageBenefitPercentage()), 526, listY.get(a), MEDIUM_SIZE);
        }

        //health question 1
        writeText(pdfContentByte, font, MARK, 170, 126, MEDIUM_SIZE);

        //health question 2
        writeText(pdfContentByte, font, MARK, 36, 70, MEDIUM_SIZE);
    }

    private void getPage3(PdfContentByte pdfContentByte) throws Exception {
        BaseFont font = getBaseFont();

        //health question 3
        writeText(pdfContentByte, font, MARK, 36, 786, MEDIUM_SIZE);

        //fatca 1
        writeText(pdfContentByte, font, MARK, 44, 700, MEDIUM_SIZE);

        //fatca 2
        writeText(pdfContentByte, font, MARK, 44, 672, MEDIUM_SIZE);

        //fatca 3
        writeText(pdfContentByte, font, MARK, 44, 642, MEDIUM_SIZE);

        //fatca 4
        writeText(pdfContentByte, font, MARK, 44, 600, MEDIUM_SIZE);

        //accept check
        writeText(pdfContentByte, font, MARK, 34, 176, MEDIUM_SIZE);

        //generate date
        Map<String, String> now = doSplitDateOfBirth(LocalDate.now());

        //date
        writeText(pdfContentByte, font, now.get("date"), 370, 160, MEDIUM_SIZE);

        //month
        writeText(pdfContentByte, font, now.get("month"), 430, 160, MEDIUM_SIZE);

        //year
        writeText(pdfContentByte, font, now.get("year"), 516, 160, MEDIUM_SIZE);
    }

    private List<Integer> getBenefitPositionY() {
        List<Integer> listY = new ArrayList<>();
        listY.add(294);
        listY.add(275);
        listY.add(256);
        listY.add(238);
        listY.add(220);
        listY.add(200);
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
