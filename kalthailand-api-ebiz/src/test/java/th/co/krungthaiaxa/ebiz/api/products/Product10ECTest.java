package th.co.krungthaiaxa.ebiz.api.products;

import org.junit.Test;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException.*;
import static th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode.*;
import static th.co.krungthaiaxa.ebiz.api.products.Product10EC.*;

public class Product10ECTest {

    @Test
    public void should_throw_error_if_age_at_subscription_is_less_than_20() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(19, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        assertThatThrownBy(() -> calculateQuote(quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(ageIsTooLowException.getMessage());
    }

    @Test
    public void should_throw_error_if_age_at_subscription_is_more_than_70() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(71, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        assertThatThrownBy(() -> calculateQuote(quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(ageIsTooHighException.getMessage());
    }

    @Test
    public void should_return_error_when_sum_insured_is_more_than_1_million_baht() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000001.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        assertThatThrownBy(() -> calculateQuote(quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(sumInsuredTooHighException.getMessage());
    }

    @Test
    public void should_return_error_when_sum_insured_is_less_than_200_thousand_baht() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(199999.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        assertThatThrownBy(() -> calculateQuote(quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(sumInsuredTooLowException.getMessage());
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_25() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(308000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_46() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(46, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(306000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_51() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(51, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(304000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_56() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(56, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(301000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_61() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(61, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(300000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_66() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(66, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(298000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_25() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(308000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_and_age_25() throws Exception {
        Quote quote = quote(EVERY_MONTH, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(27720.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarter_periodicity_and_age_25() throws Exception {
        Quote quote = quote(EVERY_QUARTER, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(83160.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_and_age_25() throws Exception {
        Quote quote = quote(EVERY_HALF_YEAR, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(160160.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_46() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(46, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(306000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_51() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(51, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(304000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_56() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(56, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(301000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_61() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(61, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(300000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_66() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(66, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(298000.0);
    }

    @Test
    public void should_calculate_cash_back_returns_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceYearlyCashBacks();
        assertThat(result).hasSize(10);

        assertThat(result.get(0).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(1).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(2).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(3).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(4).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(5).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(6).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(7).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(8).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(9).getCurrencyCode()).isEqualTo("THB");

        assertThat(result.get(0).getDate()).isEqualTo(endDate.minus(9, ChronoUnit.YEARS));
        assertThat(result.get(1).getDate()).isEqualTo(endDate.minus(8, ChronoUnit.YEARS));
        assertThat(result.get(2).getDate()).isEqualTo(endDate.minus(7, ChronoUnit.YEARS));
        assertThat(result.get(3).getDate()).isEqualTo(endDate.minus(6, ChronoUnit.YEARS));
        assertThat(result.get(4).getDate()).isEqualTo(endDate.minus(5, ChronoUnit.YEARS));
        assertThat(result.get(5).getDate()).isEqualTo(endDate.minus(4, ChronoUnit.YEARS));
        assertThat(result.get(6).getDate()).isEqualTo(endDate.minus(3, ChronoUnit.YEARS));
        assertThat(result.get(7).getDate()).isEqualTo(endDate.minus(2, ChronoUnit.YEARS));
        assertThat(result.get(8).getDate()).isEqualTo(endDate.minus(1, ChronoUnit.YEARS));
        assertThat(result.get(9).getDate()).isEqualTo(endDate);

        assertThat(result.get(0).getValue()).isEqualTo(20000.0);
        assertThat(result.get(1).getValue()).isEqualTo(20000.0);
        assertThat(result.get(2).getValue()).isEqualTo(20000.0);
        assertThat(result.get(3).getValue()).isEqualTo(20000.0);
        assertThat(result.get(4).getValue()).isEqualTo(20000.0);
        assertThat(result.get(5).getValue()).isEqualTo(20000.0);
        assertThat(result.get(6).getValue()).isEqualTo(20000.0);
        assertThat(result.get(7).getValue()).isEqualTo(20000.0);
        assertThat(result.get(8).getValue()).isEqualTo(20000.0);
        assertThat(result.get(9).getValue()).isEqualTo(1820000.0);
    }

    @Test
    public void should_calculate_minimum_yearly_returns_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceMinimumYearlyReturns();
        assertThat(result).hasSize(10);

        assertThat(result.get(0).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(1).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(2).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(3).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(4).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(5).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(6).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(7).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(8).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(9).getCurrencyCode()).isEqualTo("THB");

        assertThat(result.get(0).getDate()).isEqualTo(endDate.minus(9, ChronoUnit.YEARS));
        assertThat(result.get(1).getDate()).isEqualTo(endDate.minus(8, ChronoUnit.YEARS));
        assertThat(result.get(2).getDate()).isEqualTo(endDate.minus(7, ChronoUnit.YEARS));
        assertThat(result.get(3).getDate()).isEqualTo(endDate.minus(6, ChronoUnit.YEARS));
        assertThat(result.get(4).getDate()).isEqualTo(endDate.minus(5, ChronoUnit.YEARS));
        assertThat(result.get(5).getDate()).isEqualTo(endDate.minus(4, ChronoUnit.YEARS));
        assertThat(result.get(6).getDate()).isEqualTo(endDate.minus(3, ChronoUnit.YEARS));
        assertThat(result.get(7).getDate()).isEqualTo(endDate.minus(2, ChronoUnit.YEARS));
        assertThat(result.get(8).getDate()).isEqualTo(endDate.minus(1, ChronoUnit.YEARS));
        assertThat(result.get(9).getDate()).isEqualTo(endDate);

        assertThat(result.get(0).getValue()).isEqualTo(20000.0);
        assertThat(result.get(1).getValue()).isEqualTo(40400.0);
        assertThat(result.get(2).getValue()).isEqualTo(61208.0);
        assertThat(result.get(3).getValue()).isEqualTo(82432.0);
        assertThat(result.get(4).getValue()).isEqualTo(104081.0);
        assertThat(result.get(5).getValue()).isEqualTo(126163.0);
        assertThat(result.get(6).getValue()).isEqualTo(148686.0);
        assertThat(result.get(7).getValue()).isEqualTo(171660.0);
        assertThat(result.get(8).getValue()).isEqualTo(195093.0);
        assertThat(result.get(9).getValue()).isEqualTo(2018995.0);
    }

    @Test
    public void should_calculate_minimum_yearly_returns_from_510_thousand_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(35, TRUE), beneficiary(100.0));
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(510000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceMinimumYearlyReturns();
        assertThat(result).hasSize(10);

        assertThat(result.get(0).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(1).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(2).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(3).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(4).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(5).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(6).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(7).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(8).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(9).getCurrencyCode()).isEqualTo("THB");

        assertThat(result.get(0).getDate()).isEqualTo(endDate.minus(9, ChronoUnit.YEARS));
        assertThat(result.get(1).getDate()).isEqualTo(endDate.minus(8, ChronoUnit.YEARS));
        assertThat(result.get(2).getDate()).isEqualTo(endDate.minus(7, ChronoUnit.YEARS));
        assertThat(result.get(3).getDate()).isEqualTo(endDate.minus(6, ChronoUnit.YEARS));
        assertThat(result.get(4).getDate()).isEqualTo(endDate.minus(5, ChronoUnit.YEARS));
        assertThat(result.get(5).getDate()).isEqualTo(endDate.minus(4, ChronoUnit.YEARS));
        assertThat(result.get(6).getDate()).isEqualTo(endDate.minus(3, ChronoUnit.YEARS));
        assertThat(result.get(7).getDate()).isEqualTo(endDate.minus(2, ChronoUnit.YEARS));
        assertThat(result.get(8).getDate()).isEqualTo(endDate.minus(1, ChronoUnit.YEARS));
        assertThat(result.get(9).getDate()).isEqualTo(endDate);

        assertThat(result.get(0).getValue()).isEqualTo(10200.0);
        assertThat(result.get(1).getValue()).isEqualTo(20604.0);
        assertThat(result.get(2).getValue()).isEqualTo(31216.0);
        assertThat(result.get(3).getValue()).isEqualTo(42040.0);
        assertThat(result.get(4).getValue()).isEqualTo(53081.0);
        assertThat(result.get(5).getValue()).isEqualTo(64343.0);
        assertThat(result.get(6).getValue()).isEqualTo(75830.0);
        assertThat(result.get(7).getValue()).isEqualTo(87547.0);
        assertThat(result.get(8).getValue()).isEqualTo(99498.0);
        assertThat(result.get(9).getValue()).isEqualTo(1029688.0);
    }

    @Test
    public void should_calculate_average_yearly_returns_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceAverageYearlyReturns();
        assertThat(result).hasSize(10);

        assertThat(result.get(0).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(1).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(2).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(3).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(4).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(5).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(6).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(7).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(8).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(9).getCurrencyCode()).isEqualTo("THB");

        assertThat(result.get(0).getDate()).isEqualTo(endDate.minus(9, ChronoUnit.YEARS));
        assertThat(result.get(1).getDate()).isEqualTo(endDate.minus(8, ChronoUnit.YEARS));
        assertThat(result.get(2).getDate()).isEqualTo(endDate.minus(7, ChronoUnit.YEARS));
        assertThat(result.get(3).getDate()).isEqualTo(endDate.minus(6, ChronoUnit.YEARS));
        assertThat(result.get(4).getDate()).isEqualTo(endDate.minus(5, ChronoUnit.YEARS));
        assertThat(result.get(5).getDate()).isEqualTo(endDate.minus(4, ChronoUnit.YEARS));
        assertThat(result.get(6).getDate()).isEqualTo(endDate.minus(3, ChronoUnit.YEARS));
        assertThat(result.get(7).getDate()).isEqualTo(endDate.minus(2, ChronoUnit.YEARS));
        assertThat(result.get(8).getDate()).isEqualTo(endDate.minus(1, ChronoUnit.YEARS));
        assertThat(result.get(9).getDate()).isEqualTo(endDate);

        assertThat(result.get(0).getValue()).isEqualTo(20000.0);
        assertThat(result.get(1).getValue()).isEqualTo(40800.0);
        assertThat(result.get(2).getValue()).isEqualTo(62432.0);
        assertThat(result.get(3).getValue()).isEqualTo(84929.0);
        assertThat(result.get(4).getValue()).isEqualTo(108326.0);
        assertThat(result.get(5).getValue()).isEqualTo(132659.0);
        assertThat(result.get(6).getValue()).isEqualTo(157965.0);
        assertThat(result.get(7).getValue()).isEqualTo(184284.0);
        assertThat(result.get(8).getValue()).isEqualTo(211655.0);
        assertThat(result.get(9).getValue()).isEqualTo(2040121.0);
    }

    @Test
    public void should_calculate_maximum_yearly_returns_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceMaximumYearlyReturns();
        assertThat(result).hasSize(10);

        assertThat(result.get(0).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(1).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(2).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(3).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(4).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(5).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(6).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(7).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(8).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(9).getCurrencyCode()).isEqualTo("THB");

        assertThat(result.get(0).getDate()).isEqualTo(endDate.minus(9, ChronoUnit.YEARS));
        assertThat(result.get(1).getDate()).isEqualTo(endDate.minus(8, ChronoUnit.YEARS));
        assertThat(result.get(2).getDate()).isEqualTo(endDate.minus(7, ChronoUnit.YEARS));
        assertThat(result.get(3).getDate()).isEqualTo(endDate.minus(6, ChronoUnit.YEARS));
        assertThat(result.get(4).getDate()).isEqualTo(endDate.minus(5, ChronoUnit.YEARS));
        assertThat(result.get(5).getDate()).isEqualTo(endDate.minus(4, ChronoUnit.YEARS));
        assertThat(result.get(6).getDate()).isEqualTo(endDate.minus(3, ChronoUnit.YEARS));
        assertThat(result.get(7).getDate()).isEqualTo(endDate.minus(2, ChronoUnit.YEARS));
        assertThat(result.get(8).getDate()).isEqualTo(endDate.minus(1, ChronoUnit.YEARS));
        assertThat(result.get(9).getDate()).isEqualTo(endDate);

        assertThat(result.get(0).getValue()).isEqualTo(20000.0);
        assertThat(result.get(1).getValue()).isEqualTo(40900.0);
        assertThat(result.get(2).getValue()).isEqualTo(62741.0);
        assertThat(result.get(3).getValue()).isEqualTo(85564.0);
        assertThat(result.get(4).getValue()).isEqualTo(109414.0);
        assertThat(result.get(5).getValue()).isEqualTo(134338.0);
        assertThat(result.get(6).getValue()).isEqualTo(160383.0);
        assertThat(result.get(7).getValue()).isEqualTo(187600.0);
        assertThat(result.get(8).getValue()).isEqualTo(216042.0);
        assertThat(result.get(9).getValue()).isEqualTo(2045764.0);
    }

    @Test
    public void should_not_calculate_mimimum_extra_dividende_returns_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceMinimumExtraDividende();
        assertThat(result).isEmpty();
    }

    @Test
    public void should_calculate_average_extra_dividende_returns_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceAverageExtraDividende();
        assertThat(result).hasSize(10);

        assertThat(result.get(0).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(1).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(2).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(3).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(4).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(5).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(6).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(7).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(8).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(9).getCurrencyCode()).isEqualTo("THB");

        assertThat(result.get(0).getDate()).isEqualTo(endDate.minus(9, ChronoUnit.YEARS));
        assertThat(result.get(1).getDate()).isEqualTo(endDate.minus(8, ChronoUnit.YEARS));
        assertThat(result.get(2).getDate()).isEqualTo(endDate.minus(7, ChronoUnit.YEARS));
        assertThat(result.get(3).getDate()).isEqualTo(endDate.minus(6, ChronoUnit.YEARS));
        assertThat(result.get(4).getDate()).isEqualTo(endDate.minus(5, ChronoUnit.YEARS));
        assertThat(result.get(5).getDate()).isEqualTo(endDate.minus(4, ChronoUnit.YEARS));
        assertThat(result.get(6).getDate()).isEqualTo(endDate.minus(3, ChronoUnit.YEARS));
        assertThat(result.get(7).getDate()).isEqualTo(endDate.minus(2, ChronoUnit.YEARS));
        assertThat(result.get(8).getDate()).isEqualTo(endDate.minus(1, ChronoUnit.YEARS));
        assertThat(result.get(9).getDate()).isEqualTo(endDate);

        assertThat(result.get(0).getValue()).isEqualTo(0.0);
        assertThat(result.get(1).getValue()).isEqualTo(0.0);
        assertThat(result.get(2).getValue()).isEqualTo(0.0);
        assertThat(result.get(3).getValue()).isEqualTo(0.0);
        assertThat(result.get(4).getValue()).isEqualTo(0.0);
        assertThat(result.get(5).getValue()).isEqualTo(0.0);
        assertThat(result.get(6).getValue()).isEqualTo(15000.0);
        assertThat(result.get(7).getValue()).isEqualTo(30600.0);
        assertThat(result.get(8).getValue()).isEqualTo(46824.0);
        assertThat(result.get(9).getValue()).isEqualTo(213697.0);
    }

    @Test
    public void should_calculate_maximum_extra_dividende_returns_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = calculateQuote(quote);
        List<DatedAmount> result = quote.getPremiumsData().getLifeInsuranceMaximumExtraDividende();
        assertThat(result).hasSize(10);

        assertThat(result.get(0).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(1).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(2).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(3).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(4).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(5).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(6).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(7).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(8).getCurrencyCode()).isEqualTo("THB");
        assertThat(result.get(9).getCurrencyCode()).isEqualTo("THB");

        assertThat(result.get(0).getDate()).isEqualTo(endDate.minus(9, ChronoUnit.YEARS));
        assertThat(result.get(1).getDate()).isEqualTo(endDate.minus(8, ChronoUnit.YEARS));
        assertThat(result.get(2).getDate()).isEqualTo(endDate.minus(7, ChronoUnit.YEARS));
        assertThat(result.get(3).getDate()).isEqualTo(endDate.minus(6, ChronoUnit.YEARS));
        assertThat(result.get(4).getDate()).isEqualTo(endDate.minus(5, ChronoUnit.YEARS));
        assertThat(result.get(5).getDate()).isEqualTo(endDate.minus(4, ChronoUnit.YEARS));
        assertThat(result.get(6).getDate()).isEqualTo(endDate.minus(3, ChronoUnit.YEARS));
        assertThat(result.get(7).getDate()).isEqualTo(endDate.minus(2, ChronoUnit.YEARS));
        assertThat(result.get(8).getDate()).isEqualTo(endDate.minus(1, ChronoUnit.YEARS));
        assertThat(result.get(9).getDate()).isEqualTo(endDate);

        assertThat(result.get(0).getValue()).isEqualTo(0.0);
        assertThat(result.get(1).getValue()).isEqualTo(0.0);
        assertThat(result.get(2).getValue()).isEqualTo(0.0);
        assertThat(result.get(3).getValue()).isEqualTo(0.0);
        assertThat(result.get(4).getValue()).isEqualTo(0.0);
        assertThat(result.get(5).getValue()).isEqualTo(0.0);
        assertThat(result.get(6).getValue()).isEqualTo(18000.0);
        assertThat(result.get(7).getValue()).isEqualTo(36810.0);
        assertThat(result.get(8).getValue()).isEqualTo(56466.0);
        assertThat(result.get(9).getValue()).isEqualTo(257007.0);
    }

    @Test
    public void should_return_error_when_create_policy_with_no_main_insured() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, FALSE), beneficiary(100.0));
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noMainInsured.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_more_than_one_insured() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.addInsured(insured(30, FALSE));
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredMoreThanOne.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_insured_type() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setType(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoType.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_no_insured() throws Exception {
        final Quote quote = quote(EVERY_YEAR, null, beneficiary(100.0));
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noInsured.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_person() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setPerson(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(insuredWithNoPerson.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_given_name() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setGivenName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(personWithNoGivenName.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_middle_name() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setMiddleName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(personWithNoMiddleName.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_surname() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setSurName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(personWithNoSurname.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_title() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setTitle(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(personWithNoTitle.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_gender() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setGenderCode(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoGenderCode.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_height() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setHeightInCm(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoHeight.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_marital_status() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setMaritalStatus(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoMaritalStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_weight() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setWeightInKg(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoWeight.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_declaredTax() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setDeclaredTaxPercentAtSubscription(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoDeclaredTax.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_disable_status() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setDisableOrImmunoDeficient(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoDisableStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_hospitalized_status() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setHospitalizedInLast6Months(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoHospitalizedStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_insured_with_no_start_date() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setStartDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoStartDate.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_end_date() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setEndDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoEndDate.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_age() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setAgeAtSubscription(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(ageIsEmptyException.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_profession_name() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).setProfessionName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoProfessionName.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_dob() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setBirthDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoDOB.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_email() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setEmail(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoEmail.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_invalid_emails() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setEmail("me");
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me.com");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@me");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@me.");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@.com");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@me.c");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@*.com");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me..@me.com");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me.@me.1a");
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithInvalidEmail.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_geo_address() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setGeographicalAddress(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoGeographicalAddress.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_country() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getGeographicalAddress().setCountry(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(addressWithNoCountry.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_district() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getGeographicalAddress().setDistrict(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(addressWithNoDistrict.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_postcode() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getGeographicalAddress().setPostCode(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(addressWithNoPostCode.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_street_address_1() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getGeographicalAddress().setStreetAddress1(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(addressWithNoStreetAddress1.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_street_address_2() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getGeographicalAddress().setStreetAddress2(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(addressWithNoStreetAddress2.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_sub_country() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getGeographicalAddress().setSubCountry(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(addressWithNoSubCountry.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_sub_district() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getGeographicalAddress().setSubdistrict(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(addressWithNoSubDistrict.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_phone() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setHomePhoneNumber(null);
        quote.getInsureds().get(0).getPerson().setMobilePhoneNumber(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(mainInsuredWithNoPhoneNumber.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_no_beneficiary() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE));
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(beneficiariesNone.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_too_many_beneficiaries() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(1.0), beneficiary(1.0),
                beneficiary(1.0), beneficiary(1.0), beneficiary(1.0), beneficiary(1.0), beneficiary(94.0));
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(beneficiariesTooMany.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_beneficiaries_for_percent_sum_different_than_100() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(1.0), beneficiary(1.0),
                beneficiary(1.0), beneficiary(1.0), beneficiary(94.0));
        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(beneficiariesPercentSumNot100.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_too_young() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(19, TRUE), beneficiary(100.0));

        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(ageIsTooLowException.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_too_old() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(71, TRUE), beneficiary(100.0));

        Policy policy = new Policy();
        assertThatThrownBy(() -> getPolicyFromQuote(policy, quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(ageIsTooHighException.getMessage());
    }

    @Test
    public void should_copy_quote_details_into_policy() throws Exception {
        final Quote quote = quote(EVERY_YEAR, insured(25, TRUE), beneficiary(100.0));

        Policy policy = new Policy();
        getPolicyFromQuote(policy, quote);
        assertThat(policy.getQuoteFunctionalId()).isEqualTo(quote.getQuoteId());
        assertThat(policy.getCommonData()).isEqualToComparingFieldByField(quote.getCommonData());
        assertThat(policy.getPremiumsData()).isEqualTo(quote.getPremiumsData());
        assertThat(policy.getCoverages()).isEqualTo(quote.getCoverages());
        assertThat(policy.getInsureds()).isEqualTo(quote.getInsureds());
    }

    private static Quote quote(PeriodicityCode periodicityCode, Insured insured, CoverageBeneficiary... beneficiaries) {
        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);

        Periodicity periodicity = new Periodicity();
        periodicity.setCode(periodicityCode);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        PremiumsDataLifeInsurance premiumsData = new PremiumsDataLifeInsurance();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setLifeInsuranceSumInsured(amount);

        CommonData commonData = new CommonData();
        commonData.setProductId(PRODUCT_10_EC_NAME);
        commonData.setProductName(PRODUCT_10_EC_NAME);

        Coverage coverage = new Coverage();
        coverage.setName(PRODUCT_10_EC_NAME);
        for (CoverageBeneficiary beneficiary : beneficiaries) {
            coverage.addBeneficiary(beneficiary);
        }

        Quote quote = new Quote();
        quote.setCommonData(commonData);
        quote.setPremiumsData(premiumsData);
        if (insured != null) {
            quote.addInsured(insured);
        }
        quote.addCoverage(coverage);
        return quote;
    }

    private static CoverageBeneficiary beneficiary(Double benefitPercent) {
        CoverageBeneficiary result = new CoverageBeneficiary();
        result.setCoverageBenefitPercentage(benefitPercent);
        result.setRelationship(BeneficiaryRelationshipType.CHILD);
        return result;
    }

    private static Insured insured(int ageAtSubscription, boolean mainInsured) {
        Insured insured = new Insured();
        insured.setAgeAtSubscription(ageAtSubscription);
        insured.setDeclaredTaxPercentAtSubscription(5);
        insured.setDisableOrImmunoDeficient(FALSE);
        insured.setEndDate(LocalDate.now().plus(10, ChronoUnit.YEARS));
        insured.setHospitalizedInLast6Months(FALSE);
        insured.setMainInsuredIndicator(mainInsured);
        insured.setProfessionName("Something");
        insured.setStartDate(LocalDate.now());
        insured.setType(InsuredType.Insured);

        GeographicalAddress geographicalAddress = new GeographicalAddress();
        geographicalAddress.setCountry("France");
        geographicalAddress.setDistrict("Something");
        geographicalAddress.setPostCode("75015");
        geographicalAddress.setStreetAddress1("rue du paradis");
        geographicalAddress.setStreetAddress2("apartement 2");
        geographicalAddress.setSubCountry("Ile de France");
        geographicalAddress.setSubdistrict("Paris");

        Person person = new Person();
        person.setBirthDate(LocalDate.now().minus(ageAtSubscription, ChronoUnit.YEARS));
        person.setEmail("something@something.com");
        person.setGenderCode(GenderCode.FEMALE);
        person.setGeographicalAddress(geographicalAddress);
        person.setGivenName("Someone");
        person.setHeightInCm(100);
        person.setHomePhoneNumber(new PhoneNumber());
        person.setMaritalStatus(MaritalStatus.MARRIED);
        person.setMiddleName("Else");
        person.setMobilePhoneNumber(new PhoneNumber());
        person.setSurName("Surname");
        person.setTitle("M");
        person.setWeightInKg(100);
        insured.setPerson(person);
        return insured;
    }
}
