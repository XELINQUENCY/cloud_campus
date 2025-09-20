package DAO.bank;

import DAO.MyBatisUtil;
import entity.bank.BankAccount;
import mapper.BankAccountMapper;

import java.util.List;

/**
 * 银行账户数据访问对象
 * 专门处理 entity.bank.BankAccount 实体，并提供高效的查询方法。
 */
public class BankAccountDAO {

    public BankAccount findByAccountId(String accountId) {
        return MyBatisUtil.executeQuery(BankAccountMapper.class, mapper -> mapper.findById(accountId));
    }

    public List<BankAccount> findByUserId(String userId) {
        return MyBatisUtil.executeQuery(BankAccountMapper.class, mapper -> mapper.findByUserId(userId));
    }

    public int createAccount(BankAccount account) {
        return MyBatisUtil.executeUpdate(BankAccountMapper.class, mapper -> mapper.insert(account));
    }

    public String findLatestAccountIdByPrefix(String prefix) {
        return MyBatisUtil.executeQuery(BankAccountMapper.class, mapper -> mapper.findLatestAccountIdByPrefix(prefix + "%"));
    }
}
