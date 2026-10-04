# PRE-G9B-R6-plus-C author-decision record

```text
RECORD_KIND              = AUTHOR_DECISIONS (preparation and reconciliation instructions)
                           + DECISION QUESTIONS (DQ-C1 to DQ-C17)
DECISION_DATE            = 2026-10-04 (preparation; reconciliation the same day)
DECIDED_ON_BASE          = e613502831e3b412780d69424d4a1e433a4ae688
                           tree 48b00681a727ea4bd8768c3e9b0159bc01573ff0
                           (P_R6PLUS_A2, published A-2 closeout)
PREPARATION_BASE         = the same commit (branch phase/pre-g9b-r6-plus-c-prompt, local)

PRE-G9B-R6-plus-C        = PREPARED — NOT AUTHORIZED
selfApproved             = false
authorApproved           = false   (no C candidate exists)
implementationAuthorized = false   (C and every later subphase)
passClaimed              = false
productPhaseEffect       = NONE
DQ-C5, DQ-C7             = AUTHOR-DIRECTED (2026-10-04); confirmation at authorization
DQ-C1..C4, C6, C8..C17   = PENDING — NOT AUTHOR DECISIONS
```

This record preserves, versioned, the decisions that the author's instruction
of 2026-10-04 fixed for the preparation of `PRE-G9B-R6-plus-C` (§1, §2), the
author's reconciliation instruction of the same day (§1.1), and the decision
questions (§3). It is the authority for §1, §1.1 and §2, and for the author
directions recorded in `DQ-C5` and `DQ-C7`. **Every other row of §3 is a
question, not a decision**: it is `PENDING`, and its recommended default is the
preparation's proposal with no authority until the author disposes of it. Its
machine-readable mirror is
[`pre-g9b-r6-plus-c-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-author-decisions.json).

The instruction authorized only the documentary preparation of the canonical
[`C` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-c-2d-export-completion.prompt.md),
the [characterization report](pre_g9b_r6_plus_c_preparation_characterization_report.md)
and its mirror, this record and its mirror, and the status updates they strictly
require, on the exact published base above. It stated that it **does not
authorize the implementation of `C`**: no product code, no product test except
scratch-only probes outside the tracked tree, no normative export specification
amended into an in-force `C` state, and no `PHASE`, `INTEGRATION` or `FINAL` as
implementation evidence. Execution needs a further explicit author instruction
that names the exact prepared candidate or base.

## 1. Frozen semantic inputs

The instruction fixes these inputs as accepted; `C` consumes them and does not
reopen them. Each is stated once in its governing record, which this record
cites and does not restate differently.

| Input | Fixed content | Governing record |
|---|---|---|
| `ExportArea` (`B`) | one authoritative session `ExportArea`; the accepted producer precedence and behavior; LaTeX and every other `C` exporter consume it; no recreated selection-rectangle authority; a retained LaTeX bound edit updates the same `ExportArea` through the `MANUAL` producer | [B author-decision record](pre_g9b_r6_plus_b_author_decisions_record.md) `AQ-X2`; [B closeout record](pre_g9b_r6_plus_b_closeout_record.md) |
| units (`D0`/`D1`) | `constructionUnit` gives the physical meaning of one model unit; `presentationUnit` only controls expression; `output(L) = L · fb(constructionUnit) · a / b` for `drawingScale = a:b`; a `presentationUnit`-only change never changes physical size; coordinates and model geometry are never converted; `UNSPECIFIED_MODEL_UNIT` never means cm | [unit-system specification](../../geocedg/specs/units/unit-system.md) §12, §13, §15, §18.2 |
| hidden layers (`A-2`) | `hiddenLayers` is document-presentation state; `effectiveVisible` is presentation, not geometry; `C` consumes the accepted visibility semantics and modifies no object visibility, geometry, DAG or identity | [A author-decision record](pre_g9b_r6_plus_a_author_decisions_record.md) `AQ-L3`; [A-2 closeout record](pre_g9b_r6_plus_a2_closeout_record.md) |
| semantic curves | `GeoLocusV2` and Spline V2 semantics, branches, components, domains, revisions and semantic evaluation remain kernel authority; export code is a read-only consumer; render tessellation is never semantic export authority | `AGENTS.md` §11, §13; [DXF curve fidelity](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md) §1; ADR 0014 |
| `drawingScale` (`D0`) | `a:b`, positive integers, normal form `(a/g):(b/g)`, default `1:1`, `SESSION`, not serialized, not undoable, reset to `1:1` on New and Open | [unit-system specification](../../geocedg/specs/units/unit-system.md) §12 |

### 1.1 Reconciliation instruction (2026-10-04)

The author reviewed the prepared package and decided:

```text
T_R6PLUS_C_PROMPT  = 5ad96304ae9718c296db753b4822130cf3d99b61
                     tree 1596b5153681828c9c3206b98ca9ebd31505e765
                     acceptable in general; kept intact (not amended, not rewritten)
D_R6PLUS_C_PROMPT  = a linear documentary reconciliation descendant of 5ad96304,
                     without product code, product tests, normative C
                     implementation amendments or verifier changes
implementation of C = NOT AUTHORIZED
```

Scope of the reconciliation, and nothing else:

1. `DQ-C5`: the prepared wording is superseded by the author direction on local
   admissibility versus global completeness, with a characterized minimum
   result and report model and no kernel state (§3).
2. `DQ-C7`: the prepared recommendation is too broad; `drawingScale` resets only
   on a successful effective document transition, characterized on the actual
   lifecycle seams, with the determination of whether a `.ggt` or tool-save
   workflow invokes a clearing `setXML` (§3).
3. `DQ-C13`: the recommendation "DXF ignores `ExportArea`" is not accepted;
   a focused characterization of geometric clipping, participation filtering and
   a typed hybrid, keeping semantic domain, approximation work and area rule
   separate, with compatibility impact, examples, a revised recommendation and
   test obligations; the author decision is not taken by the preparation (§3).
4. `DQ-C9` stays pending; its recommended implementation constraints are
   amended (§3).
5. `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` is not fixed for
   `ExportArea` or the paste notice.

Verification of the reconciliation: prompt contract, JSON, links, EOL,
`git diff --check` and the same documentary `STATIC` as the first package.
No push, no merge, no authorization of `C`.

## 2. Fixed preparation scope

### 2.1 Verification class

```text
VERIFICATION_CLASS  = INTEGRATED_PHASE        (planning classification; re-characterized
                                               by the preparation and confirmed)
PLANNED_ACCEPTANCE  = registered PHASE + INTEGRATION on the same exact candidate
FINAL               = not required because D1 or A-2 used FINAL
```

The expected impact reasons named by the instruction: the shared LaTeX export
package; the picture-export physical-sizing path; the DXF header, model and
sidecar amendments; three LaTeX exporters; `ExportArea`; the `D1` unit state;
`A-2` effective visibility; the existing G9X1 approximation and fidelity
infrastructure. No document serialization is planned. If live evidence implies
document serialization, global renderer semantics or another impact
incompatible with `INTEGRATED_PHASE`, the preparation stops and proposes a
reclassification; it never reclassifies silently.

### 2.2 Mandatory characterization

`C1` export-route map; `C2` `drawingScale` lifecycle; `C3` engineering-scale UI;
`C4` physical picture and vector sizing; `C5` EMF physical quantization and the
FreeHEP path precision; `C6` DXF units; `C7` DXF specification and verifier
coherence; `C8` DXF visibility semantics; `C9` `ExportArea` in LaTeX and DXF;
`C10` semantic Locus V2 and Spline V2 export; `C11` LaTeX semantic-curve
tolerance; `C12` per-format semantic output; `C13` `AQ-C1` exact Spline V2;
`C14` invalid, open and incomplete semantic curves; `C15`
`OBS-R6PLUS-EXPORT-PREVIEW-ABSENT`; `C16` non-native document replacement and
`drawingScale`; `C17` unspecified-unit mode; `C18` Classic and Web boundary.
Results: prompt C1–C18 and the characterization report.

### 2.3 Preferred architecture

A shared GeoCeDG export package owns unit-aware neutral export metadata, the
read-only semantic curve export adapter, approximation evidence reused from
G9X1 and format-independent export computations; existing shared GeoGebra
exporter seams change minimally and only for dispatch and format integration,
with no GeoCeDG product policy in generic kernel geometry; Desktop GeoCeDG owns
the `drawingScale` holder and lifecycle, the export UI, the engineering-scale
input, the physical and non-physical presentation, warnings, reports and
product routing; the kernel gains no geometric semantics and is not an export
formatter. A change of Locus V2 or Spline V2 semantic identity, domains, DAG or
persistence is a stop.

### 2.4 Invariants, corpus, verification and amendment plan

The invariants (coordinates, DAG, identities, Locus V2 components, Spline V2
semantics, no render tessellation as authority, no viewport, zoom or DPI as
model-unit authority, `presentationUnit` size invariance, `drawingScale` as
interpretation only, hidden layers as presentation only, one `ExportArea`, no new
serialization, no undo state for `drawingScale`, no temporary construction
objects, determinism), the mandatory compatibility corpus, the verification
design (focal, physical-size, agreement, visibility, adapter, per-format, DXF,
G5/G9X1, `B`, `D1`, `A-2` and Classic regressions; Checkstyle; upstream
boundary; the official JUnit-inventory updater and registry pins;
`git diff --check`; guide and specification validation) and the requirement of
an exact, non-operative normative amendment map are encoded in the prompt's
*Objective*, *Geometric invariants and degeneracies*, *Required tests and
commands* and *Required design/specification*.

### 2.5 Forbidden scope and stop conditions

The forbidden scope (document-unit redesign; new document serialization; `A-2`
layer-semantics redesign; `F2` working-layer direct entry; `F3`; Apply Template
re-layering; `E1`, `E2`, `E3`; `F1`–`F3` implementation; 3D STL, Collada and AR
units; 3D hidden layers; construction-protocol rollback debt; a general solution
of `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`; manual
`ExportArea` drag; exact spline authority not justified by the semantic model)
and the stop conditions are encoded in the prompt's *Explicitly forbidden scope*
and *Stop conditions*.

### 2.6 Stop state

```text
PRE-G9B-R6-plus-C        = PREPARED — NOT AUTHORIZED
implementationAuthorized = false
authorApproved           = false
selfApproved             = false
passClaimed              = false
```

## 3. Decision questions (`DQ-C1` to `DQ-C17`)

`DQ-C5` and `DQ-C7` carry the author directions of 2026-10-04 and are marked
`AUTHOR-DIRECTED`; every other row is `PENDING`. "Recommended default" is the
preparation's proposal; it is not an author decision. "Before authorization"
states whether the implementation cannot start without the disposition.
Evidence references are prompt sections and the characterization report.

### `DQ-C1` — exact Bézier output for `SplineV2` of degree ≤ 3 (`AQ-C1`)

- **Question.** Does `C` write `SplineV2` of degree 3 as exact cubic Bézier
  paths (for example PGF `.. controls ..`) instead of the certified approximation?
- **Alternatives.** (a) No special case: every `LocusV2`, `SplineV2` included,
  uses the one approximation contract. (b) Exact cubic Bézier for degree 3
  converted from the structural truncated-power numerators and the binary64
  knots with rational arithmetic, disclosed as exact relative to the stored
  snapshot. (c) Bézier converted from the rounded power spans (approximate,
  rejected: it would look exact without being so).
- **Evidence.** C13: degree is 3 to 12, so only the cubic case qualifies; the
  model is a numerically solved interpolant; the rounded spans are "derived
  evaluation data, not exact coefficient authority"; DXF writes an approximate
  `LWPOLYLINE`; no `SPLINE` exists anywhere.
- **Recommended default.** (a).
- **Consequence.** (a): `T-SPLINE` proves degrees 3 and above through one
  contract. (b): a rational conversion, an exactness vocabulary distinct from
  `EXACT` in the fidelity sense, per-dialect tests and a statement of what it is
  exact relative to.
- **Before authorization.** Yes.

### `DQ-C2` — DXF policy for objects on hidden layers

- **Question.** How does DXF treat objects on a GeoCeDG hidden layer?
- **Alternatives.** (1) Omit them before preflight, with a distinct counted
  presentation diagnostic (not `UNSUPPORTED` or `INVALID`) and a report line.
  (2) Emit them unchanged on their `0`/`GEOCEDG_L<n>` layer and write that layer
  off in the `LAYER` table (negative color group `62`), with a report line.
  (3) A per-export user choice with one of the two as default.
- **Evidence.** C8: the accepted rules "hidden sources remain included but
  visibly reported" (fidelity spec §7) and "hidden object → `60 = 1`" (G5) keep
  hidden content and mark it; picture and LaTeX are renderings and follow
  `effectiveVisible`; the layer identity survives in the DXF layer name; the
  source layer, the GeoCeDG hidden layer and the DXF layer state stay distinct
  facts. The ability of DXF to carry an off layer is not itself the reason.
- **Recommended default.** (2): it keeps the accepted DXF inclusion contract and
  the complete-construction population, encodes a presentation state as a
  presentation state, and needs no population-rule change. Its risk is a
  consumer that ignores layer state and shows the content; (1) guarantees
  absence but drops document geometry from an interchange file.
- **Consequence.** (2): `LAYER` records with negative `62` for hidden layers;
  fingerprint includes the hidden set; preflight and sidecar mention hidden
  layers; spec §7 and the foundation's layer lines amended. (1): a new
  population exclusion, counts, spec amendment, and a rule for explicit
  selections of hidden-layer objects. Tests `T-DXF-VISIBILITY`,
  `T-A2-REGRESSION`, `T-DXF-STALENESS`.
- **Before authorization.** Yes.

### `DQ-C3` — individually hidden DXF objects and `OBS-R6P-DXF-VISIBLE-CONTRACT`

- **Question.** Do individually hidden objects stay in DXF, and how is the
  contradiction with the action's selection-contract reference
  `geocedg.dxf-exportable-visible-geometry` resolved?
- **Alternatives.** (a) Keep include + `60 = 1` + report; rename the reference
  to a truthful identifier (for example `geocedg.dxf-exportable-geometry`) and
  describe it. (b) Keep the behavior and keep the name, documenting that
  "visible" refers to DXF visibility state. (c) Exclude invisible objects, which
  amends the G5 mapping and the fidelity spec §7.
- **Evidence.** C8; the reference is used at
  `apps/geocedg/application-profile.yml:2234` and defined nowhere.
- **Recommended default.** (a).
- **Consequence.** (a): a profile change with its pins (profile tests,
  `workspace-profile.product`), no behavior change. (c): normative amendments
  and new population tests.
- **Before authorization.** Yes.

### `DQ-C4` — semantic-curve tolerance in LaTeX

- **Question.** Who owns the LaTeX approximation tolerance, what is its default,
  how is it entered and in which units?
- **Alternatives.** (a) One model-coordinate chord-tolerance field per LaTeX
  dialog, default `0.001`, positive finite, not converted by units, budgets as
  the DXF defaults, not session state. (b) One session tolerance shared by the
  LaTeX dialogs and DXF. (c) A physical tolerance on the output (for example
  0.01 mm on paper) converted through the unit and `drawingScale` into model
  units at export time.
- **Evidence.** C11: DXF uses a model-coordinate tolerance, default `0.001`,
  `ESTIMATED_ERROR` only, deterministic budgets; the builder is reusable.
  Option (c) is the only one whose visual fidelity is independent of the unit,
  but it couples approximation to units and scale and has no precedent.
- **Recommended default.** (a); a unit change never changes the numeric
  tolerance silently.
- **Consequence.** (a): a field and validation in three dialogs and the API
  route of `DQ-C16`; tests for validation, determinism and work limits.
- **Before authorization.** Yes.

### `DQ-C5` — invalid, partial and incomplete semantic curves in LaTeX

- **Status.** `AUTHOR-DIRECTED (2026-10-04)`: the author superseded the prepared
  default with the direction below. The detailed contract is prompt C14; the
  authorizing instruction confirms it. Implementation not authorized.
- **Author direction.** A locally certified, valid and unambiguous semantic
  branch component may be emitted even when the source or global export result
  is incomplete or completeness is `NOT_ESTABLISHED`; this never implies global
  completeness. Branch and component identity, explicit gaps and
  discontinuities are preserved; only certified valid components are emitted;
  nothing is interpolated across an invalid or unresolved interval; failure
  evidence is never omitted silently; the generated LaTeX carries approximation
  and completeness comments; the export UI or report states incomplete coverage
  visibly; stale-source, missing-domain, work-limit and tolerance failures are
  classified explicitly. The export result distinguishes at least `COMPLETE`,
  `INCOMPLETE_WITH_CERTIFIED_COMPONENTS` and `REJECTED / NO_ADMISSIBLE_OUTPUT`.
  G9X1's partial-output-disabled rule is not applied mechanically to LaTeX.
- **Prepared minimum model (C14).** Per-component outcomes are the existing
  `SourceExportOutcome` (`Fidelity`, `Reason`, `ComponentAddress`,
  `SemanticCoverage`, `ApproximationEvidence`); one derived classification is
  added at source level (`COMPLETE`, `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`,
  `NO_ADMISSIBLE_OUTPUT`) and at export level (`COMPLETE`,
  `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`, `REJECTED / NO_ADMISSIBLE_OUTPUT`), in
  the GeoCeDG export package, with no kernel state. `STALE_SOURCE_REVISION` and
  an unestablishable tolerance or guarantee reject the export; `MISSING_DOMAIN`,
  `DISCONTINUITY_UNRESOLVED`, `NON_FINITE`, `DEGENERATE_SOURCE` and `WORK_LIMIT`
  fail the component or the source and leave the export incomplete with
  evidence; gaps are separate paths. A source with no admissible output beside
  other admissible content makes the export incomplete; nothing admissible at
  all rejects it.
- **Evidence.** C10, C14; G9X1 keeps per-component outcomes and only its
  writability rule is strict (`GeometryExportPreflight.java:97-99`).
- **Boundary.** DXF keeps its accepted strict rule and sidecar (fidelity spec
  §7, ADR 0014 Decision 6); the shared adapter decides no writability.
- **Consequence.** `T-LATEX-FAILURES`, `T-EXPORT-RESULT-MODEL`; the exact
  comment and report form stays `DQ-C12`.
- **Before authorization.** The authorizing instruction confirms the prepared
  model or adjusts it (for example the export classification of a source with
  no admissible output beside other content).

### `DQ-C6` — `drawingScale` input range, normalization and extreme outputs

- **Question.** Which pairs can a user enter, how are they normalized, and what
  happens to outputs a format cannot represent?
- **Alternatives.** (a) Positive integers `1..2^31−1` (the `int` range; no other
  bound), normalized by gcd on acceptance, shown as `a:b`, with editable presets
  `1:1 1:2 1:5 1:10 1:20 1:50 1:100 1:200 1:500 1:1000 2:1 5:1 10:1`; invalid
  input refused with a message and the previous value kept; outputs beyond a
  format limit or below one device unit fail explicitly. (b) A fixed preset list
  only. (c) A smaller numeric bound.
- **Evidence.** C3: no current dialog bounds its values; unit-system §12 defines
  the semantics for every positive pair and allows `C` to bound the integers; C4
  extreme cases.
- **Recommended default.** (a).
- **Consequence.** `T-SCALE-VALUE`, `T-SCALE-UI`, `T-EXTREMES`.
- **Before authorization.** Yes.

### `DQ-C7` — owner and lifecycle of `drawingScale`

- **Status.** `AUTHOR-DIRECTED (2026-10-04)`: the prepared recommendation (reset
  in the `clearConstruction` override and in `commitLoadedDocument`) was judged
  too broad and is superseded. The detailed contract is prompt C2.
  Implementation not authorized.
- **Author direction.** `drawingScale` stays window/session export state,
  default `1:1`. It resets only on a successful effective document transition:
  a completed File → New, or a successful full-document replacement or Open,
  independently of transport. It stays unchanged through undo, redo, redefine,
  rollback, macro and tool processing, generic merge and `evalXML`,
  non-document `setXML` and temporary construction clearing. Lifecycle
  semantics decide, not `clearConstruction` or method names; the D1/A-2
  successful-transition hooks are reused where they exist. A `.ggt` or
  tool-save workflow that invokes a clearing `setXML` must not reset it. The
  separate `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` is not
  fixed for `ExportArea` or the paste notice.
- **Prepared seams (C2).** `E1` the completed-New branch of `AppGeoCeDG.fileNew`;
  `E2` `nativeDocumentLoadCommitted`; `E3` `documentReplacementCommitted`; `E4`
  a successful `runDocumentLoad` (`loadXML(String)` and `loadExistingFile`);
  `E5` the API `setXML(String)` document replacement, which has no
  distinguishing hook at the base and needs one GeoCeDG-gated `GgbAPID` marker
  observed by `AppGeoCeDG.setXML` when the parse completes; `E6` `reset()`
  without a current file (graphics reset icon, API `reset`), a blank-document
  replacement that `D1` and `A-2` already treat as a new document.
- **Tool workflows (determined).** Yes, a current tool-save workflow invokes a
  clearing `setXML`: Create New Tool with an existing command name → Replace,
  and the save from a macro-edit window, both reload the document window with
  `appToSave.setXML(appToSave.getXML(), true)`
  (`desktop/gui/dialog/ToolCreationDialogD.java:223-233`, `:265-294`; shared
  `common/gui/dialog/ToolCreationDialogModel.java:244-245`, `:272-291`). Probe
  `P-C7-TOOL-REPLACE` proves that this reload reaches the `A-2` commit (working
  layer 2 → 6, construction identical). Under the prepared seams it would have
  reset `drawingScale`; under `E1`–`E6` it does not, because no successful
  transition event fires on that path. The macro-edit window
  (`App.openEditMacro`) runs in a fresh window; `.ggt` open, save and delete and
  the GeoCeDG tool library never reach a transition event.
- **Open detail.** Whether `E6` (reset to a blank document without a current
  file) counts as a successful effective transition; default yes.
- **Consequence.** `T-SCALE-LIFECYCLE` with every positive and negative route of
  C2.
- **Before authorization.** The authorizing instruction confirms the seams and
  `E6`.

### `DQ-C8` — export UI per unit state

- **Question.** Which controls and labels do the picture, print and LaTeX
  dialogs show for a physical and for an unspecified construction unit?
- **Alternatives.** Physical: (a) `a:b` replaces *Scale in cm*; *Fixed size*
  (zoom-coupled) removed; *Size in pixels* kept for PNG as an explicit
  non-physical device mode; LaTeX unit and size become derived displays.
  (b) Physical documents offer `a:b` only. Unspecified: (a) the host controls
  relabelled as a non-physical device scale with a notice that the document has
  no construction unit; LaTeX fields as non-physical device parameters.
  (b) Physical-size modes disabled until a unit is declared.
- **Evidence.** C3, C17; unit-system §13 and `DQ-D0-11` allow a clearly labelled
  device mode.
- **Recommended default.** (a) for both states; exact strings in `en` and `es`
  proposed by the implementation and listed in its report.
- **Consequence.** `T-SCALE-UI`, `T-UNSPECIFIED`; guide updates.
- **Before authorization.** Yes.

### `DQ-C9` — EMF `rclFrame`

- **Status.** `PENDING`. The author amended the implementation constraints of
  the recommendation on 2026-10-04; this does not authorize implementation.
- **Question.** How does `C` make the EMF physical frame meet the §13 contract?
- **Alternatives.** (a) Device-reference scaling on FreeHEP's fixed 0.3125 mm
  unit, no FreeHEP change, documenting a frame error up to about 0.32 mm.
  (b) An opt-in exact frame in the FreeHEP EMF writer under the `B` provenance
  gate and records. (c) EMF declared device-only in GeoCeDG: no physical claim,
  labelled.
- **Author constraints if FreeHEP is modified (2026-10-04).** The change is
  opt-in for GeoCeDG; the default host and Classic behavior stays unchanged; the
  physical `rclFrame` derives directly from the requested physical output size;
  DPI does not determine the physical frame; each dimension is rounded to the
  nearest representable 0.01 mm and the unavoidable quantization of at most
  0.005 mm per dimension is documented; no wider FreeHEP redesign.
- **Evidence.** C5; probe `P-C-EMF`: the base frame is `D/81.28` times the
  labelled size; device-reference errors observed up to 0.12 mm on a 3 mm
  extent; nearest-0.01 mm errors ≤ 0.003 mm on every probed case.
- **Recommended default.** (b) within the author constraints, following the `B`
  PDF precedent; it is the only option that meets the contract within the
  format's own quantization. Because the `B` record made any further FreeHEP
  family change an explicit author decision, the disposition also confirms that
  it stays inside `INTEGRATED_PHASE`.
- **Consequence.** (b): a third-party source change with provenance,
  `THIRD_PARTY.md`, component-matrix and modified-files records, a byte-identity
  test of the non-opt-in writer, DPI-independence and nearest-0.01 mm frame
  checks in `T-PHYSICAL-PICTURE`.
- **Before authorization.** Yes.

### `DQ-C10` — `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT`

- **Question.** What is the disposition of the observation?
- **Alternatives.** (a) `NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED`: the Save
  preview exists only for an existing target in a chooser at least 600 px wide,
  by upstream design kept by the accepted `B` contract; the guides explain it.
  (b) `PRE_EXISTING_DEBT_WITH_C_OWNER`: GeoCeDG-gated preview of a new target
  name as well. (c) A preview pane inside the GeoCeDG Picture dialog (larger UI
  scope).
- **Evidence.** C15; probe `P-C-SAVE-PREVIEW`.
- **Recommended default.** (a).
- **Consequence.** (a): guide text and `T-PREVIEW` pinning the conditions.
  (b): a GeoCeDG-gated chooser change and tests; Classic unchanged.
- **Before authorization.** Yes.

### `DQ-C11` — `usm` DXF warning and sidecar schema

- **Question.** Beyond the fixed D0 rule, how is the `usm` export presented and
  how does the sidecar schema change?
- **Alternatives.** (a) Sidecar `schema_version` 2 with a `units` object
  (`construction_unit` token or `unspecified`, `meters_per_unit` canonical
  string per unit-system §8.4 or `null`, `insunits` integer);
  `source_unit`/`target_unit` carry the token; the warning is a preflight line
  plus the existing mandatory-sidecar confirmation; `usm` name and symbol are not
  exported. (b) Additive fields under `schema_version` 1. (c) A separate modal
  warning before the chooser.
- **Evidence.** C6; the manifest has no JSON Schema file; unit-system §4.4: name
  and symbol take no part in export semantics.
- **Recommended default.** (a).
- **Consequence.** `T-DXF-USM`, `T-DXF-SIDECAR`; manifest tests and spec §8.
- **Before authorization.** Yes.

### `DQ-C12` — form of the LaTeX disclosure

- **Status.** `PENDING`, narrowed by the `DQ-C5` direction, which requires both
  comments in the generated LaTeX and a visible statement in the export UI or
  report.
- **Question.** What exact form do the comments and the report take?
- **Alternatives.** (a) One header comment with the export classification, one
  comment per semantic source (fidelity, tolerance, guarantee, achieved
  estimate, components, coverage, failures), and a non-modal report area in the
  dialog. (b) The same comments with the report as a modal summary after
  Generate.
- **Evidence.** C12: the dialogs have no report area; each dialect has a
  comment syntax.
- **Recommended default.** (a).
- **Consequence.** `T-LATEX-SEMANTIC`, `T-EXPORT-RESULT-MODEL`; a GeoCeDG-gated
  report area.
- **Before authorization.** Yes.

### `DQ-C13` — how DXF consumes the authoritative `ExportArea`

- **Status.** `PENDING`. The author rejected the prepared default "DXF ignores
  `ExportArea`" (2026-10-04): it conflicts with the accepted `B`/`C` contract
  that the picture exporters, the LaTeX exporters and DXF converge on the one
  `B` `ExportArea`. The author also excluded assuming that clipping every DXF
  entity is correct. The full per-family matrix, the impact matrix and the
  examples are prompt C9-DXF; this section states the decision question.
- **Question.** Which rule lets the implementation truthfully state that DXF
  consumes the same authoritative `ExportArea` as the other `C` exporters?
- **Facts every alternative respects.** `ExportArea.resolve` always yields an
  area, falling back to `VISIBLE_VIEWPORT`; consuming that fallback would make
  the DXF zoom-dependent and break the G5 PASS clause, so every alternative
  treats it as "no explicit area" (model space as at the base) and consumes
  only `MANUAL`, `EXPORT_POINTS_EXPLICIT` and `EXPORT_POINTS_AUTOMATIC`; the
  area is resolved for the active 2D Graphics view; the semantic finite domain,
  the approximation domain and work, and the area rule stay separate, and the
  area never substitutes for a missing required semantic domain; the consumed
  area (bounds, producer, view) is reported, recorded in the sidecar when one is
  written, and enters the staleness fingerprint.
- **Alternatives.**
  - **A — geometric clipping.** Emit the part of every source inside the closed
    area. Consequences: `RAY` and `XLINE` become `LINE`; `CIRCLE` becomes 0–4
    `ARC`; ellipses become elliptic arcs; closed polygon boundaries become open
    polylines (region clipping would invent edges); clipped endpoints and angles
    are computed values (divisions, square roots), so `EXACT` in the G5 sense
    (no tolerance beyond text round trip) no longer holds; the exporter gains
    intersection solving (`AGENTS.md` §13); one source maps to several entities;
    a Locus V2 component cut at the area gets piece parameters from sampled
    coordinates, so its identity could no longer be truthful.
  - **B — participation.** The area decides which sources, and which certified
    components of a semantic source, participate; participating entities are
    emitted whole and exact; non-participating ones are reported as outside the
    area (a population outcome, not a fidelity failure). Predicate variants: B1
    the geometry meets the closed area; B2 its bounding box overlaps the area;
    B3 it lies wholly inside. Infinite `RAY`/`XLINE` that meet the area are
    emitted whole (infinite); under B3 they never participate. For a semantic
    component, participation is decided on its certified approximation enlarged
    by the achieved estimate. The meaning of the area for DXF is "the geometry
    that meets the area, whole", not "the drawing cut to the area"; the dialog,
    report, header comment and guide must say so.
  - **C — typed hybrid.** Clip some classes, filter the others. No class can be
    clipped without either a family change (`RAY`, `XLINE`, `CIRCLE`, closed
    polygon) or computed endpoints that need a new exactness class (`LINE`,
    `ARC`, `ELLIPSE`, polyline); semantic components cannot be clipped
    truthfully at all. A file would mix two meanings of the area.
- **Concrete examples** (`R = [0, 10] × [0, 10]`): segment (−1, 0)–(2, 1) → A
  `LINE` (0, 1/3)–(2, 1) with a rounded endpoint, B whole; ray from (5, 5) along
  +x → A `LINE` (5, 5)–(10, 5), B whole `RAY`; line y = 5 → A `LINE`
  (0, 5)–(10, 5), B whole `XLINE`; circle centre (10, 10), r = 3 → A `ARC`
  180°–270°, B whole `CIRCLE`; circle centre (5, 5), r = 20 → A nothing, B1 not
  participating, B2 participating; square (−2, −2)…(2, 2) → A open polyline
  (0, 2)–(2, 2)–(2, 0), B whole closed square; `LocusV2` y = x² on [−2, 2] → A
  the piece x ∈ [0, 2] cut at an interpolated vertex, B the whole component.
- **Recommended default.** **B1 participation**: explicit producers only;
  sources and certified components whose geometry meets the closed area are
  emitted whole and exact in construction order; the others are reported as
  outside the area (never silently); applied to complete-construction and
  current-selection requests alike (selection ∩ area, outside-area selected
  sources reported); a header comment and the sidecar record the area and the
  participation rule. Reasons: it is the only alternative that keeps every
  accepted G5/G9X1 contract (entity families, exactness, identity, handles per
  entity, the infinite-geometry rule), adds no geometric solving to the
  exporter (closed-form predicates only, no emitted computed geometry), keeps
  semantic component identity, and lets the implementation state truthfully
  that DXF consumes the same `ExportArea` authority with a documented,
  format-appropriate meaning. Sub-choices for the author: B1 versus B2 or B3;
  whether current selection is also filtered (default yes).
- **Compatibility cost of the recommendation.** No change without an explicit
  area (G5 and G9X1 corpora byte-identical; zoom invariance kept); documents
  with `Export_1`/`Export_2` now export a participation subset; a new
  outside-area outcome or diagnostic, counted in preflight and recorded in the
  sidecar; a header comment when an area is consumed; the area in the staleness
  fingerprint; handles renumber deterministically over the subset; spec
  amendments of the G5 foundation (area rule, population statement) and the
  fidelity spec §7 and §8. Cost of A or C: the same plus amendments of ADR 0005
  Decisions 5–6, the G5 exactness and infinite-geometry rules and the fidelity
  vocabulary, new sidecar sub-addresses for split entities, and no truthful
  clipping of semantic curves without a new kernel capability (a stop).
- **Test obligations.** `T-DXF-AREA` (prompt): the `VISIBLE_VIEWPORT` fallback
  gives the base bytes at every zoom; `MANUAL` and both `EXPORT_POINTS`
  producers; inside, crossing, outside and boundary-touching sources of every
  family, including infinite lines and rays, the area-enclosing circle and
  closed polygons; semantic components per component; both selection modes;
  outside-area reporting, header comment and sidecar record; the area of the
  active view; determinism and handle order; a missing semantic domain never
  replaced by the area; `T-DXF-STALENESS` for an area change after preflight.
- **Before authorization.** Yes.

### `DQ-C14` — FreeHEP five-significant-digit path coordinates

- **Question.** Does `C` act on `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION`?
- **Alternatives.** (a) No: an accepted writer limitation distinct from page and
  frame sizing, documented. (b) An opt-in path precision in the PDF writer.
- **Evidence.** C5.
- **Recommended default.** (a).
- **Consequence.** (a): none beyond documentation. (b): a further FreeHEP change.
- **Before authorization.** No (the default changes nothing); a disposition is
  still requested.

### `DQ-C15` — historical verifier pins

- **Question.** How are the G9X1 and G5 historical verifier pins kept coherent
  with the amended documents?
- **Alternatives.** (a) Keep the frozen G9X1 evidence and the G5 corpus pin
  (both stay true for unspecified documents); re-pin the live-document pins of
  `verify-g9x1-extended-dxf.ps1` (fidelity spec hash, guide literal) as
  recorded successors naming `C`, on the `TD-G9X1-HISTORICAL-PIN` precedent; no
  registry change. (b) Leave the historical verifiers failing on the amended
  documents. (c) Retire them (out of scope).
- **Evidence.** C7.
- **Recommended default.** (a).
- **Consequence.** `T-HISTORICAL-PINS`; one tools/agent data edit, no verifier
  architecture change.
- **Before authorization.** Yes.

### `DQ-C16` — non-dialog routes

- **Question.** How do `--export`, `ExportImage` and the JavaScript picture and
  LaTeX APIs follow the `C` contract?
- **Alternatives.** (a) Routes without an explicit scale follow the unit state
  and the session `drawingScale` (`1:1` in a fresh command-line application);
  explicit numeric arguments (`ExportImage` `scalecm` and `dpi`, API scale and
  DPI, `--dpi`, `--maxSize`) stay explicit device parameters, never labelled an
  engineering scale; LaTeX API routes use the export area, effective visibility
  and semantic curves like the dialogs. (b) Keep these routes as at the base,
  documented as an inconsistency.
- **Evidence.** C1; `B` routed them through the picture service; `ExportImage`
  is a GeoCeDG-modified GGBScript command in the capability matrix.
- **Recommended default.** (a).
- **Consequence.** `T-API-ROUTES`; GGBScript matrix rows and pins only if
  `ExportImage` behavior changes.
- **Before authorization.** Yes.

### `DQ-C17` — ADR 0005 Decision 3

- **Question.** How is ADR 0005 Decision 3 ("units explicitly unitless;
  `$INSUNITS` is 0"), which ADR 0032 does not amend, kept coherent?
- **Alternatives.** (a) A dated amendment section in ADR 0005 that keeps the
  original text and points to ADR 0032 Decision 8 and unit-system §15. (b) A new
  ADR superseding Decision 3.
- **Evidence.** C7; ADR 0032 "Related: ADR 0005 … not amended by this ADR".
- **Recommended default.** (a).
- **Consequence.** One documentary amendment in the `C` candidate, approved
  with it.
- **Before authorization.** Yes.

## 4. Authorization state

```text
PRE-G9B-R6-plus-C        = PREPARED — NOT AUTHORIZED
                           (prepared 5ad96304; reconciled by D_R6PLUS_C_PROMPT)
implementationAuthorized = false
DQ-C5, DQ-C7             = AUTHOR-DIRECTED (2026-10-04); confirmation at authorization
DQ-C1..C4, C6, C8..C17   = PENDING — NOT AUTHOR DECISIONS
OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT = RECORDED BY THE C RECONCILIATION —
                           A-2 BEHAVIOR — NOT C SCOPE — NOT FIXED
PRE-G9B-R6-plus-E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R6-plus-F3       = PLANNED — AUTHOR APPROVED — PUBLISHED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-R7               = DESIGNED — NOT AUTHORIZED
G9B                      = NOT AUTHORIZED
selfApproved             = false
```
