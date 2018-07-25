package th.co.krungthaiaxa.api.signing.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.signing.KALApiSigning;
import th.co.krungthaiaxa.api.signing.model.SignDocument;

import javax.inject.Inject;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KALApiSigning.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SigningServiceTests {

    private static final String src = "/application-form.pdf";

    @Inject
    private SigningService signingService;

    @Value("${keystore.password}")
    String password;

    @Test
    public void givenAResourcePathToSigningService_whenCreateNewSigningService_thenTheSigningServiceWillLoadKeyStoreFromResourcePath()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        new SigningService(
                "/uat_elife_krungthai-axa_co_th.p12",
                password
        );
    }

    @Test
    public void givenASystemPathToSigningServiceConstructor_whenCreateNewSigningService_thenTheSigningServiceWillLoadKeyStoreFromSystemPath()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        new SigningService(
                System.getProperty("user.dir") + "/src/main/resources/uat_elife_krungthai-axa_co_th.p12",
                password
        );
    }

    @Test(expected = FileNotFoundException.class)
    public void givenANonExistingPathToSigningServiceConstructor_whenCreateNewSigningService_theThrowsAFileNotFoundException()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        new SigningService(
                "/elife_krungthai-axa_co_th.p12",
                password
        );
    }

    @Test
    public void givenValidSigningConfiguration_whenSignDocument_thenSignDocumentProperly()
            throws DocumentException, GeneralSecurityException, IOException {
        File signedPdf = new File("target/application-form_signed.pdf");
        FileOutputStream outputStream = new FileOutputStream(signedPdf);
        SignDocument signDocument = new SignDocument();
        signDocument.setSigningLocation("Test location");
        signDocument.setSigningReason("Test reason");
        signDocument.setCertificate("ELIFE");

        signingService.sign(this.getClass().getResourceAsStream(src), outputStream, signDocument);
        PdfReader reader = new PdfReader(new FileInputStream(signedPdf));

        Assert.assertTrue(signedPdf.exists());
        assertELifeCertificate(getFirstCertificateFromChain(reader));
    }

    @Test
    public void givenEncryptionPassword_whenSignDocument_thenEncryptDocumentWithGivenEncryptionPassword()
            throws DocumentException, GeneralSecurityException, IOException {
        File signedPdf = new File("target/application-form_signed.pdf");
        FileOutputStream outputStream = new FileOutputStream(signedPdf);
        SignDocument signDocument = new SignDocument();
        signDocument.setSigningLocation("Test location");
        signDocument.setSigningReason("Test reason");
        signDocument.setCertificate("ELIFE");
        signDocument.setPassword("Test user password");

        signingService.sign(this.getClass().getResourceAsStream(src), outputStream, signDocument);
        PdfReader reader = new PdfReader(new FileInputStream(signedPdf), signDocument.getPassword().get().getBytes());

        Assert.assertTrue(signedPdf.exists());
        assertELifeCertificate(getFirstCertificateFromChain(reader));
    }

    @Test
    public void givenNonExistingCertificate_whenSignDocument_thenSignDocumentWithDefaultCertificate()
            throws DocumentException, GeneralSecurityException, IOException {
        File signedPdf = new File("target/application-form_signed.pdf");
        FileOutputStream outputStream = new FileOutputStream(signedPdf);
        SignDocument signDocument = new SignDocument();
        signDocument.setSigningLocation("Test location");
        signDocument.setSigningReason("Test reason");
        signDocument.setCertificate("NO_CERT");

        signingService.sign(this.getClass().getResourceAsStream(src), outputStream, signDocument);
        PdfReader reader = new PdfReader(new FileInputStream(signedPdf));

        Assert.assertTrue(signedPdf.exists());
        assertELifeCertificate(getFirstCertificateFromChain(reader));
    }

    @Test
    public void givenKTBCertificate_whenSignDocument_thenSignDocumentWithKTBCertificate()
            throws DocumentException, GeneralSecurityException, IOException {
        File signedPdf = new File("target/application-form_signed.pdf");
        FileOutputStream outputStream = new FileOutputStream(signedPdf);
        SignDocument signDocumentPayload = new SignDocument();
        signDocumentPayload.setSigningLocation("Test location");
        signDocumentPayload.setSigningReason("Test reason");
        signDocumentPayload.setCertificate("KTB");

        signingService.sign(this.getClass().getResourceAsStream(src), outputStream, signDocumentPayload);
        PdfReader reader = new PdfReader(new FileInputStream(signedPdf));

        Assert.assertTrue(signedPdf.exists());
        assertKTBCertificate(getFirstCertificateFromChain(reader));
    }

    private X509Certificate getFirstCertificateFromChain(PdfReader reader) {
        AcroFields af = reader.getAcroFields();
        PdfPKCS7 pkcs7 = af.verifySignature(af.getSignatureNames().get(0));
        Certificate[] chain = pkcs7.getCertificates();

        return (X509Certificate) chain[chain.length - 1];
    }

    private void assertELifeCertificate(X509Certificate x509) {
        Assert.assertTrue(x509.getSubjectX500Principal().toString().matches("EMAILADDRESS=itso@krungthai-axa\\.co\\.th, " +
                "CN=(uat\\.)?elife\\.krungthai-axa\\.co\\.th, " +
                "OU=Information Technology, " +
                "O=Krungthai-AXA Life Insurance Public Company Limited, " +
                "L=Huai Khwang, " +
                "ST=Bangkok, " +
                "C=TH"
        ));
    }

    private void assertKTBCertificate(X509Certificate x509) {
        Assert.assertTrue(x509.getSubjectX500Principal().toString().matches("CN=(uat\\.)?banca\\.ktb\\.co\\.th, " +
                "OU=KRUNG THAI BANK PUBLIC COMPANY LIMITED, " +
                "O=KRUNG THAI BANK PUBLIC COMPANY LIMITED, " +
                "L=Wattana, " +
                "ST=Bangkok, " +
                "C=TH"
        ));
    }
}
