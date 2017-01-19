package th.co.krungthaiaxa.api.elife.commission.service;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.CommissionCalculationSessionException;
import th.co.krungthaiaxa.api.common.filter.ExceptionTranslator;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculation;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroup;
import th.co.krungthaiaxa.api.elife.commission.data.cdb.CDBPolicyCommissionEntity;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionCalculationSessionRepository;
import th.co.krungthaiaxa.api.elife.commission.util.CommissionUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PreviousPolicy;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 9/5/16.
 */
@Service
public class CommissionCalculationSessionService {

    public final static Logger LOGGER = LoggerFactory.getLogger(CommissionCalculationSessionService.class);
    private final CommissionPlanService commissionPlanService;
    private final CommissionCalculationSessionRepository commissionCalculationSessionRepository;
    private final CDBRepository cdbRepository;
    private final PolicyRepository policyRepository;
    private final ExceptionTranslator exceptionTranslator;

    private static final String RESULT_CODE_SESSION_SUCCESS = "0000";
    private static final String RESULT_CODE_SESSION_WRONG_SETTING = "1000";
    private static final String RESULT_CODE_SESSION_INTERNAL_ERROR = "9000";

    private static final String RESULT_CODE_POLICY_SUCCESS = "0000";
    private static final String RESULT_CODE_INTERNAL_ERROR = "9000";

//    @Autowired
//    @Qualifier("cdbDataSource")
//    private DataSource cdbDataSource;

    private final String FY = "FY";
    private final String OV = "OV";
    private final String TARGET_ENTITY_AFF = "AFFILIATE";
    private final String TARGET_ENTITY_COM = "COMPANY";
    private final String TARGET_ENTITY_TSR = "TSR";
    private final String TARGET_ENTITY_MKR = "MKR";
    private final String TARGET_ENTITY_DIS = "DISTRIBUTION";
    private final String BLANK = "";
    private final String NEW = "NEW";
    private final String EXISTING = "EXISTING";
    private final String NULL = "NULL";
    private final DecimalFormat DCF = new DecimalFormat("#0.0000");

    @Inject
    public CommissionCalculationSessionService(CommissionPlanService commissionPlanService, CommissionCalculationSessionRepository commissionCalculationSessionRepository, CDBRepository cdbRepository, PolicyRepository policyRepository, ExceptionTranslator exceptionTranslator) {
        this.commissionPlanService = commissionPlanService;
        this.commissionCalculationSessionRepository = commissionCalculationSessionRepository;
        this.cdbRepository = cdbRepository;
        this.policyRepository = policyRepository;
        this.exceptionTranslator = exceptionTranslator;
    }

    //santi : for get list of calculated commission
    public List<CommissionCalculationSession> findAllCommissionCalculationSessions() {
        return commissionCalculationSessionRepository.findAllByOrderByCreatedDateTimeAsc();
    }

    //santi : for trigger calculation commission
    public void calculateCommissionForPolicies() {
        Instant start = LogUtil.logStarting("[Commission-calculation][start]");
        CommissionCalculationSession commissionResult = new CommissionCalculationSession();
        String resultCode;
        String resultMessage;
        try {
            Instant previousMonth = DateTimeUtil.plusMonths(Instant.now(), -1);
            commissionResult.setCommissionDate(previousMonth);

            List<CommissionPlan> commissionPlans = commissionPlanService.findAll();
            commissionResult.setCommissionPlans(commissionPlans);

            List<String> unitCodes = commissionPlans.stream().map(sc -> sc.getUnitCode()).distinct().collect(Collectors.toList());//list of UnitCode
            List<String> planCodes = commissionPlans.stream().map(sc -> sc.getPlanCode()).distinct().collect(Collectors.toList());

            List<CommissionCalculation> commissionCalculations = new ArrayList<>();
            if (!unitCodes.isEmpty() && !planCodes.isEmpty()) {
                List<CDBPolicyCommissionEntity> policiesCDB = cdbRepository.findPolicyCommissionsByUnitCodesAndPlanCodes(unitCodes, planCodes); //jdbcTemplate.queryForList(generateSql(channelIdsNoDup, planCodesNoDup), generateParameters(channelIdsNoDup, planCodesNoDup));
                Map<String, PreviousPolicy> previousPoliciesCache = new HashMap<>();
                Map<String, String> agentCodeStatusCache = new HashMap<>();
                for (CDBPolicyCommissionEntity policyCDB : policiesCDB) {
                    CommissionCalculation commissionCalculation = calculateCommissionForPolicy(policyCDB, commissionPlans, previousPoliciesCache, agentCodeStatusCache);
                    commissionCalculations.add(commissionCalculation);
                }
                commissionResult.setCommissionCalculations(commissionCalculations);
                resultCode = RESULT_CODE_SESSION_SUCCESS;
                resultMessage = "Success";
            } else {
                resultCode = RESULT_CODE_SESSION_WRONG_SETTING;
                resultMessage = String.format("Not found UnitCodes or PlanCodes in Commission Setting: UnitCodes: %s, PlanCodes: %s", unitCodes.size(), planCodes.size());
                LOGGER.debug("[Commission] {}", resultMessage);
            }
        } catch (Exception e) {
            Error error = exceptionTranslator.processUnknownInternalException(e);
            resultCode = error.getCode();
            resultMessage = error.getUserMessage();
        }
        commissionResult.setResultCode(resultCode);
        commissionResult.setResultMessage(resultMessage);
        commissionCalculationSessionRepository.save(commissionResult);
        LogUtil.logFinishing(start, "[Commission-calculation][finish]");
    }

    private CommissionCalculation calculateCommissionForPolicy(CDBPolicyCommissionEntity policyCDB, List<CommissionPlan> commissionPlans, Map<String, PreviousPolicy> previousPoliciesCache, Map<String, String> agentCodeStatusCache) {
        CommissionCalculation commissionCalculation = new CommissionCalculation();
        String policyNumber = policyCDB.getPolicyNumber();
        try {
            copyData(policyCDB, commissionCalculation);
            PreviousPolicy previousPolicy = previousPoliciesCache.get(policyNumber);
            if (previousPolicy == null) {
                Optional<PreviousPolicy> previousPolicyOptional = findPreviousPolicyOfSameInsured(policyCDB.getPolicyNumber());
                previousPolicy = previousPolicyOptional.isPresent() ? previousPolicyOptional.get() : null;
                if (previousPolicy != null) {
                    previousPoliciesCache.put(policyNumber, previousPolicy);
                }
            }
            setAgentDataAndCustomerCategoryToCommissionCalculation(commissionCalculation, previousPolicy, agentCodeStatusCache);
            CommissionPlan commissionPlan = CommissionUtil.findCommissionPlan(getProperAgentCodeNumber(commissionCalculation.getAgentCode(), 6), commissionCalculation.getPlanCode(), commissionCalculation.getCustomerCategory(), commissionPlans);
            calculateCommissionRate(commissionCalculation, commissionPlan);
            commissionCalculation.setResultCode(RESULT_CODE_POLICY_SUCCESS);
            commissionCalculation.setResultMessage("Success");
        } catch (Exception ex) {
            String msg = String.format("Error when calculation commission for policy '%s': %s", policyNumber, ex.getMessage());
            LOGGER.error(msg, ex);
            commissionCalculation.setResultCode(RESULT_CODE_INTERNAL_ERROR);
            commissionCalculation.setResultMessage(msg);
        }
        return commissionCalculation;
    }

    private Optional<PreviousPolicy> findPreviousPolicyOfSameInsured(String policyNumber) {
        Policy policy = policyRepository.findByPolicyId(policyNumber);
        Optional<PreviousPolicy> previousPolicyOptional;
        PreviousPolicy previousPolicy = null;
        if (policy != null) {
            Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
            mainInsured.setNotSearchedPreviousPolicy(false);
            previousPolicy = mainInsured.getPreviousPolicy();
            if (previousPolicy == null && mainInsured.isNotSearchedPreviousPolicy()) {
                //Don't load previous policy from eLife DB (mainInsured.getLastActivatingPreviousPolicy()) because that previous policy might be was deactivated.
                //So we need to recheck with CDB again.
                previousPolicyOptional = cdbRepository.findLastActivatingPreviousPolicy(ProductUtils.getRegistrationId(mainInsured), mainInsured.getPerson().getBirthDate());
                previousPolicy = previousPolicyOptional.isPresent() ? previousPolicyOptional.get() : null;
                mainInsured.setPreviousPolicy(previousPolicy);
                mainInsured.setNotSearchedPreviousPolicy(false);
                policyRepository.save(policy);
            }
        }
        if (previousPolicy != null) {
            previousPolicyOptional = Optional.of(previousPolicy);
        } else {
            previousPolicyOptional = Optional.empty();
        }
        return previousPolicyOptional;
    }

    private void setAgentDataAndCustomerCategoryToCommissionCalculation(CommissionCalculation commissionCalculation, PreviousPolicy previousPolicy, Map<String, String> agentStatusesCache) {
        String previousPolicyNumber;
        String customerCategory = NEW;
        if (previousPolicy != null) {
            previousPolicyNumber = previousPolicy.getPolicyNumber();
            if (!StringUtils.isBlank(previousPolicyNumber)) {
                customerCategory = EXISTING;
            }
            String agentCode1 = previousPolicy.getAgentCode1();
            String agentCode2 = previousPolicy.getAgentCode2();
            commissionCalculation.setPreviousPolicyNo(previousPolicy.getPolicyNumber());
            commissionCalculation.setExistingAgentCode1(agentCode1);
            commissionCalculation.setExistingAgentCode2(agentCode2);
            if (StringUtils.isNotBlank(agentCode1)) {
                String agentStatus = getAgentStatus(agentCode1, agentStatusesCache);
                commissionCalculation.setExistingAgentStatus1(agentStatus);
            }
            if (StringUtils.isNotBlank(agentCode2)) {
                String agentStatus = getAgentStatus(agentCode2, agentStatusesCache);
                commissionCalculation.setExistingAgentStatus2(agentStatus);
            }
        }
        commissionCalculation.setCustomerCategory(customerCategory);
    }

    private String getAgentStatus(String agentCode, Map<String, String> agentCodeStatusCache) {
        String agentStatus = agentCodeStatusCache.get(agentCode);
        if (agentStatus == null) {
            agentStatus = cdbRepository.getExistingAgentCodeStatus(getProperAgentCodeNumber(agentCode, 14));
            agentCodeStatusCache.put(agentCode, agentStatus);
        }
        return agentStatus;
    }

    private void copyData(CDBPolicyCommissionEntity policyCDB, CommissionCalculation commissionCalculation) {
        commissionCalculation.setPolicyNumber(policyCDB.getPolicyNumber());
        commissionCalculation.setPolicyStatus(policyCDB.getPolicyStatus());
        commissionCalculation.setPlanCode(policyCDB.getPlanCode());
        commissionCalculation.setPaymentCode(policyCDB.getPaymentCode());
        commissionCalculation.setAgentCode(policyCDB.getAgentCode());
        commissionCalculation.setFirstYearPremium(policyCDB.getFirstYearPremium());
        commissionCalculation.setFirstYearCommission(policyCDB.getFirstYearCommission());
    }

    private void calculateCommissionRate(CommissionCalculation commissionCalculation, CommissionPlan commissionPlan) {
        //fy
        CommissionTargetGroup targetGroupFY = CommissionUtil.findCommissionTargetGroup(FY, commissionPlan.getTargetGroups());
        CommissionTargetGroup targetGroupOV = CommissionUtil.findCommissionTargetGroup(OV, commissionPlan.getTargetGroups());
        commissionCalculation.setFyAffiliateCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_AFF, targetGroupFY).getPercentage()) / 100));
        Double fDisComm = (commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_DIS, targetGroupFY).getPercentage()) / 100;
        if (StringUtil.isBlank(commissionCalculation.getExistingAgentCode2())) {
            commissionCalculation.setFyDistribution1Commission(convertFormat(fDisComm));
            commissionCalculation.setFyDistribution2Commission(0.0);
        } else {
            Double disCommSplit = fDisComm / 2;
            commissionCalculation.setFyDistribution1Commission(convertFormat(disCommSplit));
            commissionCalculation.setFyDistribution2Commission(convertFormat(disCommSplit));
        }
        commissionCalculation.setFyTsrCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_TSR, targetGroupFY).getPercentage()) / 100));
        commissionCalculation.setFyMarketingCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_MKR, targetGroupFY).getPercentage()) / 100));
        commissionCalculation.setFyCompanyCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_COM, targetGroupFY).getPercentage()) / 100));

        //ov
        commissionCalculation.setOvAffiliateCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_AFF, targetGroupOV).getPercentage()) / 100));
        Double oDisComm = (commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_DIS, targetGroupOV).getPercentage()) / 100;
        if (StringUtil.isBlank(commissionCalculation.getExistingAgentCode2())) {
            commissionCalculation.setOvDistribution1Commission(convertFormat(oDisComm));
            commissionCalculation.setOvDistribution2Commission(0.0);
        } else {
            Double disCommSplit = oDisComm / 2;
            commissionCalculation.setOvDistribution1Commission(convertFormat(disCommSplit));
            commissionCalculation.setOvDistribution2Commission(convertFormat(disCommSplit));
        }
        commissionCalculation.setOvTsrCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_TSR, targetGroupOV).getPercentage()) / 100));
        commissionCalculation.setOvMarketingCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_MKR, targetGroupOV).getPercentage()) / 100));
        commissionCalculation.setOvCompanyCommission(convertFormat((commissionCalculation.getFirstYearCommission() * CommissionUtil.findTargetEntities(TARGET_ENTITY_COM, targetGroupOV).getPercentage()) / 100));

        //commission rate
        commissionCalculation.setFyAffiliateRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_AFF, targetGroupFY).getPercentage()));
        commissionCalculation.setFyDistributionRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_DIS, targetGroupFY).getPercentage()));
        commissionCalculation.setFyTsrRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_TSR, targetGroupFY).getPercentage()));
        commissionCalculation.setFyMarketingRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_MKR, targetGroupFY).getPercentage()));
        commissionCalculation.setFyCompanyRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_COM, targetGroupFY).getPercentage()));
        commissionCalculation.setOvAffiliateRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_AFF, targetGroupOV).getPercentage()));
        commissionCalculation.setOvDistributionRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_DIS, targetGroupOV).getPercentage()));
        commissionCalculation.setOvTsrRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_TSR, targetGroupOV).getPercentage()));
        commissionCalculation.setOvMarketingRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_MKR, targetGroupOV).getPercentage()));
        commissionCalculation.setOvCompanyRate(convertFormat(CommissionUtil.findTargetEntities(TARGET_ENTITY_COM, targetGroupOV).getPercentage()));
    }

    private Double convertFormat(Double value) {
        String formatted = DCF.format(value);
        return Double.parseDouble(formatted);
    }

    private String getProperAgentCodeNumber(String agentCode, int cutPosition) {
        String newAgentCode = "00000000000000" + agentCode;
        return newAgentCode.substring(newAgentCode.length() - 14, newAgentCode.length()).substring(0, cutPosition);
    }

    public CommissionCalculationSession validateExistCalculationSession(String calculationSessionId) {
        CommissionCalculationSession commissionCalculationSession = commissionCalculationSessionRepository.findOne(calculationSessionId);
        if (commissionCalculationSession == null) {
            throw new CommissionCalculationSessionException("Not found calculation session " + calculationSessionId);
        }
        return commissionCalculationSession;
    }

    @Deprecated
    private String getRowId(LocalDateTime now) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return "C" + now.format(formatter);
    }

}
