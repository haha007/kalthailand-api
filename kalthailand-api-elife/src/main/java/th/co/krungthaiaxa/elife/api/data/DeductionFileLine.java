package th.co.krungthaiaxa.elife.api.data;

import org.springframework.data.annotation.Id;

public class DeductionFileLine {
    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
