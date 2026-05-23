package com.webempresarial.store.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "admin_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_admin_user_email",
            columnNames = "email"
        )
    }
)
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role = AdminRole.STORE_ADMIN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = true)
    private Store store;

    @Column(nullable = false)
    private boolean enabled = true;

    public AdminUser() {}

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }

        if (role != AdminRole.SUPER_ADMIN && store == null) {
            throw new IllegalStateException("Un admin de tienda debe estar asociado a una tienda");
        }

        if (role == AdminRole.SUPER_ADMIN) {
            store = null;
        }
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public AdminRole getRole() {
        return role;
    }

    public Store getStore() {
        return store;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(AdminRole role) {
        this.role = role;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}