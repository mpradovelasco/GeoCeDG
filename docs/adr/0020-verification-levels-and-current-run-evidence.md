# ADR 0020: Verification levels and current-run evidence

- Status: **Proposed / IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW**
- Date: 2026-09-02
- Scope: operational/build/verification infrastructure only
- Entry: `3942af594e4507e479f2c75019cef62e3d9fea6f`, approved G9U0-R6
- Product/phase effect: none; G9U1 and later phase authorization remain unchanged
- Contract: [verification levels](../../geocedg/specs/operations/verification-levels.md)
- Implementation state: applied candidate; completed and outstanding validation
  are distinguished in the performance and bootstrap audit reports

## Context

The pre-change top-level verifier composed focused phase authorities. Those scripts
independently launched Gradle, executed overlapping tests, and preserved generated
state. The operational inventory identified repeated execution and snapshot work;
original measurements and applied-candidate results are recorded separately.

The user requires measurement before optimization, a four-level verification
model, a current Windows bootstrap audit and durable bootstrap/infrastructure
impact review. No scientific, geometric, persistence, compatibility or governance
gate may be weakened for speed. Original COMPOSED/bootstrap and FULL characterization
completed before source application. Original FULL failed; its failed outcome and
incomplete Desktop scope remain preserved, not relabeled as a successful baseline.

[ADR 0002](0002-g1-operational-authority.md) remains accepted authority for
composition, current-checkout applicability, CI/local consistency and generated
state restoration. This proposed decision refines execution reuse, not the
meaning of its existing validation guarantees.

## Proposed decision

1. Keep `tools/agent/verify.ps1` as executable authority. Default to COMPOSED;
   expose explicit DEV, PHASE and FULL, preserve the legacy `-FullTests` FULL
   alias and reject contradictory combinations. DEV needs explicit module/test
   scope and is never acceptance evidence. PHASE invokes a named existing
   capability authority with its complete documented perimeter.
2. Retain the independent COMPOSED/FULL execution path through
   `-IndependentBuilds`. Promote consolidated execution only after comparable
   measurements and test/context/assertion equivalence establish its safety.
3. Use two sequential canonical Gradle test invocations: shared-JRE, then
   Desktop. Preserve the developer guide's module-separated filter rule.
   COMPOSED covers the applicable phase test union; FULL adds unfiltered suites
   without replacing phase-specific assertions or numerical/reference work.
4. Force Test tasks to execute freshly. Preserve upstream forced-execution
   semantics; permit compilation/resource/style reuse only where demonstrated
   complete declared inputs support it. Keep configuration cache disabled and
   introduce no new task/fork/JUnit parallelism in this candidate.
5. Establish one process/runspace-local current-run capability after successful
   canonical execution. Bind pre/post raw input inventory, source/index state,
   environment/external Gradle configuration, exact tasks/filters/contexts and
   native/toolchain/report evidence. JSON alone cannot confer authority.
   Standalone/included task aliases also require the specification's conservative
   three-root property contract; stable bytes and matching names are insufficient.
   Contextual settings/build-logic changes need renewed review or independent
   execution, which remains available without receipt-reuse eligibility.
6. Focused consumers validate their own required evidence and retain their
   original live assertions, canonical summaries, historical checks and failure
   handling. Bypass duplicate launches/snapshots only. `-SkipBuild` stays static
   and incomplete; it is not evidence reuse.
7. Preserve default generated-state restoration with one outer transaction.
   Explicit `-KeepBuildOutputs` retains outputs without the unnecessary initial
   copy. Save diagnostic evidence before restoration and retain recovery data
   if restoration fails. Restrict `-CleanBuild` to canonical FULL: clear validated
   repository-generated outputs inside this transaction (or with explicit
   retention), rebuild without task-output cache reuse, and never clear the
   user/dependency download cache.
8. Keep bootstrap's workstation role explicit and delegate product coverage to
   the same named authority. Require substantive bootstrap-impact review when
   consumed assumptions change, infrastructure-impact review and FULL validation
   for verification-infrastructure changes, and the existing `GUIDE_IMPACT`.
9. Make CI explicitly run FULL with existing benchmarks and evidence upload.
   Add no hidden exclusions or exemption classifier. Local and CI level meanings
   remain identical. Each implicit operational invocation must allocate its own
   TEMP/GUID evidence directory; explicitly supplied paths remain unchanged and
   existing-evidence collisions fail closed, never authorize stale reuse.
10. Require PowerShell 7.2 or later at the top-level authority, shared runtime
    module and isolated operational fixture entrypoints. Focused consumers
    inherit the floor through module import. PowerShell 7.0/7.1 can promote
    redirected native stderr under `ErrorActionPreference=Stop` before exit-code
    capture; [Microsoft documents the changed behaviour from 7.2](https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_preference_variables?view=powershell-7.5#erroractionpreference).
    Narrow the declared platform contract instead of rewriting all inherited
    native captures or weakening failure handling. Historical source and approval
    locks remain unchanged. The separate necessary Java compatibility corrections
    are documented in their [bounded design](../architecture/verification-baseline-compatibility-repair.md);
    they are not product changes made for test speed.

The linked specification owns interface, evidence and review details. Prompts and
guides carry concise instructions and references rather than second copies.

## Alternatives and disposition

| Alternative | Disposition and reason |
|---|---|
| Keep every independent execution as the only mode | Retained for diagnosis/equivalence; repeated work remains a measured optimization candidate. |
| One combined shared/Desktop filtered Gradle test invocation | Rejected; existing R2 operational guidance requires separate filters to prevent cross-module class selection. |
| Replace phase assertions with another gate's PASS or aggregate test count | Rejected; cannot prove each phase's required evidence. |
| Reuse receipts from previous runs/processes | Rejected; inputs and environment can change, including raw-byte fixture changes hidden by Git normalization. |
| Cache or mark authoritative Test tasks up to date | Rejected for this candidate; tests read external fixtures/environment not fully declared as Gradle task inputs. |
| Enable configuration cache globally | Deferred/rejected for the candidate path; current Desktop provenance configuration launches external Git processes. Exact compatibility evidence is required before reconsideration. |
| Increase forks or enable in-process parallel classes | Deferred; global state/fixed-path hazards require bounded isolation and repeated stress evidence first. |
| Remove similar or slow regression tests | Rejected without requirement/failure-mode equivalence; no removal or assertion weakening is proposed. |
| Require a bootstrap script edit for every task | Rejected; explicit review and justified updates/no-change rationale are the contract. |
| Treat a PR keyword as sufficient impact compliance | Rejected; machine-checkable structure does not replace substantive review. |

## Evidence and remaining candidate-validation gates

The [performance report](../validation/verification_performance_report.md) and
[bootstrap audit](../validation/bootstrap_workstation_report.md) distinguish
original characterization, applied-candidate results, failed attempts and unresolved
gates. DEV first/repeat, R6 PHASE, COMPOSED01 and the focused shared/Desktop repairs
have completed; FULL01 failed on the then-unrepaired Desktop resource path. FULL02
subsequently completed technical FULL gates for the repaired 60-path checkpoint,
not for later proposed documentary edits. Original union first/repeat, original-
phase-to-COMPOSED01 and the bounded original-failed-shared-to-FULL02 saved-case
relations also completed. The archived DEV49 first/repeat relation now confirms
identical raw case/outcome multiplicities and recorded context, with fresh Test
execution in each run. Together with the original-source reuse controls, this
supplies bounded repetition evidence; it is not universal numerical determinism.
Clean-output FULL subsequently passed on the same 60-source checkpoint, and its
saved coverage/context plus six canonical summaries match FULL02 under explicitly
different clean/reuse policies. The first real candidate bootstrap then failed in
its new Conda preflight: a valid registered external-prefix environment was
incorrectly required to expose a short display name. The bounded origin-preserving
correction is applied and its 150-assertion workstation focus passed. The next
real attempt accepted Conda but exposed blank-line parameter binding in the native
output parser. That bounded correction and its 158-assertion focus also passed;
both failed real runs remain failed. Bootstrap03 then completed its actual normal
SkipFetch path and nested COMPOSED, native/measurement/root exit 0, with 1,096
fresh test cases and no failures/skips. Its separate r3 linked envelope remains
failed with exit 2: an instrument compared local and UTC DateTime ticks rather
than UTC instants. The saved chronology is valid, but the envelope is not
rewritten or represented as a successful original link. The artifact-only
reconciliation subsequently passed, preserving the original phase union with
16 explicitly named additions and no differences. Its nested-result digest is
an explicitly post-run review pin, not a historical producer seal.
Final CI-profile attempt01 subsequently failed at the benchmark precheck with
native/measurement/root exit 1; its generated-state fixture correctly refused an
existing result in the fixed default operational TEMP path. No warmup, measured
samples or benchmark JSON were produced. The failed root and archived TEMP data
remain preserved; a canonical receipt is not completed FULL evidence. The
[bounded default-path correction design](../../artifacts/verification-performance-bootstrap/inventory/final01-benchmark-evidence-collision-design.md)
changes only the operational default to an invocation-unique GUID and adds one
focused regression case; explicit paths and no-overwrite checks stay fail-closed.
The applied focus passed 111 runtime cases and 18 generated-state cases/143
assertions, fake-first operational evidence only. Replacement final02 with
the existing operational benchmark remains outstanding at this documentary
checkpoint. The 61-path membership remains; relative to bootstrap03, the next
checkpoint changes five documents plus the operational entrypoint/runtime fixture,
not only the five documents. No extra bootstrap repetition is required solely
to repair an artifact-layer timestamp defect or the default-only operational log
change: normal bootstrap supplies an explicit canonical log tree and its earlier
source cohort remains identified. No safe new parallelism,
configuration-cache compatibility or author approval is inferred. Report only
context-qualified measured deltas.

Required replacement evidence includes focused operational negative tests,
representative DEV and PHASE, COMPOSED, FULL, bootstrap/workstation, deterministic
repetition and clean-output execution. Preserve each phase's required cases,
non-test checks and canonical summaries. The final local CI-profile command uses
FULL with RunBenchmarks and default output restoration, not KeepBuildOutputs.
Its delegated benchmark can supply the successful benchmark integration evidence
without a redundant standalone successful benchmark run; this is not a remote
workflow execution or a matched timing pair with retained-output FULL02.
Report legacy scientific benchmark opt-ins separately from JUnit counts; do not
hide not-requested bodies.

## Consequences and retained risks

- Normal local development can select a narrow explicit scope without claiming
  global acceptance; closeout and infrastructure changes still require FULL.
- Consolidation adds a bounded same-run protocol. Raw inventory hashing has a
  measurable cost; narrower closures require evidence rather than mtime trust.
- The capability is not a security boundary or hermetic execution proof.
  Unsupported external initialization and concurrent source/build mutation are
  rejected or routed to independent execution with explicit limitations.
- Existing metadata/toolchain calls, numerical references, phase assertions and
  historical provenance remain live even when test execution is consolidated.
- FULL can remain expensive. CI timing limits must be assessed from completed
  measurements rather than lowering coverage to fit an arbitrary timeout.
- The observed candidate host is PowerShell 7.6.5. The declared 7.2 minimum is a
  native-stderr semantic prerequisite, not a completed compatibility run on 7.2;
  that exact version remains UNTESTED and must not be reported as validated.
- Accepted ADRs, sealed phase prompts/hash manifests and scientific baselines are
  not rewritten. No product Java change is justified solely by this proposal.

### Documentary checkpoint and final execution record

This source is frozen before replacement exact-source FULL attempt
`final-full-ci-profile-02`; its outcome was not yet executed at this documentary
checkpoint and no exit 0 is predicted. Attempt `final-full-ci-profile-01` remains
FAILED at the delegated benchmark precheck; an intermediate receipt does not
replace its failed root. Later completion is recorded in the designated
[measurement](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/measurement-result.json),
[final root result](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/verification-result.json),
[delegated benchmark JSON](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/operational-benchmark.json)
and [final candidate closeout](../../artifacts/verification-performance-bootstrap/inventory/final-candidate-closeout.md),
not by rewriting the tested source after execution. A missing or failed record
does not satisfy the gate. These ignored local artifacts require the retained
artifact tree or a separately supplied bundle; they do not confer author approval.

## Approval boundary

Implementation authorization for this operational task is not author acceptance
of its result. The required closeout is
`IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW`. The author must review coverage,
equivalence, measurements, bootstrap behaviour and governance before approval.
No approved phase tag, roadmap advancement or release permission is created.
