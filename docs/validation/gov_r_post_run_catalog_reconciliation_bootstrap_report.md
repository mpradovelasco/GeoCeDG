# GOV-R — post-run deterministic catalog reconciliation: bootstrap record

```text
TASK                                        = GOV-R (initial adoption)
BOOTSTRAP_EXCEPTION                         = POST_RUN_CATALOG_RECONCILIATION_INITIAL_ADOPTION
PRODUCT_PHASE_EFFECT                        = NONE
SCIENTIFIC_CONTRACT_CHANGED                 = false
PRODUCT_SEMANTICS_CHANGED                   = false
PREVIOUS_EVIDENCE_REINTERPRETED             = false
HEAVY_CAMPAIGN_RERUN_REQUIRED_FOR_BOOTSTRAP = false
TECHNICAL_CANDIDATE_STATE                   = FROZEN
AUTHOR_DECISION                             = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved                                = false
```

This record documents the technical bootstrap of the mechanism decided in
[ADR 0030](../adr/0030-post-run-deterministic-verification-catalog-reconciliation.md)
and specified in
[verification levels §11.3](../../geocedg/specs/operations/verification-levels.md#113-post-run-deterministic-catalog-reconciliation).
It restates no contract; those two documents are the authorities. It is not an
author decision, not a `FINAL` receipt and not an approval of the mechanism, of
`PRE-G9B-R3-X1` or of any product phase.

## 1. Identity

| Item | Value |
|---|---|
| parent (executed candidate `T`) | `8ab7dddc726c044b6e019e007bca5af938e834d1`, tree `7f75e3c669609be6369ddc0651e082370c96760f` |
| parent of `T` (`main` at authorization) | `78134aa8bc6ac30b65ffd916713fd3ca0502e6c6`, tree `f23218cc0b7c7189aae69489a1ce7a5ac5ee9fba` |
| branch | `feature/pre-g9b-r3-x1-certified-expression-points` (not pushed) |
| adoption commit `G` | this record is part of `G`; a commit cannot name itself, so `G` is recorded by the X1 reconciliation record and the task report |

`G` is a direct single-parent child of `T`, as §11.3 requires for the initial
adoption.

## 2. Changed paths (`T..G`)

| Path | Status | Class |
|---|---|---|
| `tools/agent/verification-catalog-reconciliation.psm1` | A | reconciliation mechanism |
| `tools/agent/reconcile-verification-catalog.ps1` | A | reconciliation mechanism |
| `tools/agent/tests/verification-catalog-reconciliation.Tests.ps1` | A | reconciliation mechanism (unregistered suite) |
| `geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json` | A | reconciliation mechanism |
| `tools/agent/phase-closeout.ps1` | M | closeout consumer (route B) |
| `geocedg/specs/operations/verification-levels.md` | M | governance policy (§11.3 and a current-contract pointer) |
| `.github/prompts/canonical/verification.prompt.md` | M | governance policy (one-paragraph pointer) |
| `docs/adr/0030-post-run-deterministic-verification-catalog-reconciliation.md` | A | documentation |
| `docs/developer/geocedg_developer_guide.md` | M | documentation |
| `docs/developer/geocedg_agent_prompt_guide.md` | M | documentation |
| `docs/validation/gov_r_post_run_catalog_reconciliation_bootstrap_report.md` | A | documentation (this record) |

The canonical verification prompt previously said that no commit follows the
`FINAL` receipt; without the pointer it would have contradicted the second
closeout route, so the change was required.

## 3. What did not change

- No product or kernel Java, Desktop/UI code, scientific or product JUnit test,
  Gradle/build file, resource, numerical reference, tolerance or toolchain file.
- No registry node, producer, projection, `verify.ps1`, supervisor, JUnit
  projection module, inventory updater, receipt module or result/receipt/registry
  schema. `verification-registry.json` and `verification-junit-inventory.json` are
  byte-identical to `T`; the X1 inventory is not updated by `G`.
- The `FINAL` execution plan resolved from the working registry is
  `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0`, equal to the
  plan of the source run.
- Closeout route A (ordinary accepted `FINAL` receipt) keeps its exact output
  shape; `verify-phase-author-closeout.ps1` is unchanged and remains a route-A
  adapter. Route B is reached through `phase-closeout.ps1 -ReconciliationCommit`.

## 4. Pre-commit validation (worktree content of `G`)

Logs are in the session scratchpad, outside the repository.

| Check | Command | Exit | Result |
|---|---|---|---|
| focused suite, run 1 | `pwsh -File tools/agent/tests/verification-catalog-reconciliation.Tests.ps1` | 0 | 21 cases, 240 assertions, 0 failures (233 s) |
| focused suite, run 2 | same | 0 | see §4.1 |
| closeout compatibility | `verification-closeout-compatibility.Tests.ps1` | 0 | 9 cases, 552 assertions |
| prompt contracts | `prompt-contract-validation.Tests.ps1` | 0 | 6 cases, 12 assertions |
| receipt | `verification-receipt.Tests.ps1` | 0 | 14 cases, 69 assertions |
| io | `verification-io.Tests.ps1` | 0 | 9 cases, 31 assertions |
| registry | `verification-registry.Tests.ps1` | 0 | 22 cases, 62 assertions |
| JUnit projection | `verification-junit-projection.Tests.ps1` | 0 | 12 cases, 36 assertions |
| final coverage | `verification-final-coverage.Tests.ps1` | 0 | 8 cases, 308 assertions |
| input identity | `repository-input-identity.Tests.ps1` | 0 | 45/45 fixtures |
| CLI compatibility | `verification-cli-compatibility.Tests.ps1` | 0 | 7 cases, 45 assertions |
| contract boundary | `verification-contract-boundary.Tests.ps1` | 0 | 5 cases, 38 assertions |
| bootstrap separation | `verification-bootstrap-separation.Tests.ps1` | 0 | 4 cases, 1011 assertions |
| diagnostic separation | `verification-diagnostic-separation.Tests.ps1` | 0 | 2 cases, 10 assertions |
| static semantic | `verification-static-semantic.Tests.ps1` | 0 | 2 cases, 38 assertions |
| §11.2 repair equivalence | `verification-repair-equivalence.Tests.ps1` | 0 | pass |
| prompt execution safety | `tools/agent/checks/prompt-execution-safety.ps1` | 0 | `CONTRACT_SATISFIED` |
| governance diagnostic | `tools/agent/diagnostics/governance-diagnostic.ps1` | 0 | standing `DIAGNOSTIC_FINDING` (pre-existing, non-acceptance) |
| whitespace | `git diff --check` | 0 | clean |

### 4.1 Focused suite coverage

The suite builds a temporary Git fixture over the real `FINAL` registry plan and
synthetic, fully sealed source runs, and covers: read-only `ANALYZE`; exact
official derivation; bootstrap acceptance and receipt content; determinism
(repeated validation and two CLI runs give one receipt identity); output
locations outside the source run and outside tracked Git; clean checkout of
exactly `R`; wrong run, result hash, tampered result, wrong candidate and foreign
plan; interrupted campaign, failed or missing producer, incomplete coverage and a
stale selection masking a failing test; semantic violation, a second untrusted
gate, two stale selections, an accepted source and a changed embedded selection;
absent logs, wrong log hashes and tampered JUnit XML; unauthenticated, modified and
unmatched discovery evidence; hand-entered hash, unexecuted identity, kept stale
catalog, stale pin and changed plan; product, scientific test, build, reference,
toolchain, verifier, updater and mechanism changes; README, frozen record, ADR
edit, execution-input directory, closeout-consumer and policy changes after
adoption and a wide adoption; grandchild, orphan, `R = G`, ordinary use without
adoption and symbolic commits; the execution-authority comparator; closeout
route B with the exact pair, wrong `R`, wrong `T`, approved `R`, missing `R`, the
source result presented as a receipt, forged product-delta and wrong-run
receipts, a re-identified tampered receipt, an invalid identity and a
schema-invalid receipt; unchanged route A; a later ordinary use with an unchanged
mechanism and its mechanism, consumer, policy and false-adoption negatives; and
the byte-identical, receipt-free source run.

### 4.2 Pre-existing failure outside this change

`tools/agent/tests/phase-lifecycle.Tests.ps1` (not a registered `FINAL` leaf)
reports 56/61 with failures 1, 3, 4, 5 and 6 about historical `BaselineVerifier`
and G9U1 fixture anchors. The identical 56/61 result with the same five failures
was reproduced on a shared clone of unmodified `T` in the session scratchpad, and
none of that suite's inputs (`verify.ps1`, `phase-lifecycle.ps1`, the phase
verifiers) is changed here. It is recorded, not repaired.

### 4.3 Read-only eligibility probe on the real source run

`Invoke-VerificationCatalogReconciliation -Action ANALYZE` against `T` and the
archived run `verification-ecdb8e4845064b46af2ec1df49963418` (result file SHA-256
`4e3fbc1d7f9a098f90068a96216d8c304d81bd4257890a4229b6c7249c086734`) returned
`ELIGIBLE`: untrusted check `junit.shared.final.semantic`, reason
`STALE_DERIVED_JUNIT_SELECTION_INVENTORY`, `final.shared` expected 6814
(`605658b0…`) versus executed 6842 (`558688f1…`), proposed inventory hash and pin
`b5488fcd…`. Nothing was written. The application is the separate X1 stage.

## 5. Residual limitations

- The focused suite is not a `FINAL` leaf; registering it would change the heavy
  plan and is deferred to the next separately authorized verification-
  infrastructure change.
- Closeout route B revalidates from the source archive and discovery evidence at
  the receipt locators. Both are ignored or external files; if they are absent the
  route fails closed and cannot be completed from the receipt alone.
- Version 1 admits one reason and exactly one reconciled selection.

## 6. Approval boundary

This record authorizes nothing. It records no author approval, no gate or phase
PASS, and no push, promotion, merge, tag or release.
