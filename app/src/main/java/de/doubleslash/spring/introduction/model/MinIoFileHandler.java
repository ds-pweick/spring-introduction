package de.doubleslash.spring.introduction.model;

import de.doubleslash.spring.introduction.config.MinIoConfiguration;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MinIoFileHandler {

    private final MinioClient minioClient;

    public MinIoFileHandler(MinIoConfiguration configuration) {
        this.minioClient = getMinioClient(configuration);
    }

    @NotNull
    private static MinioClient getMinioClient(MinIoConfiguration configuration) {
        return MinioClient.builder().endpoint(configuration.getEndpoint()).credentials(configuration.getUsername(),
                configuration.getPassword()).build();
    }

    private static String buildFilename(String fileExtension) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int random = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        String currentDateTime = LocalDateTime.now().toString();

        String message = "%d".formatted(random) + currentDateTime;
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

        return Hex.toHexString(hash) + "." + fileExtension;
    }

    public String uploadFile(String bucketName, InputStream fileStream, Long fileSize, String fileExtension)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, ServerException,
            InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException,
            InternalException {

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        String filename = buildFilename(fileExtension);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(fileStream, fileSize, -1)
                        .build());

        return filename;
    }

    public ByteArrayResource downloadFile(String bucketName, String filename)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            throw new MinioException("Something went wrong.");
        }

        InputStream fileStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(filename).build()
        );
        byte[] fileData = fileStream.readAllBytes();
        fileStream.close();

        return new ByteArrayResource(fileData);
    }

    public void deleteFile(String bucketName, String filename) throws MinioException,
            IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            throw new MinioException("Something went wrong.");
        }

        log.info("Removing file %s".formatted(filename));

        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filename).build());
    }

    public void deleteMultiple(String bucketName, List<String> filenameList) throws MinioException,
            IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            throw new MinioException("Something went wrong.");
        }

        for (String filename : filenameList) {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filename).build());
        }
    }
}
