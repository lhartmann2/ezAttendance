package com.lhp.attendance.controller;

import com.lhp.attendance.entity.AttendanceContainer;
import com.lhp.attendance.entity.Event;
import com.lhp.attendance.entity.Student;
import com.lhp.attendance.service.AttendanceContainerService;
import com.lhp.attendance.service.EventService;
import com.lhp.attendance.service.StudentService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/", "managers"})
public class ManagerController {

    private final AttendanceContainerService attendanceContainerService;
    private final StudentService studentService;
    private final EventService eventService;

    public ManagerController(AttendanceContainerService attendanceContainerService, StudentService studentService,
                             EventService eventService) {
        this.attendanceContainerService = attendanceContainerService;
        this.studentService = studentService;
        this.eventService = eventService;
    }

    @GetMapping({"/", "/index"})
    public String getHomePage(Model model) {

        List<AttendanceContainer> containers = attendanceContainerService.findAll().stream().parallel()
                .filter(c -> DateUtils.isSameDay(c.getDate(), new Date(System.currentTimeMillis())))
                .collect(Collectors.toList());

        boolean noClasses = containers.isEmpty();

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        List<Student> birthdays = studentService.findAll().stream().parallel()
                        .filter(s -> {
                            Calendar birthday = Calendar.getInstance();
                            birthday.setTime(s.getDob());
                            return(now.get(Calendar.MONTH) == birthday.get(Calendar.MONTH));
                        }).collect(Collectors.toList());

        boolean noBirthdays = birthdays.isEmpty();

        List<Event> events = eventService.findAll().stream().parallel()
                        .sorted(Comparator.comparing(Event::getDate)).collect(Collectors.toList());

        boolean noEvents = events.isEmpty();

        model.addAttribute("todayContainers", containers);
        model.addAttribute("noClasses", noClasses);
        model.addAttribute("birthdays", birthdays);
        model.addAttribute("noBirthdays", noBirthdays);
        model.addAttribute("events", events);
        model.addAttribute("noEvents", noEvents);

        return "managers/index";
    }

    @GetMapping("/help")
    public String getHelp() {
        return "managers/help";
    }
}
