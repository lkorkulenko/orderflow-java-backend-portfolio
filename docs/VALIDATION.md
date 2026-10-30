# Verification status and reproducibility

## Completed in the creation environment

- Java 21 compiler successfully compiled the dependency-free domain classes and standalone smoke test.
- The standalone smoke test checked decimal multiplication, permitted transitions, and invalid transition rejection.

## Not yet verified in the creation environment

- Full Spring/Maven build and HTTP integration suite. Maven was not installed, and repository access was unavailable.
- Hibernate SQL statement counts. Expected query counts in README are design expectations, not measured results.
- PostgreSQL docker environment. Use it as an optional second-stage check after H2 tests.

## Validation on your Windows workstation

```powershell
java -version
mvn -version
./scripts/domain-smoke.ps1
mvn -B -ntp clean verify
```

Inspect the test console line `SQL statements for 20-order page: legacy=..., optimized=...`.

For this portfolio project's first iteration, acceptance criteria are:

1. All Maven tests pass on Java 21 and Boot 3.5.16.
2. The optimized and legacy JSON responses match for the same fixture.
3. Legacy SQL prepared-statement count >=21; optimized <=3 for 20 orders.
4. Invalid order transitions and duplicate references produce HTTP 409.
5. A sample order can be created/listed/updated through PowerShell.

If any acceptance criterion fails, fix the implementation before making the repository public or using the results on Upwork. The CI workflow is prepared to run Maven verification after your first push.
