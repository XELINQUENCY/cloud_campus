package entity.shop;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Getter
@Setter
public class Order {
	public String orderId;
	public String userId;
	public double finalAmount;
	public double Off; 
	public LocalDateTime createTime;
	public LocalDateTime expectTime;
	public String address;
	public String payWay;
    public ArrayList<OrderItem>items = new ArrayList();
	public Object getUserId() {
		return userId;
	}
}