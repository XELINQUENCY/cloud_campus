package mapper;

import entity.Transaction;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionMapper {
    int insert(Transaction tx);
    List<Transaction> findByAccountIdAndTime(
            @Param("accountId") String accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
