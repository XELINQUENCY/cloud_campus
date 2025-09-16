package mapper;

import entity.schoolroll.StudentClass;

import java.util.List;

public interface KlassMapper {
    /**
     * 获取所有班级列表（用于前端下拉框等）
     * @return 所有班级的列表
     */
    List<StudentClass> findAll();

    // (管理员) 增删改班级信息
    int insert(StudentClass kls);
    int update(StudentClass kls);
    int delete(String classId);

}
