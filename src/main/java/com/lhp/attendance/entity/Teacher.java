package com.lhp.attendance.entity;

import com.lhp.attendance.validator.PhoneNumberValidationAnnotationName;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "firstName")
    @NotEmpty(message = "First name is required.")
    @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters.")
    private String firstName;

    @Column(name = "lastName")
    @NotEmpty(message = "Last name is required.")
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters.")
    private String lastName;

    @Column(name = "phone")
    @NotEmpty(message = "Phone number is required.")
    @PhoneNumberValidationAnnotationName(message = "Phone number is required (no dashes or spaces).")
    private String phone;

    @Column(name = "email")
    @Email
    private String email;

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Class> classes = new HashSet<>();

    public Teacher() {}

    public Teacher(String username, String firstName, String lastName, String phone, String email) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Class> getClasses() {
        return classes;
    }

    public void setClasses(Set<Class> classes) {
        this.classes = classes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return id == teacher.id && username.equals(teacher.username) && firstName.equals(teacher.firstName) && lastName.equals(teacher.lastName) && phone.equals(teacher.phone) && Objects.equals(email, teacher.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, firstName, lastName, phone, email);
    }
}
