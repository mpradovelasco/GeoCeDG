# GeoCeDG verification levels and bootstrap-impact contract

- Status: **NORMATIVE / AUTHOR APPROVED through section 11**;
  section 12 is an **IMPLEMENTATION CANDIDATE — AUTHOR REVIEW READY** amendment
- Scope: operational/build/verification infrastructure; no product semantics
- Decision: [accepted ADR 0020 and author closeout](../../../docs/adr/0020-verification-levels-and-current-run-evidence.md#author-approval-and-closeout)
- Existing authority: [ADR 0002](../../../docs/adr/0002-g1-operational-authority.md)
- Prospective amendment: [ADR 0025](../../../docs/adr/0025-commit-first-acceptance-and-dual-closeout.md)
- Accepted sections 1–11 evidence state: implementation and independent technical review complete;
  author approval applies to `2b82034dbedf6f26250ad4aefb9eead700e33e66` and its
  status-only closeout. The technical contract below is unchanged.
- Historical pre-final02 checkpoint: applied implementation candidate; original characterization and
  completed/pending candidate checks are recorded separately in the
  [performance report](../../../docs/validation/verification_performance_report.md)
  and [bootstrap audit](../../../docs/validation/bootstrap_workstation_report.md).
  FULL02, clean-output FULL and bounded saved-case relations cover their recorded
  source checkpoints, not later edits. The first candidate bootstrap's Conda
  display-identity false rejection and the next attempt's native-output
  blank-line binding failure are retained. Their successive 150- and 158-assertion
  workstation focuses retain their own source cohorts. Bootstrap03 subsequently
  completed the real normal path and nested COMPOSED with exit 0. Its separate
  timestamp-link envelope remains failed with exit 2. Separate archived
  reconciliation passed with explicit post-run review pins; it neither rewrites
  that failure nor fabricates an original nested-result link. Bootstrap evidence
  and final-source gates retain distinct records. Final CI-profile attempt01
  failed at benchmark precheck because an existing generated-state result collided
  with a fixed default operational log directory; its root remains FAILED.
  The applied default-only invocation-unique directory correction passed its
  focused 111-runtime/18-generated-case,143-assertion operational validation;
  that fake-first result is not FULL or a benchmark. Replacement attempt02 remains
  pending at that historical checkpoint, not at the accepted closeout above.

### Historical documentary checkpoint and final execution record

The document version retained in reviewed implementation
`2b82034dbedf6f26250ad4aefb9eead700e33e66` was frozen before replacement
exact-source FULL attempt `final-full-ci-profile-02`.
Its original pending language records that checkpoint, not the current closeout:
final02 subsequently passed. Attempt `final-full-ci-profile-01` remains
FAILED at the delegated benchmark precheck; an intermediate receipt does not
replace its failed root. Later completion is recorded in the designated
[measurement](../../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/measurement-result.json),
[final root result](../../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/verification-result.json),
[delegated benchmark JSON](../../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/operational-benchmark.json)
and [final candidate closeout](../../../artifacts/verification-performance-bootstrap/inventory/final-candidate-closeout.md),
as separate saved execution evidence. These status-only closeout edits do not
alter executable inputs or those records. A missing or failed record
does not satisfy the gate. These ignored local artifacts require the retained
artifact tree or a separately supplied bundle; they do not confer author approval.

## 1. Purpose and authority

Reduce development and verification elapsed time without weakening geometric,
scientific, persistence, compatibility, licensing or governance checks. The
executable authority remains `tools/agent/verify.ps1` and the focused verifiers
it composes. This contract does not replace their assertions or accepted feature
specifications. It is not a GeoCeDG product phase and authorizes no future phase.

The interface in sections 1–11 is the accepted implementation. Section 12 is a
prospective implementation-candidate amendment. Commands are not evidence of
execution; implementation and measured results must be reported separately.
Existing accepted gates remain in force throughout adoption.

## 2. Four verification levels

| Level | Intended use | Required scope and claim |
|---|---|---|
| DEV | Inner development loop | Explicit module/test filters, required compilation dependencies, directly affected and explicitly selected adjacent tests; incomplete global coverage, never acceptance. |
| PHASE | Named capability/regression perimeter | Existing named verifier, all of its normative tests and lifecycle/persistence/compatibility, dependency and numerical checks; no inferred global coverage. |
| COMPOSED | Repository candidate integration/review | Current applicable cross-phase scientific and governance checks, baseline build and phase assertions; default top-level mode. |
| FULL | Exhaustive repository assurance | COMPOSED plus unfiltered shared-JRE and Desktop test suites, retaining all required numerical/reference and non-test checks. |

The levels describe coverage and intended use, not phase authorization. A
smaller DEV or PHASE selection never reduces COMPOSED/FULL coverage. A successful
FULL run is technical evidence, not author approval or release permission. Its
technical result is also independent of closeout consumability: section 12
requires an explicit evidence use and clean exact-commit cohort before a run
can be consumed as final acceptance evidence.

FULL is required for phase closeout, release, major integration, build/toolchain
or verifier/bootstrap changes, shared kernel infrastructure changes, and changes
whose regression perimeter cannot be bounded reliably. The infrastructure-impact
rule in section 8 also applies. Section 10 permits explicit linkage for a
status-only closeout; section 11 defines the author-approved narrow
evidence-preserving identity/provenance repair exception. The prospective
section 12 amendment makes final acceptance commit-first and adds two explicit
post-approval closeout modes. None permits a receipt from a previous process to
become a current-run capability.

## 3. Entry points and parameter contracts

All examples run from the repository root with PowerShell 7.2 or later. The
top-level verifier, shared runtime module and isolated operational fixture
entrypoints enforce this minimum with `#requires -Version 7.2`. Importing the
module enforces the same boundary for focused receipt/incremental verifiers;
do not rewrite sealed historical scripts merely to repeat the requirement.

PowerShell 7.0/7.1 are excluded because redirected native stderr can be promoted
by `ErrorActionPreference=Stop` before an explicit native exit is captured. The
changed stderr behaviour starts in 7.2; disabling native exit-code promotion
alone does not repair older hosts. This floor preserves existing fail-closed
native capture rather than rewriting all inherited launch paths.
[Microsoft native-error preference documentation](https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_preference_variables?view=powershell-7.5#erroractionpreference)
documents the version boundary.

```powershell
# Explicit, incomplete inner-loop evidence.
.\tools\agent\verify.ps1 -Level DEV -Module shared `
  -TestFilter 'org.geocedg.common.locus.G9U0R6SemanticLocusPointInteractionTest' `
  -KeepBuildOutputs

# Existing capability authority and its declared regression perimeter.
.\tools\agent\verify.ps1 -Level PHASE -Phase G9U0-R6 -KeepBuildOutputs

# Default repository-level candidate coverage.
.\tools\agent\verify.ps1
.\tools\agent\verify.ps1 -Level COMPOSED

# Exhaustive supported test coverage; the legacy switch is equivalent.
.\tools\agent\verify.ps1 -Level FULL
.\tools\agent\verify.ps1 -FullTests

# Clean generated outputs, not downloaded dependencies or the user cache.
.\tools\agent\verify.ps1 -Level FULL -CleanBuild

# Original independent orchestration for equivalence/diagnosis.
.\tools\agent\verify.ps1 -Level FULL -IndependentBuilds
```

- `-Level` accepts only `DEV`, `PHASE`, `COMPOSED` and `FULL`; omitted means
  `COMPOSED`.
- DEV requires `-Module shared|desktop` and a nonempty `-TestFilter` array.
  The caller identifies adjacent regression coverage. No path heuristic may
  silently imply completeness. Module dependencies still compile as required.
- PHASE requires `-Phase` resolving to an explicit supported capability entry.
  Unknown identifiers fail; no branch-name or latest-phase guessing is allowed.
  The resolved verifier and regression perimeter are printed in the evidence.
- The current R6 example resolves to
  `tools/agent/verify-g9u0-r6-semantic-locus-point-interaction-support.ps1`.
  Its source owns required shared/Desktop cases, Checkstyle, semantic/scenario
  assertions, historical checks and default regression dependencies. Other
  supported identifiers must have equally explicit mappings.
- `-FullTests` selects FULL. Combining it with an explicitly different level
  fails; it must not silently override DEV or PHASE.
- `-IndependentBuilds` selects the existing independent COMPOSED/FULL execution
  path. It does not reduce coverage, bypass assertions or enable evidence reuse.
  Independent FULL (including standalone baseline `-FullTests`) clears only
  validated generated JUnit XML before each unfiltered module invocation and
  checks fresh task execution plus every resulting suite before output
  restoration. A zero Gradle exit cannot hide XML failures/errors under CI
  `ignoreFailures`; mandatory skipped/zero-test outcomes remain failures.
  Receipt-consuming baseline calls do not clear or re-inspect shared evidence.
- `-SkipBuild` remains static-only/incomplete verification. It is not a synonym
  for receipt consumption and cannot produce COMPOSED/FULL acceptance evidence.
  It is incompatible with FULL and with modes requiring real test execution.
- `-KeepBuildOutputs` explicitly retains generated outputs and permits the
  documented fast-loop lifecycle. It changes output retention, not test scope
  or freshness; omitted preserves the default restoration guarantee.
- Verification logs must remain outside repository `build`, `.gradle` and
  `.kotlin` directory components and the temporary generated-state backup tree.
  Reject linked log/repository ancestry before creation; the repository root is
  not a log directory. These rules also apply with Keep/Clean options so cleanup
  cannot delete the evidence used to report a successful result.
  When operational verification is invoked without `-LogDirectory`, allocate a
  fresh `TEMP/geocedg-operational/<GUID>` path for that invocation. In particular,
  benchmark precheck, warmup and each measured child must not share a fixed
  evidence path. Preserve explicit caller paths exactly; do not delete, overwrite
  or accept previous fixture reports to evade an existing-evidence rejection.
- `-CleanBuild` is valid only with canonical FULL, not `-IndependentBuilds` or
  `-SkipBuild`. Clear only validated repository-generated `build`, `.gradle`
  and `.kotlin` directories inside the outer restoration transaction, or with
  explicit `-KeepBuildOutputs`. Rebuild dependencies without task-output cache
  reuse; never empty the Gradle user home or downloaded dependency cache.
- Existing toolchain-download, packaging-artifact, interactive-launch,
  benchmark and historical-reproduction options keep their explicit meanings.
  Unsupported or contradictory combinations fail before work begins.

FULL does not silently enable historical reproduction modes that require a
different frozen checkout. Interactive smoke and packaging artifact checks
remain separate applicable gates; a report must state requested, performed,
deferred or unavailable evidence and its governing authority. A missing
required gate prevents a complete acceptance claim.

## 4. Canonical module execution

COMPOSED and FULL may consolidate duplicate test execution only after equivalence
with the independent path is demonstrated. Keep two sequential Gradle test
invocations: shared-JRE tests with shared build/style work, then Desktop tests
with Desktop build/style work. Do not combine cross-module `--tests` filters.

The accepted COMPOSED test selection is:

| Module | Test filters |
|---|---|
| shared | `org.geocedg.*`; `org.geogebra.common.kernel.commands.RedefineTest`; `org.geogebra.common.euclidian.DrawablesTest`; `org.geogebra.common.kernel.LocusV2InputPreviewLifecycleTest` |
| desktop | `org.geocedg.*` |

FULL runs the shared-JRE and Desktop test tasks without these filters. Build and
style task coverage remains the union demanded by applicable focused verifiers.
The executable plan must account for every current required test/check; unknown
or uncovered demands fail closed. Selection changes require the review in
section 8. Report runtime-discovered test counts; static annotation counts are
not execution evidence.

The same test is a duplicate execution only when its task/build root, source,
filters and effective JVM/system-property/environment context are equivalent.
Different execution contexts, distinct tests of one requirement and deliberate
defence-in-depth checks are not automatically deduplicated.

Each required Test task must execute freshly using the supported task-scoped
rerun mechanism. Verify actual outcomes; reject cached, up-to-date, skipped,
no-source or absent mandatory test executions. Do not remove upstream
`outputs.upToDateWhen { false }` or accept cached tests for this optimization.
Compilation/resources/Checkstyle may reuse outputs only under demonstrated
complete declared-input contracts, with actual task outcomes recorded.

Keep configuration cache disabled on this candidate canonical path. Introduce
no new task, test-fork or in-process JUnit parallelism. Any later change needs
separate compatibility, stress/determinism, performance and FULL evidence.
Record daemon and build-cache options explicitly; availability alone is not
evidence that a policy is safe or beneficial.

## 5. Current-run build evidence

One aggregate receipt may connect the two canonical module runs to the focused
verifiers in the same top-level invocation. It is a run-scoped capability, not
a persistent cache or security signature system. An explicit child
`-BuildEvidencePath` never establishes trust from JSON alone.

The shared module must enforce all of the following:

1. One active owner token, run nonce, PID/runspace, resolved module path and
   receipt path/hash. Opening another active run fails. Only the owning token
   closes the run, always in the top-level `finally` block.
   Bind the executing script module to its raw source bytes captured at import.
   A persistent PowerShell session must not use cached old functions to validate
   edited module bytes. Reject a changed loaded module before new work or receipt
   consumption; close-time rejection must still invalidate ownership. Start a
   fresh shell (or explicitly reload only when no run is active) after such edits.
2. Capture input identity before execution; compare after each module run,
   before sealing, and at every consumer. No concurrent builds/source writers
   are supported. A changed input invalidates the receipt.
3. Bind HEAD/index/status separately from raw on-disk input bytes. Initially
   hash the complete tracked and nonignored-untracked file inventory; do not
   discard tracked files because their path looks generated. Explicitly
   excluded ignored generated outputs remain governed by their task/lifecycle
   contracts. Git-normalized diffs or modification times alone are insufficient.
   Git-ignore status alone is not an input-safety decision: ignored consumed
   source/configuration/fixture files must be hashed or explicitly rejected.
4. Hash the full process environment without persisting plaintext values.
   Fingerprint effective external Gradle properties and init-script existence,
   directory membership and bytes. Unsupported arbitrary initialization or
   transitive external input dependencies require independent execution, not
   an unsupported hermeticity claim. The canonical wrapper layout is
   `distributionBase=GRADLE_USER_HOME` and `distributionPath=wrapper/dists`
   (including their defaults); custom layouts require `-IndependentBuilds`.
   Root, user-home and distribution `gradle.properties` participate in the
   external-input inventory, as do the shared and Desktop build-root files.
   Standalone/included task aliases require an explicit three-root property
   contract: only `org.gradle.daemon`, `org.gradle.parallel`,
   `org.gradle.caching` and `org.gradle.jvmargs` are supported in those three
   files. The first three accept canonical boolean values and are explicitly
   overridden by execution policy; JVM arguments must be present, identical
   across the roots, and free of unsupported external inputs. Unknown keys,
   including `systemProp.*`, require review or `-IndependentBuilds`, even if
   identical. Matching task names and stable hashes alone do not prove context
   equivalence. The inspected current included-build layout is not a proof for
   arbitrary future settings/plugin changes; contextual changes require renewed
   review or independent execution. Independent FULL only normalizes its own
   fresh task log and does not require this receipt-reuse eligibility contract.
   Canonical property inspection deliberately rejects
   escaped keys, logical-line continuations, and escapes in `org.gradle.jvmargs`
   or layout values instead of guessing Java-properties decoding. Ordinary
   unescaped heap flags remain supported; JVM agents, argument/flags files and
   home overrides in `org.gradle.jvmargs` require `-IndependentBuilds`.
5. Bind exact module/build root, task, filter and effective execution contexts,
   real compiler/test JVM and wrapper evidence, native exit codes and task
   outcomes. Both mandatory test executions must complete successfully before
   the receipt becomes active.
6. Retain live and archived JUnit/Checkstyle report inventories and hashes.
   Missing, stale, changed, wrong-context or partial evidence fails. Reject
   JUnit failures/errors independently of Gradle's exit status, including when
   upstream CI `ignoreFailures` permits Gradle exit zero.
7. Every focused consumer validates its demanded context and required reports,
   then executes its original live test-case/method/count/style, metric,
   canonical-summary and other assertion branches. Another verifier's PASS,
   report existence or aggregate count never substitutes for those assertions.
8. Bypass only duplicate Gradle launches and nested generated-output snapshots.
   Historical source/hash/approval checks and required numerical/reference work
   remain live. Java version/toolchain inspection remains live; do not intercept
   interactive or packaging execution as test evidence.
9. Capture native/child exit codes immediately, before fingerprinting or other
   commands overwrite `LASTEXITCODE`. Preserve original and cleanup failures.
   Evidence-reference logs identify the real canonical command; they must not
   claim a historical command was executed again.
10. Reject old/new-process, wrong-runspace, closed, mutated or tampered receipts.
    Import the shared module without `-Force` while a run is active. Execute
    module-reset/fixture tests before activation or in a separate process.

Measure fingerprint cost before narrowing the input closure. DEV and PHASE do
not consume canonical receipts and need not pay whole-tree receipt costs.

## 6. Generated-state and scientific invariants

Preserve ADR 0002's default generated-state transaction. One outer transaction
surrounds canonical execution and consumers. Archive evidence before restoration;
restore pre-existing generated contents on success or failure. Preserve recovery
data and report restoration failures. Validate exact generated-directory targets
inside the repository before removal/movement; never operate on a broad root.

Explicit `-KeepBuildOutputs` avoids an unnecessary snapshot copy and retains
current generated outputs. This opt-in does not waive source/index/status checks.
A clean-output FULL validation remains required for infrastructure changes
unless every condition of the narrow section 11 exception is established;
use `-Level FULL -CleanBuild` for the candidate's explicit clean-output path.
Clean generated outputs and an empty dependency/download cache are different
conditions and must be reported separately; this mode never requests the latter.

No optimization may weaken assertions, numerical tolerances, persistence or
serialization coverage, regenerate scientific references merely to pass,
convert failures to warnings, depend on test ordering, or hide slow tests from
FULL. Existing opt-in scientific benchmark bodies must be reported as requested
or not requested separately from JUnit method counts. Keep all tests unless an
explicit requirement/failure-mode equivalence review justifies removal.

## 7. Bootstrap and CI

Windows bootstrap owns supported workstation prerequisites and the ability to
run the canonical build/verification toolchain. It delegates product verification
through named levels; it must not carry a divergent hidden scientific gate.
The bootstrap audit must document its actual default verification level, stages,
toolchains, numerical/generated-source/packaging prerequisites, log paths and
exit propagation. Moving exhaustive checks to FULL requires a documented reason
and preserved coverage. It never permits a workstation incapable of canonical
verification to pass.

CI and workstation invocations use identical level meanings. The candidate CI
policy explicitly runs FULL, retaining existing benchmarks and evidence uploads.
No path-based or prose-based exemption is introduced. A timeout, missing runtime
or unexecuted required gate is incomplete/failed evidence, not a PASS. Any later
selective CI policy needs conservative, tested scope routing and FULL for
unknown/unbounded or infrastructure changes.

## 8. Mandatory impact reviews

Every task distinguishes DEV, PHASE, COMPOSED and FULL evidence and records the
required level/perimeter. Review changes to test selection, cache policy,
parallelization, verifier orchestration, acceptance classification, closeout
readiness/mode, promotion audit, bootstrap and numerical baselines as
verification-infrastructure changes. Such changes require FULL evidence in
addition to focused operational tests, except for the exhaustively proven
identity/provenance repair defined in section 11. An unrun/failed required FULL
is incomplete, not an implicit exemption.

Any repository advance changing workstation prerequisites, supported JDK or
toolchain versions, Gradle behaviour, build/verification entrypoints, numerical
references, generated sources, packaging prerequisites or other assumptions
consumed by Windows bootstrap requires an explicit bootstrap-impact review.
Record exactly one outcome:

```text
BOOTSTRAP IMPACT — UPDATED
Affected paths: <repository-relative paths>
Rationale and validation: <what changed and saved evidence>
```

or:

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: <which consumed assumptions were inspected and why they remain valid>
```

Require review, not arbitrary bootstrap edits. The task/closeout report also
records infrastructure-impact scope, required levels, exact FULL command,
exit/log/evidence and any unresolved gate. Preserve the existing
[`GUIDE_IMPACT` protocol](documentation-maintenance.md#5-documentation-impact).
Templates and review prompts reference this contract rather than copy it.
Automation may validate reliable structured facts; a declaration's presence or
keyword match does not establish substantive compliance.

## 9. Validation and reporting

Before implementation, complete comparable original DEV/PHASE/COMPOSED/FULL and
bootstrap characterization. After implementation, run focused operational
fixtures, representative DEV and PHASE, COMPOSED, FULL, workstation/bootstrap,
repeat/determinism and clean-output checks. Compare independent and consolidated
test/context coverage and each phase's canonical/assertion evidence. Preserve
distinct scientific/numerical checks and classify failures before correction.

Negative fixtures cover stale/missing/closed/wrong-process receipts, source and
raw-EOL changes, new/deleted init scripts, environment/context changes, missing,
failed or skipped tests despite exit zero, cached Test outcomes, report/receipt
tampering, wrong module/filter/task, conflicting flags, native-exit handling,
restoration failures and recovery retention. Operational default-log regressions
also cover successive unique implicit invocations, unchanged explicit binding
and refusal to overwrite existing evidence.

Report exact commands/workdirs, machine/toolchain/environment context, cold/warm
conditions, elapsed time, real task/test counts and outcomes, cache/fork/process
facts, log paths, failures, skips and incomparable cases. Give absolute and
percentage speed-up only for comparable completed before/after measurements.
No pending measurement is a PASS or an estimated improvement.

The observed candidate host is PowerShell 7.6.5. PowerShell 7.2 is the declared
minimum, not an executed compatibility result; 7.2 itself remains UNTESTED.
Record the actual runtime version with validation evidence and do not generalize
one host's result to every version at or above the floor.

This cross-cutting operational task is closed by the explicit
[author decision in ADR 0020](../../../docs/adr/0020-verification-levels-and-current-run-evidence.md#author-approval-and-closeout).
Technical execution alone still cannot confer author approval, product-phase
promotion, release permission or an approved phase tag.

## 10. Phase-candidate and author-closeout lifecycle

A phase verifier whose original entry contract names an uncommitted candidate
must distinguish three states instead of weakening that contract after the
candidate is committed:

1. `PRECOMMIT_CANDIDATE` retains the original HEAD, branch, candidate inventory
   and false-approval assertions. Its evidence belongs only to that raw input
   cohort.
2. `COMMITTED_CANDIDATE` names an exact implementation commit, proves its
   authorized base and complete candidate blob inventory, and permits only an
   exhaustively listed verification-infrastructure descendant. Branch names are
   not candidate identity. All scientific, source, scenario, JUnit, style and
   current-run receipt assertions remain live.
3. `AUTHOR_CLOSEOUT` requires a later explicit author decision naming the exact
   reviewed technical commit and an exact closeout target commit. It reconstructs
   every permitted status-only blob change from policy frozen in the reviewed
   commit, rejects productive, test, verifier, tolerance, reference or unknown
   changes, and proves unchanged non-allowlisted tracked paths, modes and blobs.
   A later clean checkout may use a different supported LF/CRLF materialization;
   that does not change the versioned repository authority.

Historical PHASE, COMPOSED and FULL roots may be linked to an author closeout
only through their exact hashes, ancestry, authenticated historical raw input
inventory and an exhaustive Git-tree closeout delta. This is documentary
evidence linkage, not a current-run receipt and not a new execution. The report
must distinguish the technical execution commit, closeout target commit and
verification-code cohort. Missing artifacts, changed non-allowlisted tracked
blobs/modes/paths, differing consumed-untracked inputs, invalid materialization
or an absent exact-SHA author decision fail closed.

The initial implementation of this contract is the G9S1-R1 lifecycle repair in
[ADR 0023](../../../docs/adr/0023-phase-verifier-lifecycle-and-author-closeout.md).
Its historical raw cross-checkout requirement is narrowly superseded by
[ADR 0024](../../../docs/adr/0024-verification-input-identity.md); prior execution
and failure records remain unchanged. Neither contract creates author approval.

An already published phase may subsequently run live regression through a
distinct `PUBLISHED_REGRESSION` context. Explicit committed authority identifies
the exact historical reviewed/closeout commits; their frozen policy, approved
delta, author record and ancestry are authenticated. The current HEAD must
descend from that closeout, but branch name is not authority and later product
inputs are not claimed identical to the historical reviewed cohort. Every
scientific/source/scenario/JUnit/Checkstyle assertion and same-run raw receipt
check remains live. Historical candidate paths describe original phase scope.
This context does not link archived execution, require a historical build bundle
for new execution, skip tests, or manufacture a current build receipt. It is
separate from the three candidate/author-decision lifecycle states above.

Commit-range whitespace evidence must declare Git's whitespace policy rather
than inherit workstation configuration. GeoCeDG uses Git's normal blank-at-EOL,
blank-at-EOF and space-before-tab checks with `cr-at-eol`, so physical CRLF/LF
representation alone is not a defect while actual trailing spaces remain an
error. This interpretation does not relax same-run raw-byte identity: an LF/CRLF
change during an active verification invocation invalidates its receipt. Across
checkouts, Git tracked identity and supported materialization, not equality to
an older physical raw inventory, are authoritative as defined below.

## 11. Verification input identities and evidence-preserving repair

This section is a new, explicit author authorization for a bounded operational
repair. It was not a general exemption implicit in ADR 0020 or ADR 0023.
The decision and exact scope are in [ADR 0024](../../../docs/adr/0024-verification-input-identity.md).

### 11.1 Three independent authorities

- **Repository identity:** tracked repository path, Git mode and blob OID from
  an exact commit tree. A deterministic digest may serialize only these facts.
  For reviewed technical commit `T` and closeout commit `C`, prove ancestry and
  the complete exact allowlisted `T..C` tree/content delta. No non-allowlisted
  tracked path, mode or blob may change.
- **Same-run identity:** preserve the existing physical raw SHA-256 inventory,
  separate HEAD/index/status, process/runspace ownership, loaded-module identity,
  environment/external configuration and report seals. Compare at start, after
  required module runs, before sealing, at consumers and completion. Git-clean
  status is never a substitute for this concurrent-mutation defence.
- **Materialization validity:** the target checkout's index tree must equal its
  explicitly expected commit tree and tracked working files must be Git-clean.
  Audit effective Git attributes and configuration before allowing Git's
  clean/smudge semantics. Support declared ordinary text/EOL and binary handling;
  reject unsupported effective external/custom filters, encodings, entry kinds
  or other conversion contracts. Do not normalize the repository or rewrite
  line endings. Report LF/CRLF physical differences without changing raw seals.

Configured but unused filter definitions are not applied transformations.
Effective `text`, `eol`, `working-tree-encoding`, `ident` and `filter` attributes,
attributes-file sources, and relevant `core.autocrlf`, `core.eol` and mode policy
must be inspected. Unsupported effective transformations fail before invoking
their external programs. Supported native transformations require fixture
evidence; unsupported native transformations also fail closed.

Partition inputs explicitly: tracked inputs use Git authority across checkouts;
consumed untracked inputs retain exact raw hashes and exhaustive membership;
ignored generated state retains its current explicit lifecycle/exclusion
contract; external build/JVM inputs retain their current fingerprint contract.
Historical raw inventories remain authenticated evidence of the physical inputs
actually executed. They are never rewritten or treated as newly executed.

Cross-checkout closeout invocation names exact `ReviewedTechnicalCommit` and
`CloseoutCommit`. A newer verification-code cohort may inspect those fixed
authorities through an independent clean target worktree; the verifier must not
substitute its own HEAD or branch for either target. Policy/content approval
rules are read from the reviewed technical authority, not silently broadened
by a newer working copy.

### 11.2 Narrow evidence-preserving verifier repair

`EVIDENCE_PRESERVING_VERIFIER_REPAIR = true` is permitted only when all fifteen
conditions below are proven for the final operational repair cohort:

1. No product Java change.
2. No Desktop product or UI change.
3. No scientific-test change.
4. No numerical reference or tolerance change.
5. No Gradle or build-script change.
6. No test task/filter selection change.
7. No required Java/toolchain change.
8. No numerical-command change.
9. No JUnit pass/fail acceptance-semantics change.
10. No generated-state lifecycle change affecting test execution.
11. Changed executable functions are limited to input identity, provenance and
    cross-checkout closeout validation; focused fixtures validate that scope.
12. Prior successful PHASE/COMPOSED/FULL evidence is sealed and names the exact
    scientific/product cohort to which the linkage applies.
13. The complete dedicated identity/lifecycle infrastructure suite passes twice
    with deterministic results, including tampering and materialization negatives.
14. Bounded real canonical shared-module and Desktop verification integrations
    pass, alongside parser/static checks, Git whitespace checks and the actual
    previously failing R1 closeout/materialization case.
15. An authenticated deterministic execution-plan/impact comparison establishes
    unchanged tasks, module roots, test filters, Checkstyle requirements,
    numerical/reference commands, JVM/toolchain requirements, relevant execution
    environment/system-property policy and result acceptance semantics.

A file-name allowlist alone is not proof of condition 15. Compare actual
declarations/functions or their exact Git content, include transitive execution
authorities, and reject unclassified changes. A caller-supplied success Boolean
cannot replace this proof. Sealed prior evidence remains documentary linkage,
not an active receipt for a new invocation.

When every condition holds, link the old heavy scientific evidence and new
focused infrastructure evidence explicitly without rerunning or claiming another
PHASE/COMPOSED/FULL campaign. Otherwise record
`EVIDENCE_PRESERVING_VERIFIER_REPAIR = false` and apply the normal required
levels, including FULL. Do not launch repeated heavy campaigns on intermediate
fixes; freeze the final executable cohort before the required fallback run.

This exception is never the ordinary candidate-to-closeout workflow. It cannot
waive FULL for a deliberate change to verification policy, orchestration,
acceptance-purpose classification, `CLOSEOUT_READINESS`, closeout modes or
promotion audit, and it cannot make a known post-FULL lifecycle repair an
acceptable plan. Such changes use focused evidence plus the normal required
levels, including a fresh FULL on their final executable cohort.

This exception changes neither coverage nor scientific success criteria and
does not authorize generic evidence reuse across product/test/build changes.
Bootstrap impact and GUIDE_IMPACT still require substantive review. Technical
success never supplies a product-phase author decision.

## 12. Commit-first acceptance and dual closeout

This section is the prospective implementation-candidate amendment governed by
[ADR 0025](../../../docs/adr/0025-commit-first-acceptance-and-dual-closeout.md).
It changes operational lifecycle only. Until the author approves an exact
implementation commit, it must be reported as `AUTHOR REVIEW READY`, not as an
accepted replacement for sections 1–11.

### 12.1 Final-acceptance order

A closable future phase uses this order:

```text
implementation
  -> DEV/focal
  -> clean technical commit T
  -> CLOSEOUT_READINESS(T; both routes validated, no mode selected)
  -> compare the real PHASE/COMPOSED/FULL execution plans
  -> one canonical clean FULL(T), carrying authenticated PHASE,
     COMPOSED and FULL coverage claims
  -> applicable author review/smoke
  -> explicit author approval of exact T
  -> selected closeout path
```

`T` is the immutable technical candidate, not a branch, `latest`, moving ref or
future tag. The acceptance-intended canonical FULL campaign must execute with
`HEAD=T` and a clean index/worktree under the input and materialization rules in
sections 5 and 11. A dirty/precommit PHASE/COMPOSED/FULL remains permitted for
explicit development or diagnosis, but can never become acceptance or closeout
evidence for a later commit. Equal bytes or tree provenance do not change the
execution cohort recorded by a historical result.

Verification levels are coverage/evidence obligations, not a mandate to repeat
their work in separate physical processes. Before heavy acceptance execution,
READINESS compares the real executable plans. A higher-level run may discharge
a lower-level obligation only when the same cohort and root execution
authenticates every lower-level assertion and artifact. For the canonical
schema-v2 route, one physical FULL run supplies three separable claims:

- `PHASE`: a physically executed, same-run nested phase verifier with its exact
  mapping, argv, exit, verifier blob and evidence hashes;
- `COMPOSED`: a normative subsumption proof that the unfiltered FULL selection
  and common gates contain the complete COMPOSED plan; this is not reported as
  an independent execution; and
- `FULL`: the physical root campaign and its complete canonical receipt.

Schema v2 is valid only when the compared FULL plan executes and authenticates
every required obligation, and therefore seals `complements=[]`. If comparison
finds an uncovered PHASE or COMPOSED obligation, READINESS fails before heavy
execution. Running the exact minimal complement first requires an approved
generic extension of the schema, producer and consumer; it may not be inferred
or attached ad hoc. Removing duplicate campaigns never removes tests,
Checkstyle, numerical/reference checks, phase assertions or acceptance criteria.

### 12.2 Machine-readable evidence classification

Every verification root records evidence use separately from its technical
outcome:

| Field | Required meaning |
|---|---|
| `evidenceUse` | Explicitly `DEVELOPMENT_DIAGNOSTIC` or `FINAL_ACCEPTANCE`; final acceptance is selected only by a validated readiness receipt, never inferred from level, cleanliness or branch. |
| `requestedEvidenceUse` | Preserves the caller's explicit intent (`FINAL_ACCEPTANCE` when a readiness receipt was supplied) even if preflight fails before heavy execution. |
| `repositoryCohort` | `DIRTY_PRECOMMIT` or `CLEAN_COMMIT`, evaluated and recorded for the actual execution. |
| `technicalResult` / existing `status` | PASS/failure result for the requested verification coverage; it does not imply consumability. |
| `closeoutConsumable` | Boolean independent of technical PASS; true only after the bound final-acceptance gate succeeds on exact `T`. |
| `reviewedCandidate` | Exact full commit SHA `T` for final acceptance; otherwise null. |
| `closeoutMode` | Null throughout the mode-neutral technical campaign. The explicit `VERIFIED` or `AUTHOR_OPERATED` value first appears in the post-approval decision/PREPARE/AUDIT chain. |
| `validatedCloseoutModes` / `acceptancePlanSha256` | Exact two-route set and mode-neutral plan identity inherited from readiness; neither permits an inferred mode. |
| `readinessReceiptPath` / `readinessReceiptSha256` | Exact receipt identity consumed for final acceptance; otherwise null. |
| `reason` | One deterministic reason code describing current consumability. |
| `heavyCampaignStarted` | Whether expensive acceptance execution began; false for a rejected preflight. |

A technically successful dirty or precommit run reports:

```json
{
  "evidenceUse": "DEVELOPMENT_DIAGNOSTIC",
  "repositoryCohort": "DIRTY_PRECOMMIT",
  "closeoutConsumable": false,
  "reviewedCandidate": null,
  "reason": "DIRTY_PRECOMMIT_COHORT"
}
```

A consumable acceptance root reports:

```json
{
  "evidenceUse": "FINAL_ACCEPTANCE",
  "repositoryCohort": "CLEAN_COMMIT",
  "closeoutConsumable": true,
  "reviewedCandidate": "<exact full T>",
  "closeoutMode": null,
  "validatedCloseoutModes": ["VERIFIED", "AUTHOR_OPERATED"],
  "acceptancePlanSha256": "<mode-neutral-plan-sha256>",
  "reason": "READINESS_BOUND_TECHNICAL_GATES_PASSED"
}
```

The closeout consumer must revalidate the one archived canonical FULL campaign,
its authenticated coverage-claim envelope and the evidence behind every claim.
This includes exact clean-`T` input/index/status identity, exact unfiltered
shared/Desktop selection, one fresh execution of each canonical test task, the
archived JUnit XML and case/counter agreement, the complete clean Checkstyle
authority set, successful native-run/JVM provenance, the nested phase result,
the COMPOSED plan-subsumption proof and an exact hashed audit inventory
containing every native log plus the input and external-configuration
inventories. A root or receipt with case-mismatched levels, narrowed filters,
string-typed booleans, missing files, inconsistent counters, invented physical
lower-level runs or empty synthetic evidence is not consumable.

The canonical reason vocabulary is `DIRTY_PRECOMMIT_COHORT`,
`NO_CLOSEOUT_READINESS_RECEIPT`, `CLOSEOUT_READINESS_FAILED`,
`TECHNICAL_CAMPAIGN_PENDING`, `TECHNICAL_CAMPAIGN_FAILED`,
`READINESS_BOUND_TECHNICAL_GATES_PASSED` and the fail-closed
`ACCEPTANCE_BINDING_INCOMPLETE`. A later commit, clean checkout,
policy edit or evidence link cannot mutate these fields in sealed historical
results.

`tools/agent/verify.ps1 -Level FULL -CleanBuild -CloseoutReadinessPath <receipt>`
explicitly selects `FINAL_ACCEPTANCE`. The verifier must validate the live
receipt and its pre-heavy plan comparison before starting work. This parameter
is accepted only for FULL, is incompatible with DEV/PHASE/COMPOSED,
`-SkipBuild` and `-IndependentBuilds`, and requires an explicit, previously
nonexistent `-LogDirectory`. The one acceptance root must be new and must carry
the authenticated PHASE/COMPOSED/FULL claim envelope; it never pretends that
the derived COMPOSED claim was a separate execution. Without the receipt, any
level is `DEVELOPMENT_DIAGNOSTIC`, even when the worktree happens to be clean. A
dirty checkout, invalid receipt, candidate/HEAD mismatch, uncovered plan delta
or incomplete binding rejects final acceptance with
`heavyCampaignStarted=false`.

### 12.3 `CLOSEOUT_READINESS`

The generic entry point is:

```powershell
.\tools\agent\phase-closeout.ps1 -Action READINESS `
  -TechnicalCommit <full-T> `
  -PolicyPath <repository-relative-policy> `
  -ReadinessReceiptPath <ignored-or-external-path>
```

`CLOSEOUT_READINESS` is a cheap mandatory gate before the acceptance-intended
canonical FULL campaign. It writes a `GEOCEDG_CLOSEOUT_READINESS` receipt and
must prove all of the following:

1. exact `T` resolves to a commit and tree, `HEAD` is exactly `T`, and the
   repository index/worktree are clean and supported; the sealed promotion
   anchor reaches `T` only through linear single-parent history;
2. future acceptance roots/receipts will be bound to that exact SHA and source
   cohort;
3. the phase has one complete declarative closeout policy and both exact
   `VERIFIED` and `AUTHOR_OPERATED` routes are constructible;
4. candidate, author-decision, tag and promotion authority contain no branch
   name, `latest`, moving ref or other inferred identity;
5. the expected status-only projection contains no impossible SHA/evidence
   self-reference;
6. the expected delta can be constructed and content/mode audited for either
   later explicit selection, and its future decision-record path is physically
   absent, nonignored and safely materializable as a regular `100644` blob;
7. the exact tag, target branch and remote authority required by the policy are
   defined without performing promotion; configured fetch/push URL identities
   are identical, single-authority and hash-sealed without persisting credential
   material; and
8. the actual PHASE, COMPOSED and FULL executable plans were compared; FULL
   contains all COMPOSED coverage, the applicable phase verifier has an exact
   live FULL integration mapping, and no obligation remains uncovered under the
   schema-v2 empty-complement contract; and
9. there is no known missing lifecycle or verifier change that would need to be
   repaired after FULL.

The readiness result is bound to the policy bytes, exact `T`, both validated
routes, a mode-neutral acceptance-plan hash and relevant repository/remote
configuration. It records `closeoutMode=null`; supplying `-CloseoutMode` to
READINESS is an error. A change to any bound input
invalidates it. `CLOSEOUT_READINESS = FAIL` means the acceptance FULL campaign
must not start; it is not an invitation to run first and repair provenance
later.

### 12.4 Declarative phase policy

The generic `tools/agent/closeout-workflow.ps1` authority supplies readiness,
closeout preparation, verified closeout validation and post-promotion audit;
`tools/agent/phase-closeout.ps1` is its operator entry point. A future closable
phase supplies data, not a copy of the helper logic. Policy schema v2 declares
at least:

- `phase` and exact technical/base ancestry constraints;
- `technicalEvidence.requiredLevels` exactly `PHASE`, `COMPOSED`, `FULL`, a
  `SINGLE_FULL_WITH_AUTHENTICATED_CLAIMS` strategy, physical level `FULL`, exact
  PHASE-nested/COMPOSED-subsumed/FULL-root claims, `complements=[]`,
  `fullRequiresCleanBuild=true` and the false `lifecycleRepairRequired`
  readiness invariant;
- `closeout.supportedModes` containing exactly `VERIFIED` and
  `AUTHOR_OPERATED`, `recordPath` and bounded `literalReplacements`; schema-v2
  requires `canonicalLfHashManifests=[]` so a status closeout cannot rewrite
  executable, tolerance or reference authority;
- author-decision record and evidence-role closure; and
- `promotion.branch`, `remote`, `tagName`, `tagMessage` and
  `closeoutCommitMessage`.

Unknown or missing fields, a post-approval selection containing zero or more
than one mode, an unprojectable delta, path-only approval, or dependency on
inferred branch/`latest` state fail closed.
Literal replacements may target Markdown documentation or
`geocedg/specs/operations/`, but never a product/scientific specification or a
Markdown file under validation authority; the only validation-tree creation is
the exact policy-declared JSON decision record.
Scientific and phase-specific assertions remain in their existing authorities;
they are not duplicated in lifecycle code or policy.

PREPARE uses bounded writes and atomic persistence for its result receipt. Once
its first bounded repository mutation begins, any failure preserves the current
index and worktree for inspection; automation must not perform a destructive
rollback across the index/worktree concurrency boundary. The failure must report
`UNKNOWN_REQUIRES_INSPECTION`, `partialMutationPossible=true`, the bounded paths
to inspect through the policy-declared status/decision scope, and no rollback
proof. It may not claim `mutationState=NONE` or exact clean `T`. A fresh PREPARE
is permitted only after the author/operator explicitly restores and
re-establishes exact clean `T`.

### 12.5 Exactly two closeout modes

`CLOSEOUT_MODE` is explicitly one of `VERIFIED` or `AUTHOR_OPERATED`. It is
selected only after technical evidence and author review, in the explicit
author approval consumed by PREPARE/AUDIT. READINESS validates both routes but
never selects or infers one. The selected mode is recorded in the author-facing
closeout plan. It
affects only the post-approval Git-operation boundary. It never changes the
single-FULL plan or its PHASE/COMPOSED/FULL coverage claims, tasks, filters,
numerical/reference work, tests, tolerances, acceptance semantics, author smoke
or exact-SHA approval rule.

#### `VERIFIED`

The existing ADR 0023/0024 guarantees remain intact:

```text
T
  -> explicit AUTHOR APPROVAL of T
  -> verified AUTHOR_CLOSEOUT
  -> status-only closeout commit C
  -> annotated phase tag
  -> fast-forward main
```

Exact ancestry, evidence linkage, complete delta/path/mode/content authority,
materialization validity, unchanged executable inputs, no-self-approval and
publication checks remain mandatory. Section 12 does not relax any existing
`AUTHOR_CLOSEOUT` assertion. For schema-v2 policies,
`phase-closeout.ps1 -Action PREPARE -CloseoutMode VERIFIED` is the generic
verified `AUTHOR_CLOSEOUT` preparation gate: it validates exact
approval/evidence and stages the bounded projected delta, but does not yet claim
the section-10 gate requiring exact `C`. `-Action FINALIZE -CloseoutMode
VERIFIED` then revalidates PREPARE and repository state, creates and validates
exact direct child `C`, creates the annotated tag, fast-forwards the protected
branch, performs one non-force atomic push containing only the literal branch
and tag refspecs, and automatically runs the same post-promotion audit. The push
must disable ref expansion and its pre-push hook; an effective remote mirror or
configured `push.pushOption` fails closed. The complete verified
`AUTHOR_CLOSEOUT` is established only by that validated `C` plus the passing audit.
`FINALIZE` is rejected for `AUTHOR_OPERATED`.

```powershell
.\tools\agent\phase-closeout.ps1 -Action FINALIZE `
  -CloseoutMode VERIFIED `
  -TechnicalCommit <full-T> `
  -PolicyPath <repository-relative-policy> `
  -ReadinessReceiptPath <receipt> `
  -TechnicalCampaignPath <FULL-verification-result> `
  -AuthorApprovalPath <explicit-author-decision.json> `
  -PreparationResultPath <verified-prepare-result.json> `
  -ResultPath <ignored-or-external-finalization-result.json>
```

#### `AUTHOR_OPERATED`

After an explicit author decision naming `T`, the author supplies this exact
decision shape. Tooling may persist and validate a decision already made in the
conversation, but may not originate it:

```json
{
  "schemaVersion": 1,
  "kind": "GEOCEDG_EXPLICIT_AUTHOR_APPROVAL",
  "phase": "<exact-policy-phase>",
  "reviewedTechnicalCommit": "<full-T>",
  "closeoutMode": "AUTHOR_OPERATED",
  "decision": "PASS_AUTHOR_APPROVED",
  "authority": "AUTHOR",
  "selfApproved": false
}
```

Preparation then uses:

```powershell
.\tools\agent\phase-closeout.ps1 -Action PREPARE `
  -CloseoutMode AUTHOR_OPERATED `
  -TechnicalCommit <full-T> `
  -PolicyPath <repository-relative-policy> `
  -ReadinessReceiptPath <receipt> `
  -TechnicalCampaignPath <FULL-verification-result> `
  -AuthorApprovalPath <explicit-author-decision.json> `
  -ResultPath <ignored-or-external-path>
```

Preparation validates approval and the single FULL campaign plus all three
authenticated coverage claims, constructs the exact expected
status/documentation delta and stages only that delta/decision record. It then
stops. Its output must display:

- exact approved technical SHA `T`;
- every expected closeout path, mode and bounded content transformation;
- the expected annotated tag and target;
- the declared promotion branch and remote; and
- the remaining human-owned commit `C`, tag, fast-forward and push operations.

Remaining operations are machine-readable data (`program`, an `arguments`
array, explicit placeholder inputs/outputs and expected parent), never an
interpolated shell command. Operator UIs may serialize that argv vector for
inspection but must not concatenate or execute it implicitly.

The human boundary contains exactly seven structured Git operations: create
`C`, capture its exact SHA, verify its direct parent, create the annotated tag,
switch to the promotion branch, fast-forward it and push branch plus tag. The
mandatory read-only post-promotion audit is emitted as a separate structured
action with its complete parameter binding; it is not a human Git operation.

Preparation in this mode must not commit, tag, merge, rebase, fast-forward,
push, force-push or otherwise mutate refs/remotes. The author performs those Git
operations. When executable inputs have not changed, no second PHASE/COMPOSED/
FULL run is required merely because the status-only commit and promotion were
human-operated.

The mandatory post-promotion audit is Git-read-only except for its ignored or
external result file:

```powershell
.\tools\agent\phase-closeout.ps1 -Action AUDIT `
  -CloseoutMode AUTHOR_OPERATED `
  -TechnicalCommit <full-T> `
  -CloseoutCommit <full-C> `
  -PolicyPath <repository-relative-policy> `
  -ReadinessReceiptPath <receipt> `
  -TechnicalCampaignPath <FULL-verification-result> `
  -AuthorApprovalPath <explicit-author-decision.json> `
  -ResultPath <ignored-or-external-path>
```

It verifies at least:

1. `T` is an ancestor of `C`;
2. `T..C` contains exactly the policy-authorized status/documentation delta and
   allowed content, paths and modes;
3. product, test, verifier, build inputs, tolerances, numerical references and
   every other executable input are unchanged;
4. the expected tag is annotated and peels to the policy-defined target;
5. the target branch advanced through the required linear fast-forward, with no
   merge commit, unexpected parent, rebase/substitution or force-push;
6. local `main`, `origin/main` and the queried live remote agree at `C`;
7. worktree and index are clean;
8. technical evidence still names `T`, never `C`; and
9. `selfApproved=false` is explicit and coherent with the author decision.

Remote-tracking refs alone are not a live-remote audit. The audit records the
remote URL/identity and queried exact refs without silently repairing them. Any
mismatch is a reported closeout failure; the tool must not mutate history, move
tags or reinterpret evidence to obtain PASS.

The repository-visible proof of "no force-push" is necessarily bounded: the
sealed readiness anchor must remain an ancestor of exact `T` and `C`, `C` must
be the direct single-parent child of `T`, and all current local/tracking/live
refs must equal `C`. A transient server-side force update that ultimately
restores the same object graph is not observable without an independent hosting
audit log; do not claim that stronger historical fact from Git objects alone.

### 12.6 Readiness vocabulary

`AUTHOR REVIEW READY` means only that the candidate is prepared for author
review. It does not assert clean-commit acceptance, closeout-consumable evidence,
a selected viable route or permission to promote.

`AUTHOR CLOSEOUT READY` means exact `T` was accepted from a clean committed
checkout, the single FULL campaign and its authenticated PHASE/COMPOSED/FULL
claims passed and are closeout-consumable for `T`, one post-approval route is
explicitly selected, and PREPARE validated that route against the mode-neutral
`CLOSEOUT_READINESS` plan. The current index/worktree then contains exclusively
the exact staged closeout delta; it is not misreported as clean. Under schema v2,
applicable review/smoke and the explicit exact-`T` author decision precede that
selection/PREPARE state. Readiness alone never means `AUTHOR APPROVED` and
never chooses a mode; automation may validate but not originate approval.

### 12.7 Compatibility and historical boundary

Existing ADR 0023/0024 closeouts and G9U1 artifacts retain their original
meaning. In particular, the G9U1 heavy executions remain attributed to their
recorded dirty precommit cohort, while the later lifecycle normalization and
published closeout retain their existing distinct SHAs. Do not edit old
receipts, change their classification, or claim that this prospective rule was
in force for them.

The focused contract suite covers dirty final-acceptance rejection, clean exact
`T` acceptance, missing/invalid route and self-reference readiness failures,
both mode-positive paths, and proof that preparation performs no final Git/ref/
remote operation. Post-promotion negatives include substituted SHA,
wrong/lightweight tag, merge or non-linear history, productive/executable
change, extra closeout delta, dirty state and local/tracking/live-remote
divergence. It also proves evidence remains attributed to `T`,
`selfApproved=false`, one physical FULL campaign supplies truthful separable
PHASE/COMPOSED/FULL claims, and the same frozen campaign/claim plan is consumed
by both modes.

This amendment authorizes no product phase. Its infrastructure implementation
requires focused positive/negative coverage and one normal clean FULL gate
under section 8, with authenticated lower-level claims; section 11.2 cannot be
used to avoid that deliberate methodology validation.

```text
PRODUCT_PHASE_EFFECT = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, supported runtime/toolchain, Gradle,
Conda, packaging, download or environment contract changes.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_developer_guide.md;
  docs/developer/geocedg_agent_prompt_guide.md;
  .github/prompts/canonical/verification.prompt.md;
  .github/prompts/canonical/governance.prompt.md;
  .github/prompts/reviews/change-review.prompt.md;
  .github/prompts/tasks/task-template.prompt.md
selfApproved = false
```
