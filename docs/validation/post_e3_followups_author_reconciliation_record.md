# POST-E3 follow-ups — author decision reconciliation and integration record

```text
TASK                                   = POST-E3-AUTHOR-RECONCILIATION-AND-INTEGRATION-PREP
AUTHOR DECISIONS                       = RECORDED — 2026-10-10
POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION = PASS — AUTHOR APPROVED
OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP     = D0 — DISPOSITIONED — NOT LEGACY-LOAD SPECIFIC
OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP = OPEN — PRE-EXISTING UPSTREAM BEHAVIOR —
                                         USER-VISIBLE THROUGH SCRIPTING API —
                                         PRODUCT CORRECTION NOT AUTHORIZED
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE    = PASS — AUTHOR APPROVED
PSTRICKS C1 DESIGN                     = AUTHOR APPROVED
PSTRICKS C1 IMPLEMENTATION             = NOT AUTHORIZED IN THIS TASK — SEPARATE TASK
OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION = OPEN — PRE-EXISTING — OUTSIDE C1 —
                                         IMPLEMENTATION NOT AUTHORIZED
POST-E3-LEGACY-INVENTORY-CASE-FIX      = PASS — AUTHOR APPROVED
INTEGRATION CANDIDATE                  = PENDING AUTHOR REVIEW
PUBLICATION                            = NOT AUTHORIZED
F1                                     = NOT AUTHORIZED
selfApproved = false
```

This record preserves the explicit author decisions of 2026-10-10 on the three
independent POST-E3 activities. They were given in writing in the session, in
the instruction "GeoCeDG — POST-E3 Author Decision Reconciliation and
Publication Preparation". The record also documents the linear local
integration of their approved deltas. It is the sole authority for those author
decisions. Its machine-readable mirror is
[`post-e3-followups-author-reconciliation.json`](../../geocedg/validation/post-e3/post-e3-followups-author-reconciliation.json).

Under the frozen-candidate rule, the three original candidates, their reports
and their evidence mirrors record frozen state. They describe the original
candidates, not the integrated successors, and are not amended:

- [ES characterization report](post_e3_es_command_lookup_characterization_report.md)
- [PSTricks characterization report](post_e3_e2_pstricks_dimension_angle_characterization_report.md)
- [legacy inventory candidate report](post_e3_legacy_inventory_case_fix_candidate_report.md)

## 1. Published baseline and entry verification

```text
P_POST_E3 = 31286a2355cabebc69c3937442e7f15636da12ba
            tree d223d7400b2eabacddafba874b49eae581c5c6c4
            published E3/E3-R1 author closeout (PRE-G9B-R6-plus-E3 and E3-R1:
            PASS — AUTHOR APPROVED — PUBLISHED, author instruction of 2026-10-10)
local main = origin/main = live remote main = P_POST_E3; worktree clean
the three original candidates: each one commit, each a direct child of P_POST_E3,
not amended, not rebased, not on the remote
```

## 2. Original candidates and their verification (frozen evidence)

| Task | Original candidate | Tree | Verification (bound to the original) |
|---|---|---|---|
| `POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION` | `4fe048095015a1f6b3dae170b84f265d56e4d216` | `d009e7512f1d209db22aa015b3ad827c94a170ba` | `STATIC` `verification-f98eeff39b7e4505b39c6c44fb8bb0b1`, `ACCEPTED / COMPLETE` |
| `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE` | `4866ce2843915ce609a91fe6ab2f4cb67c94380e` | `3ad78fe20fea904d6a3d59436a8802469cfed16d` | `STATIC` `verification-f7a407c391c34f76af0c1c6463fb1e32`, `ACCEPTED / COMPLETE` |
| `POST-E3-LEGACY-INVENTORY-CASE-FIX` | `28e9aebe17725310ed785afad0a3f46e20126305` | `326a7603a42fe5b6f2d0018f27f7095fe8593c2a` | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`; `STATIC` `verification-9841175d464a4401b4104344d3fcde67` and `INFRA_UNIT` `verification-a142b475b4be4e39be760d43c4fdca95` (22/22), `ACCEPTED / COMPLETE`; `verify-legacy.ps1` passed |

These receipts stay attached to the original commits. They are not evidence for
the integrated successors (§8).

## 3. Decision A — Spanish command lookup

**`POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION` = `PASS — AUTHOR APPROVED`.**
The characterization established the following supported findings:

- normal application language changes work;
- starting GeoCeDG in Spanish works;
- opening legacy documents before or after a normal language change works;
- `setLanguage(String)` and the scripting API can leave localized command lookup
  stale;
- the GUI scripting route can produce an observable command-resolution failure;
- the behaviour predates `E3`;
- GeoCeDG and Classic share the inherited mechanism.

### 3.1 D0 for the original observation

`OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` = **`DISPOSITIONED — NOT LEGACY-LOAD
SPECIFIC`** (author decision D0). No product correction is authorized for the
supposed legacy-document-loading defect. This does not claim that localized
command resolution is correct under every application API.

### 3.2 Separate scripting API observation

```text
OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP =
  OPEN — PRE-EXISTING UPSTREAM BEHAVIOR — USER-VISIBLE THROUGH SCRIPTING API —
  PRODUCT CORRECTION NOT AUTHORIZED
```

Switching language through the scripting API may leave localized command lookup
inconsistent with the selected locale; English canonical command names remain
usable. The observation is independent of legacy-document loading. It is not
merely a test-harness artifact: the real GUI scripting route reproduced the
failure. `AppD.setLanguage`, `App.initTranslatedCommands`, `Localization`, the
command dictionaries, reverse command lookup and ADR 0031 compatibility
semantics are unchanged. Any future correction needs its own authorization.

## 4. Decision B — PSTricks dimension-angle characterization and C1

**`POST-E3-E2-PSTRICKS-DIMENSION-ANGLE` = `PASS — AUTHOR APPROVED`.**
PSTricks rotation arguments produced by the inherited numerical formatter can
exceed the numerical range accepted by the TeX processing path ("Number too
big"). The defect predates `E3`. The native dimension's geometric orientation is
correct, and the failure belongs to the export serialization layer.

### 4.1 C1 design — `AUTHOR APPROVED`

- correct only the PSTricks representation of the `E2` native-dimension text
  rotation, in the native-dimension text branch of `GeoCeDGGeoGebraToPstricks`;
- limit the serialized angle to six decimal places, deterministically, without
  scientific notation;
- keep the geometric orientation, the document serialization, the construction
  DAG, and the PGF/TikZ and Asymptote behaviour unchanged;
- meet the bound `|Δθ| ≤ 5·10⁻⁷°`, which affects only the export representation
  and never the authoritative CeDG geometry; the implementation verifies the
  bound and that the serialized angle is admissible to PSTricks;
- no global change to `GeoGebraExport.format()`, kernel geometry or
  native-dimension construction semantics.

### 4.2 C1 implementation boundary

The author approves C1 for a subsequent, separately executed bounded
implementation task, `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1`. It is **not**
authorized within this task. The next implementation instruction names the
exact published baseline that results from this integration and confirms the
readiness and verification requirements. The prepared prompt is the starting
execution contract. Its documentary status is reconciled by this task:
[`post-e3-e2-pstricks-dimension-angle-correction.prompt.md`](../../.github/prompts/tasks/post-e3-e2-pstricks-dimension-angle-correction.prompt.md),
`PREPARED — DESIGN AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED`. A
successful verification of this integration does not start C1.

### 4.3 Planned verification class

`BOUNDED_PHASE` is accepted as the planned class for C1. It is subject to the
implementation entry gate confirming that the correction stays in the PSTricks
dimension-text adapter and its tests, and is frozen at the start of that task.
If the scope is broader, the task stops for author disposition.

## 5. Decision C — host PSTricks rotation defect

```text
OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION =
  OPEN — PRE-EXISTING — OUTSIDE C1 — IMPLEMENTATION NOT AUTHORIZED
```

The same formatting problem affects inherited PSTricks rotations of objects
other than native dimensions, for example a rotated ellipse. It is retained as
separate technical debt. C1 does not modify the shared formatter or unrelated
rotated objects, and C1 is not a global PSTricks repair. The debt is not
assigned to `F1`. A separate characterization and correction may be considered
later.

## 6. Decision D — legacy inventory correction

**`POST-E3-LEGACY-INVENTORY-CASE-FIX` = `PASS — AUTHOR APPROVED`** on the
original technical candidate `28e9aebe17725310ed785afad0a3f46e20126305` (tree
`326a7603a42fe5b6f2d0018f27f7095fe8593c2a`). The correction:

- replaces the case-insensitive label-to-type map of `tools/legacy/ingest.ps1`
  with an ordinal, case-sensitive lookup;
- corrects 24 I/O type records in 8 macros of `Templatev7`, of 169 records
  examined;
- regenerates the three derived inventories deterministically and idempotently;
- leaves historical originals, macro definitions, manifests, schemas and
  curation unchanged.

The canonical legacy verification passed, and `STATIC` and `INFRA_UNIT` were
accepted. The frozen class `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` is
accepted. No interactive product smoke is required, because the correction
changes inventory metadata and its generation, not executable macro semantics.
`Templatev7.ggb` and `models/legacy/*/original/` stay unchanged, and the
ingestion process receives no further change.

## 7. Linear local integration

The three candidates are independent children of `P_POST_E3`. They are
integrated on the local branch `phase/post-e3-followups-author-integration` by
`git cherry-pick -x`, in the authorized order. Each integrated commit message
carries `(cherry picked from commit <original>)`:

| Order | Source task | Original candidate / tree | Integrated successor / tree | Method |
|---|---|---|---|---|
| 1 | ES characterization | `4fe04809…` / `d009e751…` | `ed3f7a5e6a73aa0fd53bf069530ff6a128d44d6d` / `d009e7512f1d209db22aa015b3ad827c94a170ba` (tree identical to the original) | clean cherry-pick |
| 2 | PSTricks characterization | `4866ce28…` / `3ad78fe2…` | `61dbb0adba8e880e284ba08cb6e5f4e20591cd75` / `7719b6911a4f40e08c26d2150666068c3024a731` | cherry-pick; additive resolution of the adjacent mini-track status lines |
| 3 | legacy inventory correction | `28e9aebe…` / `326a7603…` | `718c408118168591ccc2799a862538f1afdeb367` / `3996114b64d8d58245a4bff30cc58e9b2ff1c124` | cherry-pick; additive resolution of the adjacent mini-track status lines |
| 4 | author reconciliation and integration status | — | the commit that contains this record | new commit |

**Conflicts and resolution.** Each task changed its own `POST-E3-*` status line
in the mini-track plan §14. The lines are adjacent, so cherry-picks 2 and 3
conflicted. The resolution keeps all three task blocks in their original order,
each exactly as its task wrote it. Against each original candidate, the
resolved plan differs only by the other tasks' blocks. No other file conflicted.

**Equivalence.**

- Every file a task added or changed has the same bytes in the integrated
  successor as in its original candidate. The only exception is the shared
  mini-track plan, reconciled as above.
- `git diff 28e9aebe <successor 3> -- tools/legacy models/legacy` and the
  legacy reports are empty. The ordinal lookup, the 24 corrected records, the
  three `generator.sha256` values and the derived inventories are therefore
  identical to the approved candidate.
- The two characterizations stay documentary-only.
- The C1 prompt is reconciled in status only and stays unexecuted; no PSTricks
  product code is changed.

## 8. Integration verification (frozen requirements)

The integrated diff against `P_POST_E3` is the union of the three approved
deltas plus this reconciliation: documents, evidence mirrors, two task prompts
(the C1 prompt reconciled in status), status lines, and the approved legacy
operational correction (`tools/legacy/ingest.ps1` and three derived
inventories). No product code, test, build, registry, inventory selection,
verifier or serialization path changes.

The frozen class is `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, the class of the
only non-documentary change. The minimum obligations on the integration
candidate are:

- documentary validation: record consistency, JSON parsing, relative links, the
  prompt-contract parser on the C1 prompt, roadmap and mini-track consistency,
  `git diff --check`, and no change to protected files;
- legacy operational validation: `tools/agent/verify-legacy.ps1`,
  `ingest.ps1 -Check` for all three resources, idempotent regeneration,
  original hashes, and record-level equivalence with `28e9aebe`;
- `STATIC` and `INFRA_UNIT` on the integration candidate.

No `FINAL`, because the global verification infrastructure and the product are
unchanged. The receipts of that verification are new and bound to the
integration candidate. They are reported outside this record, which cannot name
its own commit; the original receipts of §2 are never reattributed.

## 9. Product effect

The integration changes no product behaviour. Its only non-documentary content
is the approved legacy metadata correction: `tools/legacy/ingest.ps1` and the
derived inventories. Executable macro semantics, `Templatev7.ggb` and the
curated GGT library are unchanged.

## 10. Publication and next activities

```text
PUBLICATION          = NOT AUTHORIZED (separate author instruction after review of the
                       exact integration candidate)
NEXT                 = author review of the integration and possible publication
AFTER PUBLICATION    = POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 as a separate task on the
                       published integration commit
PLANNING INTENT      = POST-E3 integration → C1 implementation → C1 author review and smoke
                       → C1 approval and publication → F1 readiness review
                       (author planning intent, not an authorization chain)
F1: NOT AUTHORIZED   F2, F3, F4, G: unchanged
```

No tag, release, application-version change or installer publication is
authorized. The integration is not marked `PASS — AUTHOR APPROVED — PUBLISHED`.
The author has approved the original task outcomes, and the integrated
successor stays pending review until it is explicitly accepted.
