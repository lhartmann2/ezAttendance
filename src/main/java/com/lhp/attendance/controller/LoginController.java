package com.lhp.attendance.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "access-denied";
    }

    @GetMapping({"/loginPage", "/loginPage?error"})
    public String showLoginPage() {
        return "loginPage";
    }


    @GetMapping("/logout")
    public String doLogout(HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpSession session = request.getSession(false);
        SecurityContextHolder.clearContext();;
        session = request.getSession(false);
        if(session != null) {
            session.invalidate();;
        }
        for(Cookie cookie : request.getCookies()) {
            cookie.setMaxAge(0);
        }

        //model.addAttribute("logout", true);

        return "logoutPage";
    }

}
