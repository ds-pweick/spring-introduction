package de.doubleslash.spring.introduction.model;

import de.doubleslash.spring.introduction.config.FileHandlerConfiguration;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;


@Profile("!test")
@Service
@Slf4j
public class MinioFileHandler implements BlobStoreFileHandler {
    private final MinioClient minioClient;

    public MinioFileHandler(FileHandlerConfiguration configuration) {
        this.minioClient = getMinioClient(configuration);
    }

    @NotNull
    private static MinioClient getMinioClient(FileHandlerConfiguration configuration) {
        log.info(configuration.getEndpoint());

        return MinioClient.builder().endpoint(configuration.getEndpoint()).credentials(configuration.getUsername(),
                configuration.getPassword()).build();
    }

    private void makeBucketIfNotExists(String minioBucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
            log.info("Minio client created bucket %s".formatted(minioBucket));
        }
    }

    private void continueIfBucketExistsOrThrow(String minioBucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build())) {
            throw new MinioException("Bucket %s doesn't exist.".formatted(minioBucket));
        }
    }

    @Override
    public String uploadFile(InputStream fileStream, Long fileSize, String fileExtension, String bucketName)
            throws Exception {

        makeBucketIfNotExists(bucketName);

        String filename = buildUniqueFilename(fileExtension, MessageDigest.getInstance("SHA256"));

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(fileStream, fileSize, -1)
                        .build());

        return filename;
    }

    @Override
    public byte[] downloadFile(String filename, String bucketName)
            throws Exception {
        continueIfBucketExistsOrThrow(bucketName);

        InputStream fileStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(filename).build()
        );
        byte[] fileData = fileStream.readAllBytes();
        fileStream.close();

        return fileData;
    }

    @Override
    public void deleteMultiple(List<String> filenameList, String bucketName) throws Exception {
        continueIfBucketExistsOrThrow(bucketName);

        for (String filename : filenameList) {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filename).build());
        }
    }
}
