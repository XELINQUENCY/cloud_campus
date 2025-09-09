package entity;

import enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class User {
    private String id;
    private String name;
    private int age;
    private String gender;
    private String password;
    private UserRole role;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
