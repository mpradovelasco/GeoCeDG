# Maintenance — generated-state historical verification residue: candidate report

```text
TRACK                        = maintenance/final-generated-state-historical-retirement
OBSERVATION                  = OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES (remainder)
CHANGE_ROUTE                 = ORDINARY
VERIFICATION_CLASS           = OPERATIONAL_VERIFICATION_INFRASTRUCTURE (frozen before implementation)
GLOBAL_INFRASTRUCTURE_CHANGE = true (one new active INFRA_UNIT/FINAL registry leaf)
PLANNED_ACCEPTANCE           = focused operational evidence + one normal FINAL
PRODUCT_PHASE_EFFECT         = NONE
SCIENTIFIC_CONTRACT_CHANGED  = false
PRODUCT_SEMANTICS_CHANGED    = false
TECHNICAL_CANDIDATE_STATE    = FROZEN
AUTHOR_DECISION              = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved                 = false
```

This track is independent of every phase. It is based on the published historical-suite
maintenance closeout `387084422106e66540a3e3400b8b65677087ecc2` (tree
`0fed799f4f704387dcfe96f8fcf3eb5d94d33991`). It reuses no evidence of `PRE-G9B-R3-X1`,
of the post-run catalog reconciliation or of the two earlier maintenance candidates.

## 1. Historical diagnosis (re-confirmed)

| Fact | Evidence |
|---|---|
| helper and suite unchanged since the last good commit | `repository-generated-state.ps1` blob `508e775d…` and `generated-state.tests.ps1` blob `9929e2b0…` are identical at `c976deca6`, `4117bdfd5` and the base |
| last good commit | `c976deca6`: 19/19 cases, 148 assertions, exit 0 (archived tree) |
| stale from `4117bdfd5` | 18/19 cases pass; only "cleanability rejects inaccessible generated state before costly gates" fails, at its final assertion "Cleanability preflight is not ordered before costly gates" |
| cause | that assertion searches `verify.ps1` for `Assert-RepositoryGeneratedStateCleanability` before `& $OperationalVerifier` and `& $WorkstationVerifier`; `4117bdfd5` made `verify.ps1` a registry/profile dispatcher, so both anchors vanished. It is a structural assertion about the superseded verifier, not about the helper |
| on the base | identical to `4117bdfd5` (18/19) |

The same commit unhooked the suite: the old infrastructure loop that ran it was
replaced by a delegation to `verify.ps1 -Profile INFRA_UNIT`, whose registered nodes
never included it.

## 2. Current coupling map

| Consumer | Relation | Status |
|---|---|---|
| registry, profiles, `verify.ps1`, verifier modules, checks | none use the helper or the suite | — |
| `tools/bootstrap/bootstrap-windows.ps1` | dot-sources the helper; calls `Assert-VerificationLogDirectoryOutsideGeneratedState` | live |
| phase verifiers `tools/agent/verify-*.ps1` (25 scripts incl. `verify-baseline.ps1`) | snapshot/restore through the helper | in tree; not registry-invoked |
| `Clear-RepositoryGeneratedOutputs`, `Assert-RepositoryGeneratedStateCleanability` | no caller besides the suite | helper functions |
| `tools/agent/tests/verification-runtime.Tests.ps1` | resolves `generated-state.tests.ps1` (param `GeneratedStateTestsPath`), lists it in the PowerShell-floor case and AST-extracts its fixture-summary refusal | the runtime suite is itself **unregistered** (only the historical `verify-input-identity-repair.ps1` runs it) |
| `tools/agent/verify-input-identity-repair.ps1:164` | invokes the suite | historical script, unchanged |
| historical reports (`bootstrap_workstation_report.md`, `verification_performance_report.md`) | describe the suite | historical evidence, unchanged |

**Author decision (2026-09-29).** `repository-generated-state.ps1` is live
infrastructure and must retain active test coverage; its still-valid safety contracts
migrate to a new focused registered `INFRA_UNIT` suite; `generated-state.tests.ps1` is
retired after migration; `verification-runtime.Tests.ps1` stays unregistered and only
loses its coupling to the retired file.

## 3. Per-case classification

| # | Old case | Classification | Disposition |
|---|---|---|---|
| 1 | verification logs reject repository and normalized generated-state components | UNIQUE LIVE CONTRACT (log guard) | migrated unchanged |
| 2 | verification logs allow normalized artifacts siblings and external paths without creation | UNIQUE LIVE CONTRACT | migrated unchanged |
| 3 | verification logs reject the snapshot backup tree and device aliases | UNIQUE LIVE CONTRACT | migrated unchanged |
| 4 | verification logs reject injected linked ancestry before creating missing descendants | UNIQUE LIVE CONTRACT | migrated unchanged |
| 5 | default exact restore removes newly generated directories | UNIQUE LIVE CONTRACT (snapshot/restore) | migrated unchanged |
| 6 | Keep snapshot fast path never enumerates or copies | UNIQUE LIVE CONTRACT | migrated unchanged |
| 7 | skipped snapshot cannot restore without Keep | UNIQUE LIVE CONTRACT | migrated unchanged |
| 8 | retention of a real snapshot keeps current outputs | UNIQUE LIVE CONTRACT | migrated unchanged |
| 9 | missing backup validates all entries before any removal | UNIQUE LIVE CONTRACT (no partial deletion) | migrated unchanged |
| 10 | restore validates all current targets before any mutation | UNIQUE LIVE CONTRACT | migrated unchanged |
| 11 | remove failure preserves backups and primary message | UNIQUE LIVE CONTRACT (recovery retention) | migrated unchanged |
| 12 | copy failure preserves recovery and supports explicit retry | UNIQUE LIVE CONTRACT | migrated unchanged |
| 13 | outside root repository root and unexpected names rejected | UNIQUE LIVE CONTRACT (ownership boundary) | migrated unchanged |
| 14 | linked target ancestor and repository root rejected | UNIQUE LIVE CONTRACT (reparse refusal) | migrated unchanged |
| 15 | snapshot removal rejects outside root nested and linked roots | UNIQUE LIVE CONTRACT | migrated unchanged |
| 16 | restore rejects outside backup and linked recovery ancestry before removal | UNIQUE LIVE CONTRACT | migrated unchanged |
| 17 | CleanBuild clears only enumerated allowed generated directories | UNIQUE LIVE CONTRACT (tracked/unselected preservation) | migrated; renamed to the helper term "clearing" |
| 18 | cleanability rejects inaccessible generated state before costly gates | helper part: UNIQUE LIVE CONTRACT; `verify.ps1` ordering assertion: OBSOLETE STRUCTURAL ASSERTION | helper assertions migrated; ordering assertion **dropped, not re-pinned**; renamed |
| 19 | CleanBuild validates the complete list before first deletion | UNIQUE LIVE CONTRACT | migrated; renamed |
| — | fixture harness: `-LogDirectory`, `generated-state-tests.json` summary and its "Refusing to overwrite existing fixture summary" guard | HISTORICAL FIXTURE-ONLY CONTRACT (the file's own result publication into the old shared evidence directory) | retired with the suite |

No case is currently covered by an active registered suite, so none is `DUPLICATED`.
The unregistered bootstrap test suite exercises the log guard only through bootstrap
integration.

Runtime-suite coupling: the PowerShell-floor entry for the retired file is no longer
meaningful (E); the fixture-summary refusal protected only that file's own output (C).
The current evidence writer publishes into fresh per-run directories and has no
equivalent contract to test instead. Neither check is migrated.

## 4. Live contracts that survive

In `tools/agent/tests/repository-generated-state.Tests.ps1`, over the unchanged helper:
verification-log placement (repository root, `build`/`.gradle`/`.kotlin` components,
backup tree, device aliases, linked ancestry, no creation); generated-directory target
validation (inside the repository, allowed leaf, no linked ancestry or root); snapshot
and exact restoration; retention; complete-list validation before any destructive
removal; recovery retention on remove/copy failure with explicit retry; snapshot-root
ownership; output clearing that preserves tracked and unselected data; the cleanability
preflight failing with identity diagnostics before mutation. One new case exercises the
real Git enumeration in a throwaway repository (only untracked or ignored allowed
roots, nested roots collapsed, tracked `build` content and other names never
selected) and the positive cleanability probe, which leaves no file behind; the old
suite only ever injected the enumeration.

## 5. Architecture

```text
live helper contract      -> active INFRA_UNIT leaf infra.repository-generated-state
historical fixture        -> retired; reproducible at c976deca6 and later history
runtime-suite coupling    -> removed; suite stays unregistered
```

No existing active suite owns the helper: the closest, `infra.bootstrap-separation`, is
a static analysis of the bootstrap command graph. The new suite follows the registered
suite conventions (no parameters, owned TEMP root with marker and checked cleanup,
summary line, exit status through `EXIT_CODE_V1`). The node is `WINDOWS`-only because
the helper's cleanability preflight uses the Windows identity API and its live consumer
is the Windows bootstrap. Its impact path is the helper.

## 6. Frozen verification class

```text
VERIFICATION_CLASS                 = OPERATIONAL_VERIFICATION_INFRASTRUCTURE
FROZEN                             = before implementation, after the author decision
GLOBAL_VERIFICATION_INFRASTRUCTURE = CHANGED (registry gains an active INFRA_UNIT/FINAL leaf; FINAL plan 67 -> 68 nodes)
REQUIRED_ACCEPTANCE                = focused operational evidence + one normal FINAL on the exact candidate
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: bootstrap-windows.ps1 and the helper it consumes are unchanged; no
workstation prerequisite, toolchain, Gradle, Conda, packaging or environment
contract changes; the new leaf only tests the guard bootstrap already uses.
GUIDE_IMPACT                       = NONE
GUIDE_JUSTIFICATION                = no guide enumerates INFRA_UNIT leaves; the INFRA_UNIT and FINAL entry points are unchanged
UPSTREAM_BOUNDARY_IMPACT           = NONE (tools/agent and geocedg/specs are GeoCeDG-owned)
```

## 7. Changes

| Path | Change |
|---|---|
| `tools/agent/tests/repository-generated-state.Tests.ps1` | added: 20 cases (19 migrated, 1 new real-Git enumeration case) |
| `geocedg/specs/operations/verification-registry.json` | one node added: `infra.repository-generated-state` (`ACCEPTANCE_LEAF`, `VERIFICATION_CORE`, `INFRA_UNIT`, `INFRA_UNIT` + `FINAL`) |
| `tools/agent/tests/generated-state.tests.ps1` | deleted (retired as historical) |
| `tools/agent/tests/verification-runtime.Tests.ps1` | `GeneratedStateTestsPath` parameter, its floor-list entry, its hash and the fixture-summary refusal block removed; case names unchanged (historical reports cite them) |
| this report and its [evidence mirror](../../geocedg/validation/maintenance-generated-state/maintenance-generated-state-candidate-evidence.json) | added |

`tools/agent/repository-generated-state.ps1` is unchanged: no helper defect was found.

## 8. Focused verification before freeze

| Check | Result |
|---|---|
| `repository-generated-state.Tests.ps1` | exit 0; 20 cases, 150 assertions; no fixture root or snapshot left in TEMP |
| mutation probes (scratch copies of the helper) | removing the clearing preflight, the `build` log-component rule or the ignored-directory enumeration each fails exactly the intended case (exit 1) |
| `verification-runtime.Tests.ps1` | 119/119 (same as on the base) |
| registry, final-coverage, closeout-compatibility, cli-compatibility, contract-boundary suites | all exit 0 |
| `FINAL -PlanOnly` | 68 nodes, `COMPLETE`, plan `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517`; versus the base (67, `defe8ab4…`) exactly one node added, no node removed or changed |

## 9. Historical evidence

No historical record, report, hash, policy or commit is edited. The retired suite passed
19/19 at `c976deca6` and became stale at `4117bdfd5` with the verifier redesign; it
remains reproducible from history. Its historical callers
(`verify-input-identity-repair.ps1`) and the historical reports that describe it are
unchanged.

## 10. Debt disposition (proposed)

```text
OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES = RESOLVED (candidate)
  phase-lifecycle.Tests.ps1      = RETIRED AS HISTORICAL (earlier approved maintenance)
  generated-state.tests.ps1      = RETIRED AS HISTORICAL (this candidate)
  repository-generated-state.ps1 = LIVE, covered by active INFRA_UNIT leaf
  verification-runtime.Tests.ps1 = no dependency on the retired file; remains unregistered
```

`PRE-G9B-R4` remains not authorized.
