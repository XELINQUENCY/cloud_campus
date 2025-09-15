package DAO.shop;

import DAO.MyBatisUtil;
import entity.shop.SalePromotion;
import mapper.ProductMapper;
import mapper.SalePromotionMapper;

import java.util.List;

/**
 * 商品促销活动数据访问对象 (DAO)。
 */
public class SalePromotionDAO {

    /**
     * 根据促销ID查找促销活动。
     * @param promotionId 促销活动的唯一标识符。
     * @return 找到的 SalePromotion 对象，如果不存在则返回 null。
     */
    public SalePromotion findById(String promotionId) {
        return MyBatisUtil.executeQuery(SalePromotionMapper.class, mapper->mapper.findById(promotionId));
    }

    /**
     * 查找指定商品的所有促销活动。
     * @param productId 商品ID。
     * @return 该商品的促销活动列表。
     */
    public List<SalePromotion> findByProductId(String productId) {
        return MyBatisUtil.executeQuery(SalePromotionMapper.class, mapper->mapper.findByProductId(productId));
    }

    /**
     * 获取所有促销活动。
     * @return 包含所有促销活动的列表。
     */
    public List<SalePromotion> findAll() {
        return MyBatisUtil.executeQuery(SalePromotionMapper.class, SalePromotionMapper::findAll);
    }

    /**
     * 插入一个新的促销活动。
     * @param promotion 要插入的促销活动对象。
     */
    public int insert(SalePromotion promotion) {
        return MyBatisUtil.executeUpdate(SalePromotionMapper.class, mapper->mapper.insert(promotion));
    }

    /**
     * 更新一个促销活动。
     * @param promotion 包含更新信息的促销活动对象。
     */
    public int update(SalePromotion promotion) {
        return MyBatisUtil.executeUpdate(SalePromotionMapper.class, mapper->mapper.update(promotion));
    }

    /**
     * 根据ID删除一个促销活动。
     * @param promotionId 要删除的促销活动的ID。
     */
    public int delete(String promotionId) {
        return MyBatisUtil.executeUpdate(SalePromotionMapper.class, mapper->mapper.delete(promotionId));
    }
}
