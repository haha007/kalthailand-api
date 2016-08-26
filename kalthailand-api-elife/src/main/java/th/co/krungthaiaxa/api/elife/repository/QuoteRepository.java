package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Quote;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuoteRepository extends PagingAndSortingRepository<Quote, String> {

    @Query(value = "{"
            + "'$and':["
            + "{'commonData.productId': ?0 },"
            + "{'creationDateTime':{$gte: ?1, $lte: ?2}}"
            + "]"
            + "}", fields = "['_id']")
    List<Quote> findObjectIdsByProductIdAndStartDateInRange(String productName, LocalDateTime startTimeBeginAt, LocalDateTime startTimeEndAt);

}
