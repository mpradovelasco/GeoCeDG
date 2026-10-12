# PRE-G9B-R6-plus-F3 — restricted native insertion: design candidate

- Status: **PROPOSED — PENDING AUTHOR REVIEW**
- Produced by: `PRE-G9B-R6-plus-F3-FOCUSED-RISK-CLOSEOUT` (author instruction
  of 2026-10-12; documentary only)
- Base: `T_F3_RECONCILIATION` `0eb50311d55a099de70e3f222b885b31685ac0df`,
  tree `061e08f1a9d821cf642827afaf52d802629b940a`
- Successor of (both unchanged, kept as history): the
  [conservative insertion design candidate](pre_g9b_r6_plus_f3_conservative_insertion_design_candidate.md)
  and the [F3-PREP design candidate](pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md),
  whose surface (§3), Open route and OFF (§4), helper (§8) and Classic/fallback
  (§9) sections remain the proposal by reference
- Evidence: [focused feasibility report](../validation/pre_g9b_r6_plus_f3_focused_feasibility_report.md);
  [risk closeout candidate](../validation/pre_g9b_r6_plus_f3_risk_closeout_candidate.md)
- Questions: [focused author-decision preparation record](../validation/pre_g9b_r6_plus_f3_focused_author_decision_preparation_record.md)
  v3.0 (`DQ-F3-1` to `DQ-F3-24`, pending)
- `selfApproved = false`

No requirement of the earlier proposals is weakened silently: every narrowing
is a named amendment in the decision record §4.

## 1. Contracts

```text
INSERT FILE (R + M1 + L1)
ADMIT      container .cedg/.ggb; preflight; helper load; non-empty; every object in the
           admissibility matrix (§3); target allows required runtime features; macros:
           transitive closure computed; each required macro absent in the target or
           digest-equal to the target's; no identity records together with macros;
           label conflicts resolvable by renaming; no scripts
DRY RUN    same insertion on a scratch host loaded from the target XML, strict check
COMMIT     insertFrom(helper, target, conflicts, overwrite = false) on an operation-
           scoped GeoCeDG insertion buffer that drops digest-equal (reused) macros
VERIFY     count; pairwise object class and parent-algorithm class; closure internal;
           identity records allocated for identity-bearing objects
SUCCESS    one undo step (the orchestration stores one only when the identity-bearing
           paste has not); D1 notice from the insertion provenance; selection cleared
           (inherited); document unsaved
FAILURE    before commit: nothing changes; after commit (DQ-F3-21 K1): restore the last
           undo state, remove transferred macros, restore the saved flag, remove the
           identity-paste undo entry (entry gate), message "nothing was inserted"

APPLY STYLES (T0)
ADMIT      container; preflight; helper load
APPLY      colour, line style, line thickness, point style, point size, fill opacity
           from the helper default of the object's default type, to the selection or to
           all objects except native-dimension, IsoABorder and Locus V2 family outputs
SUCCESS    one undo step; unsaved
FAILURE    nothing changes
```

## 2. Components (U0)

| Component | Role |
|---|---|
| `GeoCeDGDocumentInsertion` (new) | chooser, admission, dry run, commit, verification, compensation, undo, helper release, messages |
| `GeoCeDGInsertionCopyPaste` (new, subclass of `CopyPasteD`) | operation-scoped buffer: records the source `UnitState` through the existing `D1` path (`AppGeoCeDG.unitPasteCompleted`), drops reused macros from `copiedMacros` before `pasteFromXML`; never stored on the window |
| `GeoCeDGUserToolLibrary` digest | macro definition equality (`definitionDigest`, made package-accessible) |
| `AppGeoCeDG` | helper flag (no recent-list publication), helper reference for release |
| `GuiManagerGeoCeDG`, `GeoCeDGDocumentPolicy`, profile, registry | as the F3-PREP design §3–§4 |

`GeoCeDGCopyPaste` (the window buffer) is not changed. No inherited or shared
class changes.

## 3. Admissibility matrix

The risk candidate §2 is the matrix. Supported without condition: elementary 2D
construction types of the conservative assessment §4 (points, lines, rays,
segments, vectors, circles, ellipses, functions and function chains, polygons,
angles, numbers, booleans, dependent texts, lists, sequences, show conditions
inside the insertion). Supported with conditions: Locus V2 family, Spline V2,
Linear and Aligned dimensions, IsoABorder, macro-generated objects. Rejected:
scripts, 3D objects, projection-binding and spatial-object records, payloads
with identity records and macros, macros with different or unproven
definitions. Everything else is not established and rejected. A row is
implemented only with its test.

## 4. Verification and integration

`INTEGRATED_PHASE` (not frozen): registered `PHASE` `PRE-G9B-R6-PLUS-F3`
(`compile.desktop.semantic`, `workspace-profile.product`,
`junit.desktop.pre-g9b-r6-plus-f3.semantic`) and `INTEGRATION`. Concrete
integration coverage: the File surface across profile compiler, menu bar and
registry; the insertion transaction across helper load, kernel construction
copy, undo manager, macro manager and identity registry; the `D1` provenance and
notice with an independent buffer; L1 across macro and specialized objects;
style application across the `A-1`/`A-2` layer workspace and undo; the Open and
document-lifecycle suites. `DQ-F3-21` K2 would require a shared change and an
author reclassification.

## 5. Tests (additions to the conservative design §9)

| Id | Obligation |
|---|---|
| `T-F3R-ATOMIC-PRECOMMIT` | every rejection class: serializable and runtime certificate unchanged |
| `T-F3R-ATOMIC-DRYRUN` | unknown command, renamed command, missing macro: rejected by the dry run without target change |
| `T-F3R-ATOMIC-COMMIT` | injected view fault and stage faults: serializable restore, saved flag, macro cleanup, no surviving undo entry, disclosed session reset |
| `T-F3R-STRICT-VERIFY` | degraded objects (free instead of dependent) detected |
| `T-F3R-M1` | simple, nested (transitive collision), different definition, digest-equal reuse without duplicates, repeated insertion, curated library tool, identity + macros rejection, Undo/Redo macro behavior per `DQ-F3-22` |
| `T-F3R-L1` | chains, conditions, texts, functions, macro and specialized objects; determinism; undo, redo, reopen |
| `T-F3R-SPECIAL` | Locus V2 family, Spline V2, Linear/Aligned dimensions, IsoABorder: construction, edits, identity, undo/redo, reopen, unit text, export link |
| `T-F3R-BUFFER` | two windows, success, failure, Ctrl+V, units, repeated and macro insertions |
| `T-F3R-T0` | property whitelist, exclusions, selection scope, undo/redo, saved state, failed load |
