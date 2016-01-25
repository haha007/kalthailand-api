package th.co.krungthaiaxa.ebiz.api.exception;

public class PolicyValidationException extends Exception {
    public static PolicyValidationException policyCantBeCreatedFromEmptyQuoteException = new PolicyValidationException("Policy needs a quote to be created.");
    public static PolicyValidationException policyCantBeCreatedFromNoneExistingQuoteException = new PolicyValidationException("The quote to create the policy from does not exist.");

    private PolicyValidationException(String message) {
        super(message);
    }
}
