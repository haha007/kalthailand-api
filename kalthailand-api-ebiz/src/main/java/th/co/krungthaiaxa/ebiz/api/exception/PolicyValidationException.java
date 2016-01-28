package th.co.krungthaiaxa.ebiz.api.exception;

public class PolicyValidationException extends Exception {
    public static PolicyValidationException emptyQuote = new PolicyValidationException("Policy needs a quote to be created.");
    public static PolicyValidationException noneExistingQuote = new PolicyValidationException("The quote to create the policy from does not exist.");

    public static PolicyValidationException noInsured = new PolicyValidationException("There is no insured for the policy.");
    public static PolicyValidationException noMainInsured = new PolicyValidationException("There is no main insured person for the policy.");
    public static PolicyValidationException moreThanOneMainInsured = new PolicyValidationException("There is more than one main insured person for the policy.");
    public static PolicyValidationException insuredWithNoMainInsured = new PolicyValidationException("At least one insured has no main insured indicator.");
    public static PolicyValidationException insuredWithNoType = new PolicyValidationException("At least one insured has no insured type.");

    public static PolicyValidationException insuredWithNoPerson = new PolicyValidationException("At least one insured has no person.");
    public static PolicyValidationException personWithNoGivenName = new PolicyValidationException("At least one insured has no given name.");
    public static PolicyValidationException personWithNoMiddleName = new PolicyValidationException("At least one insured has no middle name.");
    public static PolicyValidationException personWithNoSurname = new PolicyValidationException("At least one insured has no surname.");
    public static PolicyValidationException personWithNoTitle = new PolicyValidationException("At least one insured has no title.");

    public static PolicyValidationException mainInsuredWithNoGenderCode = new PolicyValidationException("Gender code of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoMaritalStatus = new PolicyValidationException("Marital status of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoWeight = new PolicyValidationException("Height of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoHeight = new PolicyValidationException("Weight of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoDeclaredTax = new PolicyValidationException("Declared tax of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoDisableStatus = new PolicyValidationException("Disability or HIV status of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoHospitalizedStatus = new PolicyValidationException("Hospitalized status of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoStartDate = new PolicyValidationException("Start date of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoEndDate = new PolicyValidationException("End date of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoProfessionName = new PolicyValidationException("Profession name of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoAge = new PolicyValidationException("Age of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoDOB = new PolicyValidationException("Date of birth of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoEmail = new PolicyValidationException("Email of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoGeographicalAddress = new PolicyValidationException("Address of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoHomePhoneNumber = new PolicyValidationException("Home phone number of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoMobilePhoneNumber = new PolicyValidationException("Mobile phone number of main insured is empty.");

    public static PolicyValidationException beneficiariesNone = new PolicyValidationException("There must be at least one beneficiary.");
    public static PolicyValidationException beneficiariesTooMany = new PolicyValidationException("There are too many beneficiaries.");

    private PolicyValidationException(String message) {
        super(message);
    }
}
