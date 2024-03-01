package com.lhp.attendance.service;

import com.lhp.attendance.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserServiceI extends UserDetailsService {

    public User findByUserName(String username);

}
