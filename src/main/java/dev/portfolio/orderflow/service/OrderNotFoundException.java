package dev.portfolio.orderflow.service;

public final class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order " + id + " does not exist");
    }
}
