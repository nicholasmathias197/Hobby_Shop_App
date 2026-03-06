package com.hobby.shop.repository;

import com.hobby.shop.model.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByName(String name);
    Page<Brand> findByIsActiveTrue(Pageable pageable);
    boolean existsByName(String name);
}