package com.storex.storex.controller;

import com.storex.storex.dto.FileResponseDto;
import com.storex.storex.dto.FileUploadRequest;
import com.storex.storex.entity.FileMetadata;
import com.storex.storex.service.FileService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
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
    public String uploadFile(@RequestParam("file") MultipartFile file, @Valid @ModelAttribute FileUploadRequest request)
            throws Exception {

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setUploadedAt(LocalDateTime.now());
        metadata.setDescription(
                request.getDescription()
        );

        // Resolve logged-in username programmatically from the security context
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        metadata.setUploadedBy(currentUsername);

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
    public Page<FileResponseDto> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Sort sort = direction.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
                
        Pageable pageable = PageRequest.of(page, size, sort);
        return fileService.getAllFilesDto(currentUsername, pageable);
    }
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long id) throws Exception {

        FileMetadata metadata =
                fileService.getMetadata(id);

        // Check if current user owns the file
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals(metadata.getUploadedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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
    public ResponseEntity<String> DeleteFie(@PathVariable Long id) throws Exception{
        FileMetadata metadata = fileService.getMetadata(id);

        // Check if current user owns the file
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals(metadata.getUploadedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: You do not own this file");
        }

        fileService.deleteFile(id);
        return ResponseEntity.ok("Deleted Successfully");
    }
}


