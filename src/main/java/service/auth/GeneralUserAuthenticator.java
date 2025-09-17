// 创建新类: service.auth.GeneralUserAuthenticator.java
package service.auth;

import DAO.UserDAO;
import dto.LoginRequest;
import entity.User;
import enums.UserRole;

public class GeneralUserAuthenticator implements Authenticator {
    private final UserDAO userDAO;

    public GeneralUserAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean supports(LoginRequest request) {
        // 这个认证器处理所有非银行模块的登录
        return true; // 作为默认的认证器，总是支持
    }

    @Override
    public User authenticate(LoginRequest request) throws Exception {
        User user = userDAO.findByNameForAuth(request.getUsername());
        if (user == null) {
            return null; // 返回null，让AuthService尝试下一个认证器
        }
        if (!request.getPassword().equals(user.getPassword())) {
            throw new Exception("密码错误。");
        }
        boolean hasRequiredRole = request.isAdmin()
                ? user.hasRole(UserRole.LIBRARIAN) || user.hasRole(UserRole.STORE_ADMIN) || user.hasRole(UserRole.ACADEMIC_ADMIN)
                : user.hasRole(UserRole.READER) || user.hasRole(UserRole.STORE_CUSTOMER) || user.hasRole(UserRole.STORE_ADMIN);
        if (!hasRequiredRole) {
            throw new Exception("权限不足。");
        }
        return user;
    }
}