package de.doubleslash.spring.introduction.model;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

public record MinIoFileHandler() {
    public static void uploadFile(String bucketName, InputStream imageStream, Long imageSize, String fileExtension) throws IOException, NoSuchAlgorithmException, InvalidKeyException, ServerException,
            InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException,
            InternalException {
        // Create a minioClient with the local MinIO server, its access key and secret key.
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("http://127.0.0.1:9000")
                        .credentials("root", "password")
                        .build();

        // Make bucket if non-existent
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(buildFileName(fileExtension))
                        .stream(imageStream, imageSize, -1)
                        .build());

    }

    private static String buildFileName(String fileExtension) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int random = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        String currentDateTime = LocalDateTime.now().toString();

        String message = "%d".formatted(random) + currentDateTime;
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

        return Hex.toHexString(hash) + "." + fileExtension;
    }
}
