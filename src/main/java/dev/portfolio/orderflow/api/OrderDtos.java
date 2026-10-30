package dev.portfolio.orderflow.api;

import dev.portfolio.orderflow.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() { }

    public record LineInput(@NotBlank String sku,
                            @Min(1) int quantity,
                            @NotNull @DecimalMin("0.01") BigDecimal unitPrice) { }

    public record CreateOrder(@NotBlank @Size(max = 80) String clientReference,
                              @NotEmpty List<@Valid LineInput> lines) { }

    public record ChangeStatus(@NotNull OrderStatus status) { }

    public record OrderSummary(Long id, String clientReference, OrderStatus status,
                               Instant placedAt, long itemCount, BigDecimal total) { }

    public record LineDetail(Long id, String sku, int quantity,
                             BigDecimal unitPrice, BigDecimal lineTotal) { }

    public record OrderDetails(Long id, String clientReference, OrderStatus status,
                               Instant placedAt, Long version, List<LineDetail> lines,
                               BigDecimal total) { }

    public record PageResult<T>(List<T> content, int page, int size, long totalElements,
                                int totalPages) { }
}
