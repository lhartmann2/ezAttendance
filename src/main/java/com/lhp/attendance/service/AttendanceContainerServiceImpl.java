package com.lhp.attendance.service;

import com.lhp.attendance.dao.AttendanceContainerRepository;
import com.lhp.attendance.entity.AttendanceContainer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AttendanceContainerServiceImpl implements AttendanceContainerService {

    private final AttendanceContainerRepository attendanceContainerRepository;

    public AttendanceContainerServiceImpl(AttendanceContainerRepository attendanceContainerRepository) {
        this.attendanceContainerRepository = attendanceContainerRepository;
    }

    @Override
    public List<AttendanceContainer> findAll() {
        return StreamSupport.stream(attendanceContainerRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public AttendanceContainer findById(int id) {
        Optional<AttendanceContainer> result = attendanceContainerRepository.findById(id);
        AttendanceContainer container = null;
        if(result.isPresent()) {
            container = result.get();
        }
        return container;
    }

    @Override
    public AttendanceContainer save(AttendanceContainer attendanceContainer) {
        return attendanceContainerRepository.save(attendanceContainer);
    }

    @Override
    public void deleteById(int id) {
        attendanceContainerRepository.deleteById(id);
    }

    @Override
    public List<AttendanceContainer> getContainerByClassId(int id) {
        return attendanceContainerRepository.getContainersByClassId(id);
    }
}
