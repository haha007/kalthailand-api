package th.co.krungthaiaxa.api.elife.products;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class ProductIBeginRate {
    @Id
    private String id;
    @Indexed
    private Integer nbOfYearsOfPayment;
    @Indexed
    private Double sumInsured;
    private List<Double> maleRate;
    private List<Double> femaleRate;

    public Integer getNbOfYearsOfPayment() {
        return nbOfYearsOfPayment;
    }

    public Double getSumInsured() {
        return sumInsured;
    }

    public List<Double> getMaleRate() {
        return maleRate;
    }

    public List<Double> getFemaleRate() {
        return femaleRate;
    }

}
