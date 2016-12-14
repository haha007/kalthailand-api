package th.co.krungthaiaxa.api.elife.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.action.ActionLoopByPage;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.ProfileHelper;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PreviousPolicy;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 12/8/16.
 */
@Service
public class PreviousPolicyMigration {
    public static final Logger LOGGER = LoggerFactory.getLogger(PreviousPolicyMigration.class);
    private final PolicyRepository policyRepository;
    private final ProfileHelper profileHelper;
    private final ObjectMapper objectMapper;
    private final ElifeEmailService elifeEmailService;

    @Autowired
    public PreviousPolicyMigration(PolicyRepository policyRepository, ProfileHelper profileHelper, ObjectMapper objectMapper, ElifeEmailService elifeEmailService) {
        this.policyRepository = policyRepository;
        this.profileHelper = profileHelper;
        this.objectMapper = objectMapper;
        this.elifeEmailService = elifeEmailService;
    }

    @PostConstruct
    public void migratePreviousPolicy() {
        ActionLoopByPage<Policy> actionLoopByPage = new ActionLoopByPage<Policy>() {
            @Override
            protected List<Policy> executeEachPageData(Pageable pageRequest) {
                List<Policy> policies = policyRepository.findByInsuredPreviousPolicyNotNull(pageRequest);
                for (Policy policy : policies) {
                    for (Insured insured : policy.getInsureds()) {
                        List<String> previousInformations = insured.getInsuredPreviousInformations();
                        String policyNumber = previousInformations.get(0);
                        String agentCode1 = previousInformations.get(1);
                        String agentCode2 = previousInformations.get(2);

                        if (!isBlankValueOfPreviousPolicy(policyNumber) || !isBlankValueOfPreviousPolicy(agentCode1) || !isBlankValueOfPreviousPolicy(agentCode2)) {
                            PreviousPolicy previousPolicy = new PreviousPolicy();
                            previousPolicy.setPolicyNumber(policyNumber);
                            previousPolicy.setAgentCode1(agentCode1);
                            previousPolicy.setAgentCode2(agentCode2);

                            insured.setLastActivatingPreviousPolicy(previousPolicy);
                        }
                    }
                }
                if (!policies.isEmpty()) {
                    policyRepository.save(policies);
                }
                return policies;
            }
        };
        List<Policy> policies = actionLoopByPage.executeAllPages(50);
        try {
            List<String> policiesNumbers = policies.stream().map(policy -> policy.getPolicyId()).collect(Collectors.toList());
            byte[] previousPoliciesMigrationData = ObjectMapperUtil.toJsonBytes(objectMapper, policiesNumbers);
            elifeEmailService.sendEmail(
                    "khoi.tran.ags@gmail.com",
                    "[eLife][Migrate][" + profileHelper.getFirstUsingProfile() + "] Previous Policies_" + DateTimeUtil.formatNowForFilePath(),
                    "Policies: " + policiesNumbers.size(),
                    "migrated-previous-policies_" + DateTimeUtil.formatNowForFilePath() + ".json",
                    previousPoliciesMigrationData
            );
        } catch (Exception ex) {
            LOGGER.error("Cannot send email " + ex.getMessage(), ex);
        }
    }

    private boolean isBlankValueOfPreviousPolicy(String value) {
        return StringUtils.isBlank(value) || value.equalsIgnoreCase("NULL");
    }
}
