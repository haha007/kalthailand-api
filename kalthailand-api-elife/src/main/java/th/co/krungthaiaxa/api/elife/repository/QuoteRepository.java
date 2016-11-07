package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuoteRepository extends MongoRepository<Quote, String> {

    @Query(value = "{"
            + "'$and':["
            + "{'commonData.productId': ?0 },"
            + "{'creationDateTime':{$gte: ?1, $lte: ?2}}"
            + "]"
            + "}", fields = "['_id']")
    List<Quote> findObjectIdsByProductIdAndStartDateInRange(String productName, LocalDateTime startTimeBeginAt, LocalDateTime startTimeEndAt);

    @Query(value = "{"
            + "'$and':["
            + "{'commonData.productId': ?0 },"
            + "{'creationDateTime':{$gte: ?1, $lte: ?2}}"
            + "]"
            + "}", count = true)
    long countByProductIdAndStartDateInRange(String productId, LocalDateTime startDate, LocalDateTime endDate);

    Quote findByQuoteId(String quoteId);

    @Query("{'premiumsData.financialScheduler.periodicity.code': ?0}")
    List<Quote> findByPeriodicityCode(PeriodicityCode everyMonth);
}
