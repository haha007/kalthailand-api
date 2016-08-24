package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.SessionQuote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;

@Repository
public interface SessionQuoteRepository extends PagingAndSortingRepository<SessionQuote, String> {
    SessionQuote findBySessionIdAndChannelType(String sessionId, ChannelType channelType);

//    @Query(value = "{"
//            + " 'quotes': {}"
//            + "     '$and':["
//            + "         {'commonData.productId': ?0 },"
//            + "         {'insureds.startDate':{$gte: ?1, $lte: ?2}}"
//            + "     ]"
//            + "}", count = true)
//    Long countByProductIdAndStartDateInRange(String productName, LocalDate startTimeBeginAt, LocalDate startTimeEndAt);
//    {
//        "$lookup:": {
//        "from":"quote",
//                "localField": "quotes.oid",
//                "foreignField": "_id",
//                "as":"joinedquote"
//    }
//    }
}
