package de.doubleslash.spring.introduction.model;

import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

public interface BlobStoreFileHandler {
    default String buildUniqueFilename(String fileExtension, MessageDigest digest) {
        int random = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        String currentDateTime = LocalDateTime.now().toString();

        String message = "%d".formatted(random) + currentDateTime;
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

        return Hex.toHexString(hash) + "." + fileExtension;
    }

    String uploadFile(InputStream fileStream, @Nullable Long fileSize,
                      String fileExtension, String bucketName) throws Exception;

    byte[] downloadFile(String filename, String bucketName) throws Exception;

    void deleteMultiple(List<String> filenameList, String bucketName) throws Exception;
}
