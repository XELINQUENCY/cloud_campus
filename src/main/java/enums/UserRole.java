package enums;

import enums.common.DisplayNameEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserRole implements DisplayNameEnum {
    STUDENT("学生"),
    TEACHER("教师"),
    ADMIN("管理员");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public static UserRole fromDisplayName(String displayName) {
        return Arrays.stream(UserRole.values())
                .filter(status -> status.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(null);
    }
}
