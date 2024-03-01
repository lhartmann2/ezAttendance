package com.lhp.attendance.service;

import com.lhp.attendance.entity.AttendanceContainer;

import java.util.List;

public interface AttendanceContainerService {

    public List<AttendanceContainer> findAll();

    public AttendanceContainer findById(int id);

    public AttendanceContainer save(AttendanceContainer attendanceContainer);

    public void deleteById(int id);

    public List<AttendanceContainer> getContainerByClassId(int id);
}
