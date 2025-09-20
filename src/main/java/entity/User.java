package entity;

import enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private Integer age;
    private String gender;
    private String password;
    private String email;
    private Set<UserRole> userRoles = new HashSet<>();
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;

    public boolean hasRole(UserRole role) {
        return this.userRoles.contains(role);
    }

}
