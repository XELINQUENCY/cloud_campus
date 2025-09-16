package service.shop.impl;

import DAO.shop.SalePromotionDAO;
import entity.shop.SalePromotion;
import service.shop.SalePromotionService;

import java.util.List;

public class SalePromotionServiceImpl implements SalePromotionService {
	
    public SalePromotionServiceImpl() {
		
	}
    private final SalePromotionDAO salePromotionDAO = new SalePromotionDAO();

    @Override
    public SalePromotion getPromotionById(String promotionId) {
        return salePromotionDAO.findById(promotionId);
    }

    @Override
    public List<SalePromotion> getPromotionsByProductId(String productId) {
        return salePromotionDAO.findByProductId(productId);
    }

    @Override
    public List<SalePromotion> getAllPromotions() {
        return salePromotionDAO.findAll();
    }

    @Override
    public void addPromotion(SalePromotion promotion) {
        salePromotionDAO.insert(promotion);
    }

    @Override
    public void updatePromotion(SalePromotion promotion) {
        salePromotionDAO.update(promotion);
    }

    @Override
    public void deletePromotion(String promotionId) {
        salePromotionDAO.delete(promotionId);
    }
}
