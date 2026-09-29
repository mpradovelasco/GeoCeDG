# PRE-G9B-R3-X1 — post-run catalog reconciliation record

```text
PHASE                     = PRE-G9B-R3-X1
RECORD_KIND               = POST_RUN_CATALOG_RECONCILIATION_RECORD
TECHNICAL_CANDIDATE_STATE = RECONCILED / FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
HEAVY_CAMPAIGN_RERUN      = false
NEW FINAL RUN             = NONE
```

This record applies the post-run deterministic catalog reconciliation of
[verification levels §11.3](../../geocedg/specs/operations/verification-levels.md#113-post-run-deterministic-catalog-reconciliation)
and [ADR 0030](../adr/0030-post-run-deterministic-verification-catalog-reconciliation.md)
to the X1 campaign, under the same author instruction that authorized the
mechanism's bootstrap. It is separate from, and does not amend, the frozen
[candidate report](pre_g9b_r3_x1_candidate_report.md), whose statements remain
historically true. It records no author approval.

## 1. Identities

| Role | Commit | Tree |
|---|---|---|
| published base | `78134aa8bc6ac30b65ffd916713fd3ca0502e6c6` | `f23218cc0b7c7189aae69489a1ce7a5ac5ee9fba` |
| technical candidate `T` (product and scientific payload) | `8ab7dddc726c044b6e019e007bca5af938e834d1` | `7f75e3c669609be6369ddc0651e082370c96760f` |
| mechanism adoption `G` (direct child of `T`) | `8e4218dbfac89e9aca0e1f45c5635cb159b0ee1b` | `ae197c6f52d9e56fc55bd25110e461a89ef9ba96` |
| reconciliation descendant `R` (direct child of `G`) | the commit that contains this record | reported outside this artifact |

A commit cannot name itself, so `R`, its tree and the reconciliation receipt that
binds `R` are reported outside this artifact, as the source `FINAL` was for `T`.

## 2. Historical source run

```text
FINAL          = verification-ecdb8e4845064b46af2ec1df49963418
STATUS         = REJECTED_VERIFICATION_CORE / UNTRUSTED   (unchanged, never relabelled)
result file    = artifacts/agent/pre-g9b-r3-x1-final/verification-result.json (ignored, 427080 bytes)
result SHA-256 = 4e3fbc1d7f9a098f90068a96216d8c304d81bd4257890a4229b6c7249c086734
result_hash    = 7827ace82d27dd648db1a1817528e743315a56d750510ad7d1ca264aba0d4712
execution plan = defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0
```

**Exact untrusted cause.** The only non-satisfied result is
`junit.shared.final.semantic` (`SEMANTIC`, `MIXED`, `EVIDENCE_UNTRUSTED` /
`UNTRUSTED`) with the cause *"Executed JUnit identities differ from the tracked
selection inventory."* The candidate added tests; `T`'s inventory refreshed
`discovery.shared` from dry-run discovery but kept `final.shared` at the base
cohort, because the updater ran without executed selection evidence.

**Executed product and scientific success.** Derived independently from the
archive (a Python re-read of every JUnit XML, separate from the PowerShell
verifier and reconciler, which agree):

| Item | Value |
|---|---|
| run state, profile | `COMPLETED`, `FINAL`, candidate `T` and tree `7f75e3c6…` |
| process producers | 17 of 17 `COMPLETED`, exit 0; every log, structured output and JUnit XML matches its recorded SHA-256 |
| required coverage | 40 required, 39 complete, 1 untrusted, 0 not run, 0 violated |
| acceptance by class | `SEMANTIC` 14 satisfied + the 1 untrusted; `SAFETY` 4 satisfied; `VERIFICATION_CORE` 21 satisfied |
| `final.shared` | 686 JUnit files, 6 842 identities, 0 failed or errored, 10 skipped (allowlisted), identity hash `558688f1f679ab6cf48d19c0e8e56d072fdc3423530e66633e8f662d72f44822` |
| new X1 tests inside `final.shared` | 28 of 28 `PASSED` (9 + 3 + 9 + 4 + 3 across the five X1 classes) |
| `final.desktop` | 102 files, 1 527 identities, 0 failed, `cc942936…` (equals `T`'s catalog) |
| `final.desktop.g9u1-isolated` | 2 files, 5 identities, 0 failed, `b9600884…` (equals `T`'s catalog) |
| `final.desktop.revision3-diagnostic` | 1 identity, diagnostic `CLEAR` |
| Desktop total | 1 527 + 5 + 1 = 1 533 tests, 0 failures |
| diagnostics | Checkstyle ×4, documentation, style, whitespace and revision-3 `CLEAR`; governance `FINDING` and historical consistency `UNAVAILABLE` are the standing pre-existing diagnostics |

These are the archived results of the single X1 `FINAL`; this record reran none
of them.

## 3. Eligibility and catalog derivation

A read-only `ANALYZE` on a clean checkout of `G`
(`tools/agent/reconcile-verification-catalog.ps1 -Action ANALYZE`) returned
`ELIGIBLE`, reason `STALE_DERIVED_JUNIT_SELECTION_INVENTORY`, untrusted check
`junit.shared.final.semantic`, and left the repository unchanged.

The inventory of `R` was then produced only by the official updater:

```powershell
update-verification-junit-inventory.ps1 -InventoryPath geocedg/specs/operations/verification-junit-inventory.json `
  -DiscoveryEvidencePath <discovery.shared dry-run evidence>, <discovery.desktop dry-run evidence> `
  -SelectionEvidencePath <archive>/checks/junit.shared.final.producer/junit-shared-final-producer.json, `
                         <archive>/checks/junit.desktop.final.producer/junit-desktop-final-producer.json, `
                         <archive>/checks/junit.desktop.g9u1-isolated.producer/junit-desktop-g9u1-isolated-producer.json
```

The discovery evidence files are the ones pinned by `T`'s inventory
(`discovery_evidence_sha256` `4536e3bb…` and `f100278e…`). The resulting delta is
exactly two values of the `final.shared` selection:

| Field | `T` | `R` |
|---|---|---|
| `final.shared.expected_identity_count` | 6814 | 6842 |
| `final.shared.expected_identities_sha256` | `605658b0d154fb33b9744aea1b1beacd9dc1e69cb1b59b570fd9e218b8dbfa43` | `558688f1f679ab6cf48d19c0e8e56d072fdc3423530e66633e8f662d72f44822` |
| inventory canonical-text SHA-256 | `9fa207f0011e079cadf8bc5e4df9a3c4e4a5a378b8c18a8ca955b8d3b804b74e` | `b5488fcd46c6ef4385949b1f6151354c5db6bc7ec61d190a6bb3bafa130b9ea7` |
| registry `junit_inventory` pin | `9fa207f0…` | `b5488fcd…` |

Every other selection, module record and field is unchanged. The reconciled set
equals the executed set canonically, the pin is the canonical-text SHA-256
(CRLF→LF, UTF-8) recomputed independently, and the registry passes its catalog
identity check. No count or hash was entered by hand. The source archive was
byte-identical before and after.

## 4. Execution-authority comparison (`T..R`)

| Authority | Result |
|---|---|
| product and kernel Java, Desktop/UI | unchanged |
| scientific and product JUnit sources | unchanged |
| Gradle and build declarations | unchanged |
| heavy tasks, selections, filters and modules | unchanged (only two expected values of `final.shared`) |
| tolerances and numerical references | unchanged |
| toolchain, JVM and environment contract | unchanged |
| registry nodes, producers, projections, `verify.ps1`, supervisor | unchanged; registry differs only in the inventory pin |
| `FINAL` execution plan | `defe8ab4…` resolved from `R`, equal to the source plan |
| acceptance semantics (verifier, JUnit projection, receipt modules and schemas) | Git-identical |

`T..G` adds the mechanism and its governance documentation; `G..R` changes only
the inventory, the registry pin, this record, its JSON mirror and roadmap text.

## 5. Reconciliation receipt

The `GEOCEDG_POST_RUN_CATALOG_RECONCILIATION` receipt is generated only after `R`
exists, on a clean checkout of `R`, outside tracked Git, and is validated twice
for an identical identity. It binds `T`, `G`, `R`, the source run and result, and
records `reconciliationStatus = ACCEPTED`, `technicalAcceptanceEligible = true`,
`heavyCampaignRerun = false` and `sourceRunReinterpreted = false` only if every
condition holds. Its path and identity are reported outside this artifact. The
accepted technical evidence is the pair:

```text
SOURCE_HEAVY_EXECUTION          = verification-ecdb8e4845064b46af2ec1df49963418
POST_RUN_RECONCILIATION_RECEIPT = <reported outside this artifact>
```

Neither is ever presented alone as the other; no `FINAL` receipt exists for `T` or
`R`.

## 6. X1 technical meaning (unchanged)

- **Capability.** `certified-construction-program/v2` certifies the bounded
  expression-point translation `P+V`, `V+P`, `P-V` under its structural
  restrictions; `C+(1,0)` and `C+u` certify; `Translate(C,u)` keeps v1 behaviour;
  unsupported expressions stay fail-closed and rich-only.
- **Segment driver.** `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE =
  RESOLVED_IN_X1` candidate, including the repository witness whose
  `LocusLength` moved from the wrong `2.25` to the correct `1`.
- **Inline literals.** `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY =
  RESOLVED_IN_X1` candidate, restoring the correct isolated evaluator DAG.
- **Lifecycle.** Redefine, undo/redo, copy and native reopen evidence is that of
  the candidate report and the archived run.
- **X2.** `PRE-G9B-R3-X2 = NOT NEEDED`, conditional on author approval of X1.
  `PRE-G9B-R4` remains not authorized.

## 7. Debt ledger (carried forward)

| Identifier | State |
|---|---|
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | candidate resolution in X1 (pending author approval) |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | `RESOLVED_IN_X1` candidate (pending author approval) |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | `RESOLVED_IN_X1` candidate (pending author approval) |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained unchanged; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN`, `OBS-R2-PHASE-SELECTION-COHORT`, `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE`, `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY`, `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS`, `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | observations retained unchanged |

The reconciliation adds no debt. Outside this phase, the unregistered suite
`tools/agent/tests/phase-lifecycle.Tests.ps1` was observed at 56/61 on unmodified
`T` as well (see the [GOV-R bootstrap record](gov_r_post_run_catalog_reconciliation_bootstrap_report.md)
§4.2); it is recorded, not absorbed.

## 8. Approval boundary

This record authorizes nothing. It does not record `PASS — AUTHOR APPROVED` for
X1, for the mechanism or for any gate, and it authorizes no push, promotion,
merge, tag or release.
