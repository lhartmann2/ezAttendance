package com.lhp.attendance.dao;

import com.lhp.attendance.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentRepository extends CrudRepository<Student, Integer> {

    @Query(value = "SELECT * FROM student s JOIN parentStudent ps ON s.id=ps.studentID WHERE ps.parentID=?1", nativeQuery = true)
    public List<Student> findStudentsByParentId(int id);

    @Query(value = "SELECT * FROM student s JOIN studentClass sc ON s.id=sc.studentID WHERE sc.classID=?1", nativeQuery = true)
    public List<Student> findStudentsByClassId(int id);
}
