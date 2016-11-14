package th.co.krungthaiaxa.api.elife.factory;

import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.enums.BeneficiaryRelationshipType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

/**
 * @author khoi.tran on 9/29/16.
 */
public class BeneficiaryFactory {

    public static CoverageBeneficiary[] constructDefault2Beneficiaries() {
        return new CoverageBeneficiary[] { construct(45.25, RegistrationFactory.DEFAULT_BENEFICIARY_01), construct(54.75, RegistrationFactory.DEFAULT_BENEFICIARY_02) };
    }

    public static CoverageBeneficiary[] constructDefaultBeneficiary() {
        return new CoverageBeneficiary[] { construct(100.0, RegistrationFactory.DEFAULT_BENEFICIARY_01) };
    }

    public static CoverageBeneficiary construct(Double benefitPercent, String registrationId) {
        Person person = new Person();
        person.setGenderCode(GenderCode.FEMALE);
        person.setGivenName("Beneficiary");
        person.setMiddleName("");
        person.setSurName("Benf Last name");
        person.setTitle("MR");
        person.addRegistration(RegistrationFactory.constructThaiId(registrationId));
        person.setHomePhoneNumber(PersonFactory.constructMockHomeNumber());
        person.setWorkPhoneNumber(PersonFactory.constructMockPhoneNumber("876543210"));
        CoverageBeneficiary result = new CoverageBeneficiary();
        result.setAgeAtSubscription(40);
        result.setCoverageBenefitPercentage(benefitPercent);
        result.setRelationship(BeneficiaryRelationshipType.CHILD);
        result.setPerson(person);
        return result;
    }

}
