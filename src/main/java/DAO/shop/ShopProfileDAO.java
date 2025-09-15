package DAO.shop;

import DAO.MyBatisUtil;
import entity.shop.ShopProfile;
import mapper.ShopProfileMapper;

/**
 * 商店用户资料数据访问对象 (DAO)。
 * 用于管理用户的商店特定信息，如余额、积分等。
 */
public class ShopProfileDAO {

    /**
     * 根据用户ID查找商店资料。
     * @param userId 用户的唯一标识符 (关联到 User.id)。
     * @return 找到的 ShopProfile 对象，如果不存在则返回 null。
     */
    public ShopProfile findById(String userId) {
        return MyBatisUtil.executeQuery(ShopProfileMapper.class, mapper->mapper.findById(userId));
    }

    /**
     * 为用户创建一个新的商店资料。
     * @param profile 要插入的商店资料对象。
     */
    public int insert(String id) {
    	ShopProfile profile = new ShopProfile(id, 0.0, 0, "123456");
        return MyBatisUtil.executeUpdate(ShopProfileMapper.class, mapper->mapper.insert(profile));
    }

    /**
     * 更新用户的商店资料。
     * @param profile 包含更新信息的商店资料对象。
     */
    public int update(ShopProfile profile) {
        return MyBatisUtil.executeUpdate(ShopProfileMapper.class, mapper->mapper.update(profile));
    }
}
