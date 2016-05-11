package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.DocumentException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.KalthailandApiSigningApplication;
import th.co.krungthaiaxa.api.signing.service.SigningService;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalthailandApiSigningApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class TestSigningService {
    public static final String SRC = "application-form.pdf";

    @Inject
    private SigningService signingService;

    @Test
    public void should_sign_document() throws DocumentException, GeneralSecurityException, IOException {
        String destFile = "target/application-form_signed.pdf";
        signingService.sign(SRC, destFile, "Payment received", "Invisible");
        Assertions.assertThat(new File(destFile).exists()).isTrue();
    }
}
