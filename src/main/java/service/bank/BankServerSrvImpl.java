package service.bank;

import DAO.MyBatisUtil;
import DAO.bank.BankAccountDAO;
import DAO.bank.BankUserDAO;
import DAO.bank.TransactionDAO;
import entity.bank.BankAccount;
import entity.bank.BankUser;
import entity.bank.Transaction;
import mapper.BankAccountMapper;
import mapper.TransactionMapper;
import org.apache.ibatis.session.SqlSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

/**
 * 银行服务实现类 (最终重构版)
 * 该类负责处理所有银行业务逻辑，并通过与实体类精确匹配的DAO层与数据库交互。
 * 实现了高效的账户ID生成和严格的事务管理，并在敏感操作中加入了权限验证。
 */
public class BankServerSrvImpl implements IBankServerSrv {

    // 使用与 entity.bank.* 实体匹配的DAO层
    private final BankUserDAO bankUserDAO;
    private final BankAccountDAO bankAccountDAO;
    private final TransactionDAO transactionDAO;

    public BankServerSrvImpl() {
        this.bankUserDAO = new BankUserDAO();
        this.bankAccountDAO = new BankAccountDAO();
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * 【已优化】高效地生成一个唯一的银行账户ID。
     * 通过查询数据库中当月最新的ID来确定下一个序列号，避免了全表扫描。
     *
     * @return 新的银行账户ID
     */
    private String generateAccountId() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) % 100;
        int month = cal.get(Calendar.MONTH) + 1;
        String prefix = "CB" + String.format("%02d%02d", year, month);

        String latestAccountId = bankAccountDAO.findLatestAccountIdByPrefix(prefix);

        int nextSeq = 1;
        if (latestAccountId != null && latestAccountId.startsWith(prefix)) {
            try {
                int lastSeq = Integer.parseInt(latestAccountId.substring(8));
                nextSeq = lastSeq + 1;
            } catch (NumberFormatException e) {
                // 如果解析失败，则从1开始，作为一种安全回退机制
                System.err.println("解析账户ID序列号失败: " + latestAccountId);
            }
        }
        return prefix + String.format("%04d", nextSeq);
    }

    @Override
    public boolean login(String userId, String password) {
        BankUser user = bankUserDAO.findById(userId);
        if (user != null) {
            return PasswordUtil.verifyPassword(password, user.getPassword());
        }
        return false;
    }

    @Override
    public boolean register(String userId, String password) {
        if (bankUserDAO.findById(userId) != null) {
            return false; // 用户已存在
        }

        BankUser newUser = new BankUser(userId, PasswordUtil.encryptPassword(password), null);

        if (bankUserDAO.insert(newUser) > 0) {
            // 注册成功后，自动为用户创建一个默认银行账户
            createAccount(userId);
            return true;
        }
        return false;
    }

    @Override
    public BankAccount createAccount(String userId) {
        if (bankUserDAO.findById(userId) == null) {
            return null; // 用户必须存在才能创建账户
        }

        BankAccount newAccount = new BankAccount(generateAccountId(), userId, new BigDecimal("0"), "正常", LocalDateTime.now());

        if (bankAccountDAO.createAccount(newAccount) > 0) {
            return newAccount;
        }
        return null;
    }

    @Override
    public boolean deposit(String accountId, BigDecimal amount) throws Exception {
        if (amount.doubleValue() <= 0) {
            throw new Exception("存款金额必须大于0。");
        }

        // 使用事务确保数据一致性
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                BankAccountMapper accountMapper = sqlSession.getMapper(BankAccountMapper.class);
                TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);

                BankAccount account = accountMapper.findById(accountId);
                if (account == null) {
                    throw new Exception("存款账户不存在。");
                }

                BigDecimal newBalance = account.getBalance().add(amount);
                accountMapper.updateBalance(accountId, newBalance);

                Transaction tx = new Transaction(0, null, accountId, amount, "存款", LocalDateTime.now(), "");
                transactionMapper.insert(tx);

                sqlSession.commit();
                return true;
            } catch (Exception e) {
                sqlSession.rollback();
                // 重新抛出异常，让Controller层能捕获到具体的错误信息
                throw new Exception("存款失败：" + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean withdraw(String authenticatedUserId, String accountId, BigDecimal amount, String password) throws Exception {
        if (amount.doubleValue() <= 0) {
            throw new Exception("取款金额必须大于0。");
        }

        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                BankAccountMapper accountMapper = sqlSession.getMapper(BankAccountMapper.class);
                TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);

                BankAccount account = accountMapper.findById(accountId);
                if (account == null) {
                    throw new Exception("取款账户不存在。");
                }

                // --- 核心鉴权逻辑 ---
                if (!account.getUserId().equals(authenticatedUserId)) {
                    throw new Exception("权限不足，无法操作他人账户。");
                }
                // ---------------------

                BankUser user = bankUserDAO.findById(account.getUserId());
                if (user == null || !PasswordUtil.verifyPassword(password, user.getPassword())) {
                    throw new Exception("交易密码验证失败。");
                }

                if (account.getBalance().compareTo(amount) < 0) {
                    throw new Exception("账户余额不足。");
                }

                BigDecimal newBalance = account.getBalance().subtract(amount);
                accountMapper.updateBalance(accountId, newBalance);

                Transaction tx = new Transaction(0, accountId, null, amount, "取款", LocalDateTime.now(), "");
                transactionMapper.insert(tx);

                sqlSession.commit();
                return true;
            } catch (Exception e) {
                sqlSession.rollback();
                throw e; // 将业务异常或数据库异常继续向上抛出
            }
        }
    }

    @Override
    public boolean transfer(String authenticatedUserId, String fromAccountId, String toAccountId, BigDecimal amount, String password) throws Exception {
        if (amount.doubleValue() <= 0) {
            throw new Exception("转账金额必须大于0。");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new Exception("不能给自己转账。");
        }

        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                BankAccountMapper accountMapper = sqlSession.getMapper(BankAccountMapper.class);
                TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);

                BankAccount fromAccount = accountMapper.findById(fromAccountId);
                BankAccount toAccount = accountMapper.findById(toAccountId);
                if (fromAccount == null || toAccount == null) {
                    throw new Exception("转出或转入账户不存在。");
                }

                // --- 核心鉴权逻辑 ---
                if (!fromAccount.getUserId().equals(authenticatedUserId)) {
                    throw new Exception("权限不足，无法操作他人账户。");
                }
                // ---------------------

                BankUser user = bankUserDAO.findById(fromAccount.getUserId());
                if (user == null || !PasswordUtil.verifyPassword(password, user.getPassword())) {
                    throw new Exception("交易密码验证失败。");
                }

                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    throw new Exception("转出账户余额不足。");
                }

                accountMapper.updateBalance(fromAccountId, fromAccount.getBalance().subtract(amount));
                accountMapper.updateBalance(toAccountId, toAccount.getBalance().add(amount));

                Transaction tx = new Transaction(0, fromAccountId, toAccountId, amount, "转账", LocalDateTime.now(), "");
                transactionMapper.insert(tx);

                sqlSession.commit();
                return true;
            } catch (Exception e) {
                sqlSession.rollback();
                throw e; // 将业务异常或数据库异常继续向上抛出
            }
        }
    }

    @Override
    public BigDecimal getBalance(String accountId) {
        BankAccount account = bankAccountDAO.findByAccountId(accountId);
        return account != null ? account.getBalance() : new BigDecimal("-1");
    }

    @Override
    public List<Transaction> getTransactions(String accountId, LocalDateTime start, LocalDateTime end) {
        return transactionDAO.findByAccountIdAndTime(accountId, start, end);
    }

    @Override
    public List<BankAccount> getUserAccounts(String userId) {
        return bankAccountDAO.findByUserId(userId);
    }
}
