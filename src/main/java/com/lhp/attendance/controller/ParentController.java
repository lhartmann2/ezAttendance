package com.lhp.attendance.controller;

import com.lhp.attendance.entity.Parent;
import com.lhp.attendance.entity.Student;
import com.lhp.attendance.service.ParentService;
import com.lhp.attendance.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("managers/parents")
public class ParentController {

    public final String defaultUrl = "/managers/parents/";
    public final String detailsUrl = "/managers/parents/details/";

    private final ParentService parentService;
    private final StudentService studentService;

    public ParentController(ParentService parentService, StudentService studentService) {
        this.parentService = parentService;
        this.studentService = studentService;
    }

    @GetMapping({"/", "/{success}/{type}"}) //default / sort by first name
    public String parentsHome(@PathVariable(value = "success", required = false) Optional<Integer> success,
                              @PathVariable(value = "type", required = false) Optional<Integer> type,
                              Model model) {

        List<Parent> parents = parentService.findAll().stream().parallel().sorted(Comparator.comparing(Parent::getFirstName)).toList();
        boolean warning = false;
        model.addAttribute("parentList", parents);
        model.addAttribute("selectType", 0);
        model.addAttribute("none", parents.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);
        if(type.isPresent()) {
            String text = "";
            if(type.get() == 0)
                text = "Parent Saved";
            else if(type.get() == 1)
                text = "Parent Deleted";
            else if(type.get() == 2) {
                warning = true;
                text = "There was an error processing your request: Parent not found.";
            } else if(type.get() == 3) {
                warning = true;
                text = "There was an error processing your request: Student not found.";
            }
            else
                text = "???";
            model.addAttribute("warning", warning);
            model.addAttribute("bannerText", text);
        }
        return "managers/parents/parents";
    }

    @GetMapping("/sortLast") //default / sort by first name
    public String parentsHomeSortLast(Model model) {
        List<Parent> parents = parentService.findAll().stream().parallel().sorted(Comparator.comparing(Parent::getLastName)).toList();
        model.addAttribute("parentList", parents);
        model.addAttribute("selectType", 1);
        model.addAttribute("none", parents.isEmpty());
        return "managers/parents/parents";
    }

    @GetMapping("/add")
    public String getAddParent(Model model) {
        Parent parent = new Parent();
        model.addAttribute("parent", parent);
        return "managers/parents/addParent";
    }

    @GetMapping("/edit/{id}")
    public String getEditParent(@PathVariable(value = "id") int id, Model model) {
        Parent parent = parentService.findById(id);

        if(parent == null) {
            return "redirect:/managers/parents/1/2";
        }

        model.addAttribute("parent", parent);
        return "managers/parents/editParent";
    }

    @GetMapping("/delete/{id}")
    public String getDeleteParent(@PathVariable(value = "id") int id, Model model) {

        if(parentService.findById(id) == null) {
            return "redirect:/managers/parents/1/2";
        }

        parentService.deleteById(id);
        model.addAttribute("parentList", parentService.findAll());
        return "redirect:/managers/parents/1/1";
    }

    @GetMapping("/details/{id}")
    public String getParentDetails(@PathVariable(value = "id") int id, Model model) {
        Parent parent = parentService.findById(id);

        if(parent == null) {
            return "redirect:/managers/parents/1/2";
        }

        model.addAttribute("headerText", "Parents > "+parent.getFirstName() +
                " "+parent.getLastName());
        model.addAttribute("editUrl", "/managers/parents/edit/"+parent.getId());
        model.addAttribute("firstName", parent.getFirstName());
        model.addAttribute("lastName", parent.getLastName());
        model.addAttribute("street", parent.getStreet());
        model.addAttribute("city", parent.getCity());
        model.addAttribute("state", parent.getState());
        model.addAttribute("zip", parent.getZip());
        model.addAttribute("phone", parent.getPhone());
        model.addAttribute("email", parent.getEmail());
        model.addAttribute("other", parent.getOther());
        return "managers/parents/parentDetails";
    }

    @GetMapping("/studentDetails/{id}/{pId}")
    public String getStudentDetails(@PathVariable(value = "id") int id,
                                    @PathVariable(value = "pId") int pId, Model model) {
        Student student = studentService.findById(id);
        if(student == null) {
            return "redirect:/managers/parents/1/3";
        }

        Parent parent = parentService.findById(pId);
        if(parent == null) {
            return "redirect:/managers/parents/1/2";
        }

        List<Parent> parents = parentService.findParentsByStudentId(id);
        Parent parent1 = null;
        Parent parent2 = null;

        try {
            parent1 = parents.get(0);
        } catch(IndexOutOfBoundsException ex) {
            //No parent here
        }

        try {
            parent2 = parents.get(1);
        } catch(IndexOutOfBoundsException ex) {
            //No parent here
        }

        Calendar studentCal = new GregorianCalendar();
        Calendar nowCal = new GregorianCalendar();
        studentCal.setTime(student.getDob());
        nowCal.setTime(new Date(System.currentTimeMillis()));
        int age = (nowCal.get(Calendar.YEAR)) - (studentCal.get(Calendar.YEAR));

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        model.addAttribute("headerText", "Parents > " + parent.getFirstName() + " " +
                parent.getLastName() + " > " +studentName);
        model.addAttribute("editUrl", "/managers/students/edit/"+id);
        model.addAttribute("backUrl", "/managers/parents/students/"+parent.getId());
        model.addAttribute("firstName", student.getFirstName());
        model.addAttribute("lastName", student.getLastName());
        model.addAttribute("nickName", student.getNickName());
        model.addAttribute("phone", student.getPhone());

        model.addAttribute("dob", student.getDob());
        model.addAttribute("age", "(Age "+age+")");

        model.addAttribute("parent1", parent1 == null ? "" : parent1.getFirstName() + " " +parent1.getLastName());
        model.addAttribute("parent1Url", parent1 == null ? defaultUrl : detailsUrl + parent1.getId());
        model.addAttribute("parent2", parent2 == null ? "" : parent2.getFirstName() + " " +parent2.getLastName());
        model.addAttribute("parent2Url", parent2 == null ? defaultUrl : detailsUrl + parent2.getId());

        model.addAttribute("hospital", student.getHospital());
        model.addAttribute("allergies", student.getAllergies());
        model.addAttribute("notes", student.getNotes());

        return "managers/parents/studentDetails";
    }

    @GetMapping("/students/{id}")
    public String getParentStudents(@PathVariable(value = "id") int id, Model model) {
        Parent parent = parentService.findById(id);

        if(parent == null) {
            return "redirect:/managers/parents/1/2";
        }

        List<Student> students = studentService.findStudentsByParentId(id).stream().parallel().sorted(Comparator.comparing(Student::getFirstName)).toList();

        model.addAttribute("parent", parent);
        model.addAttribute("parentId", parent.getId());
        model.addAttribute("studentList", students);
        model.addAttribute("headerText", "Parents > "+parent.getFirstName() +
                " " +parent.getLastName()+ "'s Students");
        model.addAttribute("none", students.isEmpty());
        return "managers/parents/parentStudents";
    }

    @PostMapping("/add")
    public String postAddParent(@ModelAttribute("parent") @Valid Parent parent,
                                 BindingResult result, Model model) {
        if(result.hasErrors()) {
            return "managers/parents/addParent";
        }

        parentService.save(parent);

        model.addAttribute("parentList", parentService.findAll());
        return "redirect:/managers/parents/1/0";
    }

    @PostMapping("/edit")
    public String postEditParent(@ModelAttribute("parent") @Valid Parent parent,
                                 BindingResult result, Model model) {
        if(result.hasErrors()) {
            return "managers/parents/editParent";
        }

        parentService.save(parent);

        model.addAttribute("parentList", parentService.findAll());
        return "redirect:/managers/parents/1/0";
    }
}
