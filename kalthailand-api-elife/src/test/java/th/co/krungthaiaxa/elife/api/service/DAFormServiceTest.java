package th.co.krungthaiaxa.elife.api.service;

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
import th.co.krungthaiaxa.elife.api.products.ProductType;

import javax.inject.Inject;
import java.io.File;
import java.time.LocalDate;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static th.co.krungthaiaxa.elife.api.model.enums.RegistrationTypeName.THAI_ID_NUMBER;

/**
 * Created by santilik on 3/10/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class DAFormServiceTest {

    @Inject
    private DAFormService DAFormService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Value("${tmp.path.deleted.after.tests}")
    private String tmpPathDeletedAfterTests;

    private final String CURRENCY_CODE = "THB";

    @Test
    public void should_generate_da_form_pdf_file() throws Exception {

        Integer age = 35;
        String email = "santi.lik@krungthai-axa.co.th";
        String idNumber = "3101202780273";
        Integer tax = 5;

        Policy pol = new Policy();

        pol.setPolicyId("502-0000018");
        pol.setQuoteId("94861977720631949904");

        CommonData commonData = getCommonData(ProductType.PRODUCT_10_EC.name(),"Product 10 EC",10,6);
        commonData.setMinPremium(getAmount(2682.0,CURRENCY_CODE));
        commonData.setMaxPremium(getAmount(308000.0,CURRENCY_CODE));
        commonData.setMinSumInsured(getAmount(100000.0,CURRENCY_CODE));
        commonData.setMaxSumInsured(getAmount(1000000.0,CURRENCY_CODE));
        commonData.setMinAge(20);
        commonData.setMaxAge(70);

        pol.setCommonData(commonData);

        PremiumsData premiumData = new PremiumsData();
        premiumData.setFinancialScheduler(getFinancialScheduler(PeriodicityCode.EVERY_HALF_YEAR,30800.0));

        pol.setPremiumsData(premiumData);

        Insured insured = new Insured();
        insured.setType(InsuredType.Insured);
        insured.setMainInsuredIndicator(true);
        insured.setStartDate(LocalDate.now());
        insured.setEndDate(LocalDate.now().plusYears(10));
        insured.setAgeAtSubscription(age);
        insured.setProfessionName("Something");
        insured.setProfessionDescription("Something");
        insured.setAnnualIncome("9999999");
        insured.addIncomeSource("Something");
        insured.setEmployerName("Something");

        Person person = getPerson("สันติ", "ณเคชน์", "ลิขิตมงคลสกุล", "นาย");
        person.setMobilePhoneNumber(getPhoneNumber(66,66,"0866666666"));
        person.setWorkPhoneNumber(getPhoneNumber(66,66,"022222222"));
        person.setHomePhoneNumber(getPhoneNumber(66,66,"022222222"));
        person.setEmail(email);
        person.setMaritalStatus(MaritalStatus.SINGLE);
        person.setBirthDate(LocalDate.now().minusYears(age));
        person.setGenderCode(GenderCode.MALE);
        person.setCurrentAddress(getAddress("85/961 ม.วิเศษสุขนคร", "ถนน ประชาอุทิศ", "ทุ่งครุ", "ทุ่งครุ", "กรุงเทพมหานคร", "10140", "ไทย"));
        person.addRegistration(getRegistration(idNumber));

        insured.setPerson(person);

        insured.setFatca(getFatca());

        insured.setHealthStatus(getHealthStatus());

        insured.setDeclaredTaxPercentAtSubscription(tax);

        pol.addInsured(insured);

        pol.addCoverage(getCoverageBenefit());

        byte[] daByte = DAFormService.generateDAForm(pol);
        File pdfFile = new File(tmpPathDeletedAfterTests + File.separator + "da-form.pdf");
        writeByteArrayToFile(pdfFile, daByte);
    }

    private CommonData getCommonData(String productType, String productName, Integer productCoverageYears, Integer productPremiumYears){
        CommonData commonData = new CommonData();
        commonData.setProductId(productType);
        commonData.setProductName(productName);
        commonData.setNbOfYearsOfCoverage(productCoverageYears);
        commonData.setNbOfYearsOfPremium(productPremiumYears);
        return commonData;
    }

    private Amount getAmount(Double value, String currencyCode){
        Amount amount = new Amount();
        amount.setValue(value);
        amount.setCurrencyCode(currencyCode);
        return amount;
    }

    private FinancialScheduler getFinancialScheduler(PeriodicityCode periodicityCode, Double modalAmount){
        FinancialScheduler financialScheduler = new FinancialScheduler();
        Periodicity periodicity = new Periodicity();
        periodicity.setCode(periodicityCode);
        financialScheduler.setPeriodicity(periodicity);
        financialScheduler.setModalAmount(getAmount(modalAmount,CURRENCY_CODE));
        return financialScheduler;
    }

    private Person getPerson(String giveName, String middleName, String surName, String title){
        Person person = new Person();
        person.setGivenName(giveName);
        person.setMiddleName(middleName);
        person.setSurName(surName);
        person.setTitle(title);
        return person;
    }

    private PhoneNumber getPhoneNumber(Integer areaCode, Integer countryCode, String number){
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setAreaCode(areaCode);
        phoneNumber.setCountryCode(countryCode);
        phoneNumber.setNumber(number);
        return phoneNumber;
    }

    private Registration getRegistration(String idNumber){
        Registration registration = new Registration();
        registration.setId(idNumber);
        registration.setTypeName(THAI_ID_NUMBER);
        return registration;
    }

    private GeographicalAddress getAddress(String add1, String add2, String subDistrict, String district, String subCountry, String postCode, String country){
        GeographicalAddress address = new GeographicalAddress();
        address.setStreetAddress1(add1);
        address.setStreetAddress2(add2);
        address.setSubdistrict(subDistrict);
        address.setDistrict(district);
        address.setPostCode(postCode);
        address.setSubCountry(subCountry);
        address.setCountry(country);
        return address;
    }

    private Fatca getFatca(){
        Fatca fatca = new Fatca();
        fatca.setBornInUSA(false);
        fatca.setPayTaxInUSA(false);
        fatca.setPermanentResidentOfUSA(USPermanentResident.NOT_PR);
        return fatca;
    }

    private HealthStatus getHealthStatus(){
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setDeniedOrCounterOffer(false);
        healthStatus.setDisableOrImmunoDeficient(false);
        healthStatus.setHospitalizedInLast6Months(false);
        return healthStatus;
    }

    private Coverage getCoverageBenefit(){
        Coverage coverage = new Coverage();
        for(Integer a = 0;a<3;a++){
            CoverageBeneficiary benefit = new CoverageBeneficiary();
            Person ps = new Person();
            ps.setGivenName("name_"+a);
            ps.setMiddleName("middle_"+a);
            ps.setSurName("sur_"+a);
            ps.setTitle("title_"+a);
            ps.setGenderCode(GenderCode.MALE);
            ps.setCurrentAddress(getAddress("123 หมู่1", "ถนน ประชาอุทิศ", "", "", "", "", "ไทย"));
            Registration reg = new Registration();
            reg.setId("3101202780273");
            reg.setTypeName(THAI_ID_NUMBER);
            ps.addRegistration(reg);
            benefit.setPerson(ps);
            benefit.setRelationship(BeneficiaryRelationshipType.CHILD);
            benefit.setCoverageBenefitPercentage(10.0);
            benefit.setAgeAtSubscription(20);
            coverage.addBeneficiary(benefit);
        }
        return coverage;
    }

}
