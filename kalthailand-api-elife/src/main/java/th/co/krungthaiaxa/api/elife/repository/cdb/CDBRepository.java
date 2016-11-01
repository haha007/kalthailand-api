package th.co.krungthaiaxa.api.elife.repository.cdb;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Repository
public class CDBRepository {
    private final static Logger logger = LoggerFactory.getLogger(CDBRepository.class);

    @Autowired
    @Qualifier("cdbDataSource")
    private DataSource cdbDataSource;

    /**
     * @return Left part is the previous policy number, middle part is first agent code, right part is the second agent code
     */
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

}