package th.co.krungthaiaxa.api.blacklist.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.blacklist.KalApiBlacklist;
import th.co.krungthaiaxa.api.blacklist.exception.ElifeException;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiBlacklist.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class BlackListedServiceTest {

    @Inject
    private BlackListedService blackListedService;

    @Test
    public void should_return_error_when_thai_id_is_empty() {
        assertThatThrownBy(() -> blackListedService.checkThaiIdFormat(""))
                .isInstanceOf(ElifeException.class);
    }

    @Test
    public void should_return_error_when_thai_id_is_not_a_number() {
        assertThatThrownBy(() -> blackListedService.checkThaiIdFormat("somethingNotANumber"))
                .isInstanceOf(ElifeException.class);
    }

    @Test
    public void should_return_error_when_thai_id_is_not_a_13_digits() {
        assertThatThrownBy(() -> blackListedService.checkThaiIdLength("0123456789"))
                .isInstanceOf(ElifeException.class);
    }

    @Test
    public void should_return_ok_when_thai_id_is_a_13_digits_number() {
        blackListedService.checkThaiIdFormat("0123456789123");
        blackListedService.checkThaiIdLength("0123456789123");
    }
}
