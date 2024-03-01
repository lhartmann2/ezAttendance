package com.lhp.attendance.dto;

import com.lhp.attendance.entity.StudentAttendance;

import java.util.List;

public class StudentAttendanceDTO {

    private List<StudentAttendance> attendances;

    public StudentAttendanceDTO(List<StudentAttendance> attendances) {
        this.attendances = attendances;
    }

    public StudentAttendanceDTO() {
    }

    public void addAttendance(StudentAttendance attendance) {
        this.attendances.add(attendance);
    }

    public List<StudentAttendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<StudentAttendance> attendances) {
        this.attendances = attendances;
    }
}
