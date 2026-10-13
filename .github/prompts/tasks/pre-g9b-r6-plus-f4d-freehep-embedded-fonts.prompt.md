# PRE-G9B-R6-plus-F4-D — minimal correction of the inherited FreeHEP embedded-font failure

**CANONICAL PROMPT — PREPARED — NOT AUTHORIZED.** Prepared by
`PRE-G9B-R6-plus-F4-AUTHOR-DECISION-RECONCILIATION` (author instruction of 2026-10-13)
on `089fc2205300559c572c12f0cd32326abd0c2b5e`; successor, for this track, of the
superseded combined prompt
[`pre-g9b-r6-plus-f4-stylebar-thickness-and-pdf-fonts.prompt.md`](pre-g9b-r6-plus-f4-stylebar-thickness-and-pdf-fonts.prompt.md).
The design is `PROPOSED — PENDING AUTHOR REVIEW`; choices marked **[DQ-F4-n]** depend on
the pending decisions of the
[F4 author-decision reconciliation](../../../docs/validation/pre_g9b_r6_plus_f4_author_decision_reconciliation.md).
Nothing here authorizes implementation, and approval of track F4-A never authorizes
this track.

Evidence and design:
[F4 characterization report](../../../docs/validation/pre_g9b_r6_plus_f4_preparation_characterization_report.md) §7;
[author-decision reconciliation](../../../docs/validation/pre_g9b_r6_plus_f4_author_decision_reconciliation.md) §3, §4, §6;
[two-track design successor](../../../docs/architecture/pre_g9b_r6_plus_f4_two_track_design_successor_candidate.md) §3.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. Correct `TD-PDF-2`: with "Export text as shapes" unchecked,
every PDF that draws text fails with `StackOverflowError` in
`org.freehep.graphics2d.font.AbstractCharTable.toName(char)`, which calls itself since
upstream GeoGebra commit `c4fd03ce` replaced `toName(new Character(c))` with
`toName(c)`. Restore the delegation as `toName(Character.valueOf(c))` **[DQ-F4-5]**, in
GeoCeDG and Classic alike. `VERIFICATION_CLASS` **[DQ-F4-7]**: evidence-based proposal
`INTEGRATED_PHASE` (registered PHASE + INTEGRATION); the author's conservative default
`GLOBAL_IMPACT` (one FINAL campaign) remains admissible; frozen only by the implementation
authorization.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the F4-D implementation authorization (proposed: the
first F4 track, on the published base current at authorization **[DQ-F4-10]**). Entry
gate: exact identity; clean worktree; local, tracking and live remote `main`; the F4
decisions for this track; `AbstractCharTable.java` byte-identical to the GeoGebra
baseline `9b93256b` and still containing `return toName(c);` in `toName(char)`; the
reachability of the reconciliation §3 re-confirmed on the base (callers of
`toEncoding(char)` and `toName(char)` unchanged). Reproduce the failure on the base
before changing it.

## Authority and evidence hierarchy

`AGENTS.md`; current code; `physical-pdf-style-sizes.md` and ADR 0035 (unchanged); the F4
decisions; the evidence above.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

The single delegation line of
`source/desktop/desktop/src/main/java/org/freehep/graphics2d/font/AbstractCharTable.java`;
its registration in `docs/upstream/modified-files.yml` with provenance (upstream
`c4fd03ce`, restored semantics); tests; the phase selection `pre-g9b-r6-plus-f4-d.desktop`,
inventory and pins through the official mechanisms; the candidate report and mirror;
roadmap and mini-track status. An explicitly considered exception to the
minimal-upstream-change policy, limited to this line.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Any other FreeHEP change, FreeHEP update or replacement, exporter refactoring; the
picture dialog and its options; `PictureExportService`; text sizes, label offsets, S3
(`TD-PDF-1`); physical stroke and marker sizes; the xref observation
`OBS-F4P-FREEHEP-XREF-SIZE`; SVG, EMF, PNG, LaTeX and DXF outputs; the style bar (track
F4-A); kernel, geometry, serialization; F3 and earlier phases; G.

## Architectural placement

Inherited vendored export code in the upstream Desktop tree, used only by the Desktop
module; no kernel, shared-module, serialization or verification-infrastructure effect.

## Required design/specification

The two-track design successor §3 as decided by the author. No specification or ADR
change.

## Geometric invariants and degeneracies

Not applicable: no coordinate, definition, dependency, label, layer or unit change.

## Compatibility and serialization

No document change. Only executions that today end in `StackOverflowError` can change;
every currently successful output must stay byte-identical. Classic's embedded-font PDF
route is corrected identically and recorded as such.

## Integration obligations

Register the inherited modification; keep the upstream boundary green; no guide change
unless the author asks for one.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused tests, each obligation separately: (1) no `StackOverflowError` on the GeoCeDG
physical route, the legacy (unspecified-unit) route and the Classic dialog route with
text; (2) a complete PDF (non-empty, trailer and `%%EOF`, page and content stream);
(3) the expected font program embedded (font dictionary, `FontFile` of the configured
Type 1 embedding); (4) the text drawn correctly (extracted text equals the source
strings; raster comparison against the text-as-shapes rendering of the same scene within
a stated tolerance); (5) reader compatibility: Ghostscript rendering plus at least one
other reader in the author smoke — Ghostscript success alone is not proof of structural
validity; (6) text-as-shapes PDFs byte-identical to the base (GeoCeDG physical and
legacy, Classic); (7) Classic otherwise unchanged; (8) SVG with and without embedded fonts
byte-identical; (9) no other PDF change (physical stroke and marker sizes, page box,
`PostE2P1R2PhysicalPdfStyleTest`, `PostE2P1R2BaseFingerprintTest`,
`PreG9BR6PlusBPictureFidelityTest`, `PreG9BR6PlusCBaseIdentityTest`,
`PreG9BR6PlusCPhysicalExportTest`). Failure reporting: a failing export is never reported
as success. Then Checkstyle, the upstream boundary and, per the frozen class, the official
`verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F4-D` plus `-Profile INTEGRATION`
(`INTEGRATED_PHASE`) or one `FINAL` campaign (`GLOBAL_IMPACT`), and `STATIC`. Author
smoke: an embedded-font PDF exported from GeoCeDG and from Classic, opened in the author's
PDF reader, with the text selectable and correct.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation. A separate author instruction must name
the exact base, the decisions on `DQ-F4-5`, `-7`, `-9` and `-10`, and the frozen class.
Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

No push, merge, tag, release or binary. Publication only through a separate author
closeout naming the exact accepted commit.

## Acceptance and closeout

Stop with a frozen technical candidate pending author review; the candidate report
records invariant facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`).

## Required artifacts

Base reproduction; candidate report and mirror; PHASE and INTEGRATION (or FINAL) and
STATIC receipts; author smoke checklist; roadmap and mini-track status.

## Stop conditions

- The base no longer matches the recorded defect or perimeter.
- After the correction, embedded-font PDFs are empty, incomplete, without the font
  program, or render incorrectly.
- Any currently successful output changes.
- Any change beyond the single line appears necessary.
- Base identity, worktree, remote divergence, incomplete or untrusted coverage, or a
  missing author decision.
