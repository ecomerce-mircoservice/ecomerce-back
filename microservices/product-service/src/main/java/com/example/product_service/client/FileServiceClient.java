package com.example.product_service.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileServiceClient {

    private final RestTemplate restTemplate;
    @Value("${file.service.url}")
    private String FILE_SERVICE_URL;

    public String uploadFile(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, String>> response = restTemplate.postForEntity(
                    FILE_SERVICE_URL + "/files",
                    requestEntity,
                    (Class<Map<String, String>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String path = (String) response.getBody().get("path");
                log.info("File uploaded successfully: {}", path);
                return path;
            } else {
                log.error("Failed to upload file to file-service");
                throw new RuntimeException("Failed to upload file");
            }

        } catch (Exception e) {
            log.error("Error uploading file to file-service: {}", e.getMessage());
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                log.warn("Attempted to delete file with empty path");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("path", filePath);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    FILE_SERVICE_URL + "/files",
                    org.springframework.http.HttpMethod.DELETE,
                    requestEntity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean success = (Boolean) response.getBody().get("success");
                if (Boolean.TRUE.equals(success)) {
                    log.info("File deleted successfully: {}", filePath);
                } else {
                    log.warn("File not found for deletion: {}", filePath);
                }
            }

        } catch (Exception e) {
            log.error("Error deleting file from file-service: {}", e.getMessage());
            // Don't throw exception, just log the error since deletion failure shouldn't block the update
        }
    }
}
