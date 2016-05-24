package th.co.krungthaiaxa.api.elife;

import org.junit.Before;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.repository.CDBRepository;
import th.co.krungthaiaxa.api.elife.service.PolicyService;

import javax.inject.Inject;

import static java.util.Optional.empty;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@Component
public class ELifeTest {
    @Inject
    protected PolicyService policyService;

    private CDBRepository cdbRepository;

    @Before
    public void setupFakeTemplateAndRepository() {
        // Faking CDB by always returning empty Optional
        cdbRepository = mock(CDBRepository.class);
        policyService.setCdbRepository(cdbRepository);
        when(cdbRepository.getExistingAgentCode(anyString(), anyString())).thenReturn(empty());
    }

    private ResponseEntity<String> getFakeToken() {
        return new ResponseEntity<>("123456", OK);
    }
}
