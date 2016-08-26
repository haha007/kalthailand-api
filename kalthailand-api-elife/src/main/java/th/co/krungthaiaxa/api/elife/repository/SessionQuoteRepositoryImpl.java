package th.co.krungthaiaxa.api.elife.repository;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.SessionQuote;
import th.co.krungthaiaxa.api.elife.model.Quote;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class SessionQuoteRepositoryImpl implements SessionQuoteRepositoryExtends {
    public static Logger logger = LoggerFactory.getLogger(SessionQuoteRepositoryImpl.class);

    private final MongoOperations mongoOperations;
    private final QuoteRepository quoteRepository;

    @Inject
    public SessionQuoteRepositoryImpl(MongoOperations mongoOperations, QuoteRepository quoteRepository) {
        this.mongoOperations = mongoOperations;
        this.quoteRepository = quoteRepository;
    }

    @Override
    public long countByProductIdAndStartDateInRange(String productName, LocalDateTime startTimeBeginAt, LocalDateTime startTimeEndAt) {
        List<ObjectId> quoteIds = findQuoteIdsByProductIdAndStartDateInRange(productName, startTimeBeginAt, startTimeEndAt);

        Query query = new Query();
        query.addCriteria(where("quotes.$id").in(quoteIds));
        return mongoOperations.count(query, SessionQuote.class, "sessionQuote");
    }

    //TODO we can improve performance by using map reduce of MongoDB, don't use stream of Java.
    private List<ObjectId> findQuoteIdsByProductIdAndStartDateInRange(String productName, LocalDateTime startTimeBeginAt, LocalDateTime startTimeEndAt) {
        List<Quote> quotes = quoteRepository.findObjectIdsByProductIdAndStartDateInRange(productName, startTimeBeginAt, startTimeEndAt);
        List<ObjectId> quoteIds = quotes.stream().map(quote -> new ObjectId(quote.getId())).collect(Collectors.toList());
        return quoteIds;
    }
}
