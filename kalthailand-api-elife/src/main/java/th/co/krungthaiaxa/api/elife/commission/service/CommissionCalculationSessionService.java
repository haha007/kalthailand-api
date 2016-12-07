package th.co.krungthaiaxa.api.elife.commission.service;

import org.bson.types.ObjectId;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.CommissionCalculationSessionException;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculation;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionResult;
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
    public List<CommissionResult> findAllCommissionCalculationSessions() {
        LOGGER.debug("Start process to get commission list .....");
        LOGGER.debug("Stop process to get commission list .....");
        return commissionResultRepository.findAllByOrderByCreatedDateTimeAsc();
    }

    //santi : for trigger calculation commission
    public void calculateCommissionForPolicies() {
        LOGGER.debug("[Commission] start");

        LocalDateTime nowDate = LocalDateTime.now();

        //save first
        CommissionResult commissionResult = new CommissionResult();
        commissionResult.setCommissionMonth(String.valueOf(nowDate.getYear()) + String.valueOf((new DecimalFormat("00")).format((nowDate.getMonthValue() - 1))));
        commissionResult.setCreatedDateTime(nowDate);
        //TODO rowId is unnecessary
        commissionResult.setRowId(getRowId(nowDate));

        List<CommissionPlan> commissionPlans = commissionPlanService.findAll();
        List<String> channelIds = commissionPlans.stream().map(sc -> sc.getUnitCode()).collect(Collectors.toList());//list of UnitCode
        List<String> planCodes = commissionPlans.stream().map(sc -> sc.getPlanCode()).collect(Collectors.toList());
        List<String> channelIdsNoDup = channelIds.stream().distinct().collect(Collectors.toList());
        List<String> planCodesNoDup = planCodes.stream().distinct().collect(Collectors.toList());

        List<CommissionCalculation> listCommissionCalculated = new ArrayList<>();

        if (channelIdsNoDup.size() > 0 && planCodesNoDup.size() > 0) {
            try {
                List<CDBPolicyCommissionEntity> policiesCDB = cdbRepository.findPoliciesByChannelIdsAndPaymentModeIds(channelIdsNoDup, planCodesNoDup); //jdbcTemplate.queryForList(generateSql(channelIdsNoDup, planCodesNoDup), generateParameters(channelIdsNoDup, planCodesNoDup));
                if (policiesCDB.size() > 0) {
                    for (CDBPolicyCommissionEntity policyCDB : policiesCDB) {
                        //check policy must not null
                        Policy policy = policyRepository.findByPolicyId(String.valueOf(policyCDB.getPolicyNumber()));
                        if (policy != null) {
                            CommissionCalculation commissionCalculation = calculateCommissionForPolicy(policy, policyCDB, commissionPlans);
                            listCommissionCalculated.add(commissionCalculation);
                        }
                    }
                    commissionResult.setCommissionPoliciesCount(listCommissionCalculated.size());
                    commissionResult.setPolicies(listCommissionCalculated);
                    commissionResult.setUpdatedDateTime(LocalDateTime.now());
                    //update
                }
            } catch (Exception e) {
                LOGGER.error("Unable to query " + e.getMessage(), e);
            }
        } else {
            LOGGER.debug("[Commission] Not found channel Ids and planCodes in setting.....");
        }
        commissionResultRepository.save(commissionResult);
        LOGGER.debug("[Commission] finish");
    }

    private CommissionCalculation calculateCommissionForPolicy(Policy policy, CDBPolicyCommissionEntity policyCDB, List<CommissionPlan> commissionPlans) {
        CommissionCalculation commissionCalculation = new CommissionCalculation();

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
        return commissionCalculation;
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

    public CommissionCalculationSession validateExistCalculationSession(ObjectId calculationSessionId) {
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
