# Build Equivox, verify JAR, deploy to ATM11 (Minecraft must be closed)
# JAR name: Equivox-<mc>-<mod>.jar  e.g. Equivox-26.1.2-1.3.0.jar
param(
    [switch]$SkipBuild,
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$destDir = "C:\CurseForge\Instances\All the Mods 11 - ATM11\mods"
# Block only the actual game JVM — CurseForge launcher is also named minecraft.exe
$mcProcess = "javaw"

Set-Location $root

function Get-Prop([string]$key) {
    $props = Get-Content (Join-Path $root "gradle.properties") -Raw
    if ($props -match "(?m)^$key=(.+)$") { return $Matches[1].Trim() }
    throw "$key not found in gradle.properties"
}

$version = Get-Prop "equivox_version"
$mc = Get-Prop "minecraft_version"
$jarName = "Equivox-$mc-$version.jar"
$baseJar = Join-Path $root "build\libs\$jarName"
$dest = Join-Path $destDir $jarName

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
    Write-Host "=== Building Equivox $version (MC $mc) ===" -ForegroundColor Cyan
    & .\gradlew.bat build -x test
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if (-not (Test-Path -LiteralPath $baseJar)) {
    Write-Host "JAR not found: $baseJar" -ForegroundColor Red
    exit 1
}

Write-Host "=== Verifying JAR ===" -ForegroundColor Cyan
& (Join-Path $root "scripts\port-check.ps1") -VerifyJar -Quiet
if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR verify failed - not deploying." -ForegroundColor Red
    exit 1
}

if (-not (Test-Path -LiteralPath $destDir)) {
    Write-Host "ATM mods folder missing: $destDir" -ForegroundColor Red
    exit 1
}

Write-Host "=== Deploying $jarName ===" -ForegroundColor Cyan
# Remove older ProjectE / Equivalence / Equivox / legacy-named jars to avoid duplicate mod loads
Get-ChildItem -LiteralPath $destDir -Filter "projecte*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -LiteralPath $destDir -Filter "projectee*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -LiteralPath $destDir -Filter "equivalence*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -LiteralPath $destDir -Filter "equivox*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -LiteralPath $destDir -Filter "Equivox-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -LiteralPath $destDir -Filter "nedzoe*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Copy-Item -LiteralPath $baseJar -Destination $dest -Force
Write-Host "Copied to $dest" -ForegroundColor Green
Write-Host "Equivox v$version ready - start ATM11" -ForegroundColor Cyan
