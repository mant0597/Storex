package com.storex.storex.controller;


import com.storex.storex.entity.FileMetadata;
import com.storex.storex.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

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
    public String uploadFile(@RequestParam("file") MultipartFile file)
            throws IOException {

        fileService.saveFile(file);

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setStoragePath("uploads/" + file.getOriginalFilename());

        fileService.save(metadata);

        return "Uploaded Successfully";
    }
    @GetMapping
    public List<FileMetadata> getAllFiles() {
        return fileService.getAllFiles();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id)
            throws MalformedURLException {

        Resource resource = fileService.getFile(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() + "\""
                )
                .body(resource);
    }
}

