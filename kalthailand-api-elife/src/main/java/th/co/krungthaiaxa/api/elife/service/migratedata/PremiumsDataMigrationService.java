package th.co.krungthaiaxa.api.elife.service.migratedata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quotable;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 10/27/16.
 */
@Service
public class PremiumsDataMigrationService {
    public static final Logger LOGGER = LoggerFactory.getLogger(PremiumsDataMigrationService.class);
    private static final String INFORM_EMAIL = "khoi.tran@pyramid-consulting.com";
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ElifeEmailService elifeEmailService;

    //When you put @Transactional here, you cannot start web app when you cannot connect to CDB!
    //    @Transactional
    public void migrateData() {
        copyDataToOldStructure();
    }

    private void copyDataToOldStructure() {
        copyDataToOldStructureForPolicies();
        copyDataToOldStructureForQuotes();
    }

    private void copyDataToOldStructureForPolicies() {
        List<Policy> policies = policyRepository.findByPremiumsDataNull();
        copyDataToOldStructureForQuotableItems(policies);
        if (!policies.isEmpty()) {
            policyRepository.save(policies);
        }
        sendNotificationForMigrationSuccess("[eLife] Migrate policies", policies);
    }

    private void copyDataToOldStructureForQuotes() {
        List<Quote> quotes = quoteRepository.findByPremiumsDataNull();
        copyDataToOldStructureForQuotableItems(quotes);
        if (!quotes.isEmpty()) {
            quoteRepository.save(quotes);
        }
        sendNotificationForMigrationSuccess("[eLife] Migrate quotes", quotes);
    }

    private void copyDataToOldStructureForQuotableItems(List<? extends Quotable> quotableItems) {
        String quotableItemsString = quotableItems.stream().map(o -> String.format("\t%s: {policyId: %s, quoteId: %s}", o.getClass().getSimpleName(), o.getPolicyId(), o.getQuoteId())).collect(Collectors.joining("\n"));
        LOGGER.debug("Migrate quotable items [start] \n" + quotableItemsString);
        for (Quotable policy : quotableItems) {
            if (policy.getPremiumData() != null) {
                policy.setPremiumsData(policy.getPremiumData());
                policy.setPremiumData(null);
            } else {
                String msg = String.format("The data is wrong: there's no both premiumsData and premiumData: %s: {policyId: %s, quoteId: %s}", policy.getClass().getSimpleName(), policy.getPolicyId(), policy.getQuoteId());
                LOGGER.error(msg);
            }
        }
        LOGGER.debug("Migrate quotable items [stop] \n" + quotableItemsString);
    }

    private void sendNotificationForMigrationSuccess(String emailSubject, List<? extends Quotable> quotableItems) {
        try {
            String quotableItemRows = quotableItems.stream()
                    .map(quotableItem -> String.format("<tr><td>%s</td><td>%s</td></tr>\n", quotableItem.getPolicyId(), quotableItem.getQuoteId()))
                    .collect(Collectors.joining());
            String emailContent = String.join(""
                    , "<h2>", emailSubject, "<h2><br/>\n"
                    , "<table>\n"
                    , "<tr><td>Policy number</td><td>Quote number</td></tr>\n"
                    , quotableItemRows
                    , "</table>"
            );
            elifeEmailService.sendEmail(INFORM_EMAIL, emailSubject, emailContent, Collections.emptyList(), Collections.emptyList());
        } catch (Exception ex) {
            LOGGER.error("Unexpected exception when sending notification migration successfully. " + ex.getMessage(), ex);
        }
    }

}