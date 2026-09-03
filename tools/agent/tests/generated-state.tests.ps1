#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$HelperPath = (Join-Path $PSScriptRoot "../repository-generated-state.ps1"),
    [string]$LogDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$runToken = "generated-state-tests-" + [Guid]::NewGuid().ToString("N")
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$fixtureRoot = Join-Path $tempBase $runToken
$snapshotBase = Join-Path $tempBase "geocedg-generated-state"
$allowedNames = @("build", ".gradle", ".kotlin")
$cases = [Collections.Generic.List[object]]::new()
$ownedSnapshots = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
$cleanupErrors = [Collections.Generic.List[string]]::new()
$script:assertions = 0
$script:caseNumber = 0
$started = [DateTime]::UtcNow
$resolvedHelper = [IO.Path]::GetFullPath($HelperPath)
$helperHash = $null

function Assert-Fixture {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
    $script:assertions++
}
function Assert-Throws {
    param([scriptblock]$Action, [string]$Pattern)
    $observed = $null
    try { & $Action | Out-Null }
    catch { $observed = $_.Exception }
    Assert-Fixture ($null -ne $observed) "Expected failure matching $Pattern, but operation succeeded."
    Assert-Fixture ($observed.Message -match $Pattern) "Unexpected failure '$($observed.Message)'; expected $Pattern."
    return $observed
}
function Write-FixtureFile {
    param([string]$Path, [string]$Text)
    [void](Microsoft.PowerShell.Management\New-Item -ItemType Directory -Path (Split-Path -Parent $Path) -Force)
    [IO.File]::WriteAllText($Path, $Text, [Text.UTF8Encoding]::new($false))
}
function Get-TreeEvidence {
    param([string]$Root)
    if (-not (Test-Path -LiteralPath $Root)) { return "<absent>" }
    $fullRoot = [IO.Path]::GetFullPath($Root)
    $pending = [Collections.Generic.Stack[string]]::new()
    $pending.Push($fullRoot)
    $records = [Collections.Generic.List[string]]::new()
    while ($pending.Count -gt 0) {
        $directory = $pending.Pop()
        foreach ($item in @(Microsoft.PowerShell.Management\Get-ChildItem -LiteralPath $directory -Force)) {
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Fixture evidence refuses linked paths: $($item.FullName)" }
            $relative = [IO.Path]::GetRelativePath($fullRoot, $item.FullName).Replace('\', '/')
            if ($item.PSIsContainer) { $records.Add("D $relative"); $pending.Push($item.FullName) }
            else { $records.Add("F $relative " + (Get-FileHash -LiteralPath $item.FullName -Algorithm SHA256).Hash) }
        }
    }
    return (@($records | Sort-Object) -join [Environment]::NewLine)
}
function Assert-PhysicalFixturePath {
    param([string]$Path)
    $full = [IO.Path]::GetFullPath($Path).TrimEnd('\', '/')
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if (-not $full.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase)) { throw "Fixture cleanup refuses non-temp path: $full" }
    $ancestor = $full
    while ($ancestor.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -or $ancestor.Equals($tempBase, [StringComparison]::OrdinalIgnoreCase)) {
        if (Test-Path -LiteralPath $ancestor) {
            $item = Microsoft.PowerShell.Management\Get-Item -LiteralPath $ancestor -Force
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Fixture cleanup refuses real linked ancestry: $ancestor" }
        }
        $ancestor = Split-Path -Parent $ancestor
    }
}
function Remove-OwnedFixtureRoot {
    param([string]$Path, [bool]$IsSnapshot)
    $full = [IO.Path]::GetFullPath($Path).TrimEnd('\', '/')
    if (-not (Test-Path -LiteralPath $full)) { return }
    Assert-PhysicalFixturePath $full
    if ($IsSnapshot) {
        if (-not $ownedSnapshots.Contains($full) -or -not (Split-Path -Parent $full).Equals($snapshotBase, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Fixture cleanup refuses unregistered snapshot: $full"
        }
    } elseif (-not $full.Equals($fixtureRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Fixture cleanup refuses unexpected fixture root: $full"
    }
    $marker = Join-Path $full ".fixture-owner"
    if (-not (Test-Path -LiteralPath $marker -PathType Leaf) -or [IO.File]::ReadAllText($marker) -cne $runToken) {
        throw "Fixture cleanup refuses root without matching ownership marker: $full"
    }
    # Exact, registered and physically checked target; never delete temp/snapshot parent.
    Microsoft.PowerShell.Management\Remove-Item -LiteralPath $full -Recurse -Force
}
function New-FixtureCase {
    param([string]$Name)
    $script:caseNumber++
    $caseParent = Join-Path $fixtureRoot ("case-{0:D2}" -f $script:caseNumber)
    $repository = Join-Path $caseParent "repo"
    Write-FixtureFile (Join-Path $repository "src/tracked.txt") "TRACKED_SENTINEL"
    $state = [pscustomobject]@{
        Repository = $repository; TempBase = $tempBase; SnapshotBase = $snapshotBase; RunToken = $runToken
        Label = "gs-fixture-" + $runToken + "-" + $script:caseNumber
        Targets = @(); EnumerationCalls = 0; FailEnumeration = $false; DenyMutation = $false
        FailCopyPath = $null; FailRemovePath = $null; MockReparsePaths = @()
        CopyCalls = [Collections.Generic.List[object]]::new()
        RemoveCalls = [Collections.Generic.List[string]]::new()
        NewItemCalls = [Collections.Generic.List[string]]::new()
        SnapshotRoots = [Collections.Generic.List[string]]::new()
    }
    $module = New-Module -Name ("GeneratedStateFixture_" + [Guid]::NewGuid().ToString("N")) -ArgumentList @($resolvedHelper, $state) -ScriptBlock {
        param($SourcePath, $FixtureState)
        Set-StrictMode -Version Latest
        $ErrorActionPreference = "Stop"
        . $SourcePath
        $script:FixtureState = $FixtureState
        function Get-RepositoryGeneratedDirectories {
            param([string]$RepositoryRoot, [string[]]$DirectoryNames)
            $script:FixtureState.EnumerationCalls++
            if ($script:FixtureState.FailEnumeration) { throw "ENUMERATION_FAILURE_SENTINEL" }
            return @($script:FixtureState.Targets)
        }
        function Assert-OwnMutation {
            param([string]$Path)
            $full = [IO.Path]::GetFullPath($Path).TrimEnd('\', '/')
            $roots = @($script:FixtureState.Repository) + @($script:FixtureState.SnapshotRoots)
            $owned = $false
            foreach ($root in $roots) {
                $root = [IO.Path]::GetFullPath($root).TrimEnd('\', '/')
                if ($full.Equals($root, [StringComparison]::OrdinalIgnoreCase) -or $full.StartsWith($root + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { $owned = $true; break }
            }
            if (-not $owned) { throw "FIXTURE_MUTATION_GUARD: unowned path $full" }
            if ($script:FixtureState.DenyMutation) { throw "FIXTURE_MUTATION_GUARD: negative fixture attempted mutation" }
            # Inspect real attributes, not injected flags, before every physical mutation.
            $tempPrefix = $script:FixtureState.TempBase + [IO.Path]::DirectorySeparatorChar
            $ancestor = $full
            while ($ancestor.StartsWith($tempPrefix, [StringComparison]::OrdinalIgnoreCase) -or $ancestor.Equals($script:FixtureState.TempBase, [StringComparison]::OrdinalIgnoreCase)) {
                if (Test-Path -LiteralPath $ancestor) {
                    $actual = Microsoft.PowerShell.Management\Get-Item -LiteralPath $ancestor -Force
                    if ($actual.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "FIXTURE_MUTATION_GUARD: real linked ancestry $ancestor" }
                }
                $ancestor = Split-Path -Parent $ancestor
            }
        }
        function New-Item {
            [CmdletBinding()]
            param([string]$Path, [string]$ItemType, [switch]$Force)
            $full = [IO.Path]::GetFullPath($Path).TrimEnd('\', '/')
            $script:FixtureState.NewItemCalls.Add($full)
            $isSnapshot = (Split-Path -Parent $full).Equals($script:FixtureState.SnapshotBase, [StringComparison]::OrdinalIgnoreCase) -and (Split-Path -Leaf $full).StartsWith($script:FixtureState.Label + "-", [StringComparison]::Ordinal)
            if ($isSnapshot -and -not $script:FixtureState.SnapshotRoots.Contains($full)) { $script:FixtureState.SnapshotRoots.Add($full) }
            Assert-OwnMutation $full
            $item = Microsoft.PowerShell.Management\New-Item @PSBoundParameters
            if ($isSnapshot) { [IO.File]::WriteAllText((Join-Path $full ".fixture-owner"), $script:FixtureState.RunToken) }
            return $item
        }
        function Copy-Item {
            [CmdletBinding()]
            param([string]$LiteralPath, [string]$Destination, [switch]$Recurse, [switch]$Force)
            $script:FixtureState.CopyCalls.Add([pscustomobject]@{ Source = $LiteralPath; Destination = $Destination })
            Assert-OwnMutation $LiteralPath
            Assert-OwnMutation $Destination
            if ($LiteralPath -eq $script:FixtureState.FailCopyPath) { throw [IO.IOException]::new("COPY_FAILURE_SENTINEL") }
            Microsoft.PowerShell.Management\Copy-Item @PSBoundParameters
        }
        function Remove-Item {
            [CmdletBinding()]
            param([string]$LiteralPath, [switch]$Recurse, [switch]$Force)
            $script:FixtureState.RemoveCalls.Add($LiteralPath)
            Assert-OwnMutation $LiteralPath
            if ($LiteralPath -eq $script:FixtureState.FailRemovePath) { throw [IO.IOException]::new("REMOVE_FAILURE_SENTINEL") }
            Microsoft.PowerShell.Management\Remove-Item @PSBoundParameters
        }
        function Get-Item {
            [CmdletBinding()]
            param([string]$LiteralPath, [switch]$Force)
            $item = Microsoft.PowerShell.Management\Get-Item @PSBoundParameters
            $full = [IO.Path]::GetFullPath($LiteralPath).TrimEnd('\', '/')
            foreach ($mock in $script:FixtureState.MockReparsePaths) {
                if ($full.Equals([IO.Path]::GetFullPath($mock).TrimEnd('\', '/'), [StringComparison]::OrdinalIgnoreCase)) {
                    return [pscustomobject]@{ Attributes = $item.Attributes -bor [IO.FileAttributes]::ReparsePoint; FullName = $item.FullName }
                }
            }
            return $item
        }
    }
    return [pscustomobject]@{ Name = $Name; Parent = $caseParent; Repository = $repository; State = $state; Module = $module }
}
function New-CaseSnapshot {
    param([object]$Case, [switch]$Keep)
    return & $Case.Module {
        param($Repository, $Names, $Label, $Retain)
        New-RepositoryGeneratedStateSnapshot -RepositoryRoot $Repository -DirectoryNames $Names -Label $Label -KeepCurrentOutputs:$Retain
    } $Case.Repository $allowedNames $Case.State.Label ([bool]$Keep)
}
function Restore-CaseSnapshot {
    param([object]$Case, [object]$Snapshot, [switch]$Keep)
    & $Case.Module { param($Saved, $Retain) Restore-RepositoryGeneratedStateSnapshot -Snapshot $Saved -KeepCurrentOutputs:$Retain -Description "isolated fixture output" } $Snapshot ([bool]$Keep)
}
function Invoke-FixtureCase {
    param([string]$Name, [scriptblock]$Action)
    $case = $null
    $clock = [Diagnostics.Stopwatch]::StartNew()
    $outcome = "PASS"
    $message = $null
    try {
        $case = New-FixtureCase $Name
        & $Action $case
        Write-Host "PASS: $Name"
    } catch {
        $outcome = "FAIL"; $message = $_.Exception.Message
        Write-Host "FAIL: $Name -- $message"
    } finally {
        $clock.Stop()
        if ($null -ne $case) {
            foreach ($snapshot in $case.State.SnapshotRoots) { [void]$ownedSnapshots.Add([IO.Path]::GetFullPath($snapshot).TrimEnd('\', '/')) }
            Remove-Module -ModuleInfo $case.Module -Force -ErrorAction SilentlyContinue
        }
        $cases.Add([pscustomobject]@{ Name = $Name; Outcome = $outcome; Error = $message; ElapsedSeconds = $clock.Elapsed.TotalSeconds })
    }
}

try {
    if ([string]::IsNullOrWhiteSpace($LogDirectory)) { $LogDirectory = Join-Path (Join-Path $tempBase "geocedg-generated-state-test-logs") $runToken }
    $LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
    [void](Microsoft.PowerShell.Management\New-Item -ItemType Directory -Path $LogDirectory -Force)
    if (Test-Path -LiteralPath (Join-Path $LogDirectory "generated-state-tests.json")) { throw "Choose a fresh LogDirectory; the fixture must not overwrite prior evidence." }
    $helperHash = (Get-FileHash -LiteralPath $resolvedHelper -Algorithm SHA256).Hash.ToLowerInvariant()
    Assert-PhysicalFixturePath $fixtureRoot
    Write-FixtureFile (Join-Path $fixtureRoot ".fixture-owner") $runToken

    Invoke-FixtureCase "verification logs reject repository and normalized generated-state components" {
        param($case)
        $case.State.FailEnumeration = $true; $case.State.DenyMutation = $true
        $invalid = @($case.Repository, (Join-Path $case.Repository "."), (Join-Path $case.Repository "src/.."))
        foreach ($component in @("build", ".gradle", ".kotlin", "BuIlD", ".GRADLE", ".KOTLIN")) {
            $invalid += Join-Path $case.Repository $component
            $invalid += Join-Path $case.Repository ("source/module/" + $component + "/logs")
        }
        $invalid += Join-Path $case.Repository "artifacts/../build/logs"
        $invalid += Join-Path $case.Repository "build/../.gradle/logs"
        $invalid += Join-Path $case.Repository "source/../.kotlin/logs"
        foreach ($path in $invalid) {
            [void](Assert-Throws {
                & $case.Module { param($Root,$Logs) Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $Root -LogDirectory $Logs } $case.Repository $path
            } "repository root|outside generated-state directories")
        }
        [void](Assert-Throws {
            & $case.Module { param($Root,$Logs) Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $Root -LogDirectory $Logs } `
                (Join-Path $case.Repository "src/..") (Join-Path $case.Repository "source/build/logs")
        } "outside generated-state directories")
        Assert-Fixture ($case.State.EnumerationCalls -eq 0 -and $case.State.NewItemCalls.Count -eq 0 -and $case.State.CopyCalls.Count -eq 0 -and $case.State.RemoveCalls.Count -eq 0) "Invalid log paths triggered enumeration or mutation."
    }
    Invoke-FixtureCase "verification logs allow normalized artifacts siblings and external paths without creation" {
        param($case)
        $case.State.FailEnumeration = $true; $case.State.DenyMutation = $true
        $safe = @(
            (Join-Path $case.Repository "artifacts/verification/logs"),
            (Join-Path $case.Repository "build-not/.gradle-cache/.kotlin-cache/logs"),
            (Join-Path $case.Repository "source/build/../../artifacts/normalized-logs"),
            (Join-Path ($case.Repository + "-sibling") "build/logs"),
            (Join-Path $case.Parent "external/build/.gradle/.kotlin/logs"),
            (Join-Path $tempBase ("geocedg-generated-state-logs/" + $runToken)))
        foreach ($path in $safe) {
            $output = @(& $case.Module {
                param($Root,$Logs)
                Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $Root -LogDirectory $Logs
            } (Join-Path $case.Repository "src/..") $path)
            Assert-Fixture ($output.Count -eq 0) "Log path assertion emitted pipeline values."
            Assert-Fixture (-not (Test-Path -LiteralPath ([IO.Path]::GetFullPath($path)))) "Log guard created the proposed directory."
        }
        Assert-Fixture ($case.State.EnumerationCalls -eq 0 -and $case.State.NewItemCalls.Count -eq 0 -and $case.State.CopyCalls.Count -eq 0 -and $case.State.RemoveCalls.Count -eq 0) "Safe log path inspection performed enumeration or mutation."
    }
    Invoke-FixtureCase "verification logs reject the snapshot backup tree and device aliases" {
        param($case)
        $case.State.FailEnumeration = $true; $case.State.DenyMutation = $true
        foreach ($path in @($snapshotBase, (Join-Path $snapshotBase ($runToken + "/evidence")))) {
            [void](Assert-Throws {
                & $case.Module { param($Root,$Logs) Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $Root -LogDirectory $Logs } $case.Repository $path
            } "outside the generated-state backup tree")
        }
        if ([IO.Path]::DirectorySeparatorChar -eq [char]92) {
            $deviceAlias = '\\?\' + $case.Repository + '\artifacts\logs'
            [void](Assert-Throws {
                & $case.Module { param($Root,$Logs) Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $Root -LogDirectory $Logs } $case.Repository $deviceAlias
            } "device-prefixed aliases")
        }
        Assert-Fixture ($case.State.EnumerationCalls -eq 0 -and $case.State.NewItemCalls.Count -eq 0 -and $case.State.CopyCalls.Count -eq 0 -and $case.State.RemoveCalls.Count -eq 0) "Unsafe backup log path triggered enumeration or mutation."
    }
    Invoke-FixtureCase "verification logs reject injected linked ancestry before creating missing descendants" {
        param($case)
        $logRoot = Join-Path $case.Repository "artifacts/logs"
        $linkParent = Join-Path $case.Repository "artifacts/anchor"
        $externalParent = Join-Path $case.Parent "external"
        Write-FixtureFile (Join-Path $logRoot "sentinel") "LOG_SENTINEL"
        Write-FixtureFile (Join-Path $linkParent "sentinel") "ANCESTOR_SENTINEL"
        Write-FixtureFile (Join-Path $externalParent "sentinel") "EXTERNAL_SENTINEL"
        $case.State.FailEnumeration = $true; $case.State.DenyMutation = $true
        foreach ($scenario in @(
                @{ Linked = $logRoot; Logs = $logRoot },
                @{ Linked = (Split-Path -Parent $logRoot); Logs = $logRoot },
                @{ Linked = $case.Repository; Logs = $logRoot },
                @{ Linked = $case.Parent; Logs = $logRoot },
                @{ Linked = $linkParent; Logs = (Join-Path $linkParent "not-created/logs") },
                @{ Linked = $externalParent; Logs = (Join-Path $externalParent "not-created/logs") })) {
            $case.State.MockReparsePaths = @($scenario.Linked)
            [void](Assert-Throws {
                & $case.Module { param($Root,$Logs) Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $Root -LogDirectory $Logs } $case.Repository $scenario.Logs
            } "linked repository/log ancestry")
        }
        Assert-Fixture (-not (Test-Path -LiteralPath (Join-Path $linkParent "not-created")) -and -not (Test-Path -LiteralPath (Join-Path $externalParent "not-created"))) "Linked log path created a missing descendant."
        Assert-Fixture ([IO.File]::ReadAllText((Join-Path $logRoot "sentinel")) -ceq "LOG_SENTINEL" -and [IO.File]::ReadAllText((Join-Path $externalParent "sentinel")) -ceq "EXTERNAL_SENTINEL") "Linked log path changed existing evidence/sentinel bytes."
        Assert-Fixture ($case.State.EnumerationCalls -eq 0 -and $case.State.NewItemCalls.Count -eq 0 -and $case.State.CopyCalls.Count -eq 0 -and $case.State.RemoveCalls.Count -eq 0) "Linked log path triggered enumeration or mutation."
    }

    Invoke-FixtureCase "default exact restore removes newly generated directories" {
        param($case)
        $build = Join-Path $case.Repository "build"; $cache = Join-Path $case.Repository ".gradle"
        Write-FixtureFile (Join-Path $build "nested/original.txt") "ORIGINAL"
        [void](Microsoft.PowerShell.Management\New-Item -ItemType Directory -Path (Join-Path $build "empty") -Force)
        Write-FixtureFile (Join-Path $cache "cache.txt") "CACHE_ORIGINAL"
        [IO.File]::WriteAllBytes((Join-Path $build "binary.bin"), [byte[]]@(0,1,2,127,255))
        $beforeBuild = Get-TreeEvidence $build; $beforeCache = Get-TreeEvidence $cache
        $case.State.Targets = @($build, $cache)
        $snapshot = New-CaseSnapshot $case
        Assert-Fixture (-not $snapshot.SnapshotSkipped -and $snapshot.Entries.Count -eq 2) "Expected complete default snapshot."
        Write-FixtureFile (Join-Path $build "nested/original.txt") "MUTATED"
        Write-FixtureFile (Join-Path $build "new.txt") "NEW"
        $newOutput = Join-Path $case.Repository "module/build"
        Write-FixtureFile (Join-Path $newOutput "new.txt") "NEW_DIRECTORY"
        $case.State.Targets = @($build, $cache, $newOutput)
        Restore-CaseSnapshot $case $snapshot
        Assert-Fixture ((Get-TreeEvidence $build) -ceq $beforeBuild -and (Get-TreeEvidence $cache) -ceq $beforeCache) "Default restore did not preserve exact file bytes/tree including empty directory."
        Assert-Fixture (-not (Test-Path -LiteralPath $newOutput) -and -not (Test-Path -LiteralPath $snapshot.SnapshotRoot)) "New output or successful snapshot backup was not removed."
        Assert-Fixture ([IO.File]::ReadAllText((Join-Path $case.Repository "src/tracked.txt")) -ceq "TRACKED_SENTINEL") "Tracked fixture file changed."
    }
    Invoke-FixtureCase "Keep snapshot fast path never enumerates or copies" {
        param($case)
        $case.State.FailEnumeration = $true; $case.State.DenyMutation = $true
        $snapshot = New-CaseSnapshot $case -Keep
        Assert-Fixture ($snapshot.SnapshotSkipped -and $null -eq $snapshot.SnapshotRoot -and $snapshot.Entries.Count -eq 0) "Retention snapshot must be an explicit skipped descriptor."
        Restore-CaseSnapshot $case $snapshot -Keep
        Assert-Fixture ($case.State.EnumerationCalls -eq 0 -and $case.State.CopyCalls.Count -eq 0 -and $case.State.NewItemCalls.Count -eq 0 -and $case.State.RemoveCalls.Count -eq 0) "Keep fast path performed enumeration/copy/allocation/removal."
    }
    Invoke-FixtureCase "skipped snapshot cannot restore without Keep" {
        param($case)
        $snapshot = New-CaseSnapshot $case -Keep
        $case.State.DenyMutation = $true
        [void](Assert-Throws { Restore-CaseSnapshot $case $snapshot } "Cannot restore outputs from a snapshot explicitly skipped")
        Assert-Fixture ($case.State.EnumerationCalls -eq 0 -and $case.State.RemoveCalls.Count -eq 0) "Skipped restore attempted work before rejection."
    }
    Invoke-FixtureCase "retention of a real snapshot keeps current outputs" {
        param($case)
        $build = Join-Path $case.Repository "build"; Write-FixtureFile (Join-Path $build "value") "OLD"
        $case.State.Targets = @($build); $snapshot = New-CaseSnapshot $case
        Write-FixtureFile (Join-Path $build "value") "CURRENT"
        $case.State.FailEnumeration = $true
        Restore-CaseSnapshot $case $snapshot -Keep
        Assert-Fixture ([IO.File]::ReadAllText((Join-Path $build "value")) -ceq "CURRENT") "Retention restored old content."
        Assert-Fixture (-not (Test-Path -LiteralPath $snapshot.SnapshotRoot)) "Retention did not clean a real backup."
        Assert-Fixture ($case.State.RemoveCalls.Count -eq 1 -and $case.State.RemoveCalls[0] -eq $snapshot.SnapshotRoot) "Retention removed a current output instead of only its backup."
    }
    Invoke-FixtureCase "missing backup validates all entries before any removal" {
        param($case)
        $a = Join-Path $case.Repository "a/build"; $b = Join-Path $case.Repository "b/build"
        Write-FixtureFile (Join-Path $a "value") "A"; Write-FixtureFile (Join-Path $b "value") "B"
        $case.State.Targets = @($a,$b); $snapshot = New-CaseSnapshot $case
        $missing = [IO.Path]::GetFullPath($snapshot.Entries[1].BackupPath)
        Assert-PhysicalFixturePath $missing
        Assert-Fixture ($missing.StartsWith([IO.Path]::GetFullPath($snapshot.SnapshotRoot) + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) "Missing-backup fixture path is not in its exact owned snapshot."
        Microsoft.PowerShell.Management\Remove-Item -LiteralPath $missing -Recurse -Force
        Write-FixtureFile (Join-Path $a "value") "CURRENT_A"
        $case.State.DenyMutation = $true
        $restoreFailure = Assert-Throws { Restore-CaseSnapshot $case $snapshot } "recovery entry is missing or outside"
        Assert-Fixture ($restoreFailure.Message.Contains($snapshot.SnapshotRoot) -and (Test-Path -LiteralPath $snapshot.Entries[0].BackupPath)) "Failure lost remaining recovery data/path."
        Assert-Fixture ($case.State.RemoveCalls.Count -eq 0 -and [IO.File]::ReadAllText((Join-Path $a "value")) -ceq "CURRENT_A") "Missing backup was detected after destructive work."
    }
    Invoke-FixtureCase "restore validates all current targets before any mutation" {
        param($case)
        $build = Join-Path $case.Repository "build"
        Write-FixtureFile (Join-Path $build "value") "ORIGINAL"
        $case.State.Targets = @($build)
        $snapshot = New-CaseSnapshot $case
        Write-FixtureFile (Join-Path $build "value") "CURRENT"
        $invalid = Join-Path $case.Repository "new/build"
        Write-FixtureFile (Join-Path $invalid "value") "NEW_CURRENT_OUTPUT"
        $beforeCurrent = Get-TreeEvidence $case.Repository
        $beforeBackup = Get-TreeEvidence $snapshot.SnapshotRoot
        # This second target was not present in the snapshot entries and is
        # invalid via an injected reparse flag, despite its allowed leaf name.
        # Mutation injection prevents physical deletion even on regression.
        $case.State.Targets = @($build, $invalid)
        $case.State.MockReparsePaths = @($invalid)
        $case.State.NewItemCalls.Clear(); $case.State.CopyCalls.Clear(); $case.State.RemoveCalls.Clear()
        $case.State.EnumerationCalls = 0; $case.State.DenyMutation = $true
        $restoreFailure = Assert-Throws { Restore-CaseSnapshot $case $snapshot } "Refusing to manage generated output through a linked path"
        Assert-Fixture ($restoreFailure.Message.Contains($invalid) -and $restoreFailure.Message.Contains($snapshot.SnapshotRoot)) "Restore did not identify the late invalid target and retained recovery path."
        Assert-Fixture ($case.State.EnumerationCalls -eq 1 -and $case.State.NewItemCalls.Count -eq 0 -and $case.State.CopyCalls.Count -eq 0 -and $case.State.RemoveCalls.Count -eq 0) "Restore mutated an earlier target before validating the complete current target list."
        Assert-Fixture ((Get-TreeEvidence $case.Repository) -ceq $beforeCurrent -and (Get-TreeEvidence $snapshot.SnapshotRoot) -ceq $beforeBackup) "Rejected current target changed output, tracked files or recovery data."
    }
    Invoke-FixtureCase "remove failure preserves backups and primary message" {
        param($case)
        $build = Join-Path $case.Repository "build"; Write-FixtureFile (Join-Path $build "value") "OLD"
        $case.State.Targets = @($build); $snapshot = New-CaseSnapshot $case
        $before = Get-TreeEvidence $snapshot.SnapshotRoot
        Write-FixtureFile (Join-Path $build "value") "CURRENT"
        $case.State.FailRemovePath = $build
        $restoreFailure = Assert-Throws { Restore-CaseSnapshot $case $snapshot } "REMOVE_FAILURE_SENTINEL"
        Assert-Fixture ($restoreFailure.Message.Contains($snapshot.SnapshotRoot) -and (Get-TreeEvidence $snapshot.SnapshotRoot) -ceq $before) "Removal failure destroyed recovery data or path."
        Assert-Fixture (-not $case.State.RemoveCalls.Contains($snapshot.SnapshotRoot)) "Failure tried to delete recovery snapshot."
    }
    Invoke-FixtureCase "copy failure preserves recovery and supports explicit retry" {
        param($case)
        $build = Join-Path $case.Repository "build"; Write-FixtureFile (Join-Path $build "value") "OLD"
        $case.State.Targets = @($build); $snapshot = New-CaseSnapshot $case
        $original = Get-TreeEvidence $build; $backup = Get-TreeEvidence $snapshot.SnapshotRoot
        Write-FixtureFile (Join-Path $build "value") "CURRENT"
        $case.State.FailCopyPath = $snapshot.Entries[0].BackupPath
        $case.State.FailRemovePath = $snapshot.SnapshotRoot
        $restoreFailure = Assert-Throws { Restore-CaseSnapshot $case $snapshot } "COPY_FAILURE_SENTINEL"
        Assert-Fixture (-not $restoreFailure.Message.Contains("REMOVE_FAILURE_SENTINEL") -and $restoreFailure.Message.Contains($snapshot.SnapshotRoot)) "Cleanup masked the primary copy failure."
        Assert-Fixture ((Get-TreeEvidence $snapshot.SnapshotRoot) -ceq $backup -and -not $case.State.RemoveCalls.Contains($snapshot.SnapshotRoot)) "Copy failure removed recovery data."
        # The helper is recoverable, not atomic: current output may already be absent.
        $case.State.FailCopyPath = $null; $case.State.FailRemovePath = $null
        Restore-CaseSnapshot $case $snapshot
        Assert-Fixture ((Get-TreeEvidence $build) -ceq $original -and -not (Test-Path -LiteralPath $snapshot.SnapshotRoot)) "Explicit retry did not restore retained backup."
    }
    Invoke-FixtureCase "outside root repository root and unexpected names rejected" {
        param($case)
        $outside = Join-Path $case.Parent "outside/build"
        Write-FixtureFile (Join-Path $outside "sentinel") "OUTSIDE"
        foreach ($target in @($outside, $case.Repository, (Join-Path $case.Repository "src"))) {
            [void](Assert-Throws { & $case.Module { param($Root,$Path,$Names) Assert-GeneratedDirectoryTarget -RepositoryRoot $Root -Path $Path -DirectoryNames $Names } $case.Repository $target $allowedNames } "Refusing to manage")
        }
        Assert-Fixture ([IO.File]::ReadAllText((Join-Path $outside "sentinel")) -ceq "OUTSIDE") "Outside fixture target changed."
    }
    Invoke-FixtureCase "linked target ancestor and repository root rejected" {
        param($case)
        $build = Join-Path $case.Repository "branch/build"; Write-FixtureFile (Join-Path $build "value") "SAFE"
        $case.State.DenyMutation = $true
        foreach ($linked in @($build, (Split-Path -Parent $build), $case.Repository)) {
            $case.State.MockReparsePaths = @($linked)
            [void](Assert-Throws { & $case.Module { param($Root,$Path,$Names) Assert-GeneratedDirectoryTarget -RepositoryRoot $Root -Path $Path -DirectoryNames $Names } $case.Repository $build $allowedNames } "Refusing to manage.*linked")
        }
        Assert-Fixture ($case.State.RemoveCalls.Count -eq 0) "Linked-target validation attempted mutation."
    }
    Invoke-FixtureCase "snapshot removal rejects outside root nested and linked roots" {
        param($case)
        $build = Join-Path $case.Repository "build"; Write-FixtureFile (Join-Path $build "value") "OLD"
        $case.State.Targets = @($build); $snapshot = New-CaseSnapshot $case
        $case.State.DenyMutation = $true
        foreach ($invalid in @($case.Parent, $snapshotBase, (Join-Path $snapshot.SnapshotRoot "nested"))) {
            $fake = [pscustomobject]@{ SnapshotRoot = $invalid; SnapshotSkipped = $false }
            [void](Assert-Throws { & $case.Module { param($Saved) Remove-RepositoryGeneratedStateSnapshot -Snapshot $Saved } $fake } "Refusing to manage")
        }
        foreach ($linked in @($snapshot.SnapshotRoot, $snapshotBase)) {
            $case.State.MockReparsePaths = @($linked)
            [void](Assert-Throws { & $case.Module { param($Saved) Remove-RepositoryGeneratedStateSnapshot -Snapshot $Saved } $snapshot } "Refusing to manage.*linked")
        }
        Assert-Fixture ($case.State.RemoveCalls.Count -eq 0 -and (Test-Path -LiteralPath $snapshot.SnapshotRoot)) "Invalid snapshot target reached deletion."
    }
    Invoke-FixtureCase "restore rejects outside backup and linked recovery ancestry before removal" {
        param($case)
        $build = Join-Path $case.Repository "build"; Write-FixtureFile (Join-Path $build "value") "OLD"
        $case.State.Targets = @($build); $snapshot = New-CaseSnapshot $case
        $originalBackup = $snapshot.Entries[0].BackupPath
        $outside = Join-Path $case.Parent "outside-backup"
        Write-FixtureFile (Join-Path $outside "value") "OUTSIDE"
        $snapshot.Entries[0].BackupPath = $outside
        $case.State.DenyMutation = $true
        [void](Assert-Throws { Restore-CaseSnapshot $case $snapshot } "recovery entry is missing or outside")
        $snapshot.Entries[0].BackupPath = $originalBackup
        foreach ($linked in @($originalBackup, $snapshot.SnapshotRoot)) {
            $case.State.MockReparsePaths = @($linked)
            [void](Assert-Throws { Restore-CaseSnapshot $case $snapshot } "recovery entry uses a linked path|Refusing to manage.*linked")
        }
        Assert-Fixture ($case.State.RemoveCalls.Count -eq 0 -and (Test-Path -LiteralPath $originalBackup)) "Invalid recovery entry was detected after removal."
    }
    Invoke-FixtureCase "CleanBuild clears only enumerated allowed generated directories" {
        param($case)
        $build = Join-Path $case.Repository "build"; $cache = Join-Path $case.Repository "module/.gradle"
        $trackedBuild = Join-Path $case.Repository "tracked/build"
        Write-FixtureFile (Join-Path $build "generated") "REMOVE"
        Write-FixtureFile (Join-Path $cache "cache") "REMOVE"
        Write-FixtureFile (Join-Path $trackedBuild "tracked.txt") "TRACKED_BUILD_SENTINEL"
        Write-FixtureFile (Join-Path $case.Repository "notes/keep.txt") "UNENUMERATED"
        $case.State.Targets = @($build,$cache)
        & $case.Module { param($Root,$Names) Clear-RepositoryGeneratedOutputs -RepositoryRoot $Root -DirectoryNames $Names } $case.Repository $allowedNames
        Assert-Fixture (-not (Test-Path -LiteralPath $build) -and -not (Test-Path -LiteralPath $cache)) "Selected generated outputs survived CleanBuild."
        Assert-Fixture ([IO.File]::ReadAllText((Join-Path $trackedBuild "tracked.txt")) -ceq "TRACKED_BUILD_SENTINEL" -and [IO.File]::ReadAllText((Join-Path $case.Repository "src/tracked.txt")) -ceq "TRACKED_SENTINEL") "CleanBuild changed a mock-tracked/unselected file."
        Assert-Fixture ([IO.File]::ReadAllText((Join-Path $case.Repository "notes/keep.txt")) -ceq "UNENUMERATED" -and $case.State.CopyCalls.Count -eq 0) "CleanBuild touched unenumerated data or copied files."
    }
    Invoke-FixtureCase "CleanBuild validates the complete list before first deletion" {
        param($case)
        $build = Join-Path $case.Repository "build"; Write-FixtureFile (Join-Path $build "value") "KEEP"
        $case.State.Targets = @($build, (Join-Path $case.Repository "src"))
        $case.State.DenyMutation = $true
        [void](Assert-Throws { & $case.Module { param($Root,$Names) Clear-RepositoryGeneratedOutputs -RepositoryRoot $Root -DirectoryNames $Names } $case.Repository $allowedNames } "Refusing to manage unexpected")
        Assert-Fixture ($case.State.RemoveCalls.Count -eq 0 -and (Test-Path -LiteralPath $build)) "CleanBuild deleted an earlier target before validating all targets."
    }
} catch {
    $cases.Add([pscustomobject]@{ Name = "fixture harness"; Outcome = "FAIL"; Error = $_.Exception.Message; ElapsedSeconds = 0 })
} finally {
    foreach ($snapshot in $ownedSnapshots) {
        try { Remove-OwnedFixtureRoot -Path $snapshot -IsSnapshot $true }
        catch { $cleanupErrors.Add($_.Exception.Message) }
    }
    try { Remove-OwnedFixtureRoot -Path $fixtureRoot -IsSnapshot $false }
    catch { $cleanupErrors.Add($_.Exception.Message) }
}

$failed = @($cases | Where-Object { $_.Outcome -ne "PASS" })
$result = [ordered]@{
    schema_version = 1
    evidence_kind = "fake-first operational filesystem fixtures; no real Git enumeration or real links"
    helper_path = $resolvedHelper; helper_sha256 = $helperHash; run_id = $runToken
    started_utc = $started.ToString("o"); finished_utc = [DateTime]::UtcNow.ToString("o")
    status = if ($failed.Count -eq 0 -and $cleanupErrors.Count -eq 0) { "PASS" } else { "FAIL" }
    assertions = $script:assertions; cases = @($cases.ToArray()); cleanup_errors = @($cleanupErrors.ToArray())
    scope_note = "Enumeration and reparse flags are injected in an isolated dynamic module. Copy/remove execute only within unique owned temp fixture roots except explicitly injected throws. Existing real-Git workstation tests remain the separate enumeration integration evidence."
}
try {
    if ([string]::IsNullOrWhiteSpace($LogDirectory)) { throw "No log directory was initialized." }
    $summaryPath = Join-Path $LogDirectory "generated-state-tests.json"
    if (Test-Path -LiteralPath $summaryPath) { throw "Refusing to overwrite existing fixture summary: $summaryPath" }
    [IO.File]::WriteAllText($summaryPath, ($result | ConvertTo-Json -Depth 8), [Text.UTF8Encoding]::new($false))
    Write-Host "Generated-state fixture summary: $summaryPath"
} catch {
    Write-Error "Fixture summary failed: $($_.Exception.Message)" -ErrorAction Continue
    exit 1
}
if ($result.status -ne "PASS") {
    Write-Error ("Generated-state fixtures failed. " + (@($failed.Error) + @($cleanupErrors) -join " | ")) -ErrorAction Continue
    exit 1
}
Write-Host "Generated-state fixtures passed: $($cases.Count) cases, $script:assertions assertions."
exit 0
