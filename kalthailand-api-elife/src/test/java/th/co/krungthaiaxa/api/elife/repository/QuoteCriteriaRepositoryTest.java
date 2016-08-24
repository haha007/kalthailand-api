package th.co.krungthaiaxa.api.elife.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.elife.KalApiApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteCriteriaRepositoryTest {
	
	@Inject
	private QuoteCriteriaRepository quoteCriteriaRepository;
	
	@Test
	public void should_return_quote_count_greather_than_zero(){	
		LocalDate today = LocalDate.now();
		LocalDate beforeDay = today.minusDays(30);
		List<Map<String,Object>> listCount = quoteCriteriaRepository.quoteCount(beforeDay, today);
		assertThat(listCount.size()).isGreaterThan(0);		
	}	
	
	@Test
	public void should_return_quote_count_zero(){
		LocalDate today = LocalDate.now();
		LocalDate beforeDay = today.minusDays(30);
		List<Map<String,Object>> listCount = quoteCriteriaRepository.quoteCount(today, beforeDay);
		assertThat(listCount.size()).isEqualTo(0);		
	}

}
