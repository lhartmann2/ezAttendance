package com.lhp.attendance.controller;

import com.lhp.attendance.entity.*;
import com.lhp.attendance.entity.Class;
import com.lhp.attendance.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("managers/students")
public class StudentController {

    public final String defaultUrl = "/managers/students/";
    public final String detailsUrl = "/managers/students/parentDetails/";

    private final StudentService studentService;
    private final ParentService parentService;
    private final ClassService classService;
    private final TeacherService teacherService;
    private final StudentAttendanceService studentAttendanceService;

    public StudentController(StudentService studentService, ParentService parentService,
                             ClassService classService, TeacherService teacherService,
                             StudentAttendanceService studentAttendanceService) {
        this.studentService = studentService;
        this.parentService = parentService;
        this.classService = classService;
        this.teacherService = teacherService;
        this.studentAttendanceService = studentAttendanceService;
    }

    @GetMapping({"/", "/{success}/{type}"}) //Default sort / By first name
    public String studentsHome(@PathVariable(value = "success", required = false) Optional<Integer> success,
                               @PathVariable(value = "type", required = false) Optional<Integer> type,
                               Model model) {

        Comparator<Student> comparator = Comparator.comparing(Student::getFirstName).thenComparing(Student::getNickName);
        List<Student> students = studentService.findAll().stream().parallel().sorted(comparator).collect(Collectors.toList());

        boolean warning = false;
        model.addAttribute("studentList", students);
        model.addAttribute("selectType", 0);
        model.addAttribute("none", students.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);
        if(type.isPresent()) {
            String text = "";
            if(type.get() == 0)
                text = "Student Saved";
            else if(type.get() == 1)
                text = "Student Deleted";
            else if(type.get() == 2) {
                warning = true;
                text = "There was an error processing your request: Student not found.";
            } else if(type.get() == 3) {
                warning = true;
                text = "There was an error processing your request: Class not found.";
            } else if(type.get() == 4) {
                warning = true;
                text = "There was an error processing your request: Teacher not found.";
            } else if(type.get() == 5) {
                warning = true;
                text = "There was an error processing your request: Parent not found.";
            }
            else
                text = "";
            model.addAttribute("warning", warning);
            model.addAttribute("bannerText", text);
        }
        return "managers/students/students";
    }

    @GetMapping("/sortLast")
    public String studentsHomeSortByLast(Model model) {
        List<Student> students = studentService.findAll().stream().parallel().sorted(Comparator.comparing(Student::getLastName)).toList();
        model.addAttribute("studentList", students);
        model.addAttribute("selectType", 1);
        model.addAttribute("none", students.isEmpty());
        return "managers/students/students";
    }

    @GetMapping("/sortAgeDesc")
    public String studentsHomeSortByAgeDesc(Model model) {
        List<Student> students = studentService.findAll().stream().parallel().sorted(Comparator.comparing(Student::getDob)).toList();
        model.addAttribute("studentList", students);
        model.addAttribute("selectType", 2);
        model.addAttribute("none", students.isEmpty());
        return "managers/students/students";
    }

    @GetMapping("/sortAgeAsc")
    public String studentsHomeSortByAgeAsc(Model model) {
        List<Student> students = studentService.findAll().stream().parallel().sorted(Comparator.comparing(Student::getDob).reversed()).toList();
        model.addAttribute("studentList", students);
        model.addAttribute("selectType", 3);
        model.addAttribute("none", students.isEmpty());
        return "managers/students/students";
    }

    @GetMapping("/classes/{sId}/{selectType}") //current classes
    public String getStudentClassesCurrent(@PathVariable(value = "sId") int id,
                                           @PathVariable(value = "selectType") int selectType, Model model) {

        Student student = studentService.findById(id);

        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        String backUrl = "/managers/students/";
        switch(selectType) {
            case 1 -> backUrl = "/managers/students/sortLast";
            case 2 -> backUrl = "/managers/students/sortAgeDesc";
            case 3 -> backUrl = "/managers/students/sortAgeAsc";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        List<Class> classes = classService.findClassesByStudentId(id).stream().parallel()
                .filter(c -> c.getEndDate().after(new Date(System.currentTimeMillis())))
                .filter(c -> c.getStartDate().before(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Students > " + studentName + "'s Current Classes");
        model.addAttribute("classList", classes);
        model.addAttribute("studentId", id);
        model.addAttribute("selectType", 0);
        model.addAttribute("origSelectType", selectType);
        model.addAttribute("none", classes.isEmpty());

        return "managers/students/studentClasses";
    }

    @GetMapping("/classes/past/{sId}/{selectType}")
    public String getStudentClassesPast(@PathVariable(value = "sId") int id,
                                        @PathVariable(value = "selectType") int selectType, Model model) {
        Student student = studentService.findById(id);

        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        String backUrl = "/managers/students/";
        switch(selectType) {
            case 1 -> backUrl = "/managers/students/sortLast";
            case 2 -> backUrl = "/managers/students/sortAgeDesc";
            case 3 -> backUrl = "/managers/students/sortAgeAsc";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        List<Class> classes = classService.findClassesByStudentId(id).stream().parallel()
                .filter(c -> c.getEndDate().before(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Students > " + studentName + "'s Past Classes");
        model.addAttribute("classList", classes);
        model.addAttribute("studentId", id);
        model.addAttribute("selectType", 1);
        model.addAttribute("origSelectType", selectType);
        model.addAttribute("none", classes.isEmpty());

        return "managers/students/studentClasses";
    }

    @GetMapping("/classes/future/{sId}/{selectType}")
    public String getStudentClassesFuture(@PathVariable(value = "sId") int id,
                                          @PathVariable(value = "selectType") int selectType, Model model) {
        Student student = studentService.findById(id);

        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        String backUrl = "/managers/students/";
        switch(selectType) {
            case 1 -> backUrl = "/managers/students/sortLast";
            case 2 -> backUrl = "/managers/students/sortAgeDesc";
            case 3 -> backUrl = "/managers/students/sortAgeAsc";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        List<Class> classes = classService.findClassesByStudentId(id).stream().parallel()
                .filter(c -> c.getStartDate().after(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Students > " + studentName + "'s Future Classes");
        model.addAttribute("classList", classes);
        model.addAttribute("studentId", id);
        model.addAttribute("selectType", 2);
        model.addAttribute("origSelectType", selectType);
        model.addAttribute("none", classes.isEmpty());

        return "managers/students/studentClasses";
    }

    @GetMapping("/classes/all/{sId}/{selectType}")
    public String getStudentClassesAll(@PathVariable(value = "sId") int id,
                                       @PathVariable(value = "selectType") int selectType, Model model) {
        Student student = studentService.findById(id);

        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        String backUrl = "/managers/students/";
        switch(selectType) {
            case 1 -> backUrl = "/managers/students/sortLast";
            case 2 -> backUrl = "/managers/students/sortAgeDesc";
            case 3 -> backUrl = "/managers/students/sortAgeAsc";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        List<Class> classes = classService.findClassesByStudentId(id).stream().parallel()
                .sorted(Comparator.comparing(Class::getName))
                .toList();

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Students > " +studentName+ "'s Classes (All)");
        model.addAttribute("classList", classes);
        model.addAttribute("studentId", id);
        model.addAttribute("selectType", 3);
        model.addAttribute("origSelectType", selectType);
        model.addAttribute("none", classes.isEmpty());

        return "managers/students/studentClasses";
    }

    @GetMapping("/classDetails/{cId}/{sId}/{selectType}/{origSelectType}")
    public String getStudentClassDetails(@PathVariable(value = "cId") int cId,
                                         @PathVariable(value = "sId") int sId,
                                         @PathVariable(value = "selectType") int selectType,
                                         @PathVariable(value = "origSelectType") int origSelectType, Model model) {
        Student student = studentService.findById(sId);

        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        Class theClass = classService.findById(cId);

        if(theClass == null) {
            return "redirect:/managers/students/1/3";
        }

        Teacher teacher = theClass.getTeacher();
        String backUrl = "/managers/students/classes/";
        if(selectType == 1)
            backUrl = backUrl + "past/";
        else if(selectType == 2)
            backUrl = backUrl + "future/";
        else if(selectType == 3)
            backUrl = backUrl + "all/";

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        model.addAttribute("headerText", "Students > " + studentName + " > " +theClass.getName());
        model.addAttribute("backUrl", backUrl + sId + "/" + origSelectType);
        model.addAttribute("editUrl", "/managers/classes/edit/"+cId);
        model.addAttribute("className", theClass.getName());
        model.addAttribute("teacherUrl", "/managers/students/teacherDetails/"+teacher.getId()+"/"+theClass.getId()+"/"+sId+"/"+selectType+"/"+origSelectType);
        model.addAttribute("teacherName", teacher.getFirstName() + " " + teacher.getLastName());
        model.addAttribute("startDate", theClass.getStartDate());
        model.addAttribute("endDate", theClass.getEndDate());
        model.addAttribute("startTime", theClass.getStartTime());
        model.addAttribute("endTime", theClass.getEndTime());
        model.addAttribute("studentUrl", "/managers/classes/students/"+theClass.getId());

        return "managers/students/classDetails";
    }

    @GetMapping("/teacherDetails/{tId}/{cId}/{sId}/{selectType}/{origSelectType}")
    public String getStudentTeacherDetails(@PathVariable(value = "tId") int tId,
                                           @PathVariable(value = "cId") int cId,
                                           @PathVariable(value = "sId") int sId,
                                           @PathVariable(value = "selectType") int selectType,
                                           @PathVariable(value = "origSelectType") int origSelectType, Model model) {

        Teacher teacher = teacherService.findById(tId);
        if(teacher == null) {
            return "redirect:/managers/students/1/4";
        }

        Student student = studentService.findById(sId);
        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        Class theClass = classService.findById(cId);
        if(theClass == null) {
            return "redirect:/managers/students/1/3";
        }

        String teacherName = teacher.getFirstName() + " " +teacher.getLastName();
        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        model.addAttribute("headerText", "Students > "+studentName+ " > "+theClass.getName()+ " > " +teacherName);
        model.addAttribute("backUrl", "/managers/students/classDetails/"+cId+"/"+sId+"/"+selectType+"/"+origSelectType);
        model.addAttribute("editUrl", "/managers/teachers/edit/"+tId);
        model.addAttribute("teacherName", teacherName);
        model.addAttribute("teacherPhone", teacher.getPhone());
        model.addAttribute("phoneUrl", "tel:+"+teacher.getPhone());
        model.addAttribute("teacherEmail", teacher.getEmail());
        model.addAttribute("emailUrl", "mailto:"+teacher.getEmail());
        model.addAttribute("classesUrl", "/managers/teachers/classes/"+tId);

        return "managers/students/teacherDetails";
    }

    @GetMapping("/add")
    public String getAddStudent(Model model) {
        Student student = new Student();
        model.addAttribute("student", student);
        model.addAttribute("parentList", parentService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Parent::getFirstName))
                .toList());
        return "managers/students/addStudent";
    }

    @GetMapping("/delete/{id}")
    public String getDeleteStudent(@PathVariable(value = "id") int id, Model model) {
        //Delete from parent bridge table first
        Student student = studentService.findById(id);
        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        List<Parent> parents = parentService.findParentsByStudentId(id);
        Parent parent1 = null;
        Parent parent2 = null;

        try {
            parent1 = parents.get(0);
        } catch(IndexOutOfBoundsException ex) {
            parent1 = null;
        }
        try {
            parent2 = parents.get(1);
        } catch(IndexOutOfBoundsException ex) {
            parent2 = null;
        }

        if(parent1 != null) {
            Set<Student> students = parent1.getStudents();
            if(students.contains(student)) {
                students.remove(student);
                parent1.setStudents(students);
                parentService.save(parent1);
            }
        }
        if(parent2 != null) {
            Set<Student> students = parent2.getStudents();
            if(students.contains(student)) {
                students.remove(student);
                parent2.setStudents(students);
                parentService.save(parent2);
            }
        }

        //Delete from enrolled classes
        List<Class> classes = classService.findClassesByStudentId(id);
        for(Class c : classes) {
            Set<Student> students = c.getStudents();
            students.remove(student);
            c.setStudents(students);
            classService.save(c);
        }

        //Delete attendance records
        List<StudentAttendance> attendances = studentAttendanceService.getSAByStudentId(id);
        for(StudentAttendance s : attendances) {
            studentAttendanceService.deleteById(s.getId());
        }

        studentService.deleteById(id);

        model.addAttribute("studentList", studentService.findAll());
        return "redirect:/managers/students/1/1";
    }

    @GetMapping("/edit/{id}")
    public String getEditStudent(@PathVariable(value = "id") int id, Model model) {
        Student student = studentService.findById(id);
        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        model.addAttribute("student", student);
        model.addAttribute("parentList", parentService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Parent::getFirstName))
                .toList());
        List<Parent> parents = parentService.findParentsByStudentId(id);

        try {
            Parent parent = parents.get(0);
            model.addAttribute("pSelect1", parent);
        } catch(IndexOutOfBoundsException ex) {
            //This shouldn't happen
        }

        try {
            Parent parent = parents.get(1);
            model.addAttribute("pSelect2", parent);
        } catch(IndexOutOfBoundsException ex) {
            //Secondary parent does not exist.
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        model.addAttribute("headerText", "Students > "+studentName+ " > Edit Student");

        return "managers/students/editStudent";
    }

    @GetMapping("/parentDetails/{id}/{sId}/{selectType}")
    public String getParentDetails(@PathVariable(value = "id") int id,
                                   @PathVariable(value = "sId") int studentId,
                                   @PathVariable(value = "selectType") int selectType, Model model) {
        Parent parent = parentService.findById(id);
        if(parent == null) {
            return "redirect:/managers/students/1/5";
        }

        Student student = studentService.findById(studentId);
        if(student == null) {
            return "redirect:/managers/students/1/2";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        model.addAttribute("headerText", "Students > " + studentName +
                " > " +parent.getFirstName() + " " +parent.getLastName());
        model.addAttribute("backUrl", "/managers/students/details/"+studentId+"/"+selectType);
        model.addAttribute("editUrl", "/managers/parents/edit/"+parent.getId());
        model.addAttribute("firstName", parent.getFirstName());
        model.addAttribute("lastName", parent.getLastName());
        model.addAttribute("street", parent.getStreet());
        model.addAttribute("city", parent.getCity());
        model.addAttribute("state", parent.getState());
        model.addAttribute("zip", parent.getZip());
        model.addAttribute("phone", parent.getPhone());
        model.addAttribute("email", parent.getEmail());
        model.addAttribute("phoneUrl", "tel:+"+parent.getPhone());
        model.addAttribute("emailUrl", "mailto:"+parent.getEmail());
        model.addAttribute("other", parent.getOther());
        return "managers/students/parentDetails";
    }

    @GetMapping("/details/{id}/{selectType}")
    public String getStudentDetails(@PathVariable(value = "id") int id,
                                    @PathVariable(value = "selectType") int selectType, Model model) {
        Student student = studentService.findById(id);
        if(student == null) {
            return "redirect:/managers/students/1/2";
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

        String backUrl = "/managers/students/";
        switch(selectType) {
            case 1 -> backUrl = "/managers/students/sortLast";
            case 2 -> backUrl = "/managers/students/sortAgeDesc";
            case 3 -> backUrl = "/managers/students/sortAgeAsc";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Students > "+studentName);
        model.addAttribute("editUrl", "/managers/students/edit/"+id);
        model.addAttribute("firstName", student.getFirstName());
        model.addAttribute("lastName", student.getLastName());
        model.addAttribute("nickName", student.getNickName());
        model.addAttribute("phone", student.getPhone());
        model.addAttribute("phoneUrl", "tel:+"+student.getPhone());

        model.addAttribute("dob", student.getDob());
        model.addAttribute("age", "(Age "+age+")");

        model.addAttribute("parent1", parent1 == null ? "" : parent1.getFirstName() + " " +parent1.getLastName());
        model.addAttribute("parent1Url", parent1 == null ? defaultUrl : detailsUrl + parent1.getId() + "/" +id + "/" +selectType);
        model.addAttribute("parent2", parent2 == null ? "" : parent2.getFirstName() + " " +parent2.getLastName());
        model.addAttribute("parent2Url", parent2 == null ? defaultUrl : detailsUrl + parent2.getId() + "/" +id + "/" +selectType);

        model.addAttribute("hospital", student.getHospital());
        model.addAttribute("allergies", student.getAllergies());
        model.addAttribute("notes", student.getNotes());

        return "managers/students/studentDetails";
    }

    @PostMapping("/add")
    public String postAddStudent(@ModelAttribute("student") @Valid Student student,
                                 BindingResult result, Model model, HttpServletRequest request) {

        boolean parentError = Integer.parseInt(request.getParameter("parentPicker1")) < 0;

        if(result.hasErrors()) {
            model.addAttribute("parentList", parentService.findAll());
            model.addAttribute("parentError", parentError);
            return "managers/students/addStudent";
        } else if(parentError) {
            model.addAttribute("parentList", parentService.findAll());
            model.addAttribute("parentError", true);
            return "managers/students/addStudent";
        }

        studentService.save(student);

        Integer parentId1 = Integer.parseInt(request.getParameter("parentPicker1"));
        Integer parentId2 = Integer.parseInt(request.getParameter("parentPicker2"));

        //Cannot be the same
        if(parentId1.equals(parentId2)) {
            parentId2 = -1;
        }

        if(parentId1 > -1) {
            Parent parent1 = parentService.findById(parentId1);
            Set<Student> students = parent1.getStudents();
            students.add(student);
            parent1.setStudents(students);
            parentService.save(parent1);
        }

        if(parentId2 > -1) {
            Parent parent2 = parentService.findById(parentId2);
            Set<Student> students = parent2.getStudents();
            students.add(student);
            parent2.setStudents(students);
            parentService.save(parent2);
        }

        model.addAttribute("studentList", studentService.findAll());
        return "redirect:/managers/students/1/0";
    }

    @PostMapping("/edit")
    public String postEditStudent(@ModelAttribute("student") @Valid Student student,
                                 BindingResult result, Model model, HttpServletRequest request) {
        List<Parent> parentList = parentService.findParentsByStudentId(student.getId());
        Parent pSelect1 = null;
        Parent pSelect2 = null;

        try {
            pSelect1 = parentList.get(0);
        } catch(IndexOutOfBoundsException ex) {
            //This shouldn't happen
            pSelect1 = null;
        }

        try {
            pSelect2 = parentList.get(1);
        } catch(IndexOutOfBoundsException ex) {
            //Secondary parent does not exist
            pSelect2 = null;
        }

        boolean parentError = Integer.parseInt(request.getParameter("parentPicker1")) < 0;

        if(result.hasErrors()) {
            model.addAttribute("parentList", parentService.findAll());
            model.addAttribute("pSelect1", pSelect1);
            model.addAttribute("pSelect2", pSelect2);
            if(parentError)
                model.addAttribute("parentError", true);
            String studentName = "";
            if((student.getNickName() == null) || (student.getNickName().isEmpty()))
                studentName = student.getFirstName() + " " + student.getLastName();
            else
                studentName = student.getNickName() + " " + student.getLastName();

            model.addAttribute("headerText", "Students > "+studentName+ " > Edit Student");
            return "managers/students/editStudent";
        } else if(parentError) {
            model.addAttribute("parentList", parentService.findAll());
            model.addAttribute("parentError", true);
            model.addAttribute("pSelect1", pSelect1);
            model.addAttribute("pSelect2", pSelect2);
            String studentName = "";
            if((student.getNickName() == null) || (student.getNickName().isEmpty()))
                studentName = student.getFirstName() + " " + student.getLastName();
            else
                studentName = student.getNickName() + " " + student.getLastName();

            model.addAttribute("headerText", "Students > "+studentName+ " > Edit Student");
            return "managers/students/editStudent";
        }

        studentService.save(student);

        Integer parentId1 = Integer.parseInt(request.getParameter("parentPicker1"));
        Integer parentId2 = Integer.parseInt(request.getParameter("parentPicker2"));

        //Delete relationships first
        if(pSelect1 != null) {
            Set<Student> students = pSelect1.getStudents();
            if(students.contains(student)) {
                students.remove(student);
                pSelect1.setStudents(students);
                parentService.save(pSelect1);
            }
        }

        if(pSelect2 != null) {
            Set<Student> students = pSelect2.getStudents();
            if(students.contains(student)) {
                students.remove(student);
                pSelect2.setStudents(students);
                parentService.save(pSelect2);
            }
        }

        //Cannot be the same
        if(parentId1.equals(parentId2)) {
            parentId2 = -1;
        }

        if(parentId1 > -1) {
            Parent parent1 = parentService.findById(parentId1);
            Set<Student> students = parent1.getStudents();
            if(!students.contains(student)) {
                students.add(student);
                parent1.setStudents(students);
                parentService.save(parent1);
            }
        }

        if(parentId2 > -1) {
            Parent parent2 = parentService.findById(parentId2);
            Set<Student> students = parent2.getStudents();
            if(!students.contains(student)) {
                students.add(student);
                parent2.setStudents(students);
                parentService.save(parent2);
            }
        }

        model.addAttribute("studentList", studentService.findAll());
        return "redirect:/managers/students/1/0";
    }
}