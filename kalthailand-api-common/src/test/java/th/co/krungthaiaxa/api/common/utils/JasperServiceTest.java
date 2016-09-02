package th.co.krungthaiaxa.api.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.service.JasperService;

/**
 * @author khoi.tran on 8/31/16.
 */
public class JasperServiceTest {

    JasperService jasperService = new JasperService(new ObjectMapper());

    @Test
    public void testPdfGenerator() {
        SampleData sampleData = new SampleData();
        sampleData.setFirstname("Khoi");
        sampleData.setLastname("Tran");

        jasperService.exportPdfFile("/test/jasper/sample.jrxml", sampleData, "target/test/jasper/sample-output.pdf");
    }

    private static class SampleData {
        private String firstname;
        private String lastname;

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
    }
}
