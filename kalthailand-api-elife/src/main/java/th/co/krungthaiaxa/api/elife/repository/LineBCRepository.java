package th.co.krungthaiaxa.api.elife.repository;

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

/**
 * Created by SantiLik on 4/4/2016.
 */
@Repository
public class LineBCRepository {
    private final static Logger logger = LoggerFactory.getLogger(LineBCRepository.class);

    @Autowired
    @Qualifier("lineBCDataSource")
    private DataSource lineBCDataSource;

    /**
     * @return Optional<Map<String,Object>> represent line bc information along with line MID
     */
    public Optional<List<Map<String,Object>>> getLineBC (String mid){

        if(isBlank(mid)){
            return Optional.empty();
        }

        if(logger.isDebugEnabled()){
            logger.debug(String.format("[%1$s] .....","getLineBC"));
            logger.debug(String.format("MID is %1$s",mid));
        }

        String sql = " SELECT " +
                " COALESCE(CONVERT(VARCHAR(19),a.dob,103),'') as dob, " +
                " COALESCE(a.pid,'') as pid, " +
                " COALESCE(a.mobile,'') as mobile, " +
                " COALESCE(a.email,'') as email, " +
                " first_name, " +
                " last_name " +
                " FROM lbc_cus_info a " +
                " WHERE a.mid = ? ";
        Object[] parameters = new Object[1];
        parameters[0] = mid;
        List<Map<String,Object>> list = null;
        JdbcTemplate jdbcTemplate = new JdbcTemplate(lineBCDataSource);

        try{
            list = jdbcTemplate.queryForList(sql, parameters);
        }catch(Exception e){
            logger.error("Unable to get Line BC information along with [" + mid + "] .....", e);
        }

        if (list == null || list.size() == 0) {
            return Optional.empty();
        }else{
            return Optional.of(list);
        }

    }
}
