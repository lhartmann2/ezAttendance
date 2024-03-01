package com.lhp.attendance.dao;

import com.lhp.attendance.entity.StudentAttendance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAttendanceRepository extends CrudRepository<StudentAttendance, Integer> {

    @Query(value = "SELECT * FROM studentAttendance WHERE studentID = ?1", nativeQuery = true)
    public List<StudentAttendance> getSAByStudentId(int studentId);

    @Query(value = "SELECT * FROM studentAttendance WHERE classID = ?1", nativeQuery = true)
    public List<StudentAttendance> deleteSAByClassId(int classId);

    @Query(value = "SELECT * FROM studentAttendance WHERE containerID = ?1", nativeQuery = true)
    public List<StudentAttendance> getSAByContainerId(int containerId);
}
