$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$out = Join-Path $env:TEMP 'orderflow-domain-smoke'
New-Item -ItemType Directory -Force -Path $out | Out-Null
$domain = Join-Path $root 'src/main/java/dev/portfolio/orderflow/domain'
javac -d $out (Join-Path $domain 'OrderStatus.java') (Join-Path $domain 'InvalidOrderTransitionException.java') (Join-Path $domain 'OrderRules.java') (Join-Path $root 'tools/DomainSmokeTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
java -cp $out DomainSmokeTest
if ($LASTEXITCODE -ne 0) { throw 'Domain smoke test failed' }
