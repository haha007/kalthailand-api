package th.co.krungthaiaxa.ebiz.api.model;

import org.springframework.data.annotation.Id;
import th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType;

public class SessionQuote {
    @Id
    private String sessionQuoteId;
    private String sessionId;
    private ChannelType channelType;
    private String quoteId;

    public String getSessionQuoteId() {
        return sessionQuoteId;
    }

    public void setSessionQuoteId(String sessionQuoteId) {
        this.sessionQuoteId = sessionQuoteId;
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
