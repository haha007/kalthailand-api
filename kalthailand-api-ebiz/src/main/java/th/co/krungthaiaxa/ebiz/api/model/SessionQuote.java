package th.co.krungthaiaxa.ebiz.api.model;

import org.springframework.data.annotation.Id;
import th.co.krungthaiaxa.ebiz.api.model.enums.SessionType;

public class SessionQuote {
    @Id
    private String sessionQuoteId;
    private String sessionId;
    private SessionType sessionType;
    private Quote quote;

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

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }
}
