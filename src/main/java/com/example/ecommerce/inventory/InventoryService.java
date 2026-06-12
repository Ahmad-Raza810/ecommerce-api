package com.example.ecommerce.inventory;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.EntityNotFoundException;
import com.example.ecommerce.inventory.dto.InventoryRequestDto;
import com.example.ecommerce.inventory.dto.InventoryResponseDto;
import com.example.ecommerce.inventory.dto.StockAdjustmentRequest;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final ProductService productService;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryMapper inventoryMapper,
                            ProductService productService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
        this.productService = productService;
    }

    @Transactional
    public InventoryResponseDto create(InventoryRequestDto request) {
        Product product = productService.getEntity(request.productId());
        if (inventoryRepository.findByProductId(request.productId()).isPresent()) {
            throw new BusinessException("Inventory already exists for product", HttpStatus.CONFLICT);
        }
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(request.availableQuantity());
        inventory.setReservedQuantity(request.reservedQuantity());
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponseDto updateStock(Long productId, StockAdjustmentRequest request) {
        Inventory inventory = getEntityByProductId(productId);
        inventory.setAvailableQuantity(request.availableQuantity());
        return inventoryMapper.toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    @Transactional
    public void reserveStock(Long productId, int quantity) {
        try {
            Inventory inventory = getEntityByProductId(productId);
            if (quantity <= 0) {
                throw new BusinessException("Quantity must be positive", HttpStatus.BAD_REQUEST);
            }
            if (inventory.getAvailableQuantity() < quantity) {
                throw new BusinessException("Product is out of stock", HttpStatus.CONFLICT);
            }
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.saveAndFlush(inventory);
        } catch (OptimisticLockingFailureException ex) {
            throw new BusinessException("Inventory was updated concurrently. Please submit the request again.", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public void releaseStock(Long productId, int quantity) {
        Inventory inventory = getEntityByProductId(productId);
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive", HttpStatus.BAD_REQUEST);
        }
        if (inventory.getReservedQuantity() < quantity) {
            throw new BusinessException("Reserved stock is insufficient", HttpStatus.CONFLICT);
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventoryRepository.saveAndFlush(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDto getByProduct(Long productId) {
        return inventoryMapper.toResponse(getEntityByProductId(productId));
    }

    private Inventory getEntityByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));
    }
}
