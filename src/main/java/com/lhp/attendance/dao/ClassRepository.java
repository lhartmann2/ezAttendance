package com.lhp.attendance.dao;

import com.lhp.attendance.entity.Class;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends CrudRepository<Class, Integer> {

    @Query(value = "SELECT * FROM class c WHERE c.teacherID=?1", nativeQuery = true)
    public List<Class> findClassesByTeacherId(int id);

    @Query(value = "SELECT * FROM class c JOIN studentClass sc ON c.id=sc.classID WHERE sc.studentID=?1", nativeQuery = true)
    public List<Class> findClassesByStudentId(int id);

}
