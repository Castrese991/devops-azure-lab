package it.castrese.lab.customer;

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
class CustomerApiTest {
  @Autowired MockMvc mvc;
  @Autowired CustomerRepository repo;
  @MockitoBean JwtDecoder decoder;

  @BeforeEach
  void clean() {
    repo.deleteAll();
  }

  private org.springframework.test.web.servlet.request.RequestPostProcessor operator() {
    return jwt().authorities(new SimpleGrantedAuthority("SCOPE_lab"));
  }

  @Test
  void requiresAuthentication() throws Exception {
    mvc.perform(get("/api/customers")).andExpect(status().isUnauthorized());
  }

  @Test
  void requiresScope() throws Exception {
    mvc.perform(
            get("/api/customers")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_other"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void validatesAndPersists() throws Exception {
    mvc.perform(
            post("/api/customers")
                .with(operator())
                .contentType("application/json")
                .content("{\"name\":\"\",\"email\":\"bad\"}"))
        .andExpect(status().isBadRequest());
    String json = "{\"name\":\"Mario\",\"email\":\"MARIO@example.com\"}";
    mvc.perform(
            post("/api/customers").with(operator()).contentType("application/json").content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("mario@example.com"));
    mvc.perform(
            post("/api/customers").with(operator()).contentType("application/json").content(json))
        .andExpect(status().isConflict());
    assertThat(repo.count()).isEqualTo(1);
    mvc.perform(get("/api/customers/" + repo.findAll().get(0).id).with(operator()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Mario"));
  }

  @Test
  void handlesMissingAndBounds() throws Exception {
    mvc.perform(get("/api/customers/999999").with(operator())).andExpect(status().isNotFound());
    mvc.perform(get("/api/customers?size=1000").with(operator()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void healthIsPublic() throws Exception {
    mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
  }
}
