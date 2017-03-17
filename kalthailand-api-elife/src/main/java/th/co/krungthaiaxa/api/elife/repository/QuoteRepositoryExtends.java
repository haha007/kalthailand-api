package th.co.krungthaiaxa.api.elife.repository;

import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.QuoteMid;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteRepositoryExtends.class);
    private static final String CREATION_DATE_TIME_FIELD = "creationDateTime";
    private static final String PRODUCT_ID_FIELD = "productId";
    private static final String LINE_ID_FIELD = "lineId";
    private static final String POLICY_ID = "policyId";

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
        final Fields groupFields = Fields.from(
                Fields.field(PRODUCT_ID_FIELD, "commonData.productId"),
                Fields.field(LINE_ID_FIELD, "insureds.person.lineId"));

        final Aggregation agg = newAggregation(
                group(groupFields)
                        .last(CREATION_DATE_TIME_FIELD).as(CREATION_DATE_TIME_FIELD)
                        .last(POLICY_ID).as(POLICY_ID),
                match(Criteria
                        .where(CREATION_DATE_TIME_FIELD)
                        .gte(startDateTime)
                        .lte(endDateTime)
                        .and("_id.productId").in(listProductType)),
                sort(Sort.Direction.ASC, CREATION_DATE_TIME_FIELD));
        final AggregationResults<DBObject> groupResults =
                mongoTemplate.aggregate(agg, Quote.class, DBObject.class);

        return groupResults.getMappedResults()
                .stream()
                .map(this::parseDbObjectToQuoteMid)
                .collect(Collectors.toList());
    }

    /**
     * Parse DBObject to QuoteMid.
     *
     * @param dbObject Aggregated data
     * @return QuoteMid
     */
    private QuoteMid parseDbObjectToQuoteMid(final DBObject dbObject) {
        try {
            final List<String> mids = (List<String>) dbObject.get(LINE_ID_FIELD);
            final String productId = String.valueOf(dbObject.get(PRODUCT_ID_FIELD));
            final LocalDateTime creationDateTime = DateTimeUtil
                    .toThaiLocalDateTime(((Date) dbObject.get(CREATION_DATE_TIME_FIELD)).toInstant());
            return new QuoteMid(productId, mids.stream().collect(Collectors.joining(", ")), creationDateTime);
        } catch (ClassCastException ex) {
            LOGGER.error("Could not get MID for policy Id {}", String.valueOf(dbObject.get(POLICY_ID)));
            return new QuoteMid();
        }
    }

}
