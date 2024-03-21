package de.doubleslash.spring.introduction.model;

import org.springframework.core.io.ByteArrayResource;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

public interface BlobStoreFileHandler {
    String buildFilename(String fileExtension, MessageDigest digest);

    String uploadFile(InputStream inputStream, @Nullable String blobBucketName, @Nullable Long fileSize,
                      String fileExtension) throws Exception;

    ByteArrayResource downloadFile(String filename, @Nullable String blobBucketName) throws Exception;

    void deleteMultiple(List<String> filenameList, @Nullable String blobBucketName) throws Exception;
}
