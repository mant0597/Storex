package com.storex.storex.repository;

import com.storex.storex.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Page<FileMetadata> findByUploadedBy(String uploadedBy, Pageable pageable);
}

