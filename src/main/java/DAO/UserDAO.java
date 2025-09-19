package DAO;

import entity.User;
import enums.UserRole;
import mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.Set;

/**
 * 用户数据访问对象 (重构版)
 * 负责封装所有与用户认证和管理相关的数据库操作。
 * 使用MyBatis实现，并处理用户和角色的事务。
 */
public class UserDAO {

    private final SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();

    /**
     * 根据用户名查找用户及其所有角色，主要用于登录验证。
     * @param name 用户名
     * @return 完整的User对象，包含角色信息；如果用户不存在，返回null。
     */
    public User findByNameForAuth(String name) {
        return MyBatisUtil.executeQuery(UserMapper.class, mapper -> mapper.findByName(name));
    }

    public List<User> findAllUsers() {
        return MyBatisUtil.executeQuery(UserMapper.class, UserMapper::findAllUsers);
    }

    /**
     * 根据ID查找用户。
     * @param id 用户ID
     * @return 完整的User对象。
     */
    public User findById(String id) {
        return MyBatisUtil.executeQuery(UserMapper.class, mapper -> mapper.findById(id));
    }

    /**
     * 创建一个新用户，并为其分配角色（事务性操作）。
     * @param user 要创建的用户对象，其 userRoles 集合应包含要分配的角色。
     * @return 操作成功返回 true，失败返回 false。
     */
    public boolean createUserWithRoles(User user) {
        // 事务管理必须在DAO或Service层手动处理
        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) { // false = 关闭自动提交
            try {
                UserMapper mapper = sqlSession.getMapper(UserMapper.class);
                // 1. 插入用户基本信息
                mapper.insertUser(user);

                // 2. 为用户分配角色
                if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                    for (UserRole role : user.getUserRoles()) {
                        Integer roleId = mapper.findRoleIdByName(role.getDisplayName());
                        if (roleId != null) {
                            mapper.addUserRole(user.getId(), roleId);
                        } else {
                            // 如果角色在数据库中不存在，抛出异常以回滚事务
                            throw new RuntimeException("角色 '" + role.getDisplayName() + "' 在数据库中不存在。");
                        }
                    }
                }
                // 3. 提交事务
                sqlSession.commit();
                return true;
            } catch (Exception e) {
                // 任何一步失败，回滚事务
                sqlSession.rollback();
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 更新用户信息，并重新设置其角色（事务性操作）。
     * @param user 包含更新信息的用户对象。
     * @return 操作成功返回 true，失败返回 false。
     */
    public boolean updateUserWithRoles(User user) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            try {
                UserMapper mapper = sqlSession.getMapper(UserMapper.class);
                // 1. 更新用户基本信息
                mapper.updateUser(user);

                // 2. 先删除该用户的所有旧角色
                mapper.removeUserRoles(user.getId());

                // 3. 再添加所有新角色
                if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                    for (UserRole role : user.getUserRoles()) {
                        Integer roleId = mapper.findRoleIdByName(role.getDisplayName());
                        if (roleId != null) {
                            mapper.addUserRole(user.getId(), roleId);
                        } else {
                            throw new RuntimeException("角色 '" + role.getDisplayName() + "' 在数据库中不存在。");
                        }
                    }
                }
                // 4. 提交事务
                sqlSession.commit();
                return true;
            } catch (Exception e) {
                sqlSession.rollback();
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 删除用户。
     * @param userId 要删除的用户ID。
     * @return 操作成功返回 true，失败返回 false。
     */
    public boolean deleteUser(String userId) {
        // 由于设置了外键级联删除，删除users表中的记录会自动删除user_roles中的关联记录
        return MyBatisUtil.executeUpdate(UserMapper.class, mapper -> mapper.deleteUser(userId)) > 0;
    }
}
