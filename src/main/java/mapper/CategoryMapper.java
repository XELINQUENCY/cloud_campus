package mapper;

import entity.library.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于操作 categories 表。
 */
public interface CategoryMapper {

    /**
     * 查找所有的书籍分类信息。
     * @return 包含所有 Category 对象的列表
     */
    List<Category> findAll();

    /**
     * 根据分类ID查找分类信息。
     * @param categoryId 分类ID
     * @return 找到则返回 Category 对象，否则返回 null
     */
    Category findById(@Param("categoryId") int categoryId);

    /**
     * 添加一个新的书籍分类。
     * @param category 要添加的 Category 对象 (不包含ID)
     * @return 成功插入的行数 (通常是1)
     */
    int insert(Category category);
}

