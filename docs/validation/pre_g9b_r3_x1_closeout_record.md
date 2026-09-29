# PRE-G9B-R3-X1 closeout record

```text
PRE-G9B-R3-X1 = PASS — AUTHOR APPROVED
GOV-R         = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
newHeavyFinal             = NONE
sourceRunReinterpreted    = false
```

This record preserves two explicit author dispositions of 2026-09-29 and the exact
evidence identities behind them. It changes no product, test, build, verifier,
inventory, registry pin, schema, serialization, reconciliation mechanism or
scientific-contract file, and it does not modify the approved technical candidate,
the governance adoption commit or the reconciliation commit. Besides this record and
its machine-readable mirror, it only updates the roadmap status statements and the
status line of ADR 0030.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the candidate's artifacts, the GOV-R bootstrap record and the reconciliation record
record only frozen technical state and `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`;
this record is the sole authority for current author approval. Every statement in
those artifacts stands unchanged.

## Author dispositions

```text
GOV-R / POST_RUN_DETERMINISTIC_CATALOG_RECONCILIATION = PASS — AUTHOR APPROVED

APPROVED_GOVERNANCE_COMMIT = 8e4218dbfac89e9aca0e1f45c5635cb159b0ee1b   (G)
APPROVED_TREE              = ae197c6f52d9e56fc55bd25110e461a89ef9ba96
```

```text
PRE-G9B-R3-X1 = PASS — AUTHOR APPROVED

APPROVED_TECHNICAL_CANDIDATE = 8ab7dddc726c044b6e019e007bca5af938e834d1   (T)
APPROVED_TREE                = 7f75e3c669609be6369ddc0651e082370c96760f
ACCEPTED_RECONCILIATION      = a624e4ad21d1b073c1153c55ae01c46cb491728a   (R)
RECONCILIATION_TREE          = b951548c1b2eef00202ef9d8d4707efa09e4cbb8
AUTHOR_SMOKE                 = PASS (corrected smoke)
SOURCE_FINAL                 = verification-ecdb8e4845064b46af2ec1df49963418,
                               REJECTED_VERIFICATION_CORE / UNTRUSTED (historical, unchanged)
RECONCILIATION_RECEIPT       = 7a4c4825447a9966e82268ecd09e4f9aba4f293f36493705d2310b57c5454400
```

The GOV-R approval applies to the governance mechanism introduced by exact `G`. The
X1 approval applies to the exact pair `T` + `R`. Neither extends to any other
descendant except this status-only closeout.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published base | `78134aa8bc6ac30b65ffd916713fd3ca0502e6c6` | `f23218cc0b7c7189aae69489a1ce7a5ac5ee9fba` | `c595adc5…` |
| technical candidate `T` | `8ab7dddc726c044b6e019e007bca5af938e834d1` | `7f75e3c669609be6369ddc0651e082370c96760f` | base |
| GOV-R adoption `G` | `8e4218dbfac89e9aca0e1f45c5635cb159b0ee1b` | `ae197c6f52d9e56fc55bd25110e461a89ef9ba96` | `T` |
| reconciliation `R` | `a624e4ad21d1b073c1153c55ae01c46cb491728a` | `b951548c1b2eef00202ef9d8d4707efa09e4cbb8` | `G` |

Branch: `feature/pre-g9b-r3-x1-certified-expression-points`. The candidate's
artifacts are the [candidate report](pre_g9b_r3_x1_candidate_report.md), the
[validation matrix](pre_g9b_r3_x1_validation_matrix.md) and the
[design record](../architecture/pre_g9b_r3_x1_certified_expression_point_design.md);
the governance and reconciliation artifacts are
[ADR 0030](../adr/0030-post-run-deterministic-verification-catalog-reconciliation.md),
the [GOV-R bootstrap record](gov_r_post_run_catalog_reconciliation_bootstrap_report.md)
and the [reconciliation record](pre_g9b_r3_x1_catalog_reconciliation_record.md).

## Accepted content

- **X1 capability.** `certified-construction-program/v2` certifies the bounded
  expression-point translation `P+V`, `V+P`, `P-V` of a driver-dependent point and a
  constant finite `GeoVector` or two-number Cartesian literal. `C+(1,0)` and `C+u`
  certify; `Translate(C,u)` keeps its v1 version and signature; every other
  `AlgoDependentPoint` stays without program and rich-only. Selector, token, ledger,
  completeness claims, serialization and identity schema are unchanged.
- **Evaluator repairs.** The reconstructible evaluator refreshes a path driver's
  inhomogeneous coordinates after `pathChanged` (segment driver) and orders its
  reconstruction slice by construction index, then creation id (inline literals).
- **GOV-R.** The post-run deterministic catalog reconciliation of
  [verification levels §11.3](../../geocedg/specs/operations/verification-levels.md#113-post-run-deterministic-catalog-reconciliation):
  a rejected `FINAL` whose only defect is a stale derived JUnit selection inventory
  may be composed with a separate reconciliation receipt, without relabelling the
  source run and without a second heavy campaign. Every later use still requires an
  explicit author authorization naming the source run.

## Accepted evidence

The accepted technical evidence is the explicit pair below. Neither element is
presented alone as the other, and no `FINAL` receipt exists for `T` or `R`.

| Field | Value |
|---|---|
| source heavy execution | `verification-ecdb8e4845064b46af2ec1df49963418`, `REJECTED_VERIFICATION_CORE / UNTRUSTED`, never relabelled |
| source result | hash `7827ace82d27dd648db1a1817528e743315a56d750510ad7d1ca264aba0d4712`, file SHA-256 `4e3fbc1d7f9a098f90068a96216d8c304d81bd4257890a4229b6c7249c086734` |
| source plan | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |
| untrusted cause | `junit.shared.final.semantic`: stale tracked `final.shared` inventory (6814 expected, 6842 executed) |
| reconciliation receipt | `7a4c4825447a9966e82268ecd09e4f9aba4f293f36493705d2310b57c5454400`, `ACCEPTED`, `technicalAcceptanceEligible = true`, `heavyCampaignRerun = false`, `sourceRunReinterpreted = false`, reason `STALE_DERIVED_JUNIT_SELECTION_INVENTORY` |
| receipt file | `artifacts/agent/pre-g9b-r3-x1-reconciliation/validation-3-durable/catalog-reconciliation-receipt.json`, SHA-256 `180070262178e2846ed6c04a5165c976102dfbc281d5453846eeba8efba6fb8d` |
| reconciled catalog | `final.shared` 6814 → 6842, `605658b0…` → `558688f1f679ab6cf48d19c0e8e56d072fdc3423530e66633e8f662d72f44822`; registry pin `9fa207f0…` → `b5488fcd46c6ef4385949b1f6151354c5db6bc7ec61d190a6bb3bafa130b9ea7` |

JUnit evidence of the source run: `final.shared` 6 842 tests, 0 failures or errors,
10 allowlisted skips, including the 28 new X1 tests; `final.desktop` 1 527 tests,
0 failures, 1 skip; `g9u1-isolated` 5 and `revision3-diagnostic` 1, both without
failures; 17/17 producers completed with exit 0. Diagnostics, separate from
acceptance: eight `DIAGNOSTIC_CLEAR`; `diagnostic.governance` `DIAGNOSTIC_FINDING`
and `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`, the standing
pre-existing diagnostics.

**Route-B closeout inspection.** `tools/agent/phase-closeout.ps1 -Action INSPECT
-CandidateCommit 8ab7dddc… -ReceiptPath <receipt> -ReconciliationCommit a624e4ad…
-ApprovedCommit 8ab7dddc…` returned exit 0, `identity_confirmed = true`,
`mismatches = []`, `closeout_route = POST_RUN_CATALOG_RECONCILIATION`,
`reconciliation_revalidated = true` (the complete reconciliation was revalidated
read-only and reproduced the receipt identity), `source_run_reinterpreted = false`,
`verification_executed = false`, `repository_mutated = false`. The receipt, the
source archive and the preserved discovery evidence
(`artifacts/agent/pre-g9b-r3-x1-reconciliation/discovery-evidence/`, 799 files,
`MANIFEST.json`) are kept locally in the ignored evidence area. No further
verification is run for this status-only closeout.

**Author smoke.** The first interactive smoke raised two findings; neither is an X1
defect. The inline-literal observation was a consequence of the second finding
together with a smoke expectation that read `LocusLength` (a rich metric result) as
a number instead of `Length`; on `T` the inline-literal locus, its `LocusLength` and
its `Length` are correct and equal to the named-point control, while on the base the
locus is undefined. The Input finding is a pre-existing Desktop defect outside X1
(registered below). The corrected smoke passed.

## Debt disposition

| Identifier | State |
|---|---|
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | **RESOLVED** by the approved candidate |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | **RESOLVED_IN_X1** |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | **RESOLVED_IN_X1** |

`PRE-G9B-R3-X2 — evaluator correctness` is **NOT NEEDED**: its condition (an
evaluator debt not cleanly resolved inside X1) is not met.

## Retained debt and observations

| Identifier | Owner / state |
|---|---|
| `TD-DESKTOP-INPUT-REDEFINE-CANCEL-STRANDS-SUBMISSION` | **new, provisional**; pre-existing before X1 (`b49219408` + `a53c1c992`); layer Desktop Input / redefine-cancellation lifecycle: after a cancelled or unavailable spatial redefine the Input submission guard is never released and later input is ignored; owner: separate maintenance track `maintenance/desktop-input-redefine-cancel-recovery`, authorized for implementation and technical verification only; not repaired here |
| `OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES` | observation; `tools/agent/tests/phase-lifecycle.Tests.ps1` and `tools/agent/tests/generated-state.tests.ps1` became stale at `4117bdfd5` and are no registered node; owner: separate maintenance track `maintenance/retire-orphaned-historical-verification-suites`; not changed here |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | observation, retained |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observation, retained |
| `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | observation, retained |
| `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | observation, retained |

## Execution order

```text
PRE-G9B-R3-C1          PASS — AUTHOR APPROVED (closed)
        ↓
PRE-G9B-R3-C1-U1       PASS — AUTHOR APPROVED (closed)
        ↓
PRE-G9B-R3-X1          PASS — AUTHOR APPROVED (closed)
        ↓
PRE-G9B-R3-X2          NOT NEEDED
        ↓
PRE-G9B-R4             DESIGNED, NOT AUTHORIZED
```

The two maintenance tracks above are independent siblings based on the published
closeout, not phases of this order.

## Promotion

The author authorizes promotion of the complete linear history
`78134aa8… → T → G → R → this closeout commit` to `main`, by ordinary clean
non-force fast-forward only: no merge, rebase, squash, amend, force push, tag or
release. Promotion proceeds only if local `main`, `origin/main` and the live remote
`main` still equal the base below immediately before it.

Remote identity before promotion:

```text
local main       = 78134aa8bc6ac30b65ffd916713fd3ca0502e6c6
origin/main      = 78134aa8bc6ac30b65ffd916713fd3ca0502e6c6
live remote main = 78134aa8bc6ac30b65ffd916713fd3ca0502e6c6
tree             = f23218cc0b7c7189aae69489a1ce7a5ac5ee9fba
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch is retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion. `PRE-G9B-R3-X2`, `R4` and
every later `PRE-G9B-R` phase, `G9B`, `G9C`, `G9U2` and productive `G10` remain
unauthorized. The two maintenance tracks are authorized for implementation and
technical verification only; their candidates require separate author decisions.
No tag or release is authorized.
