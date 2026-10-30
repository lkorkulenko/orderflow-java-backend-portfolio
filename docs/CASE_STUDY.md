# Case study: Removing N+1 database queries from a Java order API

**Project type:** Independent portfolio demonstration (fictional B2B order service). No client source code, data, branding, or private architecture has been used.

## Problem

An existing order-list API retrieves a page of order headers. Its mapper then navigates each JPA order's lazy `lines` collection to calculate totals. Because each collection is loaded separately, the number of queries grows approximately linearly with page size. On a remote database this creates avoidable latency.

## Engineering approach

1. Keep the legacy endpoint available for controlled comparison.
2. Keep deterministic, database-backed pagination on purchase orders.
3. Load only the line fields required for a summary with a single batch query using the page's order IDs.
4. Aggregate quantities and totals with `BigDecimal` in the service layer.
5. Preserve API response shape and compare old/new outputs in integration tests.
6. Use Hibernate Statistics to assert a bound on prepared statements, not only subjective timing.

## Safeguards unrelated to N+1

- Flyway schema migrations with referential integrity and money constraints.
- Unique customer order reference, handled as an HTTP conflict.
- Validated API inputs with structured problem responses.
- Transition rules for order lifecycle, tested without Spring.
- Optimistic locking via JPA `@Version` to prevent silent lost updates.
- `open-in-view=false` to keep persistence access inside service transactions.

## Evidence checklist (fill after CI)

- [ ] `mvn -B -ntp verify` completes successfully.
- [ ] SQL count output copied from `OrderApiIntegrationTest` (legacy vs modern).
- [ ] Test result file or CI link attached as evidence.
- [ ] If discussing latency, collect controlled multi-run benchmarks with warmup and realistic database latency.
- [ ] GitHub repository URL added to Upwork portfolio.

## Limitations and trade-offs

The batch read strategy holds one page of line projections in memory. It is suitable for modest bounded pages (maximum 100 orders here), but could require database-side aggregation or denormalized totals at high scale. The sample does not address authentication, distributed tracing, object-level authorization, audit requirements, backpressure, or production observability. The intentionally slow legacy endpoint must not be exposed in a production system.

## Outcome

The code demonstrates a bounded-query design and an automated regression assertion. Actual before/after SQL counts will be recorded when the Spring integration suite is executed. **No measured speedup is currently claimed.**
