package it.castrese.lab.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
class OrderController {
  private final OrderRepository orders;
  private final CustomerClient customers;

  OrderController(OrderRepository orders, CustomerClient customers) {
    this.orders = orders;
    this.customers = customers;
  }

  record CreateOrder(
      @NotNull @Positive Long customerId,
      @NotBlank @Size(max = 200) String description,
      @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {}

  record OrderView(
      Long id,
      Long customerId,
      String description,
      BigDecimal amount,
      String currency,
      String status,
      Instant createdAt) {
    static OrderView from(PurchaseOrder o) {
      return new OrderView(
          o.id, o.customerId, o.description, o.amount, o.currency, o.status, o.createdAt);
    }
  }

  @PostMapping
  ResponseEntity<OrderView> create(
      @Valid @RequestBody CreateOrder body,
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    // Remote I/O before opening the database transaction; no automatic POST retry.
    customers.requireCustomer(body.customerId(), authorization);
    var saved =
        orders.saveAndFlush(
            new PurchaseOrder(body.customerId(), body.description().trim(), body.amount()));
    return ResponseEntity.created(URI.create("/api/orders/" + saved.id))
        .body(OrderView.from(saved));
  }

  @GetMapping("/{id}")
  OrderView get(@PathVariable Long id) {
    return orders
        .findById(id)
        .map(OrderView::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
  }

  @GetMapping
  List<OrderView> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    if (page < 0 || size < 1 || size > 100)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page >= 0; size 1-100");
    return orders
        .findAll(PageRequest.of(page, size, Sort.by("id")))
        .map(OrderView::from)
        .getContent();
  }
}
