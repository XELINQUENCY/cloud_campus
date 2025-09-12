package entity.library;

import java.io.Serializable;

/**
 * 书籍分类实体类
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private int categoryId;
    private String categoryName;

    // 构造函数
    public Category() {
    }

    public Category(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Getter 和 Setter 方法
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    // 重写 toString 以便在下拉列表等组件中正确显示
    @Override
    public String toString() {
        return categoryName;
    }
}
