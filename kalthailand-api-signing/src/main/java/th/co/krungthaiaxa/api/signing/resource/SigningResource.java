package th.co.krungthaiaxa.api.signing.resource;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.signing.service.SigningService;

import javax.inject.Inject;

@RestController
@Api(value = "Document Signing")
public class SigningResource {
    private final static Logger logger = LoggerFactory.getLogger(SigningResource.class);

    private final SigningService signingService;

    @Inject
    public SigningResource(SigningService signingService) {
        this.signingService = signingService;
    }

}
