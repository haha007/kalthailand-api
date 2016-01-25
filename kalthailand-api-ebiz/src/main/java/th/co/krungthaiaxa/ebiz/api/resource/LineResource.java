package th.co.krungthaiaxa.ebiz.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.ebiz.api.model.Mid;
import th.co.krungthaiaxa.ebiz.api.model.error.Error;
import th.co.krungthaiaxa.ebiz.api.model.error.ErrorCode;
import th.co.krungthaiaxa.ebiz.api.utils.Decrypt;
import th.co.krungthaiaxa.ebiz.api.utils.JsonUtil;

@RestController
@Api(value = "Line", description = "Everything related to Line")
public class LineResource {

    @Value("${line.secret.key}")
    private String secretkey;

    @ApiOperation(value = "Decrypt line token", notes = "Decrypts line token to get the mid", response = Mid.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "If unable to decrypt or if decrypted value does not contain mid", response = Error.class)
    })
    @RequestMapping(value = "/decrypt", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity decryptLineId(@RequestParam String value) {
        String decrypted;
        try {
            decrypted = Decrypt.decrypt(value, secretkey);
        } catch (Exception e) {
            return new ResponseEntity<>(ErrorCode.UNABLE_TO_DECRYPT, HttpStatus.BAD_REQUEST);
        }

        if (!decrypted.contains(".")) {
            return new ResponseEntity<>(ErrorCode.INAVLID_LINE_ID, HttpStatus.BAD_REQUEST);
        }

        Mid result = new Mid(decrypted.substring(0, decrypted.indexOf(".")));
        return new ResponseEntity<>(JsonUtil.getJson(result), HttpStatus.OK);
    }

}
