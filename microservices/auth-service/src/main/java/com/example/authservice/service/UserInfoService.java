package com.example.authservice.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.authservice.dao.entity.UserInfo;
import com.example.authservice.dao.repository.UserInfoRepository;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfoService(UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Method to load user details by username (email)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from the database by email (username)
        Optional<UserInfo> userInfo = userInfoRepository.findByEmail(username);

        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // Convert UserInfo to UserDetails (UserInfoDetails)
        UserInfo user = userInfo.get();
        return new UserInfoDetails(user);
    }

    // Add any additional methods for registering or managing users
    public UserInfo addUser(UserInfo userInfo) {
        // Encrypt password before saving
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        return userInfoRepository.save(userInfo);
    }

    // Get user by email
    public UserInfo getUserByEmail(String email) {
        return userInfoRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Get all users
    public java.util.List<UserInfo> getAllUsers() {
        return userInfoRepository.findAll();
    }

    // Get all users with pagination and search
    public org.springframework.data.domain.Page<UserInfo> getAllUsersPaginated(int page, int size, String search) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return userInfoRepository.findAllWithSearch(search, pageable);
    }

    // Get user by id
    public UserInfo getUserById(Integer id) {
        return userInfoRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    // Update user
    public UserInfo updateUser(Integer id, UserInfo userInfo) {
        UserInfo existingUser = getUserById(id);
        existingUser.setName(userInfo.getName());
        if (userInfo.getEmail() != null) {
            existingUser.setEmail(userInfo.getEmail());
        }
        if (userInfo.getPassword() != null && !userInfo.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        }
        if (userInfo.getRoles() != null) {
            existingUser.setRoles(userInfo.getRoles());
        }
        return userInfoRepository.save(existingUser);
    }

    // Delete user
    public void deleteUser(Integer id) {
        UserInfo user = getUserById(id);
        userInfoRepository.delete(user);
    }
}
