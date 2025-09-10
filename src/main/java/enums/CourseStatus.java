package enums;

import enums.common.DisplayNameEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CourseStatus implements DisplayNameEnum {
    AVAILABLE("启用"),
    UNAVAILABLE("停用");

    private final String displayName;

    CourseStatus(String displayName) {
        this.displayName = displayName;
    }

    public static CourseStatus fromDisplayName(String displayName) {
        return Arrays.stream(CourseStatus.values())
                .filter(status -> status.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(null);
    }

}
