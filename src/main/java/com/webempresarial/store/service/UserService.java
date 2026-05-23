package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.UserNotFoundException;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String normalizedEmail =
                cliente.getEmail().trim().toLowerCase();

        Optional<Cliente> existingUser =
                userRepository.findByEmailAndStore(
                        normalizedEmail,
                        store
                );

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        cliente.setEmail(normalizedEmail);
        cliente.setStore(store);

        return userRepository.save(cliente);
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