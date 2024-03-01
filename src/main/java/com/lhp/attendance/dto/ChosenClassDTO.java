package com.lhp.attendance.dto;

import com.lhp.attendance.entity.Class;

import java.util.HashSet;
import java.util.Set;

public class ChosenClassDTO {

    private Set<Class> classes;

    public ChosenClassDTO() {
        this.classes = new HashSet<>();
    }

    public ChosenClassDTO(Set<Class> classes) {
        this.classes = classes;
    }

    public Set<Class> getClasses() {
        return classes;
    }

    public void setClasses(Set<Class> classes) {
        this.classes = classes;
    }

    public void addClass(Class theClass) {
        classes.add(theClass);
    }

    public boolean isEmpty() {
        return classes.isEmpty();
    }
}
