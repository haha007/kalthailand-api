package th.co.krungthaiaxa.elife.api;

import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IFineFileGenerator {
    public static void main(String[] args) {
        IFineFileGenerator iFineFileGenerator = new IFineFileGenerator();
        try {
            iFineFileGenerator.makeIt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeIt() throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(this.getClass().getResourceAsStream("/iFine E2016-1_update.csv")), ';');
        File resultFile = new File("/Users/carnoult/git/AGS/kalthailand-api/productIFineRate.json");
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String[] taxDeductibleMaleLine = nextLine;
            String[] taxDeductibleFemaleLine = reader.readNext();
            String[] nontaxDeductibleMaleLine = reader.readNext();
            String[] nontaxDeductibleFemaleLine = reader.readNext();

            ProductIFineRate productIFineRate = new ProductIFineRate();
            productIFineRate.setPlanName(taxDeductibleMaleLine[1]);
            productIFineRate.setRiskOccupation(getRiskOccupation(taxDeductibleMaleLine[2]));
            productIFineRate.setNonTaxDeductibleFemaleRate(getRate(nontaxDeductibleFemaleLine));
            productIFineRate.setNonTaxDeductibleMaleRate(getRate(nontaxDeductibleMaleLine));
            productIFineRate.setTaxDeductibleFemaleRate(getRate(taxDeductibleFemaleLine));
            productIFineRate.setTaxDeductibleMaleRate(getRate(taxDeductibleMaleLine));

            String line = new String(JsonUtil.getJson(productIFineRate)) + "\n";
            FileUtils.writeByteArrayToFile(resultFile, line.getBytes(), true);
        }
    }

    private Boolean getRiskOccupation(String key) {
        return !key.equals("S");
    }

    private List<Double> getRate(String[] line) {
        int indexOfAge18 = 4;
        List<Double> doubles = new ArrayList<>();
        for (int i = 0; i <= 41; i++) {
            String rate = line[indexOfAge18 + i];
            doubles.add(Double.valueOf(StringUtils.replace(rate, ",", ".")));
        }
        return doubles;
    }

    private class ProductIFineRate {
        private String planName;
        private Boolean riskOccupation;
        private List<Double> taxDeductibleMaleRate;
        private List<Double> nonTaxDeductibleMaleRate;
        private List<Double> taxDeductibleFemaleRate;
        private List<Double> nonTaxDeductibleFemaleRate;

        public String getPlanName() {
            return planName;
        }

        public void setPlanName(String planName) {
            this.planName = planName;
        }

        public Boolean getRiskOccupation() {
            return riskOccupation;
        }

        public void setRiskOccupation(Boolean riskOccupation) {
            this.riskOccupation = riskOccupation;
        }

        public List<Double> getTaxDeductibleMaleRate() {
            return taxDeductibleMaleRate;
        }

        public void setTaxDeductibleMaleRate(List<Double> taxDeductibleMaleRate) {
            this.taxDeductibleMaleRate = taxDeductibleMaleRate;
        }

        public List<Double> getNonTaxDeductibleMaleRate() {
            return nonTaxDeductibleMaleRate;
        }

        public void setNonTaxDeductibleMaleRate(List<Double> nonTaxDeductibleMaleRate) {
            this.nonTaxDeductibleMaleRate = nonTaxDeductibleMaleRate;
        }

        public List<Double> getTaxDeductibleFemaleRate() {
            return taxDeductibleFemaleRate;
        }

        public void setTaxDeductibleFemaleRate(List<Double> taxDeductibleFemaleRate) {
            this.taxDeductibleFemaleRate = taxDeductibleFemaleRate;
        }

        public List<Double> getNonTaxDeductibleFemaleRate() {
            return nonTaxDeductibleFemaleRate;
        }

        public void setNonTaxDeductibleFemaleRate(List<Double> nonTaxDeductibleFemaleRate) {
            this.nonTaxDeductibleFemaleRate = nonTaxDeductibleFemaleRate;
        }
    }
}
