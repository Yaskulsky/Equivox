# Sync Bbublick / Exchange Extended textures into ProjectEE
# Usage: .\scripts\sync-bbublick-textures.ps1
# Optional: .\scripts\sync-bbublick-textures.ps1 -Zip "C:\Users\harna\Desktop\something.zip"

param(
    [string]$Zip = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$destTex = Join-Path $root "src\main\resources\assets\projecte\textures"
$extract = Join-Path $root "build\_bbublick-extract"

function Find-Zip {
    if ($Zip -and (Test-Path $Zip)) { return (Resolve-Path $Zip).Path }
    $cands = @()
    foreach ($dir in @("$env:USERPROFILE\Desktop", "$env:USERPROFILE\Downloads")) {
        if (-not (Test-Path $dir)) { continue }
        $cands += Get-ChildItem $dir -File -Filter "*.zip" -ErrorAction SilentlyContinue |
            Where-Object {
                $_.Name -match '(?i)exchange|retexture|projecte|bbublick|extended'
            }
        # Also newest zip if named oddly
        $cands += Get-ChildItem $dir -File -Filter "*.zip" -ErrorAction SilentlyContinue |
            Where-Object { $_.LastWriteTime -gt (Get-Date).AddDays(-2) }
    }
    $cands = $cands | Sort-Object LastWriteTime -Descending | Select-Object -Unique -Property FullName, Name, Length, LastWriteTime
    if (-not $cands) { throw "No ZIP found on Desktop/Downloads. Pass -Zip path." }
    Write-Host "Candidates:" -ForegroundColor Cyan
    $cands | ForEach-Object { Write-Host ("  {0}  ({1:N0} bytes, {2})" -f $_.FullName, $_.Length, $_.LastWriteTime) }
    # Prefer Exchange_Extended / Retexture names
    $pref = $cands | Where-Object { $_.Name -match '(?i)exchange|retexture|bbublick|projecte' } | Select-Object -First 1
    if (-not $pref) { $pref = $cands | Select-Object -First 1 }
    return $pref.FullName
}

$zipPath = Find-Zip
Write-Host "Using ZIP: $zipPath" -ForegroundColor Green

if (Test-Path $extract) { Remove-Item $extract -Recurse -Force }
New-Item -ItemType Directory -Path $extract -Force | Out-Null

Expand-Archive -LiteralPath $zipPath -DestinationPath $extract -Force

# Find assets/projecte/textures under extract
$srcTex = Get-ChildItem $extract -Recurse -Directory -Filter "textures" -ErrorAction SilentlyContinue |
    Where-Object {
        $_.FullName -match '[\\/]assets[\\/]projecte[\\/]textures$' -or
        $_.Parent.Name -eq "projecte"
    } |
    Select-Object -First 1

if (-not $srcTex) {
    # Fallback: any folder named textures that contains item/ or block/
    $srcTex = Get-ChildItem $extract -Recurse -Directory -Filter "textures" |
        Where-Object {
            (Test-Path (Join-Path $_.FullName "item")) -or (Test-Path (Join-Path $_.FullName "block"))
        } |
        Select-Object -First 1
}

if (-not $srcTex) {
    Write-Host "Extract layout (top):" -ForegroundColor Yellow
    Get-ChildItem $extract -Recurse -Depth 3 | Select-Object -First 40 FullName
    throw "Could not find assets/projecte/textures inside ZIP"
}

Write-Host "Source textures: $($srcTex.FullName)" -ForegroundColor Cyan
if (-not (Test-Path $destTex)) { New-Item -ItemType Directory -Path $destTex -Force | Out-Null }

$copied = 0
Get-ChildItem $srcTex.FullName -Recurse -File -Filter "*.png" | ForEach-Object {
    $rel = $_.FullName.Substring($srcTex.FullName.Length).TrimStart("\", "/")
    $out = Join-Path $destTex $rel
    $outDir = Split-Path $out -Parent
    if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir -Force | Out-Null }
    Copy-Item $_.FullName $out -Force
    $copied++
}

Write-Host "Copied/overwrote $copied PNG files into:" -ForegroundColor Green
Write-Host "  $destTex"
@(
    "item\philosophers_stone.png",
    "item\stars\klein_star_1.png",
    "block\alchemical_chest.png"
) | ForEach-Object {
    $p = Join-Path $destTex $_
    if (Test-Path $p) { Write-Host "  OK $_" } else { Write-Host "  MISSING $_" -ForegroundColor Yellow }
}

Write-Host "Done. Rebuild/deploy when ready (no version bump)." -ForegroundColor Cyan
