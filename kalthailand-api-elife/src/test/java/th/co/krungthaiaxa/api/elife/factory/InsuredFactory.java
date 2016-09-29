package th.co.krungthaiaxa.api.elife.factory;

import th.co.krungthaiaxa.api.elife.model.Fatca;
import th.co.krungthaiaxa.api.elife.model.GeographicalAddress;
import th.co.krungthaiaxa.api.elife.model.HealthStatus;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.PhoneNumber;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PhoneNumberType;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.BeneficiaryUtils;

import static java.lang.Boolean.FALSE;
import static th.co.krungthaiaxa.api.elife.model.enums.MaritalStatus.MARRIED;
import static th.co.krungthaiaxa.api.elife.model.enums.USPermanentResident.NOT_PR;

/**
 * @author khoi.tran on 9/29/16.
 */
public class InsuredFactory {

    /**
     * This method only set default data which won't affect the flow of quote & policy calculation.
     * For age, gender, occupation..., it should be set before quoteCalculation {@link th.co.krungthaiaxa.api.elife.service.QuoteService#createQuote(String, ChannelType, ProductQuotation)}
     *
     * @param person
     * @param email
     * @param registrationId
     */
    public static void setDefaultValue(Person person, String email, String registrationId, String phoneNumber) {
        person.setEmail(email);
        person.addRegistration(RegistrationFactory.constructThaiId(registrationId));

        person.setCurrentAddress(constructDefaultGeographical());
        person.setGivenName("วุฒิชัย");
        person.setHomePhoneNumber(constructMockHomeNumber());
        person.setMaritalStatus(MARRIED);
        person.setMiddleName("Else");
        person.setMobilePhoneNumber(constructMobileNumber(phoneNumber));
        person.setSurName("ศรีสุข");
        person.setTitle("MR");
    }

    /**
     * This method only set default data which won't affect the flow of quote & policy calculation.
     * For age, gender, occupation..., it should be set before quoteCalculation {@link th.co.krungthaiaxa.api.elife.service.QuoteService#createQuote(String, ChannelType, ProductQuotation)}
     *
     * @param quote
     * @return
     */
    public static void setDefaultValuesToMainInsured(Quote quote) {
        setMainInsured(quote, "santi.lik@krungthai-axa.co.th", RegistrationFactory.DEFAULT_MAIN_INSURED, "0841139301");
    }

    public static void setDefaultValuesToMainInsuredAnd2Beneficiaries(Quote quote) {
        setDefaultValuesToMainInsured(quote);
        BeneficiaryUtils.addBeneficiariesToFirstCoverage(quote, BeneficiaryFactory.constructDefault2Beneficiaries());
    }

    /**
     * You should call this method only after quoteCalculation ({@link th.co.krungthaiaxa.api.elife.service.QuoteService#createQuote(String, ChannelType, ProductQuotation)}
     *
     * @param quote
     * @param email
     * @param registrationId
     * @param phoneNumber
     */
    public static void setMainInsured(Quote quote, String email, String registrationId, String phoneNumber) {
        Insured insured = ProductUtils.validateExistMainInsured(quote);
        insured.setFatca(constructNoUSAFatca());
        insured.setHealthStatus(constructDefaultHealthStatus());

        Person person = insured.getPerson();
        setDefaultValue(person, email, registrationId, phoneNumber);

        person.addRegistration(RegistrationFactory.constructThaiId(registrationId));

        insured.setPerson(person);
    }

    private static PhoneNumber constructMobileNumber(String number) {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setType(PhoneNumberType.MOBILE);
        phoneNumber.setNumber(number);
        phoneNumber.setCountryCode(66);
        return phoneNumber;
    }

    private static PhoneNumber constructMockHomeNumber() {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setType(PhoneNumberType.HOME);
        phoneNumber.setNumber("000000000");
        phoneNumber.setCountryCode(66);
        return phoneNumber;
    }

    private static Fatca constructNoUSAFatca() {
        Fatca fatca = new Fatca();
        fatca.setBornInUSA(FALSE);
        fatca.setPayTaxInUSA(FALSE);
        fatca.setPermanentResidentOfUSA(NOT_PR);
        fatca.setPermanentResidentOfUSAForTax(FALSE);
        return fatca;
    }

    private static HealthStatus constructDefaultHealthStatus() {
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setDisableOrImmunoDeficient(FALSE);
        healthStatus.setDeniedOrCounterOffer(FALSE);
        healthStatus.setHeightInCm(175);
        healthStatus.setHospitalizedInLast6Months(FALSE);
        healthStatus.setWeightChangeInLast6Months(FALSE);
        healthStatus.setWeightInKg(68);
        return healthStatus;
    }

    private static GeographicalAddress constructDefaultGeographical() {
        GeographicalAddress geographicalAddress = new GeographicalAddress();
        geographicalAddress.setDistrict("จตุจักร");
        geographicalAddress.setPostCode("10900");
        geographicalAddress.setStreetAddress1("Condo U-delight");
        geographicalAddress.setStreetAddress2("ประชาชื่น");
        geographicalAddress.setSubCountry("Ile de France");
        geographicalAddress.setSubdistrict("ลาดยาว");
        return geographicalAddress;
    }
}
