package entity;

import enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class User {
    private String id;
    private String name;
    private int age;
    private String gender;
    private String password;
    private Set<UserRole> userRoles = new HashSet<>();
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;


    public boolean hasRole(UserRole role) {
        return this.userRoles.contains(role);
    }
}
