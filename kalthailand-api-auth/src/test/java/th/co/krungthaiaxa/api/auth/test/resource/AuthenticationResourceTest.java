package th.co.krungthaiaxa.api.auth.test.resource;

import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import th.co.krungthaiaxa.api.auth.KALApiAuth;
import th.co.krungthaiaxa.api.auth.model.RequestForToken;
import th.co.krungthaiaxa.api.common.basetest.BaseIntegrationResourceTest;
import th.co.krungthaiaxa.api.common.model.authentication.AuthenticatedUser;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 11/11/16.
 */
@SpringApplicationConfiguration(classes = KALApiAuth.class)
public class AuthenticationResourceTest extends BaseIntegrationResourceTest {

    @Test
    public void auth_success() {
        testLogin("/auth", "user1", "user1", HttpStatus.OK, String.class, null);
        testLogin("/auth/user", "user1", "user1", HttpStatus.OK, AuthenticatedUser.class, null);
    }

    @Test
    public void auth_wrong_account_info() {
        testLogin("/auth", "XXX", "YYY" + System.currentTimeMillis(), HttpStatus.FORBIDDEN, Error.class, ErrorCode.ERROR_CODE_AUTHENTICATION);
        testLogin("/auth/user", "XXX", "YYY" + System.currentTimeMillis(), HttpStatus.FORBIDDEN, Error.class, ErrorCode.ERROR_CODE_AUTHENTICATION);
    }

    private ResponseEntity<String> testLogin(String uri, String username, String password, HttpStatus expectHttpStatus, Class<?> expectedResponseClass, String expectedErrorCode) {
        RequestForToken requestForToken = new RequestForToken();
        requestForToken.setUserName(username);
        requestForToken.setPassword(password);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseUrl + uri, requestForToken, String.class);
        if (expectedResponseClass != null) {
            assertResponseClass(responseEntity, expectHttpStatus, expectedResponseClass);
        }
        if (expectedErrorCode != null) {
            assertError(responseEntity, expectHttpStatus, expectedErrorCode);
        }
        return responseEntity;
    }
}
