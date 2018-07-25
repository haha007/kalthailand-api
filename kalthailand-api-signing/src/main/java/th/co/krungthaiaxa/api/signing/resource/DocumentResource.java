package th.co.krungthaiaxa.api.signing.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;
import th.co.krungthaiaxa.api.signing.model.SignDocument;
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
            @ApiParam(value = "The Base 64 encoded pdf to sign") @RequestBody String encodedBase64Pdf,
            @RequestHeader HttpHeaders headers,
            HttpServletResponse response
    ) {
        SignDocument signDocument;
        String contentType = "";
        if (headers.getContentType() != null) {
            contentType = headers.getContentType().toString();
        }
        signDocument = parseSignDocumentPayload(encodedBase64Pdf, contentType);

        byte[] decodedBody;
        try {
            decodedBody = decodeBase64PDF(signDocument.getContent().get(), response);
        } catch (IllegalArgumentException e) {
            System.out.println("Some where I belong");
            logger.error("Provided document is not Base 64 encoded", e);
            RequestUtil.sendError(ErrorCode.NOT_BASE_64_ENCODED, NOT_ACCEPTABLE.value(), response);
            return;
        }

        try {
            if (!canReadDecodedBase64PDF(decodedBody)) {
                return;
            }
        } catch (IOException e) {
            logger.error("Provided document is not a valid PDF" + e.getMessage(), e);
            RequestUtil.sendError(ErrorCode.PDF_INVALID.apply(e.getMessage()), NOT_ACCEPTABLE.value(), response);
            return;
        }

        byte[] signedPdfContent;
        try (ByteArrayOutputStream signedPdfOutputStream = new ByteArrayOutputStream()) {
            signingService.sign(new ByteArrayInputStream(decodedBody), signedPdfOutputStream, signDocument);
            signedPdfContent = signedPdfOutputStream.toByteArray();
        } catch (IOException | GeneralSecurityException | DocumentException e) {
            logger.error("Unable to sign the PDF", e);
            RequestUtil.sendError(ErrorCode.UNABLE_TO_SIGN.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
            return;
        }

        try {
            if (signDocument.getPassword().isPresent()) {
                if (!canReadDecodedBase64PDFWithPasswordEncryption(signedPdfContent, "elifeSign")) {
                    return;
                }
            } else {
                if (!canReadDecodedBase64PDF(signedPdfContent)) {
                    return;
                }
            }
        } catch (IOException e) {
            logger.error("Signed PDF is invalid: " + e.getMessage(), e);
            RequestUtil.sendError(ErrorCode.PDF_INVALID.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
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

    //TODO: The password should not be PathVariable
    @ApiOperation(value = "Signs a document with a password", notes = "Signs a document with a password and server configured certificate. Response is Base 64 encoded signed document", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If given document is not Base 64 encoded or not a valid PDF", response = Error.class)
    })
    @RequestMapping(value = "/documents/signpdf/{password}", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    @Deprecated
    public void signDocumentWithPassword(
            @PathVariable String password,
            @ApiParam(value = "The Base 64 encoded pdf to sign")
            @RequestBody String encodedBase64Pdf,
            HttpServletResponse response) {
        byte[] decodedBase64Pdf = decodeBase64PDF(encodedBase64Pdf, response);

        try {
            if (!canReadDecodedBase64PDF(decodedBase64Pdf)) {
                return;
            }
        } catch (IOException e) {
            logger.error("Provided document is not a valid PDF" + e.getMessage(), e);
            RequestUtil.sendError(ErrorCode.PDF_INVALID.apply(e.getMessage()), NOT_ACCEPTABLE.value(), response);
            return;
        }

        byte[] signedPdfContent;
        try (ByteArrayOutputStream signedPdfOutputStream = new ByteArrayOutputStream()) {
            SignDocument signDocument = parseSignDocumentPayload(encodedBase64Pdf, "");
            signDocument.setPassword(password);

            signingService.sign(new ByteArrayInputStream(decodedBase64Pdf), signedPdfOutputStream, signDocument);
            signedPdfContent = signedPdfOutputStream.toByteArray();
        } catch (IOException | GeneralSecurityException | DocumentException e) {
            logger.error("Unable to sign the PDF", e);
            RequestUtil.sendError(ErrorCode.UNABLE_TO_SIGN.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
            return;
        }

        try {
            if (!canReadDecodedBase64PDFWithPasswordEncryption(signedPdfContent, "elifeSign")) {
                return;
            }
        } catch (IOException e) {
            logger.error("Signed PDF is invalid: " + e.getMessage(), e);
            RequestUtil.sendError(ErrorCode.PDF_INVALID.apply(e.getMessage()), INTERNAL_SERVER_ERROR.value(), response);
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

    private SignDocument parseSignDocumentPayload(String payload, String contentType) {
        SignDocument signDocument = new SignDocument();
        signDocument.setContent(payload);
        signDocument.setPassword("");

        try {
            if (contentType.equals("application/json")) {
                ObjectMapper mapper = new ObjectMapper();
                signDocument = mapper.readValue(payload, SignDocument.class);
            }
        } catch (IOException e) {
            logger.warn("Unable to parse the payload, this might be a base64 payload sent with Content-Type: application/json instead of application/x-www-form-urlencoded: " + e.getMessage());
            signDocument.setContent(payload);
        }

        if (!signDocument.getCertificate().isPresent()) {
            signDocument.setCertificate("ELIFE");
        }

        if (!signDocument.getSigningLocation().isPresent()) {
            signDocument.setSigningLocation("Invisible");
        }

        if (!signDocument.getSigningReason().isPresent()) {
            signDocument.setSigningReason("Payment received");
        }

        logger.info("certificate = {}, reason = {}, location = {}, password = {}",
                signDocument.getCertificate().get(),
                signDocument.getSigningReason().get(),
                signDocument.getSigningLocation().get(),
                signDocument.getPassword().isPresent()
        );
        return signDocument;
    }

    private byte[] decodeBase64PDF(String base64String, HttpServletResponse response)
            throws IllegalArgumentException {
        return Base64.getDecoder().decode(base64String);
    }

    private boolean canReadDecodedBase64PDF(byte[] decodedBase64PDF)
            throws IOException {
        return canReadDecodedBase64PDFWithPasswordEncryption(decodedBase64PDF, "");
    }

    private boolean canReadDecodedBase64PDFWithPasswordEncryption(byte[] decodedBase64PDF, String ownerPassword)
            throws IOException {
        if (ownerPassword.isEmpty()) {
            new PdfReader(decodedBase64PDF);
            return true;
        }

        new PdfReader(decodedBase64PDF, ownerPassword.getBytes());
        return true;
    }

}
