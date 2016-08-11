package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "collectionFile")
public class CollectionFile {
    @Id
    private String id;
    @Indexed(unique = true)
    private String fileHashCode;
    private LocalDateTime receivedDate;
    private LocalDateTime jobStartedDate;
    private LocalDateTime jobEndedDate;
    private List<CollectionFileLine> lines = new ArrayList<>();
    private DeductionFile deductionFile = new DeductionFile();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileHashCode() {
        return fileHashCode;
    }

    public void setFileHashCode(String fileHashCode) {
        this.fileHashCode = fileHashCode;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDateTime getJobStartedDate() {
        return jobStartedDate;
    }

    public void setJobStartedDate(LocalDateTime jobStartedDate) {
        this.jobStartedDate = jobStartedDate;
    }

    public LocalDateTime getJobEndedDate() {
        return jobEndedDate;
    }

    public void setJobEndedDate(LocalDateTime jobEndedDate) {
        this.jobEndedDate = jobEndedDate;
    }

    public List<CollectionFileLine> getLines() {
        return lines;
    }

    public void addLine(CollectionFileLine line) {
        this.lines.add(line);
    }

    public DeductionFile getDeductionFile() {
        return deductionFile;
    }

    public void setDeductionFile(DeductionFile deductionFile) {
        this.deductionFile = deductionFile;
    }
}
