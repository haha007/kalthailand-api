package th.co.krungthaiaxa.api.elife.line.v2.client.model;

/**
 * @author tuong.le on 10/19/17.
 */
public class LineMessage {
    private String to;
    private MessageObject messages;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public MessageObject getMessages() {
        return messages;
    }

    public void setMessages(MessageObject messages) {
        this.messages = messages;
    }
}
