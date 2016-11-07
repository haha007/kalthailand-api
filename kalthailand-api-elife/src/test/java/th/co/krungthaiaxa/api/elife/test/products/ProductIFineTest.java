package th.co.krungthaiaxa.api.elife.test.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.product.ProductIFinePremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.products.ProductIFineService;

import javax.inject.Inject;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.TestUtil.quote;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.api.elife.products.ProductIFinePackage.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIFineTest {
    @Inject
    private ProductIFineService productIFine;

    @Test
    public void should_create_ifine_quote_with_default_structure() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        assertThat(quote.getCoverages()).extracting("name").containsExactly("Product iFine");
        assertThat(quote.getPremiumsData().getProductIFinePremium()).isNotNull();
        assertThat(quote.getPremiumsData().getProductIFinePremium().getSumInsured().getValue()).isNotNull();
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine1() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium premium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(premium.getSumInsured().getValue()).isEqualTo(100000.0);
        assertThat(premium.getAccidentSumInsured().getValue()).isEqualTo(500000.0);
        assertThat(premium.getHealthSumInsured().getValue()).isEqualTo(500000.0);
        assertThat(premium.getHospitalizationSumInsured().getValue()).isEqualTo(1000.0);
        assertThat(premium.getDeathByAccident().getValue()).isEqualTo(500000.0);
        assertThat(premium.getDeathByAccidentInPublicTransport().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getDisabilityFromAccidentMin().getValue()).isEqualTo(10000.0);
        assertThat(premium.getDisabilityFromAccidentMax().getValue()).isEqualTo(500000.0);
        assertThat(premium.getLossOfHandOrLeg().getValue()).isEqualTo(500000.0);
        assertThat(premium.getLossOfSight().getValue()).isEqualTo(500000.0);
        assertThat(premium.getLossOfHearingMin().getValue()).isEqualTo(75000.0);
        assertThat(premium.getLossOfHearingMax().getValue()).isEqualTo(375000.0);
        assertThat(premium.getLossOfSpeech().getValue()).isEqualTo(250000.0);
        assertThat(premium.getLossOfCorneaForBothEyes().getValue()).isEqualTo(250000.0);
        assertThat(premium.getLossOfFingersMin().getValue()).isEqualTo(10000.0);
        assertThat(premium.getLossOfFingersMax().getValue()).isEqualTo(350000.0);
        assertThat(premium.getNoneCurableBoneFracture().getValue()).isEqualTo(50000.0);
        assertThat(premium.getLegsShortenBy5cm().getValue()).isEqualTo(37500.0);
        assertThat(premium.getBurnInjuryMin().getValue()).isEqualTo(125000.0);
        assertThat(premium.getBurnInjuryMax().getValue()).isEqualTo(500000.0);
        assertThat(premium.getMedicalCareCost().getValue()).isEqualTo(50000.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine2() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium premium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(premium.getSumInsured().getValue()).isEqualTo(150000.0);
        assertThat(premium.getAccidentSumInsured().getValue()).isEqualTo(750000.0);
        assertThat(premium.getHealthSumInsured().getValue()).isEqualTo(750000.0);
        assertThat(premium.getHospitalizationSumInsured().getValue()).isEqualTo(1250.0);
        assertThat(premium.getDeathByAccident().getValue()).isEqualTo(750000.0);
        assertThat(premium.getDeathByAccidentInPublicTransport().getValue()).isEqualTo(1500000.0);
        assertThat(premium.getDisabilityFromAccidentMin().getValue()).isEqualTo(15000.0);
        assertThat(premium.getDisabilityFromAccidentMax().getValue()).isEqualTo(750000.0);
        assertThat(premium.getLossOfHandOrLeg().getValue()).isEqualTo(750000.0);
        assertThat(premium.getLossOfSight().getValue()).isEqualTo(750000.0);
        assertThat(premium.getLossOfHearingMin().getValue()).isEqualTo(112500.0);
        assertThat(premium.getLossOfHearingMax().getValue()).isEqualTo(562500.0);
        assertThat(premium.getLossOfSpeech().getValue()).isEqualTo(375000.0);
        assertThat(premium.getLossOfCorneaForBothEyes().getValue()).isEqualTo(375000.0);
        assertThat(premium.getLossOfFingersMin().getValue()).isEqualTo(15000.0);
        assertThat(premium.getLossOfFingersMax().getValue()).isEqualTo(525000.0);
        assertThat(premium.getNoneCurableBoneFracture().getValue()).isEqualTo(75000.0);
        assertThat(premium.getLegsShortenBy5cm().getValue()).isEqualTo(56250.0);
        assertThat(premium.getBurnInjuryMin().getValue()).isEqualTo(187500.0);
        assertThat(premium.getBurnInjuryMax().getValue()).isEqualTo(750000.0);
        assertThat(premium.getMedicalCareCost().getValue()).isEqualTo(75000.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine3() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium premium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(premium.getSumInsured().getValue()).isEqualTo(200000.0);
        assertThat(premium.getAccidentSumInsured().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getHealthSumInsured().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getHospitalizationSumInsured().getValue()).isEqualTo(1500.0);
        assertThat(premium.getDeathByAccident().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getDeathByAccidentInPublicTransport().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getDisabilityFromAccidentMin().getValue()).isEqualTo(20000.0);
        assertThat(premium.getDisabilityFromAccidentMax().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getLossOfHandOrLeg().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getLossOfSight().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getLossOfHearingMin().getValue()).isEqualTo(150000.0);
        assertThat(premium.getLossOfHearingMax().getValue()).isEqualTo(750000.0);
        assertThat(premium.getLossOfSpeech().getValue()).isEqualTo(500000.0);
        assertThat(premium.getLossOfCorneaForBothEyes().getValue()).isEqualTo(500000.0);
        assertThat(premium.getLossOfFingersMin().getValue()).isEqualTo(20000.0);
        assertThat(premium.getLossOfFingersMax().getValue()).isEqualTo(700000.0);
        assertThat(premium.getNoneCurableBoneFracture().getValue()).isEqualTo(100000.0);
        assertThat(premium.getLegsShortenBy5cm().getValue()).isEqualTo(75000.0);
        assertThat(premium.getBurnInjuryMin().getValue()).isEqualTo(250000.0);
        assertThat(premium.getBurnInjuryMax().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getMedicalCareCost().getValue()).isEqualTo(100000.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine4() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium premium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(premium.getSumInsured().getValue()).isEqualTo(250000.0);
        assertThat(premium.getAccidentSumInsured().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getHealthSumInsured().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getHospitalizationSumInsured().getValue()).isEqualTo(2000.0);
        assertThat(premium.getDeathByAccident().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getDeathByAccidentInPublicTransport().getValue()).isEqualTo(4000000.0);
        assertThat(premium.getDisabilityFromAccidentMin().getValue()).isEqualTo(40000.0);
        assertThat(premium.getDisabilityFromAccidentMax().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getLossOfHandOrLeg().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getLossOfSight().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getLossOfHearingMin().getValue()).isEqualTo(300000.0);
        assertThat(premium.getLossOfHearingMax().getValue()).isEqualTo(1500000.0);
        assertThat(premium.getLossOfSpeech().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getLossOfCorneaForBothEyes().getValue()).isEqualTo(1000000.0);
        assertThat(premium.getLossOfFingersMin().getValue()).isEqualTo(40000.0);
        assertThat(premium.getLossOfFingersMax().getValue()).isEqualTo(1400000.0);
        assertThat(premium.getNoneCurableBoneFracture().getValue()).isEqualTo(200000.0);
        assertThat(premium.getLegsShortenBy5cm().getValue()).isEqualTo(150000.0);
        assertThat(premium.getBurnInjuryMin().getValue()).isEqualTo(500000.0);
        assertThat(premium.getBurnInjuryMax().getValue()).isEqualTo(2000000.0);
        assertThat(premium.getMedicalCareCost().getValue()).isEqualTo(200000.0);
    }

    @Test
    public void should_get_sum_insured_from_package_name_ifine5() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium premium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(premium.getSumInsured().getValue()).isEqualTo(300000.0);
        assertThat(premium.getAccidentSumInsured().getValue()).isEqualTo(3000000.0);
        assertThat(premium.getHealthSumInsured().getValue()).isEqualTo(3000000.0);
        assertThat(premium.getHospitalizationSumInsured().getValue()).isEqualTo(2500.0);
        assertThat(premium.getDeathByAccident().getValue()).isEqualTo(3000000.0);
        assertThat(premium.getDeathByAccidentInPublicTransport().getValue()).isEqualTo(6000000.0);
        assertThat(premium.getDisabilityFromAccidentMin().getValue()).isEqualTo(60000.0);
        assertThat(premium.getDisabilityFromAccidentMax().getValue()).isEqualTo(3000000.0);
        assertThat(premium.getLossOfHandOrLeg().getValue()).isEqualTo(3000000.0);
        assertThat(premium.getLossOfSight().getValue()).isEqualTo(3000000.0);
        assertThat(premium.getLossOfHearingMin().getValue()).isEqualTo(450000.0);
        assertThat(premium.getLossOfHearingMax().getValue()).isEqualTo(2250000.0);
        assertThat(premium.getLossOfSpeech().getValue()).isEqualTo(1500000.0);
        assertThat(premium.getLossOfCorneaForBothEyes().getValue()).isEqualTo(1500000.0);
        assertThat(premium.getLossOfFingersMin().getValue()).isEqualTo(60000.0);
        assertThat(premium.getLossOfFingersMax().getValue()).isEqualTo(2100000.0);
        assertThat(premium.getNoneCurableBoneFracture().getValue()).isEqualTo(300000.0);
        assertThat(premium.getLegsShortenBy5cm().getValue()).isEqualTo(225000.0);
        assertThat(premium.getBurnInjuryMin().getValue()).isEqualTo(750000.0);
        assertThat(premium.getBurnInjuryMax().getValue()).isEqualTo(3000000.0);
        assertThat(premium.getMedicalCareCost().getValue()).isEqualTo(300000.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_not_risky_ifine1() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3183.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(584.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(2599.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_risky_ifine1() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 20, EVERY_YEAR, GenderCode.MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(7082.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(38.99);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(584.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(6498.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_not_risky_ifine2() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(4566.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(876.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(3690.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_risky_ifine2() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 20, EVERY_YEAR, GenderCode.MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(10101.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(876.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(9225.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_not_risky_ifine3() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(5950.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1168.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(4782.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_risky_ifine3() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 20, EVERY_YEAR, GenderCode.MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(13120.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(35.85);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1168.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(11952.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_not_risky_ifine4() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(10547.5);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(36.35);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1460.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(9087.5);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_risky_ifine4() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 20, EVERY_YEAR, GenderCode.MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(24180.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(36.35);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(54.53);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1460.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(22720.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_not_risky_ifine5() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(15147.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(44.65);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1752.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(13395.0);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_20_risky_ifine5() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 20, EVERY_YEAR, GenderCode.MALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(35241.0);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(5.84);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(44.65);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(66.98);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1752.0);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(33489.0);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine1() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 45, EVERY_MONTH, GenderCode.FEMALE, FALSE));
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
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 45, EVERY_MONTH, GenderCode.FEMALE, TRUE));
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
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 45, EVERY_MONTH, GenderCode.FEMALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(420.79);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(88.7);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(332.1);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_risky_ifine2() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE2, 45, EVERY_MONTH, GenderCode.FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(918.94);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(88.7);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(830.25);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine3() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 45, EVERY_MONTH, GenderCode.FEMALE, FALSE));
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
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE3, 45, EVERY_MONTH, GenderCode.FEMALE, TRUE));
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
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 45, EVERY_MONTH, GenderCode.FEMALE, FALSE));
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
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE4, 45, EVERY_MONTH, GenderCode.FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(2192.63);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(36.35);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(54.53);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(147.82);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(2044.8);
    }

    @Test
    public void should_calculate_from_monthly_periodicity_female_age_45_not_risky_ifine5() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 45, EVERY_MONTH, GenderCode.FEMALE, FALSE));
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
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE5, 45, EVERY_MONTH, GenderCode.FEMALE, TRUE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3191.4);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(6.57);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(44.65);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(66.98);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(177.39);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(3014.01);
    }

    @Test
    public void should_calculate_from_yearly_periodicity_male_age_48_not_risky_ifine1() throws Exception {
        Quote quote = quote(TestUtil.productIFineService());
        productIFine.calculateQuote(quote, productQuotation(IFINE1, 48, EVERY_MONTH, GenderCode.MALE, FALSE));
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(368.55);
        assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.96);
        assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
        assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
        assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(134.64);
        assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(233.91);
    }

}
