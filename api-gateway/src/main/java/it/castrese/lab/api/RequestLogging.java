package it.castrese.lab.api;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
class RequestLogging implements GlobalFilter, Ordered {
  private static final Logger log = LoggerFactory.getLogger(RequestLogging.class);

  @Override
  public int getOrder() {
    return -1;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String supplied = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
    String id =
        supplied != null && supplied.matches("[a-zA-Z0-9-]{1,64}")
            ? supplied
            : UUID.randomUUID().toString();
    long start = System.nanoTime();
    var forwarded =
        exchange.mutate().request(r -> r.headers(h -> h.set("X-Request-ID", id))).build();
    exchange.getResponse().getHeaders().set("X-Request-ID", id);
    return chain
        .filter(forwarded)
        .doFinally(
            signal -> {
              var status = exchange.getResponse().getStatusCode();
              log.info(
                  "requestId={} method={} path={} status={} durationMs={} completion={}",
                  id,
                  exchange.getRequest().getMethod(),
                  exchange.getRequest().getPath().value(),
                  status == null ? "unset" : status.value(),
                  (System.nanoTime() - start) / 1_000_000,
                  signal);
            });
  }
}
