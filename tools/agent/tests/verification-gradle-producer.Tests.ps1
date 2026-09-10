#requires -Version 7.2
[CmdletBinding()]param()
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop'
$script:Cases=0;$script:Assertions=0
function A([bool]$c,[string]$m){$script:Assertions++;if(-not$c){throw "TEST FAILURE: $m"}}
function C([string]$n,[scriptblock]$b){$script:Cases++;&$b;Write-Host "PASS: $n"}
$commandSource=[IO.File]::ReadAllText((Join-Path $PSScriptRoot '../checks/gradle-command-evidence-producer.ps1'))
$testSource=[IO.File]::ReadAllText((Join-Path $PSScriptRoot '../checks/gradle-test-evidence-producer.ps1'))
$registrySource=[IO.File]::ReadAllText((Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-registry.json'))
$registry=$registrySource|ConvertFrom-Json -Depth 100
C 'argument arrays and deterministic Gradle boundary' {A($commandSource.Contains('ArgumentList.Add')) 'native array missing';A($commandSource.Contains('[string]$GradleTask')) 'nominal task boundary missing';A($commandSource.Contains('-Dorg.gradle.java.installations.auto-download=false')) 'toolchain isolation missing';A(-not($commandSource-match'--%|(?i)timeout|Kill\(')) 'forbidden control found'}
C 'test producer is neutral and XML-only' {A($testSource.Contains('gradle-command-evidence-producer.ps1')) 'neutral producer delegation missing';A($testSource.Contains("Get-ChildItem -LiteralPath `$junitRoot -Recurse -File -Filter '*.xml'")) 'JUnit evidence discovery missing';A(-not($testSource-match'CONTRACT_(SATISFIED|VIOLATED)')) 'producer emits verdict'}
C 'test exclusions are encoded structurally in the Gradle init script' {
    A($testSource.Contains('excluded_test_filters')) 'Excluded selection filters are not declared.'
    A($testSource.Contains('excludeTestsMatching')) 'Excluded selection filters do not reach Gradle structurally.'
    A(-not ($testSource -match 'Start-Process|Invoke-Expression')) 'Exclusions use a shell boundary.'
}
C 'registry never binds Gradle option tokens through an array script parameter' {A(-not($registrySource.Contains('-GradleArguments'))) 'registry uses ambiguous PowerShell array binding';A(@($registry.nodes|Where-Object check_id -like 'compile.*.producer'|Where-Object {$_.argv -ccontains '-GradleTask'}).Count -eq 2) 'compile producers lack nominal tasks'}
C 'Checkstyle has separate verdict-neutral producers and style projections' {A(@($registry.nodes|Where-Object check_id -like 'checkstyle.*.producer').Count -eq 4) 'Checkstyle producer count differs';A(@($registry.nodes|Where-Object check_id -like 'checkstyle.*.diagnostic'|Where-Object contract_class -CEQ 'STYLE_DIAGNOSTIC').Count -eq 4) 'Checkstyle is not projected only as style diagnostics'}
C 'nominal task survives the native pwsh file boundary' {
    if(-not$IsWindows){A($true)'Windows-only native boundary is not applicable';return}
    $tempBase=[IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\','/');$root=Join-Path $tempBase ('geocedg-gradle-producer-fixture-'+[guid]::NewGuid().ToString('N'));$repo=Join-Path $root 'repo';$output=Join-Path $root 'output';[void][IO.Directory]::CreateDirectory($repo);$marker=Join-Path $root '.fixture-owner';[IO.File]::WriteAllText($marker,'verification-gradle-producer',[Text.UTF8Encoding]::new($false));[IO.File]::WriteAllText((Join-Path $repo 'gradlew.bat'),"@echo off`r`nexit /b 0`r`n",[Text.ASCIIEncoding]::new())
    try{$evidence=Join-Path $root 'evidence.json';& pwsh -NoLogo -NoProfile -File (Join-Path $PSScriptRoot '../checks/gradle-command-evidence-producer.ps1') -EvidencePath $evidence -GradleTask ':fixture:compile' -RepositoryRoot $repo -OutputDirectory $output;A($LASTEXITCODE-eq0)'producer launch failed';$value=[IO.File]::ReadAllText($evidence,[Text.UTF8Encoding]::new($false,$true))|ConvertFrom-Json -Depth 30;A($value.completion_state-ceq'COMPLETED')'fake wrapper did not complete';A(($value.arguments[0]-ceq':fixture:compile'))'nominal task changed';A($value.arguments-ccontains'-Dorg.gradle.java.installations.auto-download=false')'toolchain flag missing'}finally{$resolved=[IO.Path]::GetFullPath($root);if($resolved.StartsWith($tempBase+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)-and(Test-Path $marker)-and[IO.File]::ReadAllText($marker)-ceq'verification-gradle-producer'){Remove-Item $resolved -Recurse -Force}else{throw "Fixture cleanup refused: $resolved"}}
}
C 'excluded selection survives the neutral producer boundary' {
    if(-not$IsWindows){A($true)'Windows-only producer fixture is not applicable';return}
    $tempBase=[IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\','/')
    $root=Join-Path $tempBase ('geocedg-gradle-exclusion-fixture-'+[guid]::NewGuid().ToString('N'))
    $repo=Join-Path $root 'repo';$output=Join-Path $root 'output'
    [void][IO.Directory]::CreateDirectory($repo)
    $marker=Join-Path $root '.fixture-owner'
    [IO.File]::WriteAllText($marker,'verification-gradle-exclusion',[Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText((Join-Path $repo 'gradlew.bat'),"@echo off`r`nexit /b 0`r`n",[Text.ASCIIEncoding]::new())
    $inventory=Join-Path $root 'inventory.json'
    $selection=[ordered]@{
        selection_id='fixture.exclusion';module='desktop';gradle_task=':fixture:test'
        test_filters=@();excluded_test_filters=@('org.example.ContractTest.excluded')
    }
    [IO.File]::WriteAllText($inventory,(ConvertTo-Json @{selections=@($selection)} -Depth 10),[Text.UTF8Encoding]::new($false))
    try{
        $evidence=Join-Path $root 'evidence.json'
        & pwsh -NoLogo -NoProfile -File (Join-Path $PSScriptRoot '../checks/gradle-test-evidence-producer.ps1') `
            -EvidencePath $evidence -InventoryPath $inventory -SelectionId fixture.exclusion `
            -RepositoryRoot $repo -OutputDirectory $output
        A($LASTEXITCODE-eq0)'test producer launch failed'
        $init=[IO.File]::ReadAllText((Join-Path $output 'junit-output.init.gradle'))
        A($init.Contains("excludeTestsMatching 'org.example.ContractTest.excluded'")) `
            'Excluded filter was not encoded literally in the init script.'
        $value=[IO.File]::ReadAllText($evidence,[Text.UTF8Encoding]::new($false,$true))|ConvertFrom-Json -Depth 30
        A(@($value.selection.excluded_test_filters).Count-eq1) `
            'Excluded filter was not preserved in producer evidence.'
    }finally{
        $resolved=[IO.Path]::GetFullPath($root)
        if($resolved.StartsWith($tempBase+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)-and(Test-Path $marker)-and[IO.File]::ReadAllText($marker)-ceq'verification-gradle-exclusion'){
            Remove-Item $resolved -Recurse -Force
        }else{throw "Fixture cleanup refused: $resolved"}
    }
}
Write-Host "verification-gradle-producer.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"
