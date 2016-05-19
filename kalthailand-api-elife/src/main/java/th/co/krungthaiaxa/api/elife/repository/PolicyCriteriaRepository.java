package th.co.krungthaiaxa.api.elife.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class PolicyCriteriaRepository {
    @Inject
    private MongoOperations mongoOperations;

    public Page<Policy> findPolicies(String policyId, ProductType productType, PolicyStatus status,
                                     Boolean nonEmptyAgentCode, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        Query query = getQuery(policyId, productType, status, nonEmptyAgentCode, fromDate, toDate);

        List<Policy> policies = mongoOperations.find(query.with(pageable), Policy.class, "policy");
        Long nbRecords = mongoOperations.count(query, Policy.class, "policy");
        return new PageImpl<>(policies, pageable, nbRecords);
    }

    public List<Policy> findPolicies(String policyId, ProductType productType, PolicyStatus status,
                                Boolean nonEmptyAgentCode, LocalDate fromDate, LocalDate toDate) {
        Query query = getQuery(policyId, productType, status, nonEmptyAgentCode, fromDate, toDate);
        query.fields()
                .include("policyId")
                .include("commonData.productId")
                .include("premiumsData.financialScheduler.modalAmount")
                .include("status")
                .include("insureds.startDate")
                .include("insureds.insuredPreviousAgents")
                .include("validationAgentCode");
        return mongoOperations.find(query, Policy.class, "policy");
    }

    private Query getQuery(String policyId, ProductType productType, PolicyStatus status, Boolean nonEmptyAgentCode, LocalDate fromDate, LocalDate toDate) {
        Query query = new Query();
        if (StringUtils.isNotEmpty(policyId)) {
            query.addCriteria(where("policyId").regex(".*" + policyId + ".*"));
        }
        if (productType != null) {
            query.addCriteria(where("commonData.productId").is(productType.getName()));
        }
        if (status != null) {
            query.addCriteria(where("status").is(status));
        }
        if (nonEmptyAgentCode != null) {
            if (nonEmptyAgentCode) {
                query.addCriteria(where("insureds.insuredPreviousAgents").not().size(0));
            } else {
                query.addCriteria(where("insureds.insuredPreviousAgents").size(0));
            }
        }
        if (fromDate != null && toDate == null) {
            query.addCriteria(where("insureds.startDate").gte(getDateFromLocalDate(fromDate)));
        } else if (fromDate == null && toDate != null) {
            query.addCriteria(where("insureds.startDate").lte(getDateFromLocalDate(toDate)));
        } else if (fromDate != null && toDate != null) {
            query.addCriteria(where("insureds.startDate").gte(getDateFromLocalDate(fromDate)).lte(getDateFromLocalDate(toDate)));
        }
        return query;
    }

    public long count() {
        return mongoOperations.getCollection("policy").count();
    }

    private Date getDateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}