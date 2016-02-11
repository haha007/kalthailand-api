package th.co.krungthaiaxa.ebiz.api.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.ebiz.api.KalApiApplication;
import th.co.krungthaiaxa.ebiz.api.data.PolicyNumber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
public class PolicyNumberGenerator {
    private static String fileName = "/Users/carnoult/git/AGS/kalthailand-api/policyNumber.json";

    public static void main(String[] args) {
        List<PolicyNumber> policyNumbers = new ArrayList<>();
        File file = new File(fileName);
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            FileUtils.writeStringToFile(file, "[");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        for (int i = 0; i <= 99999; i++) {
            try {
                FileUtils.writeStringToFile(file, "{\"policyId\":\"502-" + StringUtils.leftPad("" + i, 11, "0") + "\"}", true);
//                if (i != 99998) {
//                    FileUtils.writeStringToFile(file, ",", true);
//                }
                FileUtils.writeStringToFile(file, System.getProperty("line.separator"), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        try {
//            FileUtils.writeStringToFile(file, "]", true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // [{"policyId":"00000000000"},{"policyId":"00000000001"}]
    }
}
