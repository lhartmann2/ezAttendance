package com.lhp.attendance.config;

import com.lhp.attendance.entity.User;
import com.lhp.attendance.service.UserService;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    public CustomLoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        User user = userService.findByUserName(username);

        if((!user.isAccountNonLocked()) || (!user.isEnabled())) {
            throw new LockedException("Account is currently locked.");
        }

        if(user.getFailedAttempt() > 0) {
            userService.resetFailedAttempts(user.getUsername());
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        response.sendRedirect(request.getContextPath() + "managers/");
    }


}
