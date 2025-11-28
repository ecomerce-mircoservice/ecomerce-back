package com.example.authservice.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.authservice.dao.entity.UserInfo;
import com.example.authservice.dto.response.ApiResponse;
import com.example.authservice.dto.response.AuthResponse.UserInfoDto;
import com.example.authservice.service.UserInfoService;

@RestController
@RequestMapping("/users")
public class UserManagementController {

    private final UserInfoService userInfoService;

    public UserManagementController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<UserInfoDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Page<UserInfo> userPage = userInfoService.getAllUsersPaginated(page, size, search);

        List<UserInfoDto> userDtos = userPage.getContent().stream()
                .map(user -> new UserInfoDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        ))
                .collect(Collectors.toList());

        Map<String, Object> metadata = ApiResponse.createPaginationMetadata(
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );

        return ApiResponse.success(userDtos, "Users retrieved successfully", metadata);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserInfoDto> getUserById(@PathVariable Integer id) {
        UserInfo user = userInfoService.getUserById(id);
        UserInfoDto userDto = new UserInfoDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );

        return ApiResponse.success(userDto, null);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserInfoDto> createUser(@RequestBody UserInfo userInfo) {
        UserInfo savedUser = userInfoService.addUser(userInfo);
        UserInfoDto userDto = new UserInfoDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRoles()
        );

        return ApiResponse.success(userDto, "User created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserInfoDto> updateUser(@PathVariable Integer id, @RequestBody UserInfo userInfo) {
        UserInfo updatedUser = userInfoService.updateUser(id, userInfo);
        UserInfoDto userDto = new UserInfoDto(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRoles()
        );

        return ApiResponse.success(userDto, "User updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer id) {
        userInfoService.deleteUser(id);
    }
}
