package th.co.krungthaiaxa.api.elife.exception;

import java.util.function.Function;

public class QuoteCalculationException extends ElifeException {
    public static Function<String, QuoteCalculationException> sumInsuredCurrencyException = currency -> new QuoteCalculationException("Sum insured must be in currency " + currency + ".");
    public static Function<Double, QuoteCalculationException> sumInsuredTooHighException = max -> new QuoteCalculationException("Sum Insured cannot be over " + max + " Baht.");
    public static Function<Double, QuoteCalculationException> sumInsuredTooLowException = min -> new QuoteCalculationException("Sum Insured cannot be lower than " + min + " Baht.");
    public static Function<String, QuoteCalculationException> premiumCurrencyException = currency -> new QuoteCalculationException("Premium must be in currency " + currency + ".");
    public static Function<Double, QuoteCalculationException> premiumTooHighException = max -> new QuoteCalculationException("Premium cannot be over " + max + " Baht.");
    public static Function<Double, QuoteCalculationException> premiumTooLowException = min -> new QuoteCalculationException("Premium cannot be lower than " + min + " Baht.");
    public static Function<Integer, QuoteCalculationException> ageIsTooLowException = minAge -> new QuoteCalculationException("Insured must be at least "+minAge+" years old.");
    public static Function<Integer, QuoteCalculationException> ageIsTooHighException = maxAge -> new QuoteCalculationException("Insured must be less than "+maxAge+" years old.");
    public static QuoteCalculationException ageIsEmptyException = new QuoteCalculationException("Age of main insured is empty.");

    public static QuoteCalculationException iFinePackageNameUnknown = new QuoteCalculationException("The iFine package name is unknown.");

    private QuoteCalculationException(String message) {
        super(message);
    }
}
