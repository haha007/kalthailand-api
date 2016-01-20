package th.co.krungthaiaxa.ebiz.api.products;

import org.junit.Test;
import th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException.sumInsuredTooHighException;
import static th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException.sumInsuredTooLowException;
import static th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode.*;
import static th.co.krungthaiaxa.ebiz.api.products.Product10EC.calculateQuote;

public class Product10ECTest {
    @Test
    public void should_return_error_when_sum_insured_is_more_than_1_million_baht() throws Exception {
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(46, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(51, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(56, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(61, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(66, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_MONTH);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_QUARTER);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_HALF_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(46, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(51, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(56, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(61, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(66, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);
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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);
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
        Quote quote = getQuoteWithAgeAndPeriodicity(35, EVERY_YEAR);
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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);
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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);
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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);

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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);
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
        Quote quote = getQuoteWithAgeAndPeriodicity(25, EVERY_YEAR);
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

    private Quote getQuoteWithAgeAndPeriodicity(int age, PeriodicityCode periodicityCode) {
        Periodicity periodicity = new Periodicity();
        periodicity.setCode(periodicityCode);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        PremiumsDataLifeInsurance premiumsData = new PremiumsDataLifeInsurance();
        premiumsData.setFinancialScheduler(financialScheduler);

        Insured insured = new Insured();
        insured.setMainInsuredIndicator(true);
        insured.setFatca(new Fatca());
        insured.setPerson(new Person());
        insured.setAgeAtSubscription(age);

        Quote quote = new Quote();
        quote.setCommonData(new QuoteCommonData());
        quote.setPremiumsData(premiumsData);
        quote.addInsured(insured);

        return quote;
    }
}
