package th.co.krungthaiaxa.api.elife.repository;

import com.mongodb.DBObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.QuoteMid;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

/**
 * @author tuong.le on 3/10/17.
 */
@Repository
public class QuoteRepositoryExtends {
    private static final String CREATION_DATE_TIME_FIELD = "creationDateTime";

    @Inject
    private MongoTemplate mongoTemplate;

    /**
     * Search Quotes include MID and creationDateTime distinct by productType and Mid
     * filter Latest Quote.
     *
     * @param productTypes  list of ProductTypes
     * @param startDateTime start date time
     * @param endDateTime   end date time
     * @return list of Customized Quote include productType, mid, creationDateTime.
     */
    public List<QuoteMid> getDistinctQuote(final Collection<ProductType> productTypes,
                                           final LocalDateTime startDateTime,
                                           final LocalDateTime endDateTime) {
        final List<String> listProductType =
                productTypes.stream().map(ProductType::getLogicName).collect(Collectors.toList());
        final Aggregation agg = newAggregation(
                group("commonData.productId", "insureds.person.lineId")
                        .last(CREATION_DATE_TIME_FIELD).as(CREATION_DATE_TIME_FIELD),
                match(Criteria
                        .where(CREATION_DATE_TIME_FIELD)
                        .gte(startDateTime)
                        .lte(endDateTime)
                        .and("_id.productId").in(listProductType)),
                sort(Sort.Direction.DESC, CREATION_DATE_TIME_FIELD));
        AggregationResults<DBObject> groupResults =
                mongoTemplate.aggregate(agg, Quote.class, DBObject.class);

        return groupResults.getMappedResults()
                .stream()
                .map(this::parseDbObjectToQuoteMid)
                .collect(Collectors.toList());
    }

    /**
     * Parse DBObject ot QuoteMid.
     *
     * @param dbObject Aggregated data
     * @return QuoteMid
     */
    private QuoteMid parseDbObjectToQuoteMid(final DBObject dbObject) {
        final String productId = dbObject.get("productId").toString();
        final Set<String> midSet = Objects.isNull(dbObject.get("person.lineId"))
                ? Collections.emptySet()
                : (Set<String>) dbObject.get("person.lineId");
        final LocalDateTime creationDateTime = DateTimeUtil
                .toThaiLocalDateTime(((Date) dbObject.get(CREATION_DATE_TIME_FIELD)).toInstant());
        return new QuoteMid(productId, midSet.stream().collect(Collectors.joining(", ")), creationDateTime);
    }

}
