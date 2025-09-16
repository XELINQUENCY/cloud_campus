package mapper;

import entity.schoolroll.Major;

import java.util.List;

public interface MajorMapper {
    /**
     * 获取所有专业列表 [cite: 387]
     * @return 所有专业的列表
     */
    List<Major> findAll();

    // (管理员) 增删改专业信息 [cite: 358]
    int insert(Major major);
    int update(Major major);
    int delete(String majorId);
}
