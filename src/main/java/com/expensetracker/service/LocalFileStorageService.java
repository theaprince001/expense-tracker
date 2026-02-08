package com.expensetracker.service;

import com.expensetracker.exception.BusinessRuleException;
import com.expensetracker.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file, Long userId) {

        validateFile(file);

        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename);

            String fileName = "receipt_" + UUID.randomUUID() + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    (extension != null ? "." + extension : "");

            Path userDir = Paths.get(uploadDir, "user-" + userId);
            Files.createDirectories(userDir);

            Path targetLocation = userDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();

        } catch (IOException ex) {
            log.error("File storage failed for user {}", userId, ex);
            throw new RuntimeException("Failed to store file. Please try again later.");
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path resolvedPath = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(resolvedPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found");
            }

            return resource;

        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Invalid file path");
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path resolvedPath = Paths.get(filePath).normalize();
            Files.deleteIfExists(resolvedPath);
            log.info("File deleted: {}", filePath);
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", filePath, ex);
        }
    }

    @Override
    public Path getFileStorageLocation() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /* ===================== VALIDATION ===================== */

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("File must not be empty");
        }

        long size = file.getSize();
        if (size > 10 * 1024 * 1024) {
            throw new BusinessRuleException("File size exceeds 10MB limit");
        }

        String contentType = file.getContentType();
        if (!isValidContentType(contentType)) {
            throw new BusinessRuleException(
                    "Invalid file type. Allowed types: JPG, PNG, PDF"
            );
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("application/pdf")
        );
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return null;
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
