package com.demo_shopyy_1.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService {
    private static final Logger log = LoggerFactory.getLogger(FirebaseStorageService.class);

    @Autowired
    private StorageClient storageClient;

    public String uploadFile(MultipartFile file) throws IOException {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            log.info("Generated file name: {}", fileName);

            BlobId blobId = BlobId.of(storageClient.bucket().getName(), fileName);
            log.info("Created BlobId: {}", blobId);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            log.info("Created BlobInfo: {}", blobInfo);

            Blob blob = storageClient.bucket().create(fileName, file.getInputStream(), file.getContentType());
            log.info("Created Blob: {}", blob);

            URL signedUrl = blob.signUrl(7, TimeUnit.DAYS);
            String publicUrl = signedUrl.toString();
            log.info("Generated signed URL: {}", publicUrl);

            return publicUrl;
        } catch (IOException e) {
            log.error("Firebase storage error", e);
            throw new IOException("Failed to read or upload the file", e);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new RuntimeException("An unexpected error occurred while uploading the file", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Storage storage = storageClient.bucket().getStorage();
            BlobId blobId = BlobId.of(storageClient.bucket().getName(), fileName);
            boolean deleted = storage.delete(blobId);
            if (!deleted) {
                throw new RuntimeException("Failed to delete file: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the file: " + fileName, e);
        }
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
}
