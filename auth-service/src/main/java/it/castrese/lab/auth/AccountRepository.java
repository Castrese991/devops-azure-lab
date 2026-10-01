package it.castrese.lab.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<Account> findByUsername(String username);
}
