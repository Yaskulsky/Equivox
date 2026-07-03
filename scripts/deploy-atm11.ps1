# Build ProjectE, verify JAR, deploy to ATM11 (Minecraft must be closed)
param(
    [switch]$SkipBuild,
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$jar = Join-Path $root "build\libs\projecte-1.2.0.jar"
$dest = "C:\CurseForge\Instances\All the Mods 11 - ATM11\mods\projecte-1.2.0.jar"
$mcProcess = "javaw", "minecraft", "Minecraft"

Set-Location $root

# Warn if game running
$running = Get-Process -ErrorAction SilentlyContinue | Where-Object {
    $mcProcess -contains $_.ProcessName
}
if ($running -and -not $Force) {
    Write-Host "ERROR: Minecraft/Java appears running. Close game first or use -Force." -ForegroundColor Red
    $running | ForEach-Object { Write-Host "  PID $($_.Id): $($_.ProcessName)" }
    Write-Host "Hot deploy corrupts JAR (ZipException, missing icons, GUI crash)."
    exit 1
}

if (-not $SkipBuild) {
    Write-Host "=== Building ===" -ForegroundColor Cyan
    & .\gradlew.bat build -x test
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if (-not (Test-Path $jar)) {
    Write-Host "JAR not found: $jar" -ForegroundColor Red
    exit 1
}

Write-Host "=== Verifying JAR ===" -ForegroundColor Cyan
& (Join-Path $root "scripts\port-check.ps1") -VerifyJar -Quiet
if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR verify failed — not deploying." -ForegroundColor Red
    exit 1
}

$destDir = Split-Path $dest -Parent
if (-not (Test-Path $destDir)) {
    Write-Host "ATM mods folder missing: $destDir" -ForegroundColor Red
    exit 1
}

Write-Host "=== Deploying ===" -ForegroundColor Cyan
Copy-Item -Force $jar $dest
Write-Host "Copied to $dest" -ForegroundColor Green
Write-Host "Start ATM11 and check log for: Registered * EMC values" -ForegroundColor Cyan
