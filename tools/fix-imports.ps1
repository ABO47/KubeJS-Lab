param(
    [Parameter(Mandatory = $true)]
    [string]$Path,
    [switch]$DryRun,
    [switch]$RemoveUnused
)

$ErrorActionPreference = 'Stop'

function Get-ImportFqn([string]$importLine) {
    return ($importLine -replace '^import\s+', '' -replace ';$', '').Trim()
}

function Get-GroupKey([string]$fqn) {
    if ($fqn -like 'java.*' -or $fqn -like 'javax.*') { return 0 }
    if ($fqn -like 'org.*') { return 1 }
    if ($fqn -like 'it.unimi.*') { return 2 }
    if ($fqn -like 'com.mojang.*') { return 3 }
    if ($fqn -like 'net.minecraft.*') { return 4 }
    if ($fqn -like 'com.lowdragmc.*') { return 5 }
    if ($fqn -like 'com.abo47.*') { return 6 }
    return 7
}

function Get-SimpleName([string]$fqn) {
    if ($fqn -like '*.') { return '' }
    $parts = $fqn.Split('.')
    return $parts[$parts.Length - 1]
}

$files = Get-ChildItem -Path $Path -Recurse -Filter *.java | Where-Object {
    $_.FullName -notmatch '\\build\\'
}

foreach ($file in $files) {
    $source = [System.IO.File]::ReadAllText($file.FullName)
    $crlf = $source.Contains("`r`n")
    $hasTrailingNewline = $source.EndsWith("`n")
    $lines = @($source -split "`r?`n")

    $importIndexes = [System.Collections.Generic.List[int]]::new()
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^import\s') {
            $importIndexes.Add($i)
        }
    }
    if ($importIndexes.Count -eq 0) {
        continue
    }
    $first = $importIndexes[0]
    $last = $importIndexes[$importIndexes.Count - 1]

    $blockOk = $true
    foreach ($line in $lines[$first..$last]) {
        $t = $line.Trim()
        if ($t -eq '' -or $t.StartsWith('//') -or $t.StartsWith('import ')) {
            continue
        }
        $blockOk = $false
        break
    }
    if (-not $blockOk) {
        Write-Host "SKIP $($file.FullName): non-import content inside import window"
        continue
    }

    $unused = @()
    if ($RemoveUnused) {
        $nonImportText = ''
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($i -lt $first -or $i -gt $last) {
                $nonImportText += $lines[$i] + "`n"
            }
        }
        for ($i = $first; $i -le $last; $i++) {
            $line = $lines[$i]
            $t = $line.Trim()
            if ($t.StartsWith('import static ')) {
                continue
            }
            if (-not ($t -match '^import\s+[^;]+\*;')) {
                $fqn = Get-ImportFqn $t
                $simple = Get-SimpleName $fqn
                if ($simple -ne '' -and $simple -ne '*') {
                    if ($nonImportText.IndexOf($simple, [System.StringComparison]::Ordinal) -lt 0) {
                        $unused += $line
                    }
                }
            }
        }
        if ($unused.Count -gt 0) {
            Write-Host "UNUSED $($file.FullName):"
            foreach ($u in $unused) {
                Write-Host "  - $($u.Trim())"
            }
        }
    }

    $entries = [System.Collections.Generic.List[object]]::new()
    for ($i = $first; $i -le $last; $i++) {
        $t = $lines[$i].Trim()
        if (-not ($t.StartsWith('import '))) {
            continue
        }
        if ($RemoveUnused -and $unused.Contains($lines[$i])) {
            continue
        }
        if ($t.StartsWith('import static ')) {
            $group = 1000
        }
        else {
            $group = Get-GroupKey (Get-ImportFqn $t)
        }
        $entries.Add([pscustomobject]@{
                Group = $group
                Fqn   = Get-ImportFqn $t
                Text  = $t
            })
    }

    $sorted = @($entries | Sort-Object Group, Fqn -CaseSensitive)

    $newLines = [System.Collections.Generic.List[string]]::new()
    for ($i = 0; $i -lt $first; $i++) {
        $newLines.Add($lines[$i])
    }
    $prevGroup = -1
    $firstGroup = $true
    foreach ($e in $sorted) {
        if ($e.Group -ne $prevGroup) {
            if (-not $firstGroup) {
                $newLines.Add('')
            }
            $firstGroup = $false
            $prevGroup = $e.Group
        }
        $newLines.Add($e.Text)
    }
    $newLines.Add('')
    $newLines.Add('')
    $i = $last + 1
    while ($i -lt $lines.Count -and $lines[$i].Trim() -eq '') {
        $i++
    }
    for (; $i -lt $lines.Count; $i++) {
        $newLines.Add($lines[$i])
    }

    $eol = if ($crlf) { "`r`n" } else { "`n" }
    $out = [string]::Join($eol, $newLines.ToArray())

    $outN = $out -replace "`r`n", "`n"
    $srcN = $source -replace "`r`n", "`n"
    if ($DryRun) {
        if ($outN -ne $srcN) {
            Write-Host "WOULD FIX $($file.FullName)"
        }
    }
    else {
        if ($outN -ne $srcN) {
            [System.IO.File]::WriteAllText($file.FullName, $out, [System.Text.UTF8Encoding]::new($false))
            Write-Host "FIXED $($file.FullName)"
        }
    }
}