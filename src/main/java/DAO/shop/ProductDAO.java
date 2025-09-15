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
        // 使用 executeQuery 执行读操作
        // 使用方法引用 ProductMapper::getAllProducts 简化 Lambda 表达式
        List<Product> productList = MyBatisUtil.executeQuery(ProductMapper.class, ProductMapper::getAllProducts);
        // 根据接口定义，返回 ArrayList 类型
        return productList != null ? new ArrayList<>(productList) : new ArrayList<>();
    }

    public Product getProductById(String productId) {
        // 使用 executeQuery 执行读操作
        // Lambda 表达式调用带参数的 mapper 方法
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.getProductById(productId));
    }

    public ArrayList<Product> getProductsByCategory(String category) {
        List<Product> productList = MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.getProductsByCategory(category));
        return productList != null ? new ArrayList<>(productList) : new ArrayList<>();
    }

    public boolean addProduct(Product product) {
        // 使用 executeUpdate 执行写操作
        int result = MyBatisUtil.executeUpdate(ProductMapper.class, mapper -> mapper.addProduct(product));
        // 将受影响的行数（int）转换为布尔值
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