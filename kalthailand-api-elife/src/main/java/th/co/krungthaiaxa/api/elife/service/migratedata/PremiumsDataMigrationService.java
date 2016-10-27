package th.co.krungthaiaxa.api.elife.service.migratedata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;

import java.util.List;

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
        //TODO recheck there's no more policies with premiumsData is null.
        long countPoliciesWithPremiumDataNull = policyRepository.countByPremiumsDataNull();
        if (countPoliciesWithPremiumDataNull > 0) {
            LOGGER.error("After migration, still find the policies with premiumsData null: " + countPoliciesWithPremiumDataNull);
        }
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
        //TODO recheck there's no more policies with premiumsData is null.
        long countByPremiumDataNull = quoteRepository.countByPremiumsDataNull();
        if (countByPremiumDataNull > 0) {
            LOGGER.error("After migration, still find the quotes with premiumsData null: " + countByPremiumDataNull);
        }

    }
}