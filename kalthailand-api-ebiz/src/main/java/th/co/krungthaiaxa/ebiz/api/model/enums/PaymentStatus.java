package th.co.krungthaiaxa.ebiz.api.model.enums;

public enum PaymentStatus {
    FUTURE,
    Denied, // You denied the payment. This happens only if the payment was previously pending because of possible reasons described for the pending_reason variable or the Fraud_Management_Filters_x variable.
    Expired, // This authorization has expired and cannot be captured.
    Failed, // The payment has failed. This happens only if the payment was made from your customer’s bank account.
    Pending, // The payment is pending. See pending_reason for more information.
    Refunded, // You refunded the payment.
    Reversed, // A payment was reversed due to a chargeback or other type of reversal. The funds have been removed from your account balance and returned to the buyer. The reason for the reversal is specified in the ReasonCode element.
    Processed, // A payment has been accepted.
    Voided // This authorization has been voided.
}
