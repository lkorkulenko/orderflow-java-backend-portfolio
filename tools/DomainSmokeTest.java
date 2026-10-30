import dev.portfolio.orderflow.domain.*;
import java.math.BigDecimal;

/** Run with scripts/domain-smoke.ps1 (uses JDK only; no Maven needed). */
public class DomainSmokeTest {
    public static void main(String[] args) {
        OrderRules.requireTransition(OrderStatus.NEW, OrderStatus.PROCESSING);
        OrderRules.requireTransition(OrderStatus.PROCESSING, OrderStatus.SHIPPED);
        if (OrderRules.lineTotal(new BigDecimal("0.10"), 3)
            .compareTo(new BigDecimal("0.30")) != 0) {
            throw new AssertionError("Unexpected money calculation");
        }
        try {
            OrderRules.requireTransition(OrderStatus.SHIPPED, OrderStatus.NEW);
            throw new AssertionError("Invalid transition accepted");
        } catch (InvalidOrderTransitionException expected) { }
        System.out.println("PASS: money calculations and order state transitions");
    }
}
