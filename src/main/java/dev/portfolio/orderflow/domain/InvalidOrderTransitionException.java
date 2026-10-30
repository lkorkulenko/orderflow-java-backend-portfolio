package dev.portfolio.orderflow.domain;

public final class InvalidOrderTransitionException extends RuntimeException {
    public InvalidOrderTransitionException(OrderStatus from, OrderStatus to) {
        super("Transition from " + from + " to " + to + " is not allowed");
    }
}
