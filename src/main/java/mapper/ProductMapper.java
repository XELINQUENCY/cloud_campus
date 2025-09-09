package mapper;

import entity.Product;

import java.util.List;

public interface ProductMapper {
    Product findProductById(int productId);
    List<Product> findByCategory(String category);
    List<Product> search(String keyword);
    int insertProduct(Product product);
    int updateProduct(Product product);
    int deleteProduct(int productId);
    int updateStock(int productId, int quantity);
}
