package common;

import java.util.ArrayList;

public class User {
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String token;
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
