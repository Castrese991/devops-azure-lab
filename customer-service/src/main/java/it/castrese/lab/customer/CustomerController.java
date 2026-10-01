package it.castrese.lab.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/customers")
class CustomerController {
  private final CustomerRepository customers;

  CustomerController(CustomerRepository customers) {
    this.customers = customers;
  }

  record CreateCustomer(
      @NotBlank @Size(max = 120) String name, @NotBlank @Email @Size(max = 254) String email) {}

  record CustomerView(Long id, String name, String email, Instant createdAt) {
    static CustomerView from(Customer c) {
      return new CustomerView(c.id, c.name, c.email, c.createdAt);
    }
  }

  @PostMapping
  ResponseEntity<CustomerView> create(@Valid @RequestBody CreateCustomer body) {
    var saved =
        customers.saveAndFlush(
            new Customer(body.name().trim(), body.email().trim().toLowerCase(Locale.ROOT)));
    return ResponseEntity.created(URI.create("/api/customers/" + saved.id))
        .body(CustomerView.from(saved));
  }

  @GetMapping("/{id}")
  CustomerView get(@PathVariable Long id) {
    return customers
        .findById(id)
        .map(CustomerView::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
  }

  @GetMapping
  List<CustomerView> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    if (page < 0 || size < 1 || size > 100)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page >= 0; size 1-100");
    return customers
        .findAll(PageRequest.of(page, size, Sort.by("id")))
        .map(CustomerView::from)
        .getContent();
  }
}
