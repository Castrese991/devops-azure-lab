package it.castrese.lab.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiTest {
  @Autowired MockMvc mvc;
  @Autowired OrderRepository repo;
  @MockitoBean JwtDecoder decoder;
  @MockitoBean CustomerClient customers;

  @BeforeEach
  void clean() {
    repo.deleteAll();
    when(decoder.decode("test"))
        .thenReturn(
            org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test")
                .header("alg", "RS256")
                .subject("1")
                .claim("scope", "lab")
                .build());
  }

  private org.springframework.test.web.servlet.request.RequestPostProcessor operator() {
    return jwt().authorities(new SimpleGrantedAuthority("SCOPE_lab"));
  }

  private static final String VALID =
      "{\"customerId\":1,\"description\":\"Lab order\",\"amount\":19.95}";

  @Test
  void unauthorized() throws Exception {
    mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
  }

  @Test
  void createsAfterCustomerCheck() throws Exception {
    mvc.perform(
            post("/api/orders")
                .with(operator())
                .header("Authorization", "Bearer test")
                .contentType("application/json")
                .content(VALID))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("CREATED"));
    verify(customers).requireCustomer(1L, "Bearer test");
    assertThat(repo.count()).isEqualTo(1);
    mvc.perform(get("/api/orders/" + repo.findAll().get(0).id).with(operator()))
        .andExpect(status().isOk());
  }

  @Test
  void unavailableCustomerDoesNotPersist() throws Exception {
    doThrow(
            new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Customer verification temporarily unavailable"))
        .when(customers)
        .requireCustomer(anyLong(), anyString());
    mvc.perform(
            post("/api/orders")
                .with(operator())
                .header("Authorization", "Bearer test")
                .contentType("application/json")
                .content(VALID))
        .andExpect(status().isServiceUnavailable());
    assertThat(repo.count()).isZero();
  }

  @Test
  void invalidAmountDoesNotCallCustomer() throws Exception {
    mvc.perform(
            post("/api/orders")
                .with(operator())
                .header("Authorization", "Bearer test")
                .contentType("application/json")
                .content(VALID.replace("19.95", "-1")))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(customers);
    assertThat(repo.count()).isZero();
  }
}
