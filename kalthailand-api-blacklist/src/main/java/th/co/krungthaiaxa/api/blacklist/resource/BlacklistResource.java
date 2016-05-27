package th.co.krungthaiaxa.api.blacklist.resource;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.api.blacklist.utils.JsonUtil.getJson;


import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import io.swagger.annotations.*;
import th.co.krungthaiaxa.api.blacklist.service.BlackListedService;


@RestController
public class BlacklistResource {
	
	private final static Logger logger = LoggerFactory.getLogger(BlacklistResource.class);
	
	private final BlackListedService blackListedService;
	
	@Inject
	public BlacklistResource(BlackListedService blackListedService) {
	    this.blackListedService = blackListedService;
	}
	
	@RequestMapping(value = "/check", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> createPolicy(
            @RequestParam String thaiId) {        
        return new ResponseEntity<>(getJson(""), OK);
    }

}
