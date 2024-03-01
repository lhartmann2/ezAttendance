package com.lhp.attendance.config;

import com.lhp.attendance.entity.User;
import com.lhp.attendance.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserService userService;

    public CustomLoginFailureHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        User user = userService.findByUserName(username);

        if(user != null) {
            if(user.isEnabled() && user.isAccountNonLocked()) {
                if(user.getFailedAttempt() < UserService.MAX_FAILED_ATTEMPTS - 1) {
                    userService.increaseFailedAttempts(user);
                } else {
                    userService.lockUser(user);
                    exception = new LockedException("Your account has been locked due to "+ UserService.MAX_FAILED_ATTEMPTS +
                            " failed attempts. It will be unlocked after 24 hours.");
                }
            } else if(!user.isAccountNonLocked()) {
                if(userService.unlockUser(user)) {
                    exception = new LockedException("Your account has been unlocked. Please login again.");
                } else {
                    exception = new LockedException("Your account is currently locked.");
                }
            }
        }

        super.setDefaultFailureUrl("/loginPage?error");
        super.onAuthenticationFailure(request, response, exception);
    }
}
