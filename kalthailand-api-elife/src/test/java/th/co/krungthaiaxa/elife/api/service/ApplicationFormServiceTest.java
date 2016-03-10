package th.co.krungthaiaxa.elife.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.*;
import th.co.krungthaiaxa.elife.api.products.ProductType;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by santilik on 3/7/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ApplicationFormServiceTest {

    @Inject
    private ApplicationFormService appService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PolicyService policyService;

    private final String CURRENCY_CODE = "THB";

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

    private Product10ECPremium getLifeInsurance(Integer indx, Double sumInsured, Double datedAmountValue, DividendOption dividendOption){
        Product10ECPremium product10ECPremium = new Product10ECPremium();
        product10ECPremium.setSumInsured(getAmount(sumInsured,CURRENCY_CODE));
        List<DatedAmount> listyearlyCashBacks = new ArrayList<>();
        for(Integer a = 0;a<indx;a++){
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setValue(datedAmountValue);
            datedAmount.setCurrencyCode(CURRENCY_CODE);
            datedAmount.setDate(LocalDate.now().plusYears(a));
            listyearlyCashBacks.add(datedAmount);
        }
        product10ECPremium.setYearlyCashBacks(listyearlyCashBacks);
        product10ECPremium.setEndOfContractBenefitsMinimum(listyearlyCashBacks);
        product10ECPremium.setEndOfContractBenefitsAverage(listyearlyCashBacks);
        product10ECPremium.setEndOfContractBenefitsMaximum(listyearlyCashBacks);
        product10ECPremium.setYearlyCashBacksAverageDividende(listyearlyCashBacks);
        product10ECPremium.setYearlyCashBacksMaximumDividende(listyearlyCashBacks);
        product10ECPremium.setYearlyCashBacksAverageBenefit(listyearlyCashBacks);
        product10ECPremium.setYearlyCashBacksMaximumBenefit(listyearlyCashBacks);
        product10ECPremium.setDividendOption(dividendOption);
        return product10ECPremium;
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
        registration.setTypeName("Thai ID Card number");
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
        for(Integer a = 0;a<6;a++){
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
            reg.setTypeName("Thai ID Card number");
            ps.addRegistration(reg);
            benefit.setPerson(ps);
            benefit.setRelationship(BeneficiaryRelationshipType.CHILD);
            benefit.setCoverageBenefitPercentage(10.0);
            benefit.setAgeAtSubscription(20);
            coverage.addBeneficiary(benefit);
        }
        return coverage;
    }

    @Test
    public void generate_application_pdf_file() throws Exception {

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
        premiumData.setProduct10ECPremium(getLifeInsurance(10, 100000.0, 2000.0, DividendOption.YEARLY_CASH));

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
        insured.setIncomeSource("Something");
        insured.setEmployerName("Something");

        Person person = getPerson("สันติ", "ณเคชน์", "ลิขิตมงคลสกุล", "นาย");
        person.setMobilePhoneNumber(getPhoneNumber(66,66,"0866666666"));
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

        appService.generatePdfForm(pol);
    }

}
