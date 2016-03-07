package th.co.krungthaiaxa.elife.api.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CollectionFile {
    @Id
    private String id;
    @Indexed(unique = true)
    private String fileHashCode;
    private LocalDate sendDate;
    private List<CollectionFileLine> lines = new ArrayList<>();

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

    public List<CollectionFileLine> getLines() {
        return lines;
    }

    public void addLine(CollectionFileLine line) {
        this.lines.add(line);
    }

    public void setFileHashCode(String fileHashCode) {
        this.fileHashCode = fileHashCode;
    }

    public String getFileHashCode() {
        return fileHashCode;
    }
}
