#requires -Version 7.2
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-PromptContractProperty {
    param([object]$Value, [string]$Name)
    $property = $Value.PSObject.Properties[$Name]
    if ($null -eq $property) { throw "Prompt contract is missing '$Name'." }
    return $property.Value
}

function Read-PromptContractCatalog {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Path)
    $text = [IO.File]::ReadAllText([IO.Path]::GetFullPath($Path),
        [Text.UTF8Encoding]::new($false, $true))
    $catalog = $text | ConvertFrom-Json -Depth 50 -ErrorAction Stop
    $profiles = @($catalog.profiles)
    $profileIds = @($profiles | ForEach-Object { [string]$_.profile_id })
    if (@($profileIds | Sort-Object -Unique -CaseSensitive).Count -ne
            $profileIds.Count) {
        throw 'Prompt contract profile IDs must be unique.'
    }
    foreach ($profile in $profiles) {
        $ordered = [string[]]@($profile.ordered_fields)
        $safety = [string[]]@($profile.execution_safety_fields)
        if (@($safety | Where-Object { $ordered -cnotcontains $_ }).Count -ne 0) {
            throw "Prompt profile '$($profile.profile_id)' has an execution-safety field outside ordered_fields."
        }
    }
    $documentKeys = @($catalog.documents | ForEach-Object {
        ([string]$_.profile) + '|' + ([string]$_.path)
    })
    if (@($documentKeys | Sort-Object -Unique -CaseSensitive).Count -ne
            $documentKeys.Count) {
        throw 'Prompt contract documents must be unique by profile and path.'
    }
    return $catalog
}

function Test-PromptContractDocument {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Profile
    )
    $text = [IO.File]::ReadAllText([IO.Path]::GetFullPath($Path),
        [Text.UTF8Encoding]::new($false, $true)).Replace("`r`n", "`n").Replace("`r", "`n")
    $matches = [regex]::Matches($text,
        '(?m)^\s*<!--\s*geocedg-field:\s*(?<name>[a-z][a-z0-9_]*)\s*-->\s*$')
    $observed = @($matches | ForEach-Object { $_.Groups['name'].Value })
    $required = [string[]]@(Get-PromptContractProperty $Profile 'ordered_fields')
    $safetyFields = [string[]]@(Get-PromptContractProperty $Profile 'execution_safety_fields')
    $missing = @($required | Where-Object { $observed -cnotcontains $_ })
    $duplicate = @($observed | Group-Object -CaseSensitive | Where-Object Count -gt 1 |
        ForEach-Object Name)
    $unknown = @($observed | Where-Object { $required -cnotcontains $_ } |
        Sort-Object -Unique -CaseSensitive)
    $orderedKnown = @($observed | Where-Object { $required -ccontains $_ })
    $expectedPresent = @($required | Where-Object { $observed -ccontains $_ })
    $outOfOrder = if (($orderedKnown -join "`n") -ceq ($expectedPresent -join "`n")) {
        @()
    } else { $orderedKnown }
    $titles = @([regex]::Matches($text, '(?m)^#\s+[^#\r\n].*$'))
    return [pscustomobject][ordered]@{
        path = [IO.Path]::GetFullPath($Path)
        profile = [string](Get-PromptContractProperty $Profile 'profile_id')
        observed_fields = [string[]]$observed
        missing_fields = [string[]]$missing
        duplicate_fields = [string[]]$duplicate
        unknown_fields = [string[]]$unknown
        out_of_order_fields = [string[]]$outOfOrder
        title_count = $titles.Count
        execution_safe = (@($missing | Where-Object { $safetyFields -ccontains $_ }).Count -eq 0 -and
            @($duplicate | Where-Object { $safetyFields -ccontains $_ }).Count -eq 0 -and
            @($outOfOrder | Where-Object { $safetyFields -ccontains $_ }).Count -eq 0)
    }
}

function Test-PromptContractCatalog {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [object]$Catalog
    )
    $profiles = @{}
    foreach ($profile in @($Catalog.profiles)) {
        $profiles[[string]$profile.profile_id] = $profile
    }
    $results = [Collections.Generic.List[object]]::new()
    foreach ($document in @($Catalog.documents)) {
        $profileId = [string]$document.profile
        if (-not $profiles.ContainsKey($profileId)) {
            throw "Prompt document references unknown profile '$profileId'."
        }
        $results.Add((Test-PromptContractDocument -Path (
            Join-Path $RepositoryRoot ([string]$document.path)) -Profile $profiles[$profileId]))
    }
    return [object[]]$results.ToArray()
}

Export-ModuleMember -Function @(
    'Read-PromptContractCatalog',
    'Test-PromptContractDocument',
    'Test-PromptContractCatalog'
)
