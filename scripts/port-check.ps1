# ProjectE 26.1 port - static scan + optional compile + JAR verify
param(
    [switch]$Build,
    [switch]$VerifyJar,
    [switch]$Quiet
)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root

$jar = Join-Path $root "build\libs\projecte-1.2.0.jar"
$src = Join-Path $root "src\main\java"

function Write-Finding($severity, $message) {
    if (-not $Quiet) { Write-Host "[$severity] $message" }
}

$findings = @()

function Add-Finding($severity, $pattern, $file, $line) {
    $script:findings += [pscustomobject]@{ Severity = $severity; Pattern = $pattern; File = $file; Line = $line }
}

Write-Host "=== ProjectE port-check ===" -ForegroundColor Cyan

# --- Grep scans (rg if available, else Select-String) ---
$patterns = @(
    @{ Name = "renderBg/renderLabels (old GUI)"; Regex = "renderBg|renderLabels"; Severity = "HIGH" },
    @{ Name = "getGuiLeft/getGuiTop (deprecated)"; Regex = "getGuiLeft|getGuiTop|getSlotUnderMouse|getXSize|getYSize"; Severity = "MED" },
    @{ Name = "RegistryAccess.EMPTY"; Regex = "RegistryAccess\.EMPTY"; Severity = "HIGH" },
    @{ Name = "craftRemainder at register"; Regex = "craftRemainder\([^)]*\.get\(\)"; Severity = "HIGH" },
    @{ Name = "Invisible text color 0x404040"; Regex = "0x404040[^0-9a-fA-F]|,\s*0x404040\s*,"; Severity = "HIGH" },
    @{ Name = "TODO 26.1"; Regex = "TODO 26\.1|TODO 1\.21\.6"; Severity = "LOW" },
    @{ Name = "IGuiOverlay (old HUD)"; Regex = "IGuiOverlay|RegisterGuiOverlay"; Severity = "MED" }
)

$rg = Get-Command rg -ErrorAction SilentlyContinue
foreach ($p in $patterns) {
    if ($rg) {
        $hits = & rg -n $p.Regex $src 2>$null
        foreach ($h in $hits) {
            if ($h -match "^(.+?):(\d+):(.*)$") {
                $file = $Matches[1]
                $lineNum = $Matches[2]
                $lineText = $Matches[3]
                if ($p.Name -like "*0x404040*" -and ($lineText -match "0xFF404040|@code 0x404040")) { continue }
                if ($p.Name -eq "RegistryAccess.EMPTY" -and $lineText -match "level\.registryAccess\(\)\s*:\s*RegistryAccess\.EMPTY") { continue }
                Add-Finding $p.Severity $p.Name $file $lineNum
            }
        }
    } else {
        Get-ChildItem -Path $src -Recurse -Filter "*.java" | ForEach-Object {
            $i = 0
            Get-Content $_.FullName | ForEach-Object {
                $i++
                if ($_ -match $p.Regex) {
                    if ($p.Name -like "*0x404040*" -and ($_ -match "0xFF404040|@code 0x404040")) { return }
                    if ($p.Name -eq "RegistryAccess.EMPTY" -and $_ -match "level\.registryAccess\(\)\s*:\s*RegistryAccess\.EMPTY") { return }
                    Add-Finding $p.Severity $p.Name $_.FullName $i
                }
            }
        }
    }
}

# EMC data files
$dataChecks = @(
    "src\datagen\generated\data\projecte\pe_custom_conversions\defaults.json",
    "src\datagen\generated\data\projecte\pe_world_transmutations\defaults.json"
)
foreach ($rel in $dataChecks) {
    $path = Join-Path $root $rel
    if (-not (Test-Path $path)) {
        Add-Finding "HIGH" "Missing EMC data" $rel "-"
    }
}

# Report findings
$grouped = $findings | Group-Object Severity
foreach ($g in @("HIGH", "MED", "LOW")) {
    $items = $findings | Where-Object { $_.Severity -eq $g }
    if ($items.Count -gt 0) {
        Write-Host "`n--- $g ($($items.Count)) ---" -ForegroundColor $(if ($g -eq "HIGH") { "Red" } elseif ($g -eq "MED") { "Yellow" } else { "Gray" })
        $items | Select-Object -First 20 | ForEach-Object { Write-Host "  $($_.File):$($_.Line)  [$($_.Pattern)]" }
        if ($items.Count -gt 20) { Write-Host "  ... and $($items.Count - 20) more" }
    }
}

if ($findings | Where-Object { $_.Severity -eq "HIGH" }) {
    Write-Host "`nHIGH issues found - fix before deploy." -ForegroundColor Red
} else {
    Write-Host "`nNo HIGH scan issues." -ForegroundColor Green
}

# --- Optional build ---
if ($Build) {
    Write-Host "`n=== gradlew build -x test ===" -ForegroundColor Cyan
    & .\gradlew.bat build -x test
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

# --- JAR verify ---
if ($VerifyJar) {
    if (-not (Test-Path $jar)) {
        Write-Host "JAR missing: $jar (run with -Build)" -ForegroundColor Red
        exit 1
    }
    Write-Host "`n=== JAR verify ===" -ForegroundColor Cyan
    $size = (Get-Item $jar).Length
    Write-Host "Size: $([math]::Round($size / 1MB, 2)) MB"

    $tar = Get-Command tar -ErrorAction SilentlyContinue
    if ($tar) {
        $samples = @(
            "assets/projecte/items/dm_axe.json",
            "assets/projecte/items/philosophers_stone.json",
            "data/projecte/pe_custom_conversions/defaults.json"
        )
        $ok = $true
        foreach ($entry in $samples) {
            $out = & tar -xOf $jar $entry 2>&1
            if ($LASTEXITCODE -ne 0) {
                Write-Host "  MISSING or corrupt: $entry" -ForegroundColor Red
                $ok = $false
            } else {
                Write-Host "  OK: $entry" -ForegroundColor Green
            }
        }
        if (-not $ok) { exit 1 }
    } else {
        Write-Host "tar not found - skip content verify" -ForegroundColor Yellow
    }
}

$highCount = ($findings | Where-Object { $_.Severity -eq "HIGH" }).Count
if ($highCount -gt 0) { exit 1 } else { exit 0 }
