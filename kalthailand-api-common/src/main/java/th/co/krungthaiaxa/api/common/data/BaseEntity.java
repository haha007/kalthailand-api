package th.co.krungthaiaxa.api.common.data;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * This class ensure that these fields {@link #createdDateTime} and {@link #updatedDateTime} should always have values.
 *
 * @author khoi.tran on 9/8/16.
 */
public class BaseEntity {
    /**
     * We use String here, don't use ObjectId because ObjectId cause difficulty after converted to Json.
     * ObjectId can be converted to Json seamlessly, but from Json of ObjectId, cannot convert to String object (in JavaScript).
     */
    @Id
    private String id;

    private LocalDateTime createdDateTime = LocalDateTime.now();
    private LocalDateTime updatedDateTime = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedDateTime() {
        if (createdDateTime == null) {
            createdDateTime = LocalDateTime.now();
        }
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
        if (this.createdDateTime == null) {
            this.createdDateTime = LocalDateTime.now();
        }
    }

    public LocalDateTime getUpdatedDateTime() {
        if (updatedDateTime == null) {
            updatedDateTime = LocalDateTime.now();
        }
        return updatedDateTime;
    }

    public void setUpdatedDateTime(LocalDateTime updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
        if (this.updatedDateTime == null) {
            this.updatedDateTime = LocalDateTime.now();
        }
    }
}
