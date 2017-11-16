package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.client.SigningClient;

import javax.inject.Inject;
import java.util.Base64;

/**
 * @author tuong.le on 11/16/17.
 */
@Service
public class SigningDocumentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SigningDocumentService.class);

    private final SigningClient signingClient;

    @Inject
    public SigningDocumentService(SigningClient signingClient) {
        this.signingClient = signingClient;
    }

    public byte[] signPDFFile(final byte[] rawPdfFile, final String accessToken) {
        LOGGER.info("Starting sign PDF file");
        final byte[] encodedFile = Base64.getEncoder().encode(rawPdfFile);
        final byte[] encodedSignPDF = signingClient.getEncodedSignedPdfDocument(encodedFile, accessToken);
        LOGGER.info("Finished sign PDF file");
        return Base64.getDecoder().decode(encodedSignPDF);
    }
}
