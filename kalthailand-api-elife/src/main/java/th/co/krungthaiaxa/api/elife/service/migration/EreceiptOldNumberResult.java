package th.co.krungthaiaxa.api.elife.service.migration;

import th.co.krungthaiaxa.api.elife.model.Payment;

import java.util.List;

/**
 * @author khoi.tran on 10/31/16.
 */
public class EreceiptOldNumberResult {
    private List<Payment> newBusinessPayments;
    private List<Payment> retryPayments;

    public List<Payment> getNewBusinessPayments() {
        return newBusinessPayments;
    }

    public void setNewBusinessPayments(List<Payment> newBusinessPayments) {
        this.newBusinessPayments = newBusinessPayments;
    }

    public List<Payment> getRetryPayments() {
        return retryPayments;
    }

    public void setRetryPayments(List<Payment> retryPayments) {
        this.retryPayments = retryPayments;
    }
}
