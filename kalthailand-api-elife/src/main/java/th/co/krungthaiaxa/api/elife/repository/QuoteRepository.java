package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Quote;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface QuoteRepository extends PagingAndSortingRepository<Quote, String> {
    Quote findByQuoteId(String quoteId);

    @Query(value = "{"
            + "'$and':["
            + "{'commonData.productId': ?0 },"
            + "{'insureds.startDate':{$gte: ?1, $lte: ?2}}"
            + "]"
            + "}", count = true)
    Long countByProductIdAndInsuredStartDateInRange(String productName, LocalDate startTimeBeginAt, LocalDate startTimeEndAt);
    @Query(value = "{"
            + "'$and':["
            + "{'commonData.productId': ?0 },"
            + "{'creationDateTime':{$gte: ?1, $lte: ?2}}"
            + "]"
            + "}", count = true)
    Long countByProductIdAndStartDateInRange(String productName, LocalDateTime startTimeBeginAt, LocalDateTime startTimeEndAt);
}
