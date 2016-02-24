package th.co.krungthaiaxa.elife.api.products;

import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IBeginFileGenerator {
    public static void main(String[] args) {
        IBeginFileGenerator iBeginFileGenerator = new IBeginFileGenerator();
        try {
            iBeginFileGenerator.makeIt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeIt() throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(this.getClass().getResourceAsStream("/Calculate_Sheet_iBegin_V.01.csv")), ';');
        File resultFile = new File("/Users/carnoult/git/AGS/kalthailand-api/ProductIBeginRate.json");
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String[] femaleLine = nextLine;
            String[] maleLine = reader.readNext();

            ProductIBeginRate productIBeginRate = new ProductIBeginRate();
            productIBeginRate.setPlan(getPlan(femaleLine[0]));
            productIBeginRate.setSumInsured(getSumInsured(femaleLine[0]));
            productIBeginRate.setFemaleRate(getRate(femaleLine));
            productIBeginRate.setMaleRate(getRate(maleLine));

            String line = new String(JsonUtil.getJson(productIBeginRate)) + "\n";
            FileUtils.writeByteArrayToFile(resultFile, line.getBytes(), true);
        }
    }

    private Double getSumInsured(String key) {
        if (key.contains("T10")) {
            return 100000.0;
        } else if (key.contains("T15")) {
            return 150000.0;
        } else if (key.contains("T20")) {
            return 200000.0;
        } else if (key.contains("T25")) {
            return 250000.0;
        } else if (key.contains("T30")) {
            return 300000.0;
        } else if (key.contains("T40")) {
            return 400000.0;
        } else {
            return 500000.0;
        }
    }

    private String getPlan(String key) {
        if (key.contains("S10")) {
            return "iBegin10";
        } else {
            return "iBegin5";
        }
    }

    private List<Double> getRate(String[] line) {
        int indexOfAge50 = 58;
        List<Double> doubles = new ArrayList<>();
        for (int i = 0; i <= 21; i++) {
            String rate = line[indexOfAge50 + i];
            doubles.add(Double.valueOf(StringUtils.replace(rate, ",", ".")));
        }
        return doubles;
    }

    private class ProductIBeginRate {
        private String plan;
        private Double sumInsured;
        private List<Double> maleRate;
        private List<Double> femaleRate;

        public String getPlan() {
            return plan;
        }

        public void setPlan(String plan) {
            this.plan = plan;
        }

        public Double getSumInsured() {
            return sumInsured;
        }

        public void setSumInsured(Double sumInsured) {
            this.sumInsured = sumInsured;
        }

        public List<Double> getMaleRate() {
            return maleRate;
        }

        public void setMaleRate(List<Double> maleRate) {
            this.maleRate = maleRate;
        }

        public List<Double> getFemaleRate() {
            return femaleRate;
        }

        public void setFemaleRate(List<Double> femaleRate) {
            this.femaleRate = femaleRate;
        }
    }
}
