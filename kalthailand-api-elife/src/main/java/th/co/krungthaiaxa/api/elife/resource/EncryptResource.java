package th.co.krungthaiaxa.api.elife.resource;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.service.EncryptService;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Api(value = "Encrypt")
public class EncryptResource {

    private final static Logger logger = LoggerFactory.getLogger(EncryptResource.class);
    private final EncryptService encryptService;

    @Inject
    public EncryptResource(EncryptService encryptService) {
        this.encryptService = encryptService;
    }

    @ApiOperation(value = "Encrypt Existing Plain Text Data for Insure and Benefit thai Id and Line Pay Reg Key.", notes = "Encrypt Existing Plain Text Data for Insure and Benefit thai Id and Line Pay Reg Key.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If encrypt not success.", response = Error.class)
    })
    @RequestMapping(value = "/data-migration/encrypt", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public void encryptThaiIdAndRegistrationKey() {
        encryptService.encryptRegistrationIdAndRegistrationKey();
    }

}
