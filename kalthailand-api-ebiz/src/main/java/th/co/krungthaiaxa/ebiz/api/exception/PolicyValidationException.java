package th.co.krungthaiaxa.ebiz.api.exception;

public class PolicyValidationException extends Exception {
    public static PolicyValidationException emptyQuote = new PolicyValidationException("Policy needs a quote to be created.");
    public static PolicyValidationException noneExistingQuote = new PolicyValidationException("The quote to create the policy from does not exist.");

    public static PolicyValidationException noInsured = new PolicyValidationException("There is no insured for the policy.");
    public static PolicyValidationException noMainInsured = new PolicyValidationException("There is no main insured person for the policy.");
    public static PolicyValidationException moreThanOneMainInsured = new PolicyValidationException("There is more than one main insured person for the policy.");
    public static PolicyValidationException insuredWithNoMainInsured = new PolicyValidationException("At least one insured has no main insured indicator.");
    public static PolicyValidationException insuredWithNoType = new PolicyValidationException("At least one insured has no insured type.");
    public static PolicyValidationException insuredWithNoStartDate = new PolicyValidationException("At least one insured has no start date contract.");
    public static PolicyValidationException insuredWithNoEndDate = new PolicyValidationException("At least one insured has no end date contract.");
    public static PolicyValidationException insuredWithNoProfessionName = new PolicyValidationException("At least one insured has no professio name.");
    public static PolicyValidationException insuredWithNoAge = new PolicyValidationException("At least one insured has no age.");
    public static PolicyValidationException insuredWithNoDeclaredTax = new PolicyValidationException("At least one insured has not declared his tax level.");
    public static PolicyValidationException insuredWithNoDisableStatus = new PolicyValidationException("At least one insured has not responded to his diability or HIV status.");
    public static PolicyValidationException insuredWithNoHospitalizedStatus = new PolicyValidationException("At least one insured has not responded to his hospitalization in the last 6 months status.");

    public static PolicyValidationException insuredWithNoPerson = new PolicyValidationException("At least one insured has no person.");
    public static PolicyValidationException personWithNoDOB = new PolicyValidationException("At least one insured has no Date Of Birth.");
    public static PolicyValidationException personWithNoEmail = new PolicyValidationException("At least one insured has no Email.");
    public static PolicyValidationException personWithNoGenderCode = new PolicyValidationException("At least one insured has no gender.");
    public static PolicyValidationException personWithNoGeographicalAddress = new PolicyValidationException("At least one insured has no geographical address.");
    public static PolicyValidationException personWithNoGivenName = new PolicyValidationException("At least one insured has no given name.");
    public static PolicyValidationException personWithNoHeight = new PolicyValidationException("At least one insured has no height.");
    public static PolicyValidationException personWithNoHomePhoneNumber = new PolicyValidationException("At least one insured has no home phone number.");
    public static PolicyValidationException personWithNoMaritalStatus = new PolicyValidationException("At least one insured has no marital status.");
    public static PolicyValidationException personWithNoMiddleName = new PolicyValidationException("At least one insured has no middle name.");
    public static PolicyValidationException personWithNoMobilePhoneNumber = new PolicyValidationException("At least one insured has no mobile phone number.");
    public static PolicyValidationException personWithNoSurname = new PolicyValidationException("At least one insured has no surname.");
    public static PolicyValidationException personWithNoTitle = new PolicyValidationException("At least one insured has no title.");
    public static PolicyValidationException personWithNoWeight = new PolicyValidationException("At least one insured has no weight.");

    private PolicyValidationException(String message) {
        super(message);
    }
}
