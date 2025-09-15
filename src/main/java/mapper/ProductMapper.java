package mapper; // 请替换为您的项目包名

import entity.shop.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品数据访问的MyBatis Mapper接口
 * 它取代了原有的 ProductDAO 接口和 ProductDAOImpl 实现类.
 */
public interface ProductMapper {

    /**
     * 获取所有商品
     * @return 商品列表
     */
    List<Product> getAllProducts();

    /**
     * 根据ID获取单个商品
     * @param productId 商品ID
     * @return 单个商品对象，如果未找到则返回null
     */
    Product getProductById(@Param("productId") String productId);

    /**
     * 根据类别获取商品列表
     * @param category 商品类别
     * @return 该类别下的商品列表
     */
    List<Product> getProductsByCategory(@Param("category") String category);

    /**
     * 添加一个新商品
     * @param product 要添加的商品对象
     * @return 受影响的行数 (通常为 1 表示成功)
     */
    int addProduct(Product product);

    /**
     * 更新一个已存在的商品信息
     * @param product 包含更新信息的商品对象
     * @return 受影响的行数 (通常为 1 表示成功)
     */
    int updateProduct(Product product);

    /**
     * 根据ID删除一个商品
     * @param productId 要删除的商品ID
     * @return 受影响的行数 (通常为 1 表示成功)
     */
    int deleteProduct(@Param("productId") String productId);

    /**
     * 更新指定商品的库存数量
     * @param productId 商品ID
     * @param newStock 新的库存数量
     * @return 受影响的行数 (通常为 1 表示成功)
     */
    int updateStock(@Param("productId") String productId, @Param("newStock") int newStock);

    /**
     * 更新指定商品的已售数量
     * @param productId 商品ID
     * @param soldAmount 新的已售数量
     * @return 受影响的行数 (通常为 1 表示成功)
     */
    int updateSales(@Param("productId") String productId, @Param("soldAmount") int soldAmount);

    /**
     * 获取所有不重复的商品类别
     * @return 类别名称列表
     */
    List<String> getAllCategories();

    /**
     * 根据商品名称进行模糊搜索
     * @param keyword 搜索关键词
     * @return 匹配的商品列表
     */
    List<Product> searchProductsByName(@Param("keyword") String keyword);

    /**
     * 根据商品ID进行模糊搜索
     * @param keyword 搜索关键词
     * @return 匹配的商品列表
     */
    List<Product> searchProductsByID(@Param("keyword") String keyword);
}