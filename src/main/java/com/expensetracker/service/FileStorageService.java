package com.expensetracker.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file, Long userId);
    Resource loadFileAsResource(String filePath);
    void deleteFile(String filePath);
    Path getFileStorageLocation();
}
