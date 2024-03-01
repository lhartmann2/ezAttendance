package com.lhp.attendance.service;

import com.lhp.attendance.dao.StudentAttendanceRepository;
import com.lhp.attendance.entity.StudentAttendance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class StudentAttendanceServiceImpl implements StudentAttendanceService {

    private final StudentAttendanceRepository studentAttendanceRepository;

    public StudentAttendanceServiceImpl(StudentAttendanceRepository studentAttendanceRepository) {
        this.studentAttendanceRepository = studentAttendanceRepository;
    }

    @Override
    public List<StudentAttendance> findAll() {
        return StreamSupport.stream(studentAttendanceRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public StudentAttendance findById(int id) {
        Optional<StudentAttendance> result = studentAttendanceRepository.findById(id);
        StudentAttendance sa = null;
        if(result.isPresent()) {
            sa = result.get();
        }
        return sa;
    }

    @Override
    public StudentAttendance save(StudentAttendance studentAttendance) {
        return studentAttendanceRepository.save(studentAttendance);
    }

    @Override
    public void deleteById(int id) {
        studentAttendanceRepository.deleteById(id);
    }

    @Override
    public List<StudentAttendance> getSAByStudentId(int studentId) {
        return studentAttendanceRepository.getSAByStudentId(studentId);
    }

    @Override
    public void deleteByClassId(int classId) {
        List<StudentAttendance> list = studentAttendanceRepository.deleteSAByClassId(classId);
        for(StudentAttendance s : list) {
            deleteById(s.getId());
        }
    }

    @Override
    public List<StudentAttendance> getSAByContainerId(int containerId) {
        return studentAttendanceRepository.getSAByContainerId(containerId);
    }
}
