package com.lhp.attendance.controller;

import com.lhp.attendance.dto.ChosenStudentDTO;
import com.lhp.attendance.dto.ChosenTeacherDTO;
import com.lhp.attendance.entity.*;
import com.lhp.attendance.entity.Class;
import com.lhp.attendance.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Controller
@RequestMapping("managers/classes")
public class ClassController {

    public final String defaultUrl = "/managers/classes/";
    public final String detailsUrl = "/managers/classes/parentDetails/";

    private final ClassService classService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final ParentService parentService;
    private final StudentAttendanceService studentAttendanceService;
    private final AttendanceContainerService attendanceContainerService;

    public ClassController(ClassService classService, TeacherService teacherService, StudentService studentService, ParentService parentService, StudentAttendanceService studentAttendanceService, AttendanceContainerService attendanceContainerService) {
        this.classService = classService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.parentService = parentService;
        this.studentAttendanceService = studentAttendanceService;
        this.attendanceContainerService = attendanceContainerService;
    }

    @GetMapping({"/", "/{success}/{type}"}) //AKA Show Current
    public String getClassesHome(@PathVariable(value = "success", required = false) Optional<Integer> success,
                                 @PathVariable(value = "type", required = false) Optional<Integer> type,
                                 Model model) {

        List<Class> classes = classService.findAll().stream().parallel()
                .filter(c -> c.getEndDate().after(new Date(System.currentTimeMillis())))
                .filter(c -> c.getStartDate().before(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();

        model.addAttribute("headerText", "Current Classes");
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 0);
        model.addAttribute("none", classes.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);
        if(type.isPresent()) {
            String text = "";
            if(type.get() == 0)
                text = "Class Saved";
            else if(type.get() == 1)
                text = "Class Deleted";
            else if(type.get() == 2)
                text = "Class not found";
            else
                text = "???";
            model.addAttribute("bannerText", text);
        }
        return "managers/classes/classes";
    }

    @GetMapping({"/past", "/past/{success}/{type}"})
    public String getClassesPast(@PathVariable(value = "success", required = false) Optional<Integer> success,
                                 @PathVariable(value = "type", required = false) Optional<Integer> type,
                                 Model model) {

        List<Class> classes = classService.findAll().stream().parallel()
                .filter(c -> c.getEndDate().before(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();

        model.addAttribute("headerText", "Past Classes");
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 1);
        model.addAttribute("none", classes.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);
        if(type.isPresent()) {
            String text = "";
            if(type.get() == 0)
                text = "Class Saved";
            else if(type.get() == 1)
                text = "Class Deleted";
            else if(type.get() == 2)
                text = "Class not found";
            else
                text = "???";
            model.addAttribute("bannerText", text);
        }
        return "managers/classes/classes";
    }

    @GetMapping({"/future", "/future/{success}/{type}"})
    public String getClassesFuture(@PathVariable(value = "success", required = false) Optional<Integer> success,
                                   @PathVariable(value = "type", required = false) Optional<Integer> type,
                                   Model model) {

        List<Class> classes = classService.findAll().stream().parallel()
                .filter(c -> c.getStartDate().after(new Date(System.currentTimeMillis())))
                .sorted(Comparator.comparing(Class::getName))
                .toList();

        model.addAttribute("headerText", "Future Classes");
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 2);
        model.addAttribute("none", classes.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);
        if(type.isPresent()) {
            String text = "";
            if(type.get() == 0)
                text = "Class Saved";
            else if(type.get() == 1)
                text = "Class Deleted";
            else if(type.get() == 2)
                text = "Class not found";
            else
                text = "???";
            model.addAttribute("bannerText", text);
        }
        return "managers/classes/classes";
    }

    @GetMapping({"/all", "/all/{success}/{type}"})
    public String getClassesAll(@PathVariable(value = "success", required = false) Optional<Integer> success,
                                @PathVariable(value = "type", required = false) Optional<Integer> type,
                                Model model) {

        List<Class> classes = classService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Class::getName))
                .toList();

        model.addAttribute("headerText", "All Classes");
        model.addAttribute("classList", classes);
        model.addAttribute("selectType", 3);
        model.addAttribute("none", classes.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);
        if(type.isPresent()) {
            String text = "";
            if(type.get() == 0)
                text = "Class Saved";
            else if(type.get() == 1)
                text = "Class Deleted";
            else if(type.get() == 2)
                text = "Class not found";
            else
                text = "???";
            model.addAttribute("bannerText", text);
        }
        return "managers/classes/classes";
    }

    @GetMapping({"/edit/{id}", "/edit/{id}/{selectType}"})
    public String getEditClass(@PathVariable(value = "id") int id,
                               @PathVariable(value = "selectType", required = false) Optional<Integer> selectType,
                               Model model) {
        Class theClass = classService.findById(id);
        model.addAttribute("class", theClass);

        Set<Teacher> chosenTeacher = new HashSet<>();
        chosenTeacher.add(theClass.getTeacher());

        List<Teacher> teachers = teacherService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Teacher::getFirstName))
                .toList();
        List<Student> students = studentService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Student::getFirstName))
                .toList();

        int selectActive = 0;
        int select = -1;
        if(selectType.isPresent()) {
            if(selectType.get() > -1) {
                selectActive = 1;
                select = selectType.get();
            }
        }

        model.addAttribute("selectActive", selectActive);
        model.addAttribute("selectType", select);
        model.addAttribute("alertShown", 0);
        model.addAttribute("teacherList", teachers);
        model.addAttribute("studentList", students);
        model.addAttribute("chosenStudents", new ChosenStudentDTO(theClass.getStudents()));
        model.addAttribute("chosenTeacher", new ChosenTeacherDTO(chosenTeacher));


        return "managers/classes/editClass";
    }

    @GetMapping("/delete/{id}")
    public String getDeleteClass(@PathVariable(value = "id") int id, Model model) {
        deleteContainersByClass(id);

        classService.deleteById(id);

        model.addAttribute("classList", classService.findAll());
        return "redirect:/managers/classes/1/1";
    }

    @GetMapping({"/teacherDetails/{tId}/{cId}", "/teacherDetails/{tId}/{cId}/{selectType}"})
    public String getClassTeacherDetails(@PathVariable(value = "tId") int tId,
                                         @PathVariable(value = "cId") int cId,
                                         @PathVariable(value = "selectType", required = false) Optional<Integer> selectType,
                                         Model model) {

        Teacher teacher = teacherService.findById(tId);
        String className = classService.findById(cId).getName();

        String backUrl = "/managers/classes/details/"+cId;
        if(selectType.isPresent()) {
            if(selectType.get() > -1) {
                backUrl = backUrl + "/"+selectType.get();
            }
        }

        model.addAttribute("headerText", "Classes > "+className+ " > "+teacher.getFirstName() + " " +teacher.getLastName());
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("editUrl", "/managers/teachers/edit/"+tId);
        model.addAttribute("teacherName", teacher.getFirstName() + " " +teacher.getLastName());
        model.addAttribute("teacherPhone", teacher.getPhone());
        model.addAttribute("teacherEmail", teacher.getEmail());
        model.addAttribute("classesUrl", "/managers/teachers/classes/"+tId);

        return "managers/classes/teacherDetails";
    }

    @GetMapping({"/students/{id}", "/students/{id}/{selectType}"})
    public String getClassStudents(@PathVariable(value = "id") int id,
                                   @PathVariable(value = "selectType", required = false) Optional<Integer> selectType,
                                   Model model) {

        String className = classService.findById(id).getName();
        List<Student> students = studentService.findStudentsByClassId(id).stream().parallel()
                .sorted(Comparator.comparing(Student::getFirstName))
                .toList();

        String backUrl = "/managers/classes/";
        int select = -1;
        if(selectType.isPresent()) {
            select = selectType.get();
            switch(selectType.get()) {
                case 1 -> backUrl = "/managers/classes/past";
                case 2 -> backUrl = "/managers/classes/future";
                case 3 -> backUrl = "/managers/classes/all";
            }
        }

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("selectType", select);
        model.addAttribute("headerText", "Classes > " +className+ " > Students");
        model.addAttribute("classId", id);
        model.addAttribute("studentList", students);
        model.addAttribute("none", students.isEmpty());

        return "managers/classes/classStudents";
    }

    @GetMapping("/studentDetails/{sId}/{cId}/{selectType}")
    public String getClassStudentDetails(@PathVariable(value = "sId") int sId,
                                         @PathVariable(value = "cId") int cId,
                                         @PathVariable(value = "selectType") int selectType, Model model) {

        Class theClass = classService.findById(cId);
        Student student = studentService.findById(sId);
        List<Parent> parents = parentService.findParentsByStudentId(sId);

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


        model.addAttribute("headerText", "Classes > "+theClass.getName()+ " > "+ studentName);
        model.addAttribute("backUrl", "/managers/classes/students/"+cId+"/"+selectType);
        model.addAttribute("editUrl", "/managers/students/edit/"+sId);
        model.addAttribute("firstName", student.getFirstName());
        model.addAttribute("lastName", student.getLastName());
        model.addAttribute("nickName", student.getNickName());
        model.addAttribute("phone", student.getPhone());

        model.addAttribute("dob", student.getDob());
        model.addAttribute("age", "(Age "+age+")");

        model.addAttribute("parent1", parent1 == null ? "" : parent1.getFirstName() + " " +parent1.getLastName());
        model.addAttribute("parent1Url", parent1 == null ? defaultUrl : detailsUrl + parent1.getId() + "/" +sId+"/"+cId+"/"+selectType);
        model.addAttribute("parent2", parent2 == null ? "" : parent2.getFirstName() + " " +parent2.getLastName());
        model.addAttribute("parent2Url", parent2 == null ? defaultUrl : detailsUrl + parent2.getId() + "/" +sId+"/"+cId+"/"+selectType);

        model.addAttribute("hospital", student.getHospital());
        model.addAttribute("allergies", student.getAllergies());
        model.addAttribute("notes", student.getNotes());

        return "managers/classes/studentDetails";
    }

    @GetMapping("/parentDetails/{pId}/{sId}/{cId}/{selectType}")
    public String getClassParentDetails(@PathVariable(value = "pId") int pId,
                                        @PathVariable(value = "sId") int sId,
                                        @PathVariable(value = "cId") int cId,
                                        @PathVariable(value = "selectType") int selectType, Model model) {

        String className = classService.findById(cId).getName();
        Student student = studentService.findById(sId);
        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();
        Parent parent = parentService.findById(pId);
        String parentName = parent.getFirstName() + " " + parent.getLastName();


        model.addAttribute("headerText", "Classes > "+className+" > "+studentName+" > "+parentName);
        model.addAttribute("backUrl", "/managers/classes/studentDetails/"+sId+"/"+cId+"/"+selectType);
        model.addAttribute("editUrl", "/managers/parents/edit/"+pId);

        model.addAttribute("name", parentName);
        model.addAttribute("street", parent.getStreet());
        model.addAttribute("city", parent.getCity());
        model.addAttribute("state", parent.getState());
        model.addAttribute("zip", parent.getZip());
        model.addAttribute("phone", parent.getPhone());
        model.addAttribute("email", parent.getEmail());
        model.addAttribute("other", parent.getOther());

        return "managers/classes/parentDetails";
    }

    @GetMapping({"/details/{id}", "/details/{id}/{selectType}"})
    public String getClassDetails(@PathVariable(value = "id") int id,
                                  @PathVariable(value = "selectType", required = false) Optional<Integer> selectType,
                                  Model model) {
        Class theClass = classService.findById(id);
        Teacher teacher = theClass.getTeacher();

        String backUrl = "/managers/classes/";
        int select = -1;
        if(selectType.isPresent()) {
            select = selectType.get();
            switch(selectType.get()) {
                case 1 -> backUrl = "/managers/classes/past";
                case 2 -> backUrl = "/managers/classes/future";
                case 3 -> backUrl = "/managers/classes/all";
            }
        }

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Classes > "+theClass.getName());
        model.addAttribute("editUrl", "/managers/classes/edit/"+theClass.getId());
        model.addAttribute("className", theClass.getName());
        model.addAttribute("teacherUrl", "/managers/classes/teacherDetails/"+teacher.getId()+"/"+theClass.getId()+"/"+select);
        model.addAttribute("teacherName", teacher.getFirstName() + " " + teacher.getLastName());
        model.addAttribute("startDate", theClass.getStartDate());
        model.addAttribute("endDate", theClass.getEndDate());
        model.addAttribute("startTime", theClass.getStartTime());
        model.addAttribute("endTime", theClass.getEndTime());
        model.addAttribute("studentUrl", "/managers/classes/students/"+theClass.getId()+"/"+select);

        return "managers/classes/classDetails";
    }

    @GetMapping("/add")
    public String getAddClass(Model model) {
        model.addAttribute("class", new Class());

        List<Teacher> teachers = teacherService.findAll().stream().parallel().sorted(Comparator.comparing(Teacher::getFirstName)).toList();
        List<Student> students = studentService.findAll().stream().parallel().sorted(Comparator.comparing(Student::getFirstName)).toList();

        model.addAttribute("teacherList", teachers);
        model.addAttribute("studentList", students);
        model.addAttribute("chosenStudents", new ChosenStudentDTO(new HashSet<>()));
        model.addAttribute("chosenTeacher", new ChosenTeacherDTO(new HashSet<>()));
        return "managers/classes/addClass";
    }

    @PostMapping("/add")
    public String postAddClass(@ModelAttribute("class") @Valid Class theClass,
                               BindingResult result,
                               @ModelAttribute ChosenStudentDTO chosenStudentDTO,
                               @ModelAttribute ChosenTeacherDTO chosenTeacherDTO,
                               Model model, HttpServletRequest request) {

        Set<Teacher> teachers = chosenTeacherDTO.getTeachers();
        Set<Student> students = chosenStudentDTO.getStudents();


        boolean teacherError;
        boolean studentError;
        boolean dateError = false;
        boolean timeError = false;
        try {
            teacherError = teachers == null || (teachers.iterator().next() == null);
        } catch (NoSuchElementException ex) {
            teacherError = true;
        }
        try {
            studentError = students == null || (students.iterator().next() == null);
        } catch(NoSuchElementException ex) {
            studentError = true;
        }

        boolean weekDayError = theClass.getDayOfWeek().equals("none");

        if(theClass.getEndDate().before(theClass.getStartDate()))
            dateError = true;

        if(theClass.getEndTime().before(theClass.getStartTime()))
            timeError = true;

        if((result.hasErrors()) || (teacherError) || (studentError) || (weekDayError) || (dateError) || (timeError)) {
            List<Teacher> teacherList = teacherService.findAll();
            teacherList.sort(Comparator.comparing(Teacher::getFirstName));
            List<Student> studentList = studentService.findAll();
            studentList.sort(Comparator.comparing(Student::getFirstName));

            model.addAttribute("teacherError", teacherError);
            model.addAttribute("studentError", studentError);
            model.addAttribute("weekDayError", weekDayError);
            model.addAttribute("dateError", dateError);
            model.addAttribute("timeError", timeError);

            model.addAttribute("teacherList", teacherList);
            model.addAttribute("studentList", studentList);
            model.addAttribute("chosenStudents", new ChosenStudentDTO(students.isEmpty() ? new HashSet<>() : students));
            model.addAttribute("chosenTeacher",  new ChosenTeacherDTO(teachers.isEmpty() ? new HashSet<>() : teachers));
            return "managers/classes/addClass";
        }

        theClass.setStudents(students);
        theClass.setTeacher(teachers.iterator().next());
        classService.save(theClass);

        createContainers(theClass);

        model.addAttribute("classList", classService.findAll());
        return "redirect:/managers/classes/1/0";
    }

    @PostMapping("/edit/{selectActive}/{selectType}")
    public String postEditClass(@ModelAttribute("class") @Valid Class theClass,
                               BindingResult result,
                               @ModelAttribute ChosenStudentDTO chosenStudentDTO,
                               @ModelAttribute ChosenTeacherDTO chosenTeacherDTO,
                               @PathVariable(value = "selectActive") int selectActive,
                               @PathVariable(value = "selectType") int selectType,
                               Model model, HttpServletRequest request) {

        Set<Teacher> teachers = chosenTeacherDTO.getTeachers();
        Set<Student> students = chosenStudentDTO.getStudents();

        Teacher teacher = null;


        boolean teacherError;
        boolean studentError;
        boolean dateError = false;
        boolean timeError = false;
        try {
            teacherError = teachers == null || ((teacher = teachers.iterator().next()) == null);
        } catch (NoSuchElementException ex) {
            teacherError = true;
        }
        try {
            studentError = students == null || (students.iterator().next() == null);
        } catch(NoSuchElementException ex) {
            studentError = true;
        }

        boolean weekDayError = theClass.getDayOfWeek().equals("none");

        if(theClass.getEndDate().before(theClass.getStartDate()))
            dateError = true;

        if(theClass.getEndTime().before(theClass.getStartTime()))
            timeError = true;

        if((result.hasErrors()) || (teacherError) || (studentError) || (weekDayError) || (dateError) || (timeError)) {
            List<Teacher> teacherList = teacherService.findAll();
            teacherList.sort(Comparator.comparing(Teacher::getFirstName));
            List<Student> studentList = studentService.findAll();
            studentList.sort(Comparator.comparing(Student::getFirstName));

            model.addAttribute("teacherError", teacherError);
            model.addAttribute("studentError", studentError);
            model.addAttribute("weekDayError", weekDayError);
            model.addAttribute("dateError", dateError);
            model.addAttribute("timeError", timeError);

            model.addAttribute("teacherList", teacherList);
            model.addAttribute("studentList", studentList);
            model.addAttribute("chosenStudents", new ChosenStudentDTO(students.isEmpty() ? new HashSet<>() : students));
            model.addAttribute("chosenTeacher",  new ChosenTeacherDTO(teachers.isEmpty() ? new HashSet<>() : teachers));

            model.addAttribute("selectActive", selectActive);
            model.addAttribute("selectType", selectType);
            model.addAttribute("alertShown", 1);

            return "managers/classes/editClass";
        }

        boolean change = false;
        //Get instance of old class
        Class oldClass = classService.findById(theClass.getId());
        if(oldClass == null) {
            return "redirect:/managers/classes/1/2";
        }

        //Check if dates or day of week has changed
        if((!oldClass.getStartDate().equals(theClass.getStartDate())) ||
                (!oldClass.getEndDate().equals(theClass.getEndDate())) ||
                (!oldClass.getDayOfWeek().equals(theClass.getDayOfWeek()))) {
            change = true;
        }

        //Delete and recreate containers if dates or day of week change
        if(change) {
            deleteContainersByClass(theClass.getId());
            createContainers(theClass);
        } else {
            //Add remove students from attendance records if necessary
            if (!students.equals(oldClass.getStudents())) {
                List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(theClass.getId());

                //Get added students
                List<Student> addedStudents = new ArrayList<>();
                for (Student s : theClass.getStudents()) {
                    if (!oldClass.getStudents().contains(s)) {
                        addedStudents.add(s);
                    }
                }

                //Get removed students
                List<Student> removedStudents = new ArrayList<>();
                for (Student s : oldClass.getStudents()) {
                    if (!theClass.getStudents().contains(s)) {
                        removedStudents.add(s);
                    }
                }

                //Today - 1 day
                Date now = new Date(System.currentTimeMillis() + 86400000);

                //Create attendances for new students
                for (Student s : addedStudents) {
                    for (AttendanceContainer c : containers) {
                        //Only add for sessions on or after today
                        /*if((c.getDate().equals(now)) || (c.getDate().after(now)))*/
                        {
                            boolean found = false;
                            List<StudentAttendance> saForContainer = studentAttendanceService.getSAByContainerId(c.getId());
                            if (!saForContainer.isEmpty()) {
                                for (StudentAttendance a : saForContainer) {
                                    if (a.getStudent().getId() == s.getId()) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    StudentAttendance sa = new StudentAttendance();
                                    sa.setContainer(c);
                                    sa.setStudent(s);
                                    sa.setPresent(true);
                                    studentAttendanceService.save(sa);
                                }
                            }
                        }
                    }
                }

                //Remove attendances for removed students
                for (Student s : removedStudents) {
                    for (AttendanceContainer c : containers) {
                        //Only remove for sessions on or after today
                        /*if((c.getDate().equals(now)) || (c.getDate().after(now)))*/
                        {
                            for (StudentAttendance a : studentAttendanceService.getSAByContainerId(c.getId())) {
                                if (a.getStudent().getId() == s.getId()) {
                                    studentAttendanceService.deleteById(a.getId());
                                }
                            }
                        }
                    }
                }

            }
        }

        //Save updated class
        theClass.setStudents(students);
        theClass.setTeacher(teachers.iterator().next());
        classService.save(theClass);

        model.addAttribute("classList", classService.findAll());

        String backUrl = "/managers/classes/1/0";
        if(selectActive>0) {
            switch (selectType) {
                case 1 -> backUrl = "/managers/classes/future/1/0";
                case 2 -> backUrl = "/managers/classes/past/1/0";
                case 3 -> backUrl = "/managers/classes/all/1/0";
            }
        }

        return "redirect:"+backUrl;
    }

    private void createContainers(Class c) {
        List<LocalDate> dates = new ArrayList<>();

        //Get starting date for instant
        LocalDate startDate = convertToLocalDate(c.getStartDate());
        LocalDate endDate = convertToLocalDate(c.getEndDate());
        String weekDay = c.getDayOfWeek().toUpperCase();
        int startYear = startDate.getYear();
        Month startMonth = startDate.getMonth();
        int startDay = startDate.getDayOfMonth();

        //Get first class date
        LocalDate date = Year.of(startYear).atMonth(startMonth).atDay(startDay)
                .with(TemporalAdjusters.firstInMonth(DayOfWeek.valueOf(weekDay)));
        dates.add(date);

        //Get every date of that weekday until or before endDate
        while((date.isBefore(endDate)) || (date.isEqual(endDate))) {
             date = date.with(TemporalAdjusters.next(DayOfWeek.valueOf(weekDay)));
             if((date.isBefore(endDate)) || (date.isEqual(endDate)))
                dates.add(date);
        }

        //Double check dates are valid
        for(LocalDate d : dates) {
            if(((d.isEqual(startDate)) || (d.isAfter(startDate))) &&
                    ((d.isEqual(endDate)) || (d.isBefore(endDate)))) {
                AttendanceContainer container = new AttendanceContainer();
                container.setDate(convertToDate(d));
                container.setTheClass(c);
                attendanceContainerService.save(container);
            }
        }
    }

    private void deleteContainersByClass(int id) {
        //Delete containers and attendance records first
        List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(id);
        for(AttendanceContainer c : containers) {
            //Delete SA record
            List<StudentAttendance> attendances = studentAttendanceService.getSAByContainerId(c.getId());
            for(StudentAttendance s : attendances) {
                studentAttendanceService.deleteById(s.getId());
            }
            //Delete container
            attendanceContainerService.deleteById(c.getId());
        }
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date convertToDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
