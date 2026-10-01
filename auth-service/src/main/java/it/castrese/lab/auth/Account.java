package it.castrese.lab.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, unique = true, length = 100)
  String username;

  @Column(nullable = false, length = 100)
  String passwordHash;

  protected Account() {}

  Account(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
  }
}
