package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

import java.util.function.Function;

/**
 * This is the exception which is specific for a request. However, it's not a good Exception design. The Exception must follow domain design.
 * For example:
 * For exception {@link #mainInsuredNotExistException} cannot be reused for different requests: {@link th.co.krungthaiaxa.api.elife.service.QuoteService#createQuote(String, ChannelType, ProductQuotation)} and
 * {@link th.co.krungthaiaxa.api.elife.service.PolicyService#createPolicy(Quote)}.
 */
public class QuoteCalculationException extends ElifeException {
    public static Function<String, QuoteCalculationException> mainInsuredNotExistException = message -> new QuoteCalculationException("Not found main insured: " + message + ".");
    public static Function<Integer, QuoteCalculationException> occupationNotExistException = occupationId -> new QuoteCalculationException("Not found occupation " + occupationId + ".");
    public static Function<String, QuoteCalculationException> sumInsuredCurrencyException = currency -> new QuoteCalculationException("Sum insured must be in currency " + currency + ".");
    public static Function<String, QuoteCalculationException> sumInsuredTooHighException = msg -> new QuoteCalculationException("Sum Insured cannot be too high: " + msg + ".");
    public static Function<String, QuoteCalculationException> sumInsuredTooLowException = msg -> new QuoteCalculationException("Sum Insured cannot be too low: " + msg + ".");
    public static Function<String, QuoteCalculationException> premiumCurrencyException = currency -> new QuoteCalculationException("Premium must be in currency " + currency + ".");
    public static Function<String, QuoteCalculationException> premiumRateNotFoundException = message -> new QuoteCalculationException("Cannot find premium rate: " + message + ".");
    public static Function<Double, QuoteCalculationException> premiumTooHighException = max -> new QuoteCalculationException("Premium cannot be over " + max + " Baht.");
    public static Function<Double, QuoteCalculationException> premiumTooLowException = min -> new QuoteCalculationException("Premium cannot be lower than " + min + " Baht.");
    public static Function<Integer, QuoteCalculationException> ageIsTooLowException = minAge -> new QuoteCalculationException("Insured must be at least " + minAge + " years old.");
    public static Function<Integer, QuoteCalculationException> ageIsTooHighException = maxAge -> new QuoteCalculationException("Insured must be less than " + maxAge + " years old.");
    public static QuoteCalculationException ageIsEmptyException = new QuoteCalculationException("Age of main insured is empty.");

    public static Function<String, QuoteCalculationException> packageNameUnknown = message -> new QuoteCalculationException("The package name is unknown: " + message);
    public static Function<Double, QuoteCalculationException> discountRateNotFound = sumInsured -> new QuoteCalculationException("Cannot find discount rate which satisfies sumInsured  " + sumInsured + ".");

    private QuoteCalculationException(String message) {
        super(message);
    }
}
