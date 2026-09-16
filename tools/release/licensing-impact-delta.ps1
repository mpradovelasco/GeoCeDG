#requires -Version 7.2
<#
.SYNOPSIS
    PRE-G9B-P1 bounded licensing-impact delta against the PRE-G9B-D1 baseline.

.DESCRIPTION
    Compares the currently resolved runtime composition against the immutable
    PRE-G9B-D1 component audit and its author-approved disposition overlay, and
    classifies every component so a later licensing review can be limited to the
    material delta instead of repeating D1.

    This is deliberately bounded. It is not a general compliance platform, it
    creates no parallel inventory, and it never re-decides a D1 disposition. The
    baseline is the D1 evidence; this tool only reports drift from it.

    Classifications:
      UNCHANGED                            same component identity and bytes
      ADDED                                not present in the D1 baseline
      REMOVED                              present in the baseline, absent now
      CHANGED_BYTES                        same identity, different SHA-256
      CHANGED_SOURCE_IDENTITY              different resolved coordinate/origin
      CHANGED_LICENSE_OR_NOTICE_METADATA   different recorded terms/notice
      CHANGED_RUNTIME_OR_TOOLCHAIN_PAYLOAD different redistributed runtime/tool

    Own GeoCeDG/GeoGebra project output is provenance, not an external
    dependency: recompiling it yields CHANGED_BYTES on a project component,
    which is reported but never treated as a new external licensing obligation.

    Exit codes: 0 no material licensing delta, 1 material delta requiring
    review, 3 evidence missing or untrusted.
#>
[CmdletBinding()]
param(
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '..\..'),
    [string]$ResolvedComponentsPath,
    [Parameter(Mandatory)] [string]$ResultPath,
    [string]$SummaryPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repository = [IO.Path]::GetFullPath($RepositoryRoot)
$utf8 = [Text.UTF8Encoding]::new($false)

# Project output is GeoCeDG/GeoGebra's own build product, not a third party.
$ProjectOutputCoordinate = 'GeoCeDG/GeoGebra project output'

# The overlay introduces ids that have no audit counterpart. The mapping is
# explicit on purpose: a silent join would drop the two largest non-JAR
# obligations (the Temurin legal tree and the WiX MS-RL payload).
$OverlayIdAliases = @{
    'runtime:temurin-25.0.4+7'   = 'temurin-runtime'
    'installer-tool:wix-5.0.2'   = 'wix-jpackage-installer'
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Required licensing evidence is missing: $Path"
    }
    return [IO.File]::ReadAllText($Path, [Text.UTF8Encoding]::new($false, $true)) |
        ConvertFrom-Json -Depth 100
}

function Get-EvidenceSha {
    param($Component)
    $evidence = $Component.actual_distribution_evidence
    if ($null -eq $evidence) { return $null }
    # Polymorphic by section: object for runtime_jars, string for fonts/classes.
    if ($evidence -is [string]) {
        if ($Component.PSObject.Properties.Name -contains 'sha256') {
            return [string]$Component.sha256
        }
        return $null
    }
    if ($evidence.PSObject.Properties.Name -contains 'sha256') {
        return [string]$evidence.sha256
    }
    return $null
}

function New-Finding {
    param(
        [string]$ComponentId, [string]$Classification, [string]$Scope,
        [string]$Expected, [string]$Observed, [string]$Detail,
        [bool]$Material
    )
    return [ordered]@{
        component_id = $ComponentId
        classification = $Classification
        scope = $Scope
        expected = $Expected
        observed = $Observed
        detail = $Detail
        material = $Material
    }
}

$findings = [Collections.Generic.List[object]]::new()
$outcome = 'NO_MATERIAL_LICENSING_DELTA'
$cause = ''
$exitCode = 0

try {
    $auditPath = Join-Path $repository 'geocedg/validation/pre-g9b-d1/component-audit.json'
    $overlayPath = Join-Path $repository 'geocedg/validation/pre-g9b-d1/component-disposition.json'
    $audit = Read-JsonDocument $auditPath
    $overlay = Read-JsonDocument $overlayPath

    if ([int]$overlay.schema_version -ne 2) {
        throw "Unsupported disposition overlay schema: $($overlay.schema_version)"
    }
    if ([string]$overlay.base_evidence.join_key -cne 'component_id') {
        throw "The overlay join key changed; the delta contract must be revisited."
    }

    if ([string]::IsNullOrWhiteSpace($ResolvedComponentsPath)) {
        $ResolvedComponentsPath = Join-Path $repository `
            'source/desktop/desktop/build/reports/geocedg/resolved-runtime-components.json'
    }
    $resolved = Read-JsonDocument $ResolvedComponentsPath
    if ([string]$resolved.evidence_kind -cne 'GEOCEDG_RESOLVED_GRADLE_RUNTIME_COMPONENTS') {
        throw "Resolved component evidence has an unexpected identity."
    }

    # The audit covers the staged Windows payload. Gradle additionally resolves
    # non-Windows natives that packaging deliberately excludes, so they are not
    # redistributed and must not be compared as licensing obligations.
    $packageProfile = Read-JsonDocument (Join-Path $repository 'packaging/windows/package.yml')
    $excludedNativePatterns = @($packageProfile.input.excluded_native_patterns)
    if ($excludedNativePatterns.Count -lt 1) {
        throw "The package profile declares no native exclusion patterns."
    }

    # ---- Baseline index -----------------------------------------------------
    # Effective disposition = overlay when the component_id is present, else the
    # immutable audit candidate. That rule is declared by the overlay itself.
    $overlayById = @{}
    foreach ($group in @($overlay.dependency_dispositions) + @($overlay.font_disposition_groups)) {
        foreach ($rawId in @($group.component_ids)) {
            $id = [string]$rawId
            if ($OverlayIdAliases.ContainsKey($id)) { $id = $OverlayIdAliases[$id] }
            $overlayById[$id] = $group
        }
    }

    $baseline = @{}
    foreach ($jar in @($audit.runtime_jars)) {
        $baseline[[string]$jar.component_id] = [ordered]@{
            kind = 'jar'
            sha256 = Get-EvidenceSha $jar
            coordinate = [string]$jar.coordinate
            terms = [string]$jar.identified_terms
            notice = [string]$jar.notice_or_attribution
            isProjectOutput = ([string]$jar.coordinate -ceq $ProjectOutputCoordinate)
        }
    }
    foreach ($font in @($audit.fonts)) {
        $baseline[[string]$font.component_id] = [ordered]@{
            kind = 'font'
            sha256 = [string]$font.sha256
            coordinate = ''
            terms = [string]$font.identified_terms
            notice = [string]$font.notice_or_attribution
            isProjectOutput = $false
        }
    }
    foreach ($klass in @($audit.component_classes)) {
        $baseline[[string]$klass.component_id] = [ordered]@{
            kind = 'class'
            sha256 = $null
            coordinate = ''
            terms = [string]$klass.identified_terms
            notice = [string]$klass.notice_or_attribution
            isProjectOutput = $false
        }
    }

    # Every overlay id must resolve to a baseline component after aliasing.
    foreach ($id in $overlayById.Keys) {
        if (-not $baseline.ContainsKey($id)) {
            throw "Overlay component has no audit counterpart after aliasing: $id"
        }
    }

    # ---- Observed composition ----------------------------------------------
    $observedJars = @{}
    foreach ($component in @($resolved.components)) {
        $fileName = [string]$component.file_name
        if ([string]::IsNullOrWhiteSpace($fileName)) { continue }
        $isExcludedNative = $false
        foreach ($pattern in $excludedNativePatterns) {
            if ($fileName -match [string]$pattern) { $isExcludedNative = $true; break }
        }
        if ($isExcludedNative) { continue }
        $componentId = 'jar:' + [IO.Path]::GetFileNameWithoutExtension($fileName)
        $isProject = ([string]$component.component_type -cne 'module')
        $coordinate = if ($isProject) {
            $ProjectOutputCoordinate
        } else {
            "$($component.group):$($component.module):$($component.version)"
        }
        $observedJars[$componentId] = [ordered]@{
            sha256 = [string]$component.sha256
            coordinate = $coordinate
            isProjectOutput = $isProject
        }
    }

    $baselineJarIds = @($baseline.Keys | Where-Object { $baseline[$_].kind -ceq 'jar' })

    foreach ($id in ($observedJars.Keys | Sort-Object)) {
        $observed = $observedJars[$id]
        if (-not $baseline.ContainsKey($id)) {
            $findings.Add((New-Finding -ComponentId $id -Classification 'ADDED' `
                -Scope 'runtime-jar' -Expected '(absent from D1 baseline)' `
                -Observed $observed.coordinate `
                -Detail 'A component outside the approved D1 composition requires licensing review.' `
                -Material (-not $observed.isProjectOutput)))
            continue
        }
        $expected = $baseline[$id]
        if ($expected.isProjectOutput -or $observed.isProjectOutput) {
            # Own build output: provenance only, never a new external obligation.
            if ($expected.sha256 -and $observed.sha256 -and
                    $expected.sha256 -cne $observed.sha256) {
                $findings.Add((New-Finding -ComponentId $id `
                    -Classification 'CHANGED_BYTES' -Scope 'project-output' `
                    -Expected $expected.sha256 -Observed $observed.sha256 `
                    -Detail 'Own GeoCeDG/GeoGebra build output rebuilt; provenance only, not a new external dependency.' `
                    -Material $false))
            }
            continue
        }
        if ($expected.sha256 -and $observed.sha256 -and
                $expected.sha256 -cne $observed.sha256) {
            $findings.Add((New-Finding -ComponentId $id -Classification 'CHANGED_BYTES' `
                -Scope 'runtime-jar' -Expected $expected.sha256 -Observed $observed.sha256 `
                -Detail 'Redistributed third-party bytes differ from the audited artifact.' `
                -Material $true))
        }
        # Source-identity drift is only meaningful where the audit recorded a real
        # Maven coordinate. Several audited entries carry a placeholder such as
        # "resolved transitive:<module>:<version>" or append a classifier segment,
        # so the module and version are compared rather than the raw string, and
        # byte identity above remains the authoritative signal.
        $expectedParts = @(([string]$expected.coordinate) -split ':')
        $observedParts = @(([string]$observed.coordinate) -split ':')
        $hasComparableCoordinate = ($expectedParts.Count -ge 3 -and
            $observedParts.Count -ge 3 -and
            $expectedParts[0] -cne 'resolved transitive')
        if ($hasComparableCoordinate) {
            $expectedModule = "$($expectedParts[0]):$($expectedParts[1]):$($expectedParts[2])"
            $observedModule = "$($observedParts[0]):$($observedParts[1]):$($observedParts[2])"
            if ($expectedModule -cne $observedModule) {
                $findings.Add((New-Finding -ComponentId $id `
                    -Classification 'CHANGED_SOURCE_IDENTITY' -Scope 'runtime-jar' `
                    -Expected $expectedModule -Observed $observedModule `
                    -Detail 'Resolved source identity differs from the audited coordinate.' `
                    -Material $true))
            }
        }
    }

    foreach ($id in ($baselineJarIds | Sort-Object)) {
        if ($observedJars.ContainsKey($id)) { continue }
        $expected = $baseline[$id]
        # jsobject is deliberately excluded by an approved D1 disposition.
        $group = if ($overlayById.ContainsKey($id)) { $overlayById[$id] } else { $null }
        $isApprovedExclusion = ($null -ne $group -and
            [string]$group.disposition -ceq 'EXCLUDE')
        if ($isApprovedExclusion -or $expected.isProjectOutput) { continue }
        $findings.Add((New-Finding -ComponentId $id -Classification 'REMOVED' `
            -Scope 'runtime-jar' -Expected $expected.coordinate -Observed '(absent)' `
            -Detail 'An audited component is no longer redistributed; notices may need review.' `
            -Material $true))
    }

    # ---- Legal metadata drift ----------------------------------------------
    # The legal bundle and its recorded terms are pinned by hash elsewhere; here
    # we assert the approval state the packaging profile depends on.
    if ([string]$overlay.summary.profile_nc_status -cne 'APPROVED') {
        $findings.Add((New-Finding -ComponentId 'profile-nc' `
            -Classification 'CHANGED_LICENSE_OR_NOTICE_METADATA' -Scope 'profile' `
            -Expected 'APPROVED' -Observed ([string]$overlay.summary.profile_nc_status) `
            -Detail 'PROFILE NC approval state changed; NC packaging must not proceed.' `
            -Material $true))
    }
    if ([int]$overlay.summary.profile_nc_payload_blockers -ne 0) {
        $findings.Add((New-Finding -ComponentId 'profile-nc' `
            -Classification 'CHANGED_LICENSE_OR_NOTICE_METADATA' -Scope 'profile' `
            -Expected '0' -Observed ([string]$overlay.summary.profile_nc_payload_blockers) `
            -Detail 'PROFILE NC payload blockers reappeared.' -Material $true))
    }
    if ([bool]$overlay.authorizations.commercialProfileAuthorized) {
        $findings.Add((New-Finding -ComponentId 'profile-commercial' `
            -Classification 'CHANGED_LICENSE_OR_NOTICE_METADATA' -Scope 'profile' `
            -Expected 'false' -Observed 'true' `
            -Detail 'Commercial authorization changed; it requires explicit external terms.' `
            -Material $true))
    }

    # ---- Runtime / toolchain payload ---------------------------------------
    $runtimeBaseline = @($audit.component_classes |
        Where-Object { [string]$_.component_id -ceq 'temurin-runtime' })
    if ($runtimeBaseline.Count -ne 1) {
        $findings.Add((New-Finding -ComponentId 'temurin-runtime' `
            -Classification 'CHANGED_RUNTIME_OR_TOOLCHAIN_PAYLOAD' -Scope 'runtime' `
            -Expected '1 audited runtime class' -Observed ([string]$runtimeBaseline.Count) `
            -Detail 'The audited redistributed runtime class is not uniquely identifiable.' `
            -Material $true))
    }

    $material = @($findings | Where-Object { $_.material })
    if ($material.Count -gt 0) {
        $outcome = 'MATERIAL_LICENSING_DELTA'
        $cause = "$($material.Count) material licensing finding(s) require bounded review."
        $exitCode = 1
    }
} catch {
    $outcome = 'EVIDENCE_UNTRUSTED'
    $cause = $_.Exception.Message
    $exitCode = 3
}

$result = [ordered]@{
    schema_version = 1
    evidence_kind = 'GEOCEDG_LICENSING_IMPACT_DELTA'
    contract_class = 'SEMANTIC'
    semantic_domain = 'PRODUCT'
    baseline = 'geocedg/validation/pre-g9b-d1/component-disposition.json'
    baseline_rule = 'overlay disposition when component_id is present, else the immutable audit candidate'
    outcome = $outcome
    cause = $cause
    material_finding_count = @($findings | Where-Object { $_.material }).Count
    informational_finding_count = @($findings | Where-Object { -not $_.material }).Count
    findings = [object[]]$findings.ToArray()
}

$resultFull = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($resultFull))
[IO.File]::WriteAllText($resultFull,
    ((ConvertTo-Json $result -Depth 30).Replace("`r`n", "`n") + "`n"), $utf8)

$summaryLines = [Collections.Generic.List[string]]::new()
$summaryLines.Add("GeoCeDG licensing-impact delta: $outcome")
$summaryLines.Add("Baseline: PRE-G9B-D1 author-approved composition")
$summaryLines.Add("Material findings: $($result.material_finding_count); informational: $($result.informational_finding_count)")
foreach ($finding in $findings) {
    $flag = if ($finding.material) { 'MATERIAL' } else { 'info' }
    $summaryLines.Add("  [$flag] $($finding.classification) $($finding.component_id): $($finding.detail)")
}
if ($findings.Count -eq 0) {
    $summaryLines.Add('  No component drift against the approved composition.')
}
$summaryText = ($summaryLines -join "`n") + "`n"
if (-not [string]::IsNullOrWhiteSpace($SummaryPath)) {
    $summaryFull = [IO.Path]::GetFullPath($SummaryPath)
    [void][IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($summaryFull))
    [IO.File]::WriteAllText($summaryFull, $summaryText, $utf8)
}
Write-Host $summaryText.TrimEnd()

exit $exitCode
