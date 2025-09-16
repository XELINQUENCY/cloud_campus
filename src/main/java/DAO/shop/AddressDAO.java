package DAO.shop;

import DAO.MyBatisUtil;
import entity.shop.Address;
import mapper.AddressMapper;

import java.util.List;

import javax.swing.DefaultListModel;

/**
 * 用户收货地址数据访问对象 (DAO)。
 */
public class AddressDAO {

    /**
     * 根据用户ID查找其所有的收货地址。
     * @param userId 用户的唯一标识符。
     * @return 该用户的所有地址列表。
     */
    public List<Address> findByUserId(String userId) {
        return MyBatisUtil.executeQuery(AddressMapper.class, mapper->mapper.findByUserId(userId));
    }

    /**
     * 为用户添加一个新的收货地址。
     * @param address 要添加的地址对象。
     */
    public int insert(String userId, Address address) {
        return MyBatisUtil.executeUpdate(AddressMapper.class, mapper->mapper.insert(userId, address));
    }
    
    public int update(String userId, Address address) {
        return MyBatisUtil.executeUpdate(AddressMapper.class, mapper->mapper.update(userId, address));
    }

    /**
     * 删除一个用户的收货地址。
     * @param address 要删除的地址对象，需要包含 userId 和地址的关键信息来定位。
     */
    public int delete(String userId, Address address) {
        return MyBatisUtil.executeUpdate(AddressMapper.class, mapper->mapper.delete(userId, address));
    }
}
