package com.example.file_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path: "uploads/xxxx.png"
        return uploadDir + "/" + fileName;
    }

    public boolean deleteFile(String filePath) {
        try {
            // Extract just the filename from the path (e.g., "uploads/123_file.png" -> "123_file.png")
            String fileName = filePath.replace(uploadDir + "/", "");
            Path fileToDelete = Paths.get(uploadDir).resolve(fileName);
            
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
