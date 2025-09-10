package DAO;

import mapper.ProductMapper;
import entity.Product;
import java.util.List;

public class ProductDAO {


    public Product findById(int productId){
        // 调用MyBatisUtil的通用方法，并传入ProductMapper.class
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.findProductById(productId));
    }

    public List<Product> findByCategory(String category){
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.findByCategory(category));
    }

    public List<Product> search(String keyword){
        return MyBatisUtil.executeQuery(ProductMapper.class, mapper -> mapper.search(keyword));
    }

    public boolean addProduct(Product product){
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.insertProduct(product)) > 0;
    }

    public boolean updateProduct(Product product) {
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.updateProduct(product)) > 0;
    }

    public boolean deleteProduct(int productId) {
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.deleteProduct(productId)) > 0;
    }

    public boolean updateStock(int productId, int quantity) {
        return MyBatisUtil.executeUpdate(
                ProductMapper.class, mapper -> mapper.updateStock(productId, quantity)) > 0;
    }
}