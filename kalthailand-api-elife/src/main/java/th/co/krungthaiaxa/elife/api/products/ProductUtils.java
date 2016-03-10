package th.co.krungthaiaxa.elife.api.products;

import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    public static Amount getPremiumFromSumInsured(Amount sumInsured, Double rate, PeriodicityCode periodicityCode) {
        Amount result = new Amount();
        Double value = sumInsured.getValue();
        value = value * rate;
        value = value * modalFactor.apply(periodicityCode);
        value = value / 1000;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(sumInsured.getCurrencyCode());
        return result;
    }

    public static Amount getSumInsuredFromPremium(Amount premium, Double rate, PeriodicityCode periodicityCode) {
        Amount result = new Amount();
        Double value = premium.getValue();
        value = value * 1000;
        value = value / modalFactor.apply(periodicityCode);
        value = value / rate;
        result.setValue((double) (Math.round(value * 100)) / 100);
        result.setCurrencyCode(premium.getCurrencyCode());
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

    public static Integer getAge(LocalDate birthDate) {
        return ((Long) ChronoUnit.YEARS.between(birthDate, LocalDate.now())).intValue();
    }
}
