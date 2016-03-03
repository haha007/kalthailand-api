package th.co.krungthaiaxa.elife.api.repository;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
//import th.co.krungthaiaxa.elife.api.data.AgentData;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by santilik on 3/1/2016.
 */

@Repository
public class CDBRepository{

    private final static Logger logger = LoggerFactory.getLogger(CDBRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String,Object> getExistingAgentCode(String idCard, String dateOfBirth) {
        if(logger.isDebugEnabled()){
            logger.debug(String.format("[%1$s] .....","getExistingAgentCode"));
            logger.debug(String.format("idCard is %1$s",idCard));
            logger.debug(String.format("dateOfBirth is %1$s",dateOfBirth));
        }
        String sql = "select top 1 pno, pagt1, pagt2 " +
                "from lfkludta_lfppml " +
                "where left(coalesce(pagt1,'0'),1) not in ('2','4') " +
                "and left(coalesce(pagt2,'0'),1) not in ('2','4') " +
                "and pterm = 0 " +
                "and pstu in ('1','2','B') " +
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
        List<Map<String,Object>> l = null;
        Map<String,Object> m = null;
        if(!StringUtils.isBlank(idCard)&&!StringUtils.isBlank(dateOfBirth)){
            try{
                l = this.jdbcTemplate.queryForList(sql, parameters);
                if(l.size()!=0){
                    m = (Map<String,Object>) l.get(0);
                }
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        return  m;
    }

}
