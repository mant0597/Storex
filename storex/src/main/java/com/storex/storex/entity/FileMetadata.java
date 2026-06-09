package com.storex.storex.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String storagePath;
    private String description;
    private String uploadedBy;
    public FileMetadata() {
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description=description;
    }
    public String getUploadedBy(){
        return uploadedBy;
    }
    public void setUploadedBy(String uploadedBy){
        this.uploadedBy=uploadedBy;
    }
    public Long getId() {
        return id;
    }
    public String getFileName() {
        return fileName;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}
