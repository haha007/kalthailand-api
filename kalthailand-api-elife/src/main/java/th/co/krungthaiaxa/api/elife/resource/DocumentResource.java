package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.elife.exception.ImageTooSmallException;
import th.co.krungthaiaxa.api.elife.exception.InputImageException;
import th.co.krungthaiaxa.api.elife.exception.OutputImageException;
import th.co.krungthaiaxa.api.elife.exception.UnsupportedImageException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.elife.utils.WatermarkUtil;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Api(value = "Documents")
public class DocumentResource {
    private final static Logger logger = LoggerFactory.getLogger(DocumentResource.class);
    private final DocumentService documentService;
    private final PolicyService policyService;

    @Inject
    public DocumentResource(DocumentService documentService, PolicyService policyService) {
        this.documentService = documentService;
        this.policyService = policyService;
    }

    @ApiOperation(value = "Documents of a policy", notes = "Document collection for a policy", response = Document.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "If policy is not found", response = Error.class),
            @ApiResponse(code = 406, message = "If document is not in policy", response = Error.class),
            @ApiResponse(code = 500, message = "If document could not be downloaded", response = Error.class)
    })
    @RequestMapping(value = "/documents/policies/{policyId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> documentsOfPolicy(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        return new ResponseEntity<>(JsonUtil.getJson(policy.get().getDocuments()), OK);
    }

    @ApiOperation(value = "Download a document", notes = "Downloads a document of a policy", response = DocumentDownload.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If policy is not found", response = Error.class),
            @ApiResponse(code = 406, message = "If document is not in policy", response = Error.class),
            @ApiResponse(code = 500, message = "If document could not be downloaded", response = Error.class)
    })
    @RequestMapping(value = "/documents/policies/{policyId}/{documentId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> documentDownloadOfPolicy(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The document ID", required = true)
            @PathVariable String documentId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        Optional<Document> document = policy.get().getDocuments().stream().filter(tmp -> tmp.getId().equals(documentId)).findFirst();
        if (!document.isPresent()) {
            logger.error("Unable to find the document with ID [" + documentId + "] in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.POLICY_DOES_NOT_CONTAIN_DOCUMENT), NOT_ACCEPTABLE);
        }

        DocumentDownload documentDownload = documentService.downloadDocument(documentId);
        if (documentDownload == null) {
            logger.error("Unable to download the document with ID [" + documentId + "]");
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.UNABLE_TO_DOWNLOAD_DOCUMENT), INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(JsonUtil.getJson(documentDownload), OK);
    }

    @ApiOperation(value = "Upload Thai ID", notes = "Uploads a Thai ID, applies a watermark image and links it to the given policy. Result is the watermarked image encoded in base64", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "If input image is not readable or if output image could not be written", response = Error.class),
            @ApiResponse(code = 404, message = "If policy is not found", response = Error.class),
            @ApiResponse(code = 406, message = "If input image is too small", response = Error.class),
            @ApiResponse(code = 415, message = "If input image is not supported", response = Error.class)
    })
    @RequestMapping(value = "/documents/policies/{policyId}/thai/id", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> uploadThaiId(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The content of the image to watermark, but base 64 encoded.", required = true)
            @RequestBody String base64Image) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        byte[] inputImage;
        try {
            inputImage = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_READABLE), BAD_REQUEST);
        }

        InputStream in = new ByteArrayInputStream(inputImage);
        InputStream watermarkImage = this.getClass().getResourceAsStream("/watermark_white.png");

        String mimeType;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(in);
        } catch (IOException e) {
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.WATERMARK_IMAGE_MIME_TYPE_UNKNOWN), BAD_REQUEST);
        }

        byte[] content;
        try {
            content = WatermarkUtil.addTextWatermark(watermarkImage, mimeType, in);
        } catch (InputImageException e) {
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE), BAD_REQUEST);
        } catch (OutputImageException e) {
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN), BAD_REQUEST);
        } catch (ImageTooSmallException e) {
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.WATERMARK_IMAGE_INPUT_TOO_SMALL), NOT_ACCEPTABLE);
        } catch (UnsupportedImageException e) {
            return new ResponseEntity<>(JsonUtil.getJson(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_SUPPORTED), UNSUPPORTED_MEDIA_TYPE);
        }

        byte[] encodedContent = Base64.getEncoder().encode(content);
        documentService.addDocument(policy.get(), encodedContent, mimeType, DocumentType.THAI_ID);

        return new ResponseEntity<>(encodedContent, OK);
    }
}
