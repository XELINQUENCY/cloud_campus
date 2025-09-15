// 创建新类: service.auth.BankUserAuthenticator.java
package service.auth;

import DAO.bank.BankUserDAO;
import dto.LoginRequest;
import entity.User;
import entity.bank.BankUser;
import enums.UserRole;
import service.bank.PasswordUtil;
import java.util.Collections;

public class BankUserAuthenticator implements Authenticator {
    private final BankUserDAO bankUserDAO;

    public BankUserAuthenticator(BankUserDAO bankUserDAO) {
        this.bankUserDAO = bankUserDAO;
    }

    @Override
    public boolean supports(LoginRequest request) {
        // 假设银行登录的 isAdmin 总是 false
        return !request.isAdmin();
    }

    @Override
    public User authenticate(LoginRequest request) throws Exception {
        BankUser bankUser = bankUserDAO.findById(request.getUsername());
        if (bankUser == null) {
            return null; // 用户不存在，交由下一个认证器处理
        }
        if (!PasswordUtil.verifyPassword(request.getPassword(), bankUser.getPassword())) {
            throw new Exception("密码错误。");
        }
        // 适配为通用 User 对象
        User adaptedUser = new User();
        adaptedUser.setId(bankUser.getUserId());
        adaptedUser.setName(bankUser.getUserId());
        adaptedUser.setUserRoles(Collections.singleton(UserRole.BANK_CUSTOMER));
        return adaptedUser;
    }
}