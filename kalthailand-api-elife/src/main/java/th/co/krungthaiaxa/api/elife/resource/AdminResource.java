package th.co.krungthaiaxa.api.elife.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;

import javax.inject.Inject;

@RestController
public class AdminResource {
    private final static Logger logger = LoggerFactory.getLogger(AdminResource.class);
    private final PolicyService policyService;
    private final DocumentService documentService;

    @Inject
    public AdminResource(PolicyService policyService, DocumentService documentService) {
        this.policyService = policyService;
        this.documentService = documentService;
    }

}
