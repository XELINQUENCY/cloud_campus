package DAO;

import entity.BankAccount;
import mapper.BankAccountMapper;

import java.math.BigDecimal;
import java.util.List;

public class BankAccountDAO {
    public BankAccount findByAccountId(String accountId){
        return MyBatisUtil.executeQuery(BankAccountMapper.class, mapper->mapper.findById(accountId));
    }

    public List<BankAccount> findByUserId(String userId){
        return MyBatisUtil.executeQuery(BankAccountMapper.class, mapper->mapper.findByUserId(userId));
    }

    public int updateBalance(String accountId, BigDecimal amount){
        return MyBatisUtil.executeUpdate(BankAccountMapper.class, mapper->mapper.updateBalance(accountId, amount));
    }

    public int createAccount(BankAccount account){
        return MyBatisUtil.executeUpdate(BankAccountMapper.class, mapper->mapper.insert(account));
    }
}
