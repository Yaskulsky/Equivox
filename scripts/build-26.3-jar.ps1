# Native NeoForge 26.3 build only (NOT a renamed 26.1.2 JAR).
# Requires gradle.properties: minecraft_version=26.3, neo_version=26.3.x on branch 26.3-port.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root

$props = Get-Content (Join-Path $root "gradle.properties") -Raw
if ($props -notmatch '(?m)^minecraft_version=(.+)$') { throw 'minecraft_version missing' }
$mc = $Matches[1].Trim()
if ($mc -ne '26.3') {
    Write-Host "ERROR: build-26.3-jar.ps1 requires minecraft_version=26.3 in gradle.properties (got '$mc')." -ForegroundColor Red
    Write-Host "Use 26.3-port branch after the transfer-API port; do not rename a 26.1.2 JAR." -ForegroundColor Red
    exit 1
}

Write-Host "=== Build Equivox-26.3 (native NeoForge $mc) ===" -ForegroundColor Cyan
& .\gradlew.bat shadowJar -x test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($props -notmatch '(?m)^equivox_version=(.+)$') { throw 'equivox_version missing' }
$modVer = $Matches[1].Trim()
$jar = Join-Path $root "build\libs\Equivox-26.3-$modVer.jar"
if (-not (Test-Path -LiteralPath $jar)) { throw "JAR not found: $jar" }
Write-Host "Built: $jar" -ForegroundColor Green
