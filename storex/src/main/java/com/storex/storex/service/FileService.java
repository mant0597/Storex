package com.storex.storex.service;

import com.storex.storex.entity.FileMetadata;
import com.storex.storex.repository.FileMetadataRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileService {

    private final FileMetadataRepository repository;

    public FileService(FileMetadataRepository repository) {
        this.repository = repository;
    }

    // Save metadata in DB
    public FileMetadata save(FileMetadata file) {
        return repository.save(file);
    }

    // Save actual file on disk
    public void saveFile(MultipartFile file) throws IOException {

        String uploadDir = "uploads/";

        Path path = Paths.get(uploadDir);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        Files.copy(
                file.getInputStream(),
                path.resolve(file.getOriginalFilename()),
                StandardCopyOption.REPLACE_EXISTING
        );
    }
    public List<FileMetadata> getAllFiles() {
        return repository.findAll();
    }
    public Resource getFile(Long id) throws MalformedURLException {

        FileMetadata metadata =
                repository.findById(id).orElseThrow();

        Path path = Paths.get(metadata.getStoragePath());

        return new UrlResource(path.toUri());
    }
}
