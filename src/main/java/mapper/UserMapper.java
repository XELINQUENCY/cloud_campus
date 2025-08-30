package mapper;

import common.User;

import java.util.List;

public interface UserMapper {
    List<User> getAllUsers();
    List<User> getUserById(int id);
}
