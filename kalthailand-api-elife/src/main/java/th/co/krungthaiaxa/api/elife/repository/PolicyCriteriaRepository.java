package th.co.krungthaiaxa.api.elife.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
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
            Boolean nonEmptyAgentCode, LocalDate fromDate, LocalDate toDate, PeriodicityCode periodicityCode, Integer atpModeId, Pageable pageable) {
        Query query = getQuery(policyId, productType, status, nonEmptyAgentCode, fromDate, toDate, periodicityCode, atpModeId);
        limitFiels(query);
        List<Policy> policies = mongoOperations.find(query.with(pageable), Policy.class, "policy");
        Long nbRecords = mongoOperations.count(query, Policy.class, "policy");
        return new PageImpl<>(policies, pageable, nbRecords);
    }

    public List<Policy> findPolicies(String policyId, ProductType productType, PolicyStatus status,
            Boolean nonEmptyAgentCode, LocalDate fromDate, LocalDate toDate, PeriodicityCode periodicityCode, Integer atpModeId) {
        Query query = getQuery(policyId, productType, status, nonEmptyAgentCode, fromDate, toDate, periodicityCode, atpModeId);
        limitFiels(query);
        return mongoOperations.find(query, Policy.class, "policy");
    }

    private void limitFiels(Query query) {
        query.fields()
                .include("policyId")
                .include("commonData.productId")
                .include("premiumsData.financialScheduler.modalAmount")
                .include("status")
                .include("insureds.startDate")
                .include("insureds.insuredPreviousInformations")
                .include("insureds.mainInsuredIndicator")
                .include("insureds.person.lineId")
                .include("insureds.person.lineUserId")
                .include("validationAgentCode")
                .include("validationDateTime")
                .include("premiumsData.financialScheduler.periodicity.code")
                .include("premiumsData.financialScheduler.atpMode")
        ;
    }

    private Query getQuery(String policyId, ProductType productType, PolicyStatus status, Boolean nonEmptyAgentCode, LocalDate fromDate, LocalDate toDate, PeriodicityCode periodicityCode, Integer atpModeId) {
        Query query = new Query();
        if (StringUtils.isNotEmpty(policyId)) {
            query.addCriteria(where("policyId").regex(".*" + policyId + ".*"));
        }
        if (productType != null) {
            query.addCriteria(where("commonData.productId").is(productType.getLogicName()));
        }
        if (status != null) {
            query.addCriteria(where("status").is(status));
        }
        if (nonEmptyAgentCode != null) {
            if (nonEmptyAgentCode) {
                query.addCriteria(where("insureds.insuredPreviousInformations").not().size(0));
            } else {
                query.addCriteria(where("insureds.insuredPreviousInformations").size(0));
            }
        }
        if (fromDate != null && toDate == null) {
            query.addCriteria(where("insureds.startDate").gte(getDateFromLocalDate(fromDate)));
        } else if (fromDate == null && toDate != null) {
            query.addCriteria(where("insureds.startDate").lte(getDateFromLocalDate(toDate)));
        } else if (fromDate != null && toDate != null) {
            query.addCriteria(where("insureds.startDate").gte(getDateFromLocalDate(fromDate)).lte(getDateFromLocalDate(toDate)));
        }

        if (periodicityCode != null) {
            query.addCriteria(where("premiumsData.financialScheduler.periodicity.code").is(periodicityCode));
        }
        if (atpModeId != null) {
            query.addCriteria(where("premiumsData.financialScheduler.atpMode").is(atpModeId));
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
