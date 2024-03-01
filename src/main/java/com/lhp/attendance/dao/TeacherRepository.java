package com.lhp.attendance.dao;

import com.lhp.attendance.entity.Class;
import com.lhp.attendance.entity.Teacher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TeacherRepository extends CrudRepository<Teacher, Integer> {

    @Query(value = "SELECT * FROM class c WHERE c.teacherID=?1", nativeQuery = true)
    public List<Class> findClassesByTeacherId(int id);
}
