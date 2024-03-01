package com.lhp.attendance.controller;

import com.lhp.attendance.dto.AttendanceExportDTO;
import com.lhp.attendance.dto.ChosenClassDTO;
import com.lhp.attendance.dto.ChosenStudentDTO;
import com.lhp.attendance.dto.StudentAttendanceDTO;
import com.lhp.attendance.entity.*;
import com.lhp.attendance.entity.Class;
import com.lhp.attendance.service.*;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("managers/attendance")
public class AttendanceController {

    private final Locale locale = Locale.US;

    public final String defaultUrl = "/managers/attendance/";
    public final String detailsUrl = "/managers/attendance/parentDetails/";

    @Autowired
    private FileExporter fileExporter;

    private final AttendanceContainerService attendanceContainerService;
    private final StudentAttendanceService studentAttendanceService;
    private final ClassService classService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ParentService parentService;
    
    public AttendanceController(AttendanceContainerService attendanceContainerService, StudentAttendanceService studentAttendanceService, ClassService classService, StudentService studentService, TeacherService teacherService, ParentService parentService) {
        this.attendanceContainerService = attendanceContainerService;
        this.studentAttendanceService = studentAttendanceService;
        this.classService = classService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.parentService = parentService;
    }

    @GetMapping({"/{tId}", "/{tId}/{success}"})
    public String getAttendanceToday(@PathVariable(value = "tId") int tId,
                                     @PathVariable(value = "success", required = false) Optional<Integer> success,
                                     Model model) {

        //Setup header text
        var formatter = new SimpleDateFormat("MMM-dd-yyyy");
        var dayFormatter = new SimpleDateFormat("EEEE");
        String todayString = formatter.format(new Date(System.currentTimeMillis()));
        String todayDay = dayFormatter.format(new Date(System.currentTimeMillis()));

        List<AttendanceContainer> containers = new ArrayList<>();

        List<AttendanceContainer> fromDao = attendanceContainerService.findAll().stream().parallel()
                .filter(c -> DateUtils.isSameDay(c.getDate(), new Date(System.currentTimeMillis())))
                .toList();

        //Sort by Teacher if necessary
        String headerTeacherStr = "";
        Teacher teacher = teacherService.findById(tId);
        if(teacher != null) {
            headerTeacherStr = " > "+teacher.getFirstName()+" "+teacher.getLastName()+"'s Classes";
            for(AttendanceContainer a : fromDao) {
                if(a.getTheClass().getTeacher() == teacher) {
                    containers.add(a);
                }
            }
        } else {
            tId = -1;
            containers = fromDao;
        }

        //For dropdown
        List<Teacher> teachers = teacherService.findAll().stream().parallel()
                        .sorted(Comparator.comparing(Teacher::getFirstName)).toList();

        model.addAttribute("headerText", "Today's Attendance > "+todayDay+" "+todayString + headerTeacherStr);
        model.addAttribute("containerList", containers);
        model.addAttribute("teacherList", teachers);
        model.addAttribute("selectType", 0);
        model.addAttribute("teacherId", tId);
        model.addAttribute("none", containers.isEmpty());
        if((success.isPresent()) && (success.get() > 0))
            model.addAttribute("showBanner", true);

        return "managers/attendance/attendance";
    }

    @GetMapping({"/thisWeek/{tId}", "/thisWeek/{tId}/{success}"})
    public String getAttendanceThisWeek(@PathVariable(value = "tId") int tId,
                                        @PathVariable(value = "success", required = false) Optional<Integer> success,
                                        Model model) {

        //Setup header text
        Calendar calendar = Calendar.getInstance(locale);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM-dd-yyyy");
        String weekStart = df.format(calendar.getTime());
        calendar.add(Calendar.DATE, 6);
        String weekEnd = df.format(calendar.getTime());

        SimpleDateFormat formatter = new SimpleDateFormat("w");
        SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");

        int thisWeekNum = Integer.parseInt(formatter.format(new Date(System.currentTimeMillis())));
        int thisYear = Integer.parseInt(yearFormatter.format(new Date(System.currentTimeMillis())));

        List<AttendanceContainer> containers = new ArrayList<>();
        List<AttendanceContainer> temp = new ArrayList<>();
        List<AttendanceContainer> fromDao = attendanceContainerService.findAll();
        for(AttendanceContainer c : fromDao) {
            if(Integer.parseInt(yearFormatter.format(c.getDate())) == thisYear) {
                if(Integer.parseInt(formatter.format(c.getDate())) == thisWeekNum) {
                    temp.add(c);
                }
            }
        }

        //Filter by teacher if necessary
        Teacher teacher = teacherService.findById(tId);
        String headerTeacherStr = "";
        if(teacher != null) {
            headerTeacherStr = " > "+teacher.getFirstName()+" "+teacher.getLastName()+"'s Classes";
            for(AttendanceContainer c : temp) {
                if(c.getTheClass().getTeacher() == teacher) {
                    containers.add(c);
                }
            }
        } else {
            tId = -1;
            containers = temp;
        }

        //Sort by date ascending
        containers.sort(Comparator.comparing(AttendanceContainer::getDate));

        //For dropdown
        List<Teacher> teachers = teacherService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Teacher::getFirstName)).toList();

        model.addAttribute("headerText", "This Week's Attendance > Week " +thisWeekNum+ " > "+weekStart+" - "+weekEnd + headerTeacherStr);
        model.addAttribute("containerList", containers);
        model.addAttribute("teacherList", teachers);
        model.addAttribute("selectType", 1);
        model.addAttribute("total", "Total: "+containers.size());
        model.addAttribute("teacherId", tId);
        model.addAttribute("none", containers.isEmpty());
        if(success.isPresent()) {
            if(success.get() > 0) {
                model.addAttribute("showBanner", true);
            }
        }

        return "managers/attendance/attendance";
    }

    @GetMapping({"/thisMonth/{tId}", "/thisMonth/{tId}/{success}"})
    public String getAttendanceThisMonth(@PathVariable(value = "tId") int tId,
                                         @PathVariable(value = "success", required = false) Optional<Integer> success,
                                         Model model) {
        var monthFormatter = new SimpleDateFormat("M");
        var yearFormatter = new SimpleDateFormat("yyyy");
        int thisMonth = Integer.parseInt(monthFormatter.format(new Date(System.currentTimeMillis())));
        int thisYear = Integer.parseInt(yearFormatter.format(new Date(System.currentTimeMillis())));

        //Setup header text
        var dff = new SimpleDateFormat("MMMM");
        String thisMonthName = dff.format(new Date(System.currentTimeMillis()));

        List<AttendanceContainer> containers = new ArrayList<>();
        List<AttendanceContainer> temp = new ArrayList<>();
        List<AttendanceContainer> fromDao = attendanceContainerService.findAll();
        for(AttendanceContainer c : fromDao) {
            if(Integer.parseInt(yearFormatter.format(c.getDate())) == thisYear) {
                if(Integer.parseInt(monthFormatter.format(c.getDate())) == thisMonth) {
                    temp.add(c);
                }
            }
        }

        //Filter by teacher if necessary
        Teacher teacher = teacherService.findById(tId);
        String headerTeacherStr = "";
        if(teacher != null) {
            headerTeacherStr = " > "+teacher.getFirstName()+" "+teacher.getLastName()+"'s Classes";
            for(AttendanceContainer c : temp) {
                if(c.getTheClass().getTeacher() == teacher)
                    containers.add(c);
            }
        } else {
            tId = -1;
            containers = temp;
        }

        containers.sort(Comparator.comparing(AttendanceContainer::getDate));

        //For dropdown
        List<Teacher> teachers = teacherService.findAll().stream().parallel()
                .sorted(Comparator.comparing(Teacher::getFirstName)).toList();

        model.addAttribute("headerText", "This Month's Attendance > "+thisMonthName + " " +thisYear + headerTeacherStr);
        model.addAttribute("containerList", containers);
        model.addAttribute("teacherList", teachers);
        model.addAttribute("selectType", 2);
        model.addAttribute("total", "Total: "+containers.size());
        model.addAttribute("teacherId", tId);
        model.addAttribute("none", containers.isEmpty());
        if(success.isPresent()) {
            if(success.get() > 0) {
                model.addAttribute("showBanner", true);
            }
        }

        return "managers/attendance/attendance";
    }

    @GetMapping("/byDate/{tId}/{sId}")
    public String getAttendanceByDateSelect(@PathVariable(value = "tId") int tId,
                                            @PathVariable(value = "sId") int sId, Model model) {

        //Setup back URL
        String backUrl;
        switch (sId) {
            case 1 -> backUrl = "/managers/attendance/thisWeek/";
            case 2 -> backUrl = "/managers/attendance/thisMonth/";
            default -> backUrl = "/managers/attendance/";
        }

        if(teacherService.findById(tId) == null)
            tId = -1;


        model.addAttribute("backUrl", backUrl + tId);
        model.addAttribute("teacherId", tId);
        model.addAttribute("selectType", sId);

        return "managers/attendance/attendanceDateSelect";
    }

    @GetMapping({"/byDate/{tId}", "/byDate/{tId}/{success}/{yes}"})
    public String postAttendanceByDateSelect(@RequestParam(value = "startDate") String sD,
                                             @RequestParam(value = "endDate") String eD,
                                             @PathVariable(value = "tId") int tId,
                                             @PathVariable(value = "success", required = false) Optional<Integer> success,
                                             @PathVariable(value = "yes", required = false) Optional<Integer> dummy,
                                             Model model) {

        boolean startDateError = false;
        boolean endDateError = false;

        LocalDate startDate = LocalDate.parse(sD);
        LocalDate endDate = LocalDate.parse(eD);

        //Check date ranges are valid
        if(startDate.isAfter(endDate))
            startDateError = true;
        if(endDate.isBefore(startDate))
            endDateError = true;
        if(startDateError || endDateError) { //Errors
            model.addAttribute("startDateError", startDateError);
            model.addAttribute("endDateError", endDateError);
            model.addAttribute("sdVal", sD);
            model.addAttribute("edVal", eD);

            return "managers/attendance/attendanceDateSelect";
        }

        //Setup header text
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM-dd-yyyy");
        String sDate = df.format(convertLocalDate(startDate));
        String eDate = df.format(convertLocalDate(endDate));

        //Get containers for date range
        List<AttendanceContainer> containers = new ArrayList<>();
        List<AttendanceContainer> temp = new ArrayList<>();
        List<AttendanceContainer> fromDao = attendanceContainerService.findAll();

        for(AttendanceContainer c : fromDao) {
            LocalDate cDate = convertToLocalDate(c.getDate());
            if(((cDate.isEqual(startDate)) || (cDate.isAfter(startDate))) &&
                    ((cDate.isEqual(endDate)) || (cDate.isBefore(endDate)))) {
                temp.add(c);
            }
        }

        Teacher teacher = teacherService.findById(tId);
        String headerTeacherStr = "";
        if(teacher != null) {
            headerTeacherStr = " > "+teacher.getFirstName()+" "+teacher.getLastName()+"'s Classes";
            for(AttendanceContainer a : temp) {
                if(a.getTheClass().getTeacher() == teacher)
                    containers.add(a);
            }
        } else {
            tId = -1;
            containers = temp;
        }

        containers.sort(Comparator.comparing(AttendanceContainer::getDate));

        //For teacher filter
        List<Teacher> teachers = teacherService.findAll().stream().sorted(Comparator.comparing(Teacher::getFirstName)).toList();

        model.addAttribute("headerText", "Attendance > "+sDate+" to "+eDate + headerTeacherStr);
        model.addAttribute("sdVal", sD);
        model.addAttribute("edVal", eD);
        model.addAttribute("containerList", containers);
        model.addAttribute("teacherList", teachers);
        model.addAttribute("startDate", sD);
        model.addAttribute("endDate", eD);
        model.addAttribute("submitUrl", "/managers/attendance/byDate/"+tId);
        model.addAttribute("teacherId", tId);
        model.addAttribute("none", containers.isEmpty());
        if(success.isPresent()) {
            if(success.get() > 0) {
                model.addAttribute("showBanner", true);
            }
        }


        return "managers/attendance/attendanceByDate";
    }

    @GetMapping({"/take/{cId}/{selectType}/{tId}",
            "/take/{cId}/{selectType}/{tId}/{startDate}/{endDate}"})
    public String getTakeAttendance(@PathVariable(value = "cId") int cId,
                                    @PathVariable(value = "selectType") int selectType,
                                    @PathVariable(value = "tId") int tId,
                                    @PathVariable(value = "startDate", required = false) String sD,
                                    @PathVariable(value = "endDate", required = false) String eD,Model model) {

        AttendanceContainer container = attendanceContainerService.findById(cId);
        Class theClass = container.getTheClass();

        //Setup header text
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM-dd-yyyy");
        String date = df.format(container.getDate());

        Set<Student> students = theClass.getStudents();

        //Error if no students for class
        if((students == null) || (students.isEmpty())) {
            model.addAttribute("errorText", "No students were found for class: '"+theClass.getName()+"' (ID: "+theClass.getId()+")");
            return "managers/attendance/attendanceError";
        }

        //See if attendances exist already for this container
        List<StudentAttendance> attendances = studentAttendanceService.getSAByContainerId(cId);

        if((attendances == null) || (attendances.isEmpty())) {
            attendances = new ArrayList<>();
            //Initialize new attendance record for each student
            for (Student s : students) {
                StudentAttendance attendance = new StudentAttendance();
                attendance.setContainer(container);
                attendance.setStudent(s);
                attendance.setPresent(true);
                attendance.setExcused(false);
                attendance.setReason("");
                attendance = studentAttendanceService.save(attendance);
                attendances.add(attendance);
            }
        }

        //Error if attendances still empty
        if(attendances.isEmpty()) {
            model.addAttribute("errorText", "There were no student records created for this event. (Container ID: "+container.getId()+", Class ID: "+theClass.getId()+")");
            return "managers/attendance/attendanceError";
        }

        StudentAttendanceDTO dto = new StudentAttendanceDTO(attendances);

        String word = "Take";
        if(container.getTaken())
            word = "Edit";

        String backUrl;
        switch (selectType) {
            case 1 -> backUrl = "/managers/attendance/thisWeek/" + tId;
            case 2 -> backUrl = "/managers/attendance/thisMonth/" + tId;
            case 3 -> backUrl = "/managers/attendance/byDate/"+tId+"?startDate="+sD+"&endDate="+eD;
            default -> backUrl = "/managers/attendance/" + tId;
        }

        String takeUrl;
        if (selectType == 3) {
            takeUrl = "/managers/attendance/take/" + cId + "/" + selectType + "/" + tId + "/" + sD + "/" + eD;
        } else {
            takeUrl = "/managers/attendance/take/" + cId + "/" + selectType + "/" + tId;
        }

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", word + " Attendance > "+theClass.getName()+ " on "+date);
        model.addAttribute("dto", dto);
        model.addAttribute("submitUrl", takeUrl);

        return "managers/attendance/takeAttendance";
    }

    @PostMapping({"/take/{cId}/{selectType}/{tId}", "/take/{cId}/{selectType}/{tId}/{startDate}/{endDate}"})
    public String postTakeAttendance(@PathVariable(value = "cId") int cId,
                                     @PathVariable(value = "selectType") int selectType,
                                     @PathVariable(value = "tId") int tId,
                                     @PathVariable(value = "startDate", required = false) String sD,
                                     @PathVariable(value = "endDate", required = false) String eD,
                                     @ModelAttribute StudentAttendanceDTO wrapper, Model model) {

        //Get records out of wrapper
        List<StudentAttendance> attendances = wrapper.getAttendances();

        //Error if wrapper is empty
        if((attendances == null) || (attendances.isEmpty())) {
            model.addAttribute("errorText", "No attendance records were found in the returning wrapper.");
            return "managers/attendance/attendanceError";
        }

        //Save records to DB
        for(StudentAttendance a : attendances) {
            studentAttendanceService.save(a);
        }

        //Mark container as 'taken'
        AttendanceContainer container = attendanceContainerService.findById(cId);
        container.setTaken(true);
        attendanceContainerService.save(container);

        String backUrl;
        switch (selectType) {
            case 1 -> backUrl = "/managers/attendance/thisWeek/" + tId + "/1";
            case 2 -> backUrl = "/managers/attendance/thisMonth/" + tId + "/1";
            case 3 -> backUrl = "/managers/attendance/byDate/"+tId+"/1/1?startDate="+sD+"&endDate="+eD;
            default -> backUrl = "/managers/attendance/" + tId+ "/1";
        }


        return "redirect:"+backUrl;
    }

    @GetMapping({"/view/{cId}/{selectType}/{tId}",
            "/view/{cId}/{selectType}/{tId}/{startDate}/{endDate}"})
    public String getViewAttendance(@PathVariable(value = "cId") int cId,
                                    @PathVariable(value = "selectType") int selectType,
                                    @PathVariable(value = "tId") int tId,
                                    @PathVariable(value = "startDate", required = false) String sD,
                                    @PathVariable(value = "endDate", required = false) String eD, Model model) {

        AttendanceContainer container = attendanceContainerService.findById(cId);
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM-dd-yyyy");
        String date = df.format(container.getDate());

        List<StudentAttendance> attendances = studentAttendanceService.getSAByContainerId(cId);
        attendances.sort(Comparator.comparing(s -> s.getStudent().getFirstName()));

        String header = container.getTheClass().getName() + " on " + date;

        boolean notTaken = !(container.getTaken());
        String takeText = "Edit";
        if(notTaken)
            takeText = "Take";

        String backUrl;
        switch (selectType) {
            case 1 -> backUrl = "/managers/attendance/thisWeek/" + tId;
            case 2 -> backUrl = "/managers/attendance/thisMonth/" + tId;
            case 3 -> backUrl = "/managers/attendance/byDate/"+tId+"?startDate="+sD+"&endDate="+eD;
            default -> backUrl = "/managers/attendance/" + tId;
        }

        String takeUrl;
        if (selectType == 3) {
            takeUrl = "/managers/attendance/take/" + cId + "/" + selectType + "/" + tId + "/" + sD + "/" + eD;
        } else {
            takeUrl = "/managers/attendance/take/" + cId + "/" + selectType + "/" + tId;
        }

        boolean dateBased = (selectType == 3);

        model.addAttribute("headerText", "Attendance > View Record > "+header);
        model.addAttribute("atList", attendances);
        model.addAttribute("cId", cId);
        model.addAttribute("notTaken", notTaken);
        model.addAttribute("takeText", takeText);
        model.addAttribute("selectType", selectType);
        model.addAttribute("tId", tId);
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("takeUrl", takeUrl);
        model.addAttribute("exportUrl", "/managers/attendance/exportOne/"+cId+"/"+selectType+"/"+tId);
        model.addAttribute("dateBased", dateBased);
        if(dateBased) {
            model.addAttribute("startDate", sD);
            model.addAttribute("endDate", eD);
        }

        return "managers/attendance/attendanceDetails";
    }
    @GetMapping({"/classDetails/{cId}/{selectType}/{tId}",
            "/classDetails/{cId}/{selectType}/{tId}/{startDate}/{endDate}"})
    public String getClassDetails(@PathVariable(value = "cId") int cId,
                                  @PathVariable(value = "selectType") int selectType,
                                  @PathVariable(value = "tId") int tId,
                                  @PathVariable(value = "startDate", required = false) String sD,
                                  @PathVariable(value = "endDate", required = false) String eD,Model model) {

        Class theClass = classService.findById(cId);
        Teacher teacher = theClass.getTeacher();

        String backUrl;
        switch (selectType) {
            case 1 -> backUrl = "/managers/attendance/thisWeek/"+tId;
            case 2 -> backUrl = "/managers/attendance/thisMonth/"+tId;
            case 3 -> backUrl = "/managers/attendance/byDate/"+tId+"?startDate="+sD+"&endDate="+eD;
            default -> backUrl = "/managers/attendance/"+tId;
        }
        String teacherUrl = "/managers/attendance/teacherDetails/"+teacher.getId()+"/"+cId+"/"+selectType+"/"+tId;
        if (selectType == 3) {
            teacherUrl = teacherUrl + "/" + sD + "/" + eD;
        }

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Attendance > "+theClass.getName());
        model.addAttribute("editUrl", "/managers/classes/edit/"+theClass.getId());
        model.addAttribute("className", theClass.getName());
        model.addAttribute("teacherUrl", teacherUrl);
        model.addAttribute("teacherName", teacher.getFirstName() + " " + teacher.getLastName());
        model.addAttribute("startDate", theClass.getStartDate());
        model.addAttribute("endDate", theClass.getEndDate());
        model.addAttribute("startTime", theClass.getStartTime());
        model.addAttribute("endTime", theClass.getEndTime());
        model.addAttribute("studentUrl", "/managers/classes/students/"+theClass.getId());

        return "managers/attendance/classDetails";
    }

    @GetMapping({"/teacherDetails/{teacher}/{cId}/{selectType}/{tId}",
            "/teacherDetails/{teacher}/{cId}/{selectType}/{tId}/{startDate}/{endDate}"})
    public String getTeacherDetails(@PathVariable(value = "teacher") int teacherId,
                                    @PathVariable(value = "cId") int cId,
                                    @PathVariable(value = "selectType") int selectType,
                                    @PathVariable(value = "tId") int tId,
                                    @PathVariable(value = "startDate", required = false) String sD,
                                    @PathVariable(value = "endDate", required = false) String eD, Model model) {

        Teacher teacher = teacherService.findById(teacherId);
        String className = attendanceContainerService.findById(cId).getTheClass().getName();

        String backUrl = "/managers/attendance/classDetails/"+cId+"/"+selectType+"/"+tId;
        if (selectType == 3) {
            backUrl = backUrl + "/" + sD + "/" + eD;
        }

        model.addAttribute("headerText", "Attendance > "+className+ " > "+teacher.getFirstName() + " " +teacher.getLastName());
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("editUrl", "/managers/teachers/edit/"+teacherId);
        model.addAttribute("teacherName", teacher.getFirstName() + " " +teacher.getLastName());
        model.addAttribute("teacherPhone", teacher.getPhone());
        model.addAttribute("teacherEmail", teacher.getEmail());
        model.addAttribute("classesUrl", "/managers/teachers/classes/"+teacherId);

        return "managers/attendance/teacherDetails";
    }

    @GetMapping({"/studentDetails/{sId}/{cId}/{selectType}/{tId}",
            "/studentDetails/{sId}/{cId}/{selectType}/{tId}/{startDate}/{endDate}"})
    public String getStudentDetails(@PathVariable(value = "sId") int sId,
                                    @PathVariable(value = "cId") int cId,
                                    @PathVariable(value = "selectType") int selectType,
                                    @PathVariable(value = "tId") int tId,
                                    @PathVariable(value = "startDate", required = false) String sD,
                                    @PathVariable(value = "endDate", required = false) String eD, Model model) {
        Student student = studentService.findById(sId);
        AttendanceContainer container = attendanceContainerService.findById(cId);
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM-dd-yyyy");
        String date = df.format(container.getDate());
        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();
        String header = container.getTheClass().getName() + " on " + date + " > " + studentName;


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

        String backUrl = "/managers/attendance/view/"+cId+"/"+selectType+"/"+tId;
        String parent1Url = detailsUrl + parent1.getId() + "/" +sId+ "/" +cId+"/"+selectType+"/"+tId;
        String parent2Url;
        try {
            parent2Url = detailsUrl + parent2.getId() + "/" +sId+ "/" +cId+"/"+selectType+"/"+tId;
        } catch(NullPointerException e) {
            parent2Url = defaultUrl;
        }
        if(selectType == 3) {
            backUrl = "/managers/attendance/view/"+cId+"/"+selectType+"/"+tId+"/"+sD+"/"+eD;
            parent1Url = parent1Url + "/"+sD+"/"+eD;
            parent2Url = parent2Url + "/"+sD+"/"+eD;
        }

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("headerText", "Attendance > View Record > "+header);
        model.addAttribute("editUrl", "/managers/students/edit/"+sId);
        model.addAttribute("firstName", student.getFirstName());
        model.addAttribute("lastName", student.getLastName());
        model.addAttribute("nickName", student.getNickName());
        model.addAttribute("phone", student.getPhone());

        model.addAttribute("dob", student.getDob());
        model.addAttribute("age", "(Age "+age+")");

        model.addAttribute("parent1", parent1 == null ? "" : parent1.getFirstName() + " " +parent1.getLastName());
        model.addAttribute("parent1Url", parent1 == null ? defaultUrl : parent1Url);
        model.addAttribute("parent2", parent2 == null ? "" : parent2.getFirstName() + " " +parent2.getLastName());
        model.addAttribute("parent2Url", parent2 == null ? defaultUrl : parent2Url);

        model.addAttribute("hospital", student.getHospital());
        model.addAttribute("allergies", student.getAllergies());
        model.addAttribute("notes", student.getNotes());

        return "managers/attendance/studentDetails";
    }

    @GetMapping({"/parentDetails/{pId}/{sId}/{cId}/{selectType}/{tId}",
            "/parentDetails/{pId}/{sId}/{cId}/{selectType}/{tId}/{startDate}/{endDate}"})
    public String getParentDetails(@PathVariable(value = "pId") int pId,
                                   @PathVariable(value = "sId") int sId,
                                   @PathVariable(value = "cId") int cId,
                                   @PathVariable(value = "selectType") int selectType,
                                   @PathVariable(value = "tId") int tId,
                                   @PathVariable(value = "startDate", required = false) String sD,
                                   @PathVariable(value = "endDate", required = false) String eD,
                                   Model model) {

        Parent parent = parentService.findById(pId);
        Student student = studentService.findById(sId);
        AttendanceContainer container = attendanceContainerService.findById(cId);
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM-dd-yyyy");
        String date = df.format(container.getDate());
        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();
        String header = container.getTheClass().getName() + " on " + date + " > " +studentName+ " > " +
                parent.getFirstName() + " " +parent.getLastName();

        String backUrl = "/managers/attendance/studentDetails/"+sId+"/"+cId+"/"+selectType+"/"+tId;
        if(selectType == 3) {
            backUrl = backUrl + "/"+sD+"/"+eD;
        }

        model.addAttribute("headerText", "Attendance > View Record > "+header);
        model.addAttribute("backUrl", backUrl);
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

        return "managers/attendance/parentDetails";
    }

    @GetMapping("/exportOne/{cId}/{selectType}/{tId}")
    public String getExportOne(@PathVariable(value = "cId") int cId,
                               @PathVariable(value = "selectType") int selectType,
                               @PathVariable(value = "tId") int tId, Model model) {
        AttendanceContainer container = attendanceContainerService.findById(cId);
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM-dd-yyyy");
        String date = df.format(container.getDate());
        String header = "Attendance > View Record > " +container.getTheClass().getName() +
                " on " + date + " > Export";

        model.addAttribute("headerText", header);
        model.addAttribute("cId", cId);
        model.addAttribute("backUrl", "/managers/attendance/view/"+cId+"/"+selectType+"/"+tId);

        return "managers/attendance/attendanceExportOne";
    }

    @PostMapping("/exportCSVOne/{cId}")
    public ResponseEntity<InputStreamResource> downloadCSVFileOne(@PathVariable(value = "cId") int cId) throws Exception {
        AttendanceContainer container = attendanceContainerService.findById(cId);
        List<StudentAttendance> attendances = studentAttendanceService.getSAByContainerId(cId);
        attendances.sort(Comparator.comparing(s -> s.getStudent().getFirstName()));
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String date = df.format(container.getDate());
        String className = container.getTheClass().getName();

        String filename = "Report-" +className+ "-"+date+".csv";

        ByteArrayInputStream in = CSVHelper.attendanceToCSV(attendances, className, date, df.format(new Date(System.currentTimeMillis())));
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @PostMapping("/exportTextOne/{cId}")
    public ResponseEntity<InputStreamResource> downloadTextFileOne(@PathVariable(value = "cId") int cId) throws FileNotFoundException {

        AttendanceContainer container = attendanceContainerService.findById(cId);
        List<StudentAttendance> attendances = studentAttendanceService.getSAByContainerId(cId);
        attendances.sort(Comparator.comparing(s -> s.getStudent().getFirstName()));
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String date = df.format(container.getDate());
        String className = container.getTheClass().getName();

        String filename = "Report-" +className+ "-"+date+".txt";

        StringBuilder builder = new StringBuilder("Attendance Report for "+className+ " on " +date+"\n");
        builder.append("(Generated on " +df.format(new Date(System.currentTimeMillis()))+")\n\n");
        String content;

        for(StudentAttendance a : attendances) {
            String studentName = "";
            if((a.getStudent().getNickName() == null) || (a.getStudent().getNickName().isEmpty()))
                studentName = a.getStudent().getFirstName() + " " + a.getStudent().getLastName();
            else
                studentName = a.getStudent().getNickName() + " " + a.getStudent().getLastName();
            builder.append(studentName).append("\n");

            String present = "No";
            if(a.getPresent())
                present = "Yes";
            builder.append("\tPresent: ").append(present).append("\n");

            if(!(a.getPresent())) {
                String excused = "No";
                if(a.isExcused())
                    excused = "Yes";
                builder.append("\tExcused: ").append(excused).append("\n");
            }
            builder.append("\tNotes: ").append(a.getReason());

            builder.append("\n\n");
        }

        content = builder.toString();

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

    @GetMapping("/export") //Student
    public String getExport(Model model) {
        List<Student> students = studentService.findAll().stream()
                .sorted(Comparator.comparing(Student::getFirstName)).collect(Collectors.toList());

        model.addAttribute("studentList", students);
        model.addAttribute("chosenStudents", new ChosenStudentDTO(new HashSet<>()));

        return "managers/attendance/attendanceExport";
    }

    @PostMapping("/exportStudent") //Student
    public String postExportStudent(@ModelAttribute ChosenStudentDTO chosenStudentDTO, Model model) {

        Student student = null;

        boolean nullError = false;

        try {
            student = chosenStudentDTO.getStudents().iterator().next();
        } catch(NoSuchElementException ex) {
            nullError = true;
        }

        if((nullError) || (student == null)) {
            model.addAttribute("showBanner", true);
            model.addAttribute("errorText", "Please select a valid student.");
            List<Student> students = studentService.findAll().stream()
                    .sorted(Comparator.comparing(Student::getFirstName)).collect(Collectors.toList());

            model.addAttribute("studentList", students);
            model.addAttribute("chosenStudents", new ChosenStudentDTO(new HashSet<>()));

            return "managers/attendance/attendanceExport";
        }

        return "redirect:/managers/attendance/export2/"+student.getId();
    }

    @GetMapping("/export2/{sId}") //Class
    public String getExport2(@PathVariable(value = "sId") int sId, Model model) {
        Student student = studentService.findById(sId);
        if(student == null) {
            return "redirect:/managers/attendance/export";
        }

        List<Class> classes = classService.findClassesByStudentId(sId).stream()
                .sorted(Comparator.comparing(Class::getName)).collect(Collectors.toList());

        ChosenStudentDTO studentDTO = new ChosenStudentDTO(new HashSet<>());
        studentDTO.addStudent(student);

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        model.addAttribute("headerText", "Attendance > Report > " +studentName+ " > Select Class");
        model.addAttribute("classList", classes);
        model.addAttribute("chosenClasses", new ChosenClassDTO(new HashSet<>()));
        model.addAttribute("chosenStudents", studentDTO);
        return "managers/attendance/attendanceExport2";
    }

    @PostMapping("/export2") //Class
    public String postExport2(@ModelAttribute ChosenStudentDTO studentDTO,
                              @ModelAttribute ChosenClassDTO classDTO, Model model) {
        Student student = null;
        Class theClass = null;

        boolean nullError = false;
        try {
            student = studentDTO.getStudents().iterator().next();
        } catch(NoSuchElementException ex) {
            nullError = true;
        }
        if((nullError) || (student == null)) {
            model.addAttribute("showBanner", true);
            model.addAttribute("errorText", "Please select a valid student.");
            List<Student> students = studentService.findAll().stream()
                    .sorted(Comparator.comparing(Student::getFirstName)).collect(Collectors.toList());

            model.addAttribute("studentList", students);
            model.addAttribute("chosenStudents", new ChosenStudentDTO(new HashSet<>()));

            return "managers/attendance/attendanceExport";
        }

        boolean allClasses = (classDTO == null) || classDTO.isEmpty();

        try {
            theClass = classDTO.getClasses().iterator().next();
        } catch(NoSuchElementException ex) {
            allClasses = true;
        }
        try { //Failsafe
            for (Class c : classDTO.getClasses()) { //mark as all if any are null
                if (c == null)
                    allClasses = true;
                if (classService.findById(c.getId()) == null)
                    allClasses = true;
            }
        } catch(NullPointerException ex) {
            allClasses = true;
        }

        String url = "redirect:/managers/attendance/export3/"+student.getId()+"/";

        if(allClasses) {
            url = url + "-1";
        } else {
            url = url + theClass.getId();
        }

        return url;
    }

    @GetMapping("/export3/{sId}/{cId}") //Dates
    public String getExport3(@PathVariable(value = "sId") int sId,
                             @PathVariable(value = "cId") int cId, Model model) {

        Student student = studentService.findById(sId);
        Class theClass = null;
        ChosenClassDTO classDTO = new ChosenClassDTO(new HashSet<>());
        if(student == null) {
            return "redirect:/managers/attendance/-1";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        String headerText = "Attendance > Report > " + studentName + " > ";

        boolean allClasses = false;
        if(cId == -1) { //All classes
            headerText = headerText + "All Classes > Date Range";
            model.addAttribute("cId", -1);
            allClasses = true;
        } else {
            theClass = classService.findById(cId);
            if(theClass == null) {
                return "redirect:/managers/attendance/-1";
            }
            headerText = headerText + theClass.getName() + " > Date Range";
            model.addAttribute("cId", cId);
        }

        if(!allClasses) {
            SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");
            String startDate = df.format(theClass.getStartDate());
            String endDate = df.format(theClass.getEndDate());
            model.addAttribute("infoTextEnabled", true);
            model.addAttribute("infoText", " " + theClass.getName() + " occurs on "+theClass.getDayOfWeek() + "s from " +startDate+ " to " +endDate);
        }

        model.addAttribute("backUrl", "/managers/attendance/export2/"+sId);
        model.addAttribute("headerText", headerText);
        model.addAttribute("sId", sId);
        model.addAttribute("subUrl", "/managers/attendance/exportFinal/"+sId+"/"+cId);
        model.addAttribute("subUrlAll", "/managers/attendance/exportFinal/"+sId+"/"+cId+"/-1");

        return "managers/attendance/attendanceExport3";
    }

    @GetMapping({"/exportFinal/{sId}/{cId}", "/exportFinal/{sId}/{cId}/{allTime}"})
    public String exportFinal(@PathVariable(value = "allTime", required = false) Integer allTimeInt,
                                            @RequestParam(value = "startDate", required = false) String sD,
                                            @RequestParam(value = "endDate", required = false) String eD,
                                            @PathVariable(value = "sId") int sId,
                                            @PathVariable(value = "cId") int cId, Model model) {

        boolean startDateError = false;
        boolean endDateError = false;

        boolean allClasses = (cId == -1);
        boolean allTime = false;
        if(allTimeInt != null) {
            if(allTimeInt != 0) {
                allTime = true;
            }
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            startDate = LocalDate.parse(sD);
            endDate = LocalDate.parse(eD);
        } catch(NullPointerException ex) {
            //Probably hit all time...
            //Set to fake dates
            Date fakeStart = new Date(System.currentTimeMillis()-1000);
            Date fakeEnd = new Date(System.currentTimeMillis()+1000);
            startDate = convertToLocalDate(fakeStart);
            endDate = convertToLocalDate(fakeEnd);
        }

        Class theClass = null;
        if(!allClasses) {
            theClass = classService.findById(cId);
            if(theClass == null) { //Make sure selected class isn't null
                return "redirect:/managers/attendance/-1";
            }
        }

        //Check that student is valid
        Student student = null;
        student = studentService.findById(sId);
        if(student == null) {
            return "redirect:/managers/attendance/-1";
        }

        String studentName = "";
        if((student.getNickName() == null) || (student.getNickName().isEmpty()))
            studentName = student.getFirstName() + " " + student.getLastName();
        else
            studentName = student.getNickName() + " " + student.getLastName();

        SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");

        //Check date ranges are valid
        if(startDate.isAfter(endDate))
            startDateError = true;
        if(endDate.isBefore(startDate))
            endDateError = true;
        if(startDateError || endDateError) { //Errors
            model.addAttribute("startDateError", startDateError);
            model.addAttribute("endDateError", endDateError);
            model.addAttribute("sdVal", sD);
            model.addAttribute("edVal", eD);
            model.addAttribute("sId", sId);
            model.addAttribute("cId", cId);
            if(!allClasses) {
                String sdt = df.format(theClass.getStartDate());
                String edt = df.format(theClass.getEndDate());
                model.addAttribute("infoTextEnabled", true);
                model.addAttribute("infoText", " " + theClass.getName() + " occurs on "+theClass.getDayOfWeek() + "s from " +sdt+ " to " +edt);
            }


            String headerText = "Attendance > Report > " + studentName + " > ";
            if(cId == -1) { //All classes
                headerText = headerText + "All Classes > Date Range";
            } else {
                headerText = headerText + theClass.getName() + " > Date Range";
            }

            model.addAttribute("headerText", headerText);

            return "managers/attendance/attendanceExport3";
        }

        String headerText = "Attendance > Report > " + studentName + " > ";
        String timeText = "";
        if(allTime)
            timeText = "All Time";
        else
            timeText = "from " + df.format(convertLocalDate(startDate)) + " to " + df.format(convertLocalDate(endDate));

        if(allClasses) { //All classes
            headerText = headerText + "All Classes > " + timeText;

        } else {
            headerText = headerText + theClass.getName() + " (" +theClass.getTeacher().getFirstName()+ " " + theClass.getTeacher().getLastName() + ") > " + timeText;
        }

        Date sDate = convertLocalDate(startDate);
        Date eDate = convertLocalDate(endDate);

        //Generate List
        List<StudentAttendance> attendances = new ArrayList<>();

        if(allClasses) {
            List<Class> studentClasses = classService.findClassesByStudentId(sId);

            for(Class c : studentClasses) {
                List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(c.getId());
                for(AttendanceContainer a : containers) { //Get all containers for class
                    if (a.getTaken()) { //Only count containers marked as taken
                        List<StudentAttendance> at = studentAttendanceService.getSAByContainerId(a.getId()); //Get all attendances for this container
                        for (StudentAttendance sa : at) {
                            if (sa.getStudent().getId() == sId) { //Student found in this container
                                if (allTime) { //All time
                                    attendances.add(sa);
                                } else { //Date range
                                    if (((a.getDate().equals(sDate)) || (a.getDate().after(sDate))) &&
                                            ((a.getDate().equals(eDate)) || (a.getDate().before(eDate)))) {
                                        attendances.add(sa);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(cId);
            for (AttendanceContainer a : containers) { //Get all containers for this class
                if (a.getTaken()) { //Only count containers marked as taken
                    List<StudentAttendance> at = studentAttendanceService.getSAByContainerId(a.getId());
                    for (StudentAttendance sa : at) { //Get all attendance records in this container
                        if (sa.getStudent().getId() == sId) { //Student found in this container
                            if (allTime) { //All time
                                attendances.add(sa);
                            } else { //Date range
                                if (((a.getDate().equals(sDate)) || (a.getDate().after(sDate))) &&
                                        ((a.getDate().equals(eDate)) || (a.getDate().before(eDate)))) {
                                    attendances.add(sa);
                                }
                            }
                        }
                    }
                }
            }
        }

        //Calculate attendance percentage
        String stats = getStats(attendances, student);

        String exportTextUrl = "/managers/attendance/exportText/"+sId+"/"+cId;
        String exportCSVUrl  = "/managers/attendance/exportCSV/"+sId+"/"+cId;
        if(!allTime) {
            exportTextUrl = exportTextUrl + "/"+sD+"/"+eD;
            exportCSVUrl = exportCSVUrl + "/"+sD+"/"+eD;
        }

        model.addAttribute("headerText", headerText);
        model.addAttribute("stats", stats);
        model.addAttribute("attendanceList", attendances);
        model.addAttribute("allClasses", allClasses);
        model.addAttribute("none", attendances.isEmpty());
        model.addAttribute("backUrl", "/managers/attendance/export3/"+sId+"/"+cId);
        model.addAttribute("exportTextUrl", exportTextUrl);
        model.addAttribute("exportCSVUrl", exportCSVUrl);

        return "managers/attendance/attendanceExportFinal";
    }

    private static String getStats(List<StudentAttendance> attendances, Student student) {
        int total = attendances.size();
        int present = 0;
        int excused = 0;
        for(StudentAttendance a : attendances) {
            if(a.getPresent())
                present++;
            else if(a.isExcused())
                excused++;
        }
        int presentPercent = total == 0 ? 0 : present * 100 / total;
        int excusedPercent = total == 0 ? 0 : (present + excused) * 100 / total;
        int missed = total - present;
        int missedExcused = total - present - excused;
        String name = student.getFirstName();
        if(!student.getNickName().isEmpty())
            name = student.getNickName();

        return "Out of "+total+" class session" + (total == 1 ? "" : "s") + ", " + name + " is present " +presentPercent+ "% of the time ("+excusedPercent+"% when excused), missing " +missed+ " class" + (missed == 1 ? "" : "es" ) + " ("+missedExcused+" when excused).";
    }

    @PostMapping({"/exportCSV/{sId}/{cId}/{startDate}/{endDate}", "/exportCSV/{sId}/{cId}"})
    public ResponseEntity<InputStreamResource> downloadCSVFile(@PathVariable(value = "sId") int sId,
                                                               @PathVariable(value = "cId") int cId,
                                                               @PathVariable(value = "startDate", required = false) String sD,
                                                               @PathVariable(value = "endDate", required = false) String eD) throws Exception {

        boolean allTime = false;
        boolean allClasses = false;
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");

        //Setup dates, determine if range or all time
        LocalDate startDate = null;
        LocalDate endDate = null;
        try {
            startDate = LocalDate.parse(sD);
            endDate = LocalDate.parse(eD);
        } catch(NullPointerException ex) {
            //Probably hit all time...
            allTime = true;
            //Set fake dates
            startDate = convertToLocalDate(new Date(System.currentTimeMillis()-1000));
            endDate = convertToLocalDate(new Date(System.currentTimeMillis()+1000));
        }
        if(cId == -1)
            allClasses = true;

        //Get student name
        String name = "";
        Student student = studentService.findById(sId);
        try {
            if((student.getNickName() == null) || (student.getNickName().isEmpty()))
                name = student.getFirstName() + " " + student.getLastName();
            else
                name = student.getNickName() + " " + student.getLastName();
        } catch(NullPointerException ex) {
            //Stop
        }

        Class theClass = null;

        //Get class name
        String classes = "";
        if(allClasses)
            classes = "All Classes";
        else {
            theClass = classService.findById(cId);
            try {
                classes = theClass.getName();
            } catch(NullPointerException ex) {
                classes = "???";
            }
        }

        //Get date string
        String dateStr = "";
        if(allTime) {
            dateStr = "All-Time";
        } else {
            dateStr = df.format(convertLocalDate(startDate)) + "-to-" + df.format(convertLocalDate(endDate));
        }
        String now = df.format(new Date(System.currentTimeMillis()));

        String filename = "Report-"+name+"-"+classes+"-"+dateStr+"-("+now+").csv";

        //Generate List
        List<StudentAttendance> attendances = new ArrayList<>();
        Date sDate = convertLocalDate(startDate);
        Date eDate = convertLocalDate(endDate);

        if(allClasses) {
            List<Class> studentClasses = classService.findClassesByStudentId(sId);

            for(Class c : studentClasses) {
                List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(c.getId());
                for(AttendanceContainer a : containers) { //Get all containers for class
                    if (a.getTaken()) { //Only count containers marked as taken
                        List<StudentAttendance> at = studentAttendanceService.getSAByContainerId(a.getId()); //Get all attendances for this container
                        for (StudentAttendance sa : at) {
                            if (sa.getStudent().getId() == sId) { //Student found in this container
                                if (allTime) { //All time
                                    attendances.add(sa);
                                } else { //Date range
                                    if (((a.getDate().equals(sDate)) || (a.getDate().after(sDate))) &&
                                            ((a.getDate().equals(eDate)) || (a.getDate().before(eDate)))) {
                                        attendances.add(sa);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(cId);
            for (AttendanceContainer a : containers) { //Get all containers for this class
                if (a.getTaken()) { //Only count containers marked as taken
                    List<StudentAttendance> at = studentAttendanceService.getSAByContainerId(a.getId());
                    for (StudentAttendance sa : at) { //Get all attendance records in this container
                        if (sa.getStudent().getId() == sId) { //Student found in this container
                            if (allTime) { //All time
                                attendances.add(sa);
                            } else { //Date range
                                if (((a.getDate().equals(sDate)) || (a.getDate().after(sDate))) &&
                                        ((a.getDate().equals(eDate)) || (a.getDate().before(eDate)))) {
                                    attendances.add(sa);
                                }
                            }
                        }
                    }
                }
            }
        }

        //Generate nice list to send to CSV exporter
        List<AttendanceExportDTO> exportList = new ArrayList<>();
        if(allClasses) { //All classes
            List<Class> classList = classService.findClassesByStudentId(sId);
            for(Class c : classList) {
                for(StudentAttendance a : attendances) {
                    AttendanceExportDTO ex = new AttendanceExportDTO();
                    AttendanceContainer container = attendanceContainerService.findById(a.getContainer().getId());
                    if(container.getTheClass().equals(c)) {
                        ex.setTheClass(container.getTheClass());
                        ex.setDate(container.getDate());
                        ex.addAttendance(a);
                        exportList.add(ex);
                    }
                }
            }
        } else { //One class
            for(StudentAttendance a : attendances) {
                AttendanceExportDTO ex = new AttendanceExportDTO();
                ex.setTheClass(theClass);
                ex.setDate(attendanceContainerService.findById(a.getContainer().getId()).getDate());
                ex.addAttendance(a);
                exportList.add(ex);
            }
        }

        //Sort by class, then date
        Comparator<AttendanceExportDTO> comparator = Comparator.comparing(c -> c.getTheClass().getName());
        comparator = comparator.thenComparing(Comparator.comparing(AttendanceExportDTO::getDate));
        exportList.sort(comparator);

        //Send to CSV exporter
        ByteArrayInputStream in = CSVHelper.attendanceToCSVFromContainer(exportList, name, classes, allTime,
                convertLocalDate(startDate), convertLocalDate(endDate));
        InputStreamResource file = new InputStreamResource(in);


        //Download
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @PostMapping({"/exportText/{sId}/{cId}/{startDate}/{endDate}", "/exportText/{sId}/{cId}"})
    public ResponseEntity<InputStreamResource> downloadTextFile(@PathVariable(value = "sId") int sId,
                                                                @PathVariable(value = "cId") int cId,
                                                                @PathVariable(value = "startDate", required = false) String sD,
                                                                @PathVariable(value = "endDate", required = false) String eD) throws FileNotFoundException {

        boolean allTime = false;
        boolean allClasses = false;
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");

        //Setup dates, determine if range or all time
        LocalDate startDate = null;
        LocalDate endDate = null;
        try {
            startDate = LocalDate.parse(sD);
            endDate = LocalDate.parse(eD);
        } catch(NullPointerException ex) {
            //Probably hit all time...
            allTime = true;
            //Set fake dates
            startDate = convertToLocalDate(new Date(System.currentTimeMillis()-1000));
            endDate = convertToLocalDate(new Date(System.currentTimeMillis()+1000));
        }
        if(cId == -1)
            allClasses = true;

        //Get student name
        String name = "";
        Student student = studentService.findById(sId);
        try {
            if((student.getNickName() == null) || (student.getNickName().isEmpty()))
                name = student.getFirstName() + " " + student.getLastName();
            else
                name = student.getNickName() + " " + student.getLastName();
        } catch(NullPointerException ex) {
            //Stop
        }

        Class theClass = null;

        //Get class name
        String classes = "";
        if(allClasses)
            classes = "All Classes";
        else {
            theClass = classService.findById(cId);
            try {
                classes = theClass.getName();
            } catch(NullPointerException ex) {
                classes = "???";
            }
        }

        //Get date string
        String dateStr = "";
        if(allTime) {
            dateStr = "All-Time";
        } else {
            dateStr = df.format(convertLocalDate(startDate)) + " -to- " + df.format(convertLocalDate(endDate));
        }
        String now = df.format(new Date(System.currentTimeMillis()));

        String filename = "Report-"+name+"-"+classes+"-"+dateStr+"-("+now+").txt";

        //Generate List
        List<StudentAttendance> attendances = new ArrayList<>();
        Date sDate = convertLocalDate(startDate);
        Date eDate = convertLocalDate(endDate);

        StringBuilder builder = new StringBuilder("Attendance Report for "+name+" - "+classes+" - Dates: "+dateStr+"\nGenerated on "+now+"\n\n");
        String content;

        if(allClasses) {
            List<Class> studentClasses = classService.findClassesByStudentId(sId);

            for(Class c : studentClasses) {
                List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(c.getId());
                for(AttendanceContainer a : containers) { //Get all containers for class
                    if (a.getTaken()) { //Only count containers marked as taken
                        List<StudentAttendance> at = studentAttendanceService.getSAByContainerId(a.getId()); //Get all attendances for this container
                        for (StudentAttendance sa : at) {
                            if (sa.getStudent().getId() == sId) { //Student found in this container
                                if (allTime) { //All time
                                    attendances.add(sa);
                                } else { //Date range
                                    if (((a.getDate().equals(sDate)) || (a.getDate().after(sDate))) &&
                                            ((a.getDate().equals(eDate)) || (a.getDate().before(eDate)))) {
                                        attendances.add(sa);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            List<AttendanceContainer> containers = attendanceContainerService.getContainerByClassId(cId);
            for (AttendanceContainer a : containers) { //Get all containers for this class
                if (a.getTaken()) { //Only count containers marked as taken
                    List<StudentAttendance> at = studentAttendanceService.getSAByContainerId(a.getId());
                    for (StudentAttendance sa : at) { //Get all attendance records in this container
                        if (sa.getStudent().getId() == sId) { //Student found in this container
                            if (allTime) { //All time
                                attendances.add(sa);
                            } else { //Date range
                                if (((a.getDate().equals(sDate)) || (a.getDate().after(sDate))) &&
                                        ((a.getDate().equals(eDate)) || (a.getDate().before(eDate)))) {
                                    attendances.add(sa);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<AttendanceExportDTO> exportList = new ArrayList<>();
        if(allClasses) { //All classes
            List<Class> classList = classService.findClassesByStudentId(sId);
            for(Class c : classList) {
                for(StudentAttendance a : attendances) {
                    AttendanceExportDTO ex = new AttendanceExportDTO();
                    AttendanceContainer container = attendanceContainerService.findById(a.getContainer().getId());
                    if(container.getTheClass().equals(c)) {
                        ex.setTheClass(container.getTheClass());
                        ex.setDate(container.getDate());
                        ex.addAttendance(a);
                        exportList.add(ex);
                    }
                }
            }
        } else { //One class
            for(StudentAttendance a : attendances) {
                AttendanceExportDTO ex = new AttendanceExportDTO();
                ex.setTheClass(theClass);
                ex.setDate(attendanceContainerService.findById(a.getContainer().getId()).getDate());
                ex.addAttendance(a);
                exportList.add(ex);
            }
        }

        //Sort by class, then date
        Comparator<AttendanceExportDTO> comparator = Comparator.comparing(c -> c.getTheClass().getName());
        comparator = comparator.thenComparing(Comparator.comparing(AttendanceExportDTO::getDate));
        exportList.sort(comparator);

        //Print
        Class tempClass = new Class();
        for(AttendanceExportDTO d : exportList) {
            //New header for each class
            if(!tempClass.equals(d.getTheClass())) {
                //builder.append("\n");

                builder.append(("-=" + d.getTheClass().getName() + "=-").toUpperCase(locale)).append("\n");
                builder.append(("-=" + d.getTheClass().getDayOfWeek() + "s=-").toUpperCase(locale)).append("\n");

                tempClass = d.getTheClass();
            }

            //Add actual data
            for(StudentAttendance a :d.getAttendances()) {

                builder.append("Date: ").append(df.format(d.getDate())).append("\n");
                String present = "No";
                if(a.getPresent())
                    present = "Yes";
                builder.append("\tPresent: ").append(present).append("\n");

                if(!(a.getPresent())) {
                    String excused = "No";
                    if(a.isExcused())
                        excused = "Yes";
                    builder.append("\tExcused: ").append(excused).append("\n");
                }
                builder.append("\tNotes: ").append(a.getReason());

                builder.append("\n\n");
            }
        }

        content = builder.toString();

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

    private LocalDate convertToLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date convertLocalDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
