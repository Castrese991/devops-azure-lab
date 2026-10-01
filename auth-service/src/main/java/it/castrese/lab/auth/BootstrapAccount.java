package it.castrese.lab.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BootstrapAccount implements ApplicationRunner {
  private final JdbcTemplate jdbc;
  private final PasswordEncoder encoder;
  private final String username, password;

  BootstrapAccount(
      JdbcTemplate jdbc,
      PasswordEncoder encoder,
      @Value("${bootstrap.username}") String username,
      @Value("${bootstrap.password}") String password) {
    this.jdbc = jdbc;
    this.encoder = encoder;
    this.username = username;
    this.password = password;
  }

  public void run(ApplicationArguments args) {
    if (username.isBlank()
        || username.length() > 100
        || password.length() < 16
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException(
          "Bootstrap username required; password must be 16-72 UTF-8 bytes");
    // Idempotent even when multiple replicas start together. Existing password is not overwritten.
    jdbc.update(
        "INSERT INTO accounts(username,password_hash) VALUES (?,?) ON CONFLICT (username) DO"
            + " NOTHING",
        username,
        encoder.encode(password));
  }
}
