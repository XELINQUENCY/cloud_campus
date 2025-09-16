package mapper;

import entity.schoolroll.Major;

public interface MajorMapper {

    String getNameByMajorId(String majorId);

    String getMajorIdByName(String majorName);

    int insertMajor(Major major);

    int updateMajor(Major major);

    int deleteMajor(String majorId);
}
