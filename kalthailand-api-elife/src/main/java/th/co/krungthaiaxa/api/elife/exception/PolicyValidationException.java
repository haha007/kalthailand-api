package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BeanValidationExceptionIfc;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

import javax.validation.ConstraintViolation;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * I don't like the old code because we have to write every exception for each field. That's exhausted!
 * We should make it works with BeanValidator based on Annotation!
 */
public class PolicyValidationException extends ElifeException implements BeanValidationExceptionIfc {
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_BEAN_VALIDATION;
    private static final Object ERROR_TARGET = null;
    private static final Set<ConstraintViolation<Object>> VIOLATIONS = Collections.EMPTY_SET;

    public static PolicyValidationException product10ECExpected = new PolicyValidationException("Product 10 EC is expected to do validation.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException productIFineExpected = new PolicyValidationException("Product iFine is expected to do validation.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException productIGenExpected = new PolicyValidationException("Product iGen is expected to do validation.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException productIProtectExpected = new PolicyValidationException("Product iProtect is expected to do validation.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException emptyQuote = new PolicyValidationException("Policy needs a quote to be created.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException noneExistingQuote = new PolicyValidationException("The quote to create the policy from does not exist.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException startDateNotServerDate = new PolicyValidationException("The start date of the policy must be day of registration.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException noInsured = new PolicyValidationException("There is no insured.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException noMainInsured = new PolicyValidationException("Insured person is not flagged as main insured.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredWithNoMainInsured = new PolicyValidationException("Insured has no main insured indicator.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredWithNoType = new PolicyValidationException("Insured has no insured type.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredMoreThanOne = new PolicyValidationException("Cannot have more than one insured.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredNoFatca = new PolicyValidationException("Insured has no fatca status.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredFatcaInvalid1 = new PolicyValidationException("Fatca 'born in USA' is not answered.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredFatcaInvalid2 = new PolicyValidationException("Fatca 'pay tax in USA' is not answered.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredFatcaInvalid3 = new PolicyValidationException("Fatca 'PR for tax of USA' is not answered.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException insuredFatcaInvalid4 = new PolicyValidationException("Fatca 'PR of USA' is not answered.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException insuredWithNoPerson = new PolicyValidationException("Insured has no person data.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException personWithNoGivenName = new PolicyValidationException("Insured person has no given name.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException personWithNoSurname = new PolicyValidationException("Insured person has no surname.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException personWithNoTitle = new PolicyValidationException("Insured person has no title.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException personWithInvalidThaiIdNumber = new PolicyValidationException("Insured person has an invalid Thai ID number.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException mainInsuredWithNoGenderCode = new PolicyValidationException("Gender code of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoMaritalStatus = new PolicyValidationException("Marital status of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoWeight = new PolicyValidationException("Weight of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoHeight = new PolicyValidationException("Height of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoWeightChange = new PolicyValidationException("Weight change flag of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoWeightChangeReason = new PolicyValidationException("Weight change reason of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoDeclaredTax = new PolicyValidationException("Declared tax of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoDisableStatus = new PolicyValidationException("Disability or HIV status of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoHospitalizedStatus = new PolicyValidationException("Hospitalized status of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoDeniedOrCounterOfferStatus = new PolicyValidationException("Denied policy status of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoStartDate = new PolicyValidationException("Start date of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoEndDate = new PolicyValidationException("End date of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoProfessionId = new PolicyValidationException("Profession ID of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoProfessionName = new PolicyValidationException("Profession name of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoDOB = new PolicyValidationException("Date of birth of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoEmail = new PolicyValidationException("Email of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoGeographicalAddress = new PolicyValidationException("Address of main insured is empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithNoPhoneNumber = new PolicyValidationException("At least one phone number for main insured has to be provided.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException mainInsuredWithInvalidEmail = new PolicyValidationException("Main insured email is not valid.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException beneficiariesNone = new PolicyValidationException("There must be at least one beneficiary.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException beneficiariesTooMany = new PolicyValidationException("There are too many beneficiaries.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException beneficiariesPercentSumNot100 = new PolicyValidationException("The sum of percent between beneficiaries is not 100.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException beneficiariesIdIqualToInsuredId = new PolicyValidationException("The beneficiary cannot be the insured.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException beneficiariesWithSameId = new PolicyValidationException("The beneficiaries must be unique.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException beneficiariesAgeAtSubscriptionEmpty = new PolicyValidationException("The beneficiaries age must be filled in.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException beneficiariesWithWrongIDNumber = new PolicyValidationException("At least one beneficiary has an invalid Thai ID number.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException addressWithNoDistrict = new PolicyValidationException("District of the address cannot be empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException addressWithNoPostCode = new PolicyValidationException("Post code of the address cannot be empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException addressWithNoStreetAddress1 = new PolicyValidationException("Street address (part 1) of the address cannot be empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException addressWithNoStreetAddress2 = new PolicyValidationException("Street address (part 2) of the address cannot be empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException addressWithNoSubCountry = new PolicyValidationException("Sub Country of the address cannot be empty.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException addressWithNoSubDistrict = new PolicyValidationException("Sub District of the address cannot be empty.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException coverageExpected = new PolicyValidationException("Policy has no coverage.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException coverageMoreThanOne = new PolicyValidationException("Policy has more than one coverage.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException premiumnsDataNone = new PolicyValidationException("Policy has no premium data.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException premiumnsDataNoSumInsured = new PolicyValidationException("Policy has no sum insured.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException premiumnsSumInsuredNoCurrency = new PolicyValidationException("Policy has a sum insured without currency.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException premiumnsSumInsuredNoAmount = new PolicyValidationException("Policy has a sum insured without amount.", ERROR_TARGET, VIOLATIONS);

    public static PolicyValidationException premiumnsCalculatedAmountEmpty = new PolicyValidationException("Policy is missing premium calculations.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException premiumnsCalculatedAmountNoCurrency = new PolicyValidationException("Policy has a calculated premium with no currency.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException premiumnsCalculatedAmountNoDate = new PolicyValidationException("Policy has a calculated premium with no date.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException premiumnsCalculatedAmountDateInThePast = new PolicyValidationException("Policy has a calculated premium with a date in the past.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException premiumnsCalculatedAmountNoAmount = new PolicyValidationException("Policy has a calculated premium with no amount.", ERROR_TARGET, VIOLATIONS);
    public static Function<String, PolicyValidationException> premiumsCalculatedAmountInvalidDate = msg -> new PolicyValidationException("Policy has a calculated premium date [" + msg + "] which is invalid date.", ERROR_TARGET, VIOLATIONS);
    public static Function<Integer, PolicyValidationException> premiumsCalculatedNotEnoughCoverageYears = years -> new PolicyValidationException("Policy has calculated premiums for a number of years not compatible with contract duration: " + years + " years.", ERROR_TARGET,
            VIOLATIONS);

    public static PolicyValidationException noPolicyNumberAccessible = new PolicyValidationException("Policy numbers are not available.", ERROR_TARGET, VIOLATIONS);
    public static PolicyValidationException noPolicyNumberAvailable = new PolicyValidationException("No more Policy numbers are available.", ERROR_TARGET, VIOLATIONS);

    private final Object errorTarget;

    private final Set<ConstraintViolation<Object>> violations;

    public PolicyValidationException(String message, Object errorTarget, Set<ConstraintViolation<Object>> violations) {
        super(message);
        this.errorTarget = errorTarget;
        this.violations = violations;
    }

    public Object getErrorTarget() {
        return errorTarget;
    }

    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
