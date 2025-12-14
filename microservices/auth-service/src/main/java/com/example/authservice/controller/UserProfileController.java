package com.example.authservice.controller;

import com.example.authservice.dto.AddressDTO;
import com.example.authservice.dto.UserProfileDTO;
import com.example.authservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Integer id) {
        log.info("GET /users/{}/profile", id);
        try {
            UserProfileDTO profile = userProfileService.getUserProfile(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching profile for user {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @PathVariable Integer id,
            @RequestBody UserProfileDTO profileDTO) {
        log.info("PUT /users/{}/profile", id);
        try {
            UserProfileDTO updated = userProfileService.updateUserProfile(id, profileDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating profile for user {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<Map<String, Object>> getUserAddresses(@PathVariable Integer id) {
        log.info("GET /users/{}/addresses", id);
        try {
            List<AddressDTO> addresses = userProfileService.getUserAddresses(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", addresses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching addresses for user {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<Map<String, Object>> addAddress(
            @PathVariable Integer id,
            @RequestBody AddressDTO addressDTO) {
        log.info("POST /users/{}/addresses", id);
        try {
            AddressDTO created = userProfileService.addAddress(id, addressDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error adding address for user {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<Map<String, Object>> deleteAddress(
            @PathVariable Integer id,
            @PathVariable Long addressId) {
        log.info("DELETE /users/{}/addresses/{}", id, addressId);
        try {
            userProfileService.deleteAddress(id, addressId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Address deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting address {} for user {}: {}", addressId, id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
