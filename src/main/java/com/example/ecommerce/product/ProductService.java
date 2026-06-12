package com.example.ecommerce.product;

import com.example.ecommerce.common.EntityNotFoundException;
import com.example.ecommerce.product.dto.ProductRequestDto;
import com.example.ecommerce.product.dto.ProductResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponseDto create(ProductRequestDto request) {
        Product product = productMapper.toEntity(request);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto update(Long id, ProductRequestDto request) {
        Product product = getEntity(id);
        productMapper.updateEntity(request, product);
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getEntity(id);
        product.setActive(false);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto get(Long id) {
        return productMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> list() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }
}
