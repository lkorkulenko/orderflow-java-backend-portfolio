$ErrorActionPreference = 'Stop'
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    mvn test
    if ($LASTEXITCODE -ne 0) { throw 'Tests failed' }
    mvn spring-boot:run
} finally { Pop-Location }
