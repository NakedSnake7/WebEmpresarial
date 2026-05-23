package com.webempresarial.store.model;

import jakarta.persistence.Column;     
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(
	    name = "clientes",
	    uniqueConstraints = {
	        @UniqueConstraint(
	            name = "uk_clientes_email_store",
	            columnNames = {"email", "store_id"}
	        )
	    }
	)
public class Cliente {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String fullName;

    @Email(message = "El correo electrónico no es válido")
    @NotEmpty(message = "El correo no puede estar vacío")
    private String email;
    
    @Size(min = 10, max = 15, message = "El número de teléfono debe contener entre 10 y 15 dígitos")
    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "default_address", length = 255)
    private String defaultAddress;

    // RELACIÓN INVERSA
    @OneToOne(mappedBy = "cliente")
    private AuthUser authUser;
    
    
    public String getDefaultAddress() {
		return defaultAddress;
	}

	public void setDefaultAddress(String defaultAddress) {
		this.defaultAddress = defaultAddress;
	}

	// Constructor vacío necesario para JPA
    public Cliente() {}

    // Constructor con parámetros
    public Cliente(String fullName, String email, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
    
    @PrePersist
    @PreUpdate
    public void normalize() {

        if (email != null) {
            email = email.trim().toLowerCase();
        }

        if (store == null) {
            throw new IllegalStateException("Usuario sin tienda");
        }
    }


    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @Override
    public String toString() {
    	return "Cliente{id=" + id + ", name='" + fullName + "', email='" + email + "', phone='" + phone + "'}";
    	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente cliente)) return false;

        return Objects.equals(email, cliente.email)
                && Objects.equals(
                    store != null ? store.getId() : null,
                    cliente.store != null ? cliente.store.getId() : null
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                email,
                store != null ? store.getId() : null
        );
    }
    public AuthUser getAuthUser() {
        return authUser;
    }

    public void setAuthUser(AuthUser authUser) {
        this.authUser = authUser;
    }
}
