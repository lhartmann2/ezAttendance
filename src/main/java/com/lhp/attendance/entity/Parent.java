package com.lhp.attendance.entity;

import com.lhp.attendance.validator.PhoneNumberValidationAnnotationName;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "parent")
public class Parent {

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

    @Column(name = "street")
    @Size(max = 50, message = "Street cannot be greater than 50 characters.")
    private String street;

    @Column(name = "city")
    @Size(max = 25, message = "City cannot be greater than 25 characters.")
    private String city;

    @Column(name = "state")
    @Size(max = 2, message = "State must be 2 letter abbreviation.")
    private String state;

    @Column(name = "zip")
    @Size(max = 5, message = "Must be 5 digit zip code.")
    private String zip;

    @Column(name = "phone")
    @NotEmpty(message = "Phone number is required")
    @PhoneNumberValidationAnnotationName(message = "Phone number is required (no dashes or spaces).")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "other")
    @Size(max = 512, message = "Cannot be greater than 512 characters.")
    private String other;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "parentStudent",
    joinColumns = { @JoinColumn(name = "parentID")},
    inverseJoinColumns = {@JoinColumn(name = "studentID")})
    private Set<Student> students = new HashSet<>();

    public Parent() {

    }

    public Parent(String firstName, String lastName, String street, String city, String zip, String phone, String email, String other) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
        this.other = other;
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
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

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parent parent = (Parent) o;
        return id == parent.id && firstName.equals(parent.firstName) && lastName.equals(parent.lastName) && Objects.equals(street, parent.street) && Objects.equals(city, parent.city) && Objects.equals(state, parent.state) && Objects.equals(zip, parent.zip) && phone.equals(parent.phone) && Objects.equals(email, parent.email) && Objects.equals(other, parent.other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, street, city, state, zip, phone, email, other);
    }

    @Override
    public String toString() {
        return "Parent{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", zip='" + zip + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", other='" + other + '\'' +
                '}';
    }
}
