package th.co.krungthaiaxa.api.elife.test.products;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_HALF_YEAR;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_QUARTER;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.products.ProductIFinePackage.IFINE1;
import static th.co.krungthaiaxa.api.elife.products.ProductIFinePackage.IFINE2;
import static th.co.krungthaiaxa.api.elife.products.ProductIFinePackage.IFINE3;
import static th.co.krungthaiaxa.api.elife.products.ProductIFinePackage.IFINE4;
import static th.co.krungthaiaxa.api.elife.products.ProductIFinePackage.IFINE5;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.product.ProductIFinePremium;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIFineTest {
	@Inject
	private QuoteService quoteService;

	@Test
	public void should_create_ifine_quote_with_default_structure() throws Exception {

		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE,
				ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_YEAR, GenderCode.MALE, false));
		assertThat(quote.getCoverages()).extracting("name").containsExactly("Product iFine");
		assertThat(quote.getPremiumsData().getProductIFinePremium()).isNotNull();
		assertThat(quote.getPremiumsData().getProductIFinePremium().getSumInsured().getValue()).isNotNull();
	}

	@Test
	public void should_get_sum_insured_from_package_name_ifine1() throws Exception {

		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE,
				ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_YEAR, GenderCode.MALE, false));
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

		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE,
				ProductQuotationFactory.constructIFine(IFINE2, 20, EVERY_YEAR, GenderCode.MALE, false));
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

		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE,
				ProductQuotationFactory.constructIFine(IFINE3, 20, EVERY_YEAR, GenderCode.MALE, false));
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

		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE,
				ProductQuotationFactory.constructIFine(IFINE4, 20, EVERY_YEAR, GenderCode.MALE, false));
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

		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE,
				ProductQuotationFactory.constructIFine(IFINE5, 20, EVERY_YEAR, GenderCode.MALE, false));
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
	public void should_calculate_from_yearly_male_age_20_not_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_YEAR,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3037.0);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(438.0);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(2599.0);
	}

	@Test
	public void should_calculate_from_yearly_male_age_20_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_YEAR,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(6936.0);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(38.99);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(438.0);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(6498.0);
	}

	@Test
	public void should_calculate_from_semiannualy_male_age_20_not_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_HALF_YEAR,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1579.24);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(227.76);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(1351.48);
	}

	@Test
	public void should_calculate_from_semiannualy_male_age_20_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_HALF_YEAR,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3606.72);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(38.99);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(227.76);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(3378.96);
	}

	@Test
	public void should_calculate_from_quarterly_male_age_20_not_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_QUARTER,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(819.99);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(118.26);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(701.73);
	}

	@Test
	public void should_calculate_from_quarterly_male_age_20_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_QUARTER,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1872.72);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(38.99);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(118.26);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(1754.46);
	}

	@Test
	public void should_calculate_from_monthly_male_age_20_not_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_MONTH,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(273.33);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(39.42);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(233.91);
	}

	@Test
	public void should_calculate_from_monthly_male_age_20_risky_package_ifine1() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE1, 20, EVERY_MONTH,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(624.24);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(4.38);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(25.99);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(38.99);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(39.42);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(584.82);
	}

	@Test
	public void should_calculate_from_yearly_female_age_58_not_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_YEAR,
				GenderCode.FEMALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(5860.5);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(2170.5);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(3690.0);
	}

	@Test
	public void should_calculate_from_yearly_female_age_58_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_YEAR,
				GenderCode.FEMALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(11395.5);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(2170.5);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(9225.0);
	}

	@Test
	public void should_calculate_from_semiannualy_female_age_58_not_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_HALF_YEAR,
				GenderCode.FEMALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3047.46);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1128.66);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(1918.8);
	}

	@Test
	public void should_calculate_from_semiannualy_female_age_58_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_HALF_YEAR,
				GenderCode.FEMALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(5925.66);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1128.66);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(4797.0);
	}

	@Test
	public void should_calculate_from_quarterly_female_age_58_not_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_QUARTER,
				GenderCode.FEMALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1582.34);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(586.04);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(996.3);
	}

	@Test
	public void should_calculate_from_quarterly_female_age_58_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_QUARTER,
				GenderCode.FEMALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3076.79);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(586.04);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(2490.75);
	}

	@Test
	public void should_calculate_from_monthly_female_age_58_not_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_MONTH,
				GenderCode.FEMALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(527.44);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(195.35);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(332.1);
	}

	@Test
	public void should_calculate_from_monthly_female_age_58_risky_package_ifine2() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE2, 58, EVERY_MONTH,
				GenderCode.FEMALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1025.60);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(14.47);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(24.6);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(36.9);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(195.35);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(830.25);
	}

	@Test
	public void should_calculate_from_yearly_male_age_44_not_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_YEAR,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(6654.0);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1872.0);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(4782.0);
	}

	@Test
	public void should_calculate_from_yearly_male_age_44_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_YEAR,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(13824.0);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(35.85);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(1872.0);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(11952.0);
	}

	@Test
	public void should_calculate_from_semiannualy_male_age_44_not_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_HALF_YEAR,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3460.08);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(973.44);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(2486.64);
	}

	@Test
	public void should_calculate_from_semiannualy_male_age_44_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_HALF_YEAR,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(7188.48);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(35.85);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(973.44);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(6215.04);
	}

	@Test
	public void should_calculate_from_quarterly_male_age_44_not_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_QUARTER,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1796.58);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(505.44);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(1291.14);
	}

	@Test
	public void should_calculate_from_quarterly_male_age_44_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_QUARTER,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(3732.48);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(35.85);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(505.44);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(3227.04);
	}

	@Test
	public void should_calculate_from_monthly_male_age_44_not_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_MONTH,
				GenderCode.MALE, false);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(598.86);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(0.0);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(168.48);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(430.38);
	}

	@Test
	public void should_calculate_from_monthly_male_age_44_risky_package_ifine3() throws Exception {
		// given
		ProductQuotation productQuotation = ProductQuotationFactory.constructIFine(IFINE3, 44, EVERY_MONTH,
				GenderCode.MALE, true);

		// under test
		Quote quote = quoteService.createQuote(RequestFactory.generateSession(), ChannelType.LINE, productQuotation);
		ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();

		// assert
		assertThat(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()).isEqualTo(1244.16);
		assertThat(productIFinePremium.getBasicPremiumRate()).isEqualTo(9.36);
		assertThat(productIFinePremium.getRiderPremiumRate()).isEqualTo(23.91);
		assertThat(productIFinePremium.getRiskOccupationCharge()).isEqualTo(35.85);
		assertThat(productIFinePremium.getTaxDeductible().getValue()).isEqualTo(168.48);
		assertThat(productIFinePremium.getNonTaxDeductible().getValue()).isEqualTo(1075.68);
	}
}
