package com.lhp.attendance.controller;

import com.lhp.attendance.dao.RoleRepository;
import com.lhp.attendance.dto.UserDTO;
import com.lhp.attendance.entity.Role;
import com.lhp.attendance.entity.User;
import com.lhp.attendance.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("managers/account")
public class AccountController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AccountController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping({"/", "/{success}"})
    public String adminIndex(@PathVariable(value = "success", required = false) Optional<Integer> success,
                             Model model) {

        if((success.isPresent()) && (success.get() > 0)) {
            model.addAttribute("success", true);
            model.addAttribute("warning", false);
            if(success.get() == 1) {
                model.addAttribute("successText", "Password successfully updated.");
            } else if(success.get() == 2) {
                model.addAttribute("warning", true);
                model.addAttribute("successText", "User not found.");
            } else if(success.get() == 3) {
                model.addAttribute("warning", true);
                model.addAttribute("successText", "Only admins can delete other admins.");
            }
        } else {
            model.addAttribute("success", false);
        }
        return "managers/account/account";
    }

    @GetMapping("/changePassword")
    public String getChangePassword(Model model) {

        //Get logged in user
        String currentUsername;
        if((currentUsername = getCurrentUser()) == null)
            return "redirect:/account/2";

        UserDTO dto = new UserDTO();
        dto.setName(currentUsername);

        model.addAttribute("user", dto);
        return "managers/account/changePassword";
    }

    @PostMapping("/changePassword")
    public String postChangePassword(@ModelAttribute(value = "user") @Valid UserDTO userDTO,
                                     BindingResult result, Model model) {

        String username = getCurrentUser();
        if(username == null) {
            return "redirect:/managers/account/2";
        }
        String pw = userDTO.getPassword();
        String mPw = userDTO.getMatchingPassword();

        //Check if passwords match
        boolean pwError = !(pw.equals(mPw));

        //Pw must be at least 8 chars
        boolean pwLength = pw.length() < 8;

        //Check if user exists
        boolean userError = !doesUserExist(username);

        //Error page
        if((pwError) || (userError) || (pwLength)) {
            model.addAttribute("user", userDTO);
            model.addAttribute("pwError", pwError);
            model.addAttribute("userError", userError);
            model.addAttribute("pwLength", pwLength);

            return "managers/account/changePassword";
        }

        User user = userService.findByUserName(username);

        //Sanity check
        if(user == null) {
            throw new UsernameNotFoundException("This shouldn't happen.");
        }

        String encodedPass = passwordEncoder.encode(pw);

        user.setPassword(encodedPass);

        userService.save(user);

        return "redirect:/managers/account/1";
    }

    @GetMapping("/deleteAccount")
    public String getDeleteAccount(Model model) {

        String username;
        if((username = getCurrentUser()) == null) {
            return "redirect:/managers/account/2";
        }

        //Only admins can delete admins
        User user = userService.findByUserName(username);
        Set<Role> roles = user.getRoles();
        for(Role r : roles) {
            if(r.getName().equals("ROLE_ADMIN"))
                return "redirect:/managers/account/3";
        }

        return "managers/account/deleteAccount";
    }

    @PostMapping("/deleteAccount")
    public String postDeleteAccount() {
        String username;
        if((username = getCurrentUser()) == null)
            return "redirect:/managers/account/2";

        User user = userService.findByUserName(username);
        if(user == null) {
            throw new UsernameNotFoundException("This shouldn't happen.");
        }

        //Delete roles first
        user.setRoles(new HashSet<>());
        userService.save(user);

        userService.deleteById(user.getId());

        return "redirect:/logout";
    }

    private boolean doesUserExist(String userName) {
        return (userService.findByUserName(userName) != null);
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        } else {
            return null;
        }
    }
}
