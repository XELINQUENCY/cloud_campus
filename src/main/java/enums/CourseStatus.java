package enums;

import lombok.Getter;

@Getter
public enum CourseStatus {
    AVAILABLE("启用"),
    UNAVAILABLE("停用");

    private final String displayName;

    CourseStatus(String displayName) {
        this.displayName = displayName;
    }

}
