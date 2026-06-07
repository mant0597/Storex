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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

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
            throws Exception {

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        String objectName =
                UUID.randomUUID()
                        + "_" +
                        file.getOriginalFilename();
        metadata.setStoragePath(
                objectName
        );
        fileService.saveFile(file,objectName);
        fileService.save(metadata);
        return "Uploaded Successfully";
    }
    @GetMapping
    public List<FileMetadata> getAllFiles() {
        return fileService.getAllFiles();
    }
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long id) throws Exception {

        FileMetadata metadata =
                fileService.getMetadata(id);

        InputStream stream =
                fileService.downloadFile(
                        metadata.getStoragePath()
                );

        byte[] content = stream.readAllBytes();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                metadata.getFileName() + "\""
                )
                .body(content);
    }
    @DeleteMapping("/{id}")
    public String DeleteFie(@PathVariable Long id) throws Exception{
        fileService.deleteFile(id);
        return "Deleted Successfully";
    }
}

