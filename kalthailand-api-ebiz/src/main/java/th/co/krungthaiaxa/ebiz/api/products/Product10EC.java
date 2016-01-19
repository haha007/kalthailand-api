package th.co.krungthaiaxa.ebiz.api.products;

import th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.ebiz.api.model.Amount;
import th.co.krungthaiaxa.ebiz.api.model.DatedAmount;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Product10EC {

    private static Function<Integer, Integer> dvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 1 && numberOfYearsOfContract <= 9) {
            return 20;
        } else if (numberOfYearsOfContract == 10) {
            return 1820;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> averageExtraDvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 7 && numberOfYearsOfContract <= 9) {
            return 15;
        } else if (numberOfYearsOfContract == 10) {
            return 165;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> maximumExtraDvdRate = numberOfYearsOfContract -> {
        if (numberOfYearsOfContract >= 7 && numberOfYearsOfContract <= 9) {
            return 18;
        } else if (numberOfYearsOfContract == 10) {
            return 198;
        } else {
            return 0;
        }
    };

    private static Function<Integer, Integer> rate = age -> {
        if (age >= 0 && age <= 45) {
            return 308;
        } else if (age >= 46 && age <= 50) {
            return 306;
        } else if (age >= 51 && age <= 55) {
            return 304;
        } else if (age >= 56 && age <= 60) {
            return 301;
        } else if (age >= 61 && age <= 65) {
            return 300;
        } else if (age >= 66 && age <= 70) {
            return 298;
        } else {
            return 0;
        }
    };

    private static Function<PeriodicityCode, Double> modalFactor = periodicityCode -> {
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

    public static Quote calculateQuote(Quote quote) throws Exception {
        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(quote)) {
            return quote;
        }

        // calculates premium / sum insured
        if (quote.getPremiumsData().getLifeInsuranceSumInsured() != null) {
            quote.getPremiumsData().getFinancialScheduler().setModalAmount(getPremiumFromSumInsured(quote));
        } else {
            quote.getPremiumsData().setLifeInsuranceSumInsured(getSumInsuredFromPremium(quote));
        }

        if (quote.getPremiumsData().getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase("THB")
                && quote.getPremiumsData().getLifeInsuranceSumInsured().getValue() > 1000000.0) {
            throw QuoteCalculationException.sumInsuredTooHighException;
        } else if (quote.getPremiumsData().getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase("THB")
                && quote.getPremiumsData().getLifeInsuranceSumInsured().getValue() < 200000.0) {
            throw QuoteCalculationException.sumInsuredTooLowException;
        }

        // calculates yearly returns
        quote.getPremiumsData().setLifeInsuranceMinimumYearlyReturns(calculateDatedAmount(quote, 20, dvdRate));
        quote.getPremiumsData().setLifeInsuranceAverageYearlyReturns(calculateDatedAmount(quote, 40, dvdRate));
        quote.getPremiumsData().setLifeInsuranceMaximumYearlyReturns(calculateDatedAmount(quote, 45, dvdRate));

        // calculates yearly returns
        quote.getPremiumsData().setLifeInsuranceAverageExtraDividende(calculateDatedAmount(quote, 40, averageExtraDvdRate));
        quote.getPremiumsData().setLifeInsuranceMaximumExtraDividende(calculateDatedAmount(quote, 45, maximumExtraDvdRate));

        return quote;
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, Function<Integer, Integer> dvdFunction) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getLifeInsuranceSumInsured();
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= 10; i++) {
            Double interest = sumInsured.getValue() * dvdFunction.apply(i) / 1000;
            latestAmout = (double) Math.round(interest + latestAmout + (latestAmout * percentRate) / 1000);
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setCurrencyCode(sumInsured.getCurrencyCode());
            datedAmount.setDate(endDate.minus(10 - i, ChronoUnit.YEARS));
            datedAmount.setValue(latestAmout);
            result.add(datedAmount);
        }
        return result;
    }

    private static boolean hasEnoughTocalculate(Quote quote) {
        // Do we have enough to calculate things ?
        return quote.getPremiumsData().getLifeInsuranceSumInsured() != null
                || quote.getPremiumsData().getFinancialScheduler().getModalAmount() != null;
    }

    private static Amount getPremiumFromSumInsured(Quote quote) {
        Amount result = new Amount();
        Double value = quote.getPremiumsData().getLifeInsuranceSumInsured().getValue();
        value = value * rate.apply(quote.getInsureds().get(0).getAgeAtSubscription());
        value = value * modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        value = value / 1000;
        result.setValue(value);
        result.setCurrencyCode(quote.getPremiumsData().getLifeInsuranceSumInsured().getCurrencyCode());
        return result;
    }

    private static Amount getSumInsuredFromPremium(Quote quote) {
        Amount result = new Amount();
        Double value = quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue();
        value = value * 1000;
        value = value / modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        value = value / rate.apply(quote.getInsureds().get(0).getAgeAtSubscription());
        result.setValue(value);
        result.setCurrencyCode(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getCurrencyCode());
        return result;
    }
}
