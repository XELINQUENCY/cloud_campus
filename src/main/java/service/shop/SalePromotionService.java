package service.shop;

import entity.shop.SalePromotion;
import java.util.List;

/**
 * 商品促销活动服务接口。
 */
public interface SalePromotionService {

    SalePromotion getPromotionById(String promotionId);

    List<SalePromotion> getPromotionsByProductId(String productId);

    List<SalePromotion> getAllPromotions();

    void addPromotion(SalePromotion promotion);

    void updatePromotion(SalePromotion promotion);

    void deletePromotion(String promotionId);
}
