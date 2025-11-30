package com.example.order_service.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ApiResponse<T> {

    private boolean success;
    private int code;
    private String message;
    private T data;
    private Map<String, Object> metadata;

    // Success response with data
    public static <T> ApiResponse<T> success(T data, String message) {
        log.info("API Success: Code=200, Message={}", message);
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // Success response with data and metadata (for pagination)
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> metadata) {
        ApiResponse<T> response = success(data, message);
        response.setMetadata(metadata);
        return response;
    }

    // Error response
    public static <T> ApiResponse<T> error(int code, String message) {
        log.error("API Error: Code={}, Message={}", code, message);
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    // Helper method to create pagination metadata
    public static Map<String, Object> createPaginationMetadata(
            int page, int size, long totalElements, int totalPages) {
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("pageSize", size);
        pagination.put("totalPages", totalPages);
        pagination.put("totalItems", totalElements);
        metadata.put("pagination", pagination);
        return metadata;
    }
}
