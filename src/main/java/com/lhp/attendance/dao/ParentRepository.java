package com.lhp.attendance.dao;

import com.lhp.attendance.entity.Parent;
import com.lhp.attendance.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentRepository extends CrudRepository<Parent, Integer> {

    @Query(value = "SELECT * FROM student s JOIN parentStudent ps ON s.id=ps.studentID WHERE ps.parentID = ?1", nativeQuery = true)
    public List<Student> getStudentsByParentId(int id);

    @Query(value = "SELECT * FROM parent p JOIN parentStudent ps ON p.id=ps.parentID WHERE ps.studentID=?1", nativeQuery = true)
    public List<Parent> findParentsByStudentId(int id);

    @Query(value = "INSERT INTO parentStudent VALUES(?1, ?2)", nativeQuery = true)
    public void addStudentToParent(int studentId, int parentId);
}
