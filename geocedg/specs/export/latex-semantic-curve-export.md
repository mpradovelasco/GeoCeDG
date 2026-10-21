# Semantic-curve export to LaTeX (PSTricks, PGF/TikZ and Asymptote)

| Field | Value |
|---|---|
| Status | Sections 2 to 4: **NORMATIVE — AUTHOR APPROVED** as author dispositions `DQ-C4`, `DQ-C5` and `DQ-C12` of the [`PRE-G9B-R6-plus-C` preparation record](../../../docs/validation/pre_g9b_r6_plus_c_prompt_closeout_record.md) (2026-10-04), implemented by `PRE-G9B-R6-plus-C`. Section 5: **ARCHITECTURE AUTHOR APPROVED (2026-10-21)**; it belongs to the `CU2` technical candidate, pending author review |
| Version | `1.0` |
| Origin | decision D-CU2-11, option A ([`CU2` approval record](../../../docs/validation/pre_g9b_cu2_author_architecture_approval_record.md)): the rules that lived only in a validation record become a durable specification |
| Scope | Locus V2 and SplineV2 semantic curves written by the GeoCeDG PSTricks, PGF/TikZ and Asymptote exporters |
| Dependencies | [G5 geometry export foundation](geometry-export-foundation.md); the shared approximation adapter of [`dxf-curve-fidelity-and-approximation.md`](dxf-curve-fidelity-and-approximation.md); [`locus-v2-existence-components.md`](../locus/locus-v2-existence-components.md) §15 |

## 1. Authority

The kernel semantic source is the only geometric authority. A LaTeX path is an export
approximation with declared fidelity: it never becomes a construction object, never enters the
dependency graph and is never read back as geometry. Labels are presentation metadata only.

## 2. Tolerance (`DQ-C4`)

For PSTricks, PGF/TikZ and Asymptote the approximation tolerance is explicit in model
coordinates, with default `0.001`. It is an export-request or session input, never document
state, and it is unaffected by `constructionUnit`, `presentationUnit` and `drawingScale`. It is
validated as positive and finite. The approximation machinery and the deterministic work limits
are those of the approved shared adapter and of the G9X1 foundation. Each LaTeX dialog may expose
the same input. There is no silent conversion to a physical-paper tolerance.

## 3. Partial output and classification (`DQ-C5`)

A locally certified, valid, deterministic and unambiguous semantic branch component may be
exported when global completeness is `NOT_ESTABLISHED` or the source or result is incomplete;
this never implies global completeness. The export is classified as `COMPLETE`,
`INCOMPLETE_WITH_CERTIFIED_COMPONENTS`, `NO_ADMISSIBLE_OUTPUT` or, where required, whole-export
`REJECTED`. Source, branch and component identity are preserved; every component is emitted
independently; no path joins across a gap or a discontinuity; nothing is interpolated over an
invalid interval; no identity comes from coordinates, order or proximity; the evidence of
incomplete coverage is preserved. `MISSING_DOMAIN`, an unresolved discontinuity, a degeneracy and
a work-limit failure affect the component or the source and can yield an incomplete export when
other certified output remains. `STALE_SOURCE_REVISION`, an unestablishable requested guarantee
or tolerance, or no admissible output at all reject the applicable export without pretending
success. LaTeX does not mechanically inherit the strict partial-output rule of G9X1; DXF keeps
it.

## 4. Report and comments (`DQ-C12`)

The output carries deterministic comments that expose the overall export classification, the
requested tolerance, the approximation status, the per-source and per-component classification
and evidence, and the incomplete coverage, addressed by semantic source, branch and component.
The dialog shows a visible non-modal report with the complete, incomplete or rejected status,
the approximate components, the omitted or failed components with their reasons, and the
tolerance and work-limit information. Comments are never parsed back into geometry.

## 5. Certified topology (`PRE-G9B-CU2`, amendment A2.6)

An export approximation follows the render topology of
[`locus-v2-presentation.md`](../locus/locus-v2-presentation.md), section "PRE-G9B-CU2 amendment
(version 2)":

- one open or closed path per certified component, in arc order across the seam;
- `-- cycle` (PGF/TikZ), a closed PSTricks path (`\pspolygon` or equivalent) or `--cycle`
  (Asymptote) only for a certified closed periodic component;
- an open component ends at its certified boundary images or one-sided limits, and is exported,
  not dropped;
- an end that cannot be evaluated is exported up to the last evaluated vertex and declared in
  the comments, and the export is then `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`;
- a certified singular parameter is an exact vertex;
- a point component is never exported as a path: the comments declare it, with its parameter
  enclosure and the status of its position, and the export is then incomplete;
- without `COMPLETE` every exported component remains strict and the classification is at best
  `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`;
- exploratory evidence is never exported.

## 6. Out of scope

Physical scale and units remain governed by the unit-system specification and ADR 0032. This
specification adds no writer, no new public command and no persisted state.

## 7. Validation

The `PRE-G9B-R6-plus-C` LaTeX tests remain the evidence of sections 2 to 4. Section 5 is
validated by rows V-TEX-1 and V-TEX-2 of the
[`CU2` validation matrix](../../../docs/validation/pre_g9b_cu2_validation_matrix.md).
