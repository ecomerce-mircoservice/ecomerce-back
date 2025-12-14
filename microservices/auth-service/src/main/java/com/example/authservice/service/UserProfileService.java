package com.example.authservice.service;

import com.example.authservice.dao.entity.Address;
import com.example.authservice.dao.entity.UserInfo;
import com.example.authservice.dao.repository.AddressRepository;
import com.example.authservice.dao.repository.UserInfoRepository;
import com.example.authservice.dto.AddressDTO;
import com.example.authservice.dto.UserProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserInfoRepository userInfoRepository;
    private final AddressRepository addressRepository;

    public UserProfileDTO getUserProfile(Integer userId) {
        log.info("Fetching profile for user ID: {}", userId);
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles());
    }

    @Transactional
    public UserProfileDTO updateUserProfile(Integer userId, UserProfileDTO profileDTO) {
        log.info("Updating profile for user ID: {}", userId);
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(profileDTO.getName());
        user.setEmail(profileDTO.getEmail());

        UserInfo updated = userInfoRepository.save(user);

        return new UserProfileDTO(
                updated.getId(),
                updated.getName(),
                updated.getEmail(),
                updated.getRoles());
    }

    public List<AddressDTO> getUserAddresses(Integer userId) {
        log.info("Fetching addresses for user ID: {}", userId);
        List<Address> addresses = addressRepository.findByUserId(userId);

        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO addAddress(Integer userId, AddressDTO addressDTO) {
        log.info("Adding address for user ID: {}", userId);

        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            List<Address> existingAddresses = addressRepository.findByUserId(userId);
            existingAddresses.forEach(addr -> {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            });
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setFullName(addressDTO.getFullName());
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setZipCode(addressDTO.getZipCode());
        address.setCountry(addressDTO.getCountry());
        address.setIsDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : false);

        Address saved = addressRepository.save(address);
        log.info("Address created with ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public void deleteAddress(Integer userId, Long addressId) {
        log.info("Deleting address ID: {} for user ID: {}", addressId, userId);
        addressRepository.deleteByIdAndUserId(addressId, userId);
    }

    private AddressDTO convertToDTO(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getFullName(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry(),
                address.getIsDefault());
    }
}
