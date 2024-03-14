package de.doubleslash.spring.introduction.model;

import io.minio.*;
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
import java.util.HashMap;
import java.util.Map;

public record MinIoFileHandler() {
    private static final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://spring-introduction-minio:9000")
                    .credentials("root", "password")
                    .build();

    public static void uploadFile(String bucketName, InputStream imageStream, Long imageSize, String fileExtension, Long belongsToCarId)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, ServerException,
            InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException,
            InternalException {

        // Make bucket if non-existent
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        String filename = buildFilename(fileExtension);
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("id", belongsToCarId.toString());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(imageStream, imageSize, -1)
                        .build());

        minioClient.setObjectTags(
                SetObjectTagsArgs.builder().bucket(bucketName).object(filename).tags(tagMap).build());

    }

    private static String buildFilename(String fileExtension) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int random = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        String currentDateTime = LocalDateTime.now().toString();

        String message = "%d".formatted(random) + currentDateTime;
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

        return Hex.toHexString(hash) + "." + fileExtension;
    }
}
