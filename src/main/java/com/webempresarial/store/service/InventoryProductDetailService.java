package com.webempresarial.store.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.dto.inventory.InventoryMovementRowDTO;
import com.webempresarial.store.dto.inventory.InventoryProductDetailDTO;
import com.webempresarial.store.dto.inventory.InventoryVariantDetailDTO;
import com.webempresarial.store.entity.InventoryMovement;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.InventoryMovementRepository;
import com.webempresarial.store.repository.ProductoRepository;

@Service
public class InventoryProductDetailService {

    private final ProductoRepository productoRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryProductDetailService(
            ProductoRepository productoRepository,
            InventoryMovementRepository movementRepository
    ) {
        this.productoRepository = productoRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public InventoryProductDetailDTO getDetail(
            Long productId,
            Store store
    ) {
        Producto producto = productoRepository
                .findByIdConTodo(productId, store)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto no encontrado"
                        )
                );

        List<InventoryVariantDetailDTO> variants =
                producto.getVariantes() == null
                        ? List.of()
                        : producto.getVariantes()
                                .stream()
                                .map(variante ->
                                        new InventoryVariantDetailDTO(
                                                variante.getId(),
                                                variante.getNombreVisual(),
                                                variante.getStock(),
                                                variante.getPrecioFinal()
                                        )
                                )
                                .toList();

        List<InventoryMovementRowDTO> movements =
                movementRepository
                        .findTop50ByProductoIdAndStoreOrderByCreatedAtDesc(
                                productId,
                                store
                        )
                        .stream()
                        .map(this::toRow)
                        .toList();

        return new InventoryProductDetailDTO(
                producto.getId(),
                producto.getProductName(),
                producto.getSku(),
                producto.tieneVariantes(),
                producto.getStockSimple(),
                producto.getPrice(),
                variants,
                movements
        );
    }

    private InventoryMovementRowDTO toRow(
            InventoryMovement movement
    ) {
        return new InventoryMovementRowDTO(
                movement.getId(),
                movement.getCreatedAt(),
                movement.getType(),
                movement.getProducto().getId(),
                movement.getProducto().getProductName(),
                movement.getVariante() != null
                        ? movement.getVariante().getId()
                        : null,
                movement.getVariante() != null
                        ? movement.getVariante().getNombreVisual()
                        : null,
                movement.getOrder() != null
                        ? movement.getOrder().getId()
                        : null,
                movement.getQuantity(),
                movement.getStockBefore(),
                movement.getStockAfter(),
                movement.getReason()
        );
    }
}