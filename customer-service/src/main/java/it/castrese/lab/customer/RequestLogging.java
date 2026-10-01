package it.castrese.lab.customer;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLogging extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RequestLogging.class);

  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String id = req.getHeader("X-Request-ID");
    if (id == null || !id.matches("[a-zA-Z0-9-]{1,64}")) id = UUID.randomUUID().toString();
    res.setHeader("X-Request-ID", id);
    MDC.put("requestId", id);
    long start = System.nanoTime();
    try {
      chain.doFilter(req, res);
    } finally {
      log.info(
          "requestId={} method={} path={} status={} durationMs={}",
          id,
          req.getMethod(),
          req.getRequestURI(),
          res.getStatus(),
          (System.nanoTime() - start) / 1_000_000);
      MDC.remove("requestId");
    }
  }
}
