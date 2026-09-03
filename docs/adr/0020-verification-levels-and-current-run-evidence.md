# ADR 0020: Verification levels and current-run evidence

- Status: **Accepted — PASS — AUTHOR APPROVED**
- Date: 2026-09-02
- Accepted: 2026-09-03; decision owner: GeoCeDG author
- Scope: operational/build/verification infrastructure only
- Entry: `3942af594e4507e479f2c75019cef62e3d9fea6f`, approved G9U0-R6
- Product/phase effect: none; G9U1 and later phase authorization remain unchanged
- Contract: [verification levels](../../geocedg/specs/operations/verification-levels.md)
- Implementation state: closed; reviewed implementation
  `2b82034dbedf6f26250ad4aefb9eead700e33e66`; see [author approval](#author-approval-and-closeout)

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
state restoration. This accepted decision refines execution reuse, not the
meaning of its existing validation guarantees.

## Decision

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

## Historical implementation checkpoint

The following evidence chronology is retained from reviewed implementation
`2b82034dbedf6f26250ad4aefb9eead700e33e66`; it describes a documentary checkpoint
written before final02 completed.
Its pending-gate wording describes that checkpoint, not the current closeout.
The completed review and subsequent author decision are recorded in
[Author approval and closeout](#author-approval-and-closeout).

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

### Historical documentary checkpoint and final execution record

The document version retained in reviewed implementation
`2b82034dbedf6f26250ad4aefb9eead700e33e66` was frozen before replacement
exact-source FULL attempt `final-full-ci-profile-02`.
Its original pending language records that checkpoint, not the current closeout:
final02 subsequently passed. Attempt `final-full-ci-profile-01` remains
FAILED at the delegated benchmark precheck; an intermediate receipt does not
replace its failed root. Later completion is recorded in the designated
[measurement](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/measurement-result.json),
[final root result](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/verification-result.json),
[delegated benchmark JSON](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/operational-benchmark.json)
and [final candidate closeout](../../artifacts/verification-performance-bootstrap/inventory/final-candidate-closeout.md),
as separate saved execution evidence. These status-only closeout edits do not
alter executable inputs or those records. A missing or failed record
does not satisfy the gate. These ignored local artifacts require the retained
artifact tree or a separately supplied bundle; they do not confer author approval.

## Author approval and closeout

On 2026-09-03 the GeoCeDG author explicitly granted **PASS — AUTHOR APPROVED**
for **GeoCeDG Verification Performance + Bootstrap / Workstation Verification
Audit and Governance Hardening**. This section is the normative approval record;
the specification, reports and developer guide reference it.

Approval is limited to reviewed implementation
`2b82034dbedf6f26250ad4aefb9eead700e33e66` on
`codex/verification-performance-bootstrap-governance`, based on
`3942af594e4507e479f2c75019cef62e3d9fea6f`, and its separate status-only closeout
commit. The independent review accepted all 61 paths without source corrections,
classified all six upstream-origin Java modifications as
`NECESSARY_MINIMAL_UPSTREAM_CHANGE`, and confirmed that no tests were removed or
merged and no scientific assertion, tolerance, reference or lifecycle contract
was weakened. DEV → PHASE → COMPOSED → FULL retains independent gate assurance.

The verification-performance architecture, bootstrap/workstation audit and
governance hardening are closed. The accepted evidence includes final02 FULL
(exit 0; 7,585 passing cases, 11 inherited omissions, 703 XML, zero failures/errors),
clean-output FULL, COMPOSED, native bootstrap03 exit 0, the benchmark campaign,
the 1,453-entry archived-evidence audit and the independent review's fresh DEV,
PHASE, shared-Java and Desktop checks. The [independent review](../../artifacts/verification-performance-bootstrap/closeout-review-20260903/closeout-review.md)
and [saved checks](../../artifacts/verification-performance-bootstrap/closeout-review-20260903/review-checks.json)
retain their pre-approval meaning; this author decision does not rewrite them.

At approval entry, all 11,152 source-file byte hashes matched final02's
[input inventory](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/canonical-build/9284974de4004c8d817ec6cf89e1a027/input-inventory.json)
(SHA-256 `390aadeb44f03bd75fb499a94f252888dc572ce0dc03be652855e6958478785d`).
This closeout changes status/documentation only, not executable inputs or the
technical verification contract. Earlier clean-FULL/bootstrap records keep
their reviewed, explicitly bounded source-cohort applicability; they are not
relabelled as whole-tree final02 runs. Heavy execution is reused, not repeated.
Bootstrap03's separate timestamp-link envelope remains exit 2; the successful
native result and archived reconciliation do not erase that instrument failure.

Accepted non-blocking residuals remain: operational benchmark median
214.277636 s versus the informational 5 s target; configuration-cache support,
additional parallelism and further optimization deferred. Remote CI, interactive
GUI and installer/packaging generation were not performed in the local campaign.
The declared PowerShell 7.2 minimum remains untested on that exact version.
Approval does not report any of these items as completed or targets as achieved.

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: approval/status edits only; reviewed JDK, Conda, wrapper, build,
verification, numerical-reference and packaging assumptions remain unchanged.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_developer_guide.md
```

The author authorized a separate approval commit and ordinary fast-forward
promotion to main, preserving the reviewed implementation ancestry. This is a
cross-cutting operational closeout, not a new geometric/product phase. No phase
tag is required or created for it; G9U1 and all later phase authorizations,
scientific baselines and release/licensing permissions remain unchanged.
