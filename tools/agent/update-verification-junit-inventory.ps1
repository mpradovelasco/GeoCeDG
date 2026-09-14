#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$InventoryPath,
    [Parameter(Mandatory)] [string[]]$DiscoveryEvidencePath,
    [string[]]$SelectionEvidencePath = @()
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
$utf8=[Text.UTF8Encoding]::new($false)
Import-Module (Join-Path $PSScriptRoot 'verification-junit.psm1') -Force
Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1') -Force
$full=[IO.Path]::GetFullPath($InventoryPath)
$inventory=[IO.File]::ReadAllText($full,[Text.UTF8Encoding]::new($false,$true))|ConvertFrom-Json -Depth 100
$discoveredByModule=[Collections.Generic.Dictionary[string,object]]::new([StringComparer]::Ordinal)
foreach($evidencePath in $DiscoveryEvidencePath){
    $evidence=[IO.File]::ReadAllText([IO.Path]::GetFullPath($evidencePath),[Text.UTF8Encoding]::new($false,$true))|ConvertFrom-Json -Depth 100
    if(-not [bool]$evidence.test_dry_run){throw 'Inventory discovery evidence must come from --test-dry-run.'}
    $selection=@($inventory.selections|Where-Object selection_id -CEQ $evidence.selection_id)
    if($selection.Count-ne1){throw "Unknown discovery selection: $($evidence.selection_id)"}
    $cases=Read-VerificationJUnitFiles -Path ([string[]]@($evidence.junit_files.path)) `
        -Module ([string]$selection[0].module)
    if($cases.Count-eq0){throw "Discovery selection emitted zero tests: $($evidence.selection_id)"}
    $module=[string]$selection[0].module
    $moduleRecord=@($inventory.modules|Where-Object module -CEQ $module)
    if($moduleRecord.Count-ne1){throw "Inventory module is not unique: $module"}
    $identities=@($cases|Sort-Object identity -CaseSensitive|ForEach-Object{[pscustomobject]@{identity=$_.identity;class_name=$_.class_name;test_name=$_.test_name}})
    $identityValues=[string[]]@($identities.identity);[Array]::Sort($identityValues,[StringComparer]::Ordinal)
    $moduleRecord[0] | Add-Member -NotePropertyName discovered_identity_count -NotePropertyValue $identityValues.Count -Force
    $moduleRecord[0] | Add-Member -NotePropertyName discovered_identities_sha256 -NotePropertyValue (Get-VerificationDeterministicHash -Value $identityValues) -Force
    if($moduleRecord[0].PSObject.Properties.Name -ccontains 'identities'){$moduleRecord[0].PSObject.Properties.Remove('identities')}
    $moduleRecord[0].discovery_evidence_sha256=(Get-FileHash $evidencePath -Algorithm SHA256).Hash.ToLowerInvariant()
    $discoveredByModule[$module]=[object[]]$identities
}
foreach($selection in $inventory.selections){
    if(-not ([string]$selection.selection_id).StartsWith('discovery.',[StringComparison]::Ordinal)){
        continue
    }
    if(-not $discoveredByModule.ContainsKey([string]$selection.module)){throw "Missing discovery evidence for module: $($selection.module)"}
    $selected=@($discoveredByModule[[string]$selection.module])
    if(@($selection.test_filters).Count-gt0){
        $selected=@($selected|Where-Object{
            $entry=$_
            @($selection.test_filters|Where-Object{
                $pattern='^'+[regex]::Escape([string]$_).Replace('\*','.*')+'$'
                $entry.class_name -cmatch $pattern -or ($entry.class_name+'.'+$entry.test_name) -cmatch $pattern
            }).Count-gt0
        })
    }
    if(@($selection.excluded_test_filters).Count-gt0){
        $selected=@($selected|Where-Object{
            $entry=$_
            @($selection.excluded_test_filters|Where-Object{
                $pattern='^'+[regex]::Escape([string]$_).Replace('\*','.*')+'$'
                $entry.class_name -cmatch $pattern -or ($entry.class_name+'.'+$entry.test_name) -cmatch $pattern
            }).Count-eq0
        })
    }
    if($selected.Count-eq0){throw "Selection has zero discovered identities: $($selection.selection_id)"}
    $identityValues=[string[]]@($selected.identity);[Array]::Sort($identityValues,[StringComparer]::Ordinal)
    $selection.expected_identity_count=$identityValues.Count
    $selection.expected_identities_sha256=Get-VerificationDeterministicHash -Value $identityValues
}
foreach($evidencePath in $SelectionEvidencePath){
    $evidence=[IO.File]::ReadAllText([IO.Path]::GetFullPath($evidencePath),[Text.UTF8Encoding]::new($false,$true))|ConvertFrom-Json -Depth 100
    if([bool]$evidence.test_dry_run){throw 'Selection inventory evidence must come from an executed test run.'}
    if([string]$evidence.completion_state -cne 'COMPLETED' -or
            $null -eq $evidence.inner_exit_code -or [int]$evidence.inner_exit_code -ne 0){
        throw "Selection inventory evidence is not a completed passing run: $($evidence.selection_id)"
    }
    $selection=@($inventory.selections|Where-Object selection_id -CEQ $evidence.selection_id)
    if($selection.Count-ne1){throw "Unknown executed selection: $($evidence.selection_id)"}
    if([string]$selection[0].module -cne [string]$evidence.selection.module -or
            [string]$selection[0].gradle_task -cne [string]$evidence.selection.gradle_task -or
            (@($selection[0].test_filters) -join "`n") -cne (@($evidence.selection.test_filters) -join "`n") -or
            (@($selection[0].excluded_test_filters) -join "`n") -cne
                (@($evidence.selection.excluded_test_filters) -join "`n")){
        throw "Executed selection contract differs from the tracked inventory: $($evidence.selection_id)"
    }
    $cases=Read-VerificationJUnitFiles -Path ([string[]]@($evidence.junit_files.path)) `
        -Module ([string]$selection[0].module)
    if($cases.Count-eq0){throw "Executed selection emitted zero tests: $($evidence.selection_id)"}
    if(@($cases|Where-Object state -cin @('FAILED','ERRORED')).Count-gt0){
        throw "Executed selection contains failed or errored tests: $($evidence.selection_id)"
    }
    $identityValues=[string[]]@($cases.identity);[Array]::Sort($identityValues,[StringComparer]::Ordinal)
    $selection[0].expected_identity_count=$identityValues.Count
    $selection[0].expected_identities_sha256=Get-VerificationDeterministicHash -Value $identityValues
}
$inventory.generated_at=$null
[IO.File]::WriteAllText($full,((ConvertTo-Json $inventory -Depth 100).Replace("`r`n","`n")+"`n"),$utf8)
