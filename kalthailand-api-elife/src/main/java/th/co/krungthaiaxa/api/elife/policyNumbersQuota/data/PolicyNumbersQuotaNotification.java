package th.co.krungthaiaxa.api.elife.policyNumbersQuota.data;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.validator.ElementEmail;
import th.co.krungthaiaxa.api.elife.validator.ElementNotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Document
public class PolicyNumbersQuotaNotification {
    @Id
    private String id;

    private ZonedDateTime notificationTime;

    @Indexed(unique = true)
    private String notificationEmail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(ZonedDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }
}
