package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.PdfUtil;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.GeographicalAddress;
import th.co.krungthaiaxa.api.elife.model.HealthStatus;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.MaritalStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;
import th.co.krungthaiaxa.api.elife.model.product.PremiumDetail;
import th.co.krungthaiaxa.api.elife.products.ProductIFinePackage;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static th.co.krungthaiaxa.api.elife.model.enums.DividendOption.IN_FINE;
import static th.co.krungthaiaxa.api.elife.model.enums.DividendOption.YEARLY_CASH;
import static th.co.krungthaiaxa.api.elife.model.enums.DividendOption.YEARLY_FOR_NEXT_PREMIUM;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_HALF_YEAR;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_QUARTER;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;

@Service
public class ApplicationFormService {
    public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFormService.class);
    private static final float VERY_SMALL_SIZE = 7f;
    private static final float SMALL_SIZE = 10f;
    private static final float MEDIUM_SIZE = 13f;
    private static final float BIG_SIZE = 25f;
    public static final long LIMIT_SIZE = 2097152;//2MB
    public static final String FILE_PATH = "application-form/empty-application-form.pdf";
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");
    private final BaseFont baseFont = PdfUtil.loadBaseFont();
    private final String MARK = "X";
    private final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public byte[] generateNotValidatedApplicationForm(Policy policy) {
        return generateValidatedApplicationForm(policy, false);
    }

    public byte[] generateValidatedApplicationForm(Policy policy) {
        return generateValidatedApplicationForm(policy, true);
    }

    private byte[] generateValidatedApplicationForm(Policy policy, boolean validatedPolicy) {
        try (ByteArrayOutputStream content = new ByteArrayOutputStream()) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_PATH);
            
            PdfReader pdfReader = null;
            pdfReader = new PdfReader(inputStream);

            PdfStamper pdfStamper = new PdfStamper(pdfReader, content);

            // page1
            getPage1(pdfStamper.getOverContent(1), policy, validatedPolicy);

            // page2
            getPage2(pdfStamper.getOverContent(2), policy);

            // page3
            getPage3(pdfStamper.getOverContent(3), policy);

            pdfStamper.close();
            content.close();
            return content.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new FileIOException("Cannot generate application form for policy: " + ObjectMapperUtil.toStringMultiLine(policy), e);
        }
    }

    private void getPage1(PdfContentByte pdfContentByte, Policy policy, boolean validatedPolicy) throws IOException {    	
        Insured insured = ProductUtils.validateExistMainInsured(policy);
        Person person = insured.getPerson();
    	
        //Policy number
        writeText(pdfContentByte, baseFont, policy.getPolicyId(), 475, 757, BIG_SIZE); 

        //add *eBiz App* below barcode
        writeText(pdfContentByte, baseFont, "*eBiz App*", 505, 807, SMALL_SIZE); 

        if (validatedPolicy && isNotEmpty(policy.getValidationAgentCode())) {
            //Validate TMC agent code
            writeText(pdfContentByte, baseFont, policy.getValidationAgentCode(), 490, 739, MEDIUM_SIZE);
        }

        //Title
        if (person.getTitle().equals("MR")) {
            writeText(pdfContentByte, baseFont, MARK, 163, 534, MEDIUM_SIZE);
        }
        if (person.getTitle().equals("MRS")) {
            writeText(pdfContentByte, baseFont, MARK, 201, 534, MEDIUM_SIZE);
        }
        if (person.getTitle().equals("MS")) {
            writeText(pdfContentByte, baseFont, MARK, 233, 534, MEDIUM_SIZE);
        }

        //Name
        writeText(pdfContentByte, baseFont, person.getGivenName() + " " + person.getSurName(), 325, 532, MEDIUM_SIZE);

        //gender
        if (person.getGenderCode().equals(GenderCode.MALE)) {
            //Gender mail
            writeText(pdfContentByte, baseFont, MARK, 65, 504, MEDIUM_SIZE);
        } else {
            //Gender fesmale
            writeText(pdfContentByte, baseFont, MARK, 101, 504, MEDIUM_SIZE);
        }

        //birthdate
        Map<String, String> birthDate = doSplitDateOfBirth(person.getBirthDate());
        //date of birthday
        writeText(pdfContentByte, baseFont, birthDate.get("date"), 175, 504, MEDIUM_SIZE);
        //month of birthday
        writeText(pdfContentByte, baseFont, birthDate.get("month"), 265, 504, MEDIUM_SIZE);
        //year of birthday
        writeText(pdfContentByte, baseFont, birthDate.get("year"), 385, 504, MEDIUM_SIZE);

        //Nationality
        writeText(pdfContentByte, baseFont, "ไทย", 505, 504, MEDIUM_SIZE);

        //marital status
        if (person.getMaritalStatus().equals(MaritalStatus.SINGLE)) {
            //Marital status 1
            writeText(pdfContentByte, baseFont, MARK, 73, 486, MEDIUM_SIZE);
        } else if (person.getMaritalStatus().equals(MaritalStatus.MARRIED)) {
            //Marital status 2
            writeText(pdfContentByte, baseFont, MARK, 109, 486, MEDIUM_SIZE);
        } else if (person.getMaritalStatus().equals(MaritalStatus.DIVORCED)) {
            //Marital status 3
            writeText(pdfContentByte, baseFont, MARK, 149, 486, MEDIUM_SIZE);
        } else {
            //Marital status 4
            writeText(pdfContentByte, baseFont, MARK, 189, 486, MEDIUM_SIZE);
        }

        //height
        writeText(pdfContentByte, baseFont, String.valueOf(policy.getInsureds().get(0).getHealthStatus().getHeightInCm()), 75, 458, MEDIUM_SIZE);

        //weight
        writeText(pdfContentByte, baseFont, String.valueOf(policy.getInsureds().get(0).getHealthStatus().getWeightInKg()), 145, 458, MEDIUM_SIZE);

        //weight change in last 6 months
        if (!policy.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6Months()) {
            writeText(pdfContentByte, baseFont, MARK, 405, 458, MEDIUM_SIZE);
        } else {
            writeText(pdfContentByte, baseFont, MARK, 435, 458, MEDIUM_SIZE);
            String weightChangeReason = policy.getInsureds().get(0).getHealthStatus().getWeightChangeInLast6MonthsReason();
            if (weightChangeReason.equals("น้ำหนักเพิ่ม")) {
                writeText(pdfContentByte, baseFont, weightChangeReason, 548, 456, MEDIUM_SIZE);
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
                writeText(pdfContentByte, baseFont, w1, 549, 462, VERY_SMALL_SIZE);
                writeText(pdfContentByte, baseFont, w2, 549, 456, VERY_SMALL_SIZE);
            }
        }

        //document display
        if (person.getRegistrations().get(0).getTypeName().equals(RegistrationTypeName.THAI_ID_NUMBER)) {
            //document display id card
            writeText(pdfContentByte, baseFont, MARK, 109, 426, MEDIUM_SIZE);
        }
        /*
        //document display house registeration
        g1.drawString(MARK, 800, 915);
        //document display others
        g1.drawString("บัตรประกันสังคม", 1125, 915);
        */

        //id card number or passport number
        writeText(pdfContentByte, baseFont, person.getRegistrations().get(0).getId(), 283, 426, MEDIUM_SIZE);

        //present address number
        writeText(pdfContentByte, baseFont, person.getCurrentAddress().getStreetAddress1(), 141, 368, MEDIUM_SIZE);
        //present address road
        writeText(pdfContentByte, baseFont, solveNullValue(person.getCurrentAddress().getStreetAddress2()), 95, 352, MEDIUM_SIZE);
        //present address sub district
        writeText(pdfContentByte, baseFont, person.getCurrentAddress().getSubdistrict(), 375, 352, MEDIUM_SIZE);
        //present address district
        writeText(pdfContentByte, baseFont, person.getCurrentAddress().getDistrict(), 101, 334, MEDIUM_SIZE);
        //present address province
        writeText(pdfContentByte, baseFont, person.getCurrentAddress().getSubCountry(), 299, 334, MEDIUM_SIZE);
        //present address zipcode
        writeText(pdfContentByte, baseFont, person.getCurrentAddress().getPostCode(), 517, 334, MEDIUM_SIZE);

        //register address same present address check
        if (person.getRegistrationAddress() == null) {
            //register address same present address mark
            writeText(pdfContentByte, baseFont, MARK, 127, 318, MEDIUM_SIZE);
        } else {
            //register address number
            writeText(pdfContentByte, baseFont, person.getRegistrationAddress().getStreetAddress1(), 141, 300, MEDIUM_SIZE);
            //register address road
            writeText(pdfContentByte, baseFont, solveNullValue(person.getRegistrationAddress().getStreetAddress2()), 95, 284, MEDIUM_SIZE);
            //register address sub district
            writeText(pdfContentByte, baseFont, person.getRegistrationAddress().getSubdistrict(), 375, 284, MEDIUM_SIZE);
            //register address district
            writeText(pdfContentByte, baseFont, person.getRegistrationAddress().getDistrict(), 101, 266, MEDIUM_SIZE);
            //register address province
            writeText(pdfContentByte, baseFont, person.getRegistrationAddress().getSubCountry(), 299, 266, MEDIUM_SIZE);
            //register address zipcode
            writeText(pdfContentByte, baseFont, person.getRegistrationAddress().getPostCode(), 517, 266, MEDIUM_SIZE);
        }

        //contact telephone
        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            writeText(pdfContentByte, baseFont, person.getHomePhoneNumber().getNumber(), 95, 232, MEDIUM_SIZE);
        }
        //contact mobile
        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            writeText(pdfContentByte, baseFont, person.getMobilePhoneNumber().getNumber(), 377, 232, MEDIUM_SIZE);
        }

        //contact email
        writeText(pdfContentByte, baseFont, person.getEmail(), 89, 216, MEDIUM_SIZE);

        if (policy.getInsureds().get(0).getProfessionName().equals("คนงานก่อสร้าง")) {
            writeText(pdfContentByte, baseFont, MARK, 51, 146, MEDIUM_SIZE);
        } else if (policy.getInsureds().get(0).getProfessionName().equals("คนขับรถแท๊กซี่ / คนขับรถมอเตอร์ไซค์รับจ้าง")) {
            writeText(pdfContentByte, baseFont, MARK, 123, 146, MEDIUM_SIZE);
        } else if (policy.getInsureds().get(0).getProfessionName().equals("คนงานเหมือง / สำรวจหาน้ำมัน")) {
            writeText(pdfContentByte, baseFont, MARK, 289, 142, MEDIUM_SIZE);
        } else {
            writeText(pdfContentByte, baseFont, MARK, 51, 130, MEDIUM_SIZE);
            writeText(pdfContentByte, baseFont, policy.getInsureds().get(0).getProfessionName(), 141, 130, MEDIUM_SIZE);
        }

        //occupation position
        if (insured.getProfessionName() != null) {
            writeText(pdfContentByte, baseFont, insured.getProfessionName(), 89, 101, MEDIUM_SIZE);
        }
        //occupation job description
        if (insured.getProfessionDescription() != null) {
            writeText(pdfContentByte, baseFont, insured.getProfessionDescription(), 111, 83, MEDIUM_SIZE);
        }
        //annual income
        if (insured.getAnnualIncome() != null) {
            writeText(pdfContentByte, baseFont, MONEY_FORMAT.format(Integer.parseInt(insured.getAnnualIncome(), 10)), 95, 67, MEDIUM_SIZE);
        }
        //source income
        if (insured.getIncomeSources() != null) {
            writeText(pdfContentByte, baseFont, insured.getIncomeSources().stream().collect(joining(",")), 311, 67, MEDIUM_SIZE);
        }
        //working location
        if (insured.getEmployerName() != null) {
            writeText(pdfContentByte, baseFont, insured.getEmployerName(), 101, 49, MEDIUM_SIZE);
        }
    }

    private void getPage2(PdfContentByte pdfContentByte, Policy policy) throws IOException {
        String productId = policy.getCommonData().getProductId();
        if (productId.equals(ProductType.PRODUCT_10_EC.getLogicName())) {
            //TODO Should be changed because we don't use 10EC anymore.
            writeText(pdfContentByte, baseFont, MARK, 362, 827, MEDIUM_SIZE);
            writeText(pdfContentByte, baseFont, MARK, 365, 793, MEDIUM_SIZE);
            //Premium
            writeText(pdfContentByte, baseFont, MONEY_FORMAT.format(getYearlyPremium(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue(), policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode())), 449, 795, MEDIUM_SIZE);
        } else if (productId.equals(ProductType.PRODUCT_IFINE.getLogicName())) {
            writeText(pdfContentByte, baseFont, MARK, 52, 814, MEDIUM_SIZE);
            //Plan
            ProductIFinePackage productIFinePackage = policy.getPremiumsData().getProductIFinePremium().getProductIFinePackage();
            if (productIFinePackage.equals(ProductIFinePackage.IFINE1)) {
                writeText(pdfContentByte, baseFont, MARK, 111, 778, MEDIUM_SIZE);
            } else if (productIFinePackage.equals(ProductIFinePackage.IFINE2)) {
                writeText(pdfContentByte, baseFont, MARK, 161, 778, MEDIUM_SIZE);
            } else if (productIFinePackage.equals(ProductIFinePackage.IFINE3)) {
                writeText(pdfContentByte, baseFont, MARK, 211, 778, MEDIUM_SIZE);
            } else if (productIFinePackage.equals(ProductIFinePackage.IFINE4)) {
                writeText(pdfContentByte, baseFont, MARK, 257, 778, MEDIUM_SIZE);
            } else if (productIFinePackage.equals(ProductIFinePackage.IFINE5)) {
                writeText(pdfContentByte, baseFont, MARK, 307, 778, MEDIUM_SIZE);
            }
        } else if (productId.equals(ProductType.PRODUCT_IPROTECT.getLogicName())) {
            //Header
            writeText(pdfContentByte, baseFont, MARK, 362, 827, MEDIUM_SIZE);
            //Premium line for iProtect
            writeText(pdfContentByte, baseFont, MARK, 365, 793, MEDIUM_SIZE);
            writeText(pdfContentByte, baseFont, MONEY_FORMAT.format(policy.getPremiumsData().getProductIProtectPremium().getSumInsured().getValue()), 490, 795, MEDIUM_SIZE);
        } else if (productId.equals(ProductType.PRODUCT_IGEN.getLogicName())) {
            //Header
            writeText(pdfContentByte, baseFont, MARK, 362, 827, MEDIUM_SIZE);
            //Premium line for iGen
            writeText(pdfContentByte, baseFont, MARK, 365, 776, MEDIUM_SIZE);
            writeText(pdfContentByte, baseFont, MONEY_FORMAT.format(policy.getPremiumsData().getPremiumDetail().getSumInsured().getValue()), 445, 778, MEDIUM_SIZE);
        }

        //coverage period
        writeText(pdfContentByte, baseFont, String.valueOf(policy.getCommonData().getNbOfYearsOfCoverage()), 129, 619, MEDIUM_SIZE);
        //premium period
        writeText(pdfContentByte, baseFont, String.valueOf(policy.getCommonData().getNbOfYearsOfPremium()), 271, 619, MEDIUM_SIZE);

        //TODO how about iGen? Do we need to show dividend option?
        if (productId.equals(ProductType.PRODUCT_10_EC.getLogicName())) {
            //dividend option
            if (policy.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_CASH)) {
                //divident option 1
                writeText(pdfContentByte, baseFont, MARK, 47, 581, MEDIUM_SIZE);
                //divident option 1.1
                writeText(pdfContentByte, baseFont, MARK, 83, 581, MEDIUM_SIZE);
            } else if (policy.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(YEARLY_FOR_NEXT_PREMIUM)) {
                //divident option 1
                writeText(pdfContentByte, baseFont, MARK, 47, 581, MEDIUM_SIZE);
                //divident option 1.2
                writeText(pdfContentByte, baseFont, MARK, 83, 566, MEDIUM_SIZE);
            } else if (policy.getPremiumsData().getProduct10ECPremium().getDividendOption().equals(IN_FINE)) {
                //divident option 2
                writeText(pdfContentByte, baseFont, MARK, 47, 552, MEDIUM_SIZE);
            }

        } else if (productId.equals(ProductType.PRODUCT_IGEN.getLogicName())) {
            fillDataForDividendOption(policy, pdfContentByte, baseFont);
        }

        //payment mode
        if (policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH)) {
            //payment mode 1 m
            writeText(pdfContentByte, baseFont, MARK, 139, 525, MEDIUM_SIZE);
        } else if (policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_HALF_YEAR)) {
        	//payment mode 6 m
            writeText(pdfContentByte, baseFont, MARK, 252, 525, MEDIUM_SIZE);
        } else if (policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_QUARTER)) {
            //payment mode 3 m
            writeText(pdfContentByte, baseFont, MARK, 192, 525, MEDIUM_SIZE);
        } else if (policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_YEAR)) {
            //payment mode 12 m
            writeText(pdfContentByte, baseFont, MARK, 313, 525, MEDIUM_SIZE);
        }

        //nb premium
        writeText(pdfContentByte, baseFont, MONEY_FORMAT.format(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()), 177, 497, MEDIUM_SIZE);

        //line payment channel
        writeText(pdfContentByte, baseFont, MARK, 86, 444, MEDIUM_SIZE);

        //Benefit
        List<Integer> listY = getBenefitPositionY();
        List<CoverageBeneficiary> allBenefit = policy.getCoverages().get(0).getBeneficiaries();
        for (Integer a = 0; a < allBenefit.size(); a++) {
            CoverageBeneficiary benefit = policy.getCoverages().get(0).getBeneficiaries().get(a);
            //benefit name
            writeText(pdfContentByte, baseFont, benefit.getPerson().getGivenName() + " " + benefit.getPerson().getSurName(), 48, listY.get(a), MEDIUM_SIZE);
            //benefit age
            writeText(pdfContentByte, baseFont, String.valueOf(benefit.getAgeAtSubscription()), 188, listY.get(a), MEDIUM_SIZE);
            //benefit relation
            System.out.println((messageSource == null)+"-------->"+benefit.getRelationship());
            //writeText(pdfContentByte, baseFont, "FATHER", 228, listY.get(a), MEDIUM_SIZE);
            writeText(pdfContentByte, baseFont, messageSource.getMessage("relationship." + String.valueOf(benefit.getRelationship()), null, thLocale), 228, listY.get(a), MEDIUM_SIZE);
            
            //benefit id card number
            writeText(pdfContentByte, baseFont, benefit.getPerson().getRegistrations().get(0).getId(), 284, listY.get(a), MEDIUM_SIZE);

            //benefit address
            writeText(pdfContentByte, baseFont, generateAddress1(benefit.getPerson().getCurrentAddress()), 402, listY.get(a) + 6, SMALL_SIZE);

            writeText(pdfContentByte, baseFont, generateAddress2(benefit.getPerson().getCurrentAddress()), 402, listY.get(a), SMALL_SIZE);

            //benefit benefit percent
            writeText(pdfContentByte, baseFont, String.valueOf(benefit.getCoverageBenefitPercentage()), 536, listY.get(a), MEDIUM_SIZE);
        }

        //health question 1
        writeText(pdfContentByte, baseFont, MARK, 184, 184, MEDIUM_SIZE);

        //health question 2
        writeText(pdfContentByte, baseFont, MARK, 50, 133, MEDIUM_SIZE);
        
        //health question 3
        writeText(pdfContentByte, baseFont, MARK, 50, 66, MEDIUM_SIZE);
    }

    private void fillDataForDividendOption(Policy policy, PdfContentByte pdfContentByte, BaseFont font) {
        //dividend option
        PremiumDetail premiumDetail = policy.getPremiumsData().getPremiumDetail();
        if (premiumDetail.getDividendOptionId().equals(ProductDividendOption.ANNUAL_PAY_BACK_CASH.getId())) {
            //divident option 1
            writeText(pdfContentByte, font, MARK, 47, 581, MEDIUM_SIZE);
            //divident option 1.1
            writeText(pdfContentByte, font, MARK, 83, 581, MEDIUM_SIZE);
        } else if (premiumDetail.getDividendOptionId().equals(ProductDividendOption.ANNUAL_PAY_BACK_NEXT_PREMIUM.getId())) {
            //divident option 1
            writeText(pdfContentByte, font, MARK, 47, 581, MEDIUM_SIZE);
            //divident option 1.2
            writeText(pdfContentByte, font, MARK, 83, 566, MEDIUM_SIZE);
        } else if (premiumDetail.getDividendOptionId().equals(ProductDividendOption.END_OF_CONTRACT_PAY_BACK.getId())) {
            //divident option 2        	
            writeText(pdfContentByte, font, MARK, 47, 552, MEDIUM_SIZE);
        }
    }

    private void getPage3(PdfContentByte pdfContentByte, Policy policy) throws IOException {      
    	//tax certification   
    	boolean hasTaxId = false;
    	Person person = policy.getInsureds().get(0).getPerson();    	
    	for(Object obj : person.getRegistrations() ) {
    		Registration reg = (Registration) obj;
    		if(reg.getTypeName().equals(RegistrationTypeName.TAX_ID_NUMBER)) {
    			writeText(pdfContentByte, baseFont, MARK, 67, 794, MEDIUM_SIZE);
    			if(reg.getId() != null)
    				writeText(pdfContentByte, baseFont, reg.getId(), 290, 766, MEDIUM_SIZE);
    			hasTaxId = true;
    			break;
    		}    		
    	}
    	
    	if(!hasTaxId) {
    		writeText(pdfContentByte, baseFont, MARK, 67, 753, MEDIUM_SIZE); 
    	}         

        //fatca 1
        writeText(pdfContentByte, baseFont, MARK, 58, 680, MEDIUM_SIZE);

        //fatca 2
        writeText(pdfContentByte, baseFont, MARK, 58, 652, MEDIUM_SIZE);

        //fatca 3
        writeText(pdfContentByte, baseFont, MARK, 58, 623, MEDIUM_SIZE);

        //fatca 4
        writeText(pdfContentByte, baseFont, MARK, 58, 581, MEDIUM_SIZE);

        //accept check
        writeText(pdfContentByte, baseFont, MARK, 49, 158, MEDIUM_SIZE);

        //generate date
        Map<String, String> now = doSplitDateOfBirth(LocalDate.now());

        //date
        writeText(pdfContentByte, baseFont, now.get("date"), 390, 142, MEDIUM_SIZE);

        //month
        writeText(pdfContentByte, baseFont, now.get("month"), 450, 142, MEDIUM_SIZE);

        //year
        writeText(pdfContentByte, baseFont, now.get("year"), 536, 142, MEDIUM_SIZE);
    }

    private List<Integer> getBenefitPositionY() {
        List<Integer> listY = new ArrayList<>();
        listY.add(343);
        listY.add(324);
        listY.add(305);
        listY.add(287);
        listY.add(269);
        listY.add(249);
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
        PdfUtil.writeText(pdfContentByte, font, text, x, y, fontSize);
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

}
