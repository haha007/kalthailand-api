package th.co.krungthaiaxa.elife.api.products;

import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

import static th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException.*;

public class ProductUtils {
    public static Function<PeriodicityCode, Double> modalFactor = periodicityCode -> {
        switch (periodicityCode) {
            case EVERY_MONTH:
                return 0.09;
            case EVERY_QUARTER:
                return 0.27;
            case EVERY_HALF_YEAR:
                return 0.52;
            case EVERY_YEAR:
                return 1.0;
            default:
                throw new RuntimeException("The periodicity [" + periodicityCode.name() + "] is invalid to get modal factor");
        }
    };

    public static Amount getPremiumFromSumInsured(Quote quote, Double rate) {
        Amount result = new Amount();
        Double value = quote.getPremiumsData().getLifeInsurance().getSumInsured().getValue();
        value = value * rate;
        value = value * modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        value = value / 1000;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(quote.getPremiumsData().getLifeInsurance().getSumInsured().getCurrencyCode());
        return result;
    }

    public static Amount getSumInsuredFromPremium(Quote quote, Double rate) {
        Amount result = new Amount();
        Double value = quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        value = value * 1000;
        value = value / modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        value = value / rate;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode());
        return result;
    }

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

    public static Integer getAge(LocalDate birthDate) {
        return ((Long) ChronoUnit.YEARS.between(birthDate, LocalDate.now())).intValue();
    }
}
