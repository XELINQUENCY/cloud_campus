package DAO.schoolroll;

import DAO.MyBatisUtil;
import entity.schoolroll.Major;
import mapper.MajorMapper;

public class MajorDAO {
    public String getNameByMajorId(String majorId) {
        return MyBatisUtil.executeQuery(MajorMapper.class, mapper->mapper.getMajorIdByName(majorId));
    }

    public String getMajorIdByName(String majorName) {
        return MyBatisUtil.executeQuery(MajorMapper.class, mapper->mapper.getMajorIdByName(majorName));
    }

    public int insertMajor(Major major) {
        return MyBatisUtil.executeUpdate(MajorMapper.class, mapper->mapper.insertMajor(major));
    }

    public int updateMajor(Major major) {
        return MyBatisUtil.executeUpdate(MajorMapper.class, mapper->mapper.updateMajor(major));
    }

    public int deleteMajor(String majorId) {
        return MyBatisUtil.executeUpdate(MajorMapper.class, mapper->mapper.deleteMajor(majorId));
    }

}
