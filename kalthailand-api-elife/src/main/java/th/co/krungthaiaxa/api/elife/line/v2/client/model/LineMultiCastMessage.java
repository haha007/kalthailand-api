package th.co.krungthaiaxa.api.elife.line.v2.client.model;

import java.util.List;
import java.util.Set;

/**
 * @author tuong.le on 10/19/17.
 */
public class LineMultiCastMessage {
    private Set<String> to;
    private List<MessageObject> messages;

    public Set<String> getTo() {
        return to;
    }

    public void setTo(Set<String> to) {
        this.to = to;
    }

    public List<MessageObject> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageObject> messages) {
        this.messages = messages;
    }
}
