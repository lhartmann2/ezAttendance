package com.lhp.attendance;

import com.lhp.attendance.dao.UserRepository;
import com.lhp.attendance.entity.Role;
import com.lhp.attendance.entity.User;
import com.lhp.attendance.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

@SpringBootApplication
public class AttendanceApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceApplication.class);

    private final UserRepository userRepository;

    public AttendanceApplication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        ////////////////
        //App pre-checks
        ////////////////

        boolean checkFail = false;
        //Warn if no users exist
        if(userRepository.count() < 1) {
            logger.error("\nERROR: No users found in database! Manually insert at least one admin user and restart the application.");
            checkFail = true;
        }

        //Warn if no admins exist
        boolean adminFound = false;
        List<User> userList = userRepository.findAll();
        for(User u : userList) {
            if(adminFound)
                break;
            Set<Role> roles = u.getRoles();
            for(Role r : roles) {
                if (Objects.equals(r.getName(), "ROLE_ADMIN")) {
                    adminFound = true;
                    break;
                }
            }
        }
        if(!adminFound) {
            logger.error("\nERROR: No users have admin authority! Manually add an admin user and restart the application.");
            checkFail = true;
        }

        if(checkFail)
            System.exit(1);
    }
}
