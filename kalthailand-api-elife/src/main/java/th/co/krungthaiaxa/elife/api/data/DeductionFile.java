package th.co.krungthaiaxa.elife.api.data;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class DeductionFile {
    @Id
    private String id;
    private List<DeductionFileLine> lines = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DeductionFileLine> getLines() {
        return lines;
    }

    public void addLine(DeductionFileLine line) {
        this.lines.add(line);
    }
}
