package service.auth;

import DAO.bank.BankUserDAO;
import dto.LoginRequest;
import entity.User;
import entity.bank.BankUser;
import enums.UserRole;
import service.bank.PasswordUtil;
import java.util.Collections;
import java.util.HashSet;

public class BankUserAuthenticator implements Authenticator {
    private final BankUserDAO bankUserDAO;
    // private final UserDAO userDAO; // 【删除】

    public BankUserAuthenticator(BankUserDAO bankUserDAO) {
        this.bankUserDAO = bankUserDAO;
    }

    @Override
    public boolean supports(LoginRequest request) {
        // 这个判断逻辑可以保持，或者根据我们之前讨论的“进阶建议”进行优化
        return !request.isAdmin();
    }

    @Override
    public User authenticate(LoginRequest request) throws Exception {
        BankUser bankUser = bankUserDAO.findById(request.getUsername());
        if (bankUser == null) {
            return null; // 用户在银行不存在，返回 null
        }

        // 验证银行密码
        if (!PasswordUtil.verifyPassword(request.getPassword(), bankUser.getPassword())) {
            throw new Exception("银行密码错误。");
        }

        // --- 核心修改：只创建代表银行用户的 User 对象 ---
        // 3. 将 BankUser 适配为一个只包含银行角色的通用 User 对象
        User adaptedUser = new User();
        adaptedUser.setId(bankUser.getUserId());
        adaptedUser.setName(bankUser.getUserId()); // 银行系统可能没有独立的用户名，暂时使用ID
        adaptedUser.setUserRoles(new HashSet<>(Collections.singleton(UserRole.BANK_CUSTOMER)));
        return adaptedUser; // 返回这个独立的、只代表银行身份的 User 对象
    }
}