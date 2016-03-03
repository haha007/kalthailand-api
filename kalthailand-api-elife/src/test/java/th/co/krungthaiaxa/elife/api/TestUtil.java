package th.co.krungthaiaxa.elife.api;

import com.fasterxml.jackson.core.type.TypeReference;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.*;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.products.Product10EC;
import th.co.krungthaiaxa.elife.api.products.ProductAmounts;
import th.co.krungthaiaxa.elife.api.products.ProductQuotation;
import th.co.krungthaiaxa.elife.api.products.ProductType;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.YEARS;
import static th.co.krungthaiaxa.elife.api.model.enums.GenderCode.FEMALE;
import static th.co.krungthaiaxa.elife.api.model.enums.MaritalStatus.MARRIED;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_HALF_YEAR;
import static th.co.krungthaiaxa.elife.api.products.Product10EC.PRODUCT_10_EC_NAME;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_10_EC;

public class TestUtil {

    public static ProductQuotation productQuotation(Integer age, PeriodicityCode periodicityCode) {
        return productQuotation(PRODUCT_10_EC, age, periodicityCode);
    }

    public static ProductQuotation productQuotation(ProductType productType) {
        return productQuotation(productType, 43, EVERY_HALF_YEAR);
    }

    public static ProductQuotation productQuotation() {
        return productQuotation(PRODUCT_10_EC, 43, EVERY_HALF_YEAR);
    }

    public static ProductQuotation productQuotation(ProductType productType, Integer age, PeriodicityCode periodicityCode) {
        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(350000.0);

        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(productType);
        productQuotation.setDateOfBirth(now().minus(age, YEARS));
        productQuotation.setDeclaredTaxPercentAtSubscription(23);
        productQuotation.setGenderCode(FEMALE);
        productQuotation.setPeriodicityCode(periodicityCode);
        productQuotation.setSumInsuredAmount(amount);
        return productQuotation;
    }

    public static String getJSon(Object object) throws IOException {
        return JsonUtil.mapper.writeValueAsString(object);
    }

    public static Quote getQuoteFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Quote.class);
    }

    public static Policy getPolicyFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Policy.class);
    }

    public static List<Payment> getPaymentsFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, new TypeReference<List<Payment>>() {
        });
    }

    public static List<Document> getDocumentsFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, new TypeReference<List<Document>>() {
        });
    }

    public static Error getErrorFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Error.class);
    }

    public static ProductAmounts getProductAmountsFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, ProductAmounts.class);
    }

    public static void quote(Quote quote, PeriodicityCode periodicityCode, Double sumInsuredAmount, Insured insured, CoverageBeneficiary... beneficiaries) {
        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(sumInsuredAmount);

        Periodicity periodicity = new Periodicity();
        periodicity.setCode(periodicityCode);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        LifeInsurance lifeInsurance = new LifeInsurance();
        lifeInsurance.setSumInsured(amount);

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setLifeInsurance(lifeInsurance);

        Product10EC product10EC = new Product10EC();
        quote.setCommonData(product10EC.getCommonData());
        quote.setPremiumsData(premiumsData);
        quote.getInsureds().remove(0);
        quote.addInsured(insured);
        if (quote.getCoverages().size() == 0) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_10_EC_NAME);
            quote.addCoverage(coverage);
        }

        for (CoverageBeneficiary beneficiary : beneficiaries) {
            quote.getCoverages().get(0).addBeneficiary(beneficiary);
        }
    }

    public static Quote quote(PeriodicityCode periodicityCode, Insured insured, CoverageBeneficiary... beneficiaries) {
        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);

        Periodicity periodicity = new Periodicity();
        periodicity.setCode(periodicityCode);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        LifeInsurance lifeInsurance = new LifeInsurance();
        lifeInsurance.setSumInsured(amount);

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setLifeInsurance(lifeInsurance);

        Product10EC product10EC = new Product10EC();

        Quote quote = new Quote();
        quote.setCommonData(product10EC.getCommonData());
        quote.setPremiumsData(premiumsData);
        if (insured != null) {
            quote.addInsured(insured);
        }
        if (quote.getCoverages().size() == 0) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_10_EC_NAME);
            quote.addCoverage(coverage);
        }

        for (CoverageBeneficiary beneficiary : beneficiaries) {
            quote.getCoverages().get(0).addBeneficiary(beneficiary);
        }

        return quote;
    }

    public static CoverageBeneficiary beneficiary(Double benefitPercent) {
        return beneficiary(benefitPercent, "3101202780273");
//        return beneficiary(benefitPercent, "3120300153833");
    }

    public static CoverageBeneficiary beneficiary(Double benefitPercent, String registrationId) {
        Person person = new Person();
        person.setGenderCode(FEMALE);
        person.setGivenName("Beneficiary");
        person.setMiddleName("");
        person.setSurName("Benf Last name");
        person.setTitle("M");
        person.addRegistration(registration(registrationId));

        CoverageBeneficiary result = new CoverageBeneficiary();
        result.setAgeAtSubscription(40);
        result.setCoverageBenefitPercentage(benefitPercent);
        result.setRelationship(BeneficiaryRelationshipType.CHILD);
        result.setPerson(person);
        return result;
    }

    public static Insured insured(int ageAtSubscription) {
        return insured(ageAtSubscription, TRUE, "3841200364454", 5, FEMALE);
    }

    public static Insured insured(int ageAtSubscription, GenderCode genderCode) {
        return insured(ageAtSubscription, TRUE, "3841200364454", 5, genderCode);
    }

    public static Insured insured(int ageAtSubscription, Integer taxRate) {
        return insured(ageAtSubscription, TRUE, "3841200364454", taxRate, FEMALE);
    }

    public static Insured insured(int ageAtSubscription, String registrationId) {
        return insured(ageAtSubscription, TRUE, registrationId, 5, FEMALE);
    }

    public static Insured insured(int ageAtSubscription, boolean mainInsured) {
        return insured(ageAtSubscription, mainInsured, "3841200364454", 5, FEMALE);
    }

    public static Insured insured(int ageAtSubscription, boolean mainInsured, String registrationId, Integer taxRate, GenderCode genderCode) {
        Fatca fatca = new Fatca();
        fatca.setBornInUSA(FALSE);
        fatca.setPayTaxInUSA(FALSE);
        fatca.setPermanentResidentOfUSA(USPermanentResident.NOT_PR);
        fatca.setPermanentResidentOfUSAForTax(FALSE);

        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setDisableOrImmunoDeficient(FALSE);
        healthStatus.setHeightInCm(100);
        healthStatus.setHospitalizedInLast6Months(FALSE);
        healthStatus.setWeightInKg(100);

        Insured insured = new Insured();
        insured.setAgeAtSubscription(ageAtSubscription);
        insured.setDeclaredTaxPercentAtSubscription(taxRate);
        insured.setEndDate(now().plus(10, ChronoUnit.YEARS));
        insured.setMainInsuredIndicator(mainInsured);
        insured.setProfessionName("Something");
        insured.setStartDate(now());
        insured.setType(InsuredType.Insured);
        insured.setFatca(fatca);
        insured.setHealthStatus(healthStatus);

        GeographicalAddress geographicalAddress = new GeographicalAddress();
        geographicalAddress.setDistrict("จตุจักร");
        geographicalAddress.setPostCode("10900");
        geographicalAddress.setStreetAddress1("Condo U-delight");
        geographicalAddress.setStreetAddress2("ประชาชื่น");
        geographicalAddress.setSubCountry("Ile de France");
        geographicalAddress.setSubdistrict("ลาดยาว");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setType(PhoneNumberType.MOBILE);
        phoneNumber.setNumber("0841139301");
        phoneNumber.setCountryCode(66);

        Person person = new Person();
        person.setBirthDate(now().minus(ageAtSubscription, YEARS));
        person.setEmail("santi.lik@krungthai-axa.co.th");
        person.setGenderCode(genderCode);
        person.setCurrentAddress(geographicalAddress);
        person.setGivenName("วุฒิชัย");
        person.setHomePhoneNumber(new PhoneNumber());
        person.setMaritalStatus(MARRIED);
        person.setMiddleName("Else");
        person.setMobilePhoneNumber(phoneNumber);
        person.setSurName("ศรีสุข");
        person.setTitle("M");

        person.addRegistration(registration(registrationId));

        insured.setPerson(person);
        return insured;
    }

    public static Registration registration(String id) {
        Registration registration = new Registration();
        registration.setId(id);
        registration.setTypeName("Thai ID Card number");
        return registration;
    }

    public static void policy(Policy policy) {
        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setMethod("12");
        policy.getPayments().get(0).addPaymentInformation(paymentInformation);
    }
}
