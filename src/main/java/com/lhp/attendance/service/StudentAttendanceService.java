package com.lhp.attendance.service;

import com.lhp.attendance.entity.StudentAttendance;

import java.util.List;

public interface StudentAttendanceService {

    List<StudentAttendance> findAll();

    public StudentAttendance findById(int id);

    public StudentAttendance save(StudentAttendance studentAttendance);

    public void deleteById(int id);

    public List<StudentAttendance> getSAByStudentId(int studentId);

    public void deleteByClassId(int classId);

    public List<StudentAttendance> getSAByContainerId(int containerId);
}
