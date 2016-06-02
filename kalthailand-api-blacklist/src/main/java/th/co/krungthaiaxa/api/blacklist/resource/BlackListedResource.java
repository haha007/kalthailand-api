package th.co.krungthaiaxa.api.blacklist.resource;

import io.swagger.annotations.*;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import springfox.documentation.annotations.ApiIgnore;
import th.co.krungthaiaxa.api.blacklist.exception.ElifeException;
import th.co.krungthaiaxa.api.blacklist.model.Error;
import th.co.krungthaiaxa.api.blacklist.model.ErrorCode;
import th.co.krungthaiaxa.api.blacklist.service.BlackListedService;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.api.blacklist.model.ErrorCode.INVALID_THAI_ID_FORMAT;
import static th.co.krungthaiaxa.api.blacklist.model.ErrorCode.INVALID_THAI_ID_LENGTH;
import static th.co.krungthaiaxa.api.blacklist.utils.JsonUtil.getJson;

@RestController
@Api(value = "Blacklist")
public class BlackListedResource {
    private final static Logger logger = LoggerFactory.getLogger(BlackListedResource.class);

    private final BlackListedService blackListedService;

    @Inject
    public BlackListedResource(BlackListedService blackListedService) {
        this.blackListedService = blackListedService;
    }

    @ApiOperation(value = "Checking Thai ID is blacklisted", notes = "Checking is the given Thai ID is blacklisted. Response is Boolean true for is blacklisted and false otherwise", response = boolean.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If given thai ID is not in proper format", response = Error.class)
    })
    @RequestMapping(value = "/blacklist/isblacklist", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> checkBlacklist(
            @ApiParam(value = "The thai ID to check")
            @RequestParam(required = false) String thaiId) {
        try {
            blackListedService.checkThaiIdFormat(thaiId);
        } catch (ElifeException e) {
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

    @ApiIgnore
    @RequestMapping(value = "/admin/blackList", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> blackList(@RequestParam Integer pageNumber, @RequestParam Integer pageSize, @RequestParam String searchContent) {
        return new ResponseEntity<>(getJson(blackListedService.findAll(pageNumber, pageSize, searchContent)), OK);
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/blackList/upload", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> uploadBlackListFile(@RequestParam("file") MultipartFile file) {
        try {
            return new ResponseEntity<>(getJson(blackListedService.readBlackListedExcelFile(file.getInputStream())), CREATED);
        } catch (IOException | SAXException | OpenXML4JException | ParserConfigurationException | IllegalArgumentException | ElifeException e) {
            return new ResponseEntity<>(getJson(ErrorCode.INVALID_BLACKLIST_FILE.apply(e.getMessage())), NOT_ACCEPTABLE);
        }
    }

}
