package th.co.krungthaiaxa.ebiz.api.model;

public class FinancialScheduler {
    private Periodicity periodicity;
    private Amount modalAmount;

    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    public Amount getModalAmount() {
        return modalAmount;
    }

    public void setModalAmount(Amount modalAmount) {
        this.modalAmount = modalAmount;
    }
}
