package de.doubleslash.spring.introduction.model;

import de.doubleslash.spring.introduction.config.MinioConfiguration;
import io.minio.*;
import io.minio.errors.MinioException;
import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MinioFileHandler implements BlobStoreFileHandler {
    private final MinioClient minioClient;

    public MinioFileHandler(MinioConfiguration configuration) {
        this.minioClient = getMinioClient(configuration);
    }

    @NotNull
    private static MinioClient getMinioClient(MinioConfiguration configuration) {
        return MinioClient.builder().endpoint(configuration.getEndpoint()).credentials(configuration.getUsername(),
                configuration.getPassword()).build();
    }

    private void makeBucketIfNotExists(String minioBucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
        }
    }

    private void continueIfBucketExistsOrThrow(String minioBucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build())) {
            throw new MinioException("Something went wrong.");
        }
    }

    @Override
    public String buildFilename(String fileExtension, MessageDigest digest) {
        int random = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        String currentDateTime = LocalDateTime.now().toString();

        String message = "%d".formatted(random) + currentDateTime;
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

        return Hex.toHexString(hash) + "." + fileExtension;
    }

    @Override
    public String uploadFile(InputStream fileStream, Long fileSize, String fileExtension, String bucketName)
            throws Exception {

        makeBucketIfNotExists(bucketName);

        String filename = buildFilename(fileExtension, MessageDigest.getInstance("SHA256"));

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
                GetObjectArgs.builder().bucket(filename).object(filename).build()
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
