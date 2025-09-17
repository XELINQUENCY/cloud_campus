package service.schoolroll;

import entity.StudentQueryCriteria;
import entity.User;
import entity.schoolroll.Student;
import service.schoolroll.exception.BadRequestException;
import service.schoolroll.exception.ForbiddenException;
import service.schoolroll.exception.NotFoundException;

import java.util.List;

public interface StudentService {

    Student getStudent(String studentId, User currentUser) throws ForbiddenException, NotFoundException;

    void createStudent(Student newStudent, User currentUser) throws ForbiddenException, BadRequestException;

    void updateStudent(Student studentToUpdate, User currentUser) throws ForbiddenException, BadRequestException;

    void deleteStudent(String studentId, User currentUser) throws ForbiddenException;

    List<Student> searchStudent(StudentQueryCriteria sQC, User currentUser) throws ForbiddenException;



}
