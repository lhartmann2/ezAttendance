package com.lhp.attendance.service;

import com.lhp.attendance.entity.Parent;
import com.lhp.attendance.entity.Student;

import java.util.List;

public interface ParentService {

    public List<Parent> findAll();

    public Parent findById(int id);

    public Parent save(Parent parent);

    public void deleteById(int id);

    public List<Student> getStudentsByParentId(int id);

    public List<Parent> findParentsByStudentId(int id);
}
