package com.lhp.attendance.controller;

import com.lhp.attendance.entity.AttendanceContainer;
import com.lhp.attendance.entity.Class;
import com.lhp.attendance.entity.StudentAttendance;
import com.lhp.attendance.entity.Teacher;
import com.lhp.attendance.service.AttendanceContainerService;
import com.lhp.attendance.service.ClassService;
import com.lhp.attendance.service.StudentAttendanceService;
import com.lhp.attendance.service.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("managers/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final ClassService classService;
    private final StudentAttendanceService studentAttendanceService;
    private final AttendanceContainerService attendanceContainerService;

    public TeacherController(TeacherService teacherService, ClassService classService, StudentAttendanceService studentAttendanceService, AttendanceContainerService attendanceContainerService) {
        this.teacherService = teacherService;
        this.classService = classService;
        this.studentAttendanceService = studentAttendanceService;
        this.attendanceContainerService = attendanceContainerService;
    }

    @GetMapping({"/", "/{success}/{type}"}) //default / sort by first name
    public String teachersHome(@PathVariable(value = "success", required = false) Optional<Integer> success,
                               @PathVariable(value = "type", required = false) Optional<Integer> type,
                               Model model) {
        List<Teacher> teachers = teacherService.findAll().stream().parallel().sorted(Comparator.comparing(Teacher::getFirstName)).toList();
        boolean warning = false;
        model.addAttribute("teacherList", teachers);
        model.addAttribute("selectType", 0);
        model.addAttribute("none", teachers.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);
        if(type.isPresent()) {
            String text = "";
            if(type.get() == 0)
                text = "Teacher Saved";
            else if(type.get() == 1)
                text = "Teacher Deleted";
            else if(type.get() == 3) {
                warning = true;
                text = "There was an error processing that request: Teacher not found.";
            } else if(type.get() == 4) {
                warning = true;
                text = "There was an error processing that request: Class not found.";
            } else
                text = "???";
            model.addAttribute("warning", warning);
            model.addAttribute("bannerText", text);
        }
        return "managers/teachers/teachers";
    }

    @GetMapping("/sortLast")
    public String teachersHomeSortLast(Model model) {
        List<Teacher> teachers = teacherService.findAll().stream().parallel().sorted(Comparator.comparing(Teacher::getLastName)).toList();
        model.addAttribute("teacherList", teachers);
        model.addAttribute("selectType", 1);
        model.addAttribute("none", teachers.isEmpty());
        return "managers/teachers/teachers";
    }

    @GetMapping("/add")
    public String getAddTeacher(Model model) {
        Teacher teacher = new Teacher();
        model.addAttribute("teacher", teacher);
        return "managers/teachers/addTeacher";
    }

    @GetMapping("/edit/{id}")
    public String getEditTeacher(@PathVariable(value = "id") int id, Model model) {
        Teacher teacher = teacherService.findById(id);

        if(teacher == null) {
            return "redirect:/managers/teachers/1/3";
        }

        model.addAttribute("teacher", teacher);
        return "managers/teachers/editTeacher";
    }

    @GetMapping("/delete/{id}")
    public String deleteTeacher(@PathVariable(value = "id") int id, Model model) {
        //Make sure teacher exists
        Teacher teacher = teacherService.findById(id);
        if(teacher == null) {
            return "redirect:/managers/teachers/1/3";
        }

        //Delete classes and attendance first
        List<Class> classes = classService.findClassesByTeacherId(id);
        for(Class c : classes) {
            List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(c.getId());
            for (AttendanceContainer a : containers) {
                //Delete SA record
                List<StudentAttendance> attendances = studentAttendanceService.getSAByContainerId(a.getId());
                for (StudentAttendance s : attendances) {
                    studentAttendanceService.deleteById(s.getId());
                }
                //Delete container
                attendanceContainerService.deleteById(c.getId());
            }
            classService.deleteById(c.getId());
        }

        teacherService.deleteById(id);
        model.addAttribute("teacherList", teacherService.findAll());

        return "redirect:/managers/teachers/1/1";
    }

    @GetMapping("/classes/{id}") //Current classes
    public String teacherClasses(@PathVariable(value = "id") int id, Model model) {
        Teacher teacher = teacherService.findById(id);

        if(teacher == null)
            return "redirect:/managers/teachers/1/3";

        model.addAttribute("headerText", "Teachers > " +
                teacher.getFirstName() + " " + teacher.getLastName() + "'s Current Classes");

        List<Class> classes = classService.findClassesByTeacherId(id).stream().parallel()
                .filter(c -> c.getEndDate().after(new Date(System.currentTimeMillis())))
                .filter(c -> c.getStartDate().before(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 0);
        model.addAttribute("teacherId", id);
        model.addAttribute("none", classes.isEmpty());

        return "managers/teachers/teacherClasses";
    }

    @GetMapping("/classes/past/{id}")
    public String teacherClassesPast(@PathVariable(value = "id") int id, Model model) {
        Teacher teacher = teacherService.findById(id);

        if(teacher == null)
            return "redirect:/managers/teachers/1/3";

        model.addAttribute("headerText", "Teachers > " +
                teacher.getFirstName() + " " + teacher.getLastName() + "'s Past Classes");

        List<Class> classes = classService.findClassesByTeacherId(id).stream().parallel()
                .filter(c -> c.getEndDate().before(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 1);
        model.addAttribute("teacherId", id);
        model.addAttribute("none", classes.isEmpty());

        return "managers/teachers/teacherClasses";
    }

    @GetMapping("/classes/future/{id}")
    public String teacherClassesFuture(@PathVariable(value = "id") int id, Model model) {
        Teacher teacher = teacherService.findById(id);

        if(teacher == null)
            return "redirect:/managers/teachers/1/3";

        model.addAttribute("headerText", "Teachers > " +
                teacher.getFirstName() + " " + teacher.getLastName() + "'s Future Classes");

        List<Class> classes = classService.findClassesByTeacherId(id).stream().parallel()
                .filter(c -> c.getStartDate().after(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 2);
        model.addAttribute("teacherId", id);
        model.addAttribute("none", classes.isEmpty());

        return "managers/teachers/teacherClasses";
    }

    @GetMapping("/classes/all/{id}")
    public String teacherClassesAll(@PathVariable(value = "id") int id, Model model) {
        Teacher teacher = teacherService.findById(id);

        if(teacher == null)
            return "redirect:/managers/teachers/1/3";

        model.addAttribute("headerText", "Teachers > " +
                teacher.getFirstName() + " " + teacher.getLastName() + "'s Classes (All)");

        List<Class> classes = classService.findClassesByTeacherId(id).stream().parallel()
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 3);
        model.addAttribute("teacherId", id);
        model.addAttribute("none", classes.isEmpty());

        return "managers/teachers/teacherClasses";
    }

    @GetMapping("/classDetails/{id}/{selectType}/{tId}")
    public String getClassDetails(@PathVariable(value = "id") int id,
                                  @PathVariable(value = "selectType") int selectType,
                                  @PathVariable(value = "tId") int tId, Model model) {

        Class theClass = classService.findById(id);
        //Make sure objects exist
        if(theClass == null) {
            return "redirect:/managers/teachers/1/4";
        }

        Teacher teacher = theClass.getTeacher();
        if(teacher == null) {
            return "redirect:/managers/teachers/1/3";
        }

        String backUrl = "";
        switch (selectType) {
            case 1  -> backUrl = "/managers/teachers/classes/past/";
            case 2  -> backUrl = "/managers/teachers/classes/future/";
            case 3  -> backUrl = "/managers/teachers/classes/all/";
            default -> backUrl = "/managers/teachers/classes/";
        }

        model.addAttribute("backUrl", backUrl + tId);
        model.addAttribute("headerText", "Teachers > "+teacher.getFirstName()+ " "+teacher.getLastName()+" > "+theClass.getName());
        model.addAttribute("editUrl", "/managers/classes/edit/"+theClass.getId());
        model.addAttribute("className", theClass.getName());
        model.addAttribute("teacherName", teacher.getFirstName() + " " + teacher.getLastName());
        model.addAttribute("startDate", theClass.getStartDate());
        model.addAttribute("endDate", theClass.getEndDate());
        model.addAttribute("startTime", theClass.getStartTime());
        model.addAttribute("endTime", theClass.getEndTime());
        model.addAttribute("studentUrl", "/managers/classes/students/"+theClass.getId());

        return "managers/teachers/classDetails";
    }

    @PostMapping("/add")
    public String postAddTeacher(@ModelAttribute("teacher") @Valid Teacher teacher,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "managers/teachers/addTeacher";
        }

        teacherService.save(teacher);

        model.addAttribute("teacherList", teacherService.findAll());
        return "redirect:/managers/teachers/1/0";
    }

    @PostMapping("/edit")
    public String postEditTeacher(@ModelAttribute("teacher") @Valid Teacher teacher,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "managers/teachers/editTeacher";
        }

        teacherService.save(teacher);

        model.addAttribute("teacherList", teacherService.findAll());
        return "redirect:/managers/teachers/1/0";
    }
}