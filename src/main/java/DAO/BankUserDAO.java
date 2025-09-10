package DAO;

import entity.BankUser;
import mapper.BankUserMapper;

public class BankUserDAO {
    public BankUser findById(String userId){
        return MyBatisUtil.executeQuery(BankUserMapper.class, mapper->mapper.findById(userId));
    }

    public int insert(BankUser user){
        return MyBatisUtil.executeUpdate(BankUserMapper.class, mapper->mapper.insert(user));
    }
}
