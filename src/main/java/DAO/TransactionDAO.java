package DAO;

import entity.Transaction;
import mapper.TransactionMapper;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionDAO {
    public int insert(Transaction tx){
        return MyBatisUtil.executeUpdate(TransactionMapper.class, mapper->mapper.insert(tx));
    }

    public List<Transaction> findByAccountIdAndTime(String accountId, LocalDateTime start, LocalDateTime end){
        return MyBatisUtil.executeQuery(TransactionMapper.class, mapper->mapper.findByAccountIdAndTime(accountId, start, end));
    }
}
