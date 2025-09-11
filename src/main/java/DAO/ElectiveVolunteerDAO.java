package DAO;

import entity.ElectiveVolunteer;
import entity.SelectedCourse;
import mapper.ElectiveVolunteerMapper;
import mapper.SelectedCourseMapper;
import mapper.StudentMapper;

import java.util.List;

public class ElectiveVolunteerDAO {
    public List<ElectiveVolunteer> findByCourseId(String courseId) {
        return MyBatisUtil.executeQuery(ElectiveVolunteerMapper.class, mapper->mapper.findByCourseId(courseId));
    }

    public int batchInsert(List<ElectiveVolunteer> records) {
        return MyBatisUtil.executeUpdate(ElectiveVolunteerMapper.class, mapper->mapper.batchInsert(records));
    }

    public int deleteByStudentId(String studentId) {
        return MyBatisUtil.executeUpdate(ElectiveVolunteerMapper.class, mapper->mapper.deleteByStudentId(studentId));
    }
}
