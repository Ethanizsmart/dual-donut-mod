param([string]$Version = '2.0.2', [string]$SellVersion = '1.3.3-webhook.3', [ValidateSet('Use', 'Sell')][string[]]$Sides = @('Use', 'Sell'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot = Split-Path $PSScriptRoot -Parent
$repoRoot = Split-Path $projectRoot -Parent
$releaseDir = Join-Path $projectRoot "releases/$Version"
function Hash-Bytes([byte[]]$Bytes) {
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return ([BitConverter]::ToString($sha.ComputeHash($Bytes))).Replace('-', '').ToLowerInvariant() }
    finally { $sha.Dispose() }
}
function Entry-Bytes($Entry) {
    $stream = $Entry.Open(); $memory = [IO.MemoryStream]::new()
    try { $stream.CopyTo($memory); return ,$memory.ToArray() }
    finally { $stream.Dispose(); $memory.Dispose() }
}
$reports = @(); $sums = @()
foreach ($side in $Sides) {
    $lower = $side.ToLowerInvariant()
    $baseId = if ($side -eq 'Use') { 'autousemod' } else { 'autosell_donut' }
    $baseVersion = if ($side -eq 'Use') { '1.0.1' } else { $SellVersion }
    $baseName = if ($side -eq 'Use') { 'AutoUseMod-1.0.1.jar' } else { "autosell_donut-$SellVersion.jar" }
    $controlName = "DualDonutControl-$side-$Version.jar"
    $zipName = "DualDonutControl-$side-$Version-install.zip"
    $zipPath = Join-Path $releaseDir $zipName
    $zip = [IO.Compression.ZipFile]::OpenRead($zipPath)
    try {
        $entries = @($zip.Entries | Where-Object { $_.Name })
        if ($entries.Count -ne 2) { throw "$zipName must contain exactly two runtime JARs" }
        $jarReports = @()
        foreach ($jarName in @($controlName, $baseName)) {
            $entry = $zip.GetEntry("mods/$jarName")
            if (!$entry) { throw "Missing mods/$jarName in $zipName" }
            $bytes = Entry-Bytes $entry
            $hash = Hash-Bytes $bytes
            $original = if ($jarName -eq $controlName) { Join-Path $projectRoot "build/$lower/libs/$jarName" } else { Join-Path $repoRoot "SwordWebhookIntegration/$jarName" }
            if ($hash -ne (Get-FileHash -LiteralPath $original -Algorithm SHA256).Hash.ToLowerInvariant()) { throw "JAR differs from remapped input: $jarName" }
            $memory = [IO.MemoryStream]::new($bytes, $false)
            $jar = [IO.Compression.ZipArchive]::new($memory, [IO.Compression.ZipArchiveMode]::Read)
            try {
                if (@($jar.Entries | Where-Object { $_.FullName -match '(^net/minecraft/|/smoke/|SellSessionChecks|BountyChecks|\.java$)' }).Count) { throw "Test/game/source content in $jarName" }
                $hasBounty = $null -ne $jar.GetEntry('dev/dualdonut/bounty/BountyRuntime.class')
                if ($side -eq 'Use' -and $hasBounty) { throw 'Farm must not contain Auto Bounty' }
                if ($side -eq 'Sell' -and [version]$Version -ge [version]'2.0.3' -and $jarName -eq $controlName -and !$hasBounty) { throw 'Sell Auto Bounty runtime is missing' }
                $metadataBytes = Entry-Bytes ($jar.GetEntry('fabric.mod.json'))
                $metadata = [Text.Encoding]::UTF8.GetString($metadataBytes) | ConvertFrom-Json
                $expectedId = if ($jarName -eq $controlName) { "dualdonutcontrol_$lower" } else { $baseId }
                $expectedVersion = if ($jarName -eq $controlName) { $Version } else { $baseVersion }
                if ($metadata.id -ne $expectedId -or $metadata.version -ne $expectedVersion) { throw "Wrong metadata in $jarName" }
                if ($jarName -eq $controlName -and $metadata.depends.$baseId -ne $baseVersion) { throw "Wrong base dependency in $jarName" }
                $jarReports += [ordered]@{ name = $jarName; id = $metadata.id; version = $metadata.version; sha256 = $hash }
            } finally { $jar.Dispose(); $memory.Dispose() }
        }
        $zipHash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash.ToLowerInvariant()
        $sums += "$zipHash  $zipName"
        $reports += [ordered]@{ package = $zipName; sha256 = $zipHash; jars = $jarReports }
    } finally { $zip.Dispose() }
}
$reports | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $releaseDir 'PACKAGE_VERIFICATION.json')
$sums | Set-Content -Encoding ASCII (Join-Path $releaseDir 'SHA256SUMS.txt')
Write-Output "INSTALL_PACKAGE_VERIFY_PASS: $($Sides.Count) package(s), exactly two matching runtime JARs each; metadata/dependencies/SHA-256 verified; no test/game/source classes; Bounty profile isolation verified."
$sums
