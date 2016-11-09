package th.co.krungthaiaxa.api.elife.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quotable;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 11/7/16.
 *         Migrate for version 1.10.0
 */
@Service
public class AtpModeMigration {
    public static final Logger LOGGER = LoggerFactory.getLogger(AtpModeMigration.class);
    private final PolicyRepository policyRepository;
    private final QuoteRepository quoteRepository;

    @Autowired
    public AtpModeMigration(PolicyRepository policyRepository, QuoteRepository quoteRepository) {
        this.policyRepository = policyRepository;
        this.quoteRepository = quoteRepository;
    }

    @PostConstruct
    public void migrate() {
        migrateAtpModeForMonthlyPaymentForPolicies();
        migrateAtpModeForMonthlyPaymentForQuotes();
    }

    public void migrateAtpModeForMonthlyPaymentForPolicies() {
        //TODO must seclect by periodicity and atpMode is null: implemented but not tested yet.
        List<Policy> quotables = policyRepository.findByPeriodicityCodeAndAtpModeNull(PeriodicityCode.EVERY_MONTH);
        migrateForQuotables(quotables);
        policyRepository.save(quotables);
        printMigrate(quotables);
    }

    public void migrateAtpModeForMonthlyPaymentForQuotes() {
        List<Quote> quotables = quoteRepository.findByPeriodicityCodeAndAtpModeNull(PeriodicityCode.EVERY_MONTH);
        migrateForQuotables(quotables);
        quoteRepository.save(quotables);
        printMigrate(quotables);
    }

    private void migrateForQuotables(List<? extends Quotable> quotableList) {
        for (Quotable quotable : quotableList) {
            quotable.getPremiumsData().getFinancialScheduler().setAtpMode(AtpMode.AUTOPAY.getNumValue());
        }
    }

    private void printMigrate(List<? extends Quotable> quotables) {
        String msg = quotables.stream().map(
                quotable -> String.format(quotable.getClass().getSimpleName() + "\t QuoteID: " + quotable.getPolicyId() + ", PolicyID: " + quotable.getPolicyId() + ", Periodicity: " + ProductUtils.getPeriodicityCode(quotable)))
                .collect(Collectors.joining("\n"));
        LOGGER.debug(msg);
    }
}
