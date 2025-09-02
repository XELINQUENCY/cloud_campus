package DAO;

import mapper.UserMapper;
import common.User;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public class ConnectToDatabase {

    private final SqlSessionFactory sqlSessionFactory;

    public ConnectToDatabase() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    /**
     * 查询某个用户（只返回 id 和 name）
     */
    public User getUserById(int id) {
        // SqlSession 非线程安全，使用完关闭
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.getUserById(id);
        }
    }

    public int createUser(User user) {
        // 使用 try-with-resources 来确保 session 总能被关闭
        try(SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            mapper.createUser(user);
            session.commit();

            // 提交后，返回由 MyBatis 回填的自增 ID
            return user.getId() != null ? user.getId() : 0;
        } catch (Exception e) {
            // 强烈建议加上异常处理，这样出错了才能看到日志
            e.printStackTrace();
            return 0; // 或抛出异常
        }
    }

    public int updateUser(User user) {
        try(SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.updateUser(user);
        }
    }
}
