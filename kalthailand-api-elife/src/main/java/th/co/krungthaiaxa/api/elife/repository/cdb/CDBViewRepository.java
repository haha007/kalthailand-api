package th.co.krungthaiaxa.api.elife.repository.cdb;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.common.utils.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * @author tuong.le on 3/10/17.
 */
@Repository
public class CDBViewRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CDBViewRepository.class);

    @Autowired
    @Qualifier("cdbViewTemplate")
    private JdbcTemplate cdbViewTemplate;

    /**
     * Get payment due date by policyId.
     *
     * @param policyId
     * @return
     */
    public String getPaymentDueDate(String policyId) {
        String paymentDueDate = "";
        if (!StringUtils.isBlank(policyId)) {
            String sql = StringUtil.newString(" select pno, pptd from [dbo].[LFKLUDTA_LFPPML] where pno = ? ");
            LOGGER.trace("sql:" + sql);
            Object[] parameters = new Object[1];
            parameters[0] = policyId;
            Map<String, Object> map = null;
            try {
                List<Map<String, Object>> list = cdbViewTemplate.queryForList(sql, parameters);
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
