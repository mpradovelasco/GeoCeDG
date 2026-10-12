# PRE-G9B-R6-plus-F3 — conservative-scope assessment

- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION` (author instruction of
  2026-10-12)
- Status: **PROPOSED — PENDING AUTHOR REVIEW**
- Base: F3 preparation candidate `480c972972e90514e88421194a792898c916834e`,
  tree `166caf6c7802c37db03060a2fd07cd71d28daa17`
- Evidence: [F3 preparation characterization report](pre_g9b_r6_plus_f3_preparation_characterization_report.md)
  (historical, `K*`, `CF-F3-*`) and the
  [focused feasibility report](pre_g9b_r6_plus_f3_reconciliation_feasibility_report.md)
  (`FQ-1`–`FQ-10`, `RF-*`, `OTI-F3R-*`)
- Decisions: [author-decision reconciliation record](pre_g9b_r6_plus_f3_author_decision_reconciliation_record.md)
- Design: [conservative insertion design candidate](../architecture/pre_g9b_r6_plus_f3_conservative_insertion_design_candidate.md)
- `selfApproved = false`

## 1. Author direction recorded

```text
CONSERVATIVE AUTHOR DIRECTION = RECORDED — SCOPE REASSESSMENT AUTHORIZED (2026-10-12)
PRINCIPLE                     = fail closed: an unsupported, ambiguous, incomplete or
                                unverified insertion is rejected without modifying
                                the target document
PRIORITY                      = correctness > functional breadth > upstream behavioral
                                compatibility
INHERITED CHANGES             = acceptable in principle when justified and assessed;
                                none approved by this direction
SCOPE REDUCTION               = allowed for consideration; any reduction of F3-D2
                                needs an explicit author amendment
```

The direction is recorded here and in the decision record. It does not by itself
amend `F3-D1` to `F3-D6`, approve a feature set, freeze a class or authorize any
change.

## 2. Reconciliation with `F3-D1` to `F3-D6`

| Decision / item | Content (abridged) | Status under the conservative direction |
|---|---|---|
| `F3-D1` | debt assigned to `F3` | **retained** |
| `F3-D2` (1) | File → Insert surface | **mandatory, retained** |
| `F3-D2` (2) | Insert File and Apply Template move there | **mandatory, retained** |
| `F3-D2` (3) | both accept `.cedg` and `.ggb` | **retained as container rule; narrowed by content admissibility** — the extension identifies the container; the admissibility contract decides whether its contents can be incorporated. Proposed explicit wording in `DQ-F3-13` (an interpretation the author must confirm, not a silent redefinition) |
| `F3-D2` (4) | Insert File keeps its semantics, is not Open, keeps `D1` provenance and notice, never scales | **narrowed — needs amendment**: "imports the construction" becomes "imports an admissible construction completely or not at all"; macros, scripts and unverified types rejected; Overwrite removed; the window copy buffer no longer replaced (also amends `DQ-D1-7`, `DQ-F3-16`). `D1` provenance, notice and no scaling **retained** |
| `F3-D2` (5) | Apply Template applies labeling, defaults and styles; no geometry; no unit change | **narrowed — needs amendment** (`DQ-F3-17`): verified object styles only; construction defaults and labeling style **deferred** (not undoable today, `FQ-9`); no geometry and no unit change **retained** |
| `F3-D2` (6)–(7) | OFF out of the GeoCeDG UI; internal OFF support kept | **retained** |
| `F3-D2` (8) | no other formats, no 3D import | **retained**; content-level 3D objects inside a `.cedg` are governed by the type matrix, not by this format rule |
| `F3-D2` (9) | `F3` closes before the global closeout | **retained**; closure of the debt requires the author to approve the amended scope (`DQ-F3-20`) |
| `F3-D3` | Desktop-first, minimal upstream-identical seams | **retained**; the recommended contract needs no inherited change (`FQ-10`) |
| `F3-D4` | `INTEGRATED_PHASE`, `PHASE` + `INTEGRATION` | **retained** (§7) |
| `F3-D5` | `D1 → F3` hard dependency | **retained**, plus the `DQ-D1-7` amendment question |
| `F3-D6` | operational order | **retained** |

Requirements that stay mandatory: the File → Insert surface with two
independent actions; `.cedg`/`.ggb` containers; OFF out of the GeoCeDG UI;
`D1` provenance, notice and no coordinate scaling; no geometry import and no
unit change by Apply Template; Classic unchanged; closure before `G`.
Requirements narrowable without contradiction: the admissible content of a
container; the label-conflict interface. Requirements needing an explicit
amendment: the inherited Insert File semantics, the window-buffer replacement
of `DQ-D1-7`, the defaults and labeling parts of Apply Template. Deferred:
macro-dependent insertion (unless `DQ-F3-15` admits M1), template defaults and
labeling style, the types of `OTI-F3R-4`. Excluded: destructive Overwrite,
partial import, OFF in the GeoCeDG UI.

## 3. Insertion alternatives

| Criterion | **S — strict minimal** | **R — restricted native** | **B — broad repaired** |
|---|---|---|---|
| Scope | elementary 2D construction types only (`SUPPORTED` rows of §4 without Locus V2 family, dimensions, 3D); no macros; no scripts; label conflict → reject (L0) | every `SUPPORTED`/`SUPPORTED WITH CONDITIONS` row of §4, including the Locus V2 family, Spline V2, linear dimensions and identity-bearing records; no macros (M0) unless `DQ-F3-15` admits M1; no scripts; safe rename (L1); no Overwrite | the inherited `AppD.insertFile` with Rename/Overwrite/Cancel, all macros and scripts, repaired |
| Correctness guarantee | admission before mutation; closure; verified commit | same, plus identity-bearing atomic path | requires repairing the macro double import and rebinding, Overwrite dependents, failure paths, undo, buffer (`CF-F3-1`–`CF-F3-7`) |
| Dependency preservation | complete source construction; closure verified (`FQ-1`) | same (`FQ-1`, `FQ-5`) | macro collisions rebind to target macros (`CF-F3-3`) unless remapped |
| Transactionality | all-or-nothing: pre-mutation admission, dry run, post-verification, rollback by `restoreCurrentUndoInfo` (`FQ-3`) | same | `AppD.insertFile` mutates macros before the dialog and is `final`; atomicity needs an inherited rewrite |
| Identity | target instances untouched; inserted objects are new participants; no label inference | same; spatial identity records preserved (`FQ-1`) | Overwrite replaces target objects by label (`CF-F3-5`) |
| Undo/redo | one undo step, byte-identical Undo (`FQ-3`) | same | needs undo integration around the inherited method |
| `D1` | provenance through a dedicated buffer; notice; no scaling (`FQ-6`) | same | buffer replacement and stale provenance on failure (`CF-F3-1`) |
| Implementation size | small: one orchestration class, admission matrix, dedicated buffer | small–medium: + rename path, identity-bearing cases, larger matrix and tests | large: inherited `AppD`/`CopyPasteD`/`GuiManagerD` changes, macro remapping (M2) |
| Upstream modification | none | none | several inherited Desktop changes; possibly shared macro semantics (U2) |
| Testability | high; small matrix | high; matrix grows with each admitted type | low; many interacting legacy paths |
| Usefulness | low: almost every real pair of documents shares labels such as `A`, `B`, so L0 rejects most insertions | high for macro-free documents | highest |
| Residual risk | low | low–medium (dry-run determinism on large documents, `OTI-F3R-2`) | high |
| Extensibility | add rows to the matrix | add rows; M1 then M2 later | already broad, hard to certify |

Alternative C of the instruction (minimal initial insertion) corresponds to S;
alternative B (restricted native) to R; alternative A (broad repaired) to B.

**Recommendation: R** with M0, L1 and the type matrix of §4 as the initial
contract. S is the fallback if the author does not accept L1; B is not
recommended. Insert File stays useful (FQ-1 shows a realistic mixed CeDG
construction inserts completely) and no inherited class changes.

## 4. Evidence-based admissibility matrix (initial contract R)

| Type (class / producing algorithm) | Status | Evidence |
|---|---|---|
| free and dependent points; midpoint; point on path; single intersection | `SUPPORTED` | `FQ-1`, `FQ-5` |
| line, ray, segment, vector | `SUPPORTED` | `FQ-1` |
| circle, ellipse (conics from points) | `SUPPORTED` | `FQ-1` |
| dependent function | `SUPPORTED` | `FQ-1` |
| polygon with its edges | `SUPPORTED` | `FQ-1` |
| angle from three points | `SUPPORTED` | `FQ-1` |
| free numbers, booleans | `SUPPORTED` | `FQ-1` |
| dependent text | `SUPPORTED` | `FQ-1` |
| lists, `Sequence` (local variables) | `SUPPORTED` | `FQ-1`, `FQ-5` |
| show-object condition referencing an inserted boolean | `SUPPORTED WITH CONDITIONS` (condition object inside the insertion) | `FQ-5` |
| Locus V2, `LocusLength`, Locus V2 intersection results (identity-bearing) | `SUPPORTED WITH CONDITIONS` (target must allow Locus V2 creation, `RF-1`; no macros, `RF-6`) | `FQ-1`, `FQ-5` |
| Spline V2 with its promoted list and degree | `SUPPORTED WITH CONDITIONS` (as Locus V2) | `FQ-1` |
| `LinearDimension` and its outputs | `SUPPORTED WITH CONDITIONS` (text recomputed in target units, `RF-2`) | `FQ-1`, `FQ-5` |
| 3D point | `SUPPORTED WITH CONDITIONS` (only the probed free 3D point) | `FQ-1` |
| macro-generated objects | `REJECTED` initially (M0); `CONDITIONALLY FEASIBLE` under M1 (`FQ-4`) | `FQ-4`, `CF-F3-3` |
| objects with GGBScript or JavaScript scripts | `REJECTED` initially (GGBScript rename works, `FQ-5`; JavaScript and static label strings are not rewritten) | `FQ-5`, S |
| `AlignedDimension`, `IsoABorder` | `NOT ESTABLISHED` → rejected | `RF-3`, `OTI-F3R-4` |
| 3D lines, planes, solids, other 3D objects | `NOT ESTABLISHED` → rejected | `OTI-F3R-4` |
| images and objects bound to external files, buttons, input boxes, CAS cells | `NOT ESTABLISHED` → rejected | `OTI-F3R-4` |
| any (class, algorithm) pair not listed | `NOT ESTABLISHED` → rejected | fail-closed rule |

The matrix is a whitelist of (object class, parent-algorithm class) pairs. A
source is admissible only if **every** object of its construction matches a
`SUPPORTED` row or a `SUPPORTED WITH CONDITIONS` row whose condition holds.
Successful rendering is never evidence; each row needs a test that checks
class, algorithm, inputs inside the insertion, value and persistence on reopen.

## 5. Explicit exclusions

| Rejected capability | Why | Stage | User sees | Unchanged | Future |
|---|---|---|---|---|---|
| missing, unsupported-extension, unreadable or empty source | not a valid native construction | before the helper / after helper load | localized message naming the file and reason | target document, buffer, undo, saved state, recent list | — |
| `.off` in Insert File, Apply Template or GeoCeDG Open | OFF is out of the GeoCeDG UI (`F3-D2` 6) | name resolution | localized "OFF files are not supported in GeoCeDG" | everything | Classic keeps OFF |
| macros in the source (M0) | silent rebinding and duplicate import (`CF-F3-3`, `CF-F3-4`) | admission on the helper | message listing the tools | everything | M1 feasible (`FQ-4`); M2 later phase |
| scripts in the source | executable behavior; label strings not rewritten | admission | message listing the objects | everything | later phase with a script contract |
| types outside the matrix | semantics not verified | admission | message listing labels and types | everything | add matrix rows with tests |
| label conflicts under L0 (only if the author chooses S) | no rename | admission | message listing labels | everything | L1 |
| destructive Overwrite | deletes target dependents (`CF-F3-5`) | not offered | no Overwrite choice | — | separate contract if ever wanted |
| partial import | violates fail closed | never offered | — | — | only as a separately approved contract |
| verification failure after commit | element skipped by the parser (`FQ-3`) | after commit | message "nothing was inserted" after rollback | document restored to the pre-insertion undo state | — |
| template construction defaults and labeling style | not undoable (`FQ-9`) | not applied | documented; only object styles change | defaults, labeling | later phase with undo support |
| template restyling of GeoCeDG-specialized objects | changes dimension styling (`FQ-8`) | skipped per object | documented | those objects | explicit rules later |

No message after a modification claims that "some objects could not be
imported": either everything admissible is inserted, or nothing is.

## 6. Upstream-modification assessment

| Option | Surface | Defect | Required? | Classic effect | Recommendation |
|---|---|---|---|---|---|
| U0 GeoCeDG-owned | orchestration, dedicated buffer, helper overrides, chooser policy (design §10) | all of `CF-F3-1`–`CF-F3-12` reachable from GeoCeDG | sufficient (`FQ-10`) | none | **recommended** |
| U1-a `CopyPasteD.insertFrom`: reset `duplicateLabels` after the paste | `desktop/util/CopyPasteD.java:644-684` (field `:67`) | `CF-F3-7` placeholder labels after Insert File | not needed by U0 (dedicated buffer) | Classic Ctrl+V after Insert File becomes correct | optional, separate author decision `DQ-F3-19`; not in the recommended F3 |
| U1-b `AppD.insertFile` / `applyTemplate`: check the helper load result | `desktop/main/AppD.java:4775-4856` | `CF-F3-1`, `CF-F3-12` in Classic | not needed (GeoCeDG never calls them) | Classic stops pasting stale buffers / factory defaults | out of F3 (records only) |
| U1-c `GuiManagerD.openFile`: null check on Cancel | `desktop/gui/GuiManagerD.java:2046`, `:2065`, `:2083` | `CF-F3-6` | not needed (GeoCeDG Open has no such filters) | Classic Cancel stops throwing | out of F3 |
| U2 shared | macro semantics, construction transfer, undo of defaults | — | not needed for R | — | not recommended |

Each U1 option is an inherited Desktop change with a modified-files record, a
Classic regression test, no serialization or parsing effect, and no change of
the verification class (§7). None is authorized by this assessment.

## 7. Verification classification

`verification-levels.md` §12.8: `INTEGRATED_PHASE` = `PHASE`, plus `COMPOSED`
(executed as `-Profile INTEGRATION`; `COMPOSED` is its compatibility alias)
when the frozen plan identifies concrete additional integration coverage.

| Design | Impact facts | Class |
|---|---|---|
| R or S with U0 | GeoCeDG Desktop, profile, registry, validator; crosses profile/menu/registry, helper load, `D1` provenance, `A-1`/`A-2` layers, Undo; no inherited, shared, serialization or parser change | **`INTEGRATED_PHASE`** (registered `PHASE` + `INTEGRATION`) — retained |
| R + U1-a | as above plus one inherited Desktop change affecting Classic | `INTEGRATED_PHASE`, with a Classic regression node and a modified-files record |
| B | inherited Desktop rewrites; possibly shared macro semantics (M2) | `INTEGRATED_PHASE` at least; `GLOBAL_IMPACT` if shared macro or construction-transfer semantics change |

No recommended design requires `GLOBAL_IMPACT`. The class is not frozen here.

## 8. Recommended combination

- **Insert File: contract R** (whitelist matrix §4, M0, L1 safe rename, no
  Overwrite, no scripts, dedicated insertion buffer, admission before mutation,
  dry run, post-verification, rollback, one undo step, helper isolation).
- **Apply Template: contract T0** (verified object styles only: colour, line
  thickness, line style, point size, point style, fill opacity, taken from the
  template's default of the same default type, for the selection or else all
  objects; GeoCeDG-specialized objects skipped; no layers, captions, offsets,
  label visibility, fixed flag, visibility, conditions, colour functions,
  construction defaults or labeling style; one undo step; unsaved state;
  pre-validated template).
- Both: Desktop-only (U0), `INTEGRATED_PHASE`.

## 9. Residual risks

- Dry-run determinism and cost on large documents (`OTI-F3R-2`).
- Rollback after a forced mid-commit failure not probed (`OTI-F3R-1`).
- Usefulness of M0 for documents built with the curated tool library: such
  documents are rejected until `DQ-F3-15` admits M1 or a later phase adds M2.
- T0 is a narrower Apply Template than users of upstream GeoGebra expect; the
  guides must say so.
- The matrix grows only with tests; an unlisted type is rejected even if it
  would have inserted correctly.
