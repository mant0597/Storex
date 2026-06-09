package com.storex.storex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FileUploadRequest {
    @NotBlank(message = "Description cannot be empty")
    @Size(max = 100)
    private String description;

    @NotBlank(message = "Uploaded By cannot be empty")
    @Size(max = 50)
    private String uploadedBy;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
