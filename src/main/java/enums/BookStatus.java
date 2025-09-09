package enums;

import lombok.Getter;

@Getter
public enum BookStatus {
    ON_SHELF("在架"),      // On the shelf and available
    CHECKED_OUT("借出"),  // Checked out by a patron
    LOST("丢失");      // Reported as lost

    private final String displayName;

    BookStatus(String displayName) {
        this.displayName = displayName;
    }

}
