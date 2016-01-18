package th.co.krungthaiaxa.ebiz.api.model;

import java.time.LocalDate;

public class DatedAmount {
    private Double value;
    private String currencyCode;
    private LocalDate date;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
