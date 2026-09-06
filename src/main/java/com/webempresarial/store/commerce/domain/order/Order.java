package com.webempresarial.store.commerce.domain.order;

import jakarta.persistence.CascadeType;       
import jakarta.persistence.Column;
import jakarta.persistence.Entity;   
import jakarta.persistence.Index;
import jakarta.validation.constraints.Size;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
	    name = "orders",
	    indexes = {
	        @Index(name = "idx_orders_store", columnList = "store_id"),
	        @Index(name = "idx_orders_store_date", columnList = "store_id, order_date"),
	        @Index(name = "idx_orders_email", columnList = "customer_email"),
	        @Index(name = "idx_orders_guest_token", columnList = "guest_token"),
	        @Index(name = "idx_orders_email_status", columnList = "customer_email, order_status"),
	        @Index(name = "idx_orders_email_user", columnList = "customer_email, user_id")
	    }
	)
public class Order {
	

	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private Cliente cliente;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @NotNull(message = "El total de la orden no puede ser nulo")
    @Min(value = 0, message = "El total debe ser positivo")
    private BigDecimal total;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String address;

    @Version
    @Column(nullable = false)
    private Long version;


    @NotNull(message = "La fecha de la orden no puede ser nula")
    private LocalDateTime orderDate;

    
    @Email(message = "Correo inválido")
    @NotBlank(message = "El email es obligatorio")
    private String customerEmail;
    
    @Column(name = "is_guest", nullable = false)
    private boolean isGuest = false;

    @Column(name = "claimed", nullable = false)
    private boolean claimed = false;

    @Column(name = "guest_token", unique = true, nullable = true)
    private String guestToken;
    
    @Column(name = "stock_reduced", nullable = false)
    private boolean stockReduced = false;


    // Emails
    
    @Column(name = "transfer_instructions_sent", nullable = false)
    private boolean transferInstructionsSent = false;

    @Column(name = "payment_confirmed_sent", nullable = false)
    private boolean paymentConfirmedSent = false;

    @Column(name = "order_expired_sent", nullable = false)
    private boolean orderExpiredSent = false;

    @Column(name = "shipping_confirmation_sent", nullable = false)
    private boolean shippingConfirmationSent = false;

    
    

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;
    
    
    @Column(name = "stripe_session_id", unique = true)
    private String stripeSessionId;

    @Column(name = "payment_intent_id", unique = true)
    private String paymentIntentId;


    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus orderStatus = OrderStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    

    
    @Transient
    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    @Transient
    public boolean isDelivered() {
        return orderStatus == OrderStatus.DELIVERED;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }

    @Transient
    public boolean belongsTo(String email) {
        return customerEmail != null && email != null &&
               customerEmail.trim().equalsIgnoreCase(email.trim());
    }
  
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;


    public enum PaymentMethod {
    	STRIPE,
        TRANSFER
    }

    
    

@Column(name = "paid_at")
private LocalDateTime paidAt;
    
    // Constructor vacío necesario para JPA
    public Order() {}

    // Constructor sin 'phone'
    public Order(Cliente cliente, BigDecimal total, String address, String customerName, String customerEmail) {
        this.cliente = cliente;
        this.total = total;
        this.address = address;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.orderDate = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;
        this.orderStatus = OrderStatus.CREATED;
        this.isGuest = (cliente == null);
    }

    // Métodos para manejar la relación bidireccional
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public String getOrderStatusLabel() {
        if (orderStatus == null) return "Desconocido";

        return switch (orderStatus) {
            case CREATED -> "Creada";
            case PROCESSED -> "Procesada";
            case SHIPPED -> "Enviada";
            case DELIVERED -> "Entregada";
            case CANCELLED -> "Cancelada";
            default -> "Desconocido";
        };
    }

    public String getOrderStatusBadge() {
        if (orderStatus == null) return "badge-secondary";

        return switch (orderStatus) {
            case CREATED -> "badge-info";
            case PROCESSED -> "badge-warning";
            case SHIPPED -> "badge-primary";
            case DELIVERED -> "badge-success";
            case CANCELLED -> "badge-danger";
            default -> "badge-secondary";
        };
    }
  
    @Transient
    public boolean hasGuestAccess() {
        return isGuest && guestToken != null;
    }

    @Transient
    public boolean isOwnedByUser() {
        return cliente != null;
    }
    
    @PrePersist
    @PreUpdate
    public void beforeSave() {

        // 🔹 Normalizar email
        if (this.customerEmail != null) {
            this.customerEmail = this.customerEmail.trim().toLowerCase();
        }

        // 🔹 Generar token si es guest
        if (this.guestToken == null && this.cliente == null) {
            this.guestToken = java.util.UUID.randomUUID().toString().replace("-", "");
        }

        // 🔹 Validación
        if (cliente == null && 
           (customerEmail == null || customerEmail.isBlank() ||
            customerName == null || customerName.isBlank())) {

            throw new IllegalStateException("Orden inválida: falta información del cliente");
        }

        if (cliente != null && isGuest) {
            throw new IllegalStateException("Inconsistencia: usuario asignado pero marcado como guest");
        }
        if (store == null) {
            throw new IllegalStateException("Orden inválida: falta tienda");
        }
    }
    
    public void claim(Cliente cliente) {
        if (this.claimed) {
            throw new IllegalStateException("La orden ya fue reclamada");
        }
        if (!this.isGuest) {
            throw new IllegalStateException("Solo órdenes guest pueden reclamarse");
        }
        if (!belongsTo(cliente.getEmail())) {
            throw new IllegalStateException("El email no coincide con la orden");
        }

        setUser(cliente);
        this.claimed = true;
    }
    
    @Transient
    public boolean canBeClaimed() {
        return isGuest && !claimed;
    }

    public String getPaymentStatusLabel() {
        if (paymentStatus == null) return "Desconocido";

        return switch (paymentStatus) {
            case PAID -> "Pagado";
            case PENDING -> "Pendiente";
            case FAILED -> "Fallido";
            case EXPIRED -> "Expirado";
        };
    }

    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getUser() {
        return cliente;
    }

    public void setUser(Cliente cliente) {
        this.cliente = cliente;
        this.isGuest = (cliente == null);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public @NotNull(message = "La fecha de la orden no puede ser nula") LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(@NotNull(message = "La fecha de la orden no puede ser nula") LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // Método toString para depuración
    @Override
    public String toString() {
        return "Order{id=" + id +
        		", user=" + (cliente != null ? cliente.getFullName() : customerName) +
               ", total=" + total +
               ", orderStatus=" + orderStatus +
               ", paymentStatus=" + paymentStatus +
               ", orderDate=" + orderDate + "}";
    }


    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
    public String getCarrier() {
        return carrier;
    }
   

    // Setter
    public void setStockReduced(Boolean stockReduced) {
        this.stockReduced = stockReduced;
    }

  

public String getStripeSessionId() {
    return stripeSessionId;
}

public void setStripeSessionId(String stripeSessionId) {
    this.stripeSessionId = stripeSessionId;
}
public PaymentMethod getPaymentMethod() {
    return paymentMethod;
}

public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
}

public PaymentStatus getPaymentStatus() {
    return paymentStatus;
}

public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
}

public OrderStatus getOrderStatus() {
    return orderStatus;
}

public void setOrderStatus(OrderStatus orderStatus) {
    this.orderStatus = orderStatus;
}

public String getPaymentIntentId() {
    return paymentIntentId;
}

public void setPaymentIntentId(String paymentIntentId) {
    this.paymentIntentId = paymentIntentId;
}

public void setCustomerEmail(String customerEmail) {
	this.customerEmail = customerEmail;
}
//=====================
//stock
//=====================
public boolean hasStockReduced() {
    return this.stockReduced;
}

public void markStockReduced() {
    this.stockReduced = true;
}
public void markStockAsReduced() {
    this.stockReduced = true;
}

//=====================
//PAGOS
//=====================
public void markAsPaid(String paymentIntentId) {

    if (this.paymentStatus == PaymentStatus.PAID) {
        return;
    }

    if (this.orderStatus == OrderStatus.CANCELLED) {
        throw new IllegalStateException("No puedes pagar una orden cancelada");
    }

    this.paymentStatus = PaymentStatus.PAID;
    this.paidAt = LocalDateTime.now();
    this.paymentIntentId = paymentIntentId;
    this.orderStatus = OrderStatus.PAID_PENDING_STOCK;
}
public void markAsPaid() {
    markAsPaid(null);
}

public void markAsProcessed() {
    if (this.orderStatus != OrderStatus.PAID_PENDING_STOCK) {
        throw new IllegalStateException("La orden no está lista para procesarse");
    }
    this.orderStatus = OrderStatus.PROCESSED;
}

public void markAsPendingStock() {

    if (this.paymentStatus != PaymentStatus.PAID) {
        throw new IllegalStateException("Solo órdenes pagadas pueden quedar en pending stock");
    }

    this.orderStatus = OrderStatus.PAID_PENDING_STOCK;
}

public boolean canExpire() {
    if (this.paymentMethod != PaymentMethod.TRANSFER) return false;
    if (this.paymentStatus == PaymentStatus.PAID) return false;
    if (this.orderStatus != OrderStatus.CREATED) return false;

    LocalDateTime limite = this.orderDate.plusHours(24);
    return LocalDateTime.now().isAfter(limite);
}

public void markAsExpired() {

    if (!canExpire()) {
        throw new IllegalStateException("La orden no puede expirar");
    }

    this.orderStatus = OrderStatus.CANCELLED;
    this.paymentStatus = PaymentStatus.EXPIRED;
}

//============================
//VALIDACION DE TRANSICION
//============================
public void changeStatus(OrderStatus newStatus) {

    if (this.orderStatus == OrderStatus.CANCELLED) {
        throw new IllegalStateException("No puedes modificar una orden cancelada");
    }

    boolean valid = switch (this.orderStatus) {
        case CREATED -> newStatus == OrderStatus.PAID_PENDING_STOCK || newStatus == OrderStatus.CANCELLED;
        case PAID_PENDING_STOCK -> newStatus == OrderStatus.PROCESSED;
        case PROCESSED -> newStatus == OrderStatus.SHIPPED;
        case SHIPPED -> newStatus == OrderStatus.DELIVERED;
        default -> false;
    };

    if (!valid) {
        throw new IllegalStateException(
            "Transición inválida de " + this.orderStatus + " a " + newStatus
        );
    }

    this.orderStatus = newStatus;
}

public boolean isReadyForProcessing() {
    return this.paymentStatus == PaymentStatus.PAID 
        && this.orderStatus == OrderStatus.PAID_PENDING_STOCK;
}
//============================
//PRODUCTO ENVIADO
//============================
public void markAsShipped(String tracking, String carrier) {

    if (this.orderStatus != OrderStatus.PROCESSED) {
        throw new IllegalStateException("La orden debe estar procesada antes de enviarse");
    }

    if (tracking == null || tracking.isBlank()) {
        throw new IllegalArgumentException("Tracking requerido");
    }

    this.trackingNumber = tracking;
    this.carrier = carrier;
    this.orderStatus = OrderStatus.SHIPPED;
}

//============================
//EMAIL FLAGS
//============================

public boolean isTransferInstructionsSent() {
 return transferInstructionsSent;
}

public void setTransferInstructionsSent(boolean transferInstructionsSent) {
 this.transferInstructionsSent = transferInstructionsSent;
}

public boolean isPaymentConfirmedSent() {
 return paymentConfirmedSent;
}

public void setPaymentConfirmedSent(boolean paymentConfirmedSent) {
 this.paymentConfirmedSent = paymentConfirmedSent;
}

public boolean isOrderExpiredSent() {
 return orderExpiredSent;
}

public void setOrderExpiredSent(boolean orderExpiredSent) {
 this.orderExpiredSent = orderExpiredSent;
}

public boolean isShippingConfirmationSent() {
 return shippingConfirmationSent;
}

public void setShippingConfirmationSent(boolean shippingConfirmationSent) {
 this.shippingConfirmationSent = shippingConfirmationSent;
}

public boolean isStockReduced() {
    return stockReduced;
}

public void setStockReduced(boolean stockReduced) {
    this.stockReduced = stockReduced;
}
public Store getStore() {
    return store;
}

public void setStore(Store store) {
    this.store = store;
}

}
