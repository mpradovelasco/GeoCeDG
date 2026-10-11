# PRE-G9B-R6-plus-F1 closeout record

```text
PRE-G9B-R6-plus-F1        = PASS — AUTHOR APPROVED — PUBLISHED
AUTHOR_SMOKE              = PASS (author confirmation of the interactive F1 smoke)
BOUNDED_PHASE             = ACCEPTED / COMPLETE (PHASE PRE-G9B-R6-PLUS-F1, 4/4)
F1 SPECIFICATION          = NORMATIVE / AUTHOR APPROVED
OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT
                          = OPEN — NON-BLOCKING FOR F1 — CAUSAL ATTRIBUTION NOT ESTABLISHED
```

```text
selfApproved              = false
authorApproved            = true    (F1 only, candidate 1cea3c89)
passClaimed               = true    (F1 only)
implementationAuthorized  = false   (F2, F3, F4, G and every open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE    (closeout)
newHeavyVerification      = NONE    (closeout; BOUNDED_PHASE accepted by PHASE)
closeoutClass             = DOCUMENTATION_STATUS_ONLY (acceptance STATIC)
```

This record preserves the author decision of 2026-10-11 on
`PRE-G9B-R6-plus-F1`, given in writing in the session ("PRE-G9B-R6-plus-F1
Author Closeout and Publication", task `PRE-G9B-R6-plus-F1-AUTHOR-CLOSEOUT`).
It is the sole authority for the author approval of F1. Its machine-readable
mirror is
[`pre-g9b-r6-plus-f1-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f1-closeout.json).
The [candidate report](pre_g9b_r6_plus_f1_candidate_report.md), its evidence
mirror, the [F1 author-decision record](pre_g9b_r6_plus_f1_author_decisions_record.md)
v1.2, the [preparation characterization report](pre_g9b_r6_plus_f1_preparation_characterization_report.md),
the [reconciled design](../architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md)
and the `P0` [F1 design candidate](../architecture/pre_g9b_r6_plus_f1_orientation_design_candidate.md)
record earlier states and are not amended; they remain historical evidence.

## 1. Entry verification

```text
live remote main = origin/main = local main = c52f565e9f2c4c3515a68f6ac120ffc9c393dfbc
                   tree fdfbc327983a18d6761411591daf606942f15fda (published F1 preparation)
authorization    = f1166e3e0f42919e3834a39b78c0e6140489a37a (direct child of c52f565e)
candidate        = 1cea3c89e34d32ab6321a434e7bb873bc1a33f83
                   tree 0901ef120a8f945ff70e94d00f21cdd7eb10b58f, direct child of f1166e3e,
                   not amended, not on the remote; worktree clean
```

## 2. Author smoke (mandatory gate)

The author confirmed in writing that the interactive F1 smoke is satisfactory.

```text
AUTHOR_SMOKE = PASS
```

The approved functional scope is the orientation cue for admitted lines,
segments and rays; synchronized activation and deactivation from the View menu
and from Layout & Presentation; correct visual orientation; suppression of
unsupported and hidden objects; persistent preference behavior; no effect on
construction geometry or export behavior. The approval does not extend to the
excluded geometric routes (`X1`–`X10`) or to Graphics View 2.

## 3. Author decision

```text
PRE-G9B-R6-plus-F1 = PASS — AUTHOR APPROVED
approved candidate = 1cea3c89e34d32ab6321a434e7bb873bc1a33f83
                     tree 0901ef120a8f945ff70e94d00f21cdd7eb10b58f
publication        = AUTHORIZED (ordinary non-force fast-forward of main to this
                     closeout commit, through c52f565e -> f1166e3e -> 1cea3c89)
selfApproved       = false
```

No change to kernel geometric semantics, object identity, the construction DAG,
document serialization, shared drawables or export adapters is authorized by,
or contained in, the approved implementation.

## 4. Accepted evidence

```text
PHASE PRE-G9B-R6-PLUS-F1 on 1cea3c89 / 0901ef12
      verification-207719610a7042d4bf25814f78354b5a, ACCEPTED / COMPLETE, 4/4
      (compile.shared.semantic, compile.desktop.semantic, workspace-profile.product,
      junit.desktop.pre-g9b-r6-plus-f1.semantic: 225 tests), 0 diagnostics;
      result_hash 6b771cad841ad5aa06adffd3dc2aa5cdbc821b23e5806343ce9a7c208b071370
STATIC on 1cea3c89 / 0901ef12
      verification-46fb0a7ec50d400aa101b2d10cf0b334, ACCEPTED / COMPLETE, 3/3
      (standing diagnostics only); result_hash 30b0661ec85e3f46363717c3407aa8064aabd9569ebda47743cabe7ee791c4d8
INFRA_UNIT on 1cea3c89 / 0901ef12
      verification-f260f621cdb748bfabf0f0014926d362, ACCEPTED / COMPLETE, 22/22,
      0 diagnostics; result_hash 502c27ce48084bb4ee6224036258c6518b00e7d9f1ce5ea22528a4b978a05d25
```

The local receipts stay under `artifacts/agent/pre-g9b-r6-plus-f1-{phase,static,infra-unit}/`
with their commit and tree attribution. No heavy verification is repeated for
this documentary closeout; the candidate is unchanged.

## 5. Specification promotion

[`geocedg/specs/presentation/orientation-cue.md`](../../geocedg/specs/presentation/orientation-cue.md)
moves from `PROPOSED` to `NORMATIVE / AUTHOR APPROVED`. The promotion covers
the behavior of the F1 author decisions (record v1.1 content, unchanged in
v1.2) as implemented by the approved candidate. Only the status header, the
approval scope row, the status paragraph and the title of its implementation
section change; no requirement is added or altered. No ADR is required: the
seam stays the approved Graphics View 1 overlay and no repository governance
rule sets an unmet ADR obligation.

## 6. Observations

| Identifier | Disposition |
|---|---|
| `OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT` | OPEN — NON-BLOCKING FOR F1 — CAUSAL ATTRIBUTION NOT ESTABLISHED |
| `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` | OPEN — unchanged |
| `OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP` | OPEN — unchanged |

`OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT`: during the inventory
regeneration of the F1 candidate, the first executed `final.desktop` run
(2004 tests) failed once in
`G9U1SemanticPointInteractionTest.ownedDragUndoRedoRestoresExactSemanticAddress`
(`expected LocusSemanticAddress2D@494946f but was …@5ead26d6`). The test passed
three of three times in isolation and in a complete rerun (2004 tests, 0
failures, 1 skipped), which is the evidence used for the inventory; the
registered F1 `PHASE` was accepted independently. It is not declared
pre-existing (no baseline reproduction) and not attributed to F1 (no causal
evidence). The test, Locus V2, undo/redo and the verification infrastructure are
unchanged; any correction needs separate characterization and authorization.
Both executions are preserved under the ignored
`artifacts/agent/pre-g9b-r6-plus-f1-final-desktop-evidence/`:

```text
final.desktop.json       (failed run 1)  sha256 69e167aa4ab23c644467452751d446498e08836608c4e4c14900a8f417c0fb72
final.desktop.run2.json  (passing run 2) sha256 5f091b317baa356388f044496cd0ee79fb7d27f3c40c920674b089a259b586c0
run 1 TEST-org.geocedg.desktop.G9U1SemanticPointInteractionTest.xml
                                         sha256 61a8271791c6523bb8e0bcb4ff56f6144c90382014e977db26dab0d8767b2732
```

## 7. Closeout verification and publication

This closeout commit is a documentary descendant of the approved candidate,
containing this record, its mirror, the specification promotion and status
lines. It is verified with `STATIC` and the documentary checks. The author
authorizes publication of the exact linear range:

```text
P_F1_PREP  c52f565e9f2c4c3515a68f6ac120ffc9c393dfbc
  -> f1166e3e0f42919e3834a39b78c0e6140489a37a (implementation authorization)
  -> T_F1     1cea3c89e34d32ab6321a434e7bb873bc1a33f83 (approved candidate)
  -> P_F1     (this closeout commit)
```

The publication goes to `main` by one ordinary non-force fast-forward. No tag,
release, binary, packaging or product-version change is authorized. The
published identity is reported outside this record, which cannot name its own
commit.

## 8. Next activity

```text
NEXT = PRE-G9B-R6-plus-F2 (authoring-memory refinements) — the next planned
       subphase; only the P0 design candidate exists (PROPOSED); preparation,
       author decisions and a canonical prompt are needed before any
       implementation authorization
F2   = NOT AUTHORIZED (no implementation from this closeout)
F3, F4, G = unchanged
```
