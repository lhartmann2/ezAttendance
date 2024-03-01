package com.lhp.attendance.service;

import com.lhp.attendance.dao.UserRepository;
import com.lhp.attendance.entity.Role;
import com.lhp.attendance.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserServiceI {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 86400000; //24 hours

    private final UserRepository userRepository;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll(Sort.by("username").ascending());
    }

    public User save(User user) {
        boolean updatingExisting = user.getId() != null;
        if(updatingExisting) {
            User existing = userRepository.findById(user.getId()).get();

            if(user.getPassword().isEmpty()) {
                user.setPassword(existing.getPassword());
            } else {
                encoder.encode(user.getPassword());
            }
        } else {
            encoder.encode(user.getPassword());
        }

        return userRepository.save(user);
    }

    public User updateAccount(User user) {
        User existing = userRepository.findById(user.getId()).get();

        if(!user.getPassword().isEmpty()) {
            existing.setPassword(encoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public User findById(Integer id) {
        Optional<User> result = userRepository.findById(id);
        User user = null;
        if(result.isPresent()) {
            user = result.get();
        }
        return user;
    }

    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    public void updateUserEnabledStatus(Integer id, boolean enabled) {
        userRepository.updateEnabledStatus(id, enabled);
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public void increaseFailedAttempts(User user) {
        int attempts = user.getFailedAttempt();
        userRepository.updateFailedAttempt(++attempts, user.getUsername());
    }

    public void lockUser(User user) {
        user.setAccountNonLocked(false);
        user.setEnabled(false);
        user.setLockTime(new Date(System.currentTimeMillis()));

        userRepository.save(user);
    }

    public boolean unlockUser(User user) {
        long lockTime = user.getLockTime().getTime();
        long currentTime = System.currentTimeMillis();

        if((lockTime + LOCK_TIME_DURATION) < currentTime) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setEnabled(true);
            user.setFailedAttempt(0);

            userRepository.save(user);

            return true;
        }

        return false;
    }


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);
        if(user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    public void resetFailedAttempts(String username) {
        userRepository.updateFailedAttempt(0, username);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }
}
