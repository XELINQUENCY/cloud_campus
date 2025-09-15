package service.bank;

import entity.bank.BankAccount;
import entity.bank.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 银行核心服务接口 (已更新)
 * 为取款和转账等敏感操作添加了 'authenticatedUserId' 参数，用于在服务层进行权限验证。
 */
public interface IBankServerSrv {
    // 用户操作
    boolean login(String userId, String password);
    boolean register(String userId, String password);

    // 账户操作
    BankAccount createAccount(String userId);

    // 交易操作
    boolean deposit(String accountId, BigDecimal amount) throws Exception;

    /**
     * 从指定账户取款。
     * @param authenticatedUserId 从Token中解析出的、当前已登录用户的ID
     * @param accountId           要取款的银行账户ID
     * @param amount              取款金额
     * @param password            银行用户的交易密码
     * @return 操作成功返回 true
     * @throws Exception 业务异常（如权限不足、密码错误、余额不足）
     */
    boolean withdraw(String authenticatedUserId, String accountId, BigDecimal amount, String password) throws Exception;

    /**
     * 从一个账户向另一个账户转账。
     * @param authenticatedUserId 从Token中解析出的、当前已登录用户的ID
     * @param fromAccountId       转出账户ID
     * @param toAccountId         转入账户ID
     * @param amount              转账金额
     * @param password            转出账户的交易密码
     * @return 操作成功返回 true
     * @throws Exception 业务异常（如权限不足、密码错误、余额不足）
     */
    boolean transfer(String authenticatedUserId, String fromAccountId, String toAccountId, BigDecimal amount, String password) throws Exception;

    // 查询操作
    BigDecimal getBalance(String accountId);
    List<Transaction> getTransactions(String accountId, LocalDateTime start, LocalDateTime end);
    List<BankAccount> getUserAccounts(String userId);
}
