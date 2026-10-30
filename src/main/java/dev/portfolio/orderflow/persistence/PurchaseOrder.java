package dev.portfolio.orderflow.persistence;

import dev.portfolio.orderflow.domain.OrderRules;
import dev.portfolio.orderflow.domain.OrderStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_reference", nullable = false, unique = true, length = 80)
    private String clientReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** Deliberately LAZY: the legacy listing will trigger N+1. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    protected PurchaseOrder() { }

    public PurchaseOrder(String reference, Instant placedAt) {
        this.clientReference = reference;
        this.placedAt = placedAt;
        this.status = OrderStatus.NEW;
    }

    public void addLine(String sku, int quantity, java.math.BigDecimal unitPrice) {
        OrderLine line = new OrderLine(this, sku, quantity, unitPrice);
        this.lines.add(line);
    }

    public void changeStatus(OrderStatus desired) {
        OrderRules.requireTransition(this.status, desired);
        this.status = desired;
    }

    public Long getId() { return id; }
    public String getClientReference() { return clientReference; }
    public OrderStatus getStatus() { return status; }
    public Instant getPlacedAt() { return placedAt; }
    public Long getVersion() { return version; }
    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }
}
