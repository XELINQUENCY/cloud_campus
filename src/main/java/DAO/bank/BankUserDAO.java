package DAO.bank;

import DAO.MyBatisUtil;
import entity.bank.BankUser;
import mapper.BankUserMapper;

/**
 * 银行用户数据访问对象 (重构版)
 * 专门处理 entity.bank.BankUser 实体。
 */
public class BankUserDAO {

    /**
     * 根据用户ID查找银行用户。
     * @param userId 用户的唯一ID
     * @return 如果找到，返回 BankUser 对象；否则返回 null。
     */
    public BankUser findById(String userId) {
        return MyBatisUtil.executeQuery(BankUserMapper.class, mapper -> mapper.findById(userId));
    }

    /**
     * 插入一个新的银行用户。
     * @param user 要插入的 BankUser 对象
     * @return 操作成功返回大于0的整数，否则为0。
     */
    public int insert(BankUser user) {
        return MyBatisUtil.executeUpdate(BankUserMapper.class, mapper -> mapper.insert(user));
    }
}
