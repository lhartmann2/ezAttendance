package com.lhp.attendance.entity;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.util.*;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "firstName")
    @NotEmpty(message = "First name must not be blank.")
    @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters.")
    private String firstName;

    @Column(name = "lastName")
    @NotEmpty(message = "Last name must not be blank.")
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters.")
    private String lastName;

    @Column(name = "nickName")
    @Size(max = 25, message = "Nick name cannot be more than 25 characters.")
    private String nickName;

    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Date must be in the past.")
    private Date dob;

    @Column(name = "phone")
    @Size(max = 10, message = "Must be 10 digit phone number (no dashes or spaces).")
    private String phone;

    @Column(name = "hospital")
    @Size(max = 96, message = "Cannot be greater than 96 characters.")
    private String hospital;

    @Column(name = "allergies")
    @Size(max = 256, message = "Cannot be greater than 256 characters.")
    private String allergies;

    @Column(name = "notes")
    @Size(max = 1024, message = "Cannot be greater than 1024 characters.")
    private String notes;

    public Student() {

    }

    public Student(String firstName, String lastName, String nickName, Date dob, String phone, String allergies, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.dob = dob;
        this.phone = phone;
        this.allergies = allergies;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id && firstName.equals(student.firstName) && lastName.equals(student.lastName) && Objects.equals(nickName, student.nickName) && dob.equals(student.dob) && Objects.equals(phone, student.phone) && Objects.equals(hospital, student.hospital) && Objects.equals(allergies, student.allergies) && Objects.equals(notes, student.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, nickName, dob, phone, hospital, allergies, notes);
    }
}
