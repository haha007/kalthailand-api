package th.co.krungthaiaxa.api.elife.generalsetting;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.system.health.SystemHealthSetting;

import java.util.List;

/**
 * @author khoi.tran on 9/15/16.
 */
@Document(collection = "generalSetting")
public class GeneralSetting {
    @Id
    private String id;
    private boolean policyPremiumMockData = true;
    private RetryPaymentSetting retryPaymentSetting;
    private SystemHealthSetting systemHealthSetting;

    public RetryPaymentSetting getRetryPaymentSetting() {
        return retryPaymentSetting;
    }

    public void setRetryPaymentSetting(RetryPaymentSetting retryPaymentSetting) {
        this.retryPaymentSetting = retryPaymentSetting;
    }

    public boolean isPolicyPremiumMockData() {
        return policyPremiumMockData;
    }

    public void setPolicyPremiumMockData(boolean policyPremiumMockData) {
        this.policyPremiumMockData = policyPremiumMockData;
    }

    public SystemHealthSetting getSystemHealthSetting() {
        return systemHealthSetting;
    }

    public void setSystemHealthSetting(SystemHealthSetting systemHealthSetting) {
        this.systemHealthSetting = systemHealthSetting;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class RetryPaymentSetting {
        /**
         * This is not the full link with full parameters.
         * It's just the main link.
         * When client click to this link, it will open LINE browser and open our FE page. Then our FE page will redirect user to LINE page so that he can update his creditcard account.
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
