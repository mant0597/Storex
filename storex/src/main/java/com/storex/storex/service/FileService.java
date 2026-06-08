package com.storex.storex.service;
import com.storex.storex.entity.FileMetadata;
import com.storex.storex.exception.FileNotFoundException;
import com.storex.storex.repository.FileMetadataRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.List;

@Service
public class FileService {
    private final MinioClient minioClient;

    private final FileMetadataRepository repository;

    public FileService(
            FileMetadataRepository repository,
            MinioClient minioClient
    ) {
        this.repository = repository;
        this.minioClient = minioClient;
    }

    // Save metadata in DB
    public FileMetadata save(FileMetadata file) {
        return repository.save(file);
    }

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void saveFile(MultipartFile file,String objectName) throws Exception {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(
                                file.getInputStream(),
                                file.getSize(),
                                -1
                        )
                        .contentType(file.getContentType())
                        .build()
        );
    }
    public List<FileMetadata> getAllFiles() {
        return repository.findAll();
    }
    public Resource getFile(Long id) throws Exception {

        FileMetadata metadata =
                repository.findById(id)
                        .orElseThrow(() ->
                                new FileNotFoundException(
                                        "File not found with id " + id
                                )
                        );

        InputStream stream =
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket("storex-files")
                                .object(metadata.getFileName())
                                .build()
                );

        return new InputStreamResource(stream);
    }
    public FileMetadata getMetadata(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new FileNotFoundException(
                        "File not found with id " + id
                )
        );
    }
    public InputStream downloadFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder() .bucket(bucketName) .object(objectName) .build() );
    }
    public void deleteFile(Long id) throws Exception{
         FileMetadata objectName=repository.findById(id).orElseThrow(() ->
                 new FileNotFoundException(
                         "File not found with id " + id
                 )
         );
         minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName.getStoragePath()).build());
        repository.deleteById(id);
    }
}
