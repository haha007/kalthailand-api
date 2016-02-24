package th.co.krungthaiaxa.elife.api.products;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class ProductIBeginRate {
    @Id
    private String id;
    @Indexed
    private String plan;
    @Indexed
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

    public List<Double> getFemaleRate() {
        return femaleRate;
    }

}
