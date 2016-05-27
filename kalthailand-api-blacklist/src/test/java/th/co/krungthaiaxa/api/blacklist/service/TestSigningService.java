package th.co.krungthaiaxa.api.signing.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.signing.KALApiSigning;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KALApiBlacklist.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class TestSigningService {
    public static final String SRC = "/application-form.pdf";

    @Inject
    private SigningService signingService;

    @Test
    public void should_sign_document() throws DocumentException, GeneralSecurityException, IOException {
        File signedPdf = new File("target/application-form_signed.pdf");
        FileOutputStream outputStream = new FileOutputStream(signedPdf);
        signingService.sign(this.getClass().getResourceAsStream(SRC), outputStream, "Payment received", "Invisible");

        assertThat(signedPdf.exists()).isTrue();
        assertThat(new PdfReader(new FileInputStream(signedPdf))).isNotNull();
    }
}
