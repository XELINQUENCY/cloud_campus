package DAO.shop;

import entity.shop.Product;
import mapper.ProductMapper; // 假设 ProductMapper 接口在此包下
import DAO.MyBatisUtil;     // 假设 MyBatisUtil 在此包下

import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO 接口的 MyBatis 实现类。
 * 该类通过 MyBatisUtil 辅助类来执行数据库操作，
 * 实现了数据访问逻辑与数据库会话管理的解耦。
 */
public class ProductDAO {

    public ArrayList<Product> getAllProducts() {
        List<Product> productList = MyBatisUtil.executeQuery(ProductMapper.class, ProductMapper::getAllProducts);
        return productList != null ? new ArrayList<>(productList) : new ArrayList<>();
    }

    public Product getProductById(String productId) {
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.getProductById(productId));
    }

    public ArrayList<Product> getProductsByCategory(String category) {
        List<Product> productList = MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.getProductsByCategory(category));
        return productList != null ? new ArrayList<>(productList) : new ArrayList<>();
    }

    public boolean addProduct(Product product) {
        int result = MyBatisUtil.executeUpdate(ProductMapper.class, mapper -> mapper.addProduct(product));
        return result > 0;
    }

    public boolean updateProduct(Product product) {
        int result = MyBatisUtil.executeUpdate(ProductMapper.class, mapper -> mapper.updateProduct(product));
        return result > 0;
    }

    public boolean deleteProduct(String productId) {
        int result = MyBatisUtil.executeUpdate(ProductMapper.class, mapper -> mapper.deleteProduct(productId));
        return result > 0;
    }

    public boolean updateStock(String productId, int newStock) {
        int result = MyBatisUtil.executeUpdate(ProductMapper.class, mapper -> mapper.updateStock(productId, newStock));
        return result > 0;
    }

    public boolean updateSales(String productId, int soldAmount) {
        int result = MyBatisUtil.executeUpdate(ProductMapper.class, mapper -> mapper.updateSales(productId, soldAmount));
        return result > 0;
    }

    public List<String> getAllCategories() {
        return MyBatisUtil.executeQuery(ProductMapper.class, ProductMapper::getAllCategories);
    }

    public List<Product> searchProductsByName(String keyword) {
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.searchProductsByName(keyword));
    }
    
    public List<Product> searchProductsByID(String keyword) {
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.searchProductsByID(keyword));
    }
}