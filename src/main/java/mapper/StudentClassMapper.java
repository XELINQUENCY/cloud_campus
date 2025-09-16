package mapper;

import entity.schoolroll.StudentClass;

public interface StudentClassMapper {
    String getNameByClassId(String classId);

    String getClassIdByName(String className);

    int insertClass(StudentClass studentClass);

    int updateClass(StudentClass studentClass);

    int deleteClass(String classId);
}
