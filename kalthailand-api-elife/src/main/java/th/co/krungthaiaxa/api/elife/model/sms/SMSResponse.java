package th.co.krungthaiaxa.api.elife.model.sms;

/**
 * @author khoi.tran on 10/17/16.
 */
public class SMSResponse {
    public static final String STATUS_SUCCESS = "0";
    /**
     * This is the fail status which defined by our own system, not defined by SMS system.
     */
    public static final String STATUS_INTERNAL_FAIL = "-1";
    /**
     * This is the id of request message, not the response message.
     */
    private String messageId;
    private String taskId;
    private String status;
    private String end;
    private String responseMessage;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
