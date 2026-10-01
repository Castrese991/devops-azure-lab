package it.castrese.lab.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
class AuthController {
  private final AccountRepository accounts;
  private final PasswordEncoder passwords;
  private final JwtEncoder tokens;
  private final String issuer, audience, dummyHash;
  private final long ttl;

  AuthController(
      AccountRepository accounts,
      PasswordEncoder passwords,
      JwtEncoder tokens,
      @Value("${security.jwt.issuer}") String issuer,
      @Value("${security.jwt.audience}") String audience,
      @Value("${security.jwt.ttl-seconds}") long ttl) {
    this.accounts = accounts;
    this.passwords = passwords;
    this.tokens = tokens;
    this.issuer = issuer;
    this.audience = audience;
    this.ttl = ttl;
    this.dummyHash = passwords.encode("dummy-password-never-valid");
  }

  record Login(
      @NotBlank @Size(max = 100) String username, @NotBlank @Size(max = 72) String password) {}

  record AccessToken(String accessToken, String tokenType, long expiresIn) {}

  @PostMapping("/login")
  ResponseEntity<AccessToken> login(@Valid @RequestBody Login request) {
    var account = accounts.findByUsername(request.username());
    boolean matches =
        passwords.matches(request.password(), account.map(a -> a.passwordHash).orElse(dummyHash));
    if (account.isEmpty() || !matches)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(account.get().id.toString())
            .audience(List.of(audience))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttl))
            .claim("scope", "lab")
            .build();
    String token =
        tokens
            .encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
            .getTokenValue();
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new AccessToken(token, "Bearer", ttl));
  }
}
