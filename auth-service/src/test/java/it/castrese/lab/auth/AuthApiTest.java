package it.castrese.lab.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.*;
import java.security.interfaces.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AuthController.class,
    properties = {
      "security.jwt.issuer=devops-azure-lab",
      "security.jwt.audience=lab-api",
      "security.jwt.ttl-seconds=900"
    })
@Import({AuthApiTest.Keys.class, SecurityConfig.class})
class AuthApiTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired JwtEncoder encoder;
  @Autowired PasswordEncoder passwords;
  @MockitoBean AccountRepository accounts;
  @MockitoBean JwtDecoder decoder;
  static final KeyPair PAIR = pair();

  static KeyPair pair() {
    try {
      var g = KeyPairGenerator.getInstance("RSA");
      g.initialize(2048);
      return g.generateKeyPair();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @TestConfiguration
  static class Keys {
    @Bean
    PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder() {
      var key =
          new com.nimbusds.jose.jwk.RSAKey.Builder((RSAPublicKey) PAIR.getPublic())
              .privateKey((RSAPrivateKey) PAIR.getPrivate())
              .keyID("test")
              .build();
      return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }
  }

  JwtDecoder realDecoder() throws Exception {
    String pem =
        "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder().encodeToString(PAIR.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";
    return new SecurityConfig()
        .jwtDecoder(new ByteArrayResource(pem.getBytes()), "devops-azure-lab", "lab-api");
  }

  @Test
  void loginIssuesValidSignedToken() throws Exception {
    var a = new Account("operator", passwords.encode("correct-password"));
    a.id = 1L;
    when(accounts.findByUsername("operator")).thenReturn(Optional.of(a));
    var result =
        mvc.perform(
                post("/api/auth/login")
                    .contentType("application/json")
                    .content("{\"username\":\"operator\",\"password\":\"correct-password\"}"))
            .andExpect(status().isOk())
            .andReturn();
    var jwt =
        realDecoder()
            .decode(
                mapper
                    .readTree(result.getResponse().getContentAsString())
                    .get("accessToken")
                    .asText());
    assertThat(jwt.getSubject()).isEqualTo("1");
    assertThat(jwt.getClaimAsString("scope")).isEqualTo("lab");
  }

  @Test
  void wrongPasswordIs401() throws Exception {
    var a = new Account("operator", passwords.encode("correct-password"));
    when(accounts.findByUsername("operator")).thenReturn(Optional.of(a));
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"operator\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unknownUserIs401() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"missing\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void invalidInputIs400() throws Exception {
    mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejectsWrongAudienceExpiredAndWrongIssuer() throws Exception {
    for (int i = 0; i < 3; i++) {
      var claims =
          JwtClaimsSet.builder()
              .issuer(i == 2 ? "wrong" : "devops-azure-lab")
              .subject("1")
              .audience(List.of(i == 0 ? "wrong" : "lab-api"))
              .issuedAt(Instant.now().minusSeconds(600))
              .expiresAt(Instant.now().plusSeconds(i == 1 ? -120 : 900))
              .build();
      String token =
          encoder
              .encode(
                  JwtEncoderParameters.from(
                      JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
              .getTokenValue();
      assertThatThrownBy(() -> realDecoder().decode(token)).isInstanceOf(JwtException.class);
    }
  }
}
