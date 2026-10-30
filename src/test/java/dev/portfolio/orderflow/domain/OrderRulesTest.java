package dev.portfolio.orderflow.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class OrderRulesTest {
    @Test void permitsExpectedTransitions() {
        assertThatNoException().isThrownBy(() -> OrderRules.requireTransition(OrderStatus.NEW, OrderStatus.PROCESSING));
        assertThatNoException().isThrownBy(() -> OrderRules.requireTransition(OrderStatus.PROCESSING, OrderStatus.SHIPPED));
    }

    @Test void forbidsReopeningShippedOrders() {
        assertThatThrownBy(() -> OrderRules.requireTransition(OrderStatus.SHIPPED, OrderStatus.NEW))
            .isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test void totalsUseExactDecimalArithmetic() {
        assertThat(OrderRules.lineTotal(new BigDecimal("0.10"), 3))
            .isEqualByComparingTo("0.30");
    }
}
