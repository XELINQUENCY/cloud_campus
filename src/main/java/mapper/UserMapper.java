package mapper;

import common.User;

import java.util.List;

public interface UserMapper {
    List<User> getAllUsers();
    User getUserById(int id);
}
