package th.co.krungthaiaxa.api.elife.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jsoup.helper.StringUtil;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObjectBuilder;

import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;

@Repository
public class QuoteCriteriaRepository {
	
	@Inject
    private MongoOperations mongoOperations;
	
	public Long quoteCount(String productId, LocalDate startDate, LocalDate endDate){
		Query query = new Query();
		query.addCriteria(where("commonData.productId").is(productId));
		query.addCriteria(where("insureds.startDate").gte(getDateFromLocalDate(startDate)).lte(getDateFromLocalDate(endDate)));
		return mongoOperations.count(query, Quote.class, "quote");
	}
	
	private Date getDateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
