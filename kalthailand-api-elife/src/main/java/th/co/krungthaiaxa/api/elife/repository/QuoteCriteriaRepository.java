package th.co.krungthaiaxa.api.elife.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.jsoup.helper.StringUtil;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObjectBuilder;

import th.co.krungthaiaxa.api.elife.data.SessionQuote;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;

@Repository
public class QuoteCriteriaRepository {
	
	@Inject
    private MongoOperations mongoOperations;
	
	private final String PRODUCT_ID = "productId";
	private final String QUOTE_COUNT = "quoteCount";
	
	public List<Map<String,Object>> quoteCount(LocalDate startDate, LocalDate endDate){		
		List<String> mids = new ArrayList<>();
		List<Map<String,Object>> productSeparateList = new ArrayList<>();
		
		List<Quote> quotes = quoteQuery(startDate, endDate);
		
		for(Quote quote : quotes){			
			for(SessionQuote sessionQuote : sessionQuoteQuery(quote)){				
				if(mids.size()==0){
					mids.add(sessionQuote.getSessionId());		
					updateProductSeparateList(productSeparateList,quote);
				}else{
					boolean add = true;
					for(String mid : mids){
						if(add){
							if(mid.equals(sessionQuote.getSessionId())){
								add = false;
								break;
							}
						}
					}
					if(add){
						mids.add(sessionQuote.getSessionId());
						updateProductSeparateList(productSeparateList,quote);
					}
				}				
			}			
		}
		
		return productSeparateList;		
	}
	
	private void updateProductSeparateList(List<Map<String,Object>> productSeparateList, Quote quote){
		String productId = quote.getCommonData().getProductId();
		boolean notExists = true;
		int indx;
		for(indx=0;indx<productSeparateList.size();indx++){
			if(productSeparateList.get(indx).get(PRODUCT_ID)!=null){
				notExists = false;
				break;
			}
		}
		if(notExists){
			Map<String,Object> m = new HashMap<>();
			m.put(PRODUCT_ID, productId);
			m.put(QUOTE_COUNT,1);
			productSeparateList.add(m);
		}else{
			int existingCount = (int) productSeparateList.get(indx).get(QUOTE_COUNT);
			productSeparateList.get(indx).put(QUOTE_COUNT, ++existingCount);
		}
	}
	
	private List<SessionQuote> sessionQuoteQuery(Quote quote){
		Query query = new Query();
		query.addCriteria(where("quotes.$id").is(new ObjectId(quote.getId())));
		return mongoOperations.find(query, SessionQuote.class, "sessionQuote");
	}
	
	private List<Quote> quoteQuery(LocalDate startDate, LocalDate endDate){
		Query query = new Query();
		query.addCriteria(where("insureds.startDate").gte(getDateFromLocalDate(startDate)).lte(getDateFromLocalDate(endDate)));
		return mongoOperations.find(query, Quote.class, "quote");
	}
	
	private Date getDateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
