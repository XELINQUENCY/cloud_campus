package service.shop.impl;

import DAO.shop.ProductDAO;
import entity.shop.Product;
import service.shop.ProductService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductServiceImpl implements ProductService {
	
    public ProductServiceImpl() {
		
	}
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    public Product getProductById(String productId) {
        return productDAO.getProductById(productId);
    }
    
    @Override
    public ArrayList<Product> getProductsByCategory(String category) {
    	return productDAO.getProductsByCategory(category);
    }

    @Override
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    @Override
    public boolean addProduct(Product product) {
        // 在这里可以添加业务校验逻辑，例如检查商品ID是否已存在等
        return productDAO.addProduct(product);
    }

    @Override
    public boolean updateProduct(Product product) {
        return productDAO.updateProduct(product);
    }

    @Override
    public boolean deleteProduct(String productId) {
        // 在这里可以添加业务校验逻辑，例如检查商品是否在进行中的订单里
        return productDAO.deleteProduct(productId);
    }
    
    @Override
    public boolean updateStock(String productId, int newStock) {
    	return productDAO.updateStock(productId, newStock);
    }
    
    @Override
    public boolean updateSales(String productId, int soldAmount) {
    	return productDAO.updateSales(productId, soldAmount);
    }

    @Override
    public List<String> getAllCategories() {
    	return productDAO.getAllCategories();
    }

    @Override
    public List<Product> searchProductsByName(String keyword){
    	return productDAO.searchProductsByName(keyword);
    }
    
    @Override
    public List<Product> searchProductsById(String keyword){
    	return productDAO.searchProductsByID(keyword);
    }
    
    @Override
    public Map<String, ArrayList<Product>> getProductsGroupedByCategory() {
    	List<Product> allProducts = getAllProducts(); // 确保 getAllProducts() 返回的数据已排序
        Map<String, ArrayList<Product>> productsGroupedByCategory = new HashMap<>();
        for (Product p : allProducts) {
            String category = p.getCategory();
            productsGroupedByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(p);
        }
		return productsGroupedByCategory;
    }
}
