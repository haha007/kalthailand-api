package th.co.krungthaiaxa.api.elife.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.line.v2.model.PolicyIdLineUserIdMap;

import javax.inject.Inject;
import java.util.List;

/**
 * @author tuong.le on 10/24/17.
 */
@Repository
public class PolicyRepositoryExtends {
    public static Logger LOGGER = LoggerFactory.getLogger(PolicyRepositoryExtends.class);

    @Inject
    private MongoOperations mongoOperations;

    @Inject
    private QuoteRepository quoteRepository;

    public BulkWriteResult bulkUpdateLineUserIdByPolicyId(final List<PolicyIdLineUserIdMap> newPolicys) {
        DBCollection dbCollection = mongoOperations.getCollection("policy");
        BulkWriteOperation bulkWriteOperationBuilder = dbCollection.initializeOrderedBulkOperation();
        for (final PolicyIdLineUserIdMap policy : newPolicys) {
            bulkWriteOperationBuilder.find(new BasicDBObject("policyId", policy.getPolicyId()))
                    .updateOne(new BasicDBObject("$set", new BasicDBObject("insureds.0.person.lineUserId", policy.getUserLineId())));
        }
        return bulkWriteOperationBuilder.execute();
    }

}
