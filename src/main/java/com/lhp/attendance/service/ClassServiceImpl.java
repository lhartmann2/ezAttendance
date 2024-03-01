package com.lhp.attendance.service;

import com.lhp.attendance.dao.ClassRepository;
import com.lhp.attendance.entity.Class;
import com.lhp.attendance.entity.Student;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;

    public ClassServiceImpl(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    @Override
    public List<Class> findAll() {
        return StreamSupport.stream(classRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Class findById(int id) {
        Optional<Class> result = classRepository.findById(id);
        Class theClass = null;
        if(result.isPresent()) {
            theClass = result.get();
        }
        return theClass;
    }

    @Override
    public Class save(Class theClass) {
        theClass.setName(StringUtils.capitalize(theClass.getName()));
        return classRepository.save(theClass);
    }

    @Override
    public void deleteById(int id) {
        classRepository.deleteById(id);
    }

    @Override
    public List<Class> findClassesByTeacherId(int id) {
        return classRepository.findClassesByTeacherId(id);
    }

    @Override
    public boolean removeStudentFromClass(Student student, Class theClass) {
        Set<Student> students = theClass.getStudents();
        boolean result = students.remove(student);
        if(result) {
            theClass.setStudents(students);
            classRepository.save(theClass);
        }
        return result;
    }

    @Override
    public List<Class> findClassesByStudentId(int id) {
        return classRepository.findClassesByStudentId(id);
    }
}
