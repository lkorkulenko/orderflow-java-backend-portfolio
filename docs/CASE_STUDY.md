# Case study: Removing N+1 database queries from a Java order API

**Project type:** Independent portfolio demonstration (fictional B2B order service). No employer or client code, data, or branding.

## Problem
An order-list endpoint fetched a page of order headers, then lazily loaded the lines for each order. With 20 orders on one page, the legacy version issued 22 prepared SQL statements in the integration test.

## Approach
1. Preserve the same deterministic paginated order listing and JSON response.
2. Query order headers and their total count using normal Spring Data pagination.
3. Fetch the line data for that page in one scalar projection query filtered by order IDs.
4. Calculate quantities and monetary totals using exact `BigDecimal` arithmetic.
5. Compare the legacy and optimized JSON and instrument Hibernate prepared-statement counts in an automated integration test.

## Verified result — GitHub Actions
On October 30, 2026, GitHub Actions completed `mvn -B -ntp verify` successfully on Java 21.

| Metric | Legacy endpoint | Optimized endpoint |
|---|---:|---:|
| Prepared SQL statements for 20-order H2 test page | 22 | 3 |
| API response | Same JSON | Same JSON |

That is **19 fewer statements, or an 86.4% reduction in statements for this test case**. The full Maven suite passed: **6 tests, 0 failures, 0 errors, 0 skipped**.

[View the successful CI run](https://github.com/lkorkulenko/orderflow-java-backend-portfolio/actions/runs/41037257946).

This is **not a measured response-time or throughput improvement**. No controlled wall-clock benchmark, external PostgreSQL run, or realistic network-latency test has been performed.

## Other engineering safeguards
- Flyway migration and relational constraints
- Input validation and structured HTTP problem details
- Business-state transitions and exact monetary arithmetic
- Optimistic locking using JPA `@Version`
- `open-in-view=false`
- Unit tests and MockMvc API integration tests

## Limitations
This is intentionally a local portfolio demonstration, not a deployable commercial service. There is no authentication, authorization, audit trail, rate limiting, or hardened production security configuration. The deliberately inefficient legacy endpoint is retained only for the comparison. The batch read currently gathers order-line projections in memory for a bounded page (max 100 orders); at larger scales a SQL aggregate or materialized totals might be preferable. SQL counts can vary across engines and ORM versions.

## Release review notes

- PR #1 was merged into `main`: added SKU length and decimal precision checks with a passing regression test.
- An additional release-readiness change rejects null line items and empty order-line collections at the request boundary.
- The root README now includes a before/after query-flow diagram and CI badge.
- The repository deliberately uses local-only binding and contains no client/employer datasets or credentials.

## Outside the scope of this demonstration

- PostgreSQL-backed automated verification (currently H2)
- Production authentication, authorization, abuse prevention and secrets management
- Measured wall-clock latency, throughput and load testing
- Professional production deployment and observability

None of these unimplemented production capabilities are being represented as delivered.
