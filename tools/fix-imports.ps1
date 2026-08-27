# Organizes imports in Java sources to KubeJS Lab conventions.
#
# Usage:
#   powershell -File tools\fix-imports.ps1              # only git-modified/new .java files
#   powershell -File tools\fix-imports.ps1 path\to\File.java ...
#   powershell -File tools\fix-imports.ps1 -All         # every .java under common/, forge/, fabric/
#
# What it does:
#   - removes imports whose simple name is never used in the file body
#   - regroups imports into the project's block order, alphabetical inside each block:
#       1) java.* / javax.*
#       2) net.minecraft.*
#       3) com.lowdragmc.*
#       4) com.abo47.kubejslab.*
#       5) everything else (gson, mojang, jei, architectury, ...)
#       6) static imports last
#   - leaves exactly two blank lines between the import block and the code
#   - preserves each file's dominant line ending, never touches anything outside the import block

param(
    [switch]$All,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Paths
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot

function Get-TargetFiles {
    if ($All) {
        return Get-ChildItem (Join-Path $repoRoot 'common'), (Join-Path $repoRoot 'forge'), (Join-Path $repoRoot 'fabric') `
            -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
    }
    if ($Paths -and $Paths.Count -gt 0) {
        return $Paths | ForEach-Object { Join-Path $repoRoot $_ }
    }
    $dirty = @(((git -C $repoRoot status --porcelain) -join "`n") -split "`r?`n") |
        Where-Object { $_.Trim().Length -gt 0 }
    return $dirty | ForEach-Object {
        if ($_.Length -le 3 -or $_ -match '->') {
            return
        }
        $rel = $_.Substring(3).Trim()
        if ($rel.EndsWith('.java')) {
            Join-Path $repoRoot ($rel -replace '/', '\')
        }
    }
}

function Get-Group ([string]$import) {
    if ($import -match '^import static ') { return 6 }
    if ($import -match '^import (java|javax)\.') { return 1 }
    if ($import -match '^import net\.minecraft\.') { return 2 }
    if ($import -match '^import com\.lowdragmc\.') { return 3 }
    if ($import -match '^import com\.abo47\.kubejslab\.') { return 4 }
    return 5
}

function Organize-File ([string]$file) {
    if (-not (Test-Path -LiteralPath $file)) {
        Write-Warning "missing: $file"
        return
    }
    $raw = [System.IO.File]::ReadAllText($file)
    $nl = if ($raw.Contains("`r`n")) { "`r`n" } else { "`n" }

    $matchesList = [System.Text.RegularExpressions.Regex]::Matches(
        $raw, '(?m)^import (?:static )?[A-Za-z_][\w\.]*\.(?:\*|\w+);\s*$')
    if ($matchesList.Count -eq 0) {
        return
    }

    $bodyBuilder = New-Object System.Text.StringBuilder($raw)
    for ($i = $matchesList.Count - 1; $i -ge 0; $i--) {
        [void]$bodyBuilder.Remove($matchesList[$i].Index, $matchesList[$i].Length)
    }
    $body = $bodyBuilder.ToString()

    $kept = @()
    $removed = @()
    foreach ($m in $matchesList) {
        $stmt = $m.Value.Trim()
        $simple = [System.Text.RegularExpressions.Regex]::Match($stmt, '\.(\w+);').Groups[1].Value
        if ($simple -eq '*' -or $body -match "\b$simple\b") {
            $kept += $stmt
        } else {
            $removed += $stmt
        }
    }

    $groups = @{}
    foreach ($stmt in $kept) {
        $group = Get-Group $stmt
        if (-not $groups.ContainsKey($group)) {
            $groups[$group] = [System.Collections.Generic.List[string]]::new()
        }
        $groups[$group].Add($stmt)
    }

    $blocks = @()
    foreach ($key in ($groups.Keys | Sort-Object)) {
        $blocks += (($groups[$key] | Sort-Object { $_.ToLower() }) -join $nl)
    }
    $importBlock = ($blocks -join ($nl + $nl))

    $first = $matchesList[0]
    $last = $matchesList[$matchesList.Count - 1]
    $prefix = $raw.Substring(0, $first.Index)
    $suffix = $raw.Substring($last.Index + $last.Length)

    $updated = $prefix + $importBlock + $nl + $nl + $nl + $suffix.TrimStart("`r", "`n")

    if ($updated -eq $raw) {
        return
    }

    [System.IO.File]::WriteAllText($file, $updated)
    $rel = $file.Substring($repoRoot.Length + 1)
    foreach ($stmt in $removed) {
        Write-Output "$rel removed: $stmt"
    }
    if ($removed.Count -eq 0) {
        Write-Output "$rel reordered"
    }
}

$targets = Get-TargetFiles
if (-not $targets -or @($targets).Count -eq 0) {
    Write-Output 'no java files to process'
    exit 0
}

$changed = 0
foreach ($target in @($targets)) {
    $before = [System.IO.File]::ReadAllText($target)
    Organize-File $target
    $after = [System.IO.File]::ReadAllText($target)
    if ($before -ne $after) {
        $changed++
    }
}
Write-Output "done: $(@($targets).Count) scanned, $changed updated"
