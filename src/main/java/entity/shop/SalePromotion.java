package entity.shop;

import java.time.LocalDateTime;

public class SalePromotion {
	private final String productId;
	private final double discount;
	private final LocalDateTime dueTime;
	private String promotionId;
	
	//特定商品折扣 如果productID为null 则为全场折扣
	public SalePromotion(String productId, double discount, LocalDateTime dueTime, String promotionId) {
		this.productId = productId;
		this.discount = discount;
		this.dueTime = dueTime;
		this.promotionId = promotionId;
	}

	public String getPromotionId() { return promotionId; }
	public String getProductId() { return productId; }
	public double getDiscount() { return discount; }
	public LocalDateTime getDueTime() { return dueTime; }

	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	
}
