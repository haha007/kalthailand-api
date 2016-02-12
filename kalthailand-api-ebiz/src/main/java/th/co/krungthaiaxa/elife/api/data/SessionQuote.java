package th.co.krungthaiaxa.elife.api.data;

import org.springframework.data.annotation.Id;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;

public class SessionQuote {
    @Id
    private String id;
    private String sessionId;
    private ChannelType channelType;
    private String quoteId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }
}
