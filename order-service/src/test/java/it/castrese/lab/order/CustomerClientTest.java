package it.castrese.lab.order;

import static org.assertj.core.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;

class CustomerClientTest {
  HttpServer server;
  CustomerClient client;
  AtomicReference<String> authorization = new AtomicReference<>();
  int response = 200;

  @BeforeEach
  void start() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/api/customers/1",
        exchange -> {
          authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
          exchange.sendResponseHeaders(response, -1);
          exchange.close();
        });
    server.start();
    client = new CustomerClient("http://127.0.0.1:" + server.getAddress().getPort());
  }

  @AfterEach
  void stop() {
    server.stop(0);
  }

  @Test
  void forwardsToken() {
    client.requireCustomer(1L, "Bearer abc");
    assertThat(authorization.get()).isEqualTo("Bearer abc");
  }

  @Test
  void missingCustomer() {
    response = 404;
    assertThatThrownBy(() -> client.requireCustomer(1L, "Bearer abc"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(422));
  }

  @Test
  void upstreamFailure() {
    response = 500;
    assertThatThrownBy(() -> client.requireCustomer(1L, "Bearer abc"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
  }

  @Test
  void connectionFailure() {
    server.stop(0);
    assertThatThrownBy(() -> client.requireCustomer(1L, "Bearer abc"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
  }
}
