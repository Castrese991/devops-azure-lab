package it.castrese.lab.customer;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "customers")
class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, length = 120)
  String name;

  @Column(nullable = false, unique = true, length = 254)
  String email;

  @Column(nullable = false)
  Instant createdAt;

  protected Customer() {}

  Customer(String name, String email) {
    this.name = name;
    this.email = email;
    this.createdAt = Instant.now();
  }
}
