package DAO;

import mapper.ProductMapper;
import entity.Product;
import java.util.List;

public class ProductDAO {


    Product findById(int productId){
        // 调用MyBatisUtil的通用方法，并传入ProductMapper.class
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.findProductById(productId));
    }

    List<Product> findByCategory(String category){
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.findByCategory(category));
    }

    List<Product> search(String keyword){
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.search(keyword));
    }

    boolean addProduct(Product product){
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.insertProduct(product)) > 0;
    }

    boolean updateProduct(Product product) {
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.updateProduct(product)) > 0;
    }

    boolean deleteProduct(int productId) {
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.deleteProduct(productId)) > 0;
    }

    boolean updateStock(int productId, int quantity) {
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.updateStock(productId, quantity)) > 0;
    }
}