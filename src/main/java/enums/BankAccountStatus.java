package enums;

import enums.common.DisplayNameEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BankAccountStatus implements DisplayNameEnum {
    NORMAL("正常"),
    ABNORMAL("异常");

    private final String displayName;

    BankAccountStatus(String displayName) {
        this.displayName = displayName;
    }
    public static BankAccountStatus fromDisplayName(String displayName) {
        return Arrays.stream(BankAccountStatus.values())
                .filter(status -> status.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(null);
    }
}
