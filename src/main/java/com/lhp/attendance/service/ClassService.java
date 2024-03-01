package com.lhp.attendance.service;

import com.lhp.attendance.entity.Class;
import com.lhp.attendance.entity.Student;

import java.util.List;

public interface ClassService {

    public List<Class> findAll();

    public Class findById(int id);

    public Class save(Class theClass);

    public void deleteById(int id);

    public List<Class> findClassesByTeacherId(int id);

    public boolean removeStudentFromClass(Student student, Class theClass);

    public List<Class> findClassesByStudentId(int id);
}
