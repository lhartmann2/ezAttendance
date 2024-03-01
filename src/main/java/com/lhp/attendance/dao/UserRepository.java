package com.lhp.attendance.dao;

import com.lhp.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "UPDATE users u SET u.enabled = ?2 WHERE u.id = ?1", nativeQuery = true)
    @Modifying
    public void updateEnabledStatus(@Param("id") Integer id, @Param("enabled") boolean enabled);

    @Query(value = "UPDATE users u SET u.failed_attempt = ?1 WHERE u.username = ?2", nativeQuery = true)
    @Modifying
    public void updateFailedAttempt(int failedAttempt, String username);

    @Query(value = "SELECT * FROM users u WHERE u.username = ?1", nativeQuery = true)
    public User findByUserName(String userName);
}
