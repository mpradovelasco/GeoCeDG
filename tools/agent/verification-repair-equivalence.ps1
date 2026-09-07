#requires -Version 7.2
# GeoCeDG: bounded source/plan proof for an evidence-preserving identity repair.
# This is NOT a build receipt, scientific execution or author-approval mechanism.
Set-StrictMode -Version Latest

function Get-GeoCeDGRepairTextHash {
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)
    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData(
        [Text.UTF8Encoding]::new($false).GetBytes($Text))).ToLowerInvariant()
}

function Invoke-GeoCeDGRepairGit {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments)
    $start = [Diagnostics.ProcessStartInfo]::new('git')
    $start.UseShellExecute = $false
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = [Text.UTF8Encoding]::new($false, $true)
    foreach ($argument in @('--no-optional-locks', '-c', 'core.fsmonitor=false', '-c', 'core.quotepath=false',
            '-C', $RepositoryRoot) + $Arguments) { $start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        [void]$process.Start()
        $outputTask = $process.StandardOutput.ReadToEndAsync()
        $errorTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $output = $outputTask.GetAwaiter().GetResult()
        $errorText = $errorTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            throw "Repair equivalence: git $($Arguments -join ' ') failed: $errorText"
        }
        return $output
    } finally { $process.Dispose() }
}

function Assert-GeoCeDGRepairPath {
    param([Parameter(Mandatory)] [string]$Path)
    if ([IO.Path]::IsPathRooted($Path) -or $Path -match '[\\\t\r\n]' -or
            $Path -match '(^|/)\.\.?(/|$)' -or $Path.EndsWith('/')) {
        throw "Unsupported repair path: $Path"
    }
}

function Get-GeoCeDGRepairTree {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Commit)
    if ($Commit -cnotmatch '^[0-9a-f]{40}$') { throw 'An exact commit SHA is required.' }
    $resolved = (Invoke-GeoCeDGRepairGit $RepositoryRoot @('rev-parse',
        '--verify', "$Commit^{commit}")).Trim()
    if ($resolved -cne $Commit) { throw 'Commit authority did not resolve exactly.' }
    $records = [Collections.Generic.Dictionary[string, object]]::new([StringComparer]::Ordinal)
    $text = Invoke-GeoCeDGRepairGit $RepositoryRoot @('ls-tree', '-r', '-z', '--full-tree', $Commit)
    foreach ($line in $text.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)) {
        if ($line -cnotmatch '^([0-9]{6}) blob ([0-9a-f]{40})\t(.+)$') {
            throw "Unsupported tracked mode/object in execution closure: $line"
        }
        $mode = $Matches[1]; $oid = $Matches[2]; $path = $Matches[3]
        Assert-GeoCeDGRepairPath $path
        $records.Add($path, [pscustomobject][ordered]@{ path = $path; mode = $mode; oid = $oid })
    }
    return ,$records
}

function Get-GeoCeDGRepairWorkingTree {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    $materializationConfig = Get-GeoCeDGMaterializationConfig $RepositoryRoot
    $records = [Collections.Generic.Dictionary[string, object]]::new([StringComparer]::Ordinal)
    $text = Invoke-GeoCeDGRepairGit $RepositoryRoot @('ls-files', '--stage', '-z')
    foreach ($line in $text.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)) {
        if ($line -cnotmatch '^([0-9]{6}) ([0-9a-f]{40}) 0\t(.+)$') {
            throw "Unmerged or unsupported candidate index entry: $line"
        }
        $mode = $Matches[1]; $oid = $Matches[2]; $path = $Matches[3]
        Assert-GeoCeDGRepairPath $path
        $records.Add($path, [pscustomobject][ordered]@{ path = $path; mode = $mode; oid = $oid })
    }
    $untrackedText = Invoke-GeoCeDGRepairGit $RepositoryRoot @('ls-files', '--others', '--exclude-standard', '-z')
    $paths = [string[]]@((@($records.Keys) + @($untrackedText.Split([char]0,
        [StringSplitOptions]::RemoveEmptyEntries))) | Sort-Object -Unique -CaseSensitive)
    $flags = Invoke-GeoCeDGRepairGit $RepositoryRoot @('ls-files', '-v', '-z')
    foreach ($flag in $flags.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)) {
        if (-not $flag.StartsWith('H ', [StringComparison]::Ordinal)) {
            throw "Unsupported candidate index visibility flag: $flag"
        }
    }
    # check-attr invokes no clean filter. Audit BOTH authorities before any
    # command capable of executing a filter or trusting cached stat metadata.
    $cached = Assert-GeoCeDGMaterializationAttributes $RepositoryRoot $paths -Cached `
        -ConfiguredFilterDrivers $materializationConfig.configuredFilterDrivers
    $physical = Assert-GeoCeDGMaterializationAttributes $RepositoryRoot $paths `
        -ConfiguredFilterDrivers $materializationConfig.configuredFilterDrivers
    if ($cached -cne $physical) { throw 'Candidate physical/index attributes differ.' }
    $hashPaths = [Collections.Generic.List[string]]::new()
    $visitedParents = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    $rootPath = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('/', '\')
    foreach ($path in $paths) {
        Assert-GeoCeDGRepairPath $path
        $absolute = Join-Path $RepositoryRoot $path
        if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
            [void]$records.Remove($path)
            continue
        }
        $cursor = [IO.Path]::GetFullPath($absolute)
        while (-not $cursor.Equals($rootPath, [StringComparison]::OrdinalIgnoreCase)) {
            if (-not $visitedParents.Add($cursor)) { break }
            if ((Get-Item -LiteralPath $cursor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Linked repair input is unsupported: $path"
            }
            $cursor = Split-Path -Parent $cursor
        }
        $hashPaths.Add($path)
    }
    if ($hashPaths.Count -eq 0) { return ,$records }
    # Hash every physical input, including same-size/restored-mtime edits that
    # diff-files could miss. Audited Git clean conversion, no object writes.
    $text = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        -RepositoryRoot $RepositoryRoot -Arguments @('hash-object', '--stdin-paths') `
        -InputText (($hashPaths -join "`n") + "`n"))
    $oids = @($text.TrimEnd("`r", "`n") -split "`n")
    if ($oids.Count -ne $hashPaths.Count) { throw 'Incomplete candidate blob inventory.' }
    $rawText = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        -RepositoryRoot $RepositoryRoot -Arguments @('hash-object', '--no-filters', '--stdin-paths') `
        -InputText (($hashPaths -join "`n") + "`n"))
    $rawOids = @($rawText.TrimEnd("`r", "`n") -split "`n")
    if ($rawOids.Count -ne $hashPaths.Count) { throw 'Incomplete raw candidate Git blob inventory.' }
    for ($index = 0; $index -lt $hashPaths.Count; $index++) {
        $path = $hashPaths[$index]
        $oid = if ($records.ContainsKey($path) -and
                $rawOids[$index].TrimEnd("`r") -ceq $records[$path].oid) {
            # A historical blob can itself contain CRLF. Its exact bytes remain
            # valid; do not silently renormalize the versioned object identity.
            $records[$path].oid
        } else { $oids[$index].TrimEnd("`r") }
        $mode = if ($records.ContainsKey($path)) { $records[$path].mode } else { '100644' }
        $records[$path] = [pscustomobject][ordered]@{ path = $path; mode = $mode; oid = $oid }
    }
    return ,$records
}

function Get-GeoCeDGRepairScriptProjection {
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Text,
        [Parameter(Mandatory)] [string]$Path,
        [string[]]$ExcludedFunctions = @())
    $tokens = $null; $errors = $null
    $ast = [Management.Automation.Language.Parser]::ParseInput($Text, [ref]$tokens, [ref]$errors)
    if ($errors.Count -ne 0) { throw "Invalid PowerShell execution-plan input: $Path" }
    $functions = @($ast.EndBlock.Statements | Where-Object {
        $_ -is [Management.Automation.Language.FunctionDefinitionAst]
    })
    $excluded = @($functions | Where-Object { $_.Name -cin $ExcludedFunctions })
    $remainingTokens = foreach ($token in $tokens) {
        if ($token.Kind -in @('NewLine', 'Comment', 'EndOfInput')) { continue }
        $insideExcluded = $false
        foreach ($function in $excluded) {
            if ($token.Extent.StartOffset -ge $function.Extent.StartOffset -and
                    $token.Extent.EndOffset -le $function.Extent.EndOffset) {
                $insideExcluded = $true; break
            }
        }
        if (-not $insideExcluded) {
            [ordered]@{ kind = $token.Kind.ToString(); text = $token.Extent.Text }
        }
    }
    $functionRows = @($functions | Sort-Object Name -CaseSensitive | ForEach-Object {
        [ordered]@{
            name = $_.Name
            sha256 = Get-GeoCeDGRepairTextHash $_.Extent.Text
            excludedIdentityFunction = $_.Name -cin $ExcludedFunctions
        }
    })
    return [pscustomobject][ordered]@{
        sha256 = Get-GeoCeDGRepairTextHash (ConvertTo-Json -InputObject @($remainingTokens) -Depth 10 -Compress)
        functions = $functionRows
    }
}

function Get-GeoCeDGVerificationRepairEquivalence {
    [CmdletBinding(DefaultParameterSetName = 'WorkingTree')]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
        [Parameter(Mandatory)] [string]$CloseoutCommit,
        [Parameter(Mandatory, ParameterSetName = 'Commit')] [string]$CandidateCommit,
        [Parameter(Mandatory, ParameterSetName = 'WorkingTree')] [switch]$WorkingTree,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$CloseoutPaths,
        [Parameter(Mandatory)] [object[]]$RepairPaths
    )
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    [void](Get-GeoCeDGMaterializationConfig $root)
    $technical = Get-GeoCeDGRepairTree $root $ReviewedTechnicalCommit
    $closeout = Get-GeoCeDGRepairTree $root $CloseoutCommit
    [void](Invoke-GeoCeDGRepairGit $root @('merge-base', '--is-ancestor',
        $ReviewedTechnicalCommit, $CloseoutCommit))
    $candidateAuthority = if ($WorkingTree) {
        (Invoke-GeoCeDGRepairGit $root @('rev-parse', 'HEAD')).Trim()
    } else { $CandidateCommit }
    [void](Invoke-GeoCeDGRepairGit $root @('merge-base', '--is-ancestor',
        $CloseoutCommit, $candidateAuthority))
    $candidate = if ($WorkingTree) { Get-GeoCeDGRepairWorkingTree $root }
        else { Get-GeoCeDGRepairTree $root $CandidateCommit }
    $closeoutChanges = @((@($technical.Keys) + @($closeout.Keys)) |
        Sort-Object -Unique -CaseSensitive | Where-Object {
            -not $technical.ContainsKey($_) -or -not $closeout.ContainsKey($_) -or
            $technical[$_].mode -cne $closeout[$_].mode -or
            $technical[$_].oid -cne $closeout[$_].oid
        })
    if (($closeoutChanges -join "`n") -cne
            ((@($CloseoutPaths | Sort-Object -Unique -CaseSensitive)) -join "`n")) {
        throw 'Technical-to-closeout delta differs from the exact approved path set.'
    }
    $policy = [Collections.Generic.Dictionary[string, object]]::new([StringComparer]::Ordinal)
    foreach ($record in $RepairPaths) {
        $path = [string]$record.path
        Assert-GeoCeDGRepairPath $path
        if ($path -cin $CloseoutPaths) { throw "Repair overlaps immutable R1 closeout authority: $path" }
        $kind = [string]$record.kind
        if ($kind -ceq 'Documentation') {
            if ($path -cnotmatch '^(?:AGENTS\.md|docs/(?:adr|architecture|validation|guides|developer)/[^\r\n]+\.md|geocedg/specs/operations/[^\r\n]+\.md|geocedg/validation/operations/[^\r\n]+\.json)$') {
                throw "Documentation repair is outside operational scope: $path"
            }
        } elseif ($kind -ceq 'InfrastructureTest') {
            if ($path -cnotmatch '^tools/agent/tests/[^/]+\.Tests\.ps1$') {
                throw "Only focused infrastructure tests are allowed: $path"
            }
            if ($technical.ContainsKey($path) -and @($record.replacements).Count -eq 0) {
                throw "Existing infrastructure test requires exact bounded replacements: $path"
            }
        } elseif ($kind -ceq 'IdentityInfrastructure') {
            if ($path -cnotmatch '^tools/agent/[^/]+\.ps1$') {
                throw "Identity infrastructure path is outside bounded scope: $path"
            }
            if ($technical.ContainsKey($path) -and
                    @($record.functions).Count -eq 0 -and @($record.replacements).Count -eq 0) {
                throw "Existing identity script requires exact bounded changes: $path"
            }
        } else { throw "Unknown repair classification: $kind" }
        $policy.Add($path, $record)
    }
    $allPaths = @((@($technical.Keys) + @($closeout.Keys) + @($candidate.Keys)) |
        Sort-Object -Unique -CaseSensitive)
    $beforePlan = [Collections.Generic.List[object]]::new()
    $afterPlan = [Collections.Generic.List[object]]::new()
    $changes = [Collections.Generic.List[object]]::new()
    $functionChanges = [Collections.Generic.List[object]]::new()
    foreach ($path in $allPaths) {
        $old = if ($closeout.ContainsKey($path)) { $closeout[$path] } else { $null }
        $new = if ($candidate.ContainsKey($path)) { $candidate[$path] } else { $null }
        $changed = $null -eq $old -or $null -eq $new -or
            $old.mode -cne $new.mode -or $old.oid -cne $new.oid
        if ($changed -and -not $policy.ContainsKey($path)) {
            throw "Non-allowlisted executable/source change: $path"
        }
        if ($policy.ContainsKey($path)) {
            if ($null -eq $new -or ($null -ne $old -and $old.mode -cne $new.mode)) {
                throw "Deletion or mode change is not a bounded identity repair: $path"
            }
            $rule = $policy[$path]
            if ($changed) {
                $changes.Add([ordered]@{ path = $path; kind = [string]$rule.kind
                    before = $old; after = $new })
            }
            if ([string]$rule.kind -cin @('IdentityInfrastructure', 'InfrastructureTest') -and $null -ne $old) {
                $baselineText = Invoke-GeoCeDGRepairGit $root @('show', "${CloseoutCommit}:$path")
                $currentText = if ($WorkingTree) { [IO.File]::ReadAllText((Join-Path $root $path),
                    [Text.UTF8Encoding]::new($false, $true)) }
                    else { Invoke-GeoCeDGRepairGit $root @('show', "${CandidateCommit}:$path") }
                $baselineText = $baselineText.Replace("`r`n", "`n")
                $currentText = $currentText.Replace("`r`n", "`n")
                $restoredText = $currentText
                foreach ($replacement in @($rule.replacements)) {
                    $oldText = ([string]$replacement.before).Replace("`r`n", "`n")
                    $newText = ([string]$replacement.after).Replace("`r`n", "`n")
                    if ($oldText.Length -eq 0 -or $newText.Length -eq 0 -or
                            $oldText -ceq $newText -or
                            [regex]::Matches($baselineText, [regex]::Escape($oldText)).Count -ne 1 -or
                            [regex]::Matches($restoredText, [regex]::Escape($newText)).Count -ne 1) {
                        throw "Ambiguous or absent exact identity repair replacement: $path"
                    }
                    $restoredText = $restoredText.Replace($newText, $oldText)
                    $functionChanges.Add([ordered]@{ path = $path
                        beforeSha256 = Get-GeoCeDGRepairTextHash $oldText
                        afterSha256 = Get-GeoCeDGRepairTextHash $newText
                        classification = 'EXACT_REVIEWED_IDENTITY_PROVENANCE_REPLACEMENT' })
                }
                $before = Get-GeoCeDGRepairScriptProjection $baselineText $path @($rule.functions)
                $after = Get-GeoCeDGRepairScriptProjection $restoredText $path @($rule.functions)
                if ($before.sha256 -cne $after.sha256) {
                    throw "Execution code outside approved identity functions changed: $path"
                }
                foreach ($name in @($rule.functions)) {
                    $beforeFunction = @($before.functions | Where-Object name -CEQ $name)
                    $afterFunction = @($after.functions | Where-Object name -CEQ $name)
                    if ($afterFunction.Count -ne 1 -or $beforeFunction.Count -ne 0) {
                        throw "Only a unique NEW identity function can be excluded: ${path}:$name"
                    }
                    $functionChanges.Add([ordered]@{ path = $path; function = $name
                        beforeSha256 = if ($beforeFunction.Count -eq 1) { $beforeFunction[0].sha256 } else { $null }
                        afterSha256 = $afterFunction[0].sha256
                        classification = 'EXPLICITLY_REVIEWED_IDENTITY_PROVENANCE_ONLY' })
                }
                $beforePlan.Add([ordered]@{ path = $path; mode = $old.mode
                    executionProjectionSha256 = $before.sha256 })
                $afterPlan.Add([ordered]@{ path = $path; mode = $new.mode
                    executionProjectionSha256 = $after.sha256 })
            }
            continue
        }
        if ($path -cin $CloseoutPaths) { continue }
        if ($technical[$path].mode -cne $new.mode -or $technical[$path].oid -cne $new.oid) {
            throw "Reviewed scientific/execution input changed: $path"
        }
        $beforePlan.Add($technical[$path]); $afterPlan.Add($new)
    }
    # All retained source/configuration/script contents contribute their Git mode/blob,
    # not merely filenames. Modified identity scripts additionally retain their entire
    # executable token stream except the individually reviewed identity functions.
    $beforeHash = Get-GeoCeDGRepairTextHash (ConvertTo-Json -InputObject @($beforePlan) -Depth 20 -Compress)
    $afterHash = Get-GeoCeDGRepairTextHash (ConvertTo-Json -InputObject @($afterPlan) -Depth 20 -Compress)
    if ($beforeHash -cne $afterHash) { throw 'Execution-plan fingerprint mismatch.' }
    return [pscustomobject][ordered]@{
        schemaVersion = 1
        kind = 'EVIDENCE_PRESERVING_REPAIR_STRUCTURAL_IMPACT_PROOF_NOT_EXECUTION'
        reviewedTechnicalCommit = $ReviewedTechnicalCommit
        closeoutCommit = $CloseoutCommit
        candidateCommit = if ($WorkingTree) { $null } else { $CandidateCommit }
        candidateHead = $candidateAuthority
        pendingWorktree = [bool]$WorkingTree
        closeoutPathSet = $closeoutChanges
        repairPolicySha256 = Get-GeoCeDGRepairTextHash (ConvertTo-Json -InputObject @($RepairPaths) -Depth 20 -Compress)
        changedPaths = @($changes)
        individuallyReviewedIdentityFunctions = @($functionChanges)
        unchangedExecutionInputCount = $beforePlan.Count
        reviewedExecutionPlanSha256 = $beforeHash
        candidateExecutionPlanSha256 = $afterHash
        executionPlanEquivalent = $true
        protectedContracts = @('product Java/UI', 'scientific tests', 'numerical references/tolerances',
            'Gradle/build scripts/tasks', 'module roots/test filters', 'Checkstyle requirements',
            'numerical commands', 'JVM/toolchain requirements', 'system-property/environment policy',
            'JUnit/result acceptance', 'same-run raw input checking', 'generated-state execution lifecycle')
        remainingRequiredEvidence = @('authenticated successful exact-cohort PHASE/COMPOSED/FULL linkage',
            'focused identity/lifecycle repetition', 'bounded canonical shared and Desktop live integrations',
            'review of each explicitly excluded identity/provenance function and new infrastructure input')
        heavyEvidenceReuseAuthorizedByThisProofAlone = $false
        authorApprovalInferred = $false
        priorExecutionRelabeled = $false
    }
}
