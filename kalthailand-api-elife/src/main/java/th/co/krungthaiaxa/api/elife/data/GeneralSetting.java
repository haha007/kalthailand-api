package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author khoi.tran on 9/15/16.
 */
@Document(collection = "generalSetting")
public class GeneralSetting {
    private RetryPaymentSetting retryPaymentSetting;

    public RetryPaymentSetting getRetryPaymentSetting() {
        return retryPaymentSetting;
    }

    public void setRetryPaymentSetting(RetryPaymentSetting retryPaymentSetting) {
        this.retryPaymentSetting = retryPaymentSetting;
    }

    public static class RetryPaymentSetting {
        /**
         * This is not the full link with full parameters.
         * It's just the main link.
         */
        private String retryLink;
        private List<String> toSuccessEmails;

        public String getRetryLink() {
            return retryLink;
        }

        public void setRetryLink(String retryLink) {
            this.retryLink = retryLink;
        }

        public List<String> getToSuccessEmails() {
            return toSuccessEmails;
        }

        public void setToSuccessEmails(List<String> toSuccessEmails) {
            this.toSuccessEmails = toSuccessEmails;
        }
    }
}