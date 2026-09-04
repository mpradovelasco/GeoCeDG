#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$HelperPath = (Join-Path $PSScriptRoot '../verification-repair-equivalence.ps1'),
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) 'geocedg-repair-equivalence-tests')
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. $HelperPath
$Results = [Collections.Generic.List[object]]::new()
$EvidenceRoot = [IO.Path]::GetFullPath($LogDirectory)
[void][IO.Directory]::CreateDirectory($EvidenceRoot)
$FixtureRoot = Join-Path $EvidenceRoot ('fixtures-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($FixtureRoot)
$SummaryPath = Join-Path $EvidenceRoot 'verification-repair-equivalence-tests.json'
if (Test-Path -LiteralPath $SummaryPath) { throw 'Refusing to overwrite prior fixture evidence.' }

function Write-FixtureText {
    param([string]$Root, [string]$Path, [AllowEmptyString()] [string]$Text)
    $absolute = Join-Path $Root $Path
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $absolute))
    [IO.File]::WriteAllText($absolute, $Text, [Text.UTF8Encoding]::new($false))
}

function Commit-Fixture {
    param([string]$Root, [string]$Message)
    [void](Invoke-GeoCeDGRepairGit $Root @('add', '--all'))
    [void](Invoke-GeoCeDGRepairGit $Root @('-c', 'user.name=GeoCeDG fixture',
        '-c', 'user.email=fixture@example.invalid', '-c', 'commit.gpgSign=false',
        'commit', '--quiet', '-m', $Message))
    return (Invoke-GeoCeDGRepairGit $Root @('rev-parse', 'HEAD')).Trim()
}

function New-Fixture {
    param([string]$Name)
    $root = Join-Path $FixtureRoot $Name
    [void][IO.Directory]::CreateDirectory($root)
    [void](Invoke-GeoCeDGRepairGit $root @('init', '--quiet'))
    [void](Invoke-GeoCeDGRepairGit $root @('config', 'core.autocrlf', 'false'))
    [void](Invoke-GeoCeDGRepairGit $root @('config', 'core.hooksPath', (Join-Path $root 'empty-hooks')))
    $inputs = [ordered]@{
        'source/shared/main/Geometry.java' = "class Geometry { double exact() { return 1; } }`n"
        'source/shared/test/GeometryTest.java' = "assertEquals(1, geometry.exact(), 1e-9);`n"
        'source/desktop/main/View.java' = "class View { }`n"
        'geocedg/validation/reference.json' = '{"tolerance":1e-9}' + "`n"
        'build.gradle.kts' = 'tasks.test { useJUnit() }' + "`n"
        'gradle.properties' = 'org.gradle.jvmargs=-Xmx2g' + "`n"
        'tools/agent/verify.ps1' = '& Run-Test -Module shared -Filter exact' + "`n"
        'tools/agent/verification-runtime.psm1' = @'
function Run-Test { param($Module, $Filter); & gradle test --tests $Filter }
function Accept-Result { param($Failures); if ($Failures -ne 0) { throw 'failed' } }
function Same-Run { param($Before, $After); if ($Before -cne $After) { throw 'mutation' } }
'@
        'tools/agent/repository-generated-state.ps1' = 'function Restore-State { Restore-ExactBackup }' + "`n"
        'tools/agent/phase-lifecycle.ps1' = @'
function Identity { param($A, $B); Assert-RawEqual $A $B }
function Scientific { Assert-JUnit -Failures 0 -Errors 0 }
'@
        'docs/report.md' = "CANDIDATE`n"
    }
    foreach ($entry in $inputs.GetEnumerator()) { Write-FixtureText $root $entry.Key $entry.Value }
    $technical = Commit-Fixture $root 'Technical fixture'
    Write-FixtureText $root 'docs/report.md' "APPROVED`n"
    $closeout = Commit-Fixture $root 'Status fixture'
    $replacement = [pscustomobject]@{ before = 'Assert-RawEqual $A $B'; after = 'Assert-GitEqual $A $B' }
    Write-FixtureText $root 'tools/agent/phase-lifecycle.ps1' @'
function Identity { param($A, $B); Assert-GitEqual $A $B }
function Scientific { Assert-JUnit -Failures 0 -Errors 0 }
function New-Identity { Assert-Materialization }
'@
    Write-FixtureText $root 'tools/agent/new-identity.ps1' 'function Proof { Assert-GitTree }'
    Write-FixtureText $root 'tools/agent/tests/new-identity.Tests.ps1' 'Assert-IdentityFixture'
    Write-FixtureText $root 'docs/adr/new-identity.md' "Identity-only repair`n"
    return [pscustomobject]@{
        Root = $root; Technical = $technical; Closeout = $closeout
        Policy = @(
            [pscustomobject]@{ path = 'tools/agent/phase-lifecycle.ps1'; kind = 'IdentityInfrastructure'
                functions = @('New-Identity'); replacements = @($replacement) },
            [pscustomobject]@{ path = 'tools/agent/new-identity.ps1'; kind = 'IdentityInfrastructure'
                functions = @(); replacements = @() },
            [pscustomobject]@{ path = 'tools/agent/tests/new-identity.Tests.ps1'; kind = 'InfrastructureTest' },
            [pscustomobject]@{ path = 'docs/adr/new-identity.md'; kind = 'Documentation' }
        )
    }
}

function Invoke-Proof {
    param([object]$Fixture, [string]$CandidateCommit)
    $parameters = @{ RepositoryRoot = $Fixture.Root; ReviewedTechnicalCommit = $Fixture.Technical
        CloseoutCommit = $Fixture.Closeout; CloseoutPaths = @('docs/report.md'); RepairPaths = $Fixture.Policy }
    if ($CandidateCommit) { $parameters.CandidateCommit = $CandidateCommit }
    else { $parameters.WorkingTree = $true }
    return Get-GeoCeDGVerificationRepairEquivalence @parameters
}

function Assert-Fixture {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw "Fixture failure: $Message" }
}

function Assert-Failure {
    param([scriptblock]$Action, [string]$Pattern)
    try { & $Action | Out-Null } catch {
        if ($_.Exception.Message -notmatch $Pattern) { throw "Unexpected failure: $($_.Exception.Message)" }
        return
    }
    throw 'Expected fail-closed rejection.'
}

function Invoke-Case {
    param([string]$Name, [scriptblock]$Action)
    try {
        & $Action
        $Results.Add([ordered]@{ name = $Name; status = 'PASS' })
    } catch {
        $Results.Add([ordered]@{ name = $Name; status = 'FAIL'; error = $_.Exception.Message })
        Write-Host "FAIL $Name : $($_.Exception.Message)"
    }
}

Invoke-Case 'pending exact identity repair preserves full execution projection' {
    $fixture = New-Fixture 'pending'
    $proof = Invoke-Proof $fixture
    Assert-Fixture ($proof.executionPlanEquivalent -and $proof.unchangedExecutionInputCount -ge 8 -and
        $proof.reviewedExecutionPlanSha256 -ceq $proof.candidateExecutionPlanSha256 -and
        -not $proof.heavyEvidenceReuseAuthorizedByThisProofAlone) 'Wrong proof or premature reuse authority'
}

Invoke-Case 'committed repair has the same execution fingerprint as pending' {
    $fixture = New-Fixture 'committed'
    $pending = Invoke-Proof $fixture
    $commit = Commit-Fixture $fixture.Root 'Identity repair fixture'
    $committed = Invoke-Proof $fixture $commit
    Assert-Fixture ($pending.candidateExecutionPlanSha256 -ceq $committed.candidateExecutionPlanSha256) `
        'Commit changed execution projection'
}

foreach ($path in @('source/shared/main/Geometry.java', 'source/shared/test/GeometryTest.java',
        'source/desktop/main/View.java', 'geocedg/validation/reference.json', 'build.gradle.kts',
        'gradle.properties', 'tools/agent/verify.ps1', 'tools/agent/verification-runtime.psm1',
        'tools/agent/repository-generated-state.ps1')) {
    Invoke-Case "reject changed protected content: $path" {
        $fixture = New-Fixture ('content-' + $Results.Count)
        [IO.File]::AppendAllText((Join-Path $fixture.Root $path), "`nsemantic mutation`n")
        Assert-Failure { Invoke-Proof $fixture } 'Non-allowlisted executable/source change'
    }
}

Invoke-Case 'reject executable mode change' {
    $fixture = New-Fixture 'mode'
    [void](Invoke-GeoCeDGRepairGit $fixture.Root @('update-index', '--chmod=+x', 'tools/agent/verify.ps1'))
    Assert-Failure { Invoke-Proof $fixture } 'Non-allowlisted executable/source change'
}

Invoke-Case 'reject renamed scientific path' {
    $fixture = New-Fixture 'rename'
    [void](Invoke-GeoCeDGRepairGit $fixture.Root @('mv', 'source/shared/main/Geometry.java',
        'source/shared/main/Other.java'))
    Assert-Failure { Invoke-Proof $fixture } 'Non-allowlisted executable/source change'
}

Invoke-Case 'reject additional untracked executable input' {
    $fixture = New-Fixture 'extra'
    Write-FixtureText $fixture.Root 'source/shared/main/Hidden.java' 'class Hidden {}'
    Assert-Failure { Invoke-Proof $fixture } 'Non-allowlisted executable/source change'
}

Invoke-Case 'reject scientific Java disguised as allowlisted infrastructure' {
    $fixture = New-Fixture 'disguised'
    $fixture.Policy += [pscustomobject]@{ path = 'source/shared/main/Geometry.java'
        kind = 'IdentityInfrastructure'; functions = @(); replacements = @() }
    Assert-Failure { Invoke-Proof $fixture } 'outside bounded scope'
}

Invoke-Case 'reject existing execution function disguised as excluded identity function' {
    $fixture = New-Fixture 'masked'
    $fixture.Policy[0].functions += 'Scientific'
    Assert-Failure { Invoke-Proof $fixture } 'Only a unique NEW identity function'
}

Invoke-Case 'reject acceptance edit inside otherwise allowed helper' {
    $fixture = New-Fixture 'acceptance'
    $path = Join-Path $fixture.Root 'tools/agent/phase-lifecycle.ps1'
    [IO.File]::WriteAllText($path, [IO.File]::ReadAllText($path).Replace('-Failures 0', '-Failures 1'))
    Assert-Failure { Invoke-Proof $fixture } 'Execution code outside approved identity functions changed'
}

Invoke-Case 'reject top-level execution insertion in otherwise allowed helper' {
    $fixture = New-Fixture 'insertion'
    [IO.File]::AppendAllText((Join-Path $fixture.Root 'tools/agent/phase-lifecycle.ps1'),
        "`n& gradle test --exclude-task failing`n")
    Assert-Failure { Invoke-Proof $fixture } 'Execution code outside approved identity functions changed'
}

Invoke-Case 'reject unexpected change inside modified identity function' {
    $fixture = New-Fixture 'identity-extra'
    $path = Join-Path $fixture.Root 'tools/agent/phase-lifecycle.ps1'
    [IO.File]::WriteAllText($path, [IO.File]::ReadAllText($path).Replace(
        'Assert-GitEqual $A $B', 'Assert-GitEqual $A $B; Disable-Safety'))
    Assert-Failure { Invoke-Proof $fixture } 'Execution code outside approved identity functions changed'
}

Invoke-Case 'reject changed approved closeout content' {
    $fixture = New-Fixture 'closeout-content'
    Write-FixtureText $fixture.Root 'docs/report.md' 'Different approval'
    Assert-Failure { Invoke-Proof $fixture } 'Non-allowlisted executable/source change'
}

Invoke-Case 'reject wrong reviewed technical authority' {
    $fixture = New-Fixture 'wrong-reviewed'
    $fixture.Technical = $fixture.Closeout
    Assert-Failure { Invoke-Proof $fixture } 'Technical-to-closeout delta'
}

Invoke-Case 'reject wrong closeout authority' {
    $fixture = New-Fixture 'wrong-closeout'
    $fixture.Closeout = $fixture.Technical
    Assert-Failure { Invoke-Proof $fixture } 'Technical-to-closeout delta'
}

Invoke-Case 'reject custom clean filter before candidate hashing' {
    $fixture = New-Fixture 'filter'
    Write-FixtureText $fixture.Root '.gitattributes' 'tools/agent/phase-lifecycle.ps1 filter=untrusted'
    Assert-Failure { Invoke-Proof $fixture } 'Unsupported materialization attribute|Non-allowlisted'
}

Invoke-Case 'reject indexed filter without executing its clean command' {
    $fixture = New-Fixture 'indexed-filter'
    Write-FixtureText $fixture.Root '.gitattributes' 'source/shared/main/Geometry.java filter=untrusted'
    [void](Invoke-GeoCeDGRepairGit $fixture.Root @('add', '--', '.gitattributes'))
    [void](Invoke-GeoCeDGRepairGit $fixture.Root @('config', 'filter.untrusted.clean',
        'echo forbidden > filter-executed.txt'))
    [IO.File]::AppendAllText((Join-Path $fixture.Root 'source/shared/main/Geometry.java'), ' changed')
    Assert-Failure { Invoke-Proof $fixture } 'Unsupported materialization attribute'
    Assert-Fixture (-not (Test-Path -LiteralPath (Join-Path $fixture.Root 'filter-executed.txt'))) `
        'Safety audit executed a rejected filter'
}

Invoke-Case 'reject custom filter driver named unset before cleaning' {
    $fixture = New-Fixture 'reserved-filter'
    Write-FixtureText $fixture.Root '.gitattributes' 'source/shared/main/Geometry.java filter=unset'
    [void](Invoke-GeoCeDGRepairGit $fixture.Root @('add', '--', '.gitattributes'))
    [void](Invoke-GeoCeDGRepairGit $fixture.Root @('config', 'filter.unset.clean',
        'echo forbidden > reserved-filter-executed.txt'))
    Assert-Failure { Invoke-Proof $fixture } 'Unsupported materialization attribute'
    Assert-Fixture (-not (Test-Path -LiteralPath (Join-Path $fixture.Root 'reserved-filter-executed.txt'))) `
        'Safety audit executed a reserved-name filter driver'
}

Invoke-Case 'reject same-size content mutation with restored mtime' {
    $fixture = New-Fixture 'restored-stat'
    $path = Join-Path $fixture.Root 'source/shared/main/Geometry.java'
    $stamp = [IO.File]::GetLastWriteTimeUtc($path)
    $text = [IO.File]::ReadAllText($path)
    [IO.File]::WriteAllText($path, $text.Replace('return 1', 'return 2'))
    [IO.File]::SetLastWriteTimeUtc($path, $stamp)
    Assert-Failure { Invoke-Proof $fixture } 'Non-allowlisted executable/source change'
}

Invoke-Case 'reject unsupported working-tree encoding before candidate hashing' {
    $fixture = New-Fixture 'encoding'
    Write-FixtureText $fixture.Root '.gitattributes' 'tools/agent/phase-lifecycle.ps1 working-tree-encoding=UTF-16'
    Assert-Failure { Invoke-Proof $fixture } 'Unsupported materialization attribute|Non-allowlisted'
}

Invoke-Case 'LF and CRLF physical identity can differ without changing execution projection' {
    $fixture = New-Fixture 'line-endings'
    [void](Invoke-GeoCeDGRepairGit $fixture.Root @('config', 'core.autocrlf', 'true'))
    $before = Invoke-Proof $fixture
    $path = Join-Path $fixture.Root 'tools/agent/phase-lifecycle.ps1'
    $lf = [IO.File]::ReadAllText($path).Replace("`r`n", "`n")
    $crlf = $lf.Replace("`n", "`r`n")
    [IO.File]::WriteAllText($path, $crlf)
    $after = Invoke-Proof $fixture
    Assert-Fixture ((Get-GeoCeDGRepairTextHash $lf) -cne (Get-GeoCeDGRepairTextHash $crlf) -and
        $before.candidateExecutionPlanSha256 -ceq $after.candidateExecutionPlanSha256) 'EOL proof mismatch'
}

Invoke-Case 'headless canonical launcher preserves UTF8 Git path oracle' {
    $fixture = New-Fixture 'utf8-child'
    Write-FixtureText $fixture.Root 'docs/café point.txt' "UTF-8 path`n"
    [void](Commit-Fixture $fixture.Root 'Unicode transport fixture')
    $orchestratorPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../verify-input-identity-repair.ps1'))
    $tokens = $null; $errors = $null
    $ast = [Management.Automation.Language.Parser]::ParseFile($orchestratorPath, [ref]$tokens, [ref]$errors)
    $launcher = @($ast.EndBlock.Statements | Where-Object {
        $_ -is [Management.Automation.Language.FunctionDefinitionAst] -and
        $_.Name -ceq 'New-GeoCeDGIdentityFixtureProcessStartInfo'
    })
    Assert-Fixture ($errors.Count -eq 0 -and $launcher.Count -eq 1) 'Canonical UTF8 launcher is absent or ambiguous'
    . ([scriptblock]::Create($launcher[0].Extent.Text))
    $identityHelper = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../repository-input-identity.ps1')).Replace("'", "''")
    $probe = @'
param([string]$LogDirectory)
$ErrorActionPreference = 'Stop'
. '__IDENTITY_HELPER__'
$oracle = @(& git -c core.quotepath=false ls-files --stage) -join "`n"
if ($LASTEXITCODE -ne 0) { throw 'Independent Git oracle failed.' }
$strict = (ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
    $PWD @('-c', 'core.quotepath=false', 'ls-files', '--stage'))).TrimEnd("`r", "`n")
$result = [ordered]@{ oracleHash = Get-GeoCeDGRepositoryIdentityHash $oracle
    strictHash = Get-GeoCeDGRepositoryIdentityHash $strict
    unicodePathPreserved = $oracle.Contains('docs/café point.txt')
    consoleOutputCodePage = [Console]::OutputEncoding.CodePage; outputCodePage = $OutputEncoding.CodePage }
[void][IO.Directory]::CreateDirectory($LogDirectory)
[IO.File]::WriteAllText((Join-Path $LogDirectory 'utf8-probe.json'),
    ($result | ConvertTo-Json), [Text.UTF8Encoding]::new($false))
exit 0
'@
    $probe = $probe.Replace('__IDENTITY_HELPER__', $identityHelper)
    Write-FixtureText $fixture.Root 'tools/agent/probe café.ps1' $probe
    $outputDirectory = Join-Path $fixture.Root 'artifacts/utf8-probe'
    $start = New-GeoCeDGIdentityFixtureProcessStartInfo `
        -ScriptPath (Join-Path $fixture.Root 'tools/agent/probe café.ps1') `
        -OutputDirectory $outputDirectory -WorkingDirectory $fixture.Root
    $process = [Diagnostics.Process]::new(); $process.StartInfo = $start
    try {
        [void]$process.Start()
        $stdout = $process.StandardOutput.ReadToEndAsync(); $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $output = $stdout.GetAwaiter().GetResult(); $errorText = $stderr.GetAwaiter().GetResult()
        Assert-Fixture ($process.ExitCode -eq 0) "UTF8 probe failed: $output $errorText"
    } finally { $process.Dispose() }
    $result = Get-Content -Raw -LiteralPath (Join-Path $outputDirectory 'utf8-probe.json') | ConvertFrom-Json
    Assert-Fixture ($result.oracleHash -ceq $result.strictHash -and $result.unicodePathPreserved -and
        $result.consoleOutputCodePage -eq 65001 -and $result.outputCodePage -eq 65001) `
        'Headless child corrupted Unicode path or independent Git oracle'
}

$orchestratorPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../verify-input-identity-repair.ps1'))
$tokens = $null; $errors = $null
$ast = [Management.Automation.Language.Parser]::ParseFile($orchestratorPath, [ref]$tokens, [ref]$errors)
$resolver = @($ast.EndBlock.Statements | Where-Object {
    $_ -is [Management.Automation.Language.FunctionDefinitionAst] -and
    $_.Name -ceq 'Resolve-GeoCeDGIdentityFixtureSummary'
})
Assert-Fixture ($errors.Count -eq 0 -and $resolver.Count -eq 1) 'Canonical summary resolver is absent or ambiguous'
. ([scriptblock]::Create($resolver[0].Extent.Text))

Invoke-Case 'summary resolver accepts direct suite evidence' {
    $root = Join-Path $FixtureRoot 'summary-direct'
    Write-FixtureText $root 'summary.json' '{}'
    Assert-Fixture ((Resolve-GeoCeDGIdentityFixtureSummary $root 'summary.json') -ceq
        (Join-Path $root 'summary.json')) 'Direct summary was not selected exactly'
}

Invoke-Case 'summary resolver accepts unique GUID run evidence' {
    $root = Join-Path $FixtureRoot 'summary-run'
    $relative = '0123456789abcdef0123456789abcdef/summary.json'
    Write-FixtureText $root $relative '{}'
    Write-FixtureText $root 'retained-fixtures/summary.json' '{"notRunEvidence":true}'
    Assert-Fixture ((Resolve-GeoCeDGIdentityFixtureSummary $root 'summary.json') -ceq
        [IO.Path]::GetFullPath((Join-Path $root $relative))) 'Unique run summary was not selected exactly'
}

Invoke-Case 'summary resolver rejects missing evidence' {
    $root = Join-Path $FixtureRoot 'summary-missing'
    [void][IO.Directory]::CreateDirectory($root)
    Assert-Failure { Resolve-GeoCeDGIdentityFixtureSummary $root 'summary.json' } 'found 0 matches'
}

Invoke-Case 'summary resolver rejects ambiguous GUID run evidence' {
    $root = Join-Path $FixtureRoot 'summary-duplicate-run'
    Write-FixtureText $root '0123456789abcdef0123456789abcdef/summary.json' '{}'
    Write-FixtureText $root 'abcdef0123456789abcdef0123456789/summary.json' '{}'
    Assert-Failure { Resolve-GeoCeDGIdentityFixtureSummary $root 'summary.json' } 'found 2 matches'
}

Invoke-Case 'summary resolver rejects direct and run ambiguity' {
    $root = Join-Path $FixtureRoot 'summary-duplicate-direct'
    Write-FixtureText $root 'summary.json' '{}'
    Write-FixtureText $root '0123456789abcdef0123456789abcdef/summary.json' '{}'
    Assert-Failure { Resolve-GeoCeDGIdentityFixtureSummary $root 'summary.json' } 'found 2 matches'
}

Invoke-Case 'summary resolver rejects non-leaf path selection' {
    $root = Join-Path $FixtureRoot 'summary-traversal'
    [void][IO.Directory]::CreateDirectory($root)
    foreach ($path in @('../summary.json', 'nested/summary.json', 'C:/summary.json', 'summary.json:stream')) {
        Assert-Failure { Resolve-GeoCeDGIdentityFixtureSummary $root $path } 'exact JSON leaf filename'
    }
}

Invoke-Case 'summary resolver rejects linked GUID run directory' {
    $root = Join-Path $FixtureRoot 'summary-linked-run'
    $target = Join-Path $FixtureRoot 'summary-linked-run-target'
    [void][IO.Directory]::CreateDirectory($root)
    Write-FixtureText $target 'summary.json' '{}'
    $linkType = if ($IsWindows) { 'Junction' } else { 'SymbolicLink' }
    [void](New-Item -ItemType $linkType -Path (Join-Path $root '0123456789abcdef0123456789abcdef') -Target $target)
    Assert-Failure { Resolve-GeoCeDGIdentityFixtureSummary $root 'summary.json' } 'reparse-point'
}

Invoke-Case 'summary resolver rejects linked output ancestry' {
    $target = Join-Path $FixtureRoot 'summary-linked-root-target'
    Write-FixtureText $target 'child/summary.json' '{}'
    $link = Join-Path $FixtureRoot 'summary-linked-root'
    $linkType = if ($IsWindows) { 'Junction' } else { 'SymbolicLink' }
    [void](New-Item -ItemType $linkType -Path $link -Target $target)
    Assert-Failure { Resolve-GeoCeDGIdentityFixtureSummary (Join-Path $link 'child') 'summary.json' } 'reparse-point'
}

$canonical = [ordered]@{ schemaVersion = 1
    kind = 'FAKE_FIRST_EXECUTION_REPAIR_EQUIVALENCE_NOT_SCIENTIFIC_EXECUTION'
    tests = $Results.Count; passed = @($Results | Where-Object status -CEQ 'PASS').Count
    failed = @($Results | Where-Object status -CEQ 'FAIL').Count
    gradleExecutions = 0; authorApproved = $false; results = @($Results) }
$json = (ConvertTo-Json -InputObject $canonical -Depth 20).Replace("`r`n", "`n") + "`n"
[IO.File]::WriteAllText($SummaryPath, $json, [Text.UTF8Encoding]::new($false))
Write-Host "Repair equivalence tests: $($canonical.passed)/$($canonical.tests) PASS"
Write-Host "Deterministic summary SHA-256: $(Get-GeoCeDGRepairTextHash $json)"
Write-Host "Evidence: $SummaryPath"
if ($canonical.failed -ne 0) { exit 1 }
exit 0
