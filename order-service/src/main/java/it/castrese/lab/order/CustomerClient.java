package it.castrese.lab.order;

import java.time.Duration;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Component
class CustomerClient {
  private final RestClient client;

  CustomerClient(@Value("${customer.base-url}") String url) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(2));
    factory.setReadTimeout(Duration.ofSeconds(3));
    client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }

  void requireCustomer(Long id, String authorization) {
    try {
      client
          .get()
          .uri("/api/customers/{id}", id)
          .header(HttpHeaders.AUTHORIZATION, authorization)
          .header(
              "X-Request-ID",
              MDC.get("requestId") == null
                  ? java.util.UUID.randomUUID().toString()
                  : MDC.get("requestId"))
          .retrieve()
          .toBodilessEntity();
    } catch (HttpClientErrorException.NotFound e) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unknown customer");
    } catch (RestClientException e) {
      LoggerFactory.getLogger(CustomerClient.class)
          .warn("Customer lookup failed: {}", e.getClass().getSimpleName());
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Customer verification temporarily unavailable");
    }
  }
}
