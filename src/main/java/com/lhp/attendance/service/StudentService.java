package com.lhp.attendance.service;

import com.lhp.attendance.entity.Student;

import java.util.List;

public interface StudentService {

    public List<Student> findAll();

    public Student findById(int id);

    public Student save(Student student);

    public void deleteById(int id);

    public List<Student> findStudentsByParentId(int id);

    public List<Student> findStudentsByClassId(int id);
}
