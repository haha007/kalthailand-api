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
    private LocalDate receivedDate;
    private LocalDate jobStartedDate;
    private LocalDate jobEndedDate;
    private List<CollectionFileLine> lines = new ArrayList<>();
    private DeductionFile deductionFile;

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

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDate getJobStartedDate() {
        return jobStartedDate;
    }

    public void setJobStartedDate(LocalDate jobStartedDate) {
        this.jobStartedDate = jobStartedDate;
    }

    public LocalDate getJobEndedDate() {
        return jobEndedDate;
    }

    public void setJobEndedDate(LocalDate jobEndedDate) {
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
