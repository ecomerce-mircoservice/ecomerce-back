package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long id;
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean isDefault;
}
