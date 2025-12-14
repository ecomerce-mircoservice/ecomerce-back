package com.example.authservice.dao.repository;

import com.example.authservice.dao.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Integer userId);

    void deleteByIdAndUserId(Long id, Integer userId);
}
