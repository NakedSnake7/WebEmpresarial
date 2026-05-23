package com.webempresarial.store.service;

import com.webempresarial.store.contracts.StockItem;
import com.webempresarial.store.dto.checkout.CartItemDTO;
import com.webempresarial.store.exceptions.InsufficientStockException;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderItem;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final ProductoVarianteRepository varianteRepository;
    private final ProductoRepository productoRepository;

    public StockService(
            ProductoVarianteRepository varianteRepository,
            ProductoRepository productoRepository
    ) {
        this.varianteRepository = varianteRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public void validarStock(
            List<? extends StockItem> items,
            Store store
    ) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No hay items en la orden");
        }

        for (StockItem item : items) {

            if (item.getVarianteId() != null) {

                ProductoVariante variante = varianteRepository
                        .findByIdForUpdate(item.getVarianteId(), store)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Variante no encontrada: " + item.getVarianteId()
                                )
                        );

                if (variante.getStock() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para variante"
                    );
                }

            } else {

                CartItemDTO cartItem = (CartItemDTO) item;

                var producto = productoRepository
                        .findByIdConTodo(cartItem.getProductId(), store)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado: " + cartItem.getProductId()
                                )
                        );

                if (producto.getStockSimple() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para producto: " + producto.getProductName()
                    );
                }
            }
        }
    }

    @Transactional
    public void descontarStock(
            Order order,
            Store store
    ) {

        if (order.isStockReduced()) return;

        for (OrderItem item : order.getItems()) {

            if (item.getVariante() != null) {

                ProductoVariante variante = varianteRepository
                        .findByIdForUpdate(item.getVariante().getId(), store)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Variante no encontrada")
                        );

                if (variante.getStock() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para variante"
                    );
                }

                variante.setStock(
                        variante.getStock() - item.getQuantity()
                );

                varianteRepository.save(variante);

            } else {

                var producto = productoRepository
                        .findByIdConTodo(item.getProducto().getId(), store)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Producto no encontrado")
                        );

                if (producto.getStockSimple() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para producto: " + producto.getProductName()
                    );
                }

                producto.setStockSimple(
                        producto.getStockSimple() - item.getQuantity()
                );

                productoRepository.save(producto);
            }
        }

        order.setStockReduced(true);
    }

    @Transactional
    public void restaurarStock(
            Order order,
            Store store
    ) {

        if (!order.isStockReduced()) return;

        for (OrderItem item : order.getItems()) {

            if (item.getVariante() != null) {

                ProductoVariante variante = varianteRepository
                        .findByIdForUpdate(item.getVariante().getId(), store)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Variante no encontrada")
                        );

                variante.setStock(
                        variante.getStock() + item.getQuantity()
                );

                varianteRepository.save(variante);

            } else {

                var producto = productoRepository
                        .findByIdConTodo(item.getProducto().getId(), store)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Producto no encontrado")
                        );

                producto.setStockSimple(
                        producto.getStockSimple() + item.getQuantity()
                );

                productoRepository.save(producto);
            }
        }

        order.setStockReduced(false);
    }
}