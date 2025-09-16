package entity.shop;

public class OrderItem {
	public String orderId;
	public String productId;
	public int quantity;
	public double price;
	public double total;
	public OrderItem(String orderId, String productId, int quantity, double price, double total) {
		this.orderId = orderId;
		this.productId = productId;
		this.quantity = quantity;
		this.price = price;
		this.total = total;
	}
	
}