package th.co.krungthaiaxa.api.elife.repository.cdb;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.common.utils.StringUtil;
import th.co.krungthaiaxa.api.elife.commission.data.cdb.CDBPolicyCommissionEntity;
import th.co.krungthaiaxa.api.elife.model.PreviousPolicy;
import th.co.krungthaiaxa.api.elife.utils.JdbcHelper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;

@Repository
public class CDBRepository {
    private final static Logger LOGGER = LoggerFactory.getLogger(CDBRepository.class);
    @Autowired
    private JdbcHelper jdbcHelper;

    @Autowired
    @Qualifier("cdbTemplate")
    private JdbcTemplate jdbcTemplate;

    public Optional<PreviousPolicy> findLastActivatingPreviousPolicy(String insuredRegistrationId, LocalDate insuredDateOfBirth) {
        String insuredDOBString = insuredDateOfBirth.format(ofPattern("yyyyMMdd"));
        return findLastActivatingPreviousPolicy(insuredRegistrationId, insuredDOBString);
    }

    /**
     * @param insuredRegistrationId this is also the thaiId
     * @return Left part is the previous policy number, middle part is first agent code, right part is the second agent code
     */
    public Optional<PreviousPolicy> findLastActivatingPreviousPolicy(String insuredRegistrationId, String dateOfBirth) {
        Instant start = LogUtil.logStarting(String.format("findLastActivatingPreviousPolicy. registrationId: %s, dateOfBirth: %s", insuredRegistrationId, dateOfBirth));
        Optional<PreviousPolicy> result;
        if (StringUtils.isBlank(insuredRegistrationId) || StringUtils.isBlank(dateOfBirth)) {
            result = Optional.empty();
        }else {
            //TODO need to refactor.
            String sql = StringUtil.newString("select top 1 pno as policyNumber, ",
                    " case cast(coalesce(pagt1,0) as varchar) when '0' then 'NULL' else cast(coalesce(pagt1,0) as varchar) end as agentCode1, ",
                    " case cast(coalesce(pagt2,0) as varchar) when '0' then 'NULL' else cast(coalesce(pagt2,0) as varchar) end as agentCode2 ",
                    "from lfkludta_lfppml ",
                    "where left(coalesce(pagt1,'0'),1) not in ('2','4') ",
                    "and left(coalesce(pagt2,'0'),1) not in ('2','4') ",
                    "and pterm = 0 ",
                    "and pstu in ('1') ",
                    "and ? = ",
                    "case when ltrim(rtrim(coalesce(pownid,''))) <> '' and lpaydb <> 0 ",
                    "then ltrim(rtrim(coalesce(pownid,''))) else ltrim(rtrim(coalesce(pid,''))) end ",
                    "and ? = ",
                    "case when ltrim(rtrim(coalesce(pownid,''))) <> '' and lpaydb <> 0 ",
                    "then lpaydb else pdob end ",
                    "order by pdoi desc");
            Object[] parameters = new Object[2];
            parameters[0] = insuredRegistrationId;
            parameters[1] = dateOfBirth;

            BeanPropertyRowMapper<PreviousPolicy> beanPropertyRowMapper = BeanPropertyRowMapper.newInstance(PreviousPolicy.class);
            List<PreviousPolicy> previousPolicies = jdbcTemplate.query(sql, parameters, beanPropertyRowMapper);
            //Cleanup data
            ListIterator<PreviousPolicy> listIterator = previousPolicies.listIterator();
            while (listIterator.hasNext()) {
                PreviousPolicy previousPolicy = listIterator.next();
                if (!isExist(previousPolicy.getPolicyNumber())) {
                    previousPolicy.setPolicyNumber(null);
                }
                if (!isExist(previousPolicy.getAgentCode1())) {
                    previousPolicy.setAgentCode1(null);
                }
                if (!isExist(previousPolicy.getAgentCode2())) {
                    previousPolicy.setAgentCode2(null);
                }
                if (previousPolicy.getPolicyNumber() == null && previousPolicy.getAgentCode1() == null && previousPolicy.getAgentCode2() == null) {
                    listIterator.remove();
                }
            }
            if (previousPolicies.isEmpty()) {
                result = Optional.empty();
            } else {
                result = Optional.of(previousPolicies.get(0));
            }
        }
        return result;
    }

    private boolean isExist(String value) {
        return StringUtils.isNotBlank(value) && !value.equalsIgnoreCase("NULL");
    }

    /**
     * @param unitCodes related to agentCode: The agentCode which start with "40002" works for only eBiz policies.
     * @param planCodes related to product & product package
     * @return
     */
    public List<CDBPolicyCommissionEntity> findPolicyCommissionsByUnitCodesAndPlanCodes(List<String> unitCodes, List<String> planCodes) {
        Instant start = LogUtil.logStarting("[Commission-Query from CDB][start]");
        RowMapper<CDBPolicyCommissionEntity> rowMapper = jdbcHelper.getRowMapper(CDBPolicyCommissionEntity.class);
        List<CDBPolicyCommissionEntity> policiesFirstHalftMonth = jdbcTemplate.query(constructSqlFindPolicyCommissionByUnitCodesAndPlanCodes("LFKLUDTA_LFPPMSWK", unitCodes, planCodes), constructParamsFindPolicyCommissionByUnitCodesAndPlanCodes(unitCodes, planCodes), rowMapper);
        start = LogUtil.logFinishing(start, "[Commission-Query from CDB][finish], first halft " + policiesFirstHalftMonth.size());
        List<CDBPolicyCommissionEntity> policiesLastHalfMonth = jdbcTemplate.query(constructSqlFindPolicyCommissionByUnitCodesAndPlanCodes("LFKLUHST_LFPPMSWK1", unitCodes, planCodes), constructParamsFindPolicyCommissionByUnitCodesAndPlanCodes(unitCodes, planCodes), rowMapper);
        start = LogUtil.logFinishing(start, "[Commission-Query from CDB][finish], last halft " + policiesLastHalfMonth.size());
        List<CDBPolicyCommissionEntity> policies = new ArrayList<>();
        policies.addAll(policiesFirstHalftMonth);
        policies.addAll(policiesLastHalfMonth);
        LogUtil.logFinishing(start, "[Commission-Query from CDB][finish] " + policies.size());
        return policies;
    }

//    SELECT b.pno, b.lplan, b.pmode, b.pagt1 FROM [dbo].LFKLUDTA_LFPPMSWK as a
//    JOIN [dbo].[LFKLUDTA_LFPPML] as b ON a.g3pno = b.pno AND (b.pagt1 LIKE '%40002%' OR b.pagt1 LIKE '%20002%') AND a.g3bsp1 <> 0;
//    --select * from dbo.LFKLUTAB_TABPLAN where PLANC LIKE '%10IG%';

    /**
     * The agentCode example: 4000204078814
     * LFKLUDTA_LFPPML: table contains agentCode and policyNumber
     * LFKLUDTA_LFPPMSWK: commission in a middle of month
     * "KTALDB"."dbo"."LFKLUHST_LFPPMSWK1"
     *
     * @param channelIdsNoDup
     * @param planCodesNoDup
     * @return
     */
    private String constructSqlFindPolicyCommissionByUnitCodesAndPlanCodes(String commissionTableName, List<String> channelIdsNoDup, List<String> planCodesNoDup) {
        String channelIdsParams = StringUtil.joinStrings(",", "?", channelIdsNoDup.size());
        String planCodesParams = StringUtil.joinStrings(",", "?", planCodesNoDup.size());
        String sql = StringUtil.newString(
                "SELECT ",
                "ltrim(rtrim(a.pno)) as policyNumber, ",
                "ltrim(rtrim(a.pstu)) as policyStatus, ",
                "ltrim(rtrim(a.lplan)) as planCode, ",
                "ltrim(rtrim(a.pmode)) as paymentCode, ",
                "ltrim(rtrim(a.pagt1)) as agentCode, ",

                "b.g3bsp1 as firstYearPremium, ",
                "b.g3bsc1 as firstYearCommission ",
                "FROM [dbo].[LFKLUDTA_LFPPML] as a ",
                //Don't use g3bsp1 > 0 because some value is less than zero (payment is canceled)
                "INNER JOIN [dbo].[", commissionTableName, "] as b on a.pno = b.g3pno AND b.g3bsp1 <> 0 ",
                "WHERE ",
                "left(ltrim(rtrim(right('00000000000000' + cast(a.pagt1 as varchar),14))),6) in (",
                channelIdsParams,
                ") and ltrim(rtrim(a.lplan)) in (",
                planCodesParams,
                ")");
        LOGGER.trace("sql: {}", sql);
        return sql;
    }

    private Object[] constructParamsFindPolicyCommissionByUnitCodesAndPlanCodes(List<String> channelIdsNoDup, List<String> planCodesNoDup) {
        Object[] parameters = new Object[channelIdsNoDup.size() + planCodesNoDup.size()];
        int indx = 0;
        for (String channelId : channelIdsNoDup) {
            parameters[indx++] = channelId;
        }
        for (String planCode : planCodesNoDup) {
            parameters[indx++] = planCode;
        }
        return parameters;
    }

    public String getExistingAgentCodeStatus(String agentCode) {
        String status = "";
        if (!StringUtils.isBlank(agentCode)) {
            String sql = StringUtil.newString(" select ",
                    "ltrim(rtrim(agstu)) as status ",
                    "from [dbo].[AGKLCDTA_AGPCONT] ",
                    "where right('000000' + cast(agunt as varchar),6) + right('00' + cast(aglvl as varchar),2) + right('000000' + cast(agagc as varchar),6) = ? ");
            LOGGER.trace("sql:" + sql);
            Object[] parameters = new Object[1];
            parameters[0] = agentCode;
            Map<String, Object> map = null;
            try {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, parameters);
                if (list.size() != 0) {
                    status = String.valueOf(list.get(0).get("status"));
                }
            } catch (Exception e) {
                LOGGER.error("Unable to query for agent code: " + e.getMessage(), e);
            }
        }
        return status;
    }

    /**
     * Get payment due date by policyId.
     * @param policyId
     * @return
     */
    public String getPaymentDueDate(String policyId){
        String paymentDueDate = "";
        if (!StringUtils.isBlank(policyId)) {
            String sql = StringUtil.newString(" select pno, pptd from [dbo].[LFKLUDTA_LFPPML] where pno = ? ");
            LOGGER.trace("sql:" + sql);
            Object[] parameters = new Object[1];
            parameters[0] = policyId;
            Map<String, Object> map = null;
            try {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, parameters);
                if (list.size() != 0) {
                    paymentDueDate = String.valueOf(list.get(0).get("pptd"));
                }
            } catch (Exception e) {
                LOGGER.error("Unable to query for policyId: " + policyId, e);
            }
        }
        return paymentDueDate;
    }
}
