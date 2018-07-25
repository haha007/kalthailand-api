package th.co.krungthaiaxa.api.signing.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.signing.model.SignDocument;
import th.co.krungthaiaxa.api.signing.resource.DocumentResource;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SigningService {
    private final static Logger logger = LoggerFactory.getLogger(DocumentResource.class);

    private KeyStore keystore = KeyStore.getInstance("PKCS12");

    private char[] password;

    private Map<String, String> availableCerts = new HashMap<String, String>() {{
        put("ELIFE", "elife_krungthai-axa_co_th");
        put("KTB", "banca_ktb_co_th");
    }};

    public SigningService(String path, String password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        this.password = password.toCharArray();

        keystore.load(getEitherResourceOrFileAsStream(path), this.password);
    }

    public void sign(InputStream src, OutputStream dst, SignDocument signDocument)
            throws GeneralSecurityException, IOException, DocumentException {
        String defaultAliasName = availableCerts.get("ELIFE");
        String aliasName = availableCerts.getOrDefault(signDocument.getCertificate().get(), defaultAliasName);

        if (!isKeyExists(aliasName) && !aliasName.equals(defaultAliasName)) {
            aliasName = defaultAliasName;
            if (!isKeyExists(aliasName)) {
                throw new IOException("Unable to find configured content");
            }
        }

        logger.info("alias name = {}", aliasName);

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        PrivateKey privateKey = (PrivateKey) keystore.getKey(aliasName, password);
        Certificate[] certChain = keystore.getCertificateChain(aliasName);

        PdfReader pdfReader = new PdfReader(src);
        PdfStamper pdfStamper = PdfStamper.createSignature(pdfReader, dst, '\0');

        if (signDocument.getPassword().isPresent()) {
            pdfStamper.setEncryption(signDocument.getPassword().get().getBytes(), "elifeSign".getBytes(),
                    PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA);
        }

        PdfSignatureAppearance appearance = pdfStamper.getSignatureAppearance();

        if (signDocument.getSigningLocation().isPresent()) {
            appearance.setLocation(signDocument.getSigningLocation().get());
        }

        if (signDocument.getSigningReason().isPresent()) {
            appearance.setReason(signDocument.getSigningReason().get());
        }

        appearance.setVisibleSignature(new Rectangle(10, 20, 10, 20), 1, "sig");

        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA512, provider.getName());
        MakeSignature.signDetached(appearance, digest, signature, certChain, null, null, null, 0, MakeSignature.CryptoStandard.CADES);
    }

    /**
     * Try to get content of the path inside the resources folder otherwise in the actual path
     *
     * @param path A path to the resource file either resources folder or the actual file system path
     * @return InputStream
     * @throws IOException
     */
    private InputStream getEitherResourceOrFileAsStream(String path)
            throws IOException {
        InputStream input = getClass().getResourceAsStream(path);

        if (input != null) {
            logger.info("Loading KeyStore from resource path: " + path);
            return input;
        }

        logger.info("Loading KeyStore from system path: " + path);
        return new FileInputStream(new File(path));
    }

    /**
     * Return true if the given `aliasName` is exist in the KeyStore
     *
     * @param aliasName An alias name of the certificate inside the KeyStore
     * @return Boolean
     * @throws KeyStoreException
     */
    private Boolean isKeyExists(String aliasName)
            throws KeyStoreException {
        return keystore.containsAlias(aliasName) || keystore.isKeyEntry(aliasName);
    }

}
