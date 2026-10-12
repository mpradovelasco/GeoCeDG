# PRE-G9B-R6-plus-F3 — conservative insertion: reconciled design candidate

- Status: **SUPERSEDED — NOT AUTHOR APPROVED AS A WHOLE**; its §9 test matrix is part of the approved design by reference (author decision of 2026-10-13, [F3 author design closeout record](../validation/pre_g9b_r6_plus_f3_author_design_closeout_record.md); implementation NOT AUTHORIZED)
- Produced by: `PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION` (author instruction
  of 2026-10-12; conservative scope reassessment, documentary only)
- Base: F3 preparation candidate `480c972972e90514e88421194a792898c916834e`,
  tree `166caf6c7802c37db03060a2fd07cd71d28daa17`
- Supersedes as the **current proposal** (without amending it): the
  [F3-PREP reconciled design candidate](pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md),
  which stays unchanged as historical evidence; its §3 (surface), §4 (Open
  route and OFF), §8 (helper) and §9 (Classic and fallback) are reused here by
  reference
- Evidence: [focused feasibility report](../validation/pre_g9b_r6_plus_f3_reconciliation_feasibility_report.md);
  [conservative-scope assessment](../validation/pre_g9b_r6_plus_f3_conservative_scope_assessment.md);
  historical [characterization report](../validation/pre_g9b_r6_plus_f3_preparation_characterization_report.md)
- Questions: [author-decision reconciliation record](../validation/pre_g9b_r6_plus_f3_author_decision_reconciliation_record.md)
  v2.0 (`DQ-F3-1` to `DQ-F3-20`, all pending)
- Claim vocabulary: **proposed / not normative**; where a section depends on a
  pending question it names it. `selfApproved = false`.

## 1. Governing principle

An unsupported, ambiguous, incomplete or unverified insertion or template
application is rejected **without modifying the target document**. A
successful operation has demonstrable postconditions; a rejected one has
demonstrable non-mutation postconditions. Correctness takes precedence over
breadth and upstream behavioral compatibility.

## 2. Contracts

```text
INSERT FILE (contract R; DQ-F3-13, -14, -15, -18)
PRECONDITION   container .cedg/.ggb exists; archive preflight passes; isolated helper
               loads it; construction non-empty; every object matches the admissibility
               matrix; no macros (M0) [or collision-free macros, M1]; no scripts;
               label conflicts resolvable by safe rename (L1) [or none, L0];
               dry run on a scratch copy of the target verifies
OPERATION      insertFrom(helper, target, duplicates, overwrite = false) through a
               dedicated GeoCeDGCopyPaste instance
POSTCONDITION  every source object created exactly once; pairwise class and parent
               algorithm equal; every input of a created object is created; target
               objects untouched (same instances, same dependencies); target UnitState
               and drawing scale unchanged; coordinates unchanged; D1 provenance from
               the source; notice per unit-system §11; one undo step; document unsaved
FAILURE        before commit: nothing touched (XML, macros, undo history, saved state,
               window buffer, recent list); after commit: restoreCurrentUndoInfo()
               to the pre-insertion state; a "nothing was inserted" message; never a
               success claim

APPLY TEMPLATE (contract T0; DQ-F3-17)
PRECONDITION   container .cedg/.ggb exists; preflight passes; helper loads it
OPERATION      for the selection, else every object not excluded: colour, line
               thickness, line style, point size, point style and fill opacity from
               the template default of the object's default type
POSTCONDITION  only those six properties changed; geometry, labels, layers, hidden-layer
               membership, captions, offsets, label visibility, fixed flag, visibility,
               show conditions, colour functions, construction defaults, labeling
               style and UnitState unchanged; one undo step; document unsaved
FAILURE        nothing changed; message; never factory defaults
```

## 3. Surface, Open route, OFF, helper, Classic

Unchanged from the F3-PREP candidate (§3, §4, §8, §9) under `DQ-F3-1`,
`DQ-F3-2` A1, `DQ-F3-3` X1, `DQ-F3-4` OFF-B, `DQ-F3-10` H-1/H-3 and
`DQ-F3-11` F-1, with one difference: the GeoCeDG orchestration **does not call
`AppD.insertFile` or `AppD.applyTemplate`**. Consequently the inherited label
dialog, `showRenamedObjectsDialog`, the duplicate macro import and the failed-
load paths of `AppD` are never reached from GeoCeDG, and the `GuiManagerGeoCeDG`
`showRenamedObjectsDialog` override and the `GeoCeDGCopyPaste.insertFrom`
override proposed by F3-PREP are no longer needed.

## 4. Insert File orchestration (`GeoCeDGDocumentInsertion`, proposed)

1. **Selection** (`DQ-F3-3` X1): single file; `.cedg`, else `.ggb`, for a stem;
   other extensions refused.
2. **Preflight**: `DocumentArchivePreflight.validate(bytes,
   app.createDocumentPreflightConfig())`; any `false` or exception rejects.
3. **Helper**: `app.newAppForTemplateOrInsertFile()` (GeoCeDG helper flag:
   no recent-file publication, `DQ-F3-10` H-1); `loadFile`; a `false` result
   rejects.
4. **Admission** on the helper only:
   - empty construction → reject;
   - every object (with CAS cells) matched against the whitelist of
     (object class, parent-algorithm class) pairs (§5); first unmatched →
     reject listing all unmatched labels and types;
   - conditional rows: Locus V2 family only if the target's runtime features
     allow Locus V2 creation;
   - macros: M0 rejects any helper macro or `AlgoMacro` parent; M1 rejects a
     command name present in the target;
   - scripts (any event type) → reject;
   - label conflicts = helper labels ∩ target labels: L1 passes them as the
     duplicate set (rename), L0 rejects.
5. **Dry run** (`DQ-F3-18` A18-1): a scratch GeoCeDG host loaded with
   `target.getXML()` receives the same insertion through its own dedicated
   buffer; the postconditions of §2 are checked there. Failure rejects.
6. **Commit**: `new GeoCeDGCopyPaste(app).insertFrom(helper, app, duplicates,
   false)` (dedicated buffer, `DQ-F3-16` B16-1). The `D1` provenance is taken
   from the helper and the notice emitted at paste completion as today.
7. **Verification** of §2 on the real target; on any discrepancy
   `restoreCurrentUndoInfo()` and the failure message.
8. **Undo**: one `storeUndoInfo()`; the document becomes unsaved.
9. **Release** the helper and the scratch host (`OTI-F3-3`).

Precondition of step 7: the target's current state equals its last stored undo
state (`OTI-F3R-1`); the entry gate confirms it or stores a baseline before the
insertion without creating a visible extra step.

## 5. Admissibility matrix

The initial matrix is the one of the assessment §4 (`DQ-F3-14` A14-1). It is
held as data in the orchestration class, each row bound to one test that checks
class, parent algorithm, closure, value and reopen persistence. A row is added
only with its test. Unlisted pairs are rejected.

## 6. Apply Template orchestration (T0)

1. Selection and preflight as §4.1–§4.2; helper as §4.3.
2. Targets: the selected objects, else every object of the construction,
   excluding GeoCeDG-specialized results (native-dimension outputs, `IsoABorder`
   outputs, Locus V2 family — `OTI-F3R-5`).
3. For each target, `def = templateDefaults.getDefaultGeo(targetDefaults.getDefaultType(geo))`;
   when present: `setObjColor`, `setLineThickness`, `setLineType`,
   `setAlphaValue`, and for points `setPointSize`, `setPointStyle`; then a
   visual-style update.
4. One `storeUndoInfo()`; unsaved.
5. Release the helper.

`TemplateHelper`, `ConstructionDefaults` and the labeling style are not used or
changed. Construction defaults and the labeling style are deferred (`FQ-9`).

## 7. `D1`, layers and identity

| Contract | Insert File (R) | Apply Template (T0) |
|---|---|---|
| unit-system §11 provenance and notice | from the insertion's dedicated buffer; window buffer untouched (`DQ-D1-7` amendment, `DQ-F3-16`) | not involved |
| target `UnitState`, drawing scale | unchanged (`FQ-6`) | unchanged (`FQ-8`) |
| coordinates | unchanged | unchanged |
| `A-1` working layer | not applied (paste exclusion); inserted objects keep source layers | layers untouched |
| `A-2` hidden set | not imported; target set unchanged | unchanged; no object leaves a hidden layer |
| object identity | target instances untouched; inserted objects are new participants; no label or coordinate inference; spatial identity records preserved (`FQ-1`) | untouched |
| dependencies | closure verified | untouched |

## 8. Files (expected)

| Path | Change |
|---|---|
| `apps/geocedg/application-profile.yml` | surface and texts (F3-PREP §3) plus rejection and status texts |
| `…/GeoCeDGActionRegistry.java`, `…/GeoCeDGProfile.java` | targets; pin 131 → 133 |
| `…/GeoCeDGDocumentInsertion.java` (new) | §4–§6 and the matrix |
| `…/GeoCeDGDocumentPolicy.java` | extensions and Open-name resolution without `.off` |
| `…/GuiManagerGeoCeDG.java` | `openFile(File)`, `doOpenFiles(File[], boolean)` overrides (fallback-gated) |
| `…/AppGeoCeDG.java` | helper flag, helper `setCurrentFile`, helper reference for release |
| `tools/agent/workspace-profile-validation.ps1`, static-contract and registry pins, phase registration | as F3-PREP |
| tests, `docs/upstream/modified-files.yml`, guides, `geocedg/specs/ui/document-insertion.md` (proposed) | as F3-PREP, with the contracts of §2 |

No inherited or shared file changes (`DQ-F3-19` U0). `GeoCeDGCopyPaste` is
reused unchanged (its constructor is package-accessible).

## 9. Test matrix (replaces F3-PREP §11 where they differ)

| Id | Obligation |
|---|---|
| `T-F3-MENU`, `T-F3-L10N`, `T-F3-PINS`, `T-F3-CLASSIC`, `T-F3-FALLBACK`, `T-F3-OPEN-UNCHANGED`, `T-F3-OFF`, `T-F3-GUIDES` | as F3-PREP |
| `T-F3R-MATRIX` | one test per admitted row (class, algorithm, closure, value, reopen) |
| `T-F3R-REJECT` | every exclusion of the assessment §5 rejects before mutation (target XML, macros, undo history, saved state, window buffer, recent list identical) |
| `T-F3R-RENAME` | L1 closure with conflicting labels; target instances and dependents untouched; scripts absent by admission |
| `T-F3R-MACROS` | M0 rejection (and M1 if chosen: one import, value preserved, collision rejected) |
| `T-F3R-ATOMIC` | dry run equals commit; forced commit failure restores the identical XML (`OTI-F3R-1`) |
| `T-F3R-UNDO` | one step; Undo byte-identical; Redo; Undo of a later action leaves the insertion |
| `T-F3R-D1` | eight unit pairs; window buffer untouched; Ctrl+V after insertion pastes the user's buffer with correct labels |
| `T-F3R-IDENTITY` | Locus V2 family and identity records inserted, persisted and reopened |
| `T-F3R-TEMPLATE` | T0 property matrix: only the six properties change; layers, hidden membership, captions, offsets, fixed, visibility, conditions, colour functions, defaults, labeling, units unchanged; specialized objects skipped |
| `T-F3R-TEMPLATE-UNDO` | one step, byte-identical Undo, unsaved state |
| `T-F3R-TEMPLATE-FAIL` | missing/unreadable template changes nothing |
| `T-F3R-HELPER` | no recent-list entry; helper and scratch host released |
| `T-SMOKE` | author smoke (prompt) |

## 10. Verification

`INTEGRATED_PHASE` retained (assessment §7): registered `PHASE`
`PRE-G9B-R6-PLUS-F3` (`compile.desktop.semantic`, `workspace-profile.product`,
`junit.desktop.pre-g9b-r6-plus-f3.semantic`) and `INTEGRATION` on the same
commit. A chosen U1 option adds a Classic regression node and a modified-files
record; a shared change (none recommended) triggers reclassification review.

## 11. Rejected alternatives

- Repairing `AppD.insertFile` in place (Design B): inherited rewrites, macro
  remapping, low testability.
- Partial import with a warning.
- Reusing a target macro by name.
- Overwrite of existing objects.
- Applying construction defaults or the labeling style without undo support.
- A second geometric construction engine: the commit always goes through the
  kernel's construction copy (`insertFrom` → construction XML evaluation).
