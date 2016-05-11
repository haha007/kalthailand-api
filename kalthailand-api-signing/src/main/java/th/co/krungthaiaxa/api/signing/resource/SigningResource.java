package th.co.krungthaiaxa.api.signing.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.signing.service.SigningService;
import th.co.krungthaiaxa.elife.api.model.LineBC;
import th.co.krungthaiaxa.elife.api.model.Mid;
import th.co.krungthaiaxa.elife.api.service.LineBCService;
import th.co.krungthaiaxa.elife.api.utils.Decrypt;

import javax.inject.Inject;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.elife.api.utils.JsonUtil.getJson;

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
