package mapper;

import entity.bank.BankAccount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 银行账户Mapper接口 (重构版)
 * 定义了针对 bank_accounts 表的数据库操作，与 entity.bank.BankAccount 实体对应。
 */
public interface BankAccountMapper {

    BankAccount findById(@Param("accountId") String accountId);

    List<BankAccount> findByUserId(@Param("userId") String userId);

    int updateBalance(@Param("accountId") String accountId, @Param("newBalance") BigDecimal newBalance);

    int insert(BankAccount account);

    /**
     * 【新增】根据ID前缀查找最后一条记录。
     * 使用 LIKE 和 ORDER BY DESC LIMIT 1 来高效实现。
     * @param prefix eg: "CB2509%"
     * @return 最新的账户ID字符串
     */
    String findLatestAccountIdByPrefix(@Param("prefix") String prefix);
}
