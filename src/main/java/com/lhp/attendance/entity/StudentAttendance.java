package com.lhp.attendance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "studentAttendance")
public class StudentAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "containerID", nullable = false)
    private AttendanceContainer container;

    @Column(name = "present")
    @NotNull
    private Boolean present;

    @Column(name = "reason")
    @Size(max = 255, message = "Cannot be greater than 255 characters.")
    private String reason;

    @Column(name = "excused")
    private boolean excused;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studentID", nullable = false)
    private Student student;


    public StudentAttendance() {
    }

    public StudentAttendance(int id, AttendanceContainer container, Boolean present, String reason, boolean excused, Student student) {
        this.id = id;
        this.container = container;
        this.present = present;
        this.reason = reason;
        this.excused = excused;
        this.student = student;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AttendanceContainer getContainer() {
        return container;
    }

    public void setContainer(AttendanceContainer container) {
        this.container = container;
    }

    public Boolean getPresent() {
        return present;
    }

    public void setPresent(Boolean present) {
        this.present = present;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public boolean isExcused() {
        return excused;
    }

    public void setExcused(boolean excused) {
        this.excused = excused;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentAttendance that = (StudentAttendance) o;
        return id == that.id && excused == that.excused && container.equals(that.container) && present.equals(that.present) && Objects.equals(reason, that.reason) && student.equals(that.student);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, container, present, reason, excused, student);
    }
}
