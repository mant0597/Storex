package com.storex.storex.service;

import com.storex.storex.entity.FileMetadata;
import com.storex.storex.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

@Service
public class FileService {
    private final FileMetadataRepository repository;
    public FileService(FileMetadataRepository repository){
        this.repository=repository;
    }
    public FileMetadata save(FileMetadata file){
        return repository.save(file);
    }
}
