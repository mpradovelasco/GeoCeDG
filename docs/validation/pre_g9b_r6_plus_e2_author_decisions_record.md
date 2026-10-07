# PRE-G9B-R6-plus-E2 author-decision record

```text
ARTIFACT_KIND             = AUTHOR-DECISION RECORD (documentary)
AUTHOR_DECISION_SOURCE    = author instruction of 2026-10-07 ("FREEZE AUTHOR DECISIONS AND
                            FOCAL CHARACTERIZATION OF NORMATIVE DIMENSION TEXT")
DOCUMENTARY BASE          = T_R6PLUS_E2_PREP 6ad853c618da8f07a279b266c83002c145c6292c
                            tree 8fb6517b2519a6fa636b473a95432e5af7036483
                            (STATIC verification-b355b147258849beb498a541293a4fd9,
                            ACCEPTED / COMPLETE, 3/3); not amended
PUBLISHED BASE            = P_R6PLUS_E1_P_X1 dc63b0e55f12eb96d3aea359db4476cbda6c89dc
                            tree 8b2b5027a2119f996de8ca5af91b03ce32731a8c
PRE-G9B-R6-plus-E2        = PREPARED — AUTHOR DECISIONS FROZEN — IMPLEMENTATION NOT AUTHORIZED
IMPLEMENTATION            = NOT AUTHORIZED
selfApproved              = false
passClaimed               = false
```

This record transcribes the author's decisions on `DQ-E2-1` to `DQ-E2-11`. It
is their authority; it supersedes the recommendations of the canonical prompt
and of the reconciled design candidate wherever they differ. It authorizes no
implementation. The decisions are recorded as given; where the instruction
delegates a detail to the preparation, the preparation's resolution is marked
**PREPARATION RESOLUTION** and stays subject to the author's review of this
candidate. Machine-readable mirror:
[`pre-g9b-r6-plus-e2-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-author-decisions.json).

## Decisions

| ID | Author decision (2026-10-07) |
|---|---|
| `DQ-E2-1` | **β — composition of existing output types — AUTHOR APPROVED FOR E2 DESIGN.** No `GeoDimension` type in the planned implementation. A new `GeoElement` type or XML element type is an escalation condition. |
| `DQ-E2-2` | `Offset` is a **mandatory** explicit construction input; `Overshoot` and `Gap` are optional, **0 when omitted**. Signatures: `AlignedDimension(<Point A>, <Point B>, <Offset> [, <Overshoot>, <Gap>])` and `LinearDimension(<Point A>, <Point B>, <Direction>, <Offset> [, <Overshoot>, <Gap>])`. Desktop tools may supply captured values for `Overshoot` and `Gap`; the command invents no viewport-, DPI-, unit- or length-dependent default. A deterministic output order is frozen in the preparation (below). |
| `DQ-E2-3` | **AUTHOR APPROVED.** Semantic numeric output = model-unit measure, never dependent on `constructionUnit`, `presentationUnit`, `drawingScale`, viewport, zoom or DPI. Displayed value = `format(display(L)) + suffix(effP)` with `display(L) = L · metresPerUnit(effC) / metresPerUnit(effP)`; with `UNSPECIFIED_MODEL_UNIT`, `format(L)` and no suffix. Example: `constructionUnit = cm`, `presentationUnit = mm`, `L = 12` → number `12`, text `120 mm`; `presentationUnit = m` → number `12`, text `0.12 m`. The geometric algorithm never reads `UnitState` during geometric `compute()`; unit changes refresh presentation only; no presentation string is parsed back into geometry. |
| `DQ-E2-4` | **B1 — pin the side / orientation choice explicitly.** No live horizontal/vertical side classification that can flip at 45°. The chosen side becomes explicit construction state/input; dragging measured geometry never moves the dimension silently to the opposite side. No hidden heuristic state. The preparation defines its minimal exact representation (below). |
| `DQ-E2-5` | **E2-local direction authority.** No dependency on `F1`. `GeoLine.getDirection(...)` and the vector coordinates; never `getDirectionInD3` as semantic authority. The direction sign does not change the absolute projected measure; orientation and placement stay deterministic. |
| `DQ-E2-6` | The proposal "`KNOWN LIMITATION — horizontal centred text`" is **not accepted**. **NORMATIVE-STYLE ALIGNED DIMENSION VALUE — AUTHOR REQUIREMENT — IMPLEMENTATION SEAM TO BE CHARACTERIZED.** The value is parallel to its dimension line, centred longitudinally, normalized so that it is never upside down, not crossed by the dimension or extension lines, oriented by the dimension figure rather than screen axes, with zoom, DPI and viewport never semantic authority, preserved by save/reopen, and with explicit validated export behavior. Not yet decided that it needs a new `GeoElement`. Result of the authorized focal characterization: [DQ-E2-6 addendum](pre_g9b_r6_plus_e2_dq6_aligned_text_addendum.md). |
| `DQ-E2-7` | Canonical commands **`AlignedDimension`** and **`LinearDimension`**; Spanish aliases **`CotaAlineada`** and **`CotaLineal`**. `DirectDimension`, `AxisDimension`, `directDimension` and `axisDimension` are **not** registered as canonical names or aliases; they belong only to historical and user macro space. No silent rename, deletion, migration or reinterpretation of user macros. This resolves the native-naming side of `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`; the implementation still validates coexistence. |
| `DQ-E2-8` | **`verificationClass = INTEGRATED_PHASE`**, acceptance by a registered `PHASE` plus `INTEGRATION`, assuming β and existing XML and output types. A new `GeoElement`, a new XML element type or materially new global serialization semantics → **STOP** and request a `GLOBAL_IMPACT` review. |
| `DQ-E2-9` | Picture and rendered export preserve the complete figure. PSTricks, PGF/TikZ and Asymptote preserve the arrows, the text positioning and the accepted orientation through a bounded GeoCeDG export adaptation, with no silent loss. DXF gains **no** `DIMENSION` entity in `E2`; when the existing path exports only the geometric segment subset, that is recorded as a durable `E2` export limitation; no claim of full DXF dimension semantics. |
| `DQ-E2-10` | Surface frozen, subject only to revalidated current pins: commands `AlignedDimension`, `LinearDimension`; aliases `CotaAlineada`, `CotaLineal`; action ids `measure.aligned-dimension`, `measure.linear-dimension`; modes 142 and 143; group `construction-metrics`; Construction menu. Current-base counts only (125 actions at the base; the `P0` count is stale). |
| `DQ-E2-11` | The placement offset comes from the placement click and is immediately converted into the explicit model-space `Offset` parameter. `Overshoot` and `Gap` are 0 when omitted by the command; the tool may capture useful non-zero values once at creation; with a physical unit and `drawingScale` it may derive them from technical-drawing paper magnitudes converted once to model units; with `UNSPECIFIED_MODEL_UNIT` it must not pretend a millimetre paper relation; a view-derived fallback is creation-time UI input only and is materialized at once as an explicit model-space value. After creation: no live viewport, DPI, `drawingScale` or geometric `UnitState` dependency. |

## Preparation resolutions of delegated details

**PREPARATION RESOLUTION — output order (`DQ-E2-2`).** For both commands:

```text
output 0  value                GeoNumeric   model-unit measure (semantic)
output 1  dimension line       GeoSegment   arrow endings at both ends
output 2  extension line at A  GeoSegment
output 3  extension line at B  GeoSegment
output 4  presentation text    GeoText      plain value string (DERIVED_TRANSIENT)
```

**PREPARATION RESOLUTION — minimal exact representation of the pinned side
(`DQ-E2-4`).** No extra input is needed. The side is fully determined by
inputs that are already explicit, ordered and serialized in the command
element:

```text
AlignedDimension: n̂ = rot90((B − A)/|B − A|)   the left normal of the ordered pair A → B
LinearDimension : n̂ = rot90(d̂)                 d̂ = normalized getDirection() of the line,
                                                or the vector coordinates
dimension line   = { X : ⟨X − A, n̂⟩ = Offset }   the sign of Offset selects the side
```

`<input a0="A" a1="B" …>` fixes the order and `Offset` fixes the signed
distance, so the side is construction state. Under continuous motion of `A`,
`B` or the direction the normal moves continuously and never flips; the only
singular configurations are the defined degeneracies (`A = B`, a zero
direction). The Desktop tool chooses the sign of the captured `Offset` from the
placement click (the user's choice of side) once at creation; no rule is
re-evaluated afterwards. The presentation text's reading orientation (`DQ-E2-6`)
is a separate, stateless typographic rule and changes no construction state.

**PREPARATION RESOLUTION — creation-time magnitudes (`DQ-E2-11`).** Proposed
values, to be confirmed or changed in the authorization instruction: paper
overshoot 2 mm and gap 1 mm, converted once with the effective construction
unit and the session `drawingScale`; view fallback 8 px and 4 px converted once
with the view scale; both materialized as free hidden auxiliary numbers.

## What this record does not decide

- No implementation, phase registration or verification run is authorized.
- `DQ-E2-6` is a requirement; its seam is the result of the focal
  characterization and stays subject to the author's review of this candidate.
- No observation other than the naming side of
  `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` is resolved.
