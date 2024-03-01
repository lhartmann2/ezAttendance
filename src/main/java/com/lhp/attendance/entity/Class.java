package com.lhp.attendance.entity;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "class")
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacherID", nullable = false)
    private Teacher teacher;

    @Column(name = "name")
    @NotEmpty(message = "Please enter a class name.")
    @Size(min = 2, max = 50, message = "Class name must be between 2 and 50 characters.")
    private String name;

    @Column(name = "dayOfWeek")
    private String dayOfWeek;

    @Column(name = "startTime")
    @Temporal(TemporalType.TIME)
    @DateTimeFormat(pattern = "HH:mm")
    private Date startTime;

    @Column(name = "endTime")
    @Temporal(TemporalType.TIME)
    @DateTimeFormat(pattern = "HH:mm")
    private Date endTime;

    @Column(name = "startDate")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @Column(name = "endDate")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future
    private Date endDate;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "studentClass",
            joinColumns = { @JoinColumn(name = "classID")},
            inverseJoinColumns = {@JoinColumn(name = "studentID")})
    private Set<Student> students = new HashSet<>();

    @Column(name = "active")
    private boolean active;

    public Class(){}

    public Class(Teacher teacher, String name, String dayOfWeek, Date startTime, Date endTime, Date startDate, Date endDate) {
        this.teacher = teacher;
        this.name = name;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Class aClass = (Class) o;
        return id == aClass.id && active == aClass.active && name.equals(aClass.name) && dayOfWeek.equals(aClass.dayOfWeek) && startTime.equals(aClass.startTime) && endTime.equals(aClass.endTime) && startDate.equals(aClass.startDate) && endDate.equals(aClass.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dayOfWeek, startTime, endTime, startDate, endDate, active);
    }
}
