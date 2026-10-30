package dev.portfolio.orderflow.service;

import dev.portfolio.orderflow.api.OrderDtos.*;
import dev.portfolio.orderflow.domain.OrderRules;
import dev.portfolio.orderflow.persistence.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final PurchaseOrderRepository orders;
    private final OrderLineRepository lines;

    public OrderService(PurchaseOrderRepository orders, OrderLineRepository lines) {
        this.orders = orders;
        this.lines = lines;
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be >= 0, and size must be 1..100");
        }
        return PageRequest.of(page, size,
            Sort.by(Sort.Order.desc("placedAt"), Sort.Order.desc("id")));
    }

    /** PROBLEM: one SELECT for the page and one extra SELECT per order's lines. */
    @Transactional(readOnly = true)
    public PageResult<OrderSummary> listLegacy(int page, int size) {
        Page<PurchaseOrder> result = orders.findAll(pageable(page, size));
        List<OrderSummary> summaries = result.getContent().stream()
            .map(order -> summarize(order, order.getLines().stream()
                .map(line -> new LineAmount(line.getQuantity(), line.getUnitPrice())).toList()))
            .toList();
        return new PageResult<>(summaries, result.getNumber(), result.getSize(),
                                result.getTotalElements(), result.getTotalPages());
    }

    /** FIX: page orders first, then fetch only the required line columns in one IN query. */
    @Transactional(readOnly = true)
    public PageResult<OrderSummary> listOptimized(int page, int size) {
        Page<PurchaseOrder> result = orders.findAll(pageable(page, size));
        List<Long> ids = result.getContent().stream().map(PurchaseOrder::getId).toList();
        Map<Long, List<LineAmount>> amounts = ids.isEmpty() ? Map.of() :
            lines.findSlicesForOrders(ids).stream().collect(Collectors.groupingBy(
                OrderLineRepository.LineSlice::getOrderId,
                Collectors.mapping(line -> new LineAmount(line.getQuantity(), line.getUnitPrice()),
                                   Collectors.toList())));
        List<OrderSummary> summaries = result.getContent().stream()
            .map(order -> summarize(order, amounts.getOrDefault(order.getId(), List.of())))
            .toList();
        return new PageResult<>(summaries, result.getNumber(), result.getSize(),
                                result.getTotalElements(), result.getTotalPages());
    }

    private record LineAmount(int quantity, BigDecimal unitPrice) { }

    private OrderSummary summarize(PurchaseOrder order, List<LineAmount> lines) {
        long count = lines.stream().mapToLong(LineAmount::quantity).sum();
        BigDecimal total = lines.stream().map(line -> OrderRules.lineTotal(line.unitPrice(), line.quantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderSummary(order.getId(), order.getClientReference(), order.getStatus(),
                                order.getPlacedAt(), count, total);
    }

    @Transactional(readOnly = true)
    public OrderDetails details(Long id) {
        PurchaseOrder order = orders.findWithLinesById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        List<LineDetail> details = order.getLines().stream()
            .map(line -> new LineDetail(line.getId(), line.getSku(), line.getQuantity(),
                line.getUnitPrice(), OrderRules.lineTotal(line.getUnitPrice(), line.getQuantity())))
            .toList();
        BigDecimal total = details.stream().map(LineDetail::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderDetails(order.getId(), order.getClientReference(), order.getStatus(),
                                order.getPlacedAt(), order.getVersion(), details, total);
    }

    @Transactional
    public Long create(CreateOrder request) {
        PurchaseOrder order = new PurchaseOrder(request.clientReference().trim(), Instant.now());
        request.lines().forEach(line -> order.addLine(line.sku().trim(), line.quantity(), line.unitPrice()));
        return orders.saveAndFlush(order).getId();
    }

    @Transactional
    public void transition(Long id, ChangeStatus request) {
        PurchaseOrder order = orders.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        order.changeStatus(request.status());
        // Dirty checking flushes the state; @Version prevents silent lost updates.
    }
}
