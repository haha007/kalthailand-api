package th.co.krungthaiaxa.api.elife.repository.cdb;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author tuong.le on 3/10/17.
 */
@Repository
public class CDBViewRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CDBViewRepository.class);
    private static final String CDB_VIEW_DUE_DATE_FIELD = "pptd";
    private static final String QUERY_DUE_DATE_CDB_VIEW =
            "SELECT " + CDB_VIEW_DUE_DATE_FIELD +
                    " FROM dbo.DB24_VIEW_LFKLUDTA_LFPPML WHERE pno = ?";

    @Autowired
    @Qualifier("cdbViewTemplate")
    private JdbcTemplate cdbViewTemplate;

    /**
     * Get payment due date by policyId.
     *
     * @param policyId policy Id
     * @return payment due date of policy Id
     */
    public String getPaymentDueDate(final String policyId) {
        if (!StringUtils.isBlank(policyId)) {
            try {
                LOGGER.trace("sql: {}", QUERY_DUE_DATE_CDB_VIEW);
                final List<Map<String, Object>> list = cdbViewTemplate
                        .queryForList(QUERY_DUE_DATE_CDB_VIEW, Collections.singletonList(policyId).toArray());
                if (!Objects.isNull(list) && !list.isEmpty()) {
                    final String paymentDueDate = String.valueOf(list.get(0).get(CDB_VIEW_DUE_DATE_FIELD));
                    LOGGER.info("Payment DueDate in CDBView is {} for policyId {}", paymentDueDate, policyId);
                    return paymentDueDate;
                }
            } catch (final Exception e) {
                LOGGER.error("Unable to query for policyId: {}", policyId, e);
            }
        }
        return StringUtils.EMPTY;
    }
}
