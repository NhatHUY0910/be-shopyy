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

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.warn("Attempted to delete file with null or empty URL");
            return;
        }

        try {
            log.info("Attempting to delete file from URL: {}", fileUrl);
            String fileName = extractFileNameFromUrl(fileUrl);
            log.info("Extracted file name: {}", fileName);
            if (fileName == null) {
                log.warn("Could not extract filename from URL: {}", fileUrl);
                return;
            }

            Storage storage = storageClient.bucket().getStorage();
            BlobId blobId = BlobId.of(storageClient.bucket().getName(), fileName);
            log.info("Created BlobId: {}", blobId);

            boolean deleted = storage.delete(blobId);
            if (!deleted) {
                log.warn("File does not exist or could not be deleted: {}", fileName);
                // Không throw exception ở đây nếu file không tồn tại
                return;
            }
            log.info("Successfully deleted file: {}", fileName);
        } catch (Exception e) {
            log.error("Error occurred while deleting file: {}", e.getMessage(), e);
            // Có thể throw exception hoặc xử lý error tùy thuộc vào yêu cầu
        }
    }

    public String extractFileNameFromUrl(String url) {
        try {
            log.debug("Extracting filename from URL: {}", url);
            // Decode URL trước khi xử lý
            String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
            int lastSlashIndex = decodedUrl.lastIndexOf('/');
            int questionMarkIndex = decodedUrl.indexOf('?');

            if (lastSlashIndex == -1) {
                throw new IllegalArgumentException("No slash found in URL");
            }

            String fileName;
            if (questionMarkIndex == -1) {
                fileName = decodedUrl.substring(lastSlashIndex + 1);
            } else {
                fileName = decodedUrl.substring(lastSlashIndex + 1, questionMarkIndex);
            }

            log.debug("Extracted filename: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to extract filename from URL: {}. Error: {}", url, e.getMessage());
            throw new IllegalArgumentException("Unable to extract filename from URL: " + e.getMessage());
        }
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
}
