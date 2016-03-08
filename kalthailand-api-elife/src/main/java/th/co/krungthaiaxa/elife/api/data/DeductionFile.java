package th.co.krungthaiaxa.elife.api.data;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeductionFile {
    @Id
    private String id;
    private List<LocalDateTime> downloadedTimes = new ArrayList<>();
    private List<DeductionFileLine> lines = new ArrayList<>();
}
