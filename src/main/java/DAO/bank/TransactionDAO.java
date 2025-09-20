package DAO.bank;

import DAO.MyBatisUtil;
import entity.bank.Transaction;
import mapper.TransactionMapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 交易记录数据访问对象
 * 专门处理 entity.bank.Transaction 实体。
 */
public class TransactionDAO {

    public List<Transaction> findByAccountIdAndTime(String accountId, LocalDateTime start, LocalDateTime end) {
        return MyBatisUtil.executeQuery(TransactionMapper.class, mapper -> mapper.findByAccountIdAndTime(accountId, start, end));
    }
}
