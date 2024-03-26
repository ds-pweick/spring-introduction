package de.doubleslash.spring.introduction.model;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("test")
public class InMemoryFileHandler implements BlobStoreFileHandler {

    private final Map<String, byte[]> blobStore = new HashMap<>();

    @Override
    public String buildFilename(String fileExtension, MessageDigest digest) {
        int random = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        String currentDateTime = LocalDateTime.now().toString();

        String message = "%d".formatted(random) + currentDateTime;
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

        return Hex.toHexString(hash) + "." + fileExtension;
    }

    @Override
    public String uploadFile(InputStream fileStream, @Nullable Long fileSize,
                             String fileExtension, String bucketName) throws Exception {
        String filename = buildFilename(fileExtension, MessageDigest.getInstance("SHA256"));
        blobStore.put(filename, fileStream.readAllBytes());
        fileStream.close();

        return filename;
    }

    @Override
    public byte[] downloadFile(String filename, String bucketName) {
        return blobStore.get(filename);
    }

    @Override
    public void deleteMultiple(List<String> filenameList, String bucketName) {
        filenameList.stream().map(blobStore::remove).close();
    }
}
