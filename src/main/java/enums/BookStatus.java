package enums;

import enums.common.DisplayNameEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BookStatus implements DisplayNameEnum {
    ON_SHELF("在架"),      // On the shelf and available
    CHECKED_OUT("借出"),  // Checked out by a patron
    LOST("丢失"),      // Reported as lost
    RESERVED("保留");

    private final String displayName;

    BookStatus(String displayName) {
        this.displayName = displayName;
    }
    public static BookStatus fromDisplayName(String displayName) {
        return Arrays.stream(BookStatus.values())
                .filter(status -> status.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(null);
    }
	public String getDisplayName() {
		return displayName;
	}
}
