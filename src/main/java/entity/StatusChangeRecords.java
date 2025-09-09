package entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StatusChangeRecords {
    private int recordId;
    private String studentId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changeTime;
    private String reason;
}
