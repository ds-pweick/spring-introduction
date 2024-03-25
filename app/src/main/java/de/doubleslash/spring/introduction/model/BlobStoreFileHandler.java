package de.doubleslash.spring.introduction.model;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

public interface BlobStoreFileHandler {
    String buildFilename(String fileExtension, MessageDigest digest);

    String uploadFile(InputStream fileStream, @Nullable Long fileSize,
                      String fileExtension, String bucketName) throws Exception;

    byte[] downloadFile(String filename, String bucketName) throws Exception;

    void deleteMultiple(List<String> filenameList, String bucketName) throws Exception;
}
