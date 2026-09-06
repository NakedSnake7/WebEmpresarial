package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.UserNotFoundException; 
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.commerce.application.order.OrderService;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrderService orderService;

    public UserService(
            UserRepository userRepository,
            OrderService orderService
    ) {
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    public Cliente findOrCreateUserByEmail(
            String email,
            String name,
            String phone,
            Store store
    ) {
        String normalizedEmail = email.trim().toLowerCase();

        return userRepository
                .findByEmailAndStore(normalizedEmail, store)
                .orElseGet(() -> {
                    Cliente cliente = new Cliente();
                    cliente.setStore(store);
                    cliente.setEmail(normalizedEmail);
                    cliente.setFullName(name);
                    cliente.setPhone(phone != null ? phone : "No disponible");

                    return userRepository.save(cliente);
                });
    }

    @Transactional
    public Cliente registerUser(
            String email,
            String name,
            String phone,
            Store store
    ) {
        String normalizedEmail = email.trim().toLowerCase();

        Optional<Cliente> existingUser =
                userRepository.findByEmailAndStore(
                        normalizedEmail,
                        store
                );

        Cliente cliente = existingUser.orElseGet(() -> {
            Cliente nuevo = new Cliente();
            nuevo.setStore(store);
            nuevo.setEmail(normalizedEmail);
            nuevo.setFullName(name);
            nuevo.setPhone(phone != null ? phone : "No disponible");
            return userRepository.save(nuevo);
        });

        orderService.claimGuestOrders(cliente, store);

        return cliente;
    }

    public boolean existsByEmail(
            String email,
            Store store
    ) {
        return userRepository.existsByEmailAndStore(
                email.trim().toLowerCase(),
                store
        );
    }

    @Transactional
    public Cliente saveUser(
            Cliente cliente,
            Store store
    ) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException("La tienda es obligatoria");
        }

        if (cliente.getEmail() == null
                || cliente.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "El correo del cliente es obligatorio"
            );
        }

        String normalizedEmail =
                cliente.getEmail().trim().toLowerCase();

        Cliente persistentCliente = userRepository
                .findByEmailAndStore(normalizedEmail, store)
                .orElseGet(Cliente::new);

        persistentCliente.setStore(store);
        persistentCliente.setEmail(normalizedEmail);

        if (cliente.getFullName() != null
                && !cliente.getFullName().isBlank()) {
            persistentCliente.setFullName(
                    cliente.getFullName().trim()
            );
        }

        if (cliente.getPhone() != null
                && !cliente.getPhone().isBlank()) {
            persistentCliente.setPhone(
                    cliente.getPhone().trim()
            );
        }

        if (cliente.getDefaultAddress() != null
                && !cliente.getDefaultAddress().isBlank()) {
            persistentCliente.setDefaultAddress(
                    cliente.getDefaultAddress().trim()
            );
        }

        return userRepository.save(persistentCliente);
    }

    public Optional<Cliente> findByEmail(
            String email,
            Store store
    ) {
        return userRepository.findByEmailAndStore(
                email.trim().toLowerCase(),
                store
        );
    }

    public Cliente findUserByEmail(
            String email,
            Store store
    ) {
        return findByEmail(email, store)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuario no encontrado con el email: " + email
                        )
                );
    }

    public void save(
            Cliente cliente,
            Store store
    ) {
        cliente.setStore(store);
        userRepository.save(cliente);
    }
}