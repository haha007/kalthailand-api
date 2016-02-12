package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.elife.api.exception.ImageTooSmallException;
import th.co.krungthaiaxa.elife.api.exception.InputImageException;
import th.co.krungthaiaxa.elife.api.exception.OutputImageException;
import th.co.krungthaiaxa.elife.api.exception.UnsupportedImageException;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;
import th.co.krungthaiaxa.elife.api.utils.WatermarkUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Api(value = "Watermarking")
public class WatermarkResource {
    @Value("${path.store.watermarked.image}")
    private String storePath;

    @ApiOperation(value = "Apply watermark", notes = "Applies a watermark image and returns the base64 encoded watermarked image", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "If input image is not readable or if output image could not be written", response = Error.class),
            @ApiResponse(code = 406, message = "If input image is too small", response = Error.class),
            @ApiResponse(code = 415, message = "If input image is not supported", response = Error.class)
    })
    @RequestMapping(value = "/watermark/upload", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity uploadImageForWatermark(
            @ApiParam(value = "The type of the uploaded image. Should be 'png' or 'jpg'.")
            @RequestParam String type,
            @ApiParam(value = "The content of the image to watermark, but base 64 encoded.")
            @RequestParam String base64Image) {
        byte[] inputImage;
        try {
            inputImage = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_READABLE, HttpStatus.BAD_REQUEST);
        }

        InputStream in = new ByteArrayInputStream(inputImage);
        InputStream watermarkImage = this.getClass().getResourceAsStream("/watermark_white.png");

        byte[] content;
        try {
            content = WatermarkUtil.addTextWatermark(watermarkImage, storePath, type, in);
        } catch (InputImageException e) {
            return new ResponseEntity<>(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE, HttpStatus.BAD_REQUEST);
        } catch (OutputImageException e) {
            return new ResponseEntity<>(ErrorCode.WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN, HttpStatus.BAD_REQUEST);
        } catch (ImageTooSmallException e) {
            return new ResponseEntity<>(ErrorCode.WATERMARK_IMAGE_INPUT_TOO_SMALL, HttpStatus.NOT_ACCEPTABLE);
        } catch (UnsupportedImageException e) {
            return new ResponseEntity<>(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_SUPPORTED, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        return new ResponseEntity<>(Base64.getEncoder().encode(content), OK);
    }
}
