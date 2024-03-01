package com.lhp.attendance.entity;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "attendanceContainer")
public class AttendanceContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "classID", nullable = false)
    private Class theClass;

    @Column(name = "saDate")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @Column(name = "taken")
    private Boolean taken;

    public AttendanceContainer() {
        taken = false;
    }

    public AttendanceContainer(int id, Class theClass, Date date) {
        this.id = id;
        this.theClass = theClass;
        this.date = date;
        taken = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Boolean getTaken() {
        return taken;
    }

    public void setTaken(Boolean taken) {
        this.taken = taken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceContainer container = (AttendanceContainer) o;
        return id == container.id && theClass.equals(container.theClass) && date.equals(container.date) && taken.equals(container.taken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, theClass, date, taken);
    }
}
