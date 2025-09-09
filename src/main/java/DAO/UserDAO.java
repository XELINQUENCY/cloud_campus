package DAO;

import mapper.UserMapper;
import entity.User;
import java.util.List;

public class UserDAO {

    public User getUserById(int id) {
        return MyBatisUtil.executeQuery(UserMapper.class, mapper -> mapper.getUserById(id));
    }

    public int insertUser(User user) {
        return MyBatisUtil.executeUpdate(UserMapper.class, mapper-> mapper.createUser(user));
    }

    public int updateUser(User user) {
        return MyBatisUtil.executeUpdate(UserMapper.class, mapper-> mapper.updateUser(user));
    }

    public int deleteUser(int id) {
        return MyBatisUtil.executeUpdate(UserMapper.class, mapper -> mapper.deleteUser(id));
    }

    public List<User> findUsersByCondition(String condition){
        return MyBatisUtil.executeQuery(UserMapper.class, mapper -> mapper.getUsersByCondition(condition));
    }

    public List<User> findAllUsers(){
        return findUsersByCondition("");
    }
}
