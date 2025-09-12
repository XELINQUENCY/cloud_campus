package enums;

import enums.common.DisplayNameEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserRole implements DisplayNameEnum {
    STUDENT("学生"),
    TEACHER("教师"),
    ACADEMIC_ADMIN("教务管理员"),
    READER("读者"),
    LIBRARIAN("图书管理员"),
    BANK_CUSTOMER("银行客户"),
    BANK_ADMIN("银行管理员"),
    STORE_CUSTOMER("商店客户"),
    STORE_ADMIN("商店管理员");

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
