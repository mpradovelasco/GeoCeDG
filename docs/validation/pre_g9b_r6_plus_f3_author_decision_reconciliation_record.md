# PRE-G9B-R6-plus-F3 — author-decision reconciliation record

- Record version: **2.0** (successor of the
  [F3-PREP author-decision preparation record](pre_g9b_r6_plus_f3_author_decision_preparation_record.md),
  version 1, unchanged and kept as historical evidence)
- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION` (author instruction of
  2026-10-12)
- Status: **QUESTIONS RECONCILED — ALL PENDING — NO DECISION RECORDED**
- Base: F3 preparation candidate `480c972972e90514e88421194a792898c916834e`,
  tree `166caf6c7802c37db03060a2fd07cd71d28daa17`
- Evidence: [focused feasibility report](pre_g9b_r6_plus_f3_reconciliation_feasibility_report.md)
  (`FQ-*`, `RF-*`, `OTI-F3R-*`); historical
  [characterization report](pre_g9b_r6_plus_f3_preparation_characterization_report.md)
  (`K*`, `CF-F3-*`); [conservative-scope assessment](pre_g9b_r6_plus_f3_conservative_scope_assessment.md);
  design: [conservative insertion design candidate](../architecture/pre_g9b_r6_plus_f3_conservative_insertion_design_candidate.md)
- Governing planning decisions: `F3-D1` to `F3-D6` of the
  [F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md)
  (2026-10-03), unchanged; amendments are **proposed** below, never applied
- `selfApproved = false`

## 1. Author direction of 2026-10-12 (recorded, not a decision on any question)

GeoCeDG provides only insertion and template operations whose correctness,
integrity and reversibility can be demonstrated; the inherited GeoGebra
behavior is not an obligatory baseline when unsafe; F3 may be reduced,
restricted or redesigned; targeted inherited changes are acceptable in
principle when justified and assessed; correctness takes precedence over
breadth and upstream behavioral compatibility; fail closed. The direction
approves no feature set, no architecture, no inherited change and no class.

This record re-evaluates `DQ-F3-1` to `DQ-F3-12` under that direction and adds
`DQ-F3-13` to `DQ-F3-20`. Identifiers are not renumbered; version-1
recommendations remain readable in the version-1 record. Every entry is
**pending**; a recommendation is the agent's proposal.

## 2. Revised matrix of `DQ-F3-1` to `DQ-F3-12`

| Id | Existing author-fixed requirement | F3-PREP finding | Conservative alternatives | Recommendation v2 (v1 if unchanged) | Required evidence | Impact on `F3-D*` | Feasibility | Verification | Residual risk |
|---|---|---|---|---|---|---|---|---|---|
| `DQ-F3-1` menu | File → Insert with two actions (`F3-D2` 1–2) | no File submenu; group entries render submenus | N1 / N2 / N3 | **N1 unchanged** (`file-insert`; `document.insert-file`, `document.apply-template`; `geocedg.document.*`) | menu and pin tests | none | CONFIRMED (declarative) | profile pins, validator amendment | none |
| `DQ-F3-2` chooser and seams | Desktop-first (`F3-D3`) | Insert/Template/OFF are Open filters; Cancel NPE | A1 / A2 / A3 | **A1 unchanged**; the insertion chooser is part of a GeoCeDG orchestration that **no longer calls `AppD.insertFile` or `AppD.applyTemplate`** | `FQ-10` | none | CONFIRMED | Classic regression | duplicated Open-dialog setup |
| `DQ-F3-3` formats | `.cedg`/`.ggb` (`F3-D2` 3) | `.ggb`-only filters; stem → `.ggb` | X1 / X2 / X3 | **X1 unchanged**, now read as the **container** rule; content decided by `DQ-F3-13`/`-14` | `FQ-2` | interpretation of item 3 (`DQ-F3-13`) | CONFIRMED | failure tests | none |
| `DQ-F3-4` OFF | OFF out of the GeoCeDG UI (`F3-D2` 6–7) | typed and fallback routes open | OFF-A / OFF-B / OFF-C | **OFF-B unchanged** | H (`K4`) | none | CONFIRMED | OFF tests | none |
| `DQ-F3-5` objects, labels, macros | Insert File keeps its semantics (`F3-D2` 4) | macro double import and rebinding; Overwrite deletes dependents | labels L0 / L1 / L2; macros M0 / M1 / M2 | **changed**: **L1 safe rename, no Overwrite** (L0 if the author prefers S); macros per `DQ-F3-15` (recommended **M0**); objects per `DQ-F3-14` | `FQ-4`, `FQ-5` | amends item 4 (`DQ-F3-13`) | CONFIRMED | rename, rejection, closure tests | static strings and JavaScript are not renamed (scripts rejected) |
| `DQ-F3-6` undo, cancel, failure | — | no undo point; failure re-pastes stale buffer | U-1 / U-2 / U-3 | **changed**: all-or-nothing by `DQ-F3-18`; one undo step; no mutation on any rejection; no label dialog at all (Cancel only in the chooser) | `FQ-2`, `FQ-3` | amends item 4 | CONDITIONALLY FEASIBLE (`OTI-F3R-1`) | atomicity, undo/redo, failure tests | forced mid-commit failure not probed |
| `DQ-F3-7` `D1` | keep provenance and notice; no scaling (`F3-D2` 4; `F3-D5`) | intact on success; stale on failure | P-A / P-B | **changed**: provenance and notice through a **dedicated insertion buffer** (`DQ-F3-16`); no scaling; window buffer untouched | `FQ-6` | amends accepted `DQ-D1-7` | CONFIRMED | 8 unit pairs end to end; Ctrl+V after insertion | none known |
| `DQ-F3-8` template invariants | labeling, defaults, styles; no geometry; no unit change (`F3-D2` 5) | resets captions, fixed, offsets, conditions, colour functions; not undoable; failed load applies factory defaults | T-1 / T-2 / T-3 (v1); T0 / T1 / T2 (v2) | **changed**: contract **T0** (`DQ-F3-17`) | `FQ-8`, `FQ-9` | amends item 5 | CONFIRMED for object styles | property-matrix, undo, saved-state tests | narrower than upstream |
| `DQ-F3-9` layers | `A-1`/`A-2` contracts | re-layering to the working layer | L-A / L-B / L-C / L-D | **T0 makes the question moot**: T0 never touches layers; L-A only if the author keeps the inherited restyle | `FQ-8` | none | CONFIRMED | layer tests | none |
| `DQ-F3-10` helper and clipboard | no unexpected session effects (instruction §11) | recent list, leaked registrations, placeholder labels | H-1/H-2, H-3/H-4, P1/P2/P3 | **H-1, H-3 unchanged**; clipboard by `DQ-F3-16`; `CF-F3-7` no longer reachable from GeoCeDG; the upstream one-line fix becomes the optional `DQ-F3-19` | `FQ-6`, `FQ-7`, `FQ-10` | none | CONFIRMED (H-1 by source; H-3 `OTI-F3-3`) | helper and clipboard tests | `OTI-F3-3` |
| `DQ-F3-11` Classic, fallback | Classic unchanged | Classic shares the defects | F-1 / F-2 | **F-1 unchanged** | `K10` | none | CONFIRMED | Classic and fallback tests | Classic keeps `CF-F3-*` unless `DQ-F3-19` |
| `DQ-F3-12` class | `INTEGRATED_PHASE` (`F3-D4`) | Desktop-only | C-1 / C-2 | **C-1 unchanged**; `GLOBAL_IMPACT` only if a shared change were chosen (none recommended) | assessment §7 | none | — | registered `PHASE` + `INTEGRATION` | — |

## 3. New decisions

### DQ-F3-13 — Insert File contract and the reading of `F3-D2` items 3–4

**Nature.** AUTHOR DECISION (amendment of an approved planning decision).

| Alternative | Consequence |
|---|---|
| **A13-1 — amend `F3-D2` item 4 to: "Insert File inserts an admissible source construction into the current document completely or not at all; admissibility (`DQ-F3-14`) is decided before any mutation; it never replaces or overwrites existing objects; it keeps the `D1` provenance and notice and never scales coordinates", and record that item 3 names the accepted containers, not a promise to import every content type (recommended)** | Insert File becomes verifiable; some documents are rejected with a diagnostic |
| A13-2 — keep item 4 as written (inherited semantics) | contradicts the fail-closed direction (`CF-F3-1`, `-3`, `-5`) |
| A13-3 — defer Insert File (surface shows Apply Template only) | debt not closable as specified; contradicts item 2 |

**Recommendation.** A13-1. **Debt.** The debt closes only with the amended
item approved (`DQ-F3-20`).

### DQ-F3-14 — Admissibility matrix and exclusions

**Nature.** AUTHOR DECISION on CHARACTERIZATION EVIDENCE.

| Alternative | Consequence |
|---|---|
| **A14-1 — whitelist of (object class, parent algorithm) pairs with the statuses of the assessment §4: elementary 2D types `SUPPORTED`; Locus V2 family, Spline V2, `LinearDimension`, the free 3D point `SUPPORTED WITH CONDITIONS`; everything else `NOT ESTABLISHED` → rejected (recommended; Design R)** | realistic CeDG constructions insert (`FQ-1`); every admitted row has a test |
| A14-2 — Design S: elementary 2D types only | smaller matrix; Locus V2 documents rejected |
| A14-3 — blacklist (admit unless known bad) | violates "only verified types" |

**Recommendation.** A14-1, with the conditions: target allows Locus V2
creation (`RF-1`); dimension text in target units (`RF-2`).

### DQ-F3-15 — Macro support in the initial F3

**Nature.** AUTHOR DECISION.

| Alternative | Burden | Consequence |
|---|---|---|
| **M0 — reject any source with macros or macro-generated objects (recommended)** | none | documents using tools (including the curated library) are rejected with a diagnostic |
| M1 — admit macros whose command names are absent in the target; transfer only used macros, once | small: name check before mutation (`FQ-4`); one test set | unused source tools are not inserted (state it); identity-bearing payloads with macros remain refused (`RF-6`) |
| M1+ — M1 plus identical-definition reuse | medium: definition comparison (`OTI-F3R-3`) | admits documents built from the same library |
| M2 — explicit identity-aware remapping | large; shared macro semantics likely (U2) | later phase |

**Recommendation.** M0 for the initial F3; M1 is feasible and may be chosen
instead; M1+ and M2 deferred.

### DQ-F3-16 — Amendment of `DQ-D1-7`: dedicated insertion buffer

**Nature.** AUTHOR DECISION (amends an accepted `D1` decision).

**Accepted today (`DQ-D1-7`).** Insert File replaces the window copy buffer;
a later Ctrl+V keeps the insertion provenance.

| Alternative | Consequence |
|---|---|
| **B16-1 — Insert File uses a dedicated `GeoCeDGCopyPaste` instance; the window buffer and its provenance are never touched; the notice is emitted from the insertion provenance; a later Ctrl+V pastes what the user copied (recommended)** | no stale buffer, no placeholder labels (`FQ-6`, `FQ-7`); no inherited change |
| B16-2 — keep `DQ-D1-7` and add the U1-a `CopyPasteD` fix | later Ctrl+V repastes the inserted document with correct labels; inherited change; Classic changes |
| B16-3 — keep `DQ-D1-7` as is | `CF-F3-7` reachable |

**Recommendation.** B16-1. The `D1` guide sentence "a later paste of the same
objects keeps it" changes accordingly.

### DQ-F3-17 — Apply Template contract and the amendment of `F3-D2` item 5

**Nature.** AUTHOR DECISION (amendment).

| Alternative | Consequence |
|---|---|
| **T0 — verified object styles only (colour, line thickness, line style, point size, point style, fill opacity) from the template default of the object's default type; selection or else all objects; GeoCeDG-specialized objects skipped; no layers, captions, offsets, label visibility, fixed flag, visibility, conditions, colour functions, defaults or labeling style; one undo step; unsaved; pre-validated template (recommended)** | reversible (`FQ-9`); amends item 5 (defaults and labeling deferred) |
| T1 — T0 plus construction defaults | defaults are outside undo (`FQ-9`): needs a new undo integration, i.e. more design |
| T2 — inherited `TemplateHelper` with corrections | resets and non-undoable parts remain unless `TemplateHelper` changes (shared, forbidden here) |

**Recommendation.** T0; defaults and labeling style deferred to a later phase
with an undo design.

### DQ-F3-18 — Atomicity mechanism for Insert File

**Nature.** IMPLEMENTATION DESIGN requiring AUTHOR DECISION; OPEN TECHNICAL
ITEMS `OTI-F3R-1`, `OTI-F3R-2`.

| Alternative | Consequence |
|---|---|
| **A18-1 — admission on the helper; dry run on a scratch document loaded from the target XML; real commit through the dedicated buffer; post-verification (count, pairwise class/algorithm, closure, identity records); on any discrepancy `restoreCurrentUndoInfo()` and a "nothing was inserted" message; one undo step on success (recommended)** | all-or-nothing within established undo-restore semantics; extra cost of the dry run |
| A18-2 — as A18-1 without the dry run | cheaper; rollback becomes the only defense |
| A18-3 — transactional insertion inside the kernel | shared change (U2); not justified by evidence |

**Recommendation.** A18-1; the implementation entry gate proves `OTI-F3R-1`
(forced failure rolls back to the identical XML) and bounds `OTI-F3R-2`; if
either fails, stop for the author.

### DQ-F3-19 — Optional inherited corrections for Classic

**Nature.** AUTHOR DECISION.

| Alternative | Consequence |
|---|---|
| **U0 only — no inherited change in F3 (recommended)** | Classic keeps `CF-F3-1`, `-6`, `-7`, `-12` |
| + U1-a (`CopyPasteD.insertFrom` resets `duplicateLabels`) | Classic Ctrl+V after Insert File correct; one line; Classic regression test |
| + U1-b/U1-c (`AppD` load-result checks; `GuiManagerD` Cancel null checks) | Classic failure paths and Cancel correct; separate records |

**Recommendation.** U0 in F3; U1 corrections, if wanted, as a separately
authorized change with Classic evidence.

### DQ-F3-20 — Debt closure under an amended scope

**Nature.** AUTHOR DECISION.

| Alternative | Consequence |
|---|---|
| **C20-1 — `DEBT-R6PLUS-FILE-INSERT-SURFACE` closes with `F3` when the author approves the amendments `DQ-F3-13` and `DQ-F3-17` together with the candidate; the deferred items (macros beyond the chosen policy, template defaults and labeling, unverified types) are recorded as new named debts or enhancements (recommended)** | honest closure of a reduced scope |
| C20-2 — keep the debt open until the deferred items are delivered | `G` blocked longer |

**Recommendation.** C20-1. A reduced implementation is never reported as
complete fulfilment of the unamended `F3-D2`.

## 4. What stays open

- `DQ-F3-1` to `DQ-F3-20`: all pending.
- `OTI-F3R-1` to `OTI-F3R-5` and `OTI-F3-3` for the implementation entry gate.
- No class is frozen; F3 implementation and publication are not authorized.
