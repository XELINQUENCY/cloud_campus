package mapper;

import entity.shop.Address;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AddressMapper {
    List<Address> findByUserId(@Param("mainUserId") String mainUserId);
    int insert(@Param("mainUserId") String userId, Address address);
    int update(@Param("mainUserId") String userId, Address address);
    int delete(@Param("mainUserId") String userId, Address address);
}
