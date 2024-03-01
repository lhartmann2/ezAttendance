package com.lhp.attendance.service;

import com.lhp.attendance.dao.StudentRepository;
import com.lhp.attendance.entity.Student;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> findAll() {
        return StreamSupport.stream(studentRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Student findById(int id) {
        Optional<Student> result = studentRepository.findById(id);
        Student student = null;
        if(result.isPresent()) {
            student = result.get();
        }
        return student;
    }

    @Override
    public Student save(Student student) {
        student.setFirstName(StringUtils.capitalize(student.getFirstName()));
        student.setLastName(StringUtils.capitalize(student.getLastName()));
        student.setNickName(StringUtils.capitalize(student.getNickName()));
        return studentRepository.save(student);
    }

    @Override
    public void deleteById(int id) {
        studentRepository.deleteById(id);
    }

    @Override
    public List<Student> findStudentsByParentId(int id) {
        return studentRepository.findStudentsByParentId(id);
    }

    @Override
    public List<Student> findStudentsByClassId(int id) {
        return studentRepository.findStudentsByClassId(id);
    }
}
