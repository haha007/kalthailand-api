package th.co.krungthaiaxa.ebiz.api.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.*;
import th.co.krungthaiaxa.ebiz.api.model.error.Error;
import th.co.krungthaiaxa.ebiz.api.utils.JsonUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.YEARS;
import static th.co.krungthaiaxa.ebiz.api.products.Product10EC.PRODUCT_10_EC_NAME;

public class TestUtil {



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

    public static Error getErrorFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Error.class);
    }

    public static void quote(Quote quote) {
        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);

        Periodicity periodicity = new Periodicity();
        periodicity.setCode(PeriodicityCode.EVERY_MONTH);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        PremiumsDataLifeInsurance premiumsData = new PremiumsDataLifeInsurance();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setLifeInsuranceSumInsured(amount);

        CommonData commonData = new CommonData();
        commonData.setProductId(PRODUCT_10_EC_NAME);
        commonData.setProductName(PRODUCT_10_EC_NAME);

        Coverage coverage = new Coverage();
        coverage.setName(PRODUCT_10_EC_NAME);
        coverage.addBeneficiary(beneficiary(100.0));

        quote.setCommonData(commonData);
        quote.setPremiumsData(premiumsData);
        quote.getInsureds().remove(0);
        quote.addInsured(insured(35, TRUE));
        quote.addCoverage(coverage);
    }

    public static Quote quote(PeriodicityCode periodicityCode, Insured insured, CoverageBeneficiary... beneficiaries) {
        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);

        Periodicity periodicity = new Periodicity();
        periodicity.setCode(periodicityCode);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        PremiumsDataLifeInsurance premiumsData = new PremiumsDataLifeInsurance();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setLifeInsuranceSumInsured(amount);

        CommonData commonData = new CommonData();
        commonData.setProductId(PRODUCT_10_EC_NAME);
        commonData.setProductName(PRODUCT_10_EC_NAME);

        Coverage coverage = new Coverage();
        coverage.setName(PRODUCT_10_EC_NAME);
        for (CoverageBeneficiary beneficiary : beneficiaries) {
            coverage.addBeneficiary(beneficiary);
        }

        Quote quote = new Quote();
        quote.setCommonData(commonData);
        quote.setPremiumsData(premiumsData);
        if (insured != null) {
            quote.addInsured(insured);
        }
        quote.addCoverage(coverage);
        return quote;
    }

    public static CoverageBeneficiary beneficiary(Double benefitPercent) {
        Person person = new Person();
        person.setGenderCode(GenderCode.FEMALE);
        person.setGivenName("Beneficiary");
        person.setMiddleName("");
        person.setSurName("Benf Last name");
        person.setTitle("M");
        person.addRegistration(registration("3120300153833"));

        CoverageBeneficiary result = new CoverageBeneficiary();
        result.setCoverageBenefitPercentage(benefitPercent);
        result.setRelationship(BeneficiaryRelationshipType.CHILD);
        result.setPerson(person);
        return result;
    }

    public static Insured insured(int ageAtSubscription, boolean mainInsured) {
        Fatca fatca = new Fatca();
        fatca.setBornInUSA(FALSE);
        fatca.setPayTaxInUSA(FALSE);
        fatca.setPermanentResidentOfUSA(USPermanentResident.NOT_PR);
        fatca.setPermanentResidentOfUSAForTax(FALSE);

        Insured insured = new Insured();
        insured.setAgeAtSubscription(ageAtSubscription);
        insured.setDeclaredTaxPercentAtSubscription(5);
        insured.setDisableOrImmunoDeficient(FALSE);
        insured.setEndDate(now().plus(10, ChronoUnit.YEARS));
        insured.setHospitalizedInLast6Months(FALSE);
        insured.setMainInsuredIndicator(mainInsured);
        insured.setProfessionName("Something");
        insured.setStartDate(now());
        insured.setType(InsuredType.Insured);
        insured.setFatca(fatca);


        GeographicalAddress geographicalAddress = new GeographicalAddress();
        geographicalAddress.setCountry("ไทย");
        geographicalAddress.setDistrict("จตุจักร");
        geographicalAddress.setPostCode("10900");
        geographicalAddress.setStreetAddress1("Condo U-delight");
        geographicalAddress.setStreetAddress2("ประชาชื่น");
        geographicalAddress.setSubCountry("Ile de France");
        geographicalAddress.setSubdistrict("ลาดยาว");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setType(PhoneNumberType.MOBILE);
        phoneNumber.setNumber(841139301);
        phoneNumber.setCountryCode(66);

        Person person = new Person();
        person.setBirthDate(now().minus(ageAtSubscription, YEARS));
        person.setEmail("wuttichai.sri@krungthai-axa.co.th");
        person.setGenderCode(GenderCode.FEMALE);
        person.setGeographicalAddress(geographicalAddress);
        person.setGivenName("วุฒิชัย");
        person.setHeightInCm(100);
        person.setHomePhoneNumber(new PhoneNumber());
        person.setMaritalStatus(MaritalStatus.MARRIED);
        person.setMiddleName("Else");
        person.setMobilePhoneNumber(phoneNumber);
        person.setSurName("ศรีสุข");
        person.setTitle("M");
        person.setWeightInKg(75);

        Registration registration = new Registration();
        registration.setId("3841200364454");
        person.addRegistration(registration);

        insured.setPerson(person);
        return insured;
    }

    public static Registration registration(String id) {
        Registration registration = new Registration();
        registration.setId(id);
        registration.setTypeName("Thai ID Card number");
        return registration;
    }

    public static Payment payment(Double value, String currencyCode) {
        return new Payment(value, currencyCode, LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
    }

    public static void policy(Policy policy) {
        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setMethod("12");
        policy.getPayments().get(0).addPaymentInformation(paymentInformation);
    }
}
