package com.lhp.attendance.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class TextFileExporter implements FileExporter {

    @Override
    public Path export(String fileContent, String fileName) {
        Path filePath = Paths.get(fileName);
        try {
            Path exportedFile = Files.write(filePath, fileContent.getBytes(), StandardOpenOption.CREATE);
            return exportedFile;
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
