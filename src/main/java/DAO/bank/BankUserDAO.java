package DAO.bank;

import DAO.MyBatisUtil;
import entity.bank.BankUser;
import mapper.BankUserMapper;

/**
 * 银行用户数据访问对象
 * 专门处理 entity.bank.BankUser 实体。
 */
public class BankUserDAO {

    public BankUser findById(String userId) {
        return MyBatisUtil.executeQuery(BankUserMapper.class, mapper -> mapper.findById(userId));
    }

    public int insert(BankUser user) {
        return MyBatisUtil.executeUpdate(BankUserMapper.class, mapper -> mapper.insert(user));
    }
}
