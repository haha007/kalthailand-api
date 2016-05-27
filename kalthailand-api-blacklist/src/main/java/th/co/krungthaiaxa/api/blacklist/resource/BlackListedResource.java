package th.co.krungthaiaxa.api.blacklist.resource;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static th.co.krungthaiaxa.api.blacklist.model.ErrorCode.*;
import static th.co.krungthaiaxa.api.blacklist.utils.JsonUtil.getJson;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.*;
import th.co.krungthaiaxa.api.blacklist.exception.ElifeException;
import th.co.krungthaiaxa.api.blacklist.model.Error;
import th.co.krungthaiaxa.api.blacklist.model.ErrorCode;
import th.co.krungthaiaxa.api.blacklist.service.BlackListedService;
import th.co.krungthaiaxa.api.blacklist.utils.JsonUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Api(value = "Blacklist")
public class BlackListedResource {
	private final static Logger logger = LoggerFactory.getLogger(BlackListedResource.class);	
	
	private final BlackListedService blackListedService;
	
	@Inject
    public BlackListedResource(BlackListedService blackListedService) {
        this.blackListedService = blackListedService;
    }
	
	@ApiOperation(value = "Checking a blacklist", notes = "Checking a blacklist. Response is Boolean true for is blacklisted and false if not blacklisted", response = boolean.class)
	@ApiResponses({
	        @ApiResponse(code = 406, message = "If given thai ID not in number format", response = Error.class)
	})
	@RequestMapping(value = "/blacklist/isblacklist", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> checkBlacklist(
    		@ApiParam(value = "The thai ID to check blacklist")
    		@RequestParam String thaiId) {
    	
    	try {
			blackListedService.checkThaiIdFormat(thaiId);
		} catch (Exception e) {
			logger.error("Thai ID is invalid.", e);
			return new ResponseEntity<>(getJson(INVALID_THAI_ID_FORMAT.apply(e.getMessage())), NOT_ACCEPTABLE);
		}
    	
    	try {
    		blackListedService.checkThaiIdLength(thaiId);
		} catch (ElifeException e) {
			logger.error("Thai ID is invalid.", e);
			return new ResponseEntity<>(getJson(INVALID_THAI_ID_LENGTH.apply(e.getMessage())), NOT_ACCEPTABLE);
		}
    	
        return new ResponseEntity<>(getJson(blackListedService.isBlackListed(thaiId)), OK);
        
    }

}
