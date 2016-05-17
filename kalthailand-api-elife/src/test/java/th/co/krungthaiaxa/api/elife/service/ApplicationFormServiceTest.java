package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.pdf.PdfReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.model.*;
import th.co.krungthaiaxa.api.elife.model.enums.*;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.KalApiApplication;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ApplicationFormServiceTest {
    @Inject
    private ApplicationFormService appService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_10ec_generate_not_validate_application_pdf_file() throws Exception {
        Policy policy = getPolicy10EC();

        policy.getInsureds().get(0).getPerson().setTitle("MRS");
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
        cov.setRelationship(BeneficiaryRelationshipType.AUNT_UNCLE);
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
        cov.setRelationship(BeneficiaryRelationshipType.AUNT_UNCLE);
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
        cov.setRelationship(BeneficiaryRelationshipType.AUNT_UNCLE);
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
        cov.setRelationship(BeneficiaryRelationshipType.AUNT_UNCLE);
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

    private Policy getPolicyiFine() {
        Quote quote = quoteService.createQuote("xxx", ChannelType.LINE, productQuotation(ProductType.PRODUCT_IFINE, 55, EVERY_YEAR, 100000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }

    private Policy getPolicy10EC() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}