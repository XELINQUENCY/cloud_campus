package service.user;

import entity.User;
import enums.UserRole;
import java.util.List;
import java.util.Set;

/**
 * 用户管理模块的业务逻辑服务接口。
 */
public interface UserManagementService {

    /**
     * 注册一个新用户。
     * @param user 包含注册信息的User对象
     * @return 注册成功后的User对象
     * @throws Exception 如果用户名已存在或注册失败
     */
    User registerUser(User user) throws Exception;

    /**
     * 更新一个用户的信息。
     * @param userToUpdate 包含更新信息的用户对象
     * @return 更新后的User对象
     * @throws Exception 如果用户不存在或更新失败
     */
    User updateUser(User userToUpdate) throws Exception;

    /**
     * (管理员) 获取所有用户列表。
     * @return 系统中的所有用户列表
     */
    List<User> findAllUsers();

    /**
     * (管理员) 根据ID删除一个用户。
     * @param userId 要删除的用户ID
     * @throws Exception 如果用户不存在或删除失败
     */
    void deleteUserById(String userId) throws Exception;

    /**
     * (管理员) 切换用户的激活状态。
     * (此功能为扩展，当前User实体暂无active字段)
     * @param userId 用户ID
     * @param isActive 新的状态
     * @throws Exception 如果用户不存在
     */
    void toggleUserStatus(String userId, boolean isActive) throws Exception;

    /**
     * (管理员) 修改用户的角色。
     * @param userId 用户ID
     * @param newRoles 新的角色集合
     * @throws Exception 如果用户不存在或更新失败
     */
    void updateUserRoles(String userId, Set<UserRole> newRoles) throws Exception;
}
