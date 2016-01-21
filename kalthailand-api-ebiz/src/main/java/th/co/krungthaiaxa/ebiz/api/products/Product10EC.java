package th.co.krungthaiaxa.ebiz.api.products;

import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException.*;

public class Product10EC {
    private final static String PRODUCT_10_EC_NAME = "10 EC";

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
        Optional<Coverage> has10ECCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_10_EC_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(quote)) {
            // we need to delete what might have been calculated before
            quote.getPremiumsData().setLifeInsuranceAverageExtraDividende(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceAverageYearlyReturns(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMaximumExtraDividende(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMaximumYearlyReturns(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMinimumExtraDividende(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceMinimumYearlyReturns(new ArrayList<>());
            quote.getPremiumsData().setLifeInsuranceYearlyCashBacks(new ArrayList<>());
            quote.getCommonData().setProductId(null);
            quote.getCommonData().setProductName(null);
            if (has10ECCoverage.isPresent()) {
                quote.getCoverages().remove(has10ECCoverage.get());
            }
            return quote;
        }

        // cannot be too young or too old
        for (Insured insured : quote.getInsureds()) {
            if (insured.getMainInsuredIndicator()) {
                if (insured.getAgeAtSubscription() > 70) {
                    throw ageIsTooHighException;
                } else if (insured.getAgeAtSubscription() < 20) {
                    throw ageIsTooLowException;
                }
            }
        }

        // calculates premium / sum insured
        PremiumsDataLifeInsurance premiumsData = quote.getPremiumsData();
        if (premiumsData.getLifeInsuranceSumInsured() != null) {
            premiumsData.getFinancialScheduler().setModalAmount(getPremiumFromSumInsured(quote));
        } else {
            premiumsData.setLifeInsuranceSumInsured(getSumInsuredFromPremium(quote));
        }

        if (premiumsData.getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase("THB")
                && premiumsData.getLifeInsuranceSumInsured().getValue() > 1000000.0) {
            throw sumInsuredTooHighException;
        } else if (premiumsData.getLifeInsuranceSumInsured().getCurrencyCode().equalsIgnoreCase("THB")
                && premiumsData.getLifeInsuranceSumInsured().getValue() < 200000.0) {
            throw sumInsuredTooLowException;
        }

        // calculates yearly cash backs
        premiumsData.setLifeInsuranceYearlyCashBacks(calculateDatedAmount(quote, null, dvdRate));

        // calculates yearly returns
        premiumsData.setLifeInsuranceMinimumYearlyReturns(calculateDatedAmount(quote, 20, dvdRate));
        premiumsData.setLifeInsuranceAverageYearlyReturns(calculateDatedAmount(quote, 40, dvdRate));
        premiumsData.setLifeInsuranceMaximumYearlyReturns(calculateDatedAmount(quote, 45, dvdRate));

        // calculates yearly returns
        premiumsData.setLifeInsuranceAverageExtraDividende(calculateDatedAmount(quote, 40, averageExtraDvdRate));
        premiumsData.setLifeInsuranceMaximumExtraDividende(calculateDatedAmount(quote, 45, maximumExtraDvdRate));

        if (!has10ECCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_10_EC_NAME);
            quote.getCommonData().setProductId(PRODUCT_10_EC_NAME);
            quote.getCommonData().setProductName(PRODUCT_10_EC_NAME);
            quote.addCoverage(coverage);
        }

        return quote;
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, Function<Integer, Integer> dvdFunction) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = quote.getPremiumsData().getLifeInsuranceSumInsured();
        LocalDate endDate = quote.getPremiumsData().getFinancialScheduler().getEndDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= 10; i++) {
            Double interest = sumInsured.getValue() * dvdFunction.apply(i) / 1000;
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setCurrencyCode(sumInsured.getCurrencyCode());
            datedAmount.setDate(endDate.minus(10 - i, ChronoUnit.YEARS));
            if (percentRate != null) {
                latestAmout = (double) Math.round(interest + latestAmout + (latestAmout * percentRate) / 1000);
                datedAmount.setValue(latestAmout);
            } else {
                datedAmount.setValue(interest);
            }
            result.add(datedAmount);
        }
        return result;
    }

    private static boolean hasEnoughTocalculate(Quote quote) {
        // Do we have a birth date to calculate the age of insured
        boolean hasAnyDateOfBirth = quote.getInsureds().stream()
                .filter(insured -> insured != null)
                .filter(insured -> insured.getPerson() != null)
                .filter(insured -> insured.getPerson().getBirthDate() != null)
                .findFirst()
                .isPresent();
        if (!hasAnyDateOfBirth) {
            return false;
        }

        // we need an amount
        boolean hasAmount = quote.getPremiumsData().getLifeInsuranceSumInsured() != null
                || quote.getPremiumsData().getFinancialScheduler().getModalAmount() != null;
        if (!hasAmount) {
            return false;
        }

        // We need a periodicity
        return quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode() != null;
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
