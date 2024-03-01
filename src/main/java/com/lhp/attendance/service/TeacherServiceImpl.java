package com.lhp.attendance.service;

import com.lhp.attendance.dao.TeacherRepository;
import com.lhp.attendance.entity.Class;
import com.lhp.attendance.entity.Teacher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherServiceImpl(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public List<Teacher> findAll() {
        return StreamSupport.stream(teacherRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Teacher findById(int id) {
        Optional<Teacher> result = teacherRepository.findById(id);
        Teacher teacher = null;
        if(result.isPresent()) {
            teacher = result.get();
        }
        return teacher;
    }

    @Override
    public Teacher save(Teacher teacher) {
        //Capitalize first letters
        teacher.setFirstName(StringUtils.capitalize(teacher.getFirstName()));
        teacher.setLastName(StringUtils.capitalize(teacher.getLastName()));
        return teacherRepository.save(teacher);
    }

    @Override
    public void deleteById(int id) {
        teacherRepository.deleteById(id);
    }

    @Override
    public List<Class> findClassesByTeacherId(int id) {
        return teacherRepository.findClassesByTeacherId(id);
    }
}
