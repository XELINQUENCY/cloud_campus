package DAO.bank;

import DAO.MyBatisUtil;
import entity.bank.Transaction;
import mapper.TransactionMapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 交易记录数据访问对象 (重构版)
 * 专门处理 entity.bank.Transaction 实体。
 */
public class TransactionDAO {

    /**
     * 根据账户ID和时间范围查找交易记录。
     * @param accountId 账户ID
     * @param start     开始时间 (可为null)
     * @param end       结束时间 (可为null)
     * @return 交易记录列表
     */
    public List<Transaction> findByAccountIdAndTime(String accountId, LocalDateTime start, LocalDateTime end) {
        return MyBatisUtil.executeQuery(TransactionMapper.class, mapper -> mapper.findByAccountIdAndTime(accountId, start, end));
    }
}
