package th.co.krungthaiaxa.api.elife.service.migratedata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

import java.util.List;

/**
 * @author khoi.tran on 10/31/16.
 *         This class will generate the ereceipt numbers in the old ways (before 1.8.4 - 31-10-2016)
 */
@Service
public class EreceiptNumberOldPatternMigrationService {
    public static final Logger LOGGER = LoggerFactory.getLogger(EreceiptNumberOldPatternMigrationService.class);
    private final PolicyRepository policyRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public EreceiptNumberOldPatternMigrationService(PolicyRepository policyRepository, PaymentRepository paymentRepository) {
        this.policyRepository = policyRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * This method will run only one time.
     */
    public void generateEreceiptNumbersByOldPatternForOldPayments() {

    }

    private void generateEreceiptNumbersByOldPattern(String policyNumber, List<Payment> payments) {

    }

    private void generateEreceiptNumbersByOldPattern(String policyNumber, Payment payment) {

    }
}
