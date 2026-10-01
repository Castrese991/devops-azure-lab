package it.castrese.lab.order;

import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {
  @Bean
  JwtDecoder jwtDecoder(
      @Value("${security.jwt.public-key}") Resource key,
      @Value("${security.jwt.issuer}") String issuer,
      @Value("${security.jwt.audience}") String audience)
      throws Exception {
    RSAPublicKey pub;
    try (var in = key.getInputStream()) {
      pub = RsaKeyConverters.x509().convert(in);
    }
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(pub).build();
    OAuth2TokenValidator<Jwt> aud =
        jwt ->
            jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), aud));
    return decoder;
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/actuator/health", "/actuator/health/**")
                    .permitAll()
                    .anyRequest()
                    .hasAuthority("SCOPE_lab"))
        .oauth2ResourceServer(o -> o.jwt(j -> {}))
        .build();
  }
}
