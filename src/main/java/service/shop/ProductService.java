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

    boolean addProduct(Product product);

    boolean updateProduct(Product product);

    boolean deleteProduct(String productId);
    
    boolean updateStock(String productId, int newStock);
    
    boolean updateSales(String productId, int soldAmount);

    List<String> getAllCategories();

    List<Product> searchProductsByName(String keyword);
    
    List<Product> searchProductsById(String keyword);
    
    Map<String, ArrayList<Product>> getProductsGroupedByCategory();
}
