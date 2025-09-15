package service.shop;

import entity.shop.Product;
import mapper.ProductMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import DAO.MyBatisUtil;

/**
 * 商品管理服务接口。
 */
public interface ProductService {

    Product getProductById(String productId);
    
    ArrayList<Product> getProductsByCategory(String category);

    List<Product> getAllProducts();

    public boolean addProduct(Product product);

    public boolean updateProduct(Product product);

    public boolean deleteProduct(String productId);
    
    public boolean updateStock(String productId, int newStock);
    
    public boolean updateSales(String productId, int soldAmount);

    public List<String> getAllCategories();

    public List<Product> searchProductsByName(String keyword);
    
    public List<Product> searchProductsById(String keyword);
    
    public Map<String, ArrayList<Product>> getProductsGroupedByCategory();
}
