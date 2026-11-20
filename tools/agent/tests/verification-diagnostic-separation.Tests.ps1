#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
. (Join-Path $PSScriptRoot 'fixtures/verification-environment-fixture.ps1')

$script:Cases = 0
$script:Assertions = 0
function Assert-Case([bool]$Condition,[string]$Message){$script:Assertions++;if(-not $Condition){throw "TEST FAILURE: $Message"}}
function Invoke-Case([string]$Name,[scriptblock]$Action){$script:Cases++;&$Action;Write-Host "PASS: $Name"}

$identity=New-VerificationTestEnvironmentIdentity -BaseCommit $null -BaseTree $null `
    -CandidateCommit ('1'*40) -CandidateTree ('2'*40) -CompatibilitySignature ('3'*64)
$semantic=[pscustomobject][ordered]@{check_id='semantic.contract';node_kind='ACCEPTANCE_LEAF';contract_class='SEMANTIC';semantic_domain='MIXED';status='CONTRACT_SATISFIED';coverage_state='COMPLETE';command_identity=('4'*64);exit_code=0;cause=$null;evidence=[object[]]@();dependencies=[string[]]@();duration_ms=1}

Invoke-Case 'arbitrarily many diagnostics do not alter acceptance coverage or exit' {
    $diagnostics=foreach($index in 1..64){[pscustomobject][ordered]@{check_id="style.$index";node_kind='DIAGNOSTIC_LEAF';contract_class='STYLE_DIAGNOSTIC';outcome='DIAGNOSTIC_FINDING';command_identity=('5'*64);exit_code=9;cause='fixture';evidence=[object[]]@();dependencies=[string[]]@();duration_ms=$index}}
    $report=New-VerificationAggregatedReport -RunId diagnostics -Profile FINAL -Identity $identity -ExecutionPlanHash ('6'*64) -AcceptanceResults @($semantic) -DiagnosticResults $diagnostics -RequiredAcceptanceIds @('semantic.contract')
    Assert-Case ($report.acceptance_verdict -ceq 'ACCEPTED') 'Diagnostics changed acceptance.'
    Assert-Case ($report.coverage_verdict -ceq 'COMPLETE') 'Diagnostics changed coverage.'
    Assert-Case ($report.diagnostic_summary.findings -eq 64) 'Diagnostic findings were not all retained.'
    Assert-Case ((Get-VerificationSupervisorExitCode $report) -eq 0) 'Diagnostics changed the process exit.'
    Assert-Case (@($report.coverage.required_check_ids) -ccontains 'semantic.contract') 'Semantic coverage was omitted.'
    Assert-Case (@($report.coverage.required_check_ids | Where-Object { $_ -like 'style.*' }).Count -eq 0) 'Diagnostics entered acceptance coverage.'
}
Invoke-Case 'diagnostic unavailability remains non-acceptance data' {
    $diagnostic=[pscustomobject][ordered]@{check_id='history.unavailable';node_kind='DIAGNOSTIC_LEAF';contract_class='HISTORICAL_CONSISTENCY_DIAGNOSTIC';outcome='DIAGNOSTIC_UNAVAILABLE';command_identity=('7'*64);exit_code=$null;cause='raw diagnostic projection unavailable';evidence=[object[]]@();dependencies=[string[]]@();duration_ms=2}
    $report=New-VerificationAggregatedReport -RunId unavailable -Profile FINAL -Identity $identity -ExecutionPlanHash ('8'*64) -AcceptanceResults @($semantic) -DiagnosticResults @($diagnostic) -RequiredAcceptanceIds @('semantic.contract')
    Assert-Case ($report.acceptance_verdict -ceq 'ACCEPTED') 'Unavailable diagnostic changed acceptance.'
    Assert-Case ($report.coverage_verdict -ceq 'COMPLETE') 'Unavailable diagnostic changed coverage.'
    Assert-Case ($report.diagnostic_summary.unavailable -eq 1) 'Unavailable diagnostic was not visible.'
    Assert-Case ((Get-VerificationSupervisorExitCode $report) -eq 0) 'Unavailable diagnostic changed exit.'
}

Invoke-Case 'a documentary descendant keeps the technical result and executable or normative changes require a decision' {
    $git=(Get-Command git -CommandType Application -ErrorAction Stop|Select-Object -First 1).Path
    $pwsh=(Get-Process -Id $PID).Path
    $script=Join-Path $PSScriptRoot '../documentary-descendant.ps1'
    $tempBase=[IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\','/')
    $root=Join-Path $tempBase ('geocedg-documentary-descendant-'+[guid]::NewGuid().ToString('N'))
    $repo=Join-Path $root 'repo'
    $marker=Join-Path $root '.fixture-owner'
    function G([string[]]$A){$global:LASTEXITCODE=$null;$o=& $git -C $repo @A 2>&1;if($LASTEXITCODE -ne 0){throw "git failed: $($o -join ' ')"};return (($o|Out-String).Trim())}
    function W([string]$P,[string]$T){$f=Join-Path $repo $P;[void][IO.Directory]::CreateDirectory((Split-Path -Parent $f));[IO.File]::WriteAllText($f,$T,[Text.UTF8Encoding]::new($false))}
    function C([string]$Message){[void](G @('add','-A'));[void](G @('commit','-q','-m',$Message));return (G @('rev-parse','HEAD'))}
    function Assess([string]$T,[string]$D){
        $out=& $pwsh -NoLogo -NoProfile -File $script -TechnicalCommit $T -DocumentCommit $D -RepositoryRoot $repo
        return [pscustomobject]@{exit_code=$LASTEXITCODE;json=(($out|Out-String)|ConvertFrom-Json -Depth 10)}
    }
    [void][IO.Directory]::CreateDirectory($repo)
    [IO.File]::WriteAllText($marker,'verification-diagnostic-separation')
    try{
        [void](G @('init','-q'));[void](G @('config','user.name','Fixture'));[void](G @('config','user.email','fixture@example.invalid'));[void](G @('config','core.autocrlf','false'))
        W 'docs/notes.md' "notes`n"; W 'docs/cited.md' "cited`n"; W 'docs/sub/guide-content-unique.md' "guide`n"
        W 'tools/agent/checks/fixture-check.ps1' "Get-Content 'docs/cited.md'`n"
        W 'source/app/A.java' "class A { String g = `"guide-content-unique.md`"; }`n"
        W 'geocedg/specs/locus/contract.md' "contract`n"
        $t=C 'technical'
        W 'docs/notes.md' "notes corrected`n"; $d1=C 'documentary correction'
        $headBefore=G @('rev-parse','HEAD');$statusBefore=G @('status','--porcelain')
        $doc=Assess $t $d1
        Assert-Case ($doc.exit_code -eq 0 -and $doc.json.verdict -ceq 'DOCUMENTARY_ONLY' -and
            $doc.json.technical_result_reusable_for -ceq $t -and $doc.json.rerun_required -eq $false -and
            $doc.json.ancestry_verified -eq $true) "Documentary-only descendant was not accepted: $($doc.json.cause)"
        Assert-Case ((G @('rev-parse','HEAD')) -ceq $headBefore -and (G @('status','--porcelain')) -ceq $statusBefore) 'The assessment changed Git state.'
        $cases=@(
            @{path='source/app/A.java';text="class A { int changed; }`n";class='EXECUTABLE';name='product source'},
            @{path='docs/cited.md';text="cited changed`n";class='EXECUTABLE';name='document cited by a canonical check'},
            @{path='docs/sub/guide-content-unique.md';text="guide changed`n";class='EXECUTABLE';name='document cited by basename from a test or product source'},
            @{path='geocedg/specs/locus/contract.md';text="contract changed`n";class='NORMATIVE';name='normative specification'},
            @{path='tools/agent/checks/new-check.ps1';text="exit 0`n";class='EXECUTABLE';name='verifier input'}
        )
        foreach($case in $cases){
            [void](G @('checkout','-q',$d1))
            W $case.path $case.text
            $d=C $case.name
            $result=Assess $t $d
            $entry=@($result.json.changed_paths|Where-Object path -CEQ $case.path)
            Assert-Case ($result.exit_code -eq 5 -and $result.json.verdict -ceq 'REQUIRES_DECISION' -and
                $null -eq $result.json.technical_result_reusable_for -and
                $entry.Count -eq 1 -and $entry[0].classification -ceq $case.class) `
                "A $($case.name) change was not escalated as $($case.class): exit=$($result.exit_code)"
        }
        [void](G @('checkout','-q','--orphan','unrelated'));[void](G @('rm','-rq','--cached','.'))
        W 'docs/other.md' "other`n"; $unrelated=C 'unrelated'
        $invalid=Assess $t $unrelated
        Assert-Case ($invalid.exit_code -eq 3 -and $invalid.json.verdict -ceq 'INVALID') 'A non-descendant was not rejected.'
        $self=Assess $t $t
        Assert-Case ($self.exit_code -eq 3 -and $self.json.verdict -ceq 'INVALID') 'A commit was accepted as its own documentary descendant.'
    } finally {
        $resolved=[IO.Path]::GetFullPath($root)
        if($resolved.StartsWith($tempBase+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker) -and [IO.File]::ReadAllText($marker) -ceq 'verification-diagnostic-separation'){
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else { throw "Fixture cleanup refused unexpected path: $resolved" }
    }
}

Write-Host "verification-diagnostic-separation.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"
