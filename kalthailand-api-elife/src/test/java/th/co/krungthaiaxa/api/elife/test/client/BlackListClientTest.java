package th.co.krungthaiaxa.api.elife.test.client;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.client.BlackListClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class BlackListClientTest {
	
	@Inject
	private BlackListClient blackListClient;
	
	@Test
	public void should_return_value_with_true(){
		//assertThat(blackListClient.getCheckingBlackListed("3480700046900", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbGlmZWFkbWludXNlciIsInJvbGUiOlsiVUlfRUxJRkVfQURNSU4iLCJBUElfRUxJRkUiLCJBUElfQkxBQ0tMSVNUIiwiQVBJX1NJR05JTkciLCJVSV9BVVRPUEFZIiwiVUlfVkFMSURBVElPTiIsIlVJX1NMQyJdLCJjcmVhdGVkIjoiMjAxNi0wNy0wOFQxNjoyNjo1Mi4xODIiLCJleHAiOjE0Njc5NzM2MTJ9.AMKeSc07umZWoo82phfgqkQFsBZdDC3HtjQClcawohH5NBgb-UZ4pPTLgto5vow3M1tt4_ipqjie5oh8HNzw8A")).isEqualTo(true);
	}
	
	@Test
	public void should_return_value_with_false(){
		//assertThat(blackListClient.getCheckingBlackListed("3101202780273", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbGlmZWFkbWludXNlciIsInJvbGUiOlsiVUlfRUxJRkVfQURNSU4iLCJBUElfRUxJRkUiLCJBUElfQkxBQ0tMSVNUIiwiQVBJX1NJR05JTkciLCJVSV9BVVRPUEFZIiwiVUlfVkFMSURBVElPTiIsIlVJX1NMQyJdLCJjcmVhdGVkIjoiMjAxNi0wNy0wOFQxNjoyNjo1Mi4xODIiLCJleHAiOjE0Njc5NzM2MTJ9.AMKeSc07umZWoo82phfgqkQFsBZdDC3HtjQClcawohH5NBgb-UZ4pPTLgto5vow3M1tt4_ipqjie5oh8HNzw8A")).isEqualTo(false);
	}

}
