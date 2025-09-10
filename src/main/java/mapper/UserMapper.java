package mapper;

import entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    User getUserById(@Param("id")int id);
    int createUser(User user);
    int updateUser(User user);
    int deleteUser(@Param("id")int id);

    List<User> getUsersByCondition(@Param("condition") String condition);
}
