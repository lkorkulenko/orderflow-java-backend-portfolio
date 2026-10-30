package dev.portfolio.orderflow.api;

import dev.portfolio.orderflow.api.OrderDtos.*;
import dev.portfolio.orderflow.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @GetMapping
    public PageResult<OrderSummary> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return service.listOptimized(page, size);
    }

    @GetMapping("/legacy")
    public PageResult<OrderSummary> legacy(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return service.listLegacy(page, size);
    }

    @GetMapping("/{id}")
    public OrderDetails details(@PathVariable Long id) { return service.details(id); }

    @PostMapping
    public ResponseEntity<OrderDetails> create(@Valid @RequestBody CreateOrder request) {
        Long id = service.create(request);
        return ResponseEntity.created(URI.create("/api/orders/" + id)).body(service.details(id));
    }

    @PatchMapping("/{id}/status")
    public OrderDetails changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatus request) {
        service.transition(id, request);
        return service.details(id);
    }
}
