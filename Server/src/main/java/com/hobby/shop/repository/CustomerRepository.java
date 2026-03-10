package com.hobby.shop.repository;

import com.hobby.shop.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByEnabledTrue();
    long countByEnabledFalse();
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.enabled = :status")
    long countByEnabledStatus(@Param("status") boolean status);

}