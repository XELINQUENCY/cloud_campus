package entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ElectiveTime {
    private int timeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String mode;
    private LocalDateTime updateTime;
}
