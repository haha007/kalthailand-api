package th.co.krungthaiaxa.api.elife.resource;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import io.swagger.*;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.service.BlackListedService;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

@RestController
public class BlackListedResource {
	private final static Logger logger = LoggerFactory.getLogger(BlackListedResource.class);	
	
	private final BlackListedService blackListedService;
	
	@Inject
    public BlackListedResource(BlackListedService blackListedService) {
        this.blackListedService = blackListedService;
    }
	
    @RequestMapping(value = "/blacklist/check/{thaiId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public String documentsOfPolicy(
            @PathVariable String thaiId) {        
        return String.valueOf(blackListedService.isBlackListed(thaiId));
    }

}
