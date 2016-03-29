package th.co.krungthaiaxa.elife.api.model.line;

public class LinePayResponsePaymentInfo {
    private String method;
    private String amount;
    private String creditCardName;
    private String creditCardBrand;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCreditCardName() {
        return creditCardName;
    }

    public void setCreditCardName(String creditCardName) {
        this.creditCardName = creditCardName;
    }

    public String getCreditCardBrand() {
        return creditCardBrand;
    }

    public void setCreditCardBrand(String creditCardBrand) {
        this.creditCardBrand = creditCardBrand;
    }
}
