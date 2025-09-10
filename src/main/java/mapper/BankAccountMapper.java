package mapper;

import entity.BankAccount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountMapper {
    BankAccount findById(@Param("accountId")String accountId);
    List<BankAccount> findByUserId(@Param("userId")String userId);
    int updateBalance(@Param("accountId")String accountId,
                      @Param("amount")BigDecimal amount);
    int insert(BankAccount account);
}
