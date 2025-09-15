package mapper;

import entity.bank.BankUser;
import org.apache.ibatis.annotations.Param;

/**
 * 银行用户Mapper接口 (重构版)
 * 定义了针对 bank_users 表的数据库操作，与 entity.bank.BankUser 实体对应。
 */
public interface BankUserMapper {

    /**
     * 根据用户ID查找银行用户。
     * @param userId 用户ID
     * @return BankUser 对象
     */
    BankUser findById(@Param("userId") String userId);

    /**
     * 插入一个新的银行用户记录。
     * @param user BankUser 对象
     * @return 受影响的行数
     */
    int insert(BankUser user);
}
