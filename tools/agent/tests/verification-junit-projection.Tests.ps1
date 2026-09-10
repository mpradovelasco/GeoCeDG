#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-junit.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$root = Join-Path $tempBase ('geocedg-verification-junit-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($root)
$marker = Join-Path $root '.fixture-owner'
[IO.File]::WriteAllText($marker, 'verification-junit', [Text.UTF8Encoding]::new($false))

function Assert-Case([bool]$Condition, [string]$Message) {
    $script:Assertions++
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}
function Assert-CaseThrows([scriptblock]$Action, [string]$Pattern) {
    $observed = $null
    try { & $Action | Out-Null } catch { $observed = $_.Exception }
    Assert-Case ($null -ne $observed) "Expected exception matching $Pattern."
    Assert-Case ($observed.Message -match $Pattern) "Unexpected exception: $($observed.Message)"
}
function Invoke-Case([string]$Name, [scriptblock]$Action) {
    $script:Cases++
    & $Action
    Write-Host "PASS: $Name"
}
function Write-JUnit([string]$Name, [string]$Cases) {
    $path = Join-Path $root $Name
    $xml = "<?xml version=`"1.0`" encoding=`"UTF-8`"?><testsuite name=`"fixture`">$Cases</testsuite>`n"
    [IO.File]::WriteAllText($path, $xml, [Text.UTF8Encoding]::new($false))
    return $path
}
function New-Selection([object[]]$Cases, [object[]]$Allow = @()) {
    return [pscustomobject][ordered]@{
        selection_id = 'fixture.selection'
        module = 'shared'
        expected_identity_count = $Cases.Count
        expected_identities_sha256 = Get-VerificationJUnitIdentityHash $Cases
        not_applicable_allowlist = $Allow
    }
}
function New-Evidence([string[]]$Paths, [int]$ExitCode = 0) {
    return [pscustomobject][ordered]@{
        completion_state = 'COMPLETED'
        inner_exit_code = $ExitCode
        junit_files = [object[]]@($Paths | ForEach-Object { [pscustomobject]@{ path = $_ } })
        working_directory = $root
    }
}
function New-NotApplicableEntry([string]$Identity, [string]$SourceName) {
    $source = Join-Path $root $SourceName
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes('@Disabled fixture')
    [IO.File]::WriteAllBytes($source, $bytes)
    $header = [Text.UTF8Encoding]::new($false).GetBytes('blob ' + $bytes.LongLength + [char]0)
    $sha1 = [Security.Cryptography.SHA1]::Create()
    try {
        [void]$sha1.TransformBlock($header, 0, $header.Length, $header, 0)
        [void]$sha1.TransformFinalBlock($bytes, 0, $bytes.Length)
        $blob = [Convert]::ToHexString($sha1.Hash).ToLowerInvariant()
    } finally { $sha1.Dispose() }
    return [pscustomobject]@{
        identity = $Identity
        source_path = $SourceName
        source_blob = $blob
        source_sha256 = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
        reason = 'DISABLED'
    }
}

try {
    Invoke-Case 'strict parser preserves canonical identities and successful selection' {
        $path = Write-JUnit 'pass.xml' '<testcase classname="org.example.CaféTest" name="passes"/>'
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        Assert-Case ($cases.Count -eq 1) 'Expected one testcase.'
        Assert-Case ($cases[0].identity -ceq `
                'shared::org.example.CaféTest::passes::invocation[1/1]') `
            'Unicode identity changed.'
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) (New-Selection $cases) PRODUCT
        Assert-Case ($projection.outcome -ceq 'CONTRACT_SATISFIED') 'Passing selection was not satisfied.'
        Assert-Case ($projection.semantic_domain -ceq 'PRODUCT') 'Semantic domain changed.'
    }
    Invoke-Case 'failure and error are semantic violations even when producer is nonzero' {
        $path = Write-JUnit 'violations.xml' @'
<testcase classname="org.example.ContractTest" name="fails"><failure type="AssertionError" message="expected"/></testcase>
<testcase classname="org.example.ContractTest" name="errors"><error type="IllegalStateException" message="broken"/></testcase>
'@
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path) 1) (New-Selection $cases) MIXED
        Assert-Case ($projection.outcome -ceq 'CONTRACT_VIOLATED') 'Structured violations were not semantic.'
        Assert-Case (@($projection.cases | Where-Object contract_outcome -CEQ 'CONTRACT_VIOLATED').Count -eq 2) 'Failure evidence was lost.'
        Assert-Case (@($projection.cases.failure_type | Where-Object { $_ }).Count -eq 2) 'Failure/error types were lost.'
    }
    Invoke-Case 'the same structured JUnit failure is diagnostic when its selection is diagnostic' {
        $path = Write-JUnit 'diagnostic-finding.xml' `
            '<testcase classname="org.example.HistoricalTest" name="stale"><failure type="AssertionError" message="historical projection"/></testcase>'
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        $projection = ConvertFrom-VerificationJUnitDiagnosticSelection `
            (New-Evidence @($path) 1) (New-Selection $cases) `
            HISTORICAL_CONSISTENCY_DIAGNOSTIC
        Assert-Case ($projection.contract_class -ceq `
                'HISTORICAL_CONSISTENCY_DIAGNOSTIC') `
            'Diagnostic contract class changed.'
        Assert-Case ($projection.diagnostic_outcome -ceq 'DIAGNOSTIC_FINDING') `
            'Structured historical failure was not retained as a diagnostic finding.'
        Assert-Case (@($projection.cases | Where-Object `
                contract_outcome -CEQ 'CONTRACT_VIOLATED').Count -eq 1) `
            'Diagnostic detail was lost.'
    }
    Invoke-Case 'untrusted diagnostic XML becomes unavailable without acceptance semantics' {
        $selection = [pscustomobject]@{
            selection_id='diagnostic.missing'; module='shared'; expected_identity_count=1
            expected_identities_sha256=('0'*64); not_applicable_allowlist=@()
        }
        $projection = ConvertFrom-VerificationJUnitDiagnosticSelection `
            (New-Evidence @((Join-Path $root 'diagnostic-missing.xml')) 1) `
            $selection HISTORICAL_CONSISTENCY_DIAGNOSTIC
        Assert-Case ($projection.diagnostic_outcome -ceq 'DIAGNOSTIC_UNAVAILABLE') `
            'Unavailable diagnostic evidence acquired acceptance semantics.'
        Assert-Case (-not $projection.PSObject.Properties['semantic_domain']) `
            'Diagnostic JUnit projection exposed a semantic domain.'
    }
    Invoke-Case 'required skipped or aborted-like case is incomplete coverage' {
        $path = Write-JUnit 'skipped.xml' '<testcase classname="org.example.ContractTest" name="conditional"><skipped message="aborted"/></testcase>'
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) (New-Selection $cases) SCIENTIFIC
        Assert-Case ($projection.outcome -ceq 'CONTRACT_SATISFIED') `
            'A required skipped case was misreported as a semantic violation.'
        Assert-Case ($projection.coverage_state -ceq 'INCOMPLETE') `
            'Required skipped case did not make coverage incomplete.'
        Assert-Case ($projection.cases[0].contract_outcome -ceq 'COVERAGE_INCOMPLETE') 'Skipped detail was misclassified.'
    }
    Invoke-Case 'exact allowlist is the only route to not applicable' {
        $path = Write-JUnit 'allowlisted.xml' '<testcase classname="org.example.ContractTest" name="conditional"><skipped/></testcase>'
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        $entry = New-NotApplicableEntry `
            'shared::org.example.ContractTest::conditional::invocation[1/1]' 'ConditionalTest.java'
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) `
            (New-Selection $cases @($entry)) SCIENTIFIC
        Assert-Case ($projection.outcome -ceq 'CONTRACT_SATISFIED') 'Allowlisted conditional selection was rejected.'
        Assert-Case ($projection.coverage_state -ceq 'COMPLETE') 'Allowlisted case reduced coverage.'
        Assert-Case ($projection.cases[0].contract_outcome -ceq 'NOT_APPLICABLE') 'Allowlisted case was not explicit.'
        [IO.File]::WriteAllText((Join-Path $root 'ConditionalTest.java'), '@Test fixture',
            [Text.UTF8Encoding]::new($false))
        $changed = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) `
            (New-Selection $cases @($entry)) SCIENTIFIC
        Assert-Case ($changed.outcome -ceq 'EVIDENCE_UNTRUSTED' -and
            $changed.coverage_state -ceq 'UNTRUSTED') `
            'Changed source did not invalidate the exact not-applicable declaration.'
    }
    Invoke-Case 'inventory mismatch and zero matches are untrusted' {
        $path = Write-JUnit 'unexpected.xml' '<testcase classname="org.example.Unexpected" name="appeared"/>'
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        $expected = New-Selection $cases
        $expected.expected_identities_sha256 = '0' * 64
        $unexpected = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) $expected PRODUCT
        Assert-Case ($unexpected.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Unexpected identity was accepted.'
        $empty = Write-JUnit 'empty.xml' ''
        $zero = ConvertFrom-VerificationJUnitSelection (New-Evidence @($empty)) `
            ([pscustomobject]@{selection_id='empty';module='shared';expected_identity_count=0;expected_identities_sha256=$null;not_applicable_allowlist=@()}) PRODUCT
        Assert-Case ($zero.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Zero matching tests were accepted.'
    }
    Invoke-Case 'duplicate identities are rejected across evidence files' {
        $one = Write-JUnit 'duplicate-one.xml' '<testcase classname="org.example.Duplicate" name="same"/>'
        $two = Write-JUnit 'duplicate-two.xml' '<testcase classname="org.example.Duplicate" name="same"/>'
        Assert-CaseThrows { Read-VerificationJUnitFiles @($one, $two) -Module shared } 'Duplicate JUnit identity'
    }
    Invoke-Case 'repeated parameterized display identities retain stable invocation identity' {
        $path = Write-JUnit 'repeated.xml' @'
<testcase classname="org.example.ParameterizedTest" name="same-display"/>
<testcase classname="org.example.ParameterizedTest" name="same-display"/>
'@
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        Assert-Case ($cases.Count -eq 2) 'Legitimate repeated invocations were discarded.'
        Assert-Case ($cases[0].identity -ceq `
                'shared::org.example.ParameterizedTest::same-display::invocation[1/2]') `
            'First invocation identity is unstable.'
        Assert-Case ($cases[1].identity -ceq `
                'shared::org.example.ParameterizedTest::same-display::invocation[2/2]') `
            'Second invocation identity is unstable.'
    }
    Invoke-Case 'DTD malformed and invalid UTF-8 evidence are rejected' {
        $dtd = Join-Path $root 'dtd.xml'
        [IO.File]::WriteAllText($dtd, '<!DOCTYPE testsuite [<!ENTITY x "value">]><testsuite><testcase classname="a" name="b">&x;</testcase></testsuite>', [Text.UTF8Encoding]::new($false))
        Assert-CaseThrows { Read-VerificationJUnitFiles $dtd -Module shared } 'not trusted UTF-8 XML'
        $malformed = Join-Path $root 'malformed.xml'
        [IO.File]::WriteAllText($malformed, '<testsuite><testcase>', [Text.UTF8Encoding]::new($false))
        Assert-CaseThrows { Read-VerificationJUnitFiles $malformed -Module shared } 'not trusted UTF-8 XML'
        $invalid = Join-Path $root 'invalid.xml'
        [IO.File]::WriteAllBytes($invalid, [byte[]](0x3c,0x80,0x3e))
        Assert-CaseThrows { Read-VerificationJUnitFiles $invalid -Module shared } 'not trusted UTF-8 XML'
    }
    Invoke-Case 'absent evidence and nonzero producer without violations are untrusted' {
        $missing = Join-Path $root 'missing.xml'
        $selection = [pscustomobject]@{selection_id='missing';module='shared';expected_identity_count=1;expected_identities_sha256=('0'*64);not_applicable_allowlist=@()}
        $absent = ConvertFrom-VerificationJUnitSelection (New-Evidence @($missing)) $selection PRODUCT
        Assert-Case ($absent.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Absent XML was accepted.'
        $path = Write-JUnit 'nonzero-pass.xml' '<testcase classname="org.example.ContractTest" name="passes"/>'
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        $nonzero = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path) 9) (New-Selection $cases) PRODUCT
        Assert-Case ($nonzero.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Unexplained nonzero producer was accepted.'
    }
    Invoke-Case 'semantic violations and skipped coverage are both retained' {
        $path = Write-JUnit 'violation-and-skip.xml' @'
<testcase classname="org.example.MixedEvidenceTest" name="fails"><failure type="AssertionError" message="expected"/></testcase>
<testcase classname="org.example.MixedEvidenceTest" name="skips"><skipped/></testcase>
'@
        $cases = @(Read-VerificationJUnitFiles $path -Module shared)
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path) 1) `
            (New-Selection $cases) PRODUCT
        Assert-Case ($projection.outcome -ceq 'CONTRACT_VIOLATED') `
            'Coverage loss masked a structured semantic violation.'
        Assert-Case ($projection.coverage_state -ceq 'INCOMPLETE') `
            'Structured semantic violation masked simultaneous coverage loss.'
    }
} finally {
    $resolved = [IO.Path]::GetFullPath($root)
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker) -and
            [IO.File]::ReadAllText($marker) -ceq 'verification-junit') {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    } else { throw "Fixture cleanup refused unexpected path: $resolved" }
}

Write-Host "verification-junit-projection.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"
