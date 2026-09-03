# GeoCeDG verification levels and bootstrap-impact contract

- Status: **NORMATIVE / AUTHOR APPROVED**
- Scope: operational/build/verification infrastructure; no product semantics
- Decision: [accepted ADR 0020 and author closeout](../../../docs/adr/0020-verification-levels-and-current-run-evidence.md#author-approval-and-closeout)
- Existing authority: [ADR 0002](../../../docs/adr/0002-g1-operational-authority.md)
- Current evidence state: implementation and independent technical review complete;
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

The interface below is the accepted implementation. Commands are not evidence
of execution; implementation and measured results must be reported separately.
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
FULL run is technical evidence, not author approval or release permission.

FULL is required for phase closeout, release, major integration, build/toolchain
or verifier/bootstrap changes, shared kernel infrastructure changes, and changes
whose regression perimeter cannot be bounded reliably. The infrastructure-impact
rule in section 8 also applies.

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
A clean-output FULL validation remains required for infrastructure changes;
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
parallelization, verifier orchestration, bootstrap and numerical baselines as
verification-infrastructure changes. Such changes require FULL evidence in
addition to focused operational tests. An unrun/failed FULL is incomplete, not
an implicit exemption.

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
