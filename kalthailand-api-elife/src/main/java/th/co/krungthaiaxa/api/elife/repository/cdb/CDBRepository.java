package th.co.krungthaiaxa.api.elife.repository.cdb;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.common.utils.StringUtil;
import th.co.krungthaiaxa.api.elife.commission.data.cdb.CDBPolicyCommissionEntity;
import th.co.krungthaiaxa.api.elife.utils.JdbcHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CDBRepository {
    private final static Logger LOGGER = LoggerFactory.getLogger(CDBRepository.class);
    @Autowired
    private JdbcHelper jdbcHelper;

//
//    @Autowired
//    @Qualifier("cdbDataSource")
//    private DataSource cdbDataSource;

    @Autowired
    @Qualifier("cdbTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * @return Left part is the previous policy number, middle part is first agent code, right part is the second agent code
     */
    public Optional<Triple<String, String, String>> getExistingAgentCode(String idCard, String dateOfBirth) {
        if (StringUtils.isBlank(idCard) || StringUtils.isBlank(dateOfBirth)) {
            return Optional.empty();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%1$s] .....", "getExistingAgentCode"));
            LOGGER.debug(String.format("idCard is %1$s", idCard));
            LOGGER.debug(String.format("dateOfBirth is %1$s", dateOfBirth));
        }
        String sql = StringUtil.newString("select top 1 pno, ",
                " case cast(coalesce(pagt1,0) as varchar) when '0' then 'NULL' else cast(coalesce(pagt1,0) as varchar) end as pagt1, ",
                " case cast(coalesce(pagt2,0) as varchar) when '0' then 'NULL' else cast(coalesce(pagt2,0) as varchar) end as pagt2 ",
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
        parameters[0] = idCard;
        parameters[1] = dateOfBirth;
        Map<String, Object> map = null;
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, parameters);
            if (!list.isEmpty()) {
                map = list.get(0);
            }
        } catch (Exception e) {
            LOGGER.error("Unable to query for agent code: " + e.getMessage(), e);
        }

        if (map == null) {
            return Optional.of(Triple.of("NULL", "NULL", "NULL"));
        } else {
            return Optional.of(Triple.of((String) map.get("pno"), (String) map.get("pagt1"), (String) map.get("pagt2")));
        }
    }

    public List<CDBPolicyCommissionEntity> findPoliciesByChannelIdsAndPaymentModeIds(List<String> channelIdsNoDup, List<String> planCodesNoDup) {
        RowMapper<CDBPolicyCommissionEntity> rowMapper = jdbcHelper.getRowMapper(CDBPolicyCommissionEntity.class);
        List<CDBPolicyCommissionEntity> policies = jdbcTemplate.query(generateSql(channelIdsNoDup, planCodesNoDup), generateParameters(channelIdsNoDup, planCodesNoDup), rowMapper);
        return policies;
    }

    private String generateSql(List<String> channelIdsNoDup, List<String> planCodesNoDup) {
        String channelIdsParams = StringUtil.joinStrings(",", "?", channelIdsNoDup.size());
        String planCodesParams = StringUtil.joinStrings(",", "?", planCodesNoDup.size());
        String sql = StringUtil.newString("select ",
                "ltrim(rtrim(a.pno)) as policyNumber, ",
                "ltrim(rtrim(a.pstu)) as policyStatus, ",
                "ltrim(rtrim(a.lplan)) as planCode, ",
                "ltrim(rtrim(a.pmode)) as paymentCode, ",
                "ltrim(rtrim(a.pagt1)) as agentCode, ",
                "b.g3bsp1 as firstYearPremium, ",
                "b.g3bsc1 as firstYearCommission ",
                "from [dbo].[LFKLUDTA_LFPPML] a ",
                "inner join [dbo].[LFKLUDTA_LFPPMSWK] b ",
                "on a.pno = b.g3pno ",
                "where ",
                "left(ltrim(rtrim(right('00000000000000' + cast(a.pagt1 as varchar),14))),6) in (",
                channelIdsParams,
                ") and ltrim(rtrim(a.lplan)) in (",
                planCodesParams,
                ")");
        LOGGER.trace("sql: {}", sql);
        return sql;
    }

    private Object[] generateParameters(List<String> channelIdsNoDup, List<String> planCodesNoDup) {
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
}
