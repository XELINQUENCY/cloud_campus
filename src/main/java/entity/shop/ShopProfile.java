package entity.shop;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

@Getter
@Setter
public class ShopProfile {
	public String userId;
	private double balanceShop; //商店余额
	private int points;
	private String passwordShop; //商店支付密码
	public List<Coupon>myCouponList;
	public List<Order>historyOrders;
	public List<Address> addressListModel;
	
	public ShopProfile(String userId, double balanceShop, int points, String passwordShop) {
		this.balanceShop = balanceShop;
		this.passwordShop = passwordShop;
		this.points = points;
		this.userId = userId;
		this.myCouponList = new ArrayList<>();
		this.historyOrders = new ArrayList<>();
		this.addressListModel = new ArrayList<>();
	}
	
	public double getBalanceShop() { return balanceShop; }
	public void setBalanceShop(double balanceShop) { this.balanceShop = balanceShop; }
	public String getPasswordShop() { return passwordShop; }
	public void setPasswordShop(String passwordShop) { this.passwordShop = passwordShop; }
	public int getPoints() { return points; }
	public void setPoints(int points) { this.points += points; }

	public void setUserId(String userId) {
		this.userId = userId;
	}
}



