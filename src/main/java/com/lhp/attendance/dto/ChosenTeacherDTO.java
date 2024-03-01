package com.lhp.attendance.dto;

import com.lhp.attendance.entity.Teacher;

import java.util.Set;

public class ChosenTeacherDTO {

    private Set<Teacher> teachers;

    public ChosenTeacherDTO(Set<Teacher> teachers) {
        this.teachers = teachers;
    }

    public Set<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(Set<Teacher> teachers) {
        this.teachers = teachers;
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
    }

    public boolean isEmpty() {
        return teachers.isEmpty();
    }
}
