package th.co.krungthaiaxa.api.elife.test;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.elife.client.BlackListClient;
import th.co.krungthaiaxa.api.elife.client.CDBClient;
import th.co.krungthaiaxa.api.elife.client.LineBCClient;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.client.Token;
import th.co.krungthaiaxa.api.elife.filter.KalApiTokenFilter;
import th.co.krungthaiaxa.api.elife.repository.LineBCRepository;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;
import th.co.krungthaiaxa.api.elife.tmc.TMCClient;
import th.co.krungthaiaxa.api.elife.tmc.TMCSendingPDFResponse;
import th.co.krungthaiaxa.api.elife.tmc.TMCSendingPDFResponseRemark;
import th.co.krungthaiaxa.api.elife.tmc.wsdl.ReceivePDFJSON;
import th.co.krungthaiaxa.api.elife.tmc.wsdl.ReceivePDFJSONResponse;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
        String catalinaHome = new File("").getAbsolutePath();
        System.setProperty("catalina.home", catalinaHome);

        mockSigningClient(signingClient);
        mockApiTokenFilter(kalApiTokenFilter);
        mockCdbClient(cdbClient);
        mockBlackListClient(blackListClient);
        mockLineBCClient(lineBCClient);
        mockTmcClient(tmcClient);
    }

    public static void mockBlackListClient(BlackListClient blackListClient) {
        RestTemplate fakeBlacklistedTemplate = mock(RestTemplate.class);
        blackListClient.setTemplate(fakeBlacklistedTemplate);
        when(fakeBlacklistedTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String url = (String) args[0];
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
            if (params != null && params.size() > 0 && "aMockedBlackListedThaiID".equals(params.get(0).getValue())) {
                return new ResponseEntity<>("true", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("false", HttpStatus.OK);
            }
        });
    }

    public static void mockSigningClient(SigningClient signingClient) {
        RestTemplate fakeSigningRestTemplate = mock(RestTemplate.class);
        signingClient.setTemplate(fakeSigningRestTemplate);
        when(fakeSigningRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            HttpEntity entity = (HttpEntity) args[2];

            return new ResponseEntity<>(new String((byte[]) entity.getBody(), Charset.forName("UTF-8")), OK);
        });
    }

    public static void mockApiTokenFilter(KalApiTokenFilter kalApiTokenFilter) {
        RestTemplate fakeAuthRestTemplate = mock(RestTemplate.class);
        kalApiTokenFilter.setTemplate(fakeAuthRestTemplate);
        when(fakeAuthRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                return new ResponseEntity<>(new String(JsonUtil.getJson(Token.of("123456"))), OK);
            }
        });
    }

    public static void mockCdbClient(CDBClient cdbClient) {
        CDBRepository cdbRepository = mock(CDBRepository.class);
        cdbClient.setCdbRepository(cdbRepository);
        when(cdbRepository.getExistingAgentCode(anyString(), anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String id = (String) args[0];
            String dob = (String) args[1];
            if ("existingThaiId".equals(id) && "existingDOB".equals(dob)) {
                Triple<String, String, String> result = Triple.of("previousPolicyNumber", "agentCode1", "agentCode2");
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        });
    }

    public static void mockLineBCClient(LineBCClient lineBCClient) {
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

                List<Map<String, Object>> result = new ArrayList<>();
                result.add(map);

                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        });
    }

    public void mockTmcClient(TMCClient tmcClient) {
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
