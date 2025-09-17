package service.course;

public interface AdminCourseService {
    /**
     * 为指定学生添加一门课程（强制选课）。
     */
    void addCourseForStudent(String studentId, int teachingId, String semester);

    /**
     * 为指定学生删除一门课程。
     */
    void removeCourseForStudent(String studentId, int teachingId);

    /**
     * 调整课程班级的最大容量。
     */
    void updateCourseCapacity(int teachingId, int newCapacity);
}