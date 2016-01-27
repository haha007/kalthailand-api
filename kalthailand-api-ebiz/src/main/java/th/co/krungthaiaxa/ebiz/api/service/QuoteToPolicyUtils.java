package th.co.krungthaiaxa.ebiz.api.service;

import org.apache.commons.lang3.SerializationUtils;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.Insured;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;

public class QuoteToPolicyUtils {
    public static void getPolicyFromQuote(Policy policy, Quote quote) throws PolicyValidationException {
        checkInsured(quote);
        checkPerson(quote);

        policy.setQuoteFunctionalId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        quote.getCoverages().stream().forEach(coverage -> policy.addCoverage(SerializationUtils.clone(coverage)));
        quote.getInsureds().stream().forEach(insured -> policy.addInsured(SerializationUtils.clone(insured)));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
    }

    private static void checkPerson(Quote quote) throws PolicyValidationException {
        if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson() == null)) {
            throw PolicyValidationException.insuredWithNoPerson;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getBirthDate() == null)) {
            throw PolicyValidationException.personWithNoDOB;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getEmail() == null)) {
            throw PolicyValidationException.personWithNoEmail;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getGenderCode() == null)) {
            throw PolicyValidationException.personWithNoGenderCode;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getGeographicalAddress() == null)) {
            throw PolicyValidationException.personWithNoGeographicalAddress;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getGivenName() == null)) {
            throw PolicyValidationException.personWithNoGivenName;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getHeightInCm() == null)) {
            throw PolicyValidationException.personWithNoHeight;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getHomePhoneNumber() == null)) {
            throw PolicyValidationException.personWithNoHomePhoneNumber;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getMaritalStatus() == null)) {
            throw PolicyValidationException.personWithNoMaritalStatus;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getMiddleName() == null)) {
            throw PolicyValidationException.personWithNoMiddleName;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getMobilePhoneNumber() == null)) {
            throw PolicyValidationException.personWithNoMobilePhoneNumber;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getSurName() == null)) {
            throw PolicyValidationException.personWithNoSurname;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getTitle() == null)) {
            throw PolicyValidationException.personWithNoTitle;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getPerson().getWeightInKg() == null)) {
            throw PolicyValidationException.personWithNoWeight;
        }
    }

    private static void checkInsured(Quote quote) throws PolicyValidationException {
        if (quote.getInsureds() == null || quote.getInsureds().size() == 0) {
            throw PolicyValidationException.noInsured;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getType() == null)) {
            throw PolicyValidationException.insuredWithNoType;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getDisableOrImmunoDeficient() == null)) {
            throw PolicyValidationException.insuredWithNoDisableStatus;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getHospitalizedInLast6Months() == null)) {
            throw PolicyValidationException.insuredWithNoHospitalizedStatus;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getMainInsuredIndicator() == null)) {
            throw PolicyValidationException.insuredWithNoMainInsured;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getAgeAtSubscription() == null)) {
            throw PolicyValidationException.insuredWithNoAge;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getDeclaredTaxPercentAtSubscription() == null)) {
            throw PolicyValidationException.insuredWithNoDeclaredTax;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getStartDate() == null)) {
            throw PolicyValidationException.insuredWithNoStartDate;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getEndDate() == null)) {
            throw PolicyValidationException.insuredWithNoEndDate;
        } else if (quote.getInsureds().stream().anyMatch(insured -> insured.getProfessionName() == null)) {
            throw PolicyValidationException.insuredWithNoProfessionName;
        } else if (!quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().isPresent()) {
            throw PolicyValidationException.noMainInsured;
        } else if (quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).count() != 1) {
            throw PolicyValidationException.moreThanOneMainInsured;
        }
    }
}
