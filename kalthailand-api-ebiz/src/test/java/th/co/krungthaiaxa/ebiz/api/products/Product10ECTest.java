package th.co.krungthaiaxa.ebiz.api.products;

import org.junit.Test;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Product10ECTest {
    @Test
    public void should_calculate_sum_insured_from_premium_with_rate_308() {
        Quote quote = getQuoteWithAgeAndPeriodicity(25);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(308000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = Product10EC.calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_rate_306() {
        Quote quote = getQuoteWithAgeAndPeriodicity(46);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(306000.0);
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount);

        Amount result = Product10EC.calculateQuote(quote).getPremiumsData().getLifeInsuranceSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_rate_308() {
        Quote quote = getQuoteWithAgeAndPeriodicity(25);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = Product10EC.calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(308000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_rate_306() {
        Quote quote = getQuoteWithAgeAndPeriodicity(46);

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        Amount result = Product10EC.calculateQuote(quote).getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(306000.0);
    }

    @Test
    public void should_calculate_minimum_yearly_returns_from_sum_insured() {
        Quote quote = getQuoteWithAgeAndPeriodicity(25);
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = Product10EC.calculateQuote(quote);
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
    public void should_calculate_average_yearly_returns_from_sum_insured() {
        Quote quote = getQuoteWithAgeAndPeriodicity(25);
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = Product10EC.calculateQuote(quote);
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
    public void should_calculate_maximum_yearly_returns_from_sum_insured() {
        Quote quote = getQuoteWithAgeAndPeriodicity(25);
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();

        Amount amount = new Amount();
        amount.setCurrencyCode("THB");
        amount.setValue(1000000.0);
        quote.getPremiumsData().setLifeInsuranceSumInsured(amount);

        quote = Product10EC.calculateQuote(quote);
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

    private Quote getQuoteWithAgeAndPeriodicity(int age) {
        Periodicity periodicity = new Periodicity();
        periodicity.setCode(PeriodicityCode.EVERY_YEAR);

        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(periodicity);

        PremiumsData premiumsData = new PremiumsData();
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
