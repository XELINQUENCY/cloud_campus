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
    public List<User> getUserById(int id) {
        // SqlSession 非线程安全，使用完关闭
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.getUserById(id);
        }
    }
}
