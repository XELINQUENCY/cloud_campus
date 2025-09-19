package mapper;

import entity.User;
import enums.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * MyBatis Mapper 接口，用于操作 users, roles, user_roles 表。
 * 提供了用户信息的CRUD以及用户与角色的关联管理。
 */
public interface UserMapper {

    List<User> findAllUsers();

    /**
     * 根据用户名查找用户及其所有角色（用于登录认证）。
     * @param name 用户名
     * @return 包含角色信息的User对象，如果未找到则返回null。
     */
    User findByName(@Param("name") String name);

    /**
     * 根据用户ID查找用户及其所有角色。
     * @param id 用户ID
     * @return 包含角色信息的User对象，如果未找到则返回null。
     */
    User findById(@Param("id") String id);

    /**
     * 插入一个新的用户基本信息（不包含角色）。
     * @param user 要插入的User对象
     * @return 成功插入的行数。
     */
    int insertUser(User user);

    /**
     * 更新用户的基本信息。
     * @param user 包含更新信息的User对象
     * @return 成功更新的行数。
     */
    int updateUser(User user);

    /**
     * 根据用户ID删除用户。
     * user_roles表中的关联记录会因外键约束被级联删除。
     * @param id 用户ID
     * @return 成功删除的行数。
     */
    int deleteUser(@Param("id") String id);

    /**
     * 为指定用户添加一个角色。
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 成功插入的行数。
     */
    int addUserRole(@Param("userId") String userId, @Param("roleId") int roleId);

    /**
     * 移除用户的所有角色（通常在更新角色前调用）。
     * @param userId 用户ID
     * @return 成功删除的行数。
     */
    int removeUserRoles(@Param("userId") String userId);

    /**
     * 根据角色名查找角色ID。
     * @param roleName 角色名 (例如 "读者", "图书管理员")
     * @return 对应的角色ID，未找到则返回null。
     */
    Integer findRoleIdByName(@Param("roleName") String roleName);
}
