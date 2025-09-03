package common;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class User {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String token;
    @Setter
    @Getter
    private Integer id;


    public User() {
        setId(null);
        setToken(null);
        setName(null);
    }
    public User(String name, Integer id, String token) {
        setName(name);
        setId(id);
        setToken(token);

    }

    @Override
    public String toString() {
        return "User{"+getId()+','+getName()+','+getToken()+'}';
    }
}
