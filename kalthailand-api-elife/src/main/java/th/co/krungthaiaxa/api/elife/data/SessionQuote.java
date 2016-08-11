package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "sessionQuote")
@CompoundIndexes({
        @CompoundIndex(name = "SessionQuote_UniqueKey",
                unique = true,
                def = "{'sessionId' : 1, 'channelType' : 1}")
})
public class SessionQuote {
    @Id
    private String id;
    @Indexed
    private String sessionId;
    private ChannelType channelType;
    @DBRef
    private List<Quote> quotes = new ArrayList<>();

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

    public List<Quote> getQuotes() {
        Collections.reverse(quotes);
        return quotes;
    }

    public void addQuote(Quote quote) {
        this.quotes.add(quote);
    }

}
