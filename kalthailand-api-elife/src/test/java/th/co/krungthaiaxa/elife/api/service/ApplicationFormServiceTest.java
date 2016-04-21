package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.pdf.PdfReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.*;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;


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
    @Value("${tmp.path.deleted.after.tests}")
    private String tmpPathDeletedAfterTests;

    @Test
    public void should_10ec_generate_not_validate_application_pdf_file() throws Exception {
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

        CoverageBeneficiary cov = new CoverageBeneficiary();
        cov.setRelationship(BeneficiaryRelationshipType.AUNT_UNCLE);
        Person person = new Person();
        person.setGivenName("santi");
        person.setSurName("likit");
        Registration reg = new Registration();
        reg.setId("1111222233334");
        person.addRegistration(reg);
        person.setCurrentAddress(address);
        cov.setPerson(person);
        policy.getCoverages().get(0).addBeneficiary(cov);

        byte[] pdfContent = appService.generateNotValidatedApplicationForm(policy);
        File pdfFile = new File(tmpPathDeletedAfterTests + File.separator + "application-not-validated-10ec-form.pdf");
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
        File pdfFile = new File(tmpPathDeletedAfterTests + File.separator + "application-not-validated-ifine-form.pdf");
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
        File pdfFile = new File(tmpPathDeletedAfterTests + File.separator + "application-validated-10ec-form.pdf");
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
        File pdfFile = new File(tmpPathDeletedAfterTests + File.separator + "application-validated-ifine-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    private Policy getPolicyiFine() {
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 55, EVERY_YEAR, 100000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }

    private Policy getPolicy10EC() {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
