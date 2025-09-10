package mapper;

import entity.BankUser;
import org.apache.ibatis.annotations.Param;

public interface BankUserMapper {
    BankUser findById(@Param("userId")String userId);
    int insert(BankUser user);
}
