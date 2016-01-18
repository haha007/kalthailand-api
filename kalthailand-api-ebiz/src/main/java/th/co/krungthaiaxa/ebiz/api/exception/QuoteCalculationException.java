package th.co.krungthaiaxa.ebiz.api.exception;

public class QuoteCalculationException extends Exception {
    public static QuoteCalculationException sumInsuredTooHighException = new QuoteCalculationException("Sum Insured is too high.");
    public static QuoteCalculationException sumInsuredTooLowException = new QuoteCalculationException("Sum Insured is too low.");

    private QuoteCalculationException(String message) {
        super(message);
    }
}
