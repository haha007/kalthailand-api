package th.co.krungthaiaxa.api.elife.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Fatca;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.GeographicalAddress;
import th.co.krungthaiaxa.api.elife.model.HealthStatus;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.PhoneNumber;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.model.enums.BeneficiaryRelationshipType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.InsuredType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PhoneNumberType;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponseInfo;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponsePaymentInfo;
import th.co.krungthaiaxa.api.elife.products.Product10ECService;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductIFinePackage;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import java.io.IOException;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static th.co.krungthaiaxa.api.elife.model.enums.DividendOption.YEARLY_FOR_NEXT_PREMIUM;
import static th.co.krungthaiaxa.api.elife.model.enums.MaritalStatus.MARRIED;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_HALF_YEAR;
import static th.co.krungthaiaxa.api.elife.model.enums.USPermanentResident.NOT_PR;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.amountTHB;

public class TestUtil {
    public static final String PATH_TEST_RESULT = "target/testresult/";
    public static final String TESTING_EMAIL = "tuongle106@gmail.com";
    public static final String TESTING_EMAIL_FAIL_COLLECTION_PAYMENT = "khoi.tran.ags+kalthailand-api.test.fail@gmail.com";
    public static final String TESTING_HOTMAIL_JO = TestUtil.TESTING_EMAIL;

    public static ProductQuotation productQuotation(Integer age, PeriodicityCode periodicityCode) {
        return productQuotation(ProductType.PRODUCT_10_EC, age, periodicityCode, 350000.0, true, 23, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation(ProductType productType) {
        return productQuotation(productType, 55, EVERY_HALF_YEAR, 100000.0, true, 23, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation() {
        return productQuotation(ProductType.PRODUCT_10_EC, 43, EVERY_HALF_YEAR, 350000.0, true, 23, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation(Double sumInsured) {
        return productQuotation(ProductType.PRODUCT_10_EC, 43, EVERY_HALF_YEAR, sumInsured, true, 23, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation(PeriodicityCode periodicityCode, Double amount, Integer taxRate) {
        return productQuotation(ProductType.PRODUCT_10_EC, 25, periodicityCode, amount, true, taxRate, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation(Integer age, PeriodicityCode periodicityCode, Double amount) {
        return productQuotation(ProductType.PRODUCT_10_EC, age, periodicityCode, amount, true, 23, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation(Integer age, PeriodicityCode periodicityCode, Double amount, GenderCode genderCode) {
        return productQuotation(ProductType.PRODUCT_10_EC, age, periodicityCode, amount, true, 23, genderCode);
    }

    public static ProductQuotation productQuotation(Integer age, PeriodicityCode periodicityCode, Double amount, Boolean isSumInsured) {
        return productQuotation(ProductType.PRODUCT_10_EC, age, periodicityCode, amount, isSumInsured, 23, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation(ProductType productType, Integer age, PeriodicityCode periodicityCode, Double amount) {
        return productQuotation(productType, age, periodicityCode, amount, true, 23, GenderCode.FEMALE);
    }

    public static ProductQuotation productQuotation(ProductType productType, Integer age, PeriodicityCode periodicityCode, Double amount, GenderCode genderCode) {
        return productQuotation(productType, age, periodicityCode, amount, true, 23, genderCode);
    }

    public static ProductQuotation productQuotation(ProductIFinePackage productIFinePackage, Integer age, PeriodicityCode periodicityCode, GenderCode genderCode, Boolean riskOccupation) {
        ProductQuotation productQuotation = productQuotation(ProductType.PRODUCT_IFINE, productIFinePackage.name(), age, periodicityCode, productIFinePackage.getSumInsured(), true, 23, genderCode);
        if (riskOccupation) {
            productQuotation.setOccupationId(21);
        } else {
            productQuotation.setOccupationId(1);
        }
        productQuotation.setPackageName(productIFinePackage.name());
        return productQuotation;
    }

    public static ProductQuotation productQuotation(ProductType productType, Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode) {
        String packageName = null;
        if (productType == ProductType.PRODUCT_IFINE) {
            packageName = ProductIFinePackage.IFINE1.name();
        }
        return productQuotation(productType, packageName, age, periodicityCode, amountValue, isSumInsured, taxRate, genderCode);
    }

    public static ProductQuotation productQuotation(ProductType productType, String packageName, Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode) {
        Amount amount = amountTHB(amountValue);

        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(productType);
        productQuotation.setDateOfBirth(now().minusYears(age));
        productQuotation.setDeclaredTaxPercentAtSubscription(taxRate);
        productQuotation.setGenderCode(genderCode);
        productQuotation.setPeriodicityCode(periodicityCode);
        productQuotation.setOccupationId(1);
        productQuotation.setPackageName(packageName);
        if (isSumInsured) {
            productQuotation.setSumInsuredAmount(amount);
        } else {
            productQuotation.setPremiumAmount(amount);
        }
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

    public static void quote(Quote quote, CoverageBeneficiary... beneficiaries) {
        GeographicalAddress geographicalAddress = new GeographicalAddress();
        geographicalAddress.setDistrict("จตุจักร");
        geographicalAddress.setPostCode("10900");
        geographicalAddress.setStreetAddress1("Condo U-delight");
        geographicalAddress.setStreetAddress2("ประชาชื่น");
        geographicalAddress.setSubCountry("Ile de France");
        geographicalAddress.setSubdistrict("ลาดยาว");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setType(PhoneNumberType.MOBILE);
        phoneNumber.setNumber("0970307001");
        phoneNumber.setCountryCode(66);

        quote.getInsureds().get(0).getFatca().setBornInUSA(FALSE);
        quote.getInsureds().get(0).getFatca().setPayTaxInUSA(FALSE);
        quote.getInsureds().get(0).getFatca().setPermanentResidentOfUSA(NOT_PR);
        quote.getInsureds().get(0).getFatca().setPermanentResidentOfUSAForTax(FALSE);
        quote.getInsureds().get(0).getHealthStatus().setDisableOrImmunoDeficient(FALSE);
        quote.getInsureds().get(0).getHealthStatus().setDeniedOrCounterOffer(FALSE);
        quote.getInsureds().get(0).getHealthStatus().setHeightInCm(100);
        quote.getInsureds().get(0).getHealthStatus().setHospitalizedInLast6Months(FALSE);
        quote.getInsureds().get(0).getHealthStatus().setWeightInKg(100);
        quote.getInsureds().get(0).getHealthStatus().setWeightChangeInLast6Months(FALSE);
        quote.getInsureds().get(0).setEndDate(now().plusYears(10));
        quote.getInsureds().get(0).setMainInsuredIndicator(TRUE);
        quote.getInsureds().get(0).setProfessionId(1);
        quote.getInsureds().get(0).setProfessionName("Something");
        quote.getInsureds().get(0).setStartDate(now());
        quote.getInsureds().get(0).setType(InsuredType.Insured);
        quote.getInsureds().get(0).getPerson().setEmail("santi.lik@krungthai-axa.co.th");
        quote.getInsureds().get(0).getPerson().setCurrentAddress(geographicalAddress);
        quote.getInsureds().get(0).getPerson().setGivenName("วุฒิชัย");
        quote.getInsureds().get(0).getPerson().setHomePhoneNumber(new PhoneNumber());
        quote.getInsureds().get(0).getPerson().setMaritalStatus(MARRIED);
        quote.getInsureds().get(0).getPerson().setMiddleName("");
        quote.getInsureds().get(0).getPerson().setMobilePhoneNumber(phoneNumber);
        quote.getInsureds().get(0).getPerson().setSurName("ศรีสุข");
        quote.getInsureds().get(0).getPerson().setTitle("MR");
        quote.getInsureds().get(0).getPerson().addRegistration(registration("3841200364454"));
        //With "3841200364454": UnitTest run correctly. But when changing number to "3101202780273", UnitTest will not work anymore because beneficiaries Id same as insuredIds, why's that???;

        if (quote.getPremiumsData().getProduct10ECPremium() != null) {
            quote.getPremiumsData().getProduct10ECPremium().setDividendOption(YEARLY_FOR_NEXT_PREMIUM);
        }

        if (quote.getCoverages().size() == 0) {
            quote.getCoverages().add(new Coverage());
        }

        if (beneficiaries != null) {
            BeneficiaryUtils.addBeneficiariesToFirstCoverage(quote, beneficiaries);
        }
    }

    public static Quote quote(ProductService productService) {
        Periodicity periodicity = new Periodicity();
        periodicity.setCode(null);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        Insured emptyInsured = new Insured();
        emptyInsured.setMainInsuredIndicator(true);
        emptyInsured.setFatca(new Fatca());
        emptyInsured.setHealthStatus(new HealthStatus());
        emptyInsured.setPerson(new Person());
        emptyInsured.setType(InsuredType.Insured);

        Quote quote = new Quote();
        quote.setCommonData(productService.initCommonData(null));
        quote.setPremiumsData(productService.initPremiumData());
        quote.addInsured(emptyInsured);

        return quote;
    }

    public static Product10ECService product10ECService() {
        return new Product10ECService();
    }

    //TODO use BeneficiaryFactory
    @Deprecated
    public static CoverageBeneficiary beneficiary(Double benefitPercent) {
        return beneficiary(benefitPercent, "3101202780273");
//        return beneficiary(benefitPercent, "3120300153833");
    }

    //TODO use BeneficiaryFactory
    @Deprecated
    public static CoverageBeneficiary beneficiary(Double benefitPercent, String registrationId) {
        Person person = new Person();
        person.setGenderCode(GenderCode.FEMALE);
        person.setGivenName("Beneficiary");
        person.setMiddleName("");
        person.setSurName("Benf Last name");
        person.setTitle("MR");
        person.addRegistration(registration(registrationId));

        CoverageBeneficiary result = new CoverageBeneficiary();
        result.setAgeAtSubscription(40);
        result.setCoverageBenefitPercentage(benefitPercent);
        result.setRelationship(BeneficiaryRelationshipType.CHILD);
        result.setPerson(person);
        return result;
    }

    public static Insured insured(int ageAtSubscription) {
        return insured(ageAtSubscription, TRUE, "3841200364454", 5, GenderCode.FEMALE);
    }

    /**
     * TODO Replaced by {@link th.co.krungthaiaxa.api.elife.factory.InsuredFactory}
     *
     * @param ageAtSubscription
     * @param mainInsured
     * @param registrationId
     * @param taxRate
     * @param genderCode
     * @return
     */
    @Deprecated
    public static Insured insured(int ageAtSubscription, boolean mainInsured, String registrationId, Integer taxRate, GenderCode genderCode) {
        Fatca fatca = new Fatca();
        fatca.setBornInUSA(FALSE);
        fatca.setPayTaxInUSA(FALSE);
        fatca.setPermanentResidentOfUSA(NOT_PR);
        fatca.setPermanentResidentOfUSAForTax(FALSE);

        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setDisableOrImmunoDeficient(FALSE);
        healthStatus.setDeniedOrCounterOffer(FALSE);
        healthStatus.setHeightInCm(100);
        healthStatus.setHospitalizedInLast6Months(FALSE);
        healthStatus.setWeightInKg(100);

        Insured insured = new Insured();
        insured.setAgeAtSubscription(ageAtSubscription);
        insured.setDeclaredTaxPercentAtSubscription(taxRate);
        insured.setEndDate(now().plusYears(10));
        insured.setMainInsuredIndicator(mainInsured);
        insured.setProfessionId(1);
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
        person.setBirthDate(now().minusYears(ageAtSubscription));
        person.setEmail("santi.lik@krungthai-axa.co.th");
        person.setGenderCode(genderCode);
        person.setCurrentAddress(geographicalAddress);
        person.setGivenName("วุฒิชัย");
        person.setHomePhoneNumber(new PhoneNumber());
        person.setMaritalStatus(MARRIED);
        person.setMiddleName("Else");
        person.setMobilePhoneNumber(phoneNumber);
        person.setSurName("ศรีสุข");
        person.setTitle("MR");

        person.addRegistration(registration(registrationId));

        insured.setPerson(person);
        return insured;
    }

    public static Registration registration(String id) {
        Registration registration = new Registration();
        registration.setId(id);
        registration.setTypeName(RegistrationTypeName.THAI_ID_NUMBER);
        return registration;
    }

    public static void policy(Policy policy) {
        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setMethod("12");
        policy.getPayments().get(0).addPaymentInformation(paymentInformation);
        policy.setValidationAgentCode("123456-12-123456");
    }

    public static LinePayResponse linePayResponse(String returnCode, String returnMessage) {
        return linePayResponse(returnCode, returnMessage, "myTransactionId");
    }

    public static LinePayResponse linePayResponse(String returnCode, String returnMessage, String transactionId) {
        LinePayResponsePaymentInfo payInfo = new LinePayResponsePaymentInfo();
        payInfo.setCreditCardName("myCreditCardName");
        payInfo.setMethod("myMethod");

        LinePayResponseInfo info = new LinePayResponseInfo();
        info.setTransactionId(transactionId);
        info.addPayInfo(payInfo);

        LinePayResponse linePayResponse = new LinePayResponse();
        linePayResponse.setReturnCode(returnCode);
        linePayResponse.setReturnMessage(returnMessage);
        linePayResponse.setInfo(info);

        return linePayResponse;
    }
}
