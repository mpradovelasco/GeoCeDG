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
function New-Selection([object[]]$Cases, [string[]]$Allow = @()) {
    return [pscustomobject][ordered]@{
        selection_id = 'fixture.selection'
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
    }
}

try {
    Invoke-Case 'strict parser preserves canonical identities and successful selection' {
        $path = Write-JUnit 'pass.xml' '<testcase classname="org.example.CaféTest" name="passes"/>'
        $cases = @(Read-VerificationJUnitFiles $path)
        Assert-Case ($cases.Count -eq 1) 'Expected one testcase.'
        Assert-Case ($cases[0].identity -ceq 'org.example.CaféTest::passes') 'Unicode identity changed.'
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) (New-Selection $cases) PRODUCT
        Assert-Case ($projection.outcome -ceq 'CONTRACT_SATISFIED') 'Passing selection was not satisfied.'
        Assert-Case ($projection.semantic_domain -ceq 'PRODUCT') 'Semantic domain changed.'
    }
    Invoke-Case 'failure and error are semantic violations even when producer is nonzero' {
        $path = Write-JUnit 'violations.xml' @'
<testcase classname="org.example.ContractTest" name="fails"><failure type="AssertionError" message="expected"/></testcase>
<testcase classname="org.example.ContractTest" name="errors"><error type="IllegalStateException" message="broken"/></testcase>
'@
        $cases = @(Read-VerificationJUnitFiles $path)
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path) 1) (New-Selection $cases) MIXED
        Assert-Case ($projection.outcome -ceq 'CONTRACT_VIOLATED') 'Structured violations were not semantic.'
        Assert-Case (@($projection.cases | Where-Object contract_outcome -CEQ 'CONTRACT_VIOLATED').Count -eq 2) 'Failure evidence was lost.'
        Assert-Case (@($projection.cases.failure_type | Where-Object { $_ }).Count -eq 2) 'Failure/error types were lost.'
    }
    Invoke-Case 'required skipped or aborted-like case is incomplete coverage' {
        $path = Write-JUnit 'skipped.xml' '<testcase classname="org.example.ContractTest" name="conditional"><skipped message="aborted"/></testcase>'
        $cases = @(Read-VerificationJUnitFiles $path)
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) (New-Selection $cases) SCIENTIFIC
        Assert-Case ($projection.outcome -ceq 'NOT_RUN_DEPENDENCY') 'Required skipped case did not make coverage incomplete.'
        Assert-Case ($projection.cases[0].contract_outcome -ceq 'COVERAGE_INCOMPLETE') 'Skipped detail was misclassified.'
    }
    Invoke-Case 'exact allowlist is the only route to not applicable' {
        $path = Write-JUnit 'allowlisted.xml' '<testcase classname="org.example.ContractTest" name="conditional"><skipped/></testcase>'
        $cases = @(Read-VerificationJUnitFiles $path)
        $projection = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) `
            (New-Selection $cases @('org.example.ContractTest::conditional')) SCIENTIFIC
        Assert-Case ($projection.outcome -ceq 'CONTRACT_SATISFIED') 'Allowlisted conditional selection was rejected.'
        Assert-Case ($projection.cases[0].contract_outcome -ceq 'NOT_APPLICABLE') 'Allowlisted case was not explicit.'
    }
    Invoke-Case 'inventory mismatch and zero matches are untrusted' {
        $path = Write-JUnit 'unexpected.xml' '<testcase classname="org.example.Unexpected" name="appeared"/>'
        $cases = @(Read-VerificationJUnitFiles $path)
        $expected = New-Selection $cases
        $expected.expected_identities_sha256 = '0' * 64
        $unexpected = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path)) $expected PRODUCT
        Assert-Case ($unexpected.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Unexpected identity was accepted.'
        $empty = Write-JUnit 'empty.xml' ''
        $zero = ConvertFrom-VerificationJUnitSelection (New-Evidence @($empty)) `
            ([pscustomobject]@{selection_id='empty';expected_identity_count=0;expected_identities_sha256=$null;not_applicable_allowlist=@()}) PRODUCT
        Assert-Case ($zero.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Zero matching tests were accepted.'
    }
    Invoke-Case 'duplicate identities are rejected across evidence files' {
        $one = Write-JUnit 'duplicate-one.xml' '<testcase classname="org.example.Duplicate" name="same"/>'
        $two = Write-JUnit 'duplicate-two.xml' '<testcase classname="org.example.Duplicate" name="same"/>'
        Assert-CaseThrows { Read-VerificationJUnitFiles @($one, $two) } 'Duplicate JUnit identity'
    }
    Invoke-Case 'DTD malformed and invalid UTF-8 evidence are rejected' {
        $dtd = Join-Path $root 'dtd.xml'
        [IO.File]::WriteAllText($dtd, '<!DOCTYPE testsuite [<!ENTITY x "value">]><testsuite><testcase classname="a" name="b">&x;</testcase></testsuite>', [Text.UTF8Encoding]::new($false))
        Assert-CaseThrows { Read-VerificationJUnitFiles $dtd } 'not trusted UTF-8 XML'
        $malformed = Join-Path $root 'malformed.xml'
        [IO.File]::WriteAllText($malformed, '<testsuite><testcase>', [Text.UTF8Encoding]::new($false))
        Assert-CaseThrows { Read-VerificationJUnitFiles $malformed } 'not trusted UTF-8 XML'
        $invalid = Join-Path $root 'invalid.xml'
        [IO.File]::WriteAllBytes($invalid, [byte[]](0x3c,0x80,0x3e))
        Assert-CaseThrows { Read-VerificationJUnitFiles $invalid } 'not trusted UTF-8 XML'
    }
    Invoke-Case 'absent evidence and nonzero producer without violations are untrusted' {
        $missing = Join-Path $root 'missing.xml'
        $selection = [pscustomobject]@{selection_id='missing';expected_identity_count=1;expected_identities_sha256=('0'*64);not_applicable_allowlist=@()}
        $absent = ConvertFrom-VerificationJUnitSelection (New-Evidence @($missing)) $selection PRODUCT
        Assert-Case ($absent.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Absent XML was accepted.'
        $path = Write-JUnit 'nonzero-pass.xml' '<testcase classname="org.example.ContractTest" name="passes"/>'
        $cases = @(Read-VerificationJUnitFiles $path)
        $nonzero = ConvertFrom-VerificationJUnitSelection (New-Evidence @($path) 9) (New-Selection $cases) PRODUCT
        Assert-Case ($nonzero.outcome -ceq 'EVIDENCE_UNTRUSTED') 'Unexplained nonzero producer was accepted.'
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
