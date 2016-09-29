package th.co.krungthaiaxa.api.elife.factory;

import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;

/**
 * @author khoi.tran on 9/29/16.
 */
public class RegistrationFactory {
    /**
     * ID of Santi???
     */
    public final static String DEFAULT_MAIN_INSURED = "3841200364454";
    public final static String DEFAULT_BENEFICIARY_01 = "3101202780273";
    public final static String DEFAULT_BENEFICIARY_02 = "3120300153833";

    public static Registration constructThaiId(String registrationId) {
        Registration registration = new Registration();
        registration.setId(registrationId);
        registration.setTypeName(RegistrationTypeName.THAI_ID_NUMBER);
        return registration;
    }
}
