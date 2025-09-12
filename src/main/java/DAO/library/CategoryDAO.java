package DAO.library;

import DAO.MyBatisUtil;
import mapper.CategoryMapper;
import entity.library.Category;

import java.util.List;

/**
 * 书籍分类数据访问对象 (CategoryDAO)
 * 负责封装所有对 'categories' 数据表的数据库操作，使用MyBatis实现。
 */
public class CategoryDAO {

    /**
     * 查找所有的书籍分类信息。
     * @return 包含所有 Category 对象的列表。
     */
    public List<Category> findAllCategories() {
        return MyBatisUtil.executeQuery(CategoryMapper.class, mapper -> mapper.findAll());
    }

    /**
     * 根据分类ID查找分类信息。
     * @param categoryId 分类ID
     * @return 如果找到，返回 Category 对象；否则返回 null。
     */
    public Category findCategoryById(int categoryId) {
        return MyBatisUtil.executeQuery(CategoryMapper.class, mapper -> mapper.findById(categoryId));
    }

    /**
     * 添加一个新的书籍分类。 (管理员功能)
     * @param category 要添加的 Category 对象 (不包含ID)
     * @return 添加成功返回 true，失败返回 false。
     */
    public boolean addCategory(Category category) {
        return MyBatisUtil.executeUpdate(CategoryMapper.class, mapper -> mapper.insert(category)) > 0;
    }
}
