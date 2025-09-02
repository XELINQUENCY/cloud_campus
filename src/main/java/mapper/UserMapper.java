package mapper;

import common.User;

import java.util.List;

public interface UserMapper {
    List<User> getAllUsers();
    User getUserById(int id);
    int createUser(User user);

    int updateUser(User user);
}
