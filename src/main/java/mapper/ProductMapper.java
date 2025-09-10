package mapper;

import entity.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    Product findProductById(@Param("productId")int productId);
    List<Product> findByCategory(@Param("category")String category);
    List<Product> search(@Param("keyword")String keyword);
    int insertProduct(Product product);
    int updateProduct(Product product);
    int deleteProduct(@Param("productId")int productId);
    int updateStock(@Param("productId")int productId,
                    @Param("quantity")int quantity);
}
