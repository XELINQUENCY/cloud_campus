package mapper;

import entity.User;

import java.util.List;

public interface UserMapper {
    User getUserById(int id);
    int createUser(User user);
    int updateUser(User user);
    int deleteUser(int id);

    List<User> getUsersByCondition(String condition);
}
