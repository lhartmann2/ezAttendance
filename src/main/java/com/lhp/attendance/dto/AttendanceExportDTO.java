package com.lhp.attendance.dto;

import com.lhp.attendance.entity.Class;
import com.lhp.attendance.entity.StudentAttendance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AttendanceExportDTO {

    private List<StudentAttendance> attendances;

    private Class theClass;

    private Date date;

    public AttendanceExportDTO() {
        attendances = new ArrayList<>();
    }

    public List<StudentAttendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<StudentAttendance> attendances) {
        this.attendances = attendances;
    }

    public void addAttendance(StudentAttendance attendance) {
        this.attendances.add(attendance);
    }

    public Class getTheClass() {
        return theClass;
    }

    public void setTheClass(Class theClass) {
        this.theClass = theClass;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
