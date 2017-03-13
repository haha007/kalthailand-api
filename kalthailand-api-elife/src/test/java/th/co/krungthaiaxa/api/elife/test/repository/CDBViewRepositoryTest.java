package th.co.krungthaiaxa.api.elife.test.repository;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBViewRepository;

import javax.inject.Inject;
import java.util.Objects;

/**
 * @author tuong.le on 3/13/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CDBViewRepositoryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CDBViewRepositoryTest.class);

    @Inject
    private CDBViewRepository cdbViewRepository;

    @Test
    public void can_query_payment_dueDate_by_policyId() {
        String policyId = "505-8031971";
        final String dueDate = cdbViewRepository.getPaymentDueDate(policyId);
        if (StringUtils.isEmpty(dueDate)) {
            LOGGER.info("Could not found policyId {}", policyId);
            Assert.fail();
        } else {
            final String dueDateThaiDate =
                    DateTimeUtil.formatThaiDate(DateTimeUtil.toLocalDate(dueDate, DateTimeUtil.PATTERN_CDB_DUEDATE));
            LOGGER.info("CDB View DueDate {} as Thai Date for policy {}", dueDateThaiDate.toString(), policyId);
            Assert.assertTrue(!Objects.isNull(dueDateThaiDate));
        }
    }

}
