package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.BrandRequest;
import com.hobby.shop.dto.response.BrandResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.BrandMapper;
import com.hobby.shop.model.Brand;
import com.hobby.shop.repository.BrandRepository;
import com.hobby.shop.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the BrandService interface.
 * Manages product brands including CRUD operations and brand activation/deactivation.
 *
 * Brands represent manufacturers or labels of products. This service provides
 * comprehensive brand management for both admin and public-facing operations.
 *
 * @author Hobby Shop Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    /**
     * Creates a new brand.
     *
     * Process flow:
     * 1. Validates that the brand name is unique
     * 2. Maps the request to a Brand entity
     * 3. Saves the brand to the database
     *
     * @param request the brand creation request containing name and description
     * @return BrandResponse of the created brand
     * @throws BadRequestException if a brand with the same name already exists
     */
    @Override
    public BrandResponse createBrand(BrandRequest request) {
        log.info("Creating new brand with name: {}", request.getName());

        if (brandRepository.existsByName(request.getName())) {
            throw new BadRequestException("Brand with name " + request.getName() + " already exists");
        }

        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created successfully with ID: {}", savedBrand.getId());
        return brandMapper.toResponse(savedBrand);
    }

    /**
     * Updates an existing brand.
     *
     * @param id the ID of the brand to update
     * @param request the update request containing new brand data
     * @return BrandResponse with updated information
     * @throws ResourceNotFoundException if brand not found
     * @throws BadRequestException if trying to rename to an already existing name
     */
    @Override
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        log.info("Updating brand with ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        // Check if name is being changed and if it's already taken
        if (!brand.getName().equals(request.getName()) &&
                brandRepository.existsByName(request.getName())) {
            throw new BadRequestException("Brand with name " + request.getName() + " already exists");
        }

        brandMapper.updateEntity(brand, request);
        Brand updatedBrand = brandRepository.save(brand);

        log.info("Brand updated successfully with ID: {}", updatedBrand.getId());
        return brandMapper.toResponse(updatedBrand);
    }

    /**
     * Soft-deletes a brand by marking it as inactive.
     * The brand record remains in the database for historical purposes
     * but is no longer available for new product assignments.
     *
     * @param id the ID of the brand to delete
     * @throws ResourceNotFoundException if brand not found
     */
    @Override
    public void deleteBrand(Long id) {
        log.info("Deleting brand with ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        // Soft delete - just mark as inactive
        brand.setIsActive(false);
        brandRepository.save(brand);

        log.info("Brand deactivated successfully with ID: {}", id);
    }

    /**
     * Retrieves a brand by its ID.
     *
     * @param id the brand ID to search for
     * @return BrandResponse containing brand details
     * @throws ResourceNotFoundException if brand not found
     */
    @Override
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        return brandMapper.toResponse(brand);
    }

    /**
     * Retrieves a brand by its name.
     *
     * @param name the brand name to search for
     * @return BrandResponse containing brand details
     * @throws ResourceNotFoundException if brand not found
     */
    @Override
    public BrandResponse getBrandByName(String name) {
        Brand brand = brandRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with name: " + name));

        return brandMapper.toResponse(brand);
    }

    /**
     * Retrieves a paginated list of all brands (including inactive ones).
     * Primarily used for admin purposes.
     *
     * @param pageable pagination information
     * @return Page of BrandResponse objects
     */
    @Override
    public Page<BrandResponse> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(brandMapper::toResponse);
    }

    /**
     * Retrieves a list of all active brands.
     * Used for public-facing displays like brand filters or dropdowns.
     *
     * @return List of active BrandResponse objects
     */
    @Override
    public List<BrandResponse> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrue(Pageable.unpaged())
                .stream()
                .map(brandMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a paginated list of active brands.
     *
     * @param pageable pagination information
     * @return Page of active BrandResponse objects
     */
    @Override
    public Page<BrandResponse> getActiveBrands(Pageable pageable) {
        return brandRepository.findByIsActiveTrue(pageable)
                .map(brandMapper::toResponse);
    }
}