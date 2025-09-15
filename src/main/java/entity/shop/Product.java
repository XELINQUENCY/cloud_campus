package entity.shop;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 商品实体类
 */
@Getter
@Setter
public class Product implements Cloneable {
	private String productId;
    private String name;
    private String category;
    private double price;
    private double weight;
    private int soldAmount;
    private int stockAmount;
    private String description;
    private String imagePath;
    private String choice;  //可选规格
    private String specification;  //整体规格
    
    public Product(String productId, String name, String category, double price, double weight,
    		int soldAmount, int stockAmount, String description, String imagePath, String choice, String specification) {
        this.productId = productId;
    	this.name = name;
    	this.category = category;
        this.price = price;
        this.weight = weight;
        this.soldAmount = soldAmount;
        this.stockAmount = stockAmount;
        this.description = description;
        this.imagePath = imagePath;
        this.choice = choice;
        this.specification = specification;
    }
    
	public String getProductId() { return productId; }
	public String getName() { return name; }
	public String getCategory() { return category; }
	public double getPrice() { return price; }
	public double getWeight() { return weight; }
	public int getSoldAmount() { return soldAmount; }
	public void addSoldAmount(int sold) { soldAmount += sold; }
	public int getStockAmount() { return stockAmount; }
	public void minusStockAmount(int stock) { stockAmount -= stock; }
	public String getDescription() { return description; }
	public String getImagePath() { return imagePath; }
	public String getChoice() { return choice; }
	public String getSpecification() { return specification; }
	public void setSpecialSpecification(String c) {
		if(specification.contains("-")) {
			specification = specification.substring(0, specification.indexOf("-") + 1) + c;
		}
		else { specification = specification + "-" + c; }
	}
	
	//for admin
	public void setName(String name) { this.name = name; }
	public void setPrice(double price) { this.price = price; }
	public void setWeight(double weight) { this.weight = weight; }
	public void setStockAmount(int stockAmount) { this.stockAmount = stockAmount; } 
	public void setDescription(String description) { this.description = description; }
	public void setImagePath(String imagePath) { this.imagePath = imagePath; }
	public void setSpecification(String specification) { this.specification = specification; }
	public void setChoice(String choice) { this.choice = choice; }

	@Override
    public Product clone() {
        try {
            Product cloned = (Product) super.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public boolean equals (Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return Objects.equals(name, product.name) && 
               Objects.equals(specification, product.specification);
    }
    @Override
    public int hashCode() { return Objects.hash(name, specification); }
}