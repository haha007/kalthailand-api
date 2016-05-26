package th.co.krungthaiaxa.api.signing.resource;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import th.co.krungthaiaxa.api.signing.model.Error;
import th.co.krungthaiaxa.api.signing.model.ErrorCode;
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
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.api.signing.utils.JsonUtil.getJson;

@RestController
@Api(value = "Document Signing")
public class SigningResource {
    private final static Logger logger = LoggerFactory.getLogger(SigningResource.class);

    private final SigningService signingService;

    @Inject
    public SigningResource(SigningService signingService) {
        this.signingService = signingService;
    }

    @ApiIgnore
    @RequestMapping(value = "/documents/signpdf", produces = APPLICATION_JSON_VALUE, method = POST)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If given document is not Base 64 encoded or not a valid PDF", response = Error.class)
    })
    @ResponseBody
    public void signDocument(@RequestBody String encodedBase64Pdf, HttpServletResponse response) {
        byte[] decodedBase64Pdf;
        try {
            decodedBase64Pdf = Base64.getDecoder().decode(encodedBase64Pdf);
        } catch (IllegalArgumentException e) {
            logger.error("Provided document is not Base 64 encoded", e);
            sendError(ErrorCode.NOT_BASE_64_ENCODED, NOT_ACCEPTABLE.value(), response);
            return;
        }

        try {
            new PdfReader(decodedBase64Pdf);
        } catch (IOException e) {
            logger.error("Provided document is not a valid PDF", e);
            sendError(ErrorCode.PDF_INVALID.apply(e.getMessage()), NOT_ACCEPTABLE.value(), response);
            return;
        }

        byte[] signedPdfContent;
        try (ByteArrayOutputStream signedPdfOutputStream = new ByteArrayOutputStream()) {
            signingService.sign(new ByteArrayInputStream(decodedBase64Pdf), signedPdfOutputStream, "Payment received", "Invisible");
            signedPdfContent = signedPdfOutputStream.toByteArray();
        } catch (IOException | GeneralSecurityException | DocumentException e) {
            logger.error("Unable to sign the PDF", e);
            sendError(ErrorCode.UNABLE_TO_SIGN.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
            return;
        }

        try {
            new PdfReader(signedPdfContent);
        } catch (IOException e) {
            logger.error("Signed PDF is invalid", e);
            sendError(ErrorCode.PDF_INVALID.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
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
            sendError(ErrorCode.UNABLE_TO_SIGN.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
        }

        logger.info("Signed PDF has been sent");
    }

    private void sendError(Error error, Integer status, HttpServletResponse response) {
        response.setContentType("text/x-json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(status);
        try {
            response.getWriter().write(new String(getJson(error), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to get valid error message.", e);
        }
    }

}
