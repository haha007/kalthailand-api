package th.co.krungthaiaxa.api.elife.service.migratedata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Policy;
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
        for (Policy policy : policies) {
            if (policy.getPremiumData() != null) {
                policy.setPremiumsData(policy.getPremiumData());
                policy.setPremiumData(null);
            } else {
                LOGGER.error("The data of policy is wrong: there's no both premiumsData and premiumData: policyId: {}", policy.getPolicyId());
            }
        }
        if (!policies.isEmpty()) {
            policyRepository.save(policies);
        }
        sendNotificationForMigrationSuccessPolicies(policies);
    }

    private void copyDataToOldStructureForQuotes() {
        List<Quote> quotes = quoteRepository.findByPremiumsDataNull();
        for (Quote quote : quotes) {
            if (quote.getPremiumData() != null) {
                quote.setPremiumsData(quote.getPremiumData());
                quote.setPremiumData(null);
            } else {
                LOGGER.error("The data of quote is wrong: there's no both premiumsData and premiumData: policyId: {}, quoteId: {}", quote.getPolicyId(), quote.getQuoteId());
            }
        }
        if (!quotes.isEmpty()) {
            quoteRepository.save(quotes);
        }
        sendNotificationForMigrationSuccessQuotes(quotes);
    }

    private void sendNotificationForMigrationSuccessPolicies(List<Policy> policies) {
        try {
            List<String> policyNumbers = policies.stream().map(policy -> policy.getPolicyId()).collect(Collectors.toList());
            sendNotificationForMigrationSuccess("Migrate policies", policyNumbers);
        } catch (Exception ex) {
            LOGGER.error("Unexpected exception when sending notification migration successfully. " + ex.getMessage(), ex);
        }
    }

    private void sendNotificationForMigrationSuccessQuotes(List<Quote> quotes) {
        try {
            List<String> policyNumbers = quotes.stream().map(policy -> policy.getPolicyId()).collect(Collectors.toList());
            sendNotificationForMigrationSuccess("Migrate quotes", policyNumbers);
        } catch (Exception ex) {
            LOGGER.error("Unexpected exception when sending notification migration successfully. " + ex.getMessage(), ex);
        }
    }

    private void sendNotificationForMigrationSuccess(String title, List<String> policyNumbers) {
        if (!policyNumbers.isEmpty()) {
            LOGGER.error("After migration, still find the quotes with premiumsData null: " + policyNumbers.size());
        }
        String policyNumbersString = policyNumbers.stream().collect(Collectors.joining("\n"));
        String emailContent = "Migrate successfully: \n" + policyNumbersString;
        elifeEmailService.sendEmail("khoi.tran@pyramid-consulting.com", title, emailContent, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }
}