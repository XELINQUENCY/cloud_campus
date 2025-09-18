package client.user;

import client.ApiException;
import entity.User;
import enums.UserRole;
import java.util.List;

/**
 * 用户管理模块的客户端服务接口。
 * 定义了所有用户注册、信息修改、管理相关的客户端操作。
 */
public interface IUserManagementClient {

    User login(String username, String password, boolean isAdmin) throws ApiException;

    /**
     * 注册一个新用户。
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param role 角色
     * @param gender 性别
     * @param age 年龄
     * @return 注册成功的User对象
     * @throws ApiException 如果注册失败
     */
    User register(String username, String password, String email, UserRole role, String gender, Integer age) throws ApiException;

    /**
     * 获取当前登录用户的完整信息。
     * @return 当前用户的User对象
     * @throws ApiException 如果获取失败
     */
    User getMyProfile() throws ApiException;

    /**
     * 更新当前登录用户的信息。
     * @param userToUpdate 包含更新信息的用户对象
     * @return 更新后的User对象
     * @throws ApiException 如果更新失败
     */
    User updateMyProfile(User userToUpdate) throws ApiException;

    // --- 以下为管理员权限操作 ---

    /**
     * (管理员) 获取所有用户的列表。
     * @return 用户列表
     * @throws ApiException 如果操作失败或无权限
     */
    List<User> getAllUsers() throws ApiException;

    /**
     * (管理员) 删除指定ID的用户。
     * @param userId 要删除的用户ID
     * @throws ApiException 如果操作失败或无权限
     */
    void deleteUser(String userId) throws ApiException;

    /**
     * (管理员) 切换用户的激活状态。
     * @param userId 用户ID
     * @param newStatus 新的激活状态 (true为激活, false为禁用)
     * @throws ApiException 如果操作失败或无权限
     */
    void toggleUserStatus(String userId, boolean newStatus) throws ApiException;

    /**
     * (管理员) 修改用户的角色。
     * @param userId 用户ID
     * @param newRoles 新的角色集合
     * @throws ApiException 如果操作失败或无权限
     */
    void changeUserRoles(String userId, java.util.Set<UserRole> newRoles) throws ApiException;
}
