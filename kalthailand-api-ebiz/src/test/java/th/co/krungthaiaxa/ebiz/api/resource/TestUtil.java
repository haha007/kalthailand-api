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
        CoverageBeneficiary result = new CoverageBeneficiary();
        result.setCoverageBenefitPercentage(benefitPercent);
        result.setRelationship(BeneficiaryRelationshipType.CHILD);
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
        geographicalAddress.setCountry("France");
        geographicalAddress.setDistrict("Something");
        geographicalAddress.setPostCode("75015");
        geographicalAddress.setStreetAddress1("rue du paradis");
        geographicalAddress.setStreetAddress2("apartement 2");
        geographicalAddress.setSubCountry("Ile de France");
        geographicalAddress.setSubdistrict("Paris");

        Person person = new Person();
        person.setBirthDate(now().minus(ageAtSubscription, YEARS));
        person.setEmail("something@something.com");
        person.setGenderCode(GenderCode.FEMALE);
        person.setGeographicalAddress(geographicalAddress);
        person.setGivenName("Someone");
        person.setHeightInCm(100);
        person.setHomePhoneNumber(new PhoneNumber());
        person.setMaritalStatus(MaritalStatus.MARRIED);
        person.setMiddleName("Else");
        person.setMobilePhoneNumber(new PhoneNumber());
        person.setSurName("Surname");
        person.setTitle("M");
        person.setWeightInKg(100);
        insured.setPerson(person);
        return insured;
    }

    public static Payment payment(Double value, String currencyCode) {
        return new Payment(value, currencyCode, LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
    }
}
