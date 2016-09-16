package th.co.krungthaiaxa.api.elife.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;

import javax.inject.Inject;
import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RLSServiceWithSandboxLineServiceTest extends ELifeTest {

    @Inject
    private RLSService rlsService;

    @Inject
    private MongoTemplate mongoTemplate;

    @Test
    public void run_cron_job() {
        InputStream inputStream = IOUtil.loadInputStreamFileInClassPath("/collection-file/LFDISC6_2016-09-01.xls");
        rlsService.importCollectionFile(inputStream);
        rlsService.processLatestCollectionFiles();
        //Remove all after finished
//        mongoTemplate.dropCollection(CollectionFile.class);
    }

}
