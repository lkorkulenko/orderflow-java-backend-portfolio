package dev.portfolio.orderflow.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
    /** Scalar read model: avoids hydrating a full object graph. */
    interface LineSlice {
        Long getOrderId();
        int getQuantity();
        BigDecimal getUnitPrice();
    }

    @Query("""
        select l.order.id as orderId, l.quantity as quantity, l.unitPrice as unitPrice
        from OrderLine l
        where l.order.id in :orderIds
        """)
    List<LineSlice> findSlicesForOrders(@Param("orderIds") Collection<Long> orderIds);
}
