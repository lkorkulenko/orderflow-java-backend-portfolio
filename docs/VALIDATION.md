# Verification status

## Latest verified GitHub Actions run — October 31, 2026

[Successful 8-test CI run](https://github.com/lkorkulenko/orderflow-java-backend-portfolio/actions/runs/41202708566) on Java 21, Maven `verify`:

- **BUILD SUCCESS**
- **8 tests**, 0 failures, 0 errors, 0 skipped (five HTTP integration tests and three domain unit tests)
- H2-backed Spring HTTP integration tests and Flyway migrations
- `SQL statements for 20-order page: legacy=22, optimized=3`
- Legacy and optimized JSON responses asserted to be identical
- Invalid line-item limits, null and empty order lines, invalid transitions, and duplicate references covered

PR #1 and PR #2 have been merged to `main`. The CI evidence above was recorded on PR #2's branch before merge; its changes were squash-merged into `main`. The latest `main` build status should be checked separately after the documentation update.

**Interpretation:** The 19-statement reduction (86.4%) is measured on a 20-order test fixture. This **does not** establish a response-time, throughput, or production performance improvement.

## Still outside the verified scope

- Production PostgreSQL-backed integration tests (current suite uses H2)
- Local Windows end-to-end demonstration
- Authentication, authorization, auditing, rate limiting, and deployment security hardening
- Benchmarked wall-clock latency under realistic database/network conditions

The project is an independent educational demonstration with fictional data, not production or employer/client code.

## Reproduce locally on Windows

```powershell
java -version
mvn -version
./scripts/domain-smoke.ps1
mvn -B -ntp clean verify
```

Optional PostgreSQL setup is documented in the root README.
