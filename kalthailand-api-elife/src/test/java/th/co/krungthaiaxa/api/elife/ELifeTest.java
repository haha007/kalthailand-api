package th.co.krungthaiaxa.api.elife;

import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.elife.client.AuthClient;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.repository.CDBRepository;
import th.co.krungthaiaxa.api.elife.service.PolicyService;

import javax.inject.Inject;

import java.nio.charset.Charset;

import static java.util.Optional.empty;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@Component
public class ELifeTest {
    @Inject
    protected PolicyService policyService;
    @Inject
    private AuthClient authClient;
    @Inject
    private SigningClient signingClient;

    private CDBRepository cdbRepository;
    private RestTemplate fakeAuthRestTemplate;
    private RestTemplate fakeSigningRestTemplate;

    @Before
    public void setup() {
        // Faking signing by returning pdf document as received and 200 response
        fakeSigningRestTemplate = mock(RestTemplate.class);
        signingClient.setTemplate(fakeSigningRestTemplate);
        when(fakeSigningRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                HttpEntity entity = (HttpEntity) args[2];

                return new ResponseEntity<>(new String((byte[]) entity.getBody(), Charset.forName("UTF-8")), OK);
            }
        });

        // Faking authorization by always returning success
        fakeAuthRestTemplate = mock(RestTemplate.class);
        authClient.setTemplate(fakeAuthRestTemplate);
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getFakeToken());

        // Faking CDB by always returning empty Optional
        cdbRepository = mock(CDBRepository.class);
        policyService.setCdbRepository(cdbRepository);
        when(cdbRepository.getExistingAgentCode(anyString(), anyString())).thenReturn(empty());
    }

    private ResponseEntity<String> getFakeToken() {
        return new ResponseEntity<>("123456", OK);
    }
}
