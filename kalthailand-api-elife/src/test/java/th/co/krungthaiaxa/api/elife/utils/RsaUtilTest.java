package th.co.krungthaiaxa.api.elife.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.mail.internet.InternetAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Registration;

import org.apache.commons.codec.binary.Base64;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RsaUtilTest {

	@Test
	public void should_encryp_text() {
		assertThat(RsaUtil.encrypt("3101202780273").length()).isGreaterThan(13);
	}

	@Test
	public void should_decrypt_text() {
		assertThat(RsaUtil.decrypt("enncJ1GwWP4RbewQy5MHYLFRGvNHBLW88+yezjOd8dPNZXh+hr+BvXVQEoVDtQki9/eyVMt0XNgK7w+5NnSO+Pr54oYLS12jvP5GZaRbYGhKfEQraVZecBr6VCvjS0j7x2Gk/64gNaiWqLN/OtBoHR7Wu/JAxgXzmr77SbkIhwD8Gh1MY9EZBUz1L9aC5/jDIxl+ubPoQc/dUZ685JkS5fxf1p8AwKKdUcvKcgh01PMgoJXnhh5Zn9BEYfcYPNel71gABXDMBFqNfyKV5pWn2JN+B/S5kunXcG6H9lcF0SS65GNDn7lmLLpm0tyMUfjVPl6+sh+gkAiNjOS8ZOPgjg==")).contains("3101202780273");
	}

	
}
