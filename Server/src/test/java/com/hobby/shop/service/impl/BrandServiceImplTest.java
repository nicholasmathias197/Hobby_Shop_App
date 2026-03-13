package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.BrandRequest;
import com.hobby.shop.dto.response.BrandResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.BrandMapper;
import com.hobby.shop.model.Brand;
import com.hobby.shop.repository.BrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandServiceImpl brandService;

    private BrandRequest brandRequest;
    private Brand brand;
    private BrandResponse brandResponse;
    private final Long brandId = 1L;
    private final String brandName = "Test Brand";

    @BeforeEach
    void setUp() {
        brandRequest = new BrandRequest();
        brandRequest.setName(brandName);
        brandRequest.setDescription("Test Description");

        brand = new Brand();
        brand.setId(brandId);
        brand.setName(brandName);
        brand.setDescription("Test Description");
        brand.setIsActive(true);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());

        brandResponse = new BrandResponse();
        brandResponse.setId(brandId);
        brandResponse.setName(brandName);
        brandResponse.setDescription("Test Description");
        brandResponse.setIsActive(true);
    }

    @Test
    void createBrand_Success() {
        // Arrange
        when(brandRepository.existsByName(brandName)).thenReturn(false);
        when(brandMapper.toEntity(brandRequest)).thenReturn(brand);
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);
        when(brandMapper.toResponse(brand)).thenReturn(brandResponse);

        // Act
        BrandResponse result = brandService.createBrand(brandRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(brandId);
        assertThat(result.getName()).isEqualTo(brandName);
        assertThat(result.getIsActive()).isTrue();

        verify(brandRepository).existsByName(brandName);
        verify(brandMapper).toEntity(brandRequest);
        verify(brandRepository).save(brand);
        verify(brandMapper).toResponse(brand);
    }

    @Test
    void createBrand_NameAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        when(brandRepository.existsByName(brandName)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> brandService.createBrand(brandRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Brand with name " + brandName + " already exists");

        verify(brandRepository).existsByName(brandName);
        verify(brandMapper, never()).toEntity(any());
        verify(brandRepository, never()).save(any());
    }

    @Test
    void updateBrand_Success() {
        // Arrange
        BrandRequest updateRequest = new BrandRequest();
        updateRequest.setName("Updated Brand");
        updateRequest.setDescription("Updated Description");

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(brandRepository.existsByName("Updated Brand")).thenReturn(false);
        doNothing().when(brandMapper).updateEntity(brand, updateRequest);
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);

        BrandResponse updatedResponse = new BrandResponse();
        updatedResponse.setId(brandId);
        updatedResponse.setName("Updated Brand");
        updatedResponse.setDescription("Updated Description");
        when(brandMapper.toResponse(brand)).thenReturn(updatedResponse);

        // Act
        BrandResponse result = brandService.updateBrand(brandId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Brand");

        verify(brandRepository).findById(brandId);
        verify(brandRepository).existsByName("Updated Brand");
        verify(brandMapper).updateEntity(brand, updateRequest);
        verify(brandRepository).save(brand);
        verify(brandMapper).toResponse(brand);
    }

    @Test
    void updateBrand_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(brandRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> brandService.updateBrand(999L, brandRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Brand not found with id: 999");

        verify(brandRepository).findById(999L);
        verify(brandRepository, never()).existsByName(anyString());
        verify(brandRepository, never()).save(any());
    }

    @Test
    void updateBrand_NameAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        BrandRequest updateRequest = new BrandRequest();
        updateRequest.setName("Existing Brand");
        updateRequest.setDescription("Description");

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(brandRepository.existsByName("Existing Brand")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> brandService.updateBrand(brandId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Brand with name Existing Brand already exists");

        verify(brandRepository).findById(brandId);
        verify(brandRepository).existsByName("Existing Brand");
        verify(brandRepository, never()).save(any());
    }

    @Test
    void updateBrand_SameName_DoesNotCheckUniqueness() {
        // Arrange
        BrandRequest updateRequest = new BrandRequest();
        updateRequest.setName(brandName); // Same name
        updateRequest.setDescription("Updated Description");

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        // No need to mock existsByName since it shouldn't be called with same name
        doNothing().when(brandMapper).updateEntity(brand, updateRequest);
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);
        when(brandMapper.toResponse(brand)).thenReturn(brandResponse);

        // Act
        BrandResponse result = brandService.updateBrand(brandId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(brandRepository, never()).existsByName(anyString());
    }

    @Test
    void deleteBrand_Success() {
        // Arrange
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);

        // Act
        brandService.deleteBrand(brandId);

        // Assert
        verify(brandRepository).findById(brandId);
        verify(brandRepository).save(brand);
        assertThat(brand.getIsActive()).isFalse();
    }

    @Test
    void deleteBrand_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(brandRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> brandService.deleteBrand(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Brand not found with id: 999");
    }

    @Test
    void getBrandById_Success() {
        // Arrange
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(brandMapper.toResponse(brand)).thenReturn(brandResponse);

        // Act
        BrandResponse result = brandService.getBrandById(brandId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(brandId);
        assertThat(result.getName()).isEqualTo(brandName);

        verify(brandRepository).findById(brandId);
        verify(brandMapper).toResponse(brand);
    }

    @Test
    void getBrandById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(brandRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> brandService.getBrandById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Brand not found with id: 999");
    }

    @Test
    void getBrandByName_Success() {
        // Arrange
        when(brandRepository.findByName(brandName)).thenReturn(Optional.of(brand));
        when(brandMapper.toResponse(brand)).thenReturn(brandResponse);

        // Act
        BrandResponse result = brandService.getBrandByName(brandName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(brandName);

        verify(brandRepository).findByName(brandName);
        verify(brandMapper).toResponse(brand);
    }

    @Test
    void getBrandByName_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(brandRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> brandService.getBrandByName("Nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Brand not found with name: Nonexistent");
    }

    @Test
    void getAllBrands_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Brand> brands = Arrays.asList(brand, createAnotherBrand());
        Page<Brand> brandPage = new PageImpl<>(brands, pageable, brands.size());

        when(brandRepository.findAll(pageable)).thenReturn(brandPage);
        when(brandMapper.toResponse(any(Brand.class))).thenReturn(brandResponse);

        // Act
        Page<BrandResponse> result = brandService.getAllBrands(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(brandRepository).findAll(pageable);
        verify(brandMapper, times(2)).toResponse(any(Brand.class));
    }

    @Test
    void getAllActiveBrands_Unpaged_Success() {
        // Arrange
        List<Brand> activeBrands = Arrays.asList(brand, createAnotherBrand());
        Page<Brand> brandPage = new PageImpl<>(activeBrands);

        when(brandRepository.findByIsActiveTrue(Pageable.unpaged())).thenReturn(brandPage);
        when(brandMapper.toResponse(any(Brand.class))).thenReturn(brandResponse);

        // Act
        List<BrandResponse> result = brandService.getAllActiveBrands();

        // Assert
        assertThat(result).hasSize(2);
        verify(brandRepository).findByIsActiveTrue(Pageable.unpaged());
        verify(brandMapper, times(2)).toResponse(any(Brand.class));
    }

    @Test
    void getActiveBrands_Paginated_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Brand> activeBrands = Arrays.asList(brand);
        Page<Brand> brandPage = new PageImpl<>(activeBrands, pageable, 1);

        when(brandRepository.findByIsActiveTrue(pageable)).thenReturn(brandPage);
        when(brandMapper.toResponse(brand)).thenReturn(brandResponse);

        // Act
        Page<BrandResponse> result = brandService.getActiveBrands(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(brandRepository).findByIsActiveTrue(pageable);
        verify(brandMapper).toResponse(brand);
    }

    private Brand createAnotherBrand() {
        Brand anotherBrand = new Brand();
        anotherBrand.setId(2L);
        anotherBrand.setName("Another Brand");
        anotherBrand.setIsActive(true);
        return anotherBrand;
    }
}