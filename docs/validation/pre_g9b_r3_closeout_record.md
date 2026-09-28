# PRE-G9B-R3 closeout record

```text
PRE-G9B-R3 = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
```

This record preserves an explicit author disposition of 2026-09-28 and the
exact evidence identities behind it. It changes no product, test, build,
verifier, schema, serialization or scientific-contract file, and it does not
modify the approved technical candidate. Besides this record and its
machine-readable mirror, it only updates the `PRE-G9B-R3` status statements of
the roadmap to match this disposition; the roadmap order is unchanged.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the candidate's own artifacts record only `TECHNICAL_CANDIDATE_STATE = FROZEN`
and `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole
authority for current author approval. Every statement in the candidate's
artifacts stands unchanged.

## Author disposition

```text
PRE-G9B-R3 = PASS — AUTHOR APPROVED

APPROVED_TECHNICAL_CANDIDATE = 89fab9d1ee09f95f5c21f61bced46a102366b2ef
APPROVED_TREE                = e3ff7c34a6cf3ad40cdd737e463ced5a9f3066e5
AUTHOR_SMOKE                 = PASS
FINAL                        = verification-f363f882a106457e839d05e0ac40becb, ACCEPTED / COMPLETE
FINAL_RECEIPT                = c4d9a8bf0a8ab86df40faf0971ef1fa0d625684ba8dc92a78275be06eab3e0fa
```

## Approved technical candidate

```text
commit = 89fab9d1ee09f95f5c21f61bced46a102366b2ef
tree   = e3ff7c34a6cf3ad40cdd737e463ced5a9f3066e5
parent = c1f3ed61691eb351c06caf2283e90bcc28152d76
base   = ef120a54f035b85df014e89c3916cb3a26f5f933
branch = feature/pre-g9b-r3-redefine-correctness
```

The approved candidate is the bounded corrective descendant `PRE-G9B-R3-R1`
(normative, test, documentation and roadmap only; bytecode-identical product
implementation) of the frozen `PRE-G9B-R3` technical candidate
`c1f3ed61691eb351c06caf2283e90bcc28152d76` (tree
`ca913cb23f5303c8b802fbfd5eea9b9d403358e9`). Both are preserved unchanged.
Their reports are the [candidate report](pre_g9b_r3_candidate_report.md), the
[validation matrix](pre_g9b_r3_validation_matrix.md) and the
[corrective report](pre_g9b_r3_r1_corrective_candidate_report.md).

## Accepted content

The author's approval covers `PRE-G9B-R3` as finally reconciled by
`PRE-G9B-R3-R1`:

- one versioned durable-dependency projection: version 1 `DIRECT` and version 2
  `TRANSITIVE_DURABLE_FRONTIER`, with lazy migration only through an ordinary
  semantic event (`D3-a` and `D3-b` resolved);
- typed redefine outcomes, the compatible-retain predicate and the Desktop
  surfacing of reasons and durable-contract choices;
- `CERTIFIED_DURABLE_FRONTIER_REFRESH`: an ordinary edit that moves a version-2
  frontier keeps the identity only under the certified predicate, with both
  revisions advancing; otherwise the whole edit is restored;
- `LATE_PARTICIPATION_REPROJECTION`: re-projection under the record's own
  version, keeping identity, roles and both revisions, with no migration and no
  redefine predicate; a failed publication is rejected as a whole;
- undo and redo restore the committed snapshot, record versions included, and
  never migrate;
- the living normative documents: the
  [durable dependency projection contract](../../geocedg/specs/spatial/durable-dependency-projection.md)
  1.1, [ADR 0029](../adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md)
  as amended, the additive notes in
  [ADR 0011](../adr/0011-g9-spatial-persistence-and-phase-gates.md), the
  [G9 spatial projection semantics](../../geocedg/specs/spatial/g9-spatial-projection-semantics.md)
  §8.2 and the
  [G9U1 construction interaction](../../geocedg/specs/ui/g9u1-construction-interaction.md)
  §10, and the [compatible-redefine design §7](../architecture/post_g9u1_a3_v2_compatible_redefine.md);
- fail-closed treatment of every unsupported or ambiguous case.

## Author smoke

```text
PRE_G9B_R3_AUTHOR_SMOKE = PASS
```

Author-provided, as recorded in §6 of the corrective report: the canonical `R3`
witness, its redefine as a compatible retain, identity preservation, the
dependent locus and its intersections, undo, redo, and native save and reopen.
The known certified expression-point limitation is not a smoke failure.

## Accepted evidence

The author accepts the `FINAL` campaign run on the exact approved candidate
under an explicit `VERIFICATION_ESCALATION_REQUEST = APPROVED`. No further
verification is run for this closeout.

| Field | Value |
|---|---|
| command | `tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r3-r1-final` |
| run | `verification-f363f882a106457e839d05e0ac40becb` |
| verdict | `ACCEPTED / COMPLETE`; 40/40 acceptance checks satisfied, 0 violated, 0 untrusted, 0 not run; exit 0 |
| plan hash | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |
| result hash | `3e539dff79a2556b9e105b0bc9b760e2c3a00796da5544cb2e068b79d8c11fe0` |
| result file SHA-256 | `7657d5874cb3038298e8b9e1a86f27291329e3487e125f8fab70c53e52217771` |
| receipt id | `c4d9a8bf0a8ab86df40faf0971ef1fa0d625684ba8dc92a78275be06eab3e0fa` |
| receipt file SHA-256 | `af584144bd4b0b4a016c9d12ebd2bd0bf60c0b5a94b157c46572b12be3dd3d37` |
| receipt binding | `candidate_commit = 89fab9d1ee09f95f5c21f61bced46a102366b2ef`, `candidate_tree = e3ff7c34a6cf3ad40cdd737e463ced5a9f3066e5`, `base_commit = null` |
| checker / input identity | `9a78d5726556692959d68fab22bd8e1d756033a590e5a1ccd2ad95c8214f919e` / `d9eae17232276bc010a3e0bd0548df892322c6d63c1e858c143e277fb58d764b` |
| environment | signature `de567676a3168eb19a13b0b8764c15e6fdca0d97baec6925aac5b72f6c7dc4c0`, observation `a329dd0ec835b3c3d7cc0ec5561cb636ebe9552c9e2288b82b75c2fce4d1c4ba` |

JUnit evidence of the run: `final.shared` 6 787 tests, 0 failures or errors,
10 allowlisted skips, including the three `PreG9bR3R1LateParticipationTest`
methods; `final.desktop` 1 523 tests, 0 failures, 1 skip; `g9u1-isolated` 5 and
`revision3-diagnostic` 1, both without failures. All 11 classes of the
`post-g9u1-a3.narrow` selection are in the shared evidence (114 tests, 0 failed);
the inventory pin of that selection stays 114 identities, `9a45556c…`. The plan
hash equals that of the parent candidate's `FINAL`.

Diagnostics, separate from acceptance: eight `DIAGNOSTIC_CLEAR`;
`diagnostic.governance` `DIAGNOSTIC_FINDING` (the temporary recovery-protocol
note) and `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`, both
identical in outcome and cause to the parent candidate's accepted `FINAL`.

The identity-only closeout inspection
`tools/agent/phase-closeout.ps1 -Action INSPECT` with this receipt and
`-ApprovedCommit 89fab9d1…` returned `identity_confirmed = true` with no
mismatch. The receipt is kept locally under the untracked
`artifacts/agent/pre-g9b-r3-r1-final/`.

### Other evidence on the approved candidate

| Campaign | Run | Plan hash | Result hash | Verdict |
|---|---|---|---|---|
| `PHASE -Phase POST-G9U1-A3` | `verification-386a7dbd87de457691038eb600e40640` | `c2e25e0b5d28523023f88dc139f015cda004ce28cc94d1ff546a65c4c222876f` | `093b62be67669bf7c79271f0bd9fc5987fcc1ef9a5d5b96459f218f64e545e6e` | `ACCEPTED / COMPLETE`, 2/2; 114 tests, fingerprint `9a45556c…` |
| `INTEGRATION` | `verification-54122ec82e484235a06386ed7ef9bcb2` | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` | `467999c015d35a4f7b5f5d7618314ed04720fdda8d8fba7c13a9322081c0e665` | `ACCEPTED / COMPLETE`, 19/19 |

### Historical evidence of the frozen parent candidate

These runs bind `c1f3ed61691eb351c06caf2283e90bcc28152d76` and keep their
original classification; their receipt is not evidence for the approved
candidate.

| Campaign | Run | Result hash | Verdict |
|---|---|---|---|
| `FINAL` | `verification-deda408e65c24e5091e6a54fe9c99922` | `8ad0adb7d21bb8956593ecff6aebe3bbdf665868d10e6a696828e1a9f8d66e68` | `ACCEPTED / COMPLETE`, 40/40; receipt `ab8ba93a42775e7f8096877deb79d389a36a138ba6547717e6590e207434c9c5` |
| `PHASE -Phase POST-G9U1-A3` | `verification-daed7b6949f2494687263f3fa40afca4` | `92eb89d0f7446b2607ac6b6f8edb1cde75e8db92895a4b3fde7232d79381f2bb` | `ACCEPTED / COMPLETE`, 2/2 |

## Retained quality debt

Carried forward exactly, as the author directs. None is resolved by this
closeout.

| Identifier | Class | Owner / due | State |
|---|---|---|---|
| `TD-CLIPBOARD-ATOMIC-ROLLBACK-PROTOCOL` | pre-existing product defect, severity `HIGH` | `PRE-G9B-R3-C1` | retained; the outer clipboard rollback enters rollback mode without the runtime restore protocol and leaks `blockUpdateScripts=true` into the saved document (corrective report §5) |
| `TD-CLIPBOARD-CONSTANT-INPUT-PREDECESSOR-CLOSURE` | pre-existing product defect | `PRE-G9B-R3-C1` | retained; the copy payload omits predecessors whose command has a constant axis input |
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | capability extension, not a defect | `PRE-G9B-R3-X1` | retained; expression points have no certified construction model |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | pre-existing product defect | by the `PRE-G9B-R7` closeout | retained; the certified model keeps failing closed for affected shapes |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | pre-existing product defect | by the `PRE-G9B-R7` closeout | retained; new constructions fail closed (`R3` report §5.5) |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | deferred refinement | owner and disposition due by the `PRE-G9B-R7` closeout | retained |

Dispositions already recorded by the approved candidate and unchanged here:
`TD-UPSTREAM-RECORD-E0-E1-PURPOSE` resolved in `R3`;
`TD-DOC-PRE-G9B-R-ROADMAP-STATUS-RECONCILIATION` resolved in `R3-R1`;
`TD-R3-OBS-CLIPBOARD-PARTIAL-CLOSURE-PASTE` superseded by the two clipboard
debts.

The Ri quality-debt policy of the
[`PRE-G9B-R2-E0/E1` closeout record](pre_g9b_r2_e0_e1_closeout_record.md)
continues to apply to every later `PRE-G9B-R` phase.

## Other retained items

| Item | Class | State |
|---|---|---|
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | retained pre-existing baseline defect | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | verification observation | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | pre-existing documentation drift | carried forward unchanged |

## Roadmap order

The author-selected execution order is preserved:

```text
PRE-G9B-R3-C1
    ↓
PRE-G9B-R3-X1
    ↓
PRE-G9B-R4
```

`PRE-G9B-R3-C1` and `PRE-G9B-R3-X1` remain `DESIGNED / PLANNED — NOT AUTHORIZED
FOR IMPLEMENTATION`; `PRE-G9B-R4` remains `DESIGNED — NOT AUTHORIZED`. The order
is an execution order, not a semantic dependency.

## Promotion

The author authorizes promotion of the linear history ending at this closeout
descendant to `main`, by ordinary clean fast-forward only: no rebase, amend,
merge commit, squash, force push, tag or release.

Remote identity before promotion:

```text
local main       = ef120a54f035b85df014e89c3916cb3a26f5f933
origin/main      = ef120a54f035b85df014e89c3916cb3a26f5f933
live remote main = ef120a54f035b85df014e89c3916cb3a26f5f933
tree             = 98bdd7220223bedefee0eddbb2c9ef5e6572b133
```

The identity after promotion is this record's own commit, which the record
cannot name; it is reported with the promotion.

## Authorization state

This closeout authorizes nothing beyond its own promotion. `PRE-G9B-R3-C1` is
the next planned phase and requires a separate explicit author authorization
with an exact base commit; `PRE-G9B-R3-X1`, `R4` and every later `PRE-G9B-R`
phase, `G9B`, `G9C`, `G9U2` and productive `G10` remain unauthorized. Repair of
a retained debt is authorized only as its owning phase or the Ri policy allows.
No tag or release is authorized.
