package it.castrese.lab.auth;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

@Configuration
class TokenConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtEncoder jwtEncoder(
      @Value("${security.jwt.public-key}") Resource publicKey,
      @Value("${security.jwt.private-key}") Resource privateKey)
      throws Exception {
    try (var pub = publicKey.getInputStream();
        var priv = privateKey.getInputStream()) {
      RSAKey rsa =
          new RSAKey.Builder(RsaKeyConverters.x509().convert(pub))
              .privateKey(RsaKeyConverters.pkcs8().convert(priv))
              .keyID("lab-key-1")
              .build();
      return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsa)));
    }
  }
}
