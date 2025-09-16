package DAO.schoolroll;

import DAO.MyBatisUtil;
import entity.schoolroll.StudentClass;
import mapper.MajorMapper;
import mapper.StudentClassMapper;

public class StudentClassDAO {
    public String getNameByClassId(String classId) {
        return MyBatisUtil.executeQuery(StudentClassMapper.class, mapper->mapper.getNameByClassId(classId));
    }

    public String getClassIdByName(String className) {
        return MyBatisUtil.executeQuery(StudentClassMapper.class, mapper->mapper.getClassIdByName(className));
    }

    public int insertClass(StudentClass studentClass) {
        return MyBatisUtil.executeUpdate(StudentClassMapper.class, mapper->mapper.insertClass(studentClass));
    }

    public int updateClass(StudentClass studentClass) {
        return MyBatisUtil.executeUpdate(StudentClassMapper.class, mapper->mapper.updateClass(studentClass));
    }

    public int deleteClass(String classId) {
        return MyBatisUtil.executeUpdate(StudentClassMapper.class, mapper->mapper.deleteClass(classId));
    }
}
