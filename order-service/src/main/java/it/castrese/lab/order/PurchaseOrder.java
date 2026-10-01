package it.castrese.lab.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_orders")
class PurchaseOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false)
  Long customerId;

  @Column(nullable = false, length = 200)
  String description;

  @Column(nullable = false, precision = 12, scale = 2)
  BigDecimal amount;

  @Column(nullable = false, length = 3)
  String currency;

  @Column(nullable = false, length = 20)
  String status;

  @Column(nullable = false)
  Instant createdAt;

  protected PurchaseOrder() {}

  PurchaseOrder(Long customerId, String description, BigDecimal amount) {
    this.customerId = customerId;
    this.description = description;
    this.amount = amount;
    this.currency = "EUR";
    this.status = "CREATED";
    this.createdAt = Instant.now();
  }
}
