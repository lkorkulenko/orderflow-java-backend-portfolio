# Upwork portfolio entry — prepared for publication after owner review

This is an **independent, original demonstration**, not client work or a production deployment.

## Portfolio entry

**Title:** Java Backend Performance Modernization

**Role:** Java Backend Engineer — API Architecture, JPA, Automated Testing

**Short overview (Upwork):**

> Built an independent Java/Spring Boot case study to diagnose and fix an N+1 query problem. In CI, a 20-order API listing dropped from 22 to 3 Hibernate prepared SQL statements (86.4% fewer), with identical JSON responses and automated regression tests. This measures queries, not response time.

**Relevant skills:** Java, Spring Boot, Hibernate, RESTful API, Unit Testing.

## Detailed case study

**Problem.** A realistic B2B order-list API loaded each order's lines lazily, introducing an extra SQL statement per order.

**My approach.** I preserved the paginated API response, replaced per-order lazy loading with a single batched scalar read for each page, and added tests comparing both JSON outputs and Hibernate's prepared-statement counts.

**Verified result.** Against the H2-backed 20-order fixture in GitHub Actions, the legacy and optimized implementations issued 22 and 3 prepared SQL statements, respectively: **19 fewer statements / 86.4% reduction in statement count**. The latest GitHub Actions verification passed **8 tests with no failures** after the input-validation enhancements were merged. These results do not quantify response-time or production throughput changes.

**Beyond the performance fix.** The sample contains Flyway migrations, JPA entities, REST APIs, input validation, business-state transitions, optimistic locking and reproducible CI.

**Evidence:** [Verified 8-test CI run](https://github.com/lkorkulenko/orderflow-java-backend-portfolio/actions/runs/41202708566); [Root case study](../docs/CASE_STUDY.md).

## Suggested portfolio media

1. A simple before/after diagram comparing 20 lazy loads versus one batched read.
2. A screenshot of the successful CI run with statement-count output.
3. A repository link after the owner authorizes making the repo public.

## Honesty and disclosure

- Explicitly label this as an independent demonstration using fictional data.
- Do **not** imply that the code came from a past client, VARTEQ/Follett, or an actual production incident.
- Do **not** claim that 86.4% fewer SQL statements means an 86.4% response-time speedup.
- Confirm the repository is public and anonymously viewable before linking it in an Upwork portfolio entry. Otherwise clients will not be able to see it.
