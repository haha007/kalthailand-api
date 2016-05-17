package th.co.krungthaiaxa.api.elife.products;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class ProductIGenRate {
    @Id
    private String id;
    @Indexed
    private String gender;
    private List<Double> dvdRate;
    private List<Double> averageExtraDvdRate;
    private List<Double> maximumExtraDvdRate;
    private List<Double> rate;

    public String getId() {
        return id;
    }

    public String getGender() {
        return gender;
    }

    public List<Double> getDvdRate() {
        return dvdRate;
    }

    public List<Double> getAverageExtraDvdRate() {
        return averageExtraDvdRate;
    }

    public List<Double> getMaximumExtraDvdRate() {
        return maximumExtraDvdRate;
    }

    public List<Double> getRate() {
        return rate;
    }
}
