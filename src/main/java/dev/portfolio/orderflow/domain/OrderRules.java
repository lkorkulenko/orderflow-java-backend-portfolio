package dev.portfolio.orderflow.domain;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Objects;

/** Pure business logic: testable independently of Spring and the database. */
public final class OrderRules {
    private OrderRules() { }

    public static void requireTransition(OrderStatus from, OrderStatus to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        boolean allowed = switch (from) {
            case NEW -> EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED).contains(to);
            case PROCESSING -> EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED).contains(to);
            case SHIPPED, CANCELLED -> false;
        };
        if (!allowed) throw new InvalidOrderTransitionException(from, to);
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity <= 0 || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Quantity and unit price must be positive");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
