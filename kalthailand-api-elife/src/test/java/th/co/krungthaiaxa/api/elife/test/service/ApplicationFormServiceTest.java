package th.co.krungthaiaxa.api.elife.test.service;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.PdfReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.BeneficiaryFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.GeographicalAddress;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.PhoneNumber;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.model.enums.BeneficiaryRelationshipType;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.DividendOption;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.MaritalStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductIFinePackage;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.ApplicationFormService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.TestUtil.quote;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ApplicationFormServiceTest extends ELifeTest {
    Logger logger = LoggerFactory.getLogger(ApplicationFormService.class);
    @Inject
    private ApplicationFormService appService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void test_limit_size_of_application_form() {
        String filePath = ApplicationFormService.FILE_PATH;
        long sizeLimit = ApplicationFormService.LIMIT_SIZE;
        long actualSize = IOUtil.loadBinaryFileInClassPath("/" + filePath).length;

        double actualSizeMB = (double) actualSize / (1024 * 1024);
        double sizeLimitMB = (double) sizeLimit / (1024 * 1024);
        String msg = String.format("Size of application form %s is %s bytes (%s MB). Actual size: %s bytes (%s MB)", filePath, sizeLimit, sizeLimitMB, actualSize, actualSizeMB);

        logger.info(msg);
        Assert.assertTrue(msg, actualSize <= ApplicationFormService.LIMIT_SIZE);
    }

    @Test
    public void should_10ec_generate_not_validate_application_pdf_file() throws Exception {
        Policy policy = getPolicy10EC();

        policy.getInsureds().get(0).getPerson().setTitle("MR");
        policy.getInsureds().get(0).getPerson().setGenderCode(GenderCode.MALE);
        policy.getInsureds().get(0).getPerson().setMaritalStatus(MaritalStatus.WIDOW);
        GeographicalAddress address = new GeographicalAddress();
        address.setStreetAddress1("123/456 ม.วิเศษสุขนคร โครงการ18");
        address.setStreetAddress2("ถนนประชาอุทิศ");
        address.setSubdistrict("ทุ่งครุ");
        address.setDistrict("ทุ่งครุ");
        address.setSubCountry("กรุงเทพมหานคร");
        address.setPostCode("10140");
        policy.getInsureds().get(0).getPerson().setRegistrationAddress(address);
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("022222222");
        policy.getInsureds().get(0).getPerson().setHomePhoneNumber(phone);
        policy.getPremiumsData().getProduct10ECPremium().setDividendOption(DividendOption.IN_FINE);
        Periodicity p = new Periodicity();
        p.setCode(PeriodicityCode.EVERY_QUARTER);
        policy.getPremiumsData().getFinancialScheduler().setPeriodicity(p);

        //benefit 2
        CoverageBeneficiary cov = new CoverageBeneficiary();
        cov.setRelationship(BeneficiaryRelationshipType.UNCLE_AUNT_FATHER);
        Person person = new Person();
        person.setGivenName("สันติ");
        person.setSurName("ลิขิตมงคลสกุล");
        Registration reg = new Registration();
        reg.setId("1111222233334");
        person.addRegistration(reg);
        person.setCurrentAddress(address);
        cov.setPerson(person);
        policy.getCoverages().get(0).addBeneficiary(cov);

        //benefit 3
        cov = new CoverageBeneficiary();
        cov.setRelationship(BeneficiaryRelationshipType.AUNT);
        person = new Person();
        person.setGivenName("ธนวัฒน์");
        person.setSurName("เหมชัว");
        reg = new Registration();
        reg.setId("1111222233334");
        person.addRegistration(reg);

        address = new GeographicalAddress();
        address.setStreetAddress1("30/31 หมู่บ้าน พรวิภาปาร์ค");
        address.setStreetAddress2("ถนนพุฒมนทลสาย3");
        address.setSubdistrict("ทวีวัฒนา");
        address.setDistrict("ทวีวัฒนา");
        address.setSubCountry("กรุงเทพมหานคร");
        address.setPostCode("10170");

        person.setCurrentAddress(address);
        cov.setPerson(person);
        policy.getCoverages().get(0).addBeneficiary(cov);

        //benefit 4
        cov = new CoverageBeneficiary();
        cov.setRelationship(BeneficiaryRelationshipType.AUNT);
        person = new Person();
        person.setGivenName("ทศพล");
        person.setSurName("จินตนพันธ์");
        reg = new Registration();
        reg.setId("1111222233334");
        person.addRegistration(reg);

        address = new GeographicalAddress();
        address.setStreetAddress1("61/112 ซ.ประเสริฐมนูกิจ27");
        address.setStreetAddress2("ถ.ประเสริฐมนูกิจ");
        address.setSubdistrict("จรเข้บัว");
        address.setDistrict("ลาดพร้าว");
        address.setSubCountry("กรุงเทพมหานคร");
        address.setPostCode("10230");

        person.setCurrentAddress(address);
        cov.setPerson(person);
        policy.getCoverages().get(0).addBeneficiary(cov);

        //benefit 5
        cov = new CoverageBeneficiary();
        cov.setRelationship(BeneficiaryRelationshipType.AUNT);
        person = new Person();
        person.setGivenName("วิบูลย์");
        person.setSurName("โอฬารสิริกุล");
        reg = new Registration();
        reg.setId("1111222233334");
        person.addRegistration(reg);

        address = new GeographicalAddress();
        address.setStreetAddress1("34/15 หมู่บ้านนนทรี4 หมู่5");
        address.setStreetAddress2("ถนนสุขาภิบาล1");
        address.setSubdistrict("บางศรีเมือง");
        address.setDistrict("เมือง");
        address.setSubCountry("นนทบุรี");
        address.setPostCode("11000");

        person.setCurrentAddress(address);
        cov.setPerson(person);
        policy.getCoverages().get(0).addBeneficiary(cov);

        //weight change in last 6 months
        policy.getInsureds().get(0).getHealthStatus().setWeightChangeInLast6Months(true);
        policy.getInsureds().get(0).getHealthStatus().setWeightChangeInLast6MonthsReason("น้ำหนักเพิ่ม");

        byte[] pdfContent = appService.generateNotValidatedApplicationForm(policy);
        File pdfFile = new File("target/application-not-validated-10ec-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    @Test
    public void should_ifine_generate_not_validate_application_pdf_file() throws Exception {
        Policy policy = getPolicyiFine();

        policy.getInsureds().get(0).getPerson().setTitle("MRS");
        policy.getInsureds().get(0).getPerson().setGenderCode(GenderCode.MALE);
        policy.getInsureds().get(0).getPerson().setMaritalStatus(MaritalStatus.WIDOW);
        GeographicalAddress address = new GeographicalAddress();
        address.setStreetAddress1("test");
        address.setStreetAddress2("555");
        address.setSubdistrict("subdistrict");
        address.setDistrict("district");
        address.setSubCountry("province");
        address.setPostCode("11111");
        policy.getInsureds().get(0).getPerson().setRegistrationAddress(address);
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("022222222");
        policy.getInsureds().get(0).getPerson().setHomePhoneNumber(phone);
        policy.getInsureds().get(0).setProfessionName("ข้าราชการ-พนักงานฝ่ายปกครอง");
        policy.getInsureds().get(0).setProfessionDescription("test description");
        policy.getInsureds().get(0).setAnnualIncome("9999999");
        policy.getInsureds().get(0).addIncomeSource("test source income");
        policy.getInsureds().get(0).setEmployerName("test workplace");
        policy.getPremiumsData().getProductIFinePremium().setProductIFinePackage(ProductIFinePackage.IFINE5);

        byte[] pdfContent = appService.generateNotValidatedApplicationForm(policy);
        File pdfFile = new File("target/application-not-validated-ifine-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    @Test
    public void should_10ec_generate_validate_application_pdf_file() throws Exception {
        Policy policy = getPolicy10EC();

        policy.getInsureds().get(0).getPerson().setTitle("MRS");
        policy.getInsureds().get(0).getPerson().setGenderCode(GenderCode.MALE);
        policy.getInsureds().get(0).getPerson().setMaritalStatus(MaritalStatus.WIDOW);
        GeographicalAddress address = new GeographicalAddress();
        address.setStreetAddress1("test");
        address.setStreetAddress2("555");
        address.setSubdistrict("subdistrict");
        address.setDistrict("district");
        address.setSubCountry("province");
        address.setPostCode("11111");
        policy.getInsureds().get(0).getPerson().setRegistrationAddress(address);
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("022222222");
        policy.getInsureds().get(0).getPerson().setHomePhoneNumber(phone);
        policy.getPremiumsData().getProduct10ECPremium().setDividendOption(DividendOption.IN_FINE);
        Periodicity p = new Periodicity();
        p.setCode(PeriodicityCode.EVERY_QUARTER);
        policy.getPremiumsData().getFinancialScheduler().setPeriodicity(p);
        policy.setValidationAgentCode("000000-00-000000");

        byte[] pdfContent = appService.generateValidatedApplicationForm(policy);
        File pdfFile = new File("target/application-validated-10ec-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    @Test
    public void should_ifine_generate_validate_application_pdf_file() throws Exception {
        Policy policy = getPolicyiFine();

        policy.getInsureds().get(0).getPerson().setTitle("MRS");
        policy.getInsureds().get(0).getPerson().setGenderCode(GenderCode.MALE);
        policy.getInsureds().get(0).getPerson().setMaritalStatus(MaritalStatus.WIDOW);
        GeographicalAddress address = new GeographicalAddress();
        address.setStreetAddress1("test");
        address.setStreetAddress2("555");
        address.setSubdistrict("subdistrict");
        address.setDistrict("district");
        address.setSubCountry("province");
        address.setPostCode("11111");
        policy.getInsureds().get(0).getPerson().setRegistrationAddress(address);
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("022222222");
        policy.getInsureds().get(0).getPerson().setHomePhoneNumber(phone);
        policy.getInsureds().get(0).setProfessionName("ข้าราชการ-พนักงานฝ่ายปกครอง");
        policy.getInsureds().get(0).setProfessionDescription("test description");
        policy.getInsureds().get(0).setAnnualIncome("9999999");
        policy.getInsureds().get(0).addIncomeSource("test source income");
        policy.getInsureds().get(0).setEmployerName("test workplace");
        policy.getPremiumsData().getProductIFinePremium().setProductIFinePackage(ProductIFinePackage.IFINE5);
        policy.setValidationAgentCode("000000-00-000000");

        byte[] pdfContent = appService.generateValidatedApplicationForm(policy);
        File pdfFile = new File("target/application-validated-ifine-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    @Test
    public void should_iprotect_generate_not_validate_application_pdf_file() throws Exception {
        Policy policy = getPolicyiProtect();

        policy.getInsureds().get(0).getPerson().setTitle("MR");
        policy.getInsureds().get(0).getPerson().setGenderCode(GenderCode.MALE);
        policy.getInsureds().get(0).getPerson().setMaritalStatus(MaritalStatus.WIDOW);
        GeographicalAddress address = new GeographicalAddress();
        address.setStreetAddress1("123/456 ม.พฤกษา");
        address.setStreetAddress2("ซ.อ่อนนุช 34");
        address.setSubdistrict("แขวงวัดดอกไม้");
        address.setDistrict("เขตดินแดง");
        address.setSubCountry("กรุงเทพ ฯ");
        address.setPostCode("10140");
        policy.getInsureds().get(0).getPerson().setRegistrationAddress(address);
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("022222222");
        policy.getInsureds().get(0).getPerson().setHomePhoneNumber(phone);
        policy.getInsureds().get(0).setProfessionName("ข้าราชการ-พนักงานฝ่ายปกครอง");
        policy.getInsureds().get(0).setProfessionDescription("ดูแลนักการเมืองไทย");
        policy.getInsureds().get(0).setAnnualIncome("9999999");
        policy.getInsureds().get(0).addIncomeSource("ปล้นเขากิน");
        policy.getInsureds().get(0).setEmployerName("ดาวโลก");

        byte[] pdfContent = appService.generateNotValidatedApplicationForm(policy);
        File pdfFile = new File("target/application-not-validated-iprotect-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    @Test
    public void should_iprotect_generate_validate_application_pdf_file() throws Exception {
        Policy policy = getPolicyiProtect();

        policy.getInsureds().get(0).getPerson().setTitle("MR");
        policy.getInsureds().get(0).getPerson().setGenderCode(GenderCode.MALE);
        policy.getInsureds().get(0).getPerson().setMaritalStatus(MaritalStatus.WIDOW);
        GeographicalAddress address = new GeographicalAddress();
        address.setStreetAddress1("123/456 ม.พฤกษา");
        address.setStreetAddress2("ซ.อ่อนนุช 34");
        address.setSubdistrict("แขวงวัดดอกไม้");
        address.setDistrict("เขตดินแดง");
        address.setSubCountry("กรุงเทพ ฯ");
        address.setPostCode("10140");
        policy.getInsureds().get(0).getPerson().setRegistrationAddress(address);
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("022222222");
        policy.getInsureds().get(0).getPerson().setHomePhoneNumber(phone);
        policy.getInsureds().get(0).setProfessionName("ข้าราชการ-พนักงานฝ่ายปกครอง");
        policy.getInsureds().get(0).setProfessionDescription("ดูแลนักการเมืองไทย");
        policy.getInsureds().get(0).setAnnualIncome("9999999");
        policy.getInsureds().get(0).addIncomeSource("ปล้นเขากิน");
        policy.getInsureds().get(0).setEmployerName("ดาวโลก");
        policy.setValidationAgentCode("000000-00-000000");

        byte[] pdfContent = appService.generateValidatedApplicationForm(policy);
        File pdfFile = new File("target/application-validated-iprotect-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    private Policy getPolicyiProtect() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE,
                createDefaultProductQuotation());
        quote(quote, BeneficiaryFactory.constructDefaultBeneficiary());
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");

        return policyService.createPolicy(quote);
    }

    private ProductQuotation createDefaultProductQuotation() {
        return ProductQuotationFactory.constructIProtectDefault();
    }

    private Policy getPolicyiFine() {
        Quote quote = quoteService.createQuote("xxx", ChannelType.LINE, productQuotation(ProductType.PRODUCT_IFINE, 55, EVERY_YEAR, 100000.0));
        quote(quote, BeneficiaryFactory.constructDefaultBeneficiary());
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");

        return policyService.createPolicy(quote);
    }

    private Policy getPolicy10EC() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation());
        quote(quote, BeneficiaryFactory.constructDefaultBeneficiary());
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");

        return policyService.createPolicy(quote);
    }
}
