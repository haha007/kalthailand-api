package th.co.krungthaiaxa.elife.api.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.ProductIFinePremium;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.inject.Inject;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ProductIFinePackage.*;
import static th.co.krungthaiaxa.elife.api.model.enums.GenderCode.FEMALE;
import static th.co.krungthaiaxa.elife.api.model.enums.GenderCode.MALE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIFineTest {
    @Inject
    private ProductIFine productIFine;

    @Test
    public void should_get_sum_insured_from_package_name_ifine1() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(productIFinePremium.getSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(productIFinePremium.getAccidentSumInsured().getValue()).isEqualTo(500000.0);
        assertThat(productIFinePremium.getHealthSumInsured().getValue()).isEqualTo(500000.0);
        assertThat(productIFinePremium.getHospitalizationSumInsured().getValue()).isEqualTo(1000.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine2() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(productIFinePremium.getSumInsured().getValue()).isEqualTo(150000.0);
        assertThat(productIFinePremium.getAccidentSumInsured().getValue()).isEqualTo(750000.0);
        assertThat(productIFinePremium.getHealthSumInsured().getValue()).isEqualTo(750000.0);
        assertThat(productIFinePremium.getHospitalizationSumInsured().getValue()).isEqualTo(1250.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine3() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(productIFinePremium.getSumInsured().getValue()).isEqualTo(200000.0);
        assertThat(productIFinePremium.getAccidentSumInsured().getValue()).isEqualTo(1000000.0);
        assertThat(productIFinePremium.getHealthSumInsured().getValue()).isEqualTo(1000000.0);
        assertThat(productIFinePremium.getHospitalizationSumInsured().getValue()).isEqualTo(1500.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine4() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(productIFinePremium.getSumInsured().getValue()).isEqualTo(250000.0);
        assertThat(productIFinePremium.getAccidentSumInsured().getValue()).isEqualTo(2000000.0);
        assertThat(productIFinePremium.getHealthSumInsured().getValue()).isEqualTo(2000000.0);
        assertThat(productIFinePremium.getHospitalizationSumInsured().getValue()).isEqualTo(2000.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine5() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(productIFinePremium.getSumInsured().getValue()).isEqualTo(300000.0);
        assertThat(productIFinePremium.getAccidentSumInsured().getValue()).isEqualTo(3000000.0);
        assertThat(productIFinePremium.getHealthSumInsured().getValue()).isEqualTo(3000000.0);
        assertThat(productIFinePremium.getHospitalizationSumInsured().getValue()).isEqualTo(2500.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_not_risky_ifine1() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3162.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(563.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(2599.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_risky_ifine1() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 18, EVERY_YEAR, MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(7061.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(38.99);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(563.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(6498.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_not_risky_ifine2() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(4534.5);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(844.5);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(3690.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_risky_ifine2() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 18, EVERY_YEAR, MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(10069.5);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(844.5);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(9225.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_not_risky_ifine3() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(5908.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1126.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(4782.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_risky_ifine3() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 18, EVERY_YEAR, MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(13078.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(35.85);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1126.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(11952.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_not_risky_ifine4() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(10495.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(36.35);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1407.5);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(9087.5);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_risky_ifine4() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 18, EVERY_YEAR, MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(24127.5);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(36.35);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(54.53);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1407.5);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(22720.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_not_risky_ifine5() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 18, EVERY_YEAR, MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(15084.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(44.65);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1689.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(13395.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_18_risky_ifine5() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 18, EVERY_YEAR, MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(35178.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.63);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(44.65);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(66.98);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1689.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(33489.0);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine1() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 45, EVERY_MONTH, FEMALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(293.04);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(59.13);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(233.91);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_risky_ifine1() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 45, EVERY_MONTH, FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(643.95);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(38.99);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(59.13);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(584.82);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine2() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 45, EVERY_MONTH, FEMALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(420.8);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(88.7);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(332.1);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_risky_ifine2() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 45, EVERY_MONTH, FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(918.95);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(88.7);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(830.25);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine3() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 45, EVERY_MONTH, FEMALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(548.64);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(118.26);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(430.38);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_risky_ifine3() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 45, EVERY_MONTH, FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1193.94);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(35.85);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(118.26);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(1075.68);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine4() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 45, EVERY_MONTH, FEMALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(965.7);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(36.35);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(147.82);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(817.88);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_risky_ifine4() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 45, EVERY_MONTH, FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(2192.62);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(36.35);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(54.53);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(147.82);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(2044.8);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine5() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 45, EVERY_MONTH, FEMALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1382.94);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(44.65);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(177.39);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(1205.55);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_risky_ifine5() throws Exception {
        Quote quote = quote(productIFine());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 45, EVERY_MONTH, FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3191.4);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(44.65);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(66.98);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(177.39);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(3014.01);
    }

}
