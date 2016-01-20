package th.co.krungthaiaxa.ebiz.api.exception;

public class QuoteCalculationException extends Exception {
    public static QuoteCalculationException sumInsuredTooHighException = new QuoteCalculationException("Sum Insured is too high.");
    public static QuoteCalculationException sumInsuredTooLowException = new QuoteCalculationException("Sum Insured is too low.");
    public static QuoteCalculationException ageIsTooLowException = new QuoteCalculationException("Cannot insured when younger than 20 years old.");
    public static QuoteCalculationException ageIsTooHighException = new QuoteCalculationException("Cannot insured when older than 70 years old.");

    private QuoteCalculationException(String message) {
        super(message);
    }
}
