package th.co.krungthaiaxa.elife.api.products;

import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Coverage;
import th.co.krungthaiaxa.elife.api.model.Insured;
import th.co.krungthaiaxa.elife.api.model.PremiumsData;
import th.co.krungthaiaxa.elife.api.model.Quote;

import java.util.ArrayList;
import java.util.Optional;

import static th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException.*;

public class ProductUtils {

    public static void checkMainInsuredAge(Insured insured, int minAge, int maxAge) throws QuoteCalculationException {
        if (insured.getAgeAtSubscription() == null) {
            throw ageIsEmptyException;
        } else if (insured.getAgeAtSubscription() > maxAge) {
            throw ageIsTooHighException;
        } else if (insured.getAgeAtSubscription() < minAge) {
            throw ageIsTooLowException;
        }
    }

    public static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        if (quote.getPremiumsData().getLifeInsurance() != null) {
            quote.getPremiumsData().getLifeInsurance().setYearlyCashBacksAverageBenefit(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setYearlyCashBacksAverageDividende(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setEndOfContractBenefitsAverage(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setYearlyCashBacksMaximumBenefit(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setYearlyCashBacksMaximumDividende(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setEndOfContractBenefitsMaximum(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setEndOfContractBenefitsMinimum(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setYearlyCashBacks(new ArrayList<>());
            quote.getPremiumsData().getLifeInsurance().setYearlyTaxDeduction(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    public static void checkSumInsured(PremiumsData premiumsData, String currency, Double sumInsuredMin, Double sumInsuredMax) throws QuoteCalculationException {
        if (premiumsData.getLifeInsurance().getSumInsured() == null || premiumsData.getLifeInsurance().getSumInsured().getValue() == null) {
            // no amount to check
            return;
        } else if (!currency.equalsIgnoreCase(premiumsData.getLifeInsurance().getSumInsured().getCurrencyCode())) {
            throw sumInsuredCurrencyException.apply(currency);
        }

        if (premiumsData.getLifeInsurance().getSumInsured().getValue() > sumInsuredMax) {
            throw sumInsuredTooHighException.apply(sumInsuredMax);
        } else if (premiumsData.getLifeInsurance().getSumInsured().getValue() < sumInsuredMin) {
            throw sumInsuredTooLowException.apply(sumInsuredMin);
        }
    }
}
