package th.co.krungthaiaxa.api.elife.repository;

import java.time.LocalDateTime;

interface SessionQuoteRepositoryExtends {
    long countByProductIdAndStartDateInRange(String productName, LocalDateTime startTimeBeginAt, LocalDateTime startTimeEndAt);
}
