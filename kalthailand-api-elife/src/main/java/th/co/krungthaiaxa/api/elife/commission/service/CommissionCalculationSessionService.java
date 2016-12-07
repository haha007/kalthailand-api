package th.co.krungthaiaxa.api.elife.commission.service;

import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.CommissionCalculationSessionException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculation;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntity;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroup;
import th.co.krungthaiaxa.api.elife.commission.data.cdb.CDBPolicyCommissionEntity;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionCalculationSessionRepository;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionResultRepository;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private final CommissionResultRepository commissionResultRepository;
    private final PolicyRepository policyRepository;

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
    public CommissionCalculationSessionService(CommissionPlanService commissionPlanService, CommissionCalculationSessionRepository commissionCalculationSessionRepository, CDBRepository cdbRepository, PolicyRepository policyRepository,
            CommissionResultRepository commissionResultRepository) {
        this.commissionPlanService = commissionPlanService;
        this.commissionCalculationSessionRepository = commissionCalculationSessionRepository;
        this.cdbRepository = cdbRepository;
        this.policyRepository = policyRepository;
        this.commissionResultRepository = commissionResultRepository;
    }

    //santi : for get list of calculated commission
    public List<CommissionCalculationSession> findAllCommissionCalculationSessions() {
        return commissionCalculationSessionRepository.findAllByOrderByCreatedDateTimeAsc();
//        return commissionResultRepository.findAllByOrderByCreatedDateTimeAsc();
    }

    //santi : for trigger calculation commission
    public void calculateCommissionForPolicies() {
        LOGGER.debug("[Commission] start");

        LocalDateTime nowDate = LocalDateTime.now();

        List<CommissionPlan> commissionPlans = commissionPlanService.findAll();
        CommissionCalculationSession commissionResult = new CommissionCalculationSession();
        Instant previousMonth = DateTimeUtil.plusMonths(Instant.now(), -1);
        commissionResult.setCommissionDate(previousMonth);
        commissionResult.setCreatedDateTime(nowDate);
        commissionResult.setCommissionPlans(commissionPlans);

        List<String> unitCodes = commissionPlans.stream().map(sc -> sc.getUnitCode()).distinct().collect(Collectors.toList());//list of UnitCode
        List<String> planCodes = commissionPlans.stream().map(sc -> sc.getPlanCode()).distinct().collect(Collectors.toList());

        List<CommissionCalculation> listCommissionCalculated = new ArrayList<>();

        if (!unitCodes.isEmpty() && !planCodes.isEmpty()) {
            try {
                List<CDBPolicyCommissionEntity> policiesCDB = cdbRepository.findPoliciesByChannelIdsAndPaymentModeIds(unitCodes, planCodes); //jdbcTemplate.queryForList(generateSql(channelIdsNoDup, planCodesNoDup), generateParameters(channelIdsNoDup, planCodesNoDup));
                for (CDBPolicyCommissionEntity policyCDB : policiesCDB) {
                    CommissionCalculation commissionCalculation = calculateCommissionForPolicy(policyCDB, commissionPlans);
                    listCommissionCalculated.add(commissionCalculation);
                }
                commissionResult.setCommissionCalculations(listCommissionCalculated);
                commissionResult.setUpdatedDateTime(LocalDateTime.now());
                //update
            } catch (Exception e) {
                LOGGER.error("Unable to query " + e.getMessage(), e);
            }
        } else {
            LOGGER.debug("[Commission] Not found channel Ids and planCodes in setting.....");
        }
        commissionCalculationSessionRepository.save(commissionResult);
        LOGGER.debug("[Commission] finish");
    }

    private static final String CODE_SUCCESS = "0000";
    private static final String CODE_INTERNAL_ERROR = "9000";

    private CommissionCalculation calculateCommissionForPolicy(CDBPolicyCommissionEntity policyCDB, List<CommissionPlan> commissionPlans) {
        CommissionCalculation commissionCalculation = new CommissionCalculation();
        String policyNumber = policyCDB.getPolicyNumber();
        Policy policy = policyRepository.findByPolicyId(String.valueOf(policyCDB.getPolicyNumber()));
        if (policy != null) {
            try {
                calculateCommissionForPolicy(commissionCalculation, policy, policyCDB, commissionPlans);
                commissionCalculation.setResultCode(CODE_SUCCESS);
                commissionCalculation.setResultMessage("Success");
            } catch (Exception ex) {
                String msg = String.format("Error when calculation commission for policy '%s': %s", policyNumber, ex.getMessage());
                LOGGER.error(msg, ex);
                commissionCalculation.setResultCode(CODE_INTERNAL_ERROR);
                commissionCalculation.setResultMessage(msg);
            }
        } else {
            BeanUtils.copyProperties(policyCDB, commissionCalculation);
            commissionCalculation.setResultCode(CODE_INTERNAL_ERROR);
            commissionCalculation.setResultMessage(String.format("Not found policy '%s' in elife.", policyNumber));
        }
        return commissionCalculation;
    }

    private void calculateCommissionForPolicy(CommissionCalculation commissionCalculation, Policy policy, CDBPolicyCommissionEntity policyCDB, List<CommissionPlan> commissionPlans) {

        //cdb information
        commissionCalculation.setPolicyNumber(String.valueOf(policyCDB.getPolicyNumber()));
        commissionCalculation.setPolicyStatus(String.valueOf(policyCDB.getPolicyStatus()));
        commissionCalculation.setPlanCode(String.valueOf(policyCDB.getPlanCode()));
        commissionCalculation.setPaymentCode(String.valueOf(policyCDB.getPaymentCode()));
        commissionCalculation.setAgentCode(String.valueOf(policyCDB.getAgentCode()));
        commissionCalculation.setFirstYearPremium(convertFormat(Double.valueOf(String.valueOf(policyCDB.getFirstYearPremium()))));
        commissionCalculation.setFirstYearCommission(convertFormat(Double.valueOf(String.valueOf(policyCDB.getFirstYearCommission()))));

        //previously information
        Insured mainInsured = ProductUtils.getFirstInsured(policy);
        List<String> insuredPreviousInformations = mainInsured.getInsuredPreviousInformations();
        if (insuredPreviousInformations.size() == 0) {
            commissionCalculation.setCustomerCategory(NEW);
            commissionCalculation.setPreviousPolicyNo(BLANK);
            commissionCalculation.setExistingAgentCode1(BLANK);
            commissionCalculation.setExistingAgentCode1Status(BLANK);
            commissionCalculation.setExistingAgentCode2(BLANK);
            commissionCalculation.setExistingAgentCode2Status(BLANK);
        } else {
            commissionCalculation.setCustomerCategory((insuredPreviousInformations.get(0).equals(NULL) ? NEW : EXISTING));
            commissionCalculation.setPreviousPolicyNo((insuredPreviousInformations.get(0).equals(NULL) ? BLANK : insuredPreviousInformations.get(0)));
            commissionCalculation.setExistingAgentCode1((insuredPreviousInformations.get(1).equals(NULL) ? BLANK : insuredPreviousInformations.get(1)));
            commissionCalculation.setExistingAgentCode1Status((commissionCalculation.getExistingAgentCode1().equals(BLANK) ? BLANK : cdbRepository.getExistingAgentCodeStatus(getProperAgentCodeNumber(commissionCalculation.getExistingAgentCode1(), 14))));
            commissionCalculation.setExistingAgentCode2((insuredPreviousInformations.get(2).equals(NULL) ? BLANK : insuredPreviousInformations.get(2)));
            commissionCalculation.setExistingAgentCode2Status((commissionCalculation.getExistingAgentCode2().equals(BLANK) ? BLANK : cdbRepository.getExistingAgentCodeStatus(getProperAgentCodeNumber(commissionCalculation.getExistingAgentCode2(), 14))));
        }

        //calculation commission
        CommissionPlan commissionPlan = findCommissionPlan(getProperAgentCodeNumber(commissionCalculation.getAgentCode(), 6), commissionCalculation.getPlanCode(), commissionCalculation.getCustomerCategory(), commissionPlans);
        calculateCommissionRate(commissionCalculation, commissionPlan);
    }

    private void calculateCommissionRate(CommissionCalculation commissionCalculation, CommissionPlan commissionPlan) {
        //fy
        CommissionTargetGroup targetGroupFY = getCommissionTargetGroup(FY, commissionPlan.getTargetGroups());
        CommissionTargetGroup targetGroupOV = getCommissionTargetGroup(OV, commissionPlan.getTargetGroups());
        commissionCalculation.setFyAffiliateCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_AFF, targetGroupFY).getPercentage()) / 100));
        Double fDisComm = (commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_DIS, targetGroupFY).getPercentage()) / 100;
        if (StringUtil.isBlank(commissionCalculation.getExistingAgentCode2())) {
            commissionCalculation.setFyDistribution1Commission(convertFormat(fDisComm));
            commissionCalculation.setFyDistribution2Commission(0.0);
        } else {
            Double disCommSplit = fDisComm / 2;
            commissionCalculation.setFyDistribution1Commission(convertFormat(disCommSplit));
            commissionCalculation.setFyDistribution2Commission(convertFormat(disCommSplit));
        }
        commissionCalculation.setFyTsrCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_TSR, targetGroupFY).getPercentage()) / 100));
        commissionCalculation.setFyMarketingCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_MKR, targetGroupFY).getPercentage()) / 100));
        commissionCalculation.setFyCompanyCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_COM, targetGroupFY).getPercentage()) / 100));

        //ov
        commissionCalculation.setOvAffiliateCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_AFF, targetGroupOV).getPercentage()) / 100));
        Double oDisComm = (commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_DIS, targetGroupOV).getPercentage()) / 100;
        if (StringUtil.isBlank(commissionCalculation.getExistingAgentCode2())) {
            commissionCalculation.setOvDistribution1Commission(convertFormat(oDisComm));
            commissionCalculation.setOvDistribution2Commission(0.0);
        } else {
            Double disCommSplit = oDisComm / 2;
            commissionCalculation.setOvDistribution1Commission(convertFormat(disCommSplit));
            commissionCalculation.setOvDistribution2Commission(convertFormat(disCommSplit));
        }
        commissionCalculation.setOvTsrCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_TSR, targetGroupOV).getPercentage()) / 100));
        commissionCalculation.setOvMarketingCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_MKR, targetGroupOV).getPercentage()) / 100));
        commissionCalculation.setOvCompanyCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_COM, targetGroupOV).getPercentage()) / 100));

        //commission rate
        commissionCalculation.setFyAffiliateRate(convertFormat(getTargetEntities(TARGET_ENTITY_AFF, targetGroupFY).getPercentage()));
        commissionCalculation.setFyDistributionRate(convertFormat(getTargetEntities(TARGET_ENTITY_DIS, targetGroupFY).getPercentage()));
        commissionCalculation.setFyTsrRate(convertFormat(getTargetEntities(TARGET_ENTITY_TSR, targetGroupFY).getPercentage()));
        commissionCalculation.setFyMarketingRate(convertFormat(getTargetEntities(TARGET_ENTITY_MKR, targetGroupFY).getPercentage()));
        commissionCalculation.setFyCompanyRate(convertFormat(getTargetEntities(TARGET_ENTITY_COM, targetGroupFY).getPercentage()));
        commissionCalculation.setOvAffiliateRate(convertFormat(getTargetEntities(TARGET_ENTITY_AFF, targetGroupOV).getPercentage()));
        commissionCalculation.setOvDistributionRate(convertFormat(getTargetEntities(TARGET_ENTITY_DIS, targetGroupOV).getPercentage()));
        commissionCalculation.setOvTsrRate(convertFormat(getTargetEntities(TARGET_ENTITY_TSR, targetGroupOV).getPercentage()));
        commissionCalculation.setOvMarketingRate(convertFormat(getTargetEntities(TARGET_ENTITY_MKR, targetGroupOV).getPercentage()));
        commissionCalculation.setOvCompanyRate(convertFormat(getTargetEntities(TARGET_ENTITY_COM, targetGroupOV).getPercentage()));
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

    private CommissionPlan findCommissionPlan(String channelCode, String planCode, String customerCategory, List<CommissionPlan> commissionPlans) {
        CommissionPlan commissionPlan = new CommissionPlan();
        for (CommissionPlan icommissionPlan : commissionPlans) {
            if (icommissionPlan.getUnitCode().equals(channelCode) &&
                    icommissionPlan.getPlanCode().equals(planCode) &&
                    icommissionPlan.getCustomerCategory().name().equals(customerCategory)) {
                commissionPlan = icommissionPlan;
                break;
            }
        }
        return commissionPlan;
    }

    private CommissionTargetGroup getCommissionTargetGroup(String type, List<CommissionTargetGroup> commissionTargetGroups) {
        //TODO If not found, should return null.
        CommissionTargetGroup result = new CommissionTargetGroup();
        for (CommissionTargetGroup commissionTargetGroup : commissionTargetGroups) {
            if (commissionTargetGroup.getTargetGroupType().name().equalsIgnoreCase(type)) {
                result = commissionTargetGroup;
            }
        }
        return result;
    }

    private CommissionTargetEntity getTargetEntities(String entityType, CommissionTargetGroup commissionTargetGroup) {
        //TODO If not found, should return null.
        CommissionTargetEntity result = new CommissionTargetEntity();
        List<CommissionTargetEntity> targetEntities = commissionTargetGroup.getTargetEntities();
        for (CommissionTargetEntity targetEntity : targetEntities) {
            if (targetEntity.getTargetEntityType().name().equalsIgnoreCase(entityType)) {
                result = targetEntity;
                break;
            }
        }
        return result;
    }

    @Deprecated
    private String getRowId(LocalDateTime now) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return "C" + now.format(formatter);
    }

}
