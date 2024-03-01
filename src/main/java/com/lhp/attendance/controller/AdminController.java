package com.lhp.attendance.controller;

import com.lhp.attendance.dao.RoleRepository;
import com.lhp.attendance.dto.UserDTO;
import com.lhp.attendance.entity.*;
import com.lhp.attendance.entity.Class;
import com.lhp.attendance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("admins")
public class AdminController {

    private final String VERSION = "1.5";

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final EventService eventService;

    private final ParentService parentService;
    private final TeacherService teacherService;
    private final StudentService studentService;

    private final ClassService classService;

    @Autowired
    private FileExporter fileExporter;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminController(UserService userService, RoleRepository roleRepository, EventService eventService,
                           ParentService parentService, TeacherService teacherService, StudentService studentService,
                           ClassService classService) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.eventService = eventService;
        this.parentService = parentService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.classService = classService;
    }

    @GetMapping({"/", "/{success}"})
    public String adminIndex(@PathVariable(value = "success", required = false) Optional<Integer> success,
                             Model model) {

        if((success.isPresent()) && (success.get() > 0)) {
            model.addAttribute("success", true);
            if(success.get() == 1) {
                model.addAttribute("successText", "User account successfully created.");
            } else if(success.get() == 2) {
                model.addAttribute("successText", "Password successfully changed.");
            } else if(success.get() == 3) {
                model.addAttribute("successText", "User account successfully unlocked and login attempts reset.");
            } else if(success.get() == 4) {
                model.addAttribute("successText", "User account successfully deleted.");
            } else if(success.get() == 5) {
                model.addAttribute("successText", "Upcoming Event successfully created.");
            } else if(success.get() == 6) {
                model.addAttribute("successText", "An error occurred processing your request.");
            }
        } else {
            model.addAttribute("success", false);
        }
        model.addAttribute("version", "App Version: "+VERSION);
        return "admins/admins";
    }

    @GetMapping("/createUser")
    public String getCreateUser(Model model) {
        model.addAttribute("user", new UserDTO());
        return "admins/createUser";
    }

    @PostMapping("/createUser")
    public String postCreateUser(@ModelAttribute(value = "user") @Valid UserDTO userDTO,
                                 BindingResult result, Model model) {

        String username = userDTO.getName();
        String pw = userDTO.getPassword();
        String mPw = userDTO.getMatchingPassword();

        //Check if passwords match
        boolean pwError = false;
        if(!(pw.equals(mPw)))
            pwError = true;

        //Pw must be at least 8 chars
        boolean pwLength = false;
        if(pw.length() < 8)
            pwLength = true;

        //Check if user already exists
        boolean userError = false;
        if(doesUserExist(username))
            userError = true;

        //Error page
        if((result.hasErrors()) || (pwError) || (userError) || (pwLength)) {
            model.addAttribute("user", userDTO);
            model.addAttribute("pwError", pwError);
            model.addAttribute("userError", userError);
            model.addAttribute("pwLength", pwLength);

            return "admins/createUser";
        }

        //Encrypt password
        String encodedPass = passwordEncoder.encode(pw);

        //Get roles
        List<Role> roles = roleRepository.findAll();

        //Set default roles
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(roles.get(0)); //default
        userRoles.add(roles.get(1)); //manager

        //Make admin if box is checked
        if(userDTO.isAdmin()) {
            userRoles.add(roles.get(2));
        }

        //Create user obj
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPass);
        user.setEnabled(true);
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        user.setRoles(userRoles);

        userService.save(user);

        return "redirect:/admins/1";
    }

    @GetMapping("/changePassword")
    public String getChangePassword(Model model) {

        List<User> users = userService.findAll();

        model.addAttribute("user", new UserDTO());
        model.addAttribute("userList", users);
        return "admins/changePassword";
    }

    @PostMapping("/changePassword")
    public String postChangePassword(@ModelAttribute(value = "user") @Valid UserDTO userDTO,
                                     BindingResult result, Model model) {

        String username = userDTO.getName();
        String pw = userDTO.getPassword();
        String mPw = userDTO.getMatchingPassword();

        //Check if passwords match
        boolean pwError = !(pw.equals(mPw));

        //Pw must be at least 8 chars
        boolean pwLength = pw.length() < 8;

        //Check if user exists
        boolean userError = !doesUserExist(username);

        //Error page
        if((result.hasErrors()) || (pwError) || (userError) || (pwLength)) {
            model.addAttribute("userList", userService.findAll());
            model.addAttribute("user", userDTO);
            model.addAttribute("pwError", pwError);
            model.addAttribute("userError", userError);
            model.addAttribute("pwLength", pwLength);

            return "admins/changePassword";
        }

        User user = userService.findByUserName(username);

        //Sanity check
        if(user == null) {
            throw new UsernameNotFoundException("This shouldn't happen.");
        }

        String encodedPass = passwordEncoder.encode(pw);

        user.setPassword(encodedPass);

        userService.save(user);

        return "redirect:/admins/2";
    }

    @GetMapping("/deleteUser")
    public String getDeleteUser(Authentication authentication, Model model) {

        List<User> users = userService.findAll();

        //Remove this user from list
        String me = authentication.getName();
        for(User u : users) {
            if(u.getUsername().equals(me)) {
                users.remove(u);
                break;
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("a");
        userDTO.setPassword("a");

        model.addAttribute("user", userDTO);
        model.addAttribute("userList", users);

        return "admins/deleteUser";
    }

    @PostMapping("/deleteUser")
    public String postDeleteUser(@ModelAttribute(value = "user") @Valid UserDTO userDTO,
                                 Authentication authentication,
                                 BindingResult result, Model model) {
        String username = userDTO.getName();
        User user = userService.findByUserName(username);
        String me = authentication.getName();

        boolean userError = false;
        boolean isMe = false;

        if(user == null)
            userError = true;
        if(!doesUserExist(username))
            userError=true;
        if(username.equals(me)) //This shouldn't happen
            isMe=true;

        if((result.hasErrors()) || (userError) || (isMe)) {
            List<User> users = userService.findAll();

            //Remove this user from list
            for(User u : users) {
                if(u.getUsername().equals(me)) {
                    users.remove(u);
                    break;
                }
            }

            model.addAttribute("userList", users);
            model.addAttribute("userError", userError);
            model.addAttribute("isMe", isMe);
            model.addAttribute("user", userDTO);

            return "admins/deleteUser";
        }

        //Delete roles first
        user.setRoles(new HashSet<>());
        userService.save(user);

        userService.deleteById(user.getId());

        return "redirect:/admins/4";
    }

    @GetMapping("/unlockAccount")
    public String getUnlockAccount(Authentication authentication, Model model) {

        List<User> users = userService.findAll();

        //Remove this user from list
        String me = authentication.getName();
        for(User u : users) {
            if(u.getUsername().equals(me)) {
                users.remove(u);
                break;
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("a");
        userDTO.setPassword("a");

        model.addAttribute("user", userDTO);
        model.addAttribute("userList", users);
        return "admins/unlockAccount";
    }

    @PostMapping("/unlockAccount")
    public String postUnlockAccount(@ModelAttribute(value = "user") @Valid UserDTO userDTO,
                                    Authentication authentication,
                                    BindingResult result, Model model) {
        String username = userDTO.getName();
        User user = userService.findByUserName(username);

        boolean userError = false;
        if(user == null) {
            userError = true;
        }
        if(!doesUserExist(username))
            userError=true;

        if((result.hasErrors()) || (userError)) {
            List<User> users = userService.findAll();

            //Remove this user from list
            String me = authentication.getName();
            for(User u : users) {
                if(u.getUsername().equals(me)) {
                    users.remove(u);
                    break;
                }
            }

            model.addAttribute("user", userDTO);
            model.addAttribute("userList", users);
            model.addAttribute("userError", true);
            return "admins/unlockAccount";
        }

        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        userService.save(user);

        return "redirect:/admins/3";
    }

    @GetMapping("/createEvent")
    public String getCreateEvent(Model model) {
        model.addAttribute("event", new Event());
        return "admins/createEvent";
    }

    @PostMapping("/createEvent")
    public String postCreateEvent(@ModelAttribute(value = "event") @Valid Event event,
                                  BindingResult result, Model model) {

        boolean dateError = false;
        if(event.getDate().before(new Date()))
            dateError = true;

        if((result.hasErrors()) || (dateError)) {
            model.addAttribute("dateError", dateError);
            model.addAttribute("event", event);
            return "admins/createEvent";
        }

        eventService.save(event);

        return "redirect:/admins/events/3";
    }

    @GetMapping({"/events", "/events/{success}"})
    public String getEvents(@PathVariable(value = "success", required = false) Optional<Integer> success,
                            Model model) {
        List<Event> events = eventService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Event::getDate)).collect(Collectors.toList());

        model.addAttribute("events", events);
        model.addAttribute("none", events.isEmpty());
        if((success.isPresent()) && (success.get() > 0)) {
            model.addAttribute("success", true);
            if(success.get() == 1) {
                model.addAttribute("successText", "Event updated.");
            } else if(success.get() == 2) {
                model.addAttribute("successText", "Event deleted.");
            } else if(success.get() == 3) {
                model.addAttribute("successText", "Event created.");
            }
        } else {
            model.addAttribute("success", false);
        }
        return "admins/events";
    }

    @GetMapping("/editEvent/{id}")
    public String getEditEvent(@PathVariable(value = "id") int id, Model model) {
        Event event = eventService.findById(id);
        if(event == null)
            return "redirect:/admins/";
        model.addAttribute("event", event);
        return "admins/editEvent";
    }

    @PostMapping("/editEvent")
    public String postEditEvent(@ModelAttribute(value = "event") @Valid Event event,
                                  BindingResult result, Model model) {

        boolean dateError = false;
        if(event.getDate().before(new Date()))
            dateError = true;

        if((result.hasErrors()) || (dateError)) {
            model.addAttribute("dateError", dateError);
            model.addAttribute("event", event);
            return "admins/editEvent";
        }

        eventService.save(event);

        return "redirect:/admins/events/1";
    }

    @GetMapping("/deleteEvent/{id}")
    public String getDeleteEvent(@PathVariable(value = "id") int id, Model model) {
        Event event = eventService.findById(id);
        if(event == null) {
            return "redirect:/admins/";
        }
        model.addAttribute("event", event);
        return "admins/deleteEvent";
    }

    @PostMapping("/deleteEvent")
    public String postDeleteEvent(@ModelAttribute(value = "event") Event event) {
        eventService.deleteById(event.getId());
        return "redirect:/admins/events/2";
    }

    @GetMapping("/support")
    public String getSupport(Model model) {
        return "admins/support";
    }

    @GetMapping("/contacts")
    public String getContacts(Model model) {
        return "admins/contacts";
    }

    @GetMapping({"/parentContacts/{sort}", "/parentContacts/{sort}/{class}"})
    public String getParentContacts(@PathVariable(value = "sort") int sort,
                                    @PathVariable(value = "class", required = false) Optional<Integer> classId, Model model) {

        if(sort > 1) {
            return "redirect:/admins/parentContacts/0";
        }

        List<Parent> parentList;
        boolean classPresent = false;
        if(classId.isPresent()) {
            if(classId.get() > -1) {
                classPresent = true;
                Class theClass = classService.findById(classId.get());
                if(theClass == null) {
                    return "redirect:/admins/parentContacts/0/-1";
                }
            }
        }

        if(classPresent) {
            parentList = new ArrayList<>();
            List<Student> studentList = studentService.findStudentsByClassId(classId.get());
            for(Student s : studentList) {
                List<Parent> sParents = parentService.findParentsByStudentId(s.getId());
                for(Parent p : sParents) {
                    if(!parentList.contains(p)) {
                        parentList.add(p);
                    }
                }
            }
        } else {
            parentList = parentService.findAll();
        }

        parentList.sort(Comparator.comparing((sort < 1 ? Parent::getFirstName : Parent::getLastName)));
        boolean none = parentList.isEmpty();

        model.addAttribute("list", parentList);
        model.addAttribute("none", none);
        model.addAttribute("selectType", sort);
        model.addAttribute("classSelected", classPresent ? classId.get() : -1);
        model.addAttribute("headerText", classPresent ? classService.findById(classId.get()).getName() : "All");
        model.addAttribute("classList", classService.findAll());

        return "admins/parentContacts";
    }

    @GetMapping("/teacherContacts/{sort}")
    public String getTeacherContacts(@PathVariable(value = "sort") int sort, Model model) {
        List<Teacher> teacherList = teacherService.findAll();
        teacherList.sort(Comparator.comparing((sort < 1 ? Teacher::getFirstName : Teacher::getLastName)));
        boolean none = teacherList.isEmpty();

        model.addAttribute("list", teacherList);
        model.addAttribute("none", none);
        model.addAttribute("selectType", sort);

        return "admins/teacherContacts";
    }

    @GetMapping("/studentContacts/{sort}")
    public String getStudentContacts(@PathVariable(value = "sort") int sort, Model model) {
        List<Student> studentList = studentService.findAll();
        Comparator<Student> comparator = Comparator.comparing(Student::getFirstName).thenComparing(Student::getNickName);
        studentList.sort((sort < 1 ? comparator : Comparator.comparing(Student::getLastName)));
        boolean none = studentList.isEmpty();

        model.addAttribute("list", studentList);
        model.addAttribute("none", none);
        model.addAttribute("selectType", sort);

        return "admins/studentContacts";
    }

    @PostMapping({"/exportText/{type}/{sort}", "/exportText/{type}/{sort}/{classId}"})
    public ResponseEntity<InputStreamResource> exportText(@PathVariable(value = "type") int type,
                                                          @PathVariable(value = "sort") int sort,
                                                          @PathVariable(value = "classId", required = false) Optional<Integer> cId) throws FileNotFoundException {

        boolean classPresent = false;
        Class theClass = null;
        if(type == 1) {
            if (cId.isPresent()) {
                if (cId.get() > -1) {
                    theClass = classService.findById(cId.get());
                    if (theClass == null) {
                        throw new FileNotFoundException("Class not found.");
                    }
                    classPresent = true;
                }
            }
        }

        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String today = df.format(new Date(System.currentTimeMillis()));
        String filename;
        String content;

        if(type == 1) { //Parents
            List<Parent> parentList;
            String classString = "";

            if(classPresent) {
                parentList = new ArrayList<>();
                List<Student> studentList = studentService.findStudentsByClassId(cId.get());
                for(Student s : studentList) {
                    List<Parent> sParents = parentService.findParentsByStudentId(s.getId());
                    for(Parent p : sParents) {
                        if(!parentList.contains(p)) {
                            parentList.add(p);
                        }
                    }
                }
                classString = " - "+theClass.getName();
            } else {
                parentList = parentService.findAll();
            }

            parentList.sort(Comparator.comparing((sort < 1 ? Parent::getFirstName : Parent::getLastName)));
            filename = "Parent Contact List"+classString+" - "+today+".txt";

            StringBuilder builder = new StringBuilder("Parent Contact List\nGenerated: "+today+
                    (classPresent ? "\nClass: "+theClass.getName() : "") + "\n\n");
            if(parentList.isEmpty()) {
                builder.append("No Parents Found");
            } else {
                for(Parent p : parentList) {
                    builder.append("-").append(p.getFirstName()).append(" ").append(p.getLastName()).append("\n\tPhone: ")
                            .append(p.getPhone()).append("\n\tEmail: ").append(p.getEmail()).append("\n");
                }
            }
            content = builder.toString();
        } else if(type == 2) { //Teachers
            List<Teacher> teacherList = teacherService.findAll();
            teacherList.sort(Comparator.comparing((sort < 1 ? Teacher::getFirstName : Teacher::getLastName)));
            filename = "Teacher Contact List - "+today+".txt";

            StringBuilder builder = new StringBuilder("Teacher Contact List\nGenerated: "+today+"\n\n");
            if(teacherList.isEmpty()) {
                builder.append("No Teachers Found");
            } else {
                for(Teacher t : teacherList) {
                    builder.append("-").append(t.getFirstName()).append(" ").append(t.getLastName()).append("\n\tPhone: ")
                            .append(t.getPhone()).append("\n\tEmail: ").append(t.getEmail()).append("\n");
                }
            }
            content = builder.toString();
        } else if(type == 3) { //Students
            List<Student> studentList = studentService.findAll();
            Comparator<Student> comparator = Comparator.comparing(Student::getFirstName)
                    .thenComparing(Student::getNickName);
            studentList.sort((sort < 1 ? comparator : Comparator.comparing(Student::getLastName)));
            filename = "Student Contact List - "+today+".txt";

            StringBuilder builder = new StringBuilder("Student Contact List\nGenerated: "+today+"\n\n");
            if(studentList.isEmpty()) {
                builder.append("No Students Found");
            } else {
                for(Student s : studentList) {
                    String name = s.getFirstName();
                    if(!s.getNickName().isEmpty())
                        name = s.getNickName();
                    builder.append("-").append(name).append(" ").append(s.getLastName()).append("\n\tPhone: ")
                            .append(s.getPhone()).append("\n");
                }
            }
            content = builder.toString();
        } else { //Error
            throw new FileNotFoundException("Invalid export type.");
        }

        Path exportedPath = fileExporter.export(content, filename);

        File exportedFile = exportedPath.toFile();
        FileInputStream fileInputStream = new FileInputStream(exportedFile);
        InputStreamResource inputStreamResource = new InputStreamResource(fileInputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(exportedFile.length())
                .body(inputStreamResource);
    }

    @PostMapping({"/exportCSV/{type}/{sort}", "/exportCSV/{type}/{sort}/{classId}"})
    public ResponseEntity<InputStreamResource> exportCSV(@PathVariable(value = "type") int type,
                                                         @PathVariable(value = "sort") int sort,
                                                         @PathVariable(value = "classId", required = false) Optional<Integer> cId) throws Exception {
        boolean classPresent = false;
        Class theClass;
        String passedClassName = "";
        if(type == 1) {
            if (cId.isPresent()) {
                if (cId.get() > -1) {
                    theClass = classService.findById(cId.get());
                    if (theClass == null) {
                        throw new FileNotFoundException("Class not found.");
                    }
                    classPresent = true;
                    passedClassName = theClass.getName();
                }
            }
        }

        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String today = df.format(new Date(System.currentTimeMillis()));
        String filename;
        List<Object> exportList = new ArrayList<>();
        switch(type) {
            case 1 -> {
                if(classPresent) {
                    filename = "Parent Contact List - " +passedClassName+ " - "+today+".csv";
                    List<Parent> parentList = new ArrayList<>();
                    List<Student> studentList = studentService.findStudentsByClassId(cId.get());
                    for(Student s : studentList) {
                        List<Parent> sParents = parentService.findParentsByStudentId(s.getId());
                        for(Parent p : sParents) {
                            if(!parentList.contains(p)) {
                                parentList.add(p);
                            }
                        }
                    }
                    parentList.sort((sort < 1 ? Comparator.comparing(Parent::getFirstName) :
                            Comparator.comparing(Parent::getLastName)));
                    exportList.addAll(parentList);
                } else {
                    filename = "Parent Contact List - " + today + ".csv";
                    exportList = parentService.findAll().stream().parallel()
                            .sorted((sort < 1 ? Comparator.comparing(Parent::getFirstName) :
                                    Comparator.comparing(Parent::getLastName))).collect(Collectors.toList());
                }
            }
            case 2 -> {
                filename = "Teacher Contact List - "+today+".csv";
                exportList = teacherService.findAll().stream().parallel()
                        .sorted((sort < 1 ? Comparator.comparing(Teacher::getFirstName) :
                                Comparator.comparing(Teacher::getLastName))).collect(Collectors.toList());
            }
            case 3 -> {
                filename = "Student Contact List - "+today+".csv";
                Comparator<Student> comparator = Comparator.comparing(Student::getFirstName)
                        .thenComparing(Student::getNickName);
                exportList = studentService.findAll().stream().parallel()
                        .sorted((sort < 1 ? comparator : Comparator.comparing(Student::getLastName)))
                        .collect(Collectors.toList());
            }
            default -> throw new RuntimeException("Invalid export type.");
        }

        ByteArrayInputStream in = CSVHelper.contactsToCSV(type, exportList, passedClassName);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    private boolean doesUserExist(String userName) {
        return (userService.findByUserName(userName) != null);
    }

}
