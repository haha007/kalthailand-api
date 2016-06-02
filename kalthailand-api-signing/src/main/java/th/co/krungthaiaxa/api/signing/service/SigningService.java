package th.co.krungthaiaxa.api.signing.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

@Service
public class SigningService {
    private final String keystore = "/elife_krungthai-axa_co_th.p12";
    private final char[] password = "P@ssw0rd".toCharArray();
    private final String alias = "elife_krungthai-axa_co_th";

    public void sign(InputStream src, OutputStream dest, String reason, String location)
            throws GeneralSecurityException, IOException, DocumentException {
        InputStream inputStream = getClass().getResourceAsStream(keystore);
        if (inputStream == null) {
            throw new IOException("Unable to find the security settings");
        }

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(inputStream, password);
        if (!ks.containsAlias(alias)) {
            throw new IOException("Unable to find configured content");
        } else if (!ks.isKeyEntry(alias)) {
            throw new IOException("Configured content is not found");
        }

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        PrivateKey pk = (PrivateKey) ks.getKey(alias, password);
        Certificate[] chain = ks.getCertificateChain(alias);

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = PdfStamper.createSignature(reader, dest, '\0');

        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(10, 20, 10, 20), 1, "sig");

        // Creating the signature
        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA512, provider.getName());
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CADES);
    }
}
