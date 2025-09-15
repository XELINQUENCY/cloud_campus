package mapper;

import entity.bank.Transaction;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 交易记录Mapper接口 (重构版)
 * 定义了针对 transactions 表的数据库操作，与 entity.bank.Transaction 实体对应。
 */
public interface TransactionMapper {

    /**
     * 插入一条新的交易记录。
     * @param tx Transaction 对象
     * @return 受影响的行数
     */
    int insert(Transaction tx);

    /**
     * 根据账户ID和时间范围查找交易记录。
     * @param accountId 账户ID
     * @param start     开始时间
     * @param end       结束时间
     * @return 交易记录列表
     */
    List<Transaction> findByAccountIdAndTime(
            @Param("accountId") String accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
