package it.castrese.lab.api;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayTest {
  static HttpServer backend;
  static AtomicReference<String> auth = new AtomicReference<>();
  @Autowired WebTestClient client;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry props) throws Exception {
    backend = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    backend.createContext(
        "/api/",
        e -> {
          auth.set(e.getRequestHeaders().getFirst("Authorization"));
          byte[] body = e.getRequestURI().getPath().getBytes();
          e.sendResponseHeaders(200, body.length);
          e.getResponseBody().write(body);
          e.close();
        });
    backend.start();
    for (String s : new String[] {"AUTH", "CUSTOMER", "ORDER"})
      props.add(s + "_BASE_URL", () -> "http://127.0.0.1:" + backend.getAddress().getPort());
  }

  @AfterAll
  static void stop() {
    backend.stop(0);
  }

  @Test
  void routesAllServicesAndPreservesAuthorization() {
    for (String path : new String[] {"/api/auth/login", "/api/customers/1", "/api/orders/1"}) {
      client
          .get()
          .uri(path)
          .header("Authorization", "Bearer test")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(String.class)
          .isEqualTo(path);
      org.assertj.core.api.Assertions.assertThat(auth.get()).isEqualTo("Bearer test");
    }
  }

  @Test
  void health() {
    client.get().uri("/actuator/health/readiness").exchange().expectStatus().isOk();
  }

  @Test
  void unknownRoute() {
    client.get().uri("/not-routed").exchange().expectStatus().isNotFound();
  }
}
