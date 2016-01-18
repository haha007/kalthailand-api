package th.co.krungthaiaxa.ebiz.api.model;

import java.time.LocalDate;

public class FinancialScheduler {
    private Periodicity periodicity;
    private Amount modalAmount;
    private LocalDate endDate;

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

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
