package th.co.krungthaiaxa.ebiz.api.service;


import org.junit.Ignore;
import org.junit.Test;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.GenderCode;
import th.co.krungthaiaxa.ebiz.api.model.enums.InsuredType;
import th.co.krungthaiaxa.ebiz.api.model.enums.MaritalStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.ebiz.api.service.QuoteToPolicyUtils.getPolicyFromQuote;

public class QuoteToPolicyUtilsTest {

    @Test
    public void should_return_error_when_create_policy_with_no_main_insured() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setMainInsuredIndicator(FALSE);
        quote.getInsureds().get(1).setMainInsuredIndicator(FALSE);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noMainInsured.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_more_than_one_main_insured() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setMainInsuredIndicator(TRUE);
        quote.getInsureds().get(1).setMainInsuredIndicator(TRUE);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(moreThanOneMainInsured.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_insured_type() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setType(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoType.getMessage());
    }

    @Test
    @Ignore
    public void should_return_error_when_create_policy_with_no_insured() throws Exception {
        final Quote quote = quote();
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noInsured.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_start_date() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setStartDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoStartDate.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_end_date() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setEndDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoEndDate.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_age() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setAgeAtSubscription(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoAge.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_profession_name() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setProfessionName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoProfessionName.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_declaredTax() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setDeclaredTaxPercentAtSubscription(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoDeclaredTax.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_disable_status() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setDisableOrImmunoDeficient(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoDisableStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_hospitalized_status() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setHospitalizedInLast6Months(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoHospitalizedStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_person() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setPerson(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoPerson.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_dob() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).getPerson().setBirthDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(personWithNoDOB.getMessage());
    }

    @Test
    @Ignore
    public void should_copy_quote_details_into_policy() throws Exception {
        final Quote quote = quote();
        quote.getInsureds().get(0).setMainInsuredIndicator(TRUE);

        Policy policy = new Policy();
        getPolicyFromQuote(policy, quote);
        assertThat(policy.getQuoteFunctionalId()).isEqualTo(quote.getQuoteId());
        assertThat(policy.getCommonData()).isEqualToComparingFieldByField(quote.getCommonData());
        assertThat(policy.getPremiumsData()).isEqualTo(quote.getPremiumsData());
        assertThat(policy.getCoverages()).isEqualTo(quote.getCoverages());
        assertThat(policy.getInsureds()).isEqualTo(quote.getInsureds());
    }

    private Quote quote() {
        Insured insured1 = getInsured(25, 5, TRUE);
        Insured insured2 = getInsured(35, 25, FALSE);
        Quote quote = new Quote();
        quote.addInsured(insured1);
        quote.addInsured(insured2);
//        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(PeriodicityCode.EVERY_YEAR);
//
//        Amount amount = new Amount();
//        amount.setCurrencyCode("THB");
//        amount.setValue(1000000.0);
//        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);
        return quote;
    }

    private Insured getInsured(int ageAtSubscription, int declaredTaxPercentAtSubscription, boolean mainInsured) {
        Insured insured = new Insured();
        insured.setAgeAtSubscription(ageAtSubscription);
        insured.setDeclaredTaxPercentAtSubscription(declaredTaxPercentAtSubscription);
        insured.setDisableOrImmunoDeficient(FALSE);
        insured.setEndDate(LocalDate.now().plus(10, ChronoUnit.YEARS));
        insured.setHospitalizedInLast6Months(FALSE);
        insured.setMainInsuredIndicator(mainInsured);
        insured.setProfessionName("Something");
        insured.setStartDate(LocalDate.now());
        insured.setType(InsuredType.Insured);

        Person person = new Person();
        person.setBirthDate(LocalDate.now().minus(ageAtSubscription, ChronoUnit.YEARS));
        person.setEmail("something@something.com");
        person.setGenderCode(GenderCode.FEMALE);
        person.setGeographicalAddress(new GeographicalAddress());
        person.setGivenName("Someone");
        person.setHeightInCm(100);
        person.setHomePhoneNumber(new PhoneNumber());
        person.setMaritalStatus(MaritalStatus.MARRIED);
        person.setMiddleName("Else");
        person.setMobilePhoneNumber(new PhoneNumber());
        person.setSurName("Surname");
        person.setTitle("M");
        person.setWeightInKg(100);
        person.setWorkPhoneNumber(new PhoneNumber());
        insured.setPerson(person);
        return insured;
    }

}
