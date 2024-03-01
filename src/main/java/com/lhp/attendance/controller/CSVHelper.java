package com.lhp.attendance.controller;

import com.lhp.attendance.dto.AttendanceExportDTO;
import com.lhp.attendance.entity.*;
import com.lhp.attendance.entity.Class;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CSVHelper {

    /////////////////////////////////////////////////
    // Change Locale and CSVFormat to your liking. //
    /////////////////////////////////////////////////
    private static final Locale LOCALE = Locale.US;
    private static final CSVFormat FORMAT = CSVFormat.EXCEL;

    public static ByteArrayInputStream attendanceToCSV(List<StudentAttendance> attendances, String className, String date, String today) {

        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), FORMAT);) {

            //Header
            List<String> header = Arrays.asList(
                    "ATTENDANCE", "CLASS: "+className, "DATE: "+date, "GENERATED ON: "+today
            );
            csvPrinter.printRecord(header);

            header = List.of("");
            csvPrinter.printRecord(header);

            header = Arrays.asList(
                    "NAME", "PRESENT", "EXCUSED", "NOTES"
            );
            csvPrinter.printRecord(header);

            for(StudentAttendance a : attendances) {
                String studentName;
                if((a.getStudent().getNickName() == null) || (a.getStudent().getNickName().isEmpty()))
                    studentName = a.getStudent().getFirstName() + " " +a.getStudent().getLastName();
                else
                    studentName = a.getStudent().getNickName() + " " +a.getStudent().getLastName();
                List<String> data = Arrays.asList(
                        studentName,
                        Boolean.parseBoolean(String.valueOf(a.getPresent())) ? "YES" : "NO",
                        Boolean.parseBoolean(String.valueOf(a.getPresent())) ?
                                "-" : (Boolean.parseBoolean(String.valueOf(a.isExcused())) ? "YES" : "NO"),
                        String.valueOf(a.getReason())
                );

                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch(IOException e) {
            throw new RuntimeException("Failed to import data to CSV file: "+e.getMessage());
        }
    }

    public static ByteArrayInputStream contactsToCSV(int type, List<?> list, String className) {

        final CSVFormat format = CSVFormat.EXCEL;
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String today = df.format(new Date(System.currentTimeMillis()));
        String typeText = "Parents";
        switch(type) {
            case 2 -> typeText = "Teachers";
            case 3 -> typeText = "Students";
        }

        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {

            List<String> header;
            if(!className.isEmpty()) {
                header = Arrays.asList("Contact List", typeText, today, className);
            } else {
                header = Arrays.asList("Contact List", typeText, today);
            }
            csvPrinter.printRecord(header);
            header = Arrays.asList("", "", "");
            csvPrinter.printRecord(header);
            header = Arrays.asList("NAME", "PHONE", (type == 3 ? "" : "EMAIL"));
            csvPrinter.printRecord(header);
            for(Object p : list) {
                switch(type) {
                    case 1 -> {
                        List<String> entry = Arrays.asList(
                                ((Parent)p).getFirstName() + " " + ((Parent)p).getLastName(),
                                ((Parent)p).getPhone(), ((Parent)p).getEmail());
                        csvPrinter.printRecord(entry);
                    }
                    case 2 -> {
                        List<String> entry = Arrays.asList(
                                ((Teacher)p).getFirstName() + " " + ((Teacher)p).getLastName(),
                                ((Teacher)p).getPhone(), ((Teacher)p).getEmail());
                        csvPrinter.printRecord(entry);
                    }
                    case 3 -> {
                        String fName = ((Student)p).getFirstName();
                        if(!((Student) p).getNickName().isEmpty())
                            fName = ((Student)p).getNickName();
                        List<String> entry = Arrays.asList(
                                fName+ " " + ((Student)p).getLastName(),
                                ((Student)p).getPhone(), "");
                        csvPrinter.printRecord(entry);
                    }
                }
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch(IOException ex) {
            throw new RuntimeException("Failed to export data to CSV file - "+ex.getMessage());
        }
    }

    public static ByteArrayInputStream attendanceToCSVFromContainer(List<AttendanceExportDTO> dto, String studentName,
                                                                    String className, boolean allTime,
                                                                    Date startDate, Date endDate) {
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");

        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), FORMAT);) {

            String now = df.format(new Date(System.currentTimeMillis()));

            String range = "All Time";
            if(!allTime) {
                range = df.format(startDate) + " to " +df.format(endDate);
            }

            //Header
            List<String> header = Arrays.asList(
                    "Attendance Report", "Student: "+studentName, "Class: "+className, "Dates: "+range, "Generated on: "+now
            );
            csvPrinter.printRecord(header);

            header = Arrays.asList(
                    "", "", "", "", ""
            );
            csvPrinter.printRecord(header);

            header = Arrays.asList(
                    "DATE", "PRESENT", "EXCUSED", "NOTES"
            );
            csvPrinter.printRecord(header);

            Class tempClass = new Class();
            for(AttendanceExportDTO d : dto) {
                //New header for each class
                if(!tempClass.equals(d.getTheClass())) {
                    List<String> spacer = List.of("");
                    csvPrinter.printRecord(spacer);

                    List<String> data = Arrays.asList(
                            ("-="+d.getTheClass().getName()+"=-").toUpperCase(LOCALE),
                            ("-="+d.getTheClass().getDayOfWeek()+"s=-").toUpperCase(LOCALE)
                    );
                    csvPrinter.printRecord(data);
                    tempClass = d.getTheClass();
                }

                //Add actual data
                for(StudentAttendance a :d.getAttendances()) {
                    List<String> data = Arrays.asList(
                            df.format(d.getDate()),
                            Boolean.parseBoolean(String.valueOf(a.getPresent())) ? "YES" : "NO",
                            Boolean.parseBoolean(String.valueOf(a.getPresent())) ?
                                    "-" : (Boolean.parseBoolean(String.valueOf(a.isExcused())) ? "YES" : "NO"),
                            String.valueOf(a.getReason())
                    );
                    csvPrinter.printRecord(data);
                }
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch(IOException e) {
            throw new RuntimeException("Failed to export data to CSV file: "+e.getMessage());
        }
    }
}
