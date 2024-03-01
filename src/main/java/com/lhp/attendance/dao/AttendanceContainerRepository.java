package com.lhp.attendance.dao;

import com.lhp.attendance.entity.AttendanceContainer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceContainerRepository extends CrudRepository<AttendanceContainer, Integer> {

    @Query(value = "SELECT * FROM attendanceContainer a WHERE classID = ?1", nativeQuery = true)
    public List<AttendanceContainer> getContainersByClassId(int id);

}
