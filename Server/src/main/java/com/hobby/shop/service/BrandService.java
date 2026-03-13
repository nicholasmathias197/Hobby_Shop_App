package com.hobby.shop.service;

import com.hobby.shop.dto.request.BrandRequest;
import com.hobby.shop.dto.response.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandService {
    BrandResponse createBrand(BrandRequest request);

    BrandResponse updateBrand(Long id, BrandRequest request);

    void deleteBrand(Long id);

    BrandResponse getBrandById(Long id);

    BrandResponse getBrandByName(String name);

    Page<BrandResponse> getAllBrands(Pageable pageable);

    List<BrandResponse> getAllActiveBrands();

    Page<BrandResponse> getActiveBrands(Pageable pageable);
}