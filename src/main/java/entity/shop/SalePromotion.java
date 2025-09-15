package entity.shop;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SalePromotion {
	private String productId;
	private double discount;
	private LocalDateTime dueTime;
	private String promotionId;
	
	//特定商品折扣 如果productID为null 则为全场折扣
	public SalePromotion(String productId, double discount, LocalDateTime dueTime, String promotionId) {
		this.productId = productId;
		this.discount = discount;
		this.dueTime = dueTime;
		this.promotionId = promotionId;
	}

    public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	
}
