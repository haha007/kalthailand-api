package th.co.krungthaiaxa.elife.api.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CollectionFile {
    @Id
    private String id;
    @Indexed(unique = true)
    private Integer fileHashCode;
    private LocalDate sendDate;
    private List<List<String>> lines = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDate sendDate) {
        this.sendDate = sendDate;
    }

    public List<List<String>> getLines() {
        return lines;
    }

    public void addLine(List<String> line) {
        this.lines.add(line);
    }

    public void setFileHashCode(Integer fileHashCode) {
        this.fileHashCode = fileHashCode;
    }

    public Integer getFileHashCode() {
        return fileHashCode;
    }

    public int calculateFileHashCode() {
        return Objects.hash(id, sendDate, lines);
    }
}
