package th.co.krungthaiaxa.api.signing.resource;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;
import th.co.krungthaiaxa.api.signing.service.SigningService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Base64;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Api(value = "Document Signing")
public class DocumentResource {
    private final static Logger logger = LoggerFactory.getLogger(DocumentResource.class);

    private final SigningService signingService;

    @Inject
    public DocumentResource(SigningService signingService) {
        this.signingService = signingService;
    }

    @ApiOperation(value = "Signs a document", notes = "Signs a document with server configured certificate. Response is Base 64 encoded signed document", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If given document is not Base 64 encoded or not a valid PDF", response = Error.class)
    })
    @RequestMapping(value = "/documents/signpdf", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public void signDocument(
            @ApiParam(value = "The Base 64 encoded pdf to sign")
            @RequestBody String encodedBase64Pdf,
            HttpServletResponse response) {
        byte[] decodedBase64Pdf;
        try {
            decodedBase64Pdf = Base64.getDecoder().decode(encodedBase64Pdf);
        } catch (IllegalArgumentException e) {
            logger.error("Provided document is not Base 64 encoded", e);
            RequestUtil.sendError(ErrorCode.NOT_BASE_64_ENCODED, NOT_ACCEPTABLE.value(), response);
            return;
        }

        if (!checkCanReadDecodedBase64Pdf(decodedBase64Pdf, "Provided document is not a valid PDF", NOT_ACCEPTABLE.value(), response)) {
            return;
        }

        byte[] signedPdfContent;
        try (ByteArrayOutputStream signedPdfOutputStream = new ByteArrayOutputStream()) {
            signingService.sign(new ByteArrayInputStream(decodedBase64Pdf), signedPdfOutputStream, "Payment received: ", "Invisible");
            signedPdfContent = signedPdfOutputStream.toByteArray();
        } catch (IOException | GeneralSecurityException | DocumentException e) {
            logger.error("Unable to sign the PDF", e);
            RequestUtil.sendError(ErrorCode.UNABLE_TO_SIGN.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
            return;
        }

        if (!checkCanReadDecodedBase64Pdf(signedPdfContent, "Signed PDF is invalid: ", INTERNAL_SERVER_ERROR.value(), response)) {
            return;
        }

        response.setContentType("application/pdf");
        response.setStatus(OK.value());
        String fileName = "signedDocument_" + ofPattern("yyyyMMdd_hhmmss").format(now()) + ".pdf";
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(Base64.getEncoder().encode(signedPdfContent), outStream);
        } catch (IOException e) {
            logger.error("Unable to send the signed PDF", e);
            RequestUtil.sendError(ErrorCode.UNABLE_TO_SIGN.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
        }

        logger.info("Signed PDF has been sent");
    }

    private boolean checkCanReadDecodedBase64Pdf(byte[] decodedBase64Pdf, String message, Integer status, HttpServletResponse response) {
        try {
            new PdfReader(decodedBase64Pdf);
            return true;
        } catch (IOException e) {
            logger.error(message + e.getMessage(), e);
            RequestUtil.sendError(ErrorCode.PDF_INVALID.apply(e.getMessage()), status, response);
            return false;
        }
    }
}
