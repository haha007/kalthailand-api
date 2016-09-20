package th.co.krungthaiaxa.api.elife.commission.service;

import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
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
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionCalculationSessionRepository;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionResultRepository;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.repository.CDBRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

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
    private final String BLANK = "";
	private final String NEW = "NEW";
	private final String EXISTING = "EXISTING";
	private final String NULL = "NULL";

    @Inject
    public CommissionCalculationSessionService(CommissionPlanService commissionPlanService, CommissionCalculationSessionRepository commissionCalculationSessionRepository, CDBRepository cdbRepository, PolicyRepository policyRepository, CommissionResultRepository commissionResultRepository) {
        this.commissionPlanService = commissionPlanService;
        this.commissionCalculationSessionRepository = commissionCalculationSessionRepository;
        this.cdbRepository = cdbRepository;
        this.policyRepository = policyRepository;
        this.commissionResultRepository = commissionResultRepository;
    }
    
    //santi : for get list of calculated commission
    public List<CommissionResult> getCommissionCalculationedList(){
    	logger.debug("Start process to get commission list .....");
    	logger.debug("Stop process to get commission list .....");
    	return commissionResultRepository.findAllByOrderByCreatedDateTimeAsc();
    }
    
    //santi : for trigger calculation commission
    public void calculateCommissionForPolicies() {
    	
    	logger.debug("Start process to calculate commission .....");
    	
    	LocalDateTime nowDate = LocalDateTime.now();    	
    	
    	//save first
    	CommissionResult commissionResult = new CommissionResult();
    	commissionResult.setCommissionMonth(nowDate.getMonthValue()-1);
    	commissionResult.setCreatedDateTime(nowDate);
    	commissionResult.setRowId(getRowId(nowDate));
    	commissionResultRepository.save(commissionResult);
    	
        commissionPlans = commissionPlanService.findAll();
        List<String> channelIds = commissionPlans.stream().map(sc->sc.getUnitCode()).collect(Collectors.toList());        
        List<String> planCodes = commissionPlans.stream().map(sc->sc.getPlanCode()).collect(Collectors.toList());
        List<String> channelIdsNoDup = channelIds.stream().distinct().collect(Collectors.toList());
        List<String> planCodesNoDup = planCodes.stream().distinct().collect(Collectors.toList());
        
        List<CommissionCalculation> listCommissionCalculated = new ArrayList<>();  
        
        if(channelIdsNoDup.size()>0&&planCodesNoDup.size()>0){        	
        	JdbcTemplate jdbcTemplate = new JdbcTemplate(cdbDataSource);
            jdbcTemplate.setQueryTimeout(600);
            try {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(
                		generateSql(channelIdsNoDup, planCodesNoDup), 
                		generateParameters(channelIdsNoDup, planCodesNoDup));
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
                    			c.setPreviousPolicyNo(BLANK);
                    			c.setExistingAgentCode1(BLANK);
                    			c.setExistingAgentCode1Status(BLANK);
                    			c.setExistingAgentCode2(BLANK);
                    			c.setExistingAgentCode2Status(BLANK);
                    		}else{
                    			c.setCustomerCategory((prevInf.get(0).equals(NULL)?NEW:EXISTING));
                    			c.setPreviousPolicyNo((prevInf.get(0).equals(NULL)?BLANK:prevInf.get(0)));
                    			c.setExistingAgentCode1((prevInf.get(1).equals(NULL)?BLANK:prevInf.get(1)));
                    			c.setExistingAgentCode1Status((c.getExistingAgentCode1().equals(BLANK)?BLANK:getExistingAgentCodeStatus(c.getExistingAgentCode1())));
                    			c.setExistingAgentCode2((prevInf.get(2).equals(NULL)?BLANK:prevInf.get(2)));
                    			c.setExistingAgentCode2Status((c.getExistingAgentCode2().equals(BLANK)?BLANK:getExistingAgentCodeStatus(c.getExistingAgentCode2())));
                    		}
                    		
                    		//calculation commission
                    		
                    		//fy
                    		CommissionPlan plan = getCommissionPlanForCalculate(c.getAgentCode().substring(0, 6),c.getPlanCode(),c.getCustomerCategory());
                    		CommissionTargetGroup fCtg = getCommissionTargetGroup(FY,plan.getTargetGroups());
                    		CommissionTargetGroup oCtg = getCommissionTargetGroup(OV,plan.getTargetGroups());
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
                    		c.setFyMarketingCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_MKR,fCtg).getPercentage());
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
                    		c.setOvMarketingCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_MKR,oCtg).getPercentage());
                    		c.setOvCompanyCommission(c.getFirstYearCommission()*getTargetEntities(TARGET_ENTITY_COM,oCtg).getPercentage());
                    		
                    		//commission rate
                    		c.setFyAffiliateRate(getTargetEntities(TARGET_ENTITY_AFF,fCtg).getPercentage());
                    		c.setFyDistributionRate(getTargetEntities(TARGET_ENTITY_DIS,fCtg).getPercentage());
                    		c.setFyTsrRate(getTargetEntities(TARGET_ENTITY_TSR,fCtg).getPercentage());
                    		c.setFyMarketingRate(getTargetEntities(TARGET_ENTITY_MKR,fCtg).getPercentage());
                    		c.setFyCompanyRate(getTargetEntities(TARGET_ENTITY_COM,fCtg).getPercentage());
                    		c.setOvAffiliateRate(getTargetEntities(TARGET_ENTITY_AFF,oCtg).getPercentage());
                    		c.setOvDistributiionRate(getTargetEntities(TARGET_ENTITY_DIS,oCtg).getPercentage());
                    		c.setOvTsrRate(getTargetEntities(TARGET_ENTITY_TSR,oCtg).getPercentage());
                    		c.setOvMarketingRate(getTargetEntities(TARGET_ENTITY_MKR,oCtg).getPercentage());
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
                }else{
                	commissionResultRepository.delete(commissionResult);
                }
            } catch (Exception e) {
                logger.error("Unable to query", e);
            }
        }else{
        	logger.debug("Have no commissiion configure to calculate.....");
        }
        
        logger.debug("Stop process to calculate commission .....");
    }
    
    //santi : for download commission excel file
    public byte[] exportToExcel(String rowId, String now){
    	
    	logger.debug("Start process to export commission excel .....");
    	
    	byte[] content = null;
    	
    	CommissionResult commissionResult = commissionResultRepository.findByRowId(rowId);
    	List<CommissionCalculation> commissionCalculated = commissionResult.getPolicies();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("CommissionExtract_" + now);
        
        ExcelUtils.appendRow(sheet,
                text("Month"),
                text("Policy Number"),
                text("Policy Status"),
                text("Plan Code"),
                text("Payment Code"),
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
                text("FY Marketing Commission"),
                text("FY Company Commission"),
                text("OV Affiliate Commission"),
                text("OV Distribution 1 Commission"),
                text("OV Distribution 2 Commission"),
                text("OV TSR Commission"),
                text("OV Marketing Commission"),
                text("OV Company Commission"),
                text("FY Affiliate Rate"),
                text("FY Distribution Rate"),
                text("FY TSR Rate"),
                text("FY Marketion Rate"),
                text("FY Company Rate"),
                text("OV Affiliate Rate"),
                text("OV Distribution Rate"),
                text("OV TSR Rate"),
                text("OV Marketing Rate"),
                text("OV Company Rate"),
                text("Calculate Date Time"));
        commissionCalculated.stream().forEach(tmp -> createCommissionResultExtractExcelFileLine(sheet, tmp, commissionResult));
        ExcelUtils.autoWidthAllColumns(workbook);        
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            content = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }
        
        logger.debug("Stop process to export commission excel .....");
    	
    	return content;
        
    }
    
    private String generateSql(List<String> channelIdsNoDup, List<String> planCodesNoDup){
    	String sql =	"select " +
						"ltrim(rtrim(a.pno)) as policyNo, " +
						"ltrim(rtrim(a.pstu)) as policyStatus, " +
						"ltrim(rtrim(a.lplan)) as planCode, " +
						"ltrim(rtrim(a.pmode)) as paymentCode, "+
						"ltrim(rtrim(a.pagt1)) as agentCode, " +
						"b.g3bsp1 as firstYearPremium, " +
						"b.g3bsc1 as firstYearCommission " +
						"from [dbo].[LFKLUDTA_LFPPML] a " +
						"inner join [dbo].[LFKLUDTA_LFPPMSWK] b " +
						"on a.pno = b.g3pno " +
						"where " +
						"left(ltrim(rtrim(cast(a.pagt1 as varchar))),6) in (";
    	for(String channelId : channelIdsNoDup){
    		sql += "?,";
    	}
    	sql = sql.substring(0,sql.length()-1);
    	sql += ") and ltrim(rtrim(a.lplan)) in (";
    	for(String planCode : planCodesNoDup){
    		sql += "?,";
    	}
    	sql = sql.substring(0,sql.length()-1);
    	sql += ")";
    	return sql;
    }
    
    private Object[] generateParameters(List<String> channelIdsNoDup, List<String> planCodesNoDup){
    	Object[] parameters = new Object[channelIdsNoDup.size()+planCodesNoDup.size()];
    	int indx = 0;
    	for(String channelId : channelIdsNoDup){
    		parameters[indx++] = channelId;
    	}
    	for(String planCode : planCodesNoDup){
    		parameters[indx++] = planCode;
    	}
    	return parameters;
    }

    public CommissionCalculationSession validateExistCalculationSession(ObjectId calculationSessionId) {
        CommissionCalculationSession commissionCalculationSession = commissionCalculationSessionRepository.findOne(calculationSessionId);
        if (commissionCalculationSession == null) {
            throw new CommissionCalculationSessionException("Not found calculation session " + calculationSessionId);
        }
        return commissionCalculationSession;
    }
    
    private CommissionPlan getCommissionPlanForCalculate(String channelCode, String planCode, String customerCategory){
    	CommissionPlan commissionPlan = new CommissionPlan();
    	for(CommissionPlan c:commissionPlans){
    		if(c.getUnitCode().equals(channelCode) &&
    			c.getPlanCode().equals(planCode) &&
    			c.getCustomerCategory().name().equals(customerCategory)){
    			commissionPlan = c;
    			break;
    		}
    	}
    	return commissionPlan;
    }
    
    private String getExistingAgentCodeStatus(String agentCode){
    	String status = BLANK;
    	if(!StringUtil.isBlank(agentCode)){
    		DecimalFormat dcm = new DecimalFormat("00000000000000");
    		Long convertLong = Long.parseLong(agentCode);
    		String convertString = dcm.format(convertLong);
    		String split1, split2, split3 = "";
    		split1 = convertString.substring(0, 6);
    		split2 = convertString.substring(6,8);
    		split3 = convertString.substring(8,14);
    		Integer int1, int2, int3 = 0;
    		int1 = Integer.parseInt(split1,10);
    		int2 = Integer.parseInt(split2,10);
    		int3 = Integer.parseInt(split3,10);
    		String realAgentCode = String.valueOf(int1) + String.valueOf(int2) + String.valueOf(int3);
        	String sql = " select " +
        				"ltrim(rtrim(agstu)) as status " +
        				"from [dbo].[AGKLCDTA_AGPCONT] " +
        				"where cast(agunt as varchar) + cast(aglvl as varchar) + cast(agagc as varchar) = ? ";
        	Object[] parameters = new Object[1];
            parameters[0] = realAgentCode;
            Map<String, Object> map = null;
            JdbcTemplate jdbcTemplate = new JdbcTemplate(cdbDataSource);
            jdbcTemplate.setQueryTimeout(600);
            try {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, parameters);
                if (list.size() != 0) {
                    status = String.valueOf(list.get(0).get("status"));
                }
            } catch (Exception e) {
                logger.error("Unable to query for agent code", e);
            }
    	}
    	return status;
    }
    
    private CommissionTargetGroup getCommissionTargetGroup(String type, List<CommissionTargetGroup> ctgList){
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
    	CommissionTargetEntity outCtr = new CommissionTargetEntity();
    	List<CommissionTargetEntity> entityGroup = commissionTargetGroup.getTargetEntities();
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
    
    private void createCommissionResultExtractExcelFileLine(Sheet sheet, CommissionCalculation commission, CommissionResult commissionResult) {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
    	ExcelUtils.appendRow(sheet,
                text(String.valueOf(commissionResult.getCommissionMonth())),
                text(commission.getPolicyNo()),
                text(commission.getPolicyStatus()),
                text(commission.getPlanCode()),
                text(commission.getPaymentCode()),
                text(commission.getAgentCode()),
                text(commission.getCustomerCategory()),
                text(commission.getPreviousPolicyNo()),
                text(commission.getExistingAgentCode1()),
                text(commission.getExistingAgentCode1Status()),
                text(commission.getExistingAgentCode2()),
                text(commission.getExistingAgentCode2Status()),
                text(String.valueOf(commission.getFirstYearPremium())),
                text(String.valueOf(commission.getFirstYearCommission())),
                text(String.valueOf(commission.getFyAffiliateCommission())),
                text(String.valueOf(commission.getFyDistribution1Commission())),
                text(String.valueOf(commission.getFyDistribution2Commission())),
                text(String.valueOf(commission.getFyTsrCommission())),
                text(String.valueOf(commission.getFyMarketingCommission())),
                text(String.valueOf(commission.getFyCompanyCommission())),
                text(String.valueOf(commission.getOvAffiliateCommission())),
                text(String.valueOf(commission.getOvDistribution1Commission())),
                text(String.valueOf(commission.getOvDistribution2Commission())),
                text(String.valueOf(commission.getOvTsrCommission())),
                text(String.valueOf(commission.getOvMarketingCommission())),
                text(String.valueOf(commission.getOvCompanyCommission())),
                text(String.valueOf(commission.getFyAffiliateRate())),
                text(String.valueOf(commission.getFyDistributionRate())),
                text(String.valueOf(commission.getFyTsrRate())),
                text(String.valueOf(commission.getFyMarketingRate())),
                text(String.valueOf(commission.getFyCompanyRate())),
                text(String.valueOf(commission.getOvAffiliateRate())),
                text(String.valueOf(commission.getOvDistributiionRate())),
                text(String.valueOf(commission.getOvTsrRate())),
                text(String.valueOf(commission.getOvMarketingRate())),
                text(String.valueOf(commission.getOvCompanyRate())),
                text(commissionResult.getUpdatedDateTime().format(formatter)));
    }
  
    private String getRowId(LocalDateTime now){
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    	return "C"+now.format(formatter);
    }    

}
