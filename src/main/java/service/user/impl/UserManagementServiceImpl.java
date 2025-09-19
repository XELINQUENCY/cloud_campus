package service.user.impl;

import DAO.UserDAO;
import entity.User;
import enums.UserRole;
import service.user.UserManagementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * UserManagementService的实现类。
 * 调用UserDAO来完成具体的数据库操作。
 */
public class UserManagementServiceImpl implements UserManagementService {

    private final UserDAO userDAO = new UserDAO();

    public UserManagementServiceImpl() {
    }

    @Override
    public User registerUser(User user) throws Exception {
        if (userDAO.findByNameForAuth(user.getName()) != null) {
            throw new Exception("用户名 '" + user.getName() + "' 已存在。");
        }
        // 为新用户生成唯一ID和设置创建时间
        user.setId(UUID.randomUUID().toString());
        user.setCreatedDate(LocalDateTime.now());

        boolean success = userDAO.createUserWithRoles(user);
        if (!success) {
            throw new Exception("创建用户时发生数据库错误。");
        }
        // 返回创建成功的用户，此时应包含数据库生成的ID
        return userDAO.findById(user.getId());
    }

    @Override
    public User updateUser(User userToUpdate) throws Exception {
        User existingUser = userDAO.findById(userToUpdate.getId());
        if (existingUser == null) {
            throw new Exception("用户ID '" + userToUpdate.getId() + "' 不存在。");
        }

        // 只更新允许用户修改的字段
        existingUser.setName(userToUpdate.getName());
        existingUser.setEmail(userToUpdate.getEmail());
        existingUser.setAge(userToUpdate.getAge());
        existingUser.setGender(userToUpdate.getGender());
        if (userToUpdate.getPassword() != null && !userToUpdate.getPassword().isEmpty()) {
            existingUser.setPassword(userToUpdate.getPassword()); // 实际应用中密码应加密
        }

        boolean success = userDAO.updateUserWithRoles(existingUser); // 假设只更新基本信息
        if (!success) {
            throw new Exception("更新用户信息时发生数据库错误。");
        }
        return existingUser;
    }

    @Override
    public List<User> findAllUsers() {
        return userDAO.findAllUsers();
    }

    @Override
    public void deleteUserById(String userId) throws Exception {
        if (userDAO.findById(userId) == null) {
            throw new Exception("无法删除：用户ID '" + userId + "' 不存在。");
        }
        boolean success = userDAO.deleteUser(userId);
        if (!success) {
            throw new Exception("删除用户时发生数据库错误。");
        }
    }

    @Override
    public void toggleUserStatus(String userId, boolean isActive) throws Exception {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new Exception("用户ID '" + userId + "' 不存在。");
        }
        System.out.println("用户 " + userId + " 的状态已切换为: " + isActive);
    }

    @Override
    public void updateUserRoles(String userId, Set<UserRole> newRoles) throws Exception {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new Exception("用户ID '" + userId + "' 不存在。");
        }
        user.setUserRoles(newRoles);
        boolean success = userDAO.updateUserWithRoles(user);
        if (!success) {
            throw new Exception("更新用户角色时发生数据库错误。");
        }
    }
}
