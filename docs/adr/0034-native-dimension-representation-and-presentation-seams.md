# ADR 0034 — Native dimension representation and presentation seams

- Status: **ACCEPTED — AUTHOR APPROVED** (decision of 2026-10-08 on the exact
  candidate `T_R6PLUS_E2` revised `3a7246614a6ef040d84a1440c4c10b3f93be5776`, tree `00b5230d19debea5598299906f846f52b4d6e9d7`; recorded in the
  [E2 closeout record](../validation/pre_g9b_r6_plus_e2_closeout_record.md); the decision text is unchanged from that
  candidate, only the status line changed)
- Date: 2026-10-08
- Phase: `PRE-G9B-R6-plus-E2`
- Author decisions: [E2 author-decision record](../validation/pre_g9b_r6_plus_e2_author_decisions_record.md); [E2 authorization record](../validation/pre_g9b_r6_plus_e2_authorization_record.md)
- Specification: [`geocedg/specs/dimensions/native-dimensions.md`](../../geocedg/specs/dimensions/native-dimensions.md)
- Evidence: [characterization report](../validation/pre_g9b_r6_plus_e2_preparation_characterization_report.md) (`K1`–`K20`), [`DQ-E2-6` addendum](../validation/pre_g9b_r6_plus_e2_dq6_aligned_text_addendum.md) (`A1`–`A10`), [candidate report](../validation/pre_g9b_r6_plus_e2_candidate_report.md)

## Context

GeoCeDG needs engineering dimensions that are real constructions: dependent on
their points, recomputed, persisted, reachable from commands and scripts, and
consumed coherently by every frontend and exporter. The value must stay a
model-unit measure that no presentation setting can change (`AGENTS.md` §10,
unit-system specification §14), while the displayed text follows the document
units and must read parallel to the dimension line (`DQ-E2-6`). The existing
`DrawText` is final and draws every text horizontally; GeoGebra has no dimension
object.

## Decision

1. **Representation β** (`DQ-E2-1`): one shared-kernel algorithm per command
   (`AlignedDimension`, `LinearDimension`) whose five outputs are existing
   object types — value `GeoNumeric`, dimension line and two extension lines
   `GeoSegment`, value `GeoText`. No new `GeoElement` type, `GeoClass`, XML
   element type, attribute or reader change.
2. **Explicit inputs only**: points, the direction, and the signed `Offset`
   with optional `Overshoot` and `Gap`, all in model units. The side is fixed by
   input order and the sign of `Offset` (B1); nothing reads units, scale,
   viewport, zoom or DPI.
3. **Presentation seam**: `compute()` is geometry-only. A separate presentation
   step writes only the value-text string after `compute()`, at construction,
   and from one keyed unit-state listener per construction, through a small
   idempotent registration added to `DocumentUnitSystem`. Unit operations
   change only the strings.
4. **Aligned-value drawable seam**: the algorithms implement a kernel
   text-geometry interface; `EuclidianDraw` routes only their value texts to a
   GeoCeDG `DrawDimensionText`, which paints an ordinary `DrawText` of the same
   text inside a rotation (reading rule `θ ∈ (−90°, 90°]`, lift, outside
   placement for short lines) with rotated hit testing. Because `DrawText` is
   final, the drawable wraps a delegate instead of extending it. Nothing of the
   orientation is stored.
5. **Bounded export adaptation**: picture formats inherit the drawable; the
   GeoCeDG PSTricks, PGF and Asymptote exporters override segment and text
   output for dimension outputs only; DXF writes the segment subset without a
   `DIMENSION` entity.
6. **Desktop orchestration**: two tool modes (142, 143) capture the inputs once
   as free hidden numbers; the drag edits only the free `Offset`.

## Rejected alternatives

| Alternative | Reason |
|---|---|
| α: a composite `GeoDimension` type | new `GeoClass`, XML element type, drawable, styles and exporter mappings; older readers fail harder (`K17`); a `GLOBAL_IMPACT` change |
| overloading `Dimension` | upstream `Dimension` is cardinality, not graphics (`K2`) |
| `DirectDimension` / `AxisDimension` heads | collide with legacy user macros in `Templatev7.ggb` stores (`DQ-E2-7`) |
| reading units inside `compute()` or a unit factor as a DAG input | makes geometry depend on presentation (`AGENTS.md` §10) |
| global rotated-text semantics for `GeoText` | changes every text, Classic and the XML |
| horizontal centred value | rejected by the author (`DQ-E2-6`) |
| a DXF `DIMENSION` entity | DXF specification change, out of scope |

## Consequences

- Dimensions round-trip through existing XML; documents without dimensions are
  byte-identical; older readers report a load error, keep independent copies of
  the outputs (value fixed, figure and text undefined) and drop the commands on
  re-save.
- The value text is the only unit-dependent output; it is never parsed back.
- Every non-dimension text and segment keeps its drawing, hit testing and export.
- Known limitations: DXF subset; bare value inside macros; overlapping arrows on
  very short lines; `Corner` returns the axis-aligned bounds of the rotated value.
- Adds two commands to the ADR 0031 inventory (566 stored names, 515 with an
  English name, 488 displayed) and two commands (36 rows, 28 lookup probes) to
  the `PRE-G9B-R6` GGBScript capability matrix.

## Revision note (2026-10-08, author smoke follow-up)

The revised candidate adds, under the author decision of the smoke follow-up
([record](../validation/pre_g9b_r6_plus_e2_smoke_followup_author_decisions_record.md)):
an initial line thickness `2` of the dimension line and both extension lines,
with a tool creation preference, as ordinary element style (specification §4.2,
§8.5); and the document's dimension unit suffix policy, applied by the existing
presentation step of output 4 and persisted in the unit state (specification
§6.4; unit-system §20). The representation and the seams of this decision are
unchanged: no new output, argument, element type or dependency.
