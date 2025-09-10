package mapper;

import entity.StatusChangeRecord;

import java.util.List;

public interface StatusChangeRecordMapper {
    /**
     * 插入一条新的学籍异动记录
     * @param record 异动记录对象
     * @return 受影响的行数
     */
    int insert(StatusChangeRecord record);

    /**
     * 根据学生id调取所有记录
     * @param studentId 学生id
     * @return 记录列表
     */
    List<StatusChangeRecord> fintByStudentId(String studentId);
}
