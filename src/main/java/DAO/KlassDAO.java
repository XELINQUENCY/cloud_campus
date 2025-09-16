package DAO;

import entity.schoolroll.StudentClass;
import mapper.KlassMapper;

import java.util.List;

public class KlassDAO {
    public List<StudentClass> selectAll() {
        return MyBatisUtil.executeQuery(KlassMapper.class, KlassMapper::findAll);
    }

    public int insert(StudentClass studentClass) {
        return MyBatisUtil.executeUpdate(KlassMapper.class, mapper -> mapper.insert(studentClass));
    }

    public int update(StudentClass studentClass) {
        return MyBatisUtil.executeUpdate(KlassMapper.class, mapper -> mapper.update(studentClass));
    }

    public int delete(String classId) {
        return MyBatisUtil.executeUpdate(KlassMapper.class, mapper->mapper.delete(classId));
    }

}
