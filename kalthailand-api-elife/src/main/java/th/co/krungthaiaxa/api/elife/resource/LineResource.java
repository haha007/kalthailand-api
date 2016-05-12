package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.elife.model.LineBC;
import th.co.krungthaiaxa.api.elife.model.Mid;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.service.LineBCService;
import th.co.krungthaiaxa.api.elife.utils.Decrypt;

import javax.inject.Inject;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static th.co.krungthaiaxa.api.elife.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

@RestController
@Api(value = "Line")
public class LineResource {
    private final static Logger logger = LoggerFactory.getLogger(LineResource.class);

    @Value("${line.app.secret.key}")
    private String secretkey;

    private final LineBCService lineBCService;

    @Inject
    public LineResource(LineBCService lineBCService) {
        this.lineBCService = lineBCService;
    }

    @ApiOperation(value = "Get Line BC Information along with MID", response = LineBC.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If unable to get line bc information", response = Error.class)
    })
    @RequestMapping(value = "/line/bc", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getLineBC(
            @ApiParam(value = "The MID value")
            @RequestParam String mid) {
        Optional<LineBC> lineBCData = lineBCService.getLineBCInfo(mid);
        if (lineBCData.isPresent()) {
            return new ResponseEntity<>(getJson(lineBCData.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(getJson(UNABLE_TO_GET_LINE_BC), NOT_FOUND);
        }
    }

    @ApiOperation(value = "Decrypt line token", notes = "Decrypts line token to get the mid", response = Mid.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "If unable to decrypt or if decrypted value does not contain mid", response = Error.class)
    })
    @RequestMapping(value = "/decrypt", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> decryptLineId(
            @ApiParam(value = "The encrypted line token")
            @RequestParam String value) {
        String decrypted;
        try {
            decrypted = Decrypt.decrypt(value, secretkey);
        } catch (Exception e) {
            logger.error("Unable to decrypt [" + value + "]", e);
            return new ResponseEntity<>(getJson(UNABLE_TO_DECRYPT), BAD_REQUEST);
        }

        if (!decrypted.contains(".")) {
            logger.error("Decrypted value doesn't contain a '.'");
            return new ResponseEntity<>(getJson(INVALID_LINE_ID), BAD_REQUEST);
        }

        logger.info("Value has been successfuly decrypted");
        Mid result = new Mid(decrypted.substring(0, decrypted.indexOf(".")));
        return new ResponseEntity<>(getJson(result), HttpStatus.OK);
    }

}
