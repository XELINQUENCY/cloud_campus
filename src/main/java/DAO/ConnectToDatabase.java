package DAO;

import mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import entity.User;


public class ConnectToDatabase {

    private final SqlSessionFactory sqlSessionFactory;

    public ConnectToDatabase() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    /**
     * 查询某个用户（返回 id 和 name）
     */
    public User getUserById(int id) {
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
            return user.getId() != null ? 1 : 0;
        } catch (Exception e) {
            // 强烈建议加上异常处理，这样出错了才能看到日志
            e.printStackTrace();
            return 0; // 或抛出异常
        }
    }

    public int updateUser(User user) {
        try(SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // updateUser 方法的返回值是受影响的行数 (int)
            int affectedRows = mapper.updateUser(user);
            if (affectedRows > 0) {
                session.commit();
            } else {
                // 没有行被更新（可能传入的 id 不存在），
                session.rollback();
            }

            return affectedRows; // 返回受影响的行数
        } catch (Exception e) {
            // 记得打印异常，便于调试
            e.printStackTrace();
            return 0; // 发生异常时返回0
        }
    }

}
