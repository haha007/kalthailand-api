package th.co.krungthaiaxa.api.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import th.co.krungthaiaxa.api.common.service.JasperService;

import java.util.HashMap;

/**
 * @author khoi.tran on 8/31/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class JasperServiceTest {

    JasperService jasperService = new JasperService(new ObjectMapper());

    @Test
    public void testPdfGenerator() {
        Object dataSource = new HashMap<>();
        jasperService.exportPdfFile("/jasper/sample/input.jrxml", dataSource, "/jasper/sample/output.pdf");
    }
}
