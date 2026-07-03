# Parse ATM11 latest.log for ProjectE / port-related issues
param(
    [string]$LogPath = "C:\CurseForge\Instances\All the Mods 11 - ATM11\logs\latest.log",
    [int]$TailLines = 0
)

if (-not (Test-Path $LogPath)) {
    Write-Host "Log not found: $LogPath" -ForegroundColor Red
    exit 1
}

$patterns = @(
    @{ Name = "EMC registration"; Regex = "\[projecte\].*Registered \d+ EMC|Registered \d+ EMC values"; Color = "Green" },
    @{ Name = "EMC zero"; Regex = "\[projecte\].*Registered 0 EMC|Registered 0 EMC"; Color = "Red" },
    @{ Name = "ProjectE errors"; Regex = "\[projecte\].*ERROR|projecte.*Exception"; Color = "Red" },
    @{ Name = "Zip/JAR corrupt"; Regex = "ZipFile|invalid LOC header|bad signature"; Color = "Red" },
    @{ Name = "Missing item model"; Regex = "Failed to open item model projecte"; Color = "Red" },
    @{ Name = "ProjectE registry"; Regex = "projecte.*Missing registry|SlotPredicates.*Missing registry"; Color = "Red" },
    @{ Name = "Components not bound"; Regex = "Components not bound"; Color = "Red" },
    @{ Name = "Rendering screen crash"; Regex = "Rendering screen.*projecte|AbstractCondenserScreen"; Color = "Red" },
    @{ Name = "ProjectE load"; Regex = "\[projecte\]"; Color = "Cyan" }
)

Write-Host "=== Log triage: $LogPath ===" -ForegroundColor Cyan

$content = if ($TailLines -gt 0) {
    Get-Content $LogPath -Tail $TailLines
} else {
    Get-Content $LogPath
}

$found = $false
foreach ($p in $patterns) {
    $hits = $content | Select-String -Pattern $p.Regex -AllMatches
    if ($hits) {
        $found = $true
        Write-Host "`n[$($p.Name)]" -ForegroundColor $p.Color
        $hits | Select-Object -Last 5 | ForEach-Object { Write-Host "  $($_.Line.Trim())" }
    }
}

if (-not $found) {
    Write-Host "No known port patterns matched. Grep projecte manually:" -ForegroundColor Yellow
    $content | Select-String -Pattern "projecte" -CaseSensitive:$false | Select-Object -Last 15 | ForEach-Object {
        Write-Host "  $($_.Line.Trim())"
    }
}

# Quick EMC summary
$emc = $content | Select-String -Pattern "Registered (\d+) EMC" | Select-Object -Last 1
if ($emc) {
    Write-Host "`nEMC: $($emc.Line.Trim())" -ForegroundColor $(if ($emc.Matches.Groups[1].Value -eq "0") { "Red" } else { "Green" })
}
