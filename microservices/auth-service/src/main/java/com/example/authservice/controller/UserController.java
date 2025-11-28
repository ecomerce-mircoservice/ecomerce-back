package com.example.authservice.controller;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authservice.dao.entity.UserInfo;
import com.example.authservice.dto.request.AuthRequest;
import com.example.authservice.dto.response.ApiResponse;
import com.example.authservice.dto.response.AuthResponse;
import com.example.authservice.dto.response.AuthResponse.UserInfoDto;
import com.example.authservice.service.JwtService;
import com.example.authservice.service.UserInfoService;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserInfoService userInfoService;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public UserController(UserInfoService userInfoService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userInfoService = userInfoService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody UserInfo userInfo) {
        UserInfo savedUser = userInfoService.addUser(userInfo);
        String token = jwtService.generateToken(savedUser.getEmail());

        UserInfoDto userDto = new UserInfoDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRoles()
        );

        AuthResponse authResponse = new AuthResponse(token, userDto);
        return ApiResponse.success(authResponse, "Registration successful");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authRequest.getEmail());
            UserInfo user = userInfoService.getUserByEmail(authRequest.getEmail());

            UserInfoDto userDto = new UserInfoDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRoles()
            );

            AuthResponse authResponse = new AuthResponse(token, userDto);
            return ApiResponse.success(authResponse, "Login successful");
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoDto> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserInfo user = userInfoService.getUserByEmail(email);

        UserInfoDto userDto = new UserInfoDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );

        return ApiResponse.success(userDto, "User information retrieved successfully");
    }


    @PostMapping("/refresh-token")
    public ApiResponse<Map<String, String>> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Generate a new token
        String newToken = jwtService.generateToken(email);

        return ApiResponse.success(
                Map.of("token", newToken),
                "Token refreshed successfully"
        );
    }

}
