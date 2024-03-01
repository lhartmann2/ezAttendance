package com.lhp.attendance.service;

import com.lhp.attendance.entity.Class;
import com.lhp.attendance.entity.Teacher;

import java.util.List;

public interface TeacherService {

    public List<Teacher> findAll();

    public Teacher findById(int id);

    public Teacher save(Teacher teacher);

    public void deleteById(int id);

    public List<Class> findClassesByTeacherId(int id);
}
