package mapper;

import entity.Klass;

import java.util.List;

public interface KlassMapper {
    /**
     * 获取所有班级列表（用于前端下拉框等）
     * @return 所有班级的列表
     */
    List<Klass> findAll();

    // (管理员) 增删改班级信息
    int insert(Klass kls);
    int update(Klass kls);
    int delete(String classId);

}
