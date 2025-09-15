package mapper;

import entity.shop.SalePromotion;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SalePromotionMapper {
    /**
     * 根据促销ID查找促销活动。
     * @param promotionId 促销活动的唯一标识符
     * @return 找到的促销活动对象，如果不存在则返回 null
     */
    SalePromotion findById(@Param("promotionId") String promotionId);

    /**
     * 根据商品ID查找所有相关的促销活动。
     * @param productId 商品的唯一标识符
     * @return 包含该商品所有促销活动的列表
     */
    List<SalePromotion> findByProductId(@Param("productId") String productId);

    /**
     * 查找所有促销活动。
     * @return 包含所有促销活动的列表
     */
    List<SalePromotion> findAll();

    /**
     * 插入一个新的促销活动。
     * @param salePromotion 要插入的促销活动对象
     * @return 受影响的行数
     */
    int insert(SalePromotion salePromotion);

    /**
     * 更新一个已存在的促销活动。
     * @param salePromotion 包含更新信息的促销活动对象
     * @return 受影响的行数
     */
    int update(SalePromotion salePromotion);

    /**
     * 根据促销ID删除一个促销活动。
     * @param promotionId 要删除的促销活动的ID
     * @return 受影响的行数
     */
    int delete(@Param("promotionId") String promotionId);
}