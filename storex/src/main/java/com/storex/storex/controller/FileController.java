package com.storex.storex.controller;


import com.storex.storex.entity.FileMetadata;
import com.storex.storex.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    public FileController(FileService fileService){
        this.fileService=fileService;
    }
    @PostMapping("/test")
    public String testUpload() {

        FileMetadata file = new FileMetadata();

        file.setFileName("resume.pdf");
        file.setFileType("application/pdf");
        file.setStoragePath("uploads/resume.pdf");

        fileService.save(file);

        return "Saved Successfully";
    }
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {

        return "Received: " + file.getOriginalFilename()
                + " Size: " + file.getSize();
    }
}

