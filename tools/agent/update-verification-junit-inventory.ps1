#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$InventoryPath,
    [Parameter(Mandatory)] [string[]]$DiscoveryEvidencePath
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
    $cases=Read-VerificationJUnitFiles -Path ([string[]]@($evidence.junit_files.path))
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
    if($selected.Count-eq0){throw "Selection has zero discovered identities: $($selection.selection_id)"}
    $identityValues=[string[]]@($selected.identity);[Array]::Sort($identityValues,[StringComparer]::Ordinal)
    $selection.expected_identity_count=$identityValues.Count
    $selection.expected_identities_sha256=Get-VerificationDeterministicHash -Value $identityValues
}
$inventory.generated_at=$null
[IO.File]::WriteAllText($full,((ConvertTo-Json $inventory -Depth 100).Replace("`r`n","`n")+"`n"),$utf8)
