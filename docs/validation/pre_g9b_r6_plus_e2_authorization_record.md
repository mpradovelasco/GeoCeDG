# PRE-G9B-R6-plus-E2 implementation authorization record

```text
PRE-G9B-R6-plus-E2 = AUTHORIZED FOR IMPLEMENTATION
REPRESENTATION     = β (existing output types)
COMMANDS           = AlignedDimension / LinearDimension
VERIFICATION_CLASS = INTEGRATED_PHASE (frozen): registered PHASE + INTEGRATION
```

```text
selfApproved              = false
authorApproved            = false   (no technical candidate is approved by this record)
passClaimed               = false
implementationAuthorized  = true    (E2 implementation and technical verification only;
                                     E3, F1, F2, F3, G, PRE-G9B-R7, G9B: false)
productPhaseEffect        = NONE    (this record)
```

This record preserves the explicit author instruction of 2026-10-08, typed in
the session ("Ejecuta la autorización e implementación que te indico a
continuación"), titled "PRE-G9B-R6-plus-E2 — AUTHOR AUTHORIZATION AND
IMPLEMENTATION". It approves the prepared `E2` design and authorizes the
implementation and technical verification of `PRE-G9B-R6-plus-E2` only. It
changes no product, test, build, verifier, registry, specification, ADR or
serialization file. Machine-readable mirror:
[`pre-g9b-r6-plus-e2-authorization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-authorization.json).

## Entry verification

```text
live remote main = origin/main = local main
                 = dc63b0e55f12eb96d3aea359db4476cbda6c89dc   (P_R6PLUS_E1_P_X1)
                   tree 8b2b5027a2119f996de8ca5af91b03ce32731a8c
preparation chain = dc63b0e5 → 6ad853c618da8f07a279b266c83002c145c6292c (T_R6PLUS_E2_PREP)
                             → b1ee68c0f73b6c39fe8cd306e52f58bb66330c5b (decisions + DQ-E2-6)
                    linear, no merge; neither commit amended
implementation base = B_R6PLUS_E2_IMPL = b1ee68c0f73b6c39fe8cd306e52f58bb66330c5b
                      tree 7eabf6892ab00c6adc3b510e9ffac204ae13c7a8
implementation branch = phase/pre-g9b-r6-plus-e2-native-dimensions (local)
worktree = clean
```

## Author decisions frozen by the instruction

The instruction approves the [author-decision record](pre_g9b_r6_plus_e2_author_decisions_record.md)
and the [`DQ-E2-6` addendum](pre_g9b_r6_plus_e2_dq6_aligned_text_addendum.md)
with these statements, which prevail over every earlier text:

| Topic | Frozen disposition |
|---|---|
| representation (`DQ-E2-1`) | β approved; no `GeoDimension`, new `GeoElement` type, XML element type or semantic identity family; otherwise stop with `E2 REPRESENTATION ESCALATION REQUIRED` |
| names (`DQ-E2-7`, `DQ-E2-10`) | `AlignedDimension`, `LinearDimension`; ES `CotaAlineada`, `CotaLineal`; `DirectDimension`, `AxisDimension`, `directDimension`, `axisDimension` never registered; `Dimension` completely unchanged |
| signatures (`DQ-E2-2`) | `AlignedDimension(A, B, Offset[, Overshoot, Gap])`, `LinearDimension(A, B, Direction, Offset[, Overshoot, Gap])`; `Direction` = line or vector; `Offset` mandatory; `Overshoot = Gap = 0` when omitted; no hidden multiplier |
| output contract | value, dimension line, extension at `A`, extension at `B`, presentation text — part of the command contract; visibility and default labels documented |
| value (`DQ-E2-3`) | `|B − A|` or `|⟨B − A, unit(Direction)⟩|`, model units, independent of units, `drawingScale`, viewport, zoom, DPI |
| displayed value | `format(display(L)) + " " + suffix(effP)`; bare `format(L)` when unspecified; `cm`/`mm`, `L = 12` → 12 and `120 mm`; `m` → 12 and `0.12 m` without geometric recompute |
| unit boundary | no `UnitState` read in geometric `compute()`; refresh through the `D1` presentation listener; never parsed back; a presentation failure never invalidates the value; failure marker `?` plus the suffix where one exists |
| side (`DQ-E2-4`) | orientation authority = ordered inputs `A → B` (or the `Direction` authority); side authority = `sign(Offset)`; swapping `A` and `B` is a semantic input change; no 45° flip while dragging; no hidden side state |
| placement and capture (`DQ-E2-11`) | offset from the placement click, materialized at once; paper targets 2 mm overshoot and 1 mm gap converted once with a physical unit and `drawingScale`; no millimetre relation when unspecified; view fallback about 8 px and 4 px converted once; no live dependency afterwards |
| direction (`DQ-E2-5`) | `GeoLine.getDirection` and the vector direction; never `getDirectionInD3`; focused tests for `Line(A,B)`, parallel, perpendicular, `Line(P,v)`, vector, axes, ray and segment |
| aligned value (`DQ-E2-6`) | approved: the C4 strategy (plain text, drawing-time rotation from the projected dimension line, readable-angle rule in (−90°, 90°], presentation flip at the vertical recorded, nothing serialized); a minimal kernel seam identifying the dimension text and its line end points, no screen data in it; a GeoCeDG draw path for dimension texts only; hit testing on the rotated box; `Corner` behavior characterized and tested |
| figure, arrows, drag | explicit outputs; no guide line; no legacy `|AB|`-proportional geometry; existing arrow endings; dragging the line edits only `Offset` through a GeoCeDG Desktop seam, never `ChangeableParent` |
| export (`DQ-E2-9`) | picture complete at several DPI; PSTricks, PGF/TikZ, Asymptote preserve arrows, position, rotation, centring and value for dimension outputs only, compiled where the toolchain exists; DXF: no `DIMENSION` entity, segment subset recorded as a durable limitation |
| class (`DQ-E2-8`) | `INTEGRATED_PHASE` frozen; acceptance by a registered `PHASE` and `INTEGRATION` on one immutable candidate; no `FINAL` |

## Scope, stop conditions and boundaries

- Allowed: the areas listed in the instruction (§53): shared dimension
  algorithms and seam, command registration, `D1` presentation refresh, the
  GeoCeDG drawing seam, Desktop tools, profile, localization, owned icons,
  GeoCeDG export adapters, tests, verification inventories and pins, and the
  specification and design reconciliation; upstream changes minimal and
  registered.
- Forbidden (§54): `E3`, `F1`, `F2`, `F3`, `G`, `R7`, `G9B`; any change of
  Locus V2 semantics, spatial identity, projection bindings, DXF semantic
  authority beyond the accepted limitation, `D0`/`D1` unit semantics, or the
  visible product version `1.0.0`.
- Stop conditions (§55): new `GeoElement` type, XML element type or semantic
  identity family; global `GeoText` rotation semantics; a Classic text behavior
  change; replacement of existing text semantics; `GLOBAL_IMPACT`; a DXF
  `DIMENSION` entity; silent migration or renaming of legacy macros; `F1`; β
  failing `DQ-E2-6` in the implementation.
- Publication (§59): local implementation, commits, tests, verification, export
  probes and application runs only; no push, merge, publication, tag, release,
  binary publication, self-approval or `E3`.
- Required final state (§60): `TECHNICAL CANDIDATE PENDING AUTHOR REVIEW`,
  `PHASE` and `INTEGRATION` `ACCEPTED / COMPLETE`, `AUTHOR_SMOKE = PENDING`.

## Readings recorded for author review

- Icons (§34): the `E1` asset contract for Desktop tool icons
  (`owned_runtime_assets`) stores the SVG source and its canonical-LF hash; the
  raster is derived deterministically at run time by the existing renderer, and
  no stored derivative exists for any tool icon. `E2` follows that contract; the
  32 × 32 PNG derivation applies only to `.ggt` archives.
- The canonical prompt is amended in this commit to the authorized state; its
  frozen decisions are those of this record.
