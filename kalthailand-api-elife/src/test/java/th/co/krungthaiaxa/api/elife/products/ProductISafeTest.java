package th.co.krungthaiaxa.api.elife.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.DatedAmount;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.NOT_PROCESSED;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductISafeTest {
    @Inject
    private ProductISafe productISafe;

    @Test
    public void should_return_amount_for_30_yo_monthly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(30, EVERY_MONTH));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(2772.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(27720.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_30_yo_quarterly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(30, EVERY_QUARTER));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(8316.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(83160.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_30_yo_half_yearly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(30, EVERY_HALF_YEAR));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(16016.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(160160.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_30_yo_yearly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(30, EVERY_YEAR));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(30800.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(308000.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_48_yo_yearly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(48, EVERY_YEAR));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(30600.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(306000.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_53_yo_yearly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(53, EVERY_YEAR));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(30400.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(304000.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_58_yo_yearly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(58, EVERY_YEAR));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(30100.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(301000.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_63_yo_yearly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(63, EVERY_YEAR));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(30000.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(300000.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_return_amount_for_68_yo_yearly() throws Exception {
        ProductAmounts productAmounts = productISafe.getProductAmounts(TestUtil.productQuotation(68, EVERY_YEAR));
        assertThat(productAmounts.getMinPremium().getValue()).isEqualTo(29800.0);
        assertThat(productAmounts.getMaxPremium().getValue()).isEqualTo(298000.0);
        assertThat(productAmounts.getMinSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productAmounts.getMaxSumInsured().getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_throw_error_if_age_at_subscription_is_less_than_20() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        assertThatThrownBy(() -> productISafe.calculateQuote(quote, productQuotation(19, EVERY_YEAR, 1000000.0)))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(QuoteCalculationException.ageIsTooLowException.apply(20).getMessage());
    }

    @Test
    public void should_throw_error_if_age_at_subscription_is_more_than_70() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        assertThatThrownBy(() -> productISafe.calculateQuote(quote, productQuotation(71, EVERY_YEAR, 1000000.0)))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(QuoteCalculationException.ageIsTooHighException.apply(70).getMessage());
    }

    @Test
    public void should_return_error_when_sum_insured_is_over_the_limit() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        assertThatThrownBy(() -> productISafe.calculateQuote(quote, productQuotation(25, EVERY_YEAR, 1000001.0)))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(QuoteCalculationException.sumInsuredTooHighException.apply(SUM_INSURED_MAX).getMessage());
    }

    @Test
    public void should_return_error_when_sum_insured_is_below_the_limit() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        assertThatThrownBy(() -> productISafe.calculateQuote(quote, productQuotation(25, EVERY_YEAR, 99999.0)))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(QuoteCalculationException.sumInsuredTooLowException.apply(SUM_INSURED_MIN).getMessage());
    }

    @Test
    public void should_set_sum_insured_to_true_when_sum_insured_provided() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(25, EVERY_YEAR, 308000.0, true));
        assertThat(quote.getPremiumsData().getProductISafePremium().getSumInsuredOption()).isTrue();
    }

    @Test
    public void should_set_sum_insured_to_false_when_premium_provided() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(25, EVERY_YEAR, 308000.0, false));
        assertThat(quote.getPremiumsData().getProductISafePremium().getSumInsuredOption()).isFalse();
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_25() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(25, EVERY_YEAR, 308000.0, false));
        Amount result = quote.getPremiumsData().getProductISafePremium().getSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_46() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(46, EVERY_YEAR, 306000.0, false));
        Amount result = quote.getPremiumsData().getProductISafePremium().getSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_51() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(51, EVERY_YEAR, 304000.0, false));
        Amount result = quote.getPremiumsData().getProductISafePremium().getSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_56() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(56, EVERY_YEAR, 301000.0, false));
        Amount result = quote.getPremiumsData().getProductISafePremium().getSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_61() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(61, EVERY_YEAR, 300000.0, false));
        Amount result = quote.getPremiumsData().getProductISafePremium().getSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_sum_insured_from_premium_with_yearly_periodicity_and_age_66() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(66, EVERY_YEAR, 298000.0, false));
        Amount result = quote.getPremiumsData().getProductISafePremium().getSumInsured();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1000000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_25() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(308000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_monthly_periodicity_and_age_25() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(25, EVERY_MONTH, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(27720.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_quarter_periodicity_and_age_25() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(25, EVERY_QUARTER, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(83160.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_half_year_periodicity_and_age_25() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(25, EVERY_HALF_YEAR, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(160160.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_46() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(46, EVERY_YEAR, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(306000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_51() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(51, EVERY_YEAR, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(304000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_56() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(56, EVERY_YEAR, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(301000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_61() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(61, EVERY_YEAR, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(300000.0);
    }

    @Test
    public void should_calculate_premium_from_sum_insured_with_yearly_periodicity_and_age_66() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(66, EVERY_YEAR, 1000000.0));
        Amount result = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(298000.0);
    }

    @Test
    public void should_calculate_end_dates_and_start_date() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        quote.getInsureds().get(0).setStartDate(null);
        quote.getInsureds().get(0).setEndDate(null);
        quote.getPremiumsData().getFinancialScheduler().setEndDate(null);

        productISafe.calculateQuote(quote, TestUtil.productQuotation());

        LocalDate startDate = now(of(SHORT_IDS.get("VST")));
        assertThat(quote.getInsureds().get(0).getStartDate()).isEqualTo(startDate);
        assertThat(quote.getInsureds().get(0).getEndDate()).isEqualTo(startDate.plusYears(DURATION_COVERAGE_IN_YEAR));
        assertThat(quote.getPremiumsData().getFinancialScheduler().getEndDate()).isEqualTo(startDate.plusYears(DURATION_PAYMENT_IN_YEAR));
    }

    @Test
    public void should_calculate_tax_return_for_1_million_sum_insured_and_5_percent_tax_rate() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(EVERY_HALF_YEAR, 1000000.0, 5));
        Amount result = quote.getPremiumsData().getProductISafePremium().getYearlyTaxDeduction();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(16016.0);
    }

    @Test
    public void should_calculate_tax_return_for_1_thousand_sum_insured_and_5_percent_tax_rate() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(EVERY_QUARTER, 100000.0, 5));
        Amount result = quote.getPremiumsData().getProductISafePremium().getYearlyTaxDeduction();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(1663.0);
    }

    @Test
    public void should_calculate_tax_return_for_1_million_sum_insured_and_20_percent_tax_rate() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(EVERY_YEAR, 1000000.0, 20));
        Amount result = quote.getPremiumsData().getProductISafePremium().getYearlyTaxDeduction();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(61600.0);
    }

    @Test
    public void should_calculate_tax_return_for_1_thousand_sum_insured_and_20_percent_tax_rate() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(EVERY_MONTH, 100000.0, 20));
        Amount result = quote.getPremiumsData().getProductISafePremium().getYearlyTaxDeduction();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(6653.0);
    }

    @Test
    public void should_calculate_tax_return_for_1_million_sum_insured_and_70_percent_tax_rate() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(EVERY_YEAR, 1000000.0, 70));
        Amount result = quote.getPremiumsData().getProductISafePremium().getYearlyTaxDeduction();
        assertThat(result.getCurrencyCode()).isEqualTo("THB");
        assertThat(result.getValue()).isEqualTo(100000.0);
    }

    @Test
    public void should_calculate_yearly_cash_back_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getYearlyCashBacks();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

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
    public void should_calculate_minimum_end_of_contract_benefits_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getEndOfContractBenefitsMinimum();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));


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
    public void should_calculate_minimum_end_of_contract_benefits_from_510_thousand_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(35, EVERY_YEAR, 510000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getEndOfContractBenefitsMinimum();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

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
    public void should_calculate_average_end_of_contract_benefits_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getEndOfContractBenefitsAverage();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

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
    public void should_calculate_maximum_end_of_contract_benefits_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getEndOfContractBenefitsMaximum();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

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
    public void should_calculate_average_yearly_cashback_dividend_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getYearlyCashBacksAverageDividende();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

        assertThat(result.get(0).getValue()).isEqualTo(0.0);
        assertThat(result.get(1).getValue()).isEqualTo(0.0);
        assertThat(result.get(2).getValue()).isEqualTo(0.0);
        assertThat(result.get(3).getValue()).isEqualTo(0.0);
        assertThat(result.get(4).getValue()).isEqualTo(0.0);
        assertThat(result.get(5).getValue()).isEqualTo(0.0);
        assertThat(result.get(6).getValue()).isEqualTo(15000.0);
        assertThat(result.get(7).getValue()).isEqualTo(15000.0);
        assertThat(result.get(8).getValue()).isEqualTo(15000.0);
        assertThat(result.get(9).getValue()).isEqualTo(165000.0);
    }

    @Test
    public void should_calculate_maximum_yearly_cashback_dividend_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getYearlyCashBacksMaximumDividende();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

        assertThat(result.get(0).getValue()).isEqualTo(0.0);
        assertThat(result.get(1).getValue()).isEqualTo(0.0);
        assertThat(result.get(2).getValue()).isEqualTo(0.0);
        assertThat(result.get(3).getValue()).isEqualTo(0.0);
        assertThat(result.get(4).getValue()).isEqualTo(0.0);
        assertThat(result.get(5).getValue()).isEqualTo(0.0);
        assertThat(result.get(6).getValue()).isEqualTo(18000.0);
        assertThat(result.get(7).getValue()).isEqualTo(18000.0);
        assertThat(result.get(8).getValue()).isEqualTo(18000.0);
        assertThat(result.get(9).getValue()).isEqualTo(198000.0);
    }

    @Test
    public void should_calculate_average_yearly_cashback_benefit_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getYearlyCashBacksAverageBenefit();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

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
    public void should_calculate_maximum_yearly_cashback_benefit_from_1_million_sum_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());

        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        List<DatedAmount> result = quote.getPremiumsData().getProductISafePremium().getYearlyCashBacksMaximumBenefit();
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

        assertThat(result.get(0).getDate()).isEqualTo(startDate.plusYears(1));
        assertThat(result.get(1).getDate()).isEqualTo(startDate.plusYears(2));
        assertThat(result.get(2).getDate()).isEqualTo(startDate.plusYears(3));
        assertThat(result.get(3).getDate()).isEqualTo(startDate.plusYears(4));
        assertThat(result.get(4).getDate()).isEqualTo(startDate.plusYears(5));
        assertThat(result.get(5).getDate()).isEqualTo(startDate.plusYears(6));
        assertThat(result.get(6).getDate()).isEqualTo(startDate.plusYears(7));
        assertThat(result.get(7).getDate()).isEqualTo(startDate.plusYears(8));
        assertThat(result.get(8).getDate()).isEqualTo(startDate.plusYears(9));
        assertThat(result.get(9).getDate()).isEqualTo(startDate.plusYears(10));

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
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setMainInsuredIndicator(FALSE);
        assertThatThrownBy(() -> ProductUtils.checkInsured(quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.noMainInsured.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_more_than_one_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.addInsured(TestUtil.insured(30));
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.insuredMoreThanOne.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_insured_with_no_insured_type() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setType(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.insuredWithNoType.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_no_insured() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().remove(0);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.noInsured.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_insured_with_no_person() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setPerson(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.insuredWithNoPerson.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_given_name() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setGivenName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.personWithNoGivenName.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_surname() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setSurName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.personWithNoSurname.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_title() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setTitle(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.personWithNoTitle.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_gender() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setGenderCode(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoGenderCode.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_at_least_one_person_with_no_height() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getHealthStatus().setHeightInCm(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoHeight.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_marital_status() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setMaritalStatus(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoMaritalStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_weight() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getHealthStatus().setWeightInKg(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoWeight.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_weight_change() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getHealthStatus().setWeightChangeInLast6Months(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoWeightChange.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_weight_change_and_no_reason() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getHealthStatus().setWeightChangeInLast6Months(TRUE);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoWeightChangeReason.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_declaredTax() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setDeclaredTaxPercentAtSubscription(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoDeclaredTax.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_disable_status() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getHealthStatus().setDisableOrImmunoDeficient(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoDisableStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_hospitalized_status() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getHealthStatus().setHospitalizedInLast6Months(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoHospitalizedStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_denied_policy_status() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getHealthStatus().setDeniedOrCounterOffer(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoDeniedOrCounterOfferStatus.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_insured_with_no_start_date() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setStartDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoStartDate.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_insured_with_a_start_date_not_server_date() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setStartDate(now().minusDays(1));
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.startDateNotServerDate.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_end_date() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setEndDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoEndDate.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_age() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setAgeAtSubscription(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(QuoteCalculationException.ageIsEmptyException.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_profession_id() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setProfessionId(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoProfessionId.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_profession_name() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setProfessionName(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoProfessionName.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_dob() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setBirthDate(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoDOB.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_email() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setEmail(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoEmail.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_invalid_emails() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setEmail("me");
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me.com");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@me");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@me.");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@.com");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@me.c");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me@*.com");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me..@me.com");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
        quote.getInsureds().get(0).getPerson().setEmail("me.@me.1a");
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithInvalidEmail.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_geo_address() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setCurrentAddress(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoGeographicalAddress.getMessage());
    }

    @Test
    public void should_return_thailand_as_country_when_create_policy_with_main_insured_with_address_but_no_country() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        Policy policy = new Policy();
        productISafe.getPolicyFromQuote(policy, quote);
        assertThat(policy.getInsureds().get(0).getPerson().getCurrentAddress().getCountry()).isEqualTo("Thailand");
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_district() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getCurrentAddress().setDistrict(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.addressWithNoDistrict.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_postcode() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getCurrentAddress().setPostCode(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.addressWithNoPostCode.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_street_address_1() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getCurrentAddress().setStreetAddress1(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.addressWithNoStreetAddress1.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_street_address_2() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getCurrentAddress().setStreetAddress2(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.addressWithNoStreetAddress2.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_sub_country() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getCurrentAddress().setSubCountry(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.addressWithNoSubCountry.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_address_but_no_sub_district() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getCurrentAddress().setSubdistrict(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.addressWithNoSubDistrict.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_no_phone() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().setHomePhoneNumber(null);
        quote.getInsureds().get(0).getPerson().setMobilePhoneNumber(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.mainInsuredWithNoPhoneNumber.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_with_invalid_thai_id() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).getPerson().getRegistrations().get(0).setId("123456789");
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.personWithInvalidThaiIdNumber.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_beneficiary_with_invalid_thai_id() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getCoverages().get(0).getBeneficiaries().get(0).getPerson().getRegistrations().get(0).setId("123456789");
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.beneficiariesWithWrongIDNumber.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_no_beneficiary() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.beneficiariesNone.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_a_beneficiary_with_no_age() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getCoverages().get(0).getBeneficiaries().get(0).setAgeAtSubscription(null);
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.beneficiariesAgeAtSubscriptionEmpty.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_too_many_beneficiaries() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(1.0, "1"), TestUtil.beneficiary(1.0, "2"), TestUtil.beneficiary(1.0, "3"), TestUtil.beneficiary(1.0, "4"),
                TestUtil.beneficiary(1.0, "5"), TestUtil.beneficiary(1.0, "6"), TestUtil.beneficiary(94.0, "7"));
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.beneficiariesTooMany.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_beneficiaries_for_percent_sum_different_than_100() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(1.0, "1"), TestUtil.beneficiary(1.0, "2"), TestUtil.beneficiary(1.0, "3"), TestUtil.beneficiary(1.0, "4"),
                TestUtil.beneficiary(94.0, "5"));
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.beneficiariesPercentSumNot100.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_1_beneficiary_id_equal_to_insured_id() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0, "3101202780273"));
        quote.getCoverages().get(0).getBeneficiaries().get(0).getPerson().getRegistrations().get(0).setId(
                quote.getInsureds().get(0).getPerson().getRegistrations().get(0).getId());
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.beneficiariesIdIqualToInsuredId.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_2_beneficiaries_with_same_id() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(1.0, "3101202780273"), TestUtil.beneficiary(99.0, "3101202780273"));
        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.beneficiariesWithSameId.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_too_young() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setAgeAtSubscription(19);

        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(QuoteCalculationException.ageIsTooLowException.apply(20).getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_with_main_insured_too_old() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote.getInsureds().get(0).setAgeAtSubscription(71);

        Policy policy = new Policy();
        assertThatThrownBy(() -> productISafe.getPolicyFromQuote(policy, quote))
                .isInstanceOf(QuoteCalculationException.class)
                .hasMessage(QuoteCalculationException.ageIsTooHighException.apply(70).getMessage());
    }

    @Test
    public void should_copy_quote_details_into_policy() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        Policy policy = new Policy();
        productISafe.getPolicyFromQuote(policy, quote);
        assertThat(policy.getQuoteId()).isEqualTo(quote.getQuoteId());
        assertThat(policy.getCommonData()).isEqualToComparingFieldByField(quote.getCommonData());
        assertThat(policy.getPremiumsData().getProductISafePremium()).isEqualToComparingFieldByField(quote.getPremiumsData().getProductISafePremium());
        assertThat(policy.getPremiumsData()).isEqualToComparingFieldByField(quote.getPremiumsData());
        assertThat(policy.getCoverages()).isEqualTo(quote.getCoverages());
        assertThat(policy.getInsureds()).isEqualTo(quote.getInsureds());
    }

    @Test
    public void should_get_6_payments_when_choosing_yearly_schedule() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(ProductType.PRODUCT_ISAFE, 25, EVERY_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        Policy policy = new Policy();
        productISafe.getPolicyFromQuote(policy, quote);
        LocalDate startDate = policy.getInsureds().get(0).getStartDate();
        List<LocalDate> allowedDates = new ArrayList<>();
        IntStream.range(0, 6).forEach(value -> allowedDates.add(startDate.plusMonths(value * 12)));

        assertThat(policy.getPayments()).hasSize(6);
        assertThat(policy.getPayments()).extracting("dueDate").containsOnly(allowedDates.toArray());
        assertThat(policy.getPayments()).extracting("status").containsOnly(NOT_PROCESSED);
        assertThat(policy.getPayments()).extracting("effectiveDate").containsNull();
        assertThat(policy.getPayments()).extracting("amount").extracting("currencyCode").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode());
        assertThat(policy.getPayments()).extracting("amount").extracting("value").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue());
        assertThat(policy.getPayments()).extracting("registrationKey").containsNull();
        assertThat(policy.getPayments()).extracting("paymentInformations").hasSize(6);
    }

    @Test
    public void should_get_12_payments_when_choosing_half_year_schedule() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(25, EVERY_HALF_YEAR, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));

        Policy policy = new Policy();
        productISafe.getPolicyFromQuote(policy, quote);
        LocalDate startDate = policy.getInsureds().get(0).getStartDate();
        List<LocalDate> allowedDates = new ArrayList<>();
        IntStream.range(0, 12).forEach(value -> allowedDates.add(startDate.plusMonths(value * 6)));

        assertThat(policy.getPayments()).hasSize(12);
        assertThat(policy.getPayments()).extracting("dueDate").containsOnly(allowedDates.toArray());
        assertThat(policy.getPayments()).extracting("status").containsOnly(NOT_PROCESSED);
        assertThat(policy.getPayments()).extracting("effectiveDate").containsNull();
        assertThat(policy.getPayments()).extracting("amount").extracting("currencyCode").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode());
        assertThat(policy.getPayments()).extracting("amount").extracting("value").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue());
        assertThat(policy.getPayments()).extracting("registrationKey").containsNull();
        assertThat(policy.getPayments()).extracting("paymentInformations").hasSize(12);
    }

    @Test
    public void should_get_24_payments_when_choosing_quarter_schedule() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(25, EVERY_QUARTER, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));

        Policy policy = new Policy();
        productISafe.getPolicyFromQuote(policy, quote);
        LocalDate startDate = policy.getInsureds().get(0).getStartDate();
        List<LocalDate> allowedDates = new ArrayList<>();
        IntStream.range(0, 24).forEach(value -> allowedDates.add(startDate.plusMonths(value * 3)));

        assertThat(policy.getPayments()).hasSize(24);
        assertThat(policy.getPayments()).extracting("dueDate").containsOnly(allowedDates.toArray());
        assertThat(policy.getPayments()).extracting("status").containsOnly(NOT_PROCESSED);
        assertThat(policy.getPayments()).extracting("effectiveDate").containsNull();
        assertThat(policy.getPayments()).extracting("amount").extracting("currencyCode").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode());
        assertThat(policy.getPayments()).extracting("amount").extracting("value").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue());
        assertThat(policy.getPayments()).extracting("registrationKey").containsNull();
        assertThat(policy.getPayments()).extracting("paymentInformations").hasSize(24);
    }

    @Test
    public void should_get_72_payments_when_choosing_monthly_schedule() throws Exception {
        Quote quote = quote(TestUtil.productISafe());
        productISafe.calculateQuote(quote, productQuotation(25, EVERY_MONTH, 1000000.0));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));

        Policy policy = new Policy();
        productISafe.getPolicyFromQuote(policy, quote);
        LocalDate startDate = policy.getInsureds().get(0).getStartDate();
        List<LocalDate> allowedDates = new ArrayList<>();
        IntStream.range(0, 72).forEach(value -> allowedDates.add(startDate.plusMonths(value)));

        assertThat(policy.getPayments()).hasSize(72);
        assertThat(policy.getPayments()).extracting("dueDate").containsOnly(allowedDates.toArray());
        assertThat(policy.getPayments()).extracting("status").containsOnly(NOT_PROCESSED);
        assertThat(policy.getPayments()).extracting("effectiveDate").containsNull();
        assertThat(policy.getPayments()).extracting("amount").extracting("currencyCode").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode());
        assertThat(policy.getPayments()).extracting("amount").extracting("value").containsOnly(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue());
        assertThat(policy.getPayments()).extracting("registrationKey").containsNull();
        assertThat(policy.getPayments()).extracting("paymentInformations").hasSize(72);
    }
}
