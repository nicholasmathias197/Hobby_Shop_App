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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

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

    @Override
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        return brandMapper.toResponse(brand);
    }

    @Override
    public BrandResponse getBrandByName(String name) {
        Brand brand = brandRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with name: " + name));

        return brandMapper.toResponse(brand);
    }

    @Override
    public Page<BrandResponse> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(brandMapper::toResponse);
    }

    @Override
    public List<BrandResponse> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrue(Pageable.unpaged())
                .stream()
                .map(brandMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BrandResponse> getActiveBrands(Pageable pageable) {
        return brandRepository.findByIsActiveTrue(pageable)
                .map(brandMapper::toResponse);
    }
}