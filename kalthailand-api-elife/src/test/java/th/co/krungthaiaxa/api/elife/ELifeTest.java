package th.co.krungthaiaxa.api.elife;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.client.Token;
import th.co.krungthaiaxa.api.elife.filter.KalApiTokenFilter;
import th.co.krungthaiaxa.api.elife.client.*;
import th.co.krungthaiaxa.api.elife.repository.CDBRepository;
import th.co.krungthaiaxa.api.elife.repository.LineBCRepository;
import th.co.krungthaiaxa.api.elife.tmc.TMCClient;
import th.co.krungthaiaxa.api.elife.tmc.TMCSendingPDFResponse;
import th.co.krungthaiaxa.api.elife.tmc.TMCSendingPDFResponseRemark;
import th.co.krungthaiaxa.api.elife.tmc.wsdl.ReceivePDFJSON;
import th.co.krungthaiaxa.api.elife.tmc.wsdl.ReceivePDFJSONResponse;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@Component
public class ELifeTest {
    @Inject
    private CDBClient cdbClient;
    @Inject
    private LineBCClient lineBCClient;
    @Inject
    private SigningClient signingClient;
    @Inject
    private BlackListClient blackListClient;
    @Inject
    private TMCClient tmcClient;
    @Inject
    private KalApiTokenFilter kalApiTokenFilter;

    @Before
    public void setupFakeTemplateAndRepository() {
        // Faking signing by returning pdf document as received and 200 response
        RestTemplate fakeSigningRestTemplate = mock(RestTemplate.class);
        signingClient.setTemplate(fakeSigningRestTemplate);
        when(fakeSigningRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            HttpEntity entity = (HttpEntity) args[2];

            return new ResponseEntity<>(new String((byte[]) entity.getBody(), Charset.forName("UTF-8")), OK);
        });

        // Faking authorization by always returning success
        RestTemplate fakeAuthRestTemplate = mock(RestTemplate.class);
        kalApiTokenFilter.setTemplate(fakeAuthRestTemplate);
        when(fakeAuthRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                return new ResponseEntity<>(new String(JsonUtil.getJson(Token.of("123456"))), OK);
            }
        });

        // Faking CDB by always returning empty Optional
        CDBRepository cdbRepository = mock(CDBRepository.class);
        cdbClient.setCdbRepository(cdbRepository);
        when(cdbRepository.getExistingAgentCode(anyString(), anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String id = (String) args[0];
            String dob = (String) args[1];
            if ("existingThaiId".equals(id) && "existingDOB".equals(dob)) {
                Triple<String, String, String> result = Triple.of("previousPolicyNumber", "agentCode1", "agentCode2");
                return Optional.of(result);
            }
            else {
                return Optional.empty();
            }
        });

        // Faking Black list
        RestTemplate fakeBlacklistedTemplate = mock(RestTemplate.class);
        blackListClient.setTemplate(fakeBlacklistedTemplate);
        when(fakeBlacklistedTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            HttpEntity thaiId = (HttpEntity) args[2];
            if ("aMockedBlackListedThaiID".equals(thaiId.getBody())) {
                return new ResponseEntity<>("true", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("false", HttpStatus.OK);
            }
        });

        // Faking Line BC
        LineBCRepository lineBCRepository = mock(LineBCRepository.class);
        lineBCClient.setLineBCRepository(lineBCRepository);
        when(lineBCRepository.getLineBC(anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String mid = (String) args[0];
            if (mid.equals("u53cb613d9269dd6875f60249402b4542")) {
                Map<String, Object> map = new HashMap<>();
                map.put("dob", "30/11/1976");
                map.put("pid", "3100902286661");
                map.put("mobile", "0815701554");
                map.put("email", "Pimpaporn_a@hotmail.com");
                map.put("first_name", "พิมพมภรณ์");
                map.put("last_name", "อาภาศิริผล");

                List<Map<String,Object>> result = new ArrayList<>();
                result.add(map);

                return Optional.of(result);
            }
            else {
                return Optional.empty();
            }
        });

        // Faking TMC by always returning success
        WebServiceTemplate webServiceTemplate = mock(WebServiceTemplate.class);
        tmcClient.setWebServiceTemplate(webServiceTemplate);
        when(webServiceTemplate.marshalSendAndReceive(anyString(), any(ReceivePDFJSON.class), any(SoapActionCallback.class))).thenAnswer(invocation -> {
            TMCSendingPDFResponseRemark tmcSendingPDFResponseRemark = new TMCSendingPDFResponseRemark();
            tmcSendingPDFResponseRemark.setMessage("Success");

            TMCSendingPDFResponse tmcSendingPDFResponse = new TMCSendingPDFResponse();
            tmcSendingPDFResponse.setRemark(tmcSendingPDFResponseRemark);

            ReceivePDFJSONResponse response = new ReceivePDFJSONResponse();
            response.setReceivePDFJSONResult(new String(JsonUtil.getJson(tmcSendingPDFResponse)));
            return response;
        });
    }
}
