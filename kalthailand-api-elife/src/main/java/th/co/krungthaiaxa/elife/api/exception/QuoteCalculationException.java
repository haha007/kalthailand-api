package th.co.krungthaiaxa.elife.api.exception;

import java.util.function.Function;

public class QuoteCalculationException extends Exception {
    public static Function<String, QuoteCalculationException> sumInsuredCurrencyException = currency -> new QuoteCalculationException("Sum insured must be in currency " + currency + ".");
    public static Function<Double, QuoteCalculationException> sumInsuredTooHighException = max -> new QuoteCalculationException("Sum Insured cannot be over " + max + " Baht.");
    public static Function<Double, QuoteCalculationException> sumInsuredTooLowException = min -> new QuoteCalculationException("Sum Insured cannot be lower than " + min + " Baht.");
    public static Function<String, QuoteCalculationException> premiumCurrencyException = currency -> new QuoteCalculationException("Premium must be in currency " + currency + ".");
    public static Function<Double, QuoteCalculationException> premiumTooHighException = max -> new QuoteCalculationException("Premium cannot be over " + max + " Baht.");
    public static Function<Double, QuoteCalculationException> premiumTooLowException = min -> new QuoteCalculationException("Premium cannot be lower than " + min + " Baht.");
    public static QuoteCalculationException ageIsTooLowException = new QuoteCalculationException("Cannot insured when younger than 20 years old.");
    public static QuoteCalculationException ageIsTooHighException = new QuoteCalculationException("Cannot insured when older than 70 years old.");
    public static QuoteCalculationException ageIsEmptyException = new QuoteCalculationException("Age of main insured is empty.");

    private QuoteCalculationException(String message) {
        super(message);
    }
}
