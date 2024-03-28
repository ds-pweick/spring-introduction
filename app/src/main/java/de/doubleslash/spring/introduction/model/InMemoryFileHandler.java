package de.doubleslash.spring.introduction.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("test")
@Slf4j
public class InMemoryFileHandler implements BlobStoreFileHandler {

    private final Map<String, byte[]> blobStore = new HashMap<>();

    @Override
    public String uploadFile(InputStream fileStream, @Nullable Long fileSize,
                             String fileExtension, String bucketName) throws Exception {
        String filename = buildUniqueFilename(fileExtension, MessageDigest.getInstance("SHA256"));
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
