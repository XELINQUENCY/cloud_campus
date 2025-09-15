package DAO.bank;

import DAO.MyBatisUtil;
import entity.bank.BankAccount;
import mapper.BankAccountMapper;

import java.util.List;

/**
 * 银行账户数据访问对象 (重构版)
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

    /**
     * 【新增】根据前缀高效查找最新的银行账户ID。
     * 用于解决服务层生成账户ID的性能问题。
     * @param prefix 月份前缀，例如 "CB2509"
     * @return 如果找到，返回最末尾的账户ID；否则返回 null。
     */
    public String findLatestAccountIdByPrefix(String prefix) {
        return MyBatisUtil.executeQuery(BankAccountMapper.class, mapper -> mapper.findLatestAccountIdByPrefix(prefix + "%"));
    }
}
