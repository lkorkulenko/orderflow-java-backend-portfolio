# Verification status

## Verified in GitHub Actions (October 30, 2026)

[Successful run #41037257946](https://github.com/lkorkulenko/orderflow-java-backend-portfolio/actions/runs/41037257946) on Java 21 against commit `b151d5e`:

- Maven `verify`: **BUILD SUCCESS**
- **6 tests**, 0 failures, 0 errors, 0 skipped
- H2-backed Spring HTTP integration tests with Flyway
- `SQL statements for 20-order page: legacy=22, optimized=3`
- Optimized and legacy JSON responses asserted identical in the tested fixture
- Duplicate references and invalid order transitions return HTTP 409 in tests

**Interpretation:** 19 fewer Hibernate prepared SQL statements (86.4%) for this 20-order test fixture. This is **not** a measured improvement in API latency, throughput, or production performance.

## Still outstanding

- Human review of implementation and code quality
- PostgreSQL-backed validation (CI currently tests with H2)
- Local Windows end-to-end demonstration
- Production security concerns: authentication, authorization, audit logging and abuse protection
- Owner approval before public release

## Reproduce on Windows

```powershell
java -version
mvn -version
./scripts/domain-smoke.ps1
mvn -B -ntp clean verify
```

Optional PostgreSQL setup is documented in the root README.
