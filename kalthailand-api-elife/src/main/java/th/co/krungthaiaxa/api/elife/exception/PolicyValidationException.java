package th.co.krungthaiaxa.api.elife.exception;

import java.util.function.Function;

public class PolicyValidationException extends ElifeException {

    public static PolicyValidationException product10ECExpected = new PolicyValidationException("Product 10 EC is expected to do validation.");
    public static PolicyValidationException productIFineExpected = new PolicyValidationException("Product iFine is expected to do validation.");
    public static PolicyValidationException productIGenExpected = new PolicyValidationException("Product iGen is expected to do validation.");

    public static PolicyValidationException emptyQuote = new PolicyValidationException("Policy needs a quote to be created.");
    public static PolicyValidationException noneExistingQuote = new PolicyValidationException("The quote to create the policy from does not exist.");

    public static PolicyValidationException startDateNotServerDate = new PolicyValidationException("The start date of the policy must be day of registration.");

    public static PolicyValidationException noInsured = new PolicyValidationException("There is no insured.");
    public static PolicyValidationException noMainInsured = new PolicyValidationException("Insured person is not flagged as main insured.");
    public static PolicyValidationException insuredWithNoMainInsured = new PolicyValidationException("Insured has no main insured indicator.");
    public static PolicyValidationException insuredWithNoType = new PolicyValidationException("Insured has no insured type.");
    public static PolicyValidationException insuredMoreThanOne = new PolicyValidationException("Cannot have more than one insured.");
    public static PolicyValidationException insuredNoFatca = new PolicyValidationException("Insured has no fatca status.");
    public static PolicyValidationException insuredFatcaInvalid1 = new PolicyValidationException("Fatca 'born in USA' is not answered.");
    public static PolicyValidationException insuredFatcaInvalid2 = new PolicyValidationException("Fatca 'pay tax in USA' is not answered.");
    public static PolicyValidationException insuredFatcaInvalid3 = new PolicyValidationException("Fatca 'PR for tax of USA' is not answered.");
    public static PolicyValidationException insuredFatcaInvalid4 = new PolicyValidationException("Fatca 'PR of USA' is not answered.");

    public static PolicyValidationException insuredWithNoPerson = new PolicyValidationException("Insured has no person data.");
    public static PolicyValidationException personWithNoGivenName = new PolicyValidationException("Insured person has no given name.");
    public static PolicyValidationException personWithNoSurname = new PolicyValidationException("Insured person has no surname.");
    public static PolicyValidationException personWithNoTitle = new PolicyValidationException("Insured person has no title.");
    public static PolicyValidationException personWithInvalidThaiIdNumber = new PolicyValidationException("Insured person has an invalid Thai ID number.");

    public static PolicyValidationException mainInsuredWithNoGenderCode = new PolicyValidationException("Gender code of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoMaritalStatus = new PolicyValidationException("Marital status of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoWeight = new PolicyValidationException("Weight of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoHeight = new PolicyValidationException("Height of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoWeightChange = new PolicyValidationException("Weight change flag of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoWeightChangeReason = new PolicyValidationException("Weight change reason of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoDeclaredTax = new PolicyValidationException("Declared tax of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoDisableStatus = new PolicyValidationException("Disability or HIV status of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoHospitalizedStatus = new PolicyValidationException("Hospitalized status of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoDeniedOrCounterOfferStatus = new PolicyValidationException("Denied policy status of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoStartDate = new PolicyValidationException("Start date of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoEndDate = new PolicyValidationException("End date of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoProfessionId = new PolicyValidationException("Profession ID of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoProfessionName = new PolicyValidationException("Profession name of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoDOB = new PolicyValidationException("Date of birth of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoEmail = new PolicyValidationException("Email of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoGeographicalAddress = new PolicyValidationException("Address of main insured is empty.");
    public static PolicyValidationException mainInsuredWithNoPhoneNumber = new PolicyValidationException("At least one phone number for main insured has to be provided.");
    public static PolicyValidationException mainInsuredWithInvalidEmail = new PolicyValidationException("Main insured email is not valid.");

    public static PolicyValidationException beneficiariesNone = new PolicyValidationException("There must be at least one beneficiary.");
    public static PolicyValidationException beneficiariesTooMany = new PolicyValidationException("There are too many beneficiaries.");
    public static PolicyValidationException beneficiariesPercentSumNot100 = new PolicyValidationException("The sum of percent between beneficiaries is not 100.");
    public static PolicyValidationException beneficiariesIdIqualToInsuredId = new PolicyValidationException("The beneficiary cannot be the insured.");
    public static PolicyValidationException beneficiariesWithSameId = new PolicyValidationException("The beneficiaries must be unique.");
    public static PolicyValidationException beneficiariesAgeAtSubscriptionEmpty = new PolicyValidationException("The beneficiaries age must be filled in.");
    public static PolicyValidationException beneficiariesWithWrongIDNumber = new PolicyValidationException("At least one beneficiary has an invalid Thai ID number.");

    public static PolicyValidationException addressWithNoDistrict = new PolicyValidationException("District of the address cannot be empty.");
    public static PolicyValidationException addressWithNoPostCode = new PolicyValidationException("Post code of the address cannot be empty.");
    public static PolicyValidationException addressWithNoStreetAddress1 = new PolicyValidationException("Street address (part 1) of the address cannot be empty.");
    public static PolicyValidationException addressWithNoStreetAddress2 = new PolicyValidationException("Street address (part 2) of the address cannot be empty.");
    public static PolicyValidationException addressWithNoSubCountry = new PolicyValidationException("Sub Country of the address cannot be empty.");
    public static PolicyValidationException addressWithNoSubDistrict = new PolicyValidationException("Sub District of the address cannot be empty.");

    public static PolicyValidationException coverageExpected = new PolicyValidationException("Policy has no coverage.");
    public static PolicyValidationException coverageMoreThanOne = new PolicyValidationException("Policy has more than one coverage.");

    public static PolicyValidationException premiumnsDataNone = new PolicyValidationException("Policy has no premium data.");
    public static PolicyValidationException premiumnsDataNoSumInsured = new PolicyValidationException("Policy has no sum insured.");
    public static PolicyValidationException premiumnsSumInsuredNoCurrency = new PolicyValidationException("Policy has a sum insured without currency.");
    public static PolicyValidationException premiumnsSumInsuredNoAmount = new PolicyValidationException("Policy has a sum insured without amount.");

    public static PolicyValidationException premiumnsCalculatedAmountEmpty = new PolicyValidationException("Policy is missing premium calculations.");
    public static PolicyValidationException premiumnsCalculatedAmountNoCurrency = new PolicyValidationException("Policy has a calculated premium with no currency.");
    public static PolicyValidationException premiumnsCalculatedAmountNoDate = new PolicyValidationException("Policy has a calculated premium with no date.");
    public static PolicyValidationException premiumnsCalculatedAmountDateInThePast = new PolicyValidationException("Policy has a calculated premium with a date in the past.");
    public static PolicyValidationException premiumnsCalculatedAmountNoAmount = new PolicyValidationException("Policy has a calculated premium with no amount.");
    public static Function<String, PolicyValidationException> premiumnsCalculatedAmountInvalidDate = msg -> new PolicyValidationException("Policy has a calculated premium date [" + msg + "] which is invalid date.");
    public static PolicyValidationException premiumnsCalculatedAmountNotFor10Years = new PolicyValidationException("Policy has calculated premiums for a number of years not compatible with contract duration.");

    public static PolicyValidationException noPolicyNumberAccessible = new PolicyValidationException("Policy numbers are not available.");
    public static PolicyValidationException noPolicyNumberAvailable = new PolicyValidationException("No more Policy numbers are available.");

    private PolicyValidationException(String message) {
        super(message);
    }
}
