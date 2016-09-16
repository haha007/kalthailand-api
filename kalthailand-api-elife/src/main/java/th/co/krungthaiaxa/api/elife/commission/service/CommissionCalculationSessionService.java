package th.co.krungthaiaxa.api.elife.commission.service;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.CommissionCalculationSessionException;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculation;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionResult;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntity;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroup;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroupType;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionCalculationSessionRepository;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.repository.CDBRepository;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionResultRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import javax.management.remote.TargetedNotification;
import javax.sql.DataSource;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 9/5/16.
 */
@Service
public class CommissionCalculationSessionService {
	private final static Logger logger = LoggerFactory.getLogger(CDBRepository.class);
    private final CommissionPlanService commissionPlanService;
    private final CommissionCalculationSessionRepository commissionCalculationSessionRepository;
    private final CDBRepository cdbRepository;
    private final CommissionResultRepository commissionResultRepository;
    
    private List<CommissionPlan> commissionPlans;
    
    private final PolicyRepository policyRepository;
    
    @Autowired
    @Qualifier("cdbDataSource")
    private DataSource cdbDataSource;
    
    private final String FY = "FY";
    private final String OV = "OV";
    
    private final String TARGET_ENTITY_AFF = "AFFILIATE";
    private final String TARGET_ENTITY_COM = "COMPANY";
    private final String TARGET_ENTITY_TSR = "TSR";
    private final String TARGET_ENTITY_MKR = "MKR";
    private final String TARGET_ENTITY_DIS = "DISTRIBUTION";

    @Inject
    public CommissionCalculationSessionService(CommissionPlanService commissionPlanService, CommissionCalculationSessionRepository commissionCalculationSessionRepository, CDBRepository cdbRepository, PolicyRepository policyRepository, CommissionResultRepository commissionResultRepository) {
        this.commissionPlanService = commissionPlanService;
        this.commissionCalculationSessionRepository = commissionCalculationSessionRepository;
        this.cdbRepository = cdbRepository;
        this.policyRepository = policyRepository;
        this.commissionResultRepository = commissionResultRepository;
    }

    public CommissionCalculationSession validateExistCalculationSession(ObjectId calculationSessionId) {
        CommissionCalculationSession commissionCalculationSession = commissionCalculationSessionRepository.findOne(calculationSessionId);
        if (commissionCalculationSession == null) {
            throw new CommissionCalculationSessionException("Not found calculation session " + calculationSessionId);
        }
        return commissionCalculationSession;
    }
    
    private CommissionPlan getCommissionPlanForCalculate(String channelCode, String planCode, String customerCategory){
    	logger.debug("On getCommissionPlanForCalculate with channelCode("+channelCode+"), planCode("+planCode+"), customerCategory("+customerCategory+").....");
    	logger.debug("check commissionPlans null :"+(commissionPlans==null));
    	CommissionPlan commissionPlan = new CommissionPlan();
    	for(CommissionPlan c:commissionPlans){
    		logger.debug("mongodb unitcode:"+c.getUnitCode());
    		logger.debug("mongo plancode:"+c.getPlanCode());
    		logger.debug("mongo cust:"+c.getCustomerCategory());
    		if(c.getUnitCode().equals(channelCode) &&
    			c.getPlanCode().equals(planCode) &&
    			c.getCustomerCategory().name().equals(customerCategory)){
    			logger.debug("ok with c:"+c.toString());
    			commissionPlan = c;
    			break;
    		}
    	}
    	return commissionPlan;
    }
    
    private CommissionTargetGroup getCommissionTargetGroup(String type, List<CommissionTargetGroup> ctgList){
    	logger.debug("On getCommissionTargetGroup with type("+type+"), ctgList("+ctgList+").....");
    	CommissionTargetGroup outCtg = new CommissionTargetGroup();
    	for(CommissionTargetGroup ctg : ctgList){
    		if(ctg.getTargetGroupType().name().equals(FY)&&type.equals(FY)){
    			outCtg = ctg;
    		}else if(ctg.getTargetGroupType().name().equals(OV)&&type.equals(OV)){
    			outCtg = ctg;
    		}
    	}
    	return outCtg;
    }
    
    private CommissionTargetEntity getTargetEntities(String entityType, CommissionTargetGroup commissionTargetGroup ){
    	logger.debug("On getTargetEntities with entityType("+entityType+"), commissionTargetGroup("+commissionTargetGroup+").....");
    	logger.debug("check size():"+commissionTargetGroup.getTargetEntities().size());
    	CommissionTargetEntity outCtr = new CommissionTargetEntity();
    	List<CommissionTargetEntity> entityGroup = commissionTargetGroup.getTargetEntities();
    	logger.debug("check entityGroup is null:"+(entityGroup==null));
    	for(CommissionTargetEntity cte : entityGroup){
    		if(cte.getTargetEntityType().name().equals(TARGET_ENTITY_AFF)&&entityType.equals(TARGET_ENTITY_AFF)){
    			outCtr = cte;
    			break;
    		}else if(cte.getTargetEntityType().name().equals(TARGET_ENTITY_COM)&&entityType.equals(TARGET_ENTITY_COM)){
    			outCtr = cte;
    			break;
    		}else if(cte.getTargetEntityType().name().equals(TARGET_ENTITY_TSR)&&entityType.equals(TARGET_ENTITY_TSR)){
    			outCtr = cte;
    			break;
    		}else if(cte.getTargetEntityType().name().equals(TARGET_ENTITY_MKR)&&entityType.equals(TARGET_ENTITY_MKR)){
    			outCtr = cte;
    			break;
    		}else if(cte.getTargetEntityType().name().equals(TARGET_ENTITY_DIS)&&entityType.equals(TARGET_ENTITY_DIS)){
    			outCtr = cte;
    			break;
    		}
    	}
    	return outCtr;
    }
    
    private String getExistingAgentCodeStatus(String agentCode){
    	//String sql = ""
    	return null;
    }
    
    public byte[] exportToExcel(String rowId){
    	
    	/*CommissionResult commissionResult = commissionResultRepository.findByRowId(rowId);
    	
    	String now = ofPattern("yyyyMMdd_HHmmss").format(now());
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("CommissionExtract_" + now);
        
        ExcelUtils.appendRow(sheet,
                text("Month"),
                text("Policy Number"),
                text("Policy Status"),
                text("Plan Code"),
                text("Agent Code"),
                text("Customer Category"),
                text("Previous Policy Number"),
                text("Existing Agent Code 1"),
                text("Existing Agent Code 1 Status"),
                text("Existing Agent Code 2"),
                text("Existing Agent Code 2 Status"),
                text("First Year Premium (RLS)"),
                text("First Year Commission (RLS)"),
                text("FY Affiliate Commission"),
                text("FY Distribution 1 Commission"),
                text("FY Distribution 2 Commission"),
                text("FY TSR Commission"),
                text("FY Marking Commission"),
                text("FY Company Commission"),
                text("OV Affiliate Commission"),
                text("OV Distribution 1 Commission"),
                text("OV Distribution 2 Commission"),
                text("OV TSR Commission"),
                text("OV Marking Commission"),
                text("OV Company Commission"),
                text("OV ")
                
        		
        		);
        policies.stream().forEach(tmp -> createPolicyExtractExcelFileLine(sheet, tmp));
        ExcelUtils.autoWidthAllColumns(workbook);*/
    	
    	return null;
        
    }
    
    public List<CommissionResult> getCommissionCalculationedList(){
    	return commissionResultRepository.findAllByOrderByCreatedDateTimeAsc();
    }
    
    private String getRowId(LocalDateTime now){
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    	return "C"+now.format(formatter);
    }

    public void calculateCommissionForPolicies() {
    	
    	LocalDateTime nowDate = LocalDateTime.now();
    	
    	
    	//save first
    	CommissionResult commissionResult = new CommissionResult();
    	commissionResult.setCommissionMonth(nowDate.getMonthValue()-1);
    	commissionResult.setCreatedDateTime(nowDate);
    	commissionResult.setRowId(getRowId(nowDate));
    	commissionResultRepository.save(commissionResult);
    	
    	
    	String NULL = "NULL";
    	String NEW = "NEW";
    	String EXISTING = "EXISTING";
    	String DUMMY = "DUMMY";
    	
        commissionPlans = commissionPlanService.findAll();
        List<String> channelIds = commissionPlans.stream().map(sc->sc.getUnitCode()).collect(Collectors.toList());        
        List<String> planCodes = commissionPlans.stream().map(sc->sc.getPlanCode()).collect(Collectors.toList());
        List<String> channelIdsNoDup = channelIds.stream().distinct().collect(Collectors.toList());
        List<String> planCodesNoDup = planCodes.stream().distinct().collect(Collectors.toList());
        
        List<CommissionCalculation> listCommissionCalculated = new ArrayList<>();
        
        
        
        if(channelIdsNoDup.size()>0&&planCodesNoDup.size()>0){
        	//String nativeSQLchannelIdsNoDup = "("+channelIdsNoDup.toString().replace(" ","").replace("[", "'").replace("]", "'").replace(",", "','")+")";
        	//String nativeSQLplanCodesNoDup = "("+planCodesNoDup.toString().replace(" ", "").replace("[", "'").replace("]", "'").replace(",", "','")+")";
        	String nativeSQLchannelIdsNoDup = "'126620','103070'";
        	String nativeSQLplanCodesNoDup = "'WLNP60','UL90'";
        	String sql =	"select " +
        					"ltrim(rtrim(a.pno)) as policyNo, " +
        					"ltrim(rtrim(a.pstu)) as policyStatus, " +
        					"ltrim(rtrim(a.lplan)) as planCode, " +
        					"ltrim(rtrim(a.pmode)) as paymentCode, "+
        					"ltrim(rtrim(a.pagt1)) as agentCode, " +
        					"ltrim(rtrim(b.g3bsp1)) as firstYearPremium, " +
        					"ltrim(rtrim(b.g3bsc1)) as firstYearCommission " +
        					"from [dbo].[LFKLUDTA_LFPPML] a " +
        					"inner join [dbo].[LFKLUDTA_LFPPMSWK] b " +
        					"on a.pno = b.g3pno " +
        					"where " +
        					"left(ltrim(rtrim(cast(a.pagt1 as varchar))),6) in ("+nativeSQLchannelIdsNoDup+") " +
        					"and ltrim(rtrim(a.lplan)) in ("+nativeSQLplanCodesNoDup+") ";
        	System.out.println("sql:"+sql);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(cdbDataSource);
            jdbcTemplate.setQueryTimeout(600);
            try {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
                System.out.println("list.size():"+list.size());
                if (list.size()>0) {
                	for(Map<String,Object> m : list){                		             		
                		//check policy must not null
                		Policy policy = policyRepository.findByPolicyId(String.valueOf(m.get("policyNo")));
                		if(policy!=null){
                			CommissionCalculation c = new CommissionCalculation();   
                			//cdb information
                    		c.setPolicyNo(String.valueOf(m.get("policyNo")));
                    		c.setPolicyStatus(String.valueOf(m.get("policyStatus")));
                    		c.setPlanCode(String.valueOf(m.get("planCode")));
                    		c.setPaymentCode(String.valueOf(m.get("paymentCode")));
                    		c.setAgentCode(String.valueOf(m.get("agentCode")));
                    		c.setFirstYearPremium(Double.valueOf(String.valueOf(m.get("firstYearPremium"))));
                    		c.setFirstYearCommission(Double.valueOf(String.valueOf(m.get("firstYearCommission"))));
                    		//previously information
                			Insured insured = policy.getInsureds().get(0);
                    		List<String> prevInf = insured.getInsuredPreviousInformations();
                    		if(prevInf.size()==0){
                    			c.setCustomerCategory(NEW);
                    			c.setPreviousPolicyNo(NULL);
                    			c.setExistingAgentCode1(NULL);
                    			c.setExistingAgentCode1Status(DUMMY);
                    			c.setExistingAgentCode2(NULL);
                    			c.setExistingAgentCode2Status(DUMMY);
                    		}else{
                    			c.setCustomerCategory((StringUtil.isBlank(prevInf.get(0))?NEW:EXISTING));
                    			c.setPreviousPolicyNo((StringUtil.isBlank(prevInf.get(0))?NULL:prevInf.get(0)));
                    			c.setExistingAgentCode1((StringUtil.isBlank(prevInf.get(1))?NULL:prevInf.get(1)));
                    			c.setExistingAgentCode1Status(DUMMY);
                    			c.setExistingAgentCode2((StringUtil.isBlank(prevInf.get(2))?NULL:prevInf.get(2)));
                    			c.setExistingAgentCode2Status(DUMMY);
                    		}
                    		//calculation commission
                    		//fy
                    		CommissionPlan plan = getCommissionPlanForCalculate(c.getAgentCode().substring(0, 6),c.getPlanCode(),c.getCustomerCategory());
                    		logger.debug("plan.toString():"+plan.toString());
                    		logger.debug("check target group:"+plan.getTargetGroups());
                    		logger.debug(plan.getPlanCode()+","+plan.getUnitCode()+","+plan.getCreatedDateTime()+","+plan.getCustomerCategory()+","+plan.getId()+","+plan.getUpdatedDateTime());
                    		CommissionTargetGroup fCtg = getCommissionTargetGroup(FY,plan.getTargetGroups());
                    		CommissionTargetGroup oCtg = getCommissionTargetGroup(OV,plan.getTargetGroups());
                    		logger.debug("check fCtg null:"+(fCtg==null));
                    		logger.debug("check oCtg null:"+(oCtg==null));
                    		c.setFyAffiliateCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_AFF,fCtg).getPercentage());
                    		Double fDisComm = c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_DIS,fCtg).getPercentage();
                    		if(StringUtil.isBlank(c.getExistingAgentCode2())){
                    			c.setFyDistribution1Commission(fDisComm);
                    			c.setFyDistribution2Commission(0.0);
                    		}else{
                    			Double disCommSplit = fDisComm / 2;
                    			c.setFyDistribution1Commission(disCommSplit);
                    			c.setFyDistribution2Commission(disCommSplit);
                    		}
                    		c.setFyTsrCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_TSR,fCtg).getPercentage());
                    		c.setFyMarkingCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_MKR,fCtg).getPercentage());
                    		c.setFyCompanyCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_COM,fCtg).getPercentage());
                    		//ov
                    		c.setOvAffiliateCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_AFF,oCtg).getPercentage());
                    		Double oDisComm = c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_DIS,oCtg).getPercentage();
                    		if(StringUtil.isBlank(c.getExistingAgentCode2())){
                    			c.setOvDistribution1Commission(oDisComm);
                    			c.setOvDistribution2Commission(0.0);
                    		}else{
                    			Double disCommSplit = oDisComm / 2;
                    			c.setOvDistribution1Commission(disCommSplit);
                    			c.setOvDistribution2Commission(disCommSplit);
                    		}
                    		c.setOvTsrCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_TSR,oCtg).getPercentage());
                    		c.setOvMarkingCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_MKR,oCtg).getPercentage());
                    		c.setOvCompanyCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_COM,oCtg).getPercentage());
                    		//commission rage
                    		c.setFyAffiliateRate(getTargetEntities(TARGET_ENTITY_AFF,fCtg).getPercentage());
                    		c.setFyDistributionRate(getTargetEntities(TARGET_ENTITY_DIS,fCtg).getPercentage());
                    		c.setFyTsrRate(getTargetEntities(TARGET_ENTITY_TSR,fCtg).getPercentage());
                    		c.setFyMarkingRate(getTargetEntities(TARGET_ENTITY_MKR,fCtg).getPercentage());
                    		c.setFyCompanyRate(getTargetEntities(TARGET_ENTITY_COM,fCtg).getPercentage());
                    		c.setOvAffiliateRate(getTargetEntities(TARGET_ENTITY_AFF,oCtg).getPercentage());
                    		c.setOvDistributiionRate(getTargetEntities(TARGET_ENTITY_DIS,oCtg).getPercentage());
                    		c.setOvTsrRate(getTargetEntities(TARGET_ENTITY_TSR,oCtg).getPercentage());
                    		c.setOvMarkingRate(getTargetEntities(TARGET_ENTITY_MKR,oCtg).getPercentage());
                    		c.setOvCompanyRate(getTargetEntities(TARGET_ENTITY_COM,oCtg).getPercentage());
                    		//add in list
                    		listCommissionCalculated.add(c);
                		}             		
                	}                	
                	commissionResult.setCommissionPoliciesCount(listCommissionCalculated.size());
                	commissionResult.setPolicies(listCommissionCalculated);
                	commissionResult.setUpdatedDateTime(LocalDateTime.now());
                	//update
                	commissionResultRepository.save(commissionResult);                	
                }
            } catch (Exception e) {
                logger.error("Unable to query", e);
            }
        }
    }
    
    public static void main(String args[]){
    	System.out.println(LocalDate.now().getMonthValue());
    }
    
    /*
     public Optional<Triple<String, String, String>> getExistingAgentCode(String idCard, String dateOfBirth) {
        if (isBlank(idCard) || isBlank(dateOfBirth)) {
            return Optional.empty();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("[%1$s] .....", "getExistingAgentCode"));
            logger.debug(String.format("idCard is %1$s", idCard));
            logger.debug(String.format("dateOfBirth is %1$s", dateOfBirth));
        }
        String sql = "select top 1 pno, " +
    			" case cast(coalesce(pagt1,0) as varchar) when '0' then 'NULL' else cast(coalesce(pagt1,0) as varchar) end as pagt1, " + 
    			" case cast(coalesce(pagt2,0) as varchar) when '0' then 'NULL' else cast(coalesce(pagt2,0) as varchar) end as pagt2 " +
                "from lfkludta_lfppml " +
                "where left(coalesce(pagt1,'0'),1) not in ('2','4') " +
                "and left(coalesce(pagt2,'0'),1) not in ('2','4') " +
                "and pterm = 0 " +
                "and pstu in ('1') " +
                "and ? = " +
                "case when ltrim(rtrim(coalesce(pownid,''))) <> '' and lpaydb <> 0 " +
                "then ltrim(rtrim(coalesce(pownid,''))) else ltrim(rtrim(coalesce(pid,''))) end " +
                "and ? = " +
                "case when ltrim(rtrim(coalesce(pownid,''))) <> '' and lpaydb <> 0 " +
                "then lpaydb else pdob end " +
                "order by pdoi desc";
        Object[] parameters = new Object[2];
        parameters[0] = idCard;
        parameters[1] = dateOfBirth;
        Map<String, Object> map = null;
        JdbcTemplate jdbcTemplate = new JdbcTemplate(cdbDataSource);
        jdbcTemplate.setQueryTimeout(600);
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, parameters);
            if (list.size() != 0) {
                map = list.get(0);
            }
        } catch (Exception e) {
            logger.error("Unable to query for agent code", e);
        }

        if (map == null) {
        	return Optional.of(Triple.of("NULL", "NULL", "NULL"));
        } else {
            return Optional.of(Triple.of((String) map.get("pno"), (String) map.get("pagt1"), (String) map.get("pagt2")));
        }
    }
     * */

}
