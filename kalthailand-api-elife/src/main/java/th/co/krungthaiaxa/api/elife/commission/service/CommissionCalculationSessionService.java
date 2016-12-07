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
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionCalculationSessionRepository;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionResultRepository;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public List<CommissionResult> getCommissionCalculationedList() {
        LOGGER.debug("Start process to get commission list .....");
        LOGGER.debug("Stop process to get commission list .....");
        return commissionResultRepository.findAllByOrderByCreatedDateTimeAsc();
    }

    //santi : for trigger calculation commission
    public void calculateCommissionForPolicies() {

        LOGGER.debug("Start process to calculate commission .....");

        LocalDateTime nowDate = LocalDateTime.now();

        //save first
        CommissionResult commissionResult = new CommissionResult();
        commissionResult.setCommissionMonth(String.valueOf(nowDate.getYear()) + String.valueOf((new DecimalFormat("00")).format((nowDate.getMonthValue() - 1))));
        commissionResult.setCreatedDateTime(nowDate);
        //TODO rowId is unnecessary
        commissionResult.setRowId(getRowId(nowDate));
        commissionResultRepository.save(commissionResult);

        List<CommissionPlan> commissionPlans = commissionPlanService.findAll();
        List<String> channelIds = commissionPlans.stream().map(sc -> sc.getUnitCode()).collect(Collectors.toList());//list of UnitCode
        List<String> planCodes = commissionPlans.stream().map(sc -> sc.getPlanCode()).collect(Collectors.toList());
        List<String> channelIdsNoDup = channelIds.stream().distinct().collect(Collectors.toList());
        List<String> planCodesNoDup = planCodes.stream().distinct().collect(Collectors.toList());

        List<CommissionCalculation> listCommissionCalculated = new ArrayList<>();

        if (channelIdsNoDup.size() > 0 && planCodesNoDup.size() > 0) {
            try {
                List<Map<String, Object>> policies = cdbRepository.findPoliciesByChannelIdsAndPaymentModeIds(channelIdsNoDup, planCodesNoDup); //jdbcTemplate.queryForList(generateSql(channelIdsNoDup, planCodesNoDup), generateParameters(channelIdsNoDup, planCodesNoDup));
                if (policies.size() > 0) {
                    for (Map<String, Object> policyMap : policies) {
                        //check policy must not null
                        Policy policy = policyRepository.findByPolicyId(String.valueOf(policyMap.get("policyNo")));
                        if (policy != null) {
                            CommissionCalculation commissionCalculation = calculateCommissionForPolicy(policy, policyMap, commissionPlans);
                            listCommissionCalculated.add(commissionCalculation);
                        }
                    }
                    commissionResult.setCommissionPoliciesCount(listCommissionCalculated.size());
                    commissionResult.setPolicies(listCommissionCalculated);
                    commissionResult.setUpdatedDateTime(LocalDateTime.now());
                    //update
                    commissionResultRepository.save(commissionResult);
                } else {
                    commissionResultRepository.delete(commissionResult);
                }
            } catch (Exception e) {
                LOGGER.error("Unable to query " + e.getMessage(), e);
            }
        } else {
            LOGGER.debug("Have no commission configure to calculate.....");
        }
        LOGGER.debug("Stop process to calculate commission .....");
    }

    private CommissionCalculation calculateCommissionForPolicy(Policy policy, Map<String, Object> policyMap, List<CommissionPlan> commissionPlans) {
        CommissionCalculation commissionCalculation = new CommissionCalculation();

        //cdb information
        commissionCalculation.setPolicyNo(String.valueOf(policyMap.get("policyNo")));
        commissionCalculation.setPolicyStatus(String.valueOf(policyMap.get("policyStatus")));
        commissionCalculation.setPlanCode(String.valueOf(policyMap.get("planCode")));
        commissionCalculation.setPaymentCode(String.valueOf(policyMap.get("paymentCode")));
        commissionCalculation.setAgentCode(String.valueOf(policyMap.get("agentCode")));
        commissionCalculation.setFirstYearPremium(convertFormat(Double.valueOf(String.valueOf(policyMap.get("firstYearPremium")))));
        commissionCalculation.setFirstYearCommission(convertFormat(Double.valueOf(String.valueOf(policyMap.get("firstYearCommission")))));

        //previously information
        Insured insured = policy.getInsureds().get(0);
        List<String> prevInf = insured.getInsuredPreviousInformations();
        if (prevInf.size() == 0) {
            commissionCalculation.setCustomerCategory(NEW);
            commissionCalculation.setPreviousPolicyNo(BLANK);
            commissionCalculation.setExistingAgentCode1(BLANK);
            commissionCalculation.setExistingAgentCode1Status(BLANK);
            commissionCalculation.setExistingAgentCode2(BLANK);
            commissionCalculation.setExistingAgentCode2Status(BLANK);
        } else {
            commissionCalculation.setCustomerCategory((prevInf.get(0).equals(NULL) ? NEW : EXISTING));
            commissionCalculation.setPreviousPolicyNo((prevInf.get(0).equals(NULL) ? BLANK : prevInf.get(0)));
            commissionCalculation.setExistingAgentCode1((prevInf.get(1).equals(NULL) ? BLANK : prevInf.get(1)));
            commissionCalculation.setExistingAgentCode1Status((commissionCalculation.getExistingAgentCode1().equals(BLANK) ? BLANK : cdbRepository.getExistingAgentCodeStatus(getProperAgentCodeNumber(commissionCalculation.getExistingAgentCode1(), 14))));
            commissionCalculation.setExistingAgentCode2((prevInf.get(2).equals(NULL) ? BLANK : prevInf.get(2)));
            commissionCalculation.setExistingAgentCode2Status((commissionCalculation.getExistingAgentCode2().equals(BLANK) ? BLANK : cdbRepository.getExistingAgentCodeStatus(getProperAgentCodeNumber(commissionCalculation.getExistingAgentCode2(), 14))));
        }

        //calculation commission

        //fy
        CommissionPlan plan = getCommissionPlanForCalculate(getProperAgentCodeNumber(commissionCalculation.getAgentCode(), 6), commissionCalculation.getPlanCode(), commissionCalculation.getCustomerCategory(), commissionPlans);
        CommissionTargetGroup fCtg = getCommissionTargetGroup(FY, plan.getTargetGroups());
        CommissionTargetGroup oCtg = getCommissionTargetGroup(OV, plan.getTargetGroups());
        commissionCalculation.setFyAffiliateCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_AFF, fCtg).getPercentage()) / 100));
        Double fDisComm = (commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_DIS, fCtg).getPercentage()) / 100;
        if (StringUtil.isBlank(commissionCalculation.getExistingAgentCode2())) {
            commissionCalculation.setFyDistribution1Commission(convertFormat(fDisComm));
            commissionCalculation.setFyDistribution2Commission(0.0);
        } else {
            Double disCommSplit = fDisComm / 2;
            commissionCalculation.setFyDistribution1Commission(convertFormat(disCommSplit));
            commissionCalculation.setFyDistribution2Commission(convertFormat(disCommSplit));
        }
        commissionCalculation.setFyTsrCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_TSR, fCtg).getPercentage()) / 100));
        commissionCalculation.setFyMarketingCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_MKR, fCtg).getPercentage()) / 100));
        commissionCalculation.setFyCompanyCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_COM, fCtg).getPercentage()) / 100));

        //ov
        commissionCalculation.setOvAffiliateCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_AFF, oCtg).getPercentage()) / 100));
        Double oDisComm = (commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_DIS, oCtg).getPercentage()) / 100;
        if (StringUtil.isBlank(commissionCalculation.getExistingAgentCode2())) {
            commissionCalculation.setOvDistribution1Commission(convertFormat(oDisComm));
            commissionCalculation.setOvDistribution2Commission(0.0);
        } else {
            Double disCommSplit = oDisComm / 2;
            commissionCalculation.setOvDistribution1Commission(convertFormat(disCommSplit));
            commissionCalculation.setOvDistribution2Commission(convertFormat(disCommSplit));
        }
        commissionCalculation.setOvTsrCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_TSR, oCtg).getPercentage()) / 100));
        commissionCalculation.setOvMarketingCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_MKR, oCtg).getPercentage()) / 100));
        commissionCalculation.setOvCompanyCommission(convertFormat((commissionCalculation.getFirstYearCommission() * getTargetEntities(TARGET_ENTITY_COM, oCtg).getPercentage()) / 100));

        //commission rate
        commissionCalculation.setFyAffiliateRate(convertFormat(getTargetEntities(TARGET_ENTITY_AFF, fCtg).getPercentage()));
        commissionCalculation.setFyDistributionRate(convertFormat(getTargetEntities(TARGET_ENTITY_DIS, fCtg).getPercentage()));
        commissionCalculation.setFyTsrRate(convertFormat(getTargetEntities(TARGET_ENTITY_TSR, fCtg).getPercentage()));
        commissionCalculation.setFyMarketingRate(convertFormat(getTargetEntities(TARGET_ENTITY_MKR, fCtg).getPercentage()));
        commissionCalculation.setFyCompanyRate(convertFormat(getTargetEntities(TARGET_ENTITY_COM, fCtg).getPercentage()));
        commissionCalculation.setOvAffiliateRate(convertFormat(getTargetEntities(TARGET_ENTITY_AFF, oCtg).getPercentage()));
        commissionCalculation.setOvDistributiionRate(convertFormat(getTargetEntities(TARGET_ENTITY_DIS, oCtg).getPercentage()));
        commissionCalculation.setOvTsrRate(convertFormat(getTargetEntities(TARGET_ENTITY_TSR, oCtg).getPercentage()));
        commissionCalculation.setOvMarketingRate(convertFormat(getTargetEntities(TARGET_ENTITY_MKR, oCtg).getPercentage()));
        commissionCalculation.setOvCompanyRate(convertFormat(getTargetEntities(TARGET_ENTITY_COM, oCtg).getPercentage()));
        return commissionCalculation;
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

    private CommissionPlan getCommissionPlanForCalculate(String channelCode, String planCode, String customerCategory, List<CommissionPlan> commissionPlans) {
        CommissionPlan commissionPlan = new CommissionPlan();
        for (CommissionPlan c : commissionPlans) {
            if (c.getUnitCode().equals(channelCode) &&
                    c.getPlanCode().equals(planCode) &&
                    c.getCustomerCategory().name().equals(customerCategory)) {
                commissionPlan = c;
                break;
            }
        }
        return commissionPlan;
    }

    private CommissionTargetGroup getCommissionTargetGroup(String type, List<CommissionTargetGroup> ctgList) {
        CommissionTargetGroup outCtg = new CommissionTargetGroup();
        for (CommissionTargetGroup ctg : ctgList) {
            if (ctg.getTargetGroupType().name().equals(FY) && type.equals(FY)) {
                outCtg = ctg;
            } else if (ctg.getTargetGroupType().name().equals(OV) && type.equals(OV)) {
                outCtg = ctg;
            }
        }
        return outCtg;
    }

    private CommissionTargetEntity getTargetEntities(String entityType, CommissionTargetGroup commissionTargetGroup) {
        CommissionTargetEntity outCtr = new CommissionTargetEntity();
        List<CommissionTargetEntity> entityGroup = commissionTargetGroup.getTargetEntities();
        for (CommissionTargetEntity cte : entityGroup) {
            if (cte.getTargetEntityType().name().equals(TARGET_ENTITY_AFF) && entityType.equals(TARGET_ENTITY_AFF)) {
                outCtr = cte;
                break;
            } else if (cte.getTargetEntityType().name().equals(TARGET_ENTITY_COM) && entityType.equals(TARGET_ENTITY_COM)) {
                outCtr = cte;
                break;
            } else if (cte.getTargetEntityType().name().equals(TARGET_ENTITY_TSR) && entityType.equals(TARGET_ENTITY_TSR)) {
                outCtr = cte;
                break;
            } else if (cte.getTargetEntityType().name().equals(TARGET_ENTITY_MKR) && entityType.equals(TARGET_ENTITY_MKR)) {
                outCtr = cte;
                break;
            } else if (cte.getTargetEntityType().name().equals(TARGET_ENTITY_DIS) && entityType.equals(TARGET_ENTITY_DIS)) {
                outCtr = cte;
                break;
            }
        }
        return outCtr;
    }

    private String getRowId(LocalDateTime now) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return "C" + now.format(formatter);
    }

}
