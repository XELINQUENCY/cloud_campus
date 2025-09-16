package enums;

import lombok.Getter;

@Getter
public enum StudentStatus {
    ACTIVE("在读"),       // 在读
    SUSPENDED("休学"),    // 休学
    WITHDRAWN("退学"),    // 退学
    GRADUATED("毕业"),    // 毕业
    EXPELLED("开除");     // 开除

    private final String displayName;
    StudentStatus(String displayName) {
        this.displayName = displayName;
    }

}