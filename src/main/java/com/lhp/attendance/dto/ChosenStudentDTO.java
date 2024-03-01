package com.lhp.attendance.dto;

import com.lhp.attendance.entity.Student;

import java.util.HashSet;
import java.util.Set;

public class ChosenStudentDTO {

    private Set<Student> students;

    public ChosenStudentDTO() {
        students = new HashSet<>();
    }

    public ChosenStudentDTO(Set<Student> students) {
        this.students = students;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public boolean isEmpty() {
        return students.isEmpty();
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }
}
