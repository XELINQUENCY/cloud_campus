package DAO;

import entity.schoolroll.Major;
import mapper.MajorMapper;

import java.util.List;

public class MajorDAO {
    public int insert(Major major) {
        return MyBatisUtil.executeUpdate(MajorMapper.class, mapper->mapper.insert(major));
    }

    public int update(Major major) {
        return MyBatisUtil.executeUpdate(MajorMapper.class, mapper->mapper.update(major));
    }

    public int delete(String majorId) {
        return MyBatisUtil.executeUpdate(MajorMapper.class, mapper->mapper.delete(majorId));
    }

    public List<Major> findAll() {
        return MyBatisUtil.executeQuery(MajorMapper.class, MajorMapper::findAll);
    }
}
