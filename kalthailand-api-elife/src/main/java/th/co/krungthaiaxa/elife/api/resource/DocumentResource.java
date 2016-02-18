package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.elife.api.exception.ImageTooSmallException;
import th.co.krungthaiaxa.elife.api.exception.InputImageException;
import th.co.krungthaiaxa.elife.api.exception.OutputImageException;
import th.co.krungthaiaxa.elife.api.exception.UnsupportedImageException;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.service.DocumentService;
import th.co.krungthaiaxa.elife.api.service.PolicyService;
import th.co.krungthaiaxa.elife.api.utils.WatermarkUtil;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Base64;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;

@RestController
@Api(value = "Documents")
public class DocumentResource {
    private final static Logger logger = LoggerFactory.getLogger(DocumentResource.class);
    private final DocumentService documentService;
    private final PolicyService policyService;
    @Value("${path.store.watermarked.image}")
    private String storePath;

    @Inject
    public DocumentResource(DocumentService documentService, PolicyService policyService) {
        this.documentService = documentService;
        this.policyService = policyService;
    }

    @ApiOperation(value = "Upload Thai ID", notes = "Uploads a Thai ID, applies a watermark image and links it to the given policy. Result is the watermarked image encoded in base64", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "If input image is not readable or if output image could not be written", response = Error.class),
            @ApiResponse(code = 404, message = "If policy is not found", response = Error.class),
            @ApiResponse(code = 406, message = "If input image is too small", response = Error.class),
            @ApiResponse(code = 415, message = "If input image is not supported", response = Error.class)
    })
    @RequestMapping(value = "/documents/{policyId}/thai/id", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity uploadThaiId(
            @ApiParam(value = "The policy ID")
            @PathVariable String policyId,
            @ApiParam(value = "The content of the image to watermark, but base 64 encoded.")
            @RequestBody String base64Image) {
        Policy policy;
        try {
            policy = policyService.findPolicy(policyId);
        } catch (RuntimeException e) {
            logger.error("Unable to find the policy with ID [" + policyId + "]", e);
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }

        byte[] inputImage;
        try {
            inputImage = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(WATERMARK_IMAGE_INPUT_NOT_READABLE, BAD_REQUEST);
        }

        InputStream in = new ByteArrayInputStream(inputImage);
        InputStream watermarkImage = this.getClass().getResourceAsStream("/watermark_white.png");

        String mimeType;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(in);
        } catch (IOException e) {
            return new ResponseEntity<>(WATERMARK_IMAGE_MIME_TYPE_UNKNOWN, BAD_REQUEST);
        }

        byte[] content;
        try {
            content = WatermarkUtil.addTextWatermark(watermarkImage, mimeType, in);
        } catch (InputImageException e) {
            return new ResponseEntity<>(WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE, BAD_REQUEST);
        } catch (OutputImageException e) {
            return new ResponseEntity<>(WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN, BAD_REQUEST);
        } catch (ImageTooSmallException e) {
            return new ResponseEntity<>(WATERMARK_IMAGE_INPUT_TOO_SMALL, NOT_ACCEPTABLE);
        } catch (UnsupportedImageException e) {
            return new ResponseEntity<>(WATERMARK_IMAGE_INPUT_NOT_SUPPORTED, UNSUPPORTED_MEDIA_TYPE);
        }

        byte[] encodedContent = Base64.getEncoder().encode(content);
        documentService.addDocument(policy, encodedContent, mimeType, "Thai ID");

        return new ResponseEntity<>(encodedContent, OK);
    }
}
