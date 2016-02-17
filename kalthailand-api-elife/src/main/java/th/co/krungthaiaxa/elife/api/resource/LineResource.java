package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.elife.api.model.Mid;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;
import th.co.krungthaiaxa.elife.api.utils.Decrypt;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.INVALID_LINE_ID;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.UNABLE_TO_DECRYPT;

@RestController
@Api(value = "Line")
public class LineResource {
    private final static Logger logger = LoggerFactory.getLogger(LineResource.class);

    @Value("${line.secret.key}")
    private String secretkey;

    @ApiOperation(value = "Decrypt line token", notes = "Decrypts line token to get the mid", response = Mid.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "If unable to decrypt or if decrypted value does not contain mid", response = Error.class)
    })
    @RequestMapping(value = "/decrypt", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity decryptLineId(
            @ApiParam(value = "The encrypted line token")
            @RequestParam String value) {
        logger.info("Value [" + value + "] will be decrypted");
        String decrypted;
        try {
            decrypted = Decrypt.decrypt(value, secretkey);
        } catch (Exception e) {
            logger.error("Unable to decrypt [" + value + "]", e);
            return new ResponseEntity<>(UNABLE_TO_DECRYPT, BAD_REQUEST);
        }

        if (!decrypted.contains(".")) {
            logger.error("Decrypted value doesn't contain a '.'");
            return new ResponseEntity<>(INVALID_LINE_ID, BAD_REQUEST);
        }

        logger.info("Value has been successfuly decrypted");
        Mid result = new Mid(decrypted.substring(0, decrypted.indexOf(".")));
        return new ResponseEntity<>(JsonUtil.getJson(result), HttpStatus.OK);
    }

}
