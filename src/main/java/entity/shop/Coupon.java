package entity.shop;

import java.time.LocalDateTime;

public class Coupon {
	private final String couponId;
	private String name;
	private double spendMoney;
	private double offMoney;
	private String category;
	private LocalDateTime dueTime;
	private boolean used;
	
	public Coupon(String couponId, String name, double spendMoney, double offMoney, String category, LocalDateTime dueTime) {
		this.couponId = couponId;
		this.name = name;
		this.spendMoney = spendMoney;
		this.offMoney = offMoney;
		this.category = category;
		this.dueTime = dueTime;
		this.used = false;
	}

	public String getCouponId() { return couponId; }
	public String getName() { return name; }
	public double getSpendMoney() { return spendMoney; }
	public double getOffMoney() { return offMoney; }
	public String getCategory() { return category; }
	public LocalDateTime getDueTime() { return dueTime; }
	public boolean isUsed() { return used; }
	public void setUsed(boolean used) { this.used = used; }
	//for admin
	public void setName(String name) { this.name = name; }
	public void setSpendMoney(double spendMoney) { this.spendMoney = spendMoney; }
	public void setOffMoney(double offMoney) { this.offMoney = offMoney; }
	public void setCategory(String category) { this.category = category; }
	public void setDueTime(LocalDateTime dueTime) { this.dueTime = dueTime; }

}