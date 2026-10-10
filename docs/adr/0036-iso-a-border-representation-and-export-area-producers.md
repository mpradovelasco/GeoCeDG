# ADR 0036 — ISO A border representation and export-area producers

- Status: **ACCEPTED — AUTHOR APPROVED** (author decision of 2026-10-10, [E3 and E3-R1 closeout record](../validation/pre_g9b_r6_plus_e3_closeout_record.md); approved candidate `T_R6PLUS_E3_R1` `7cd501167c856eef356888ca87ed3a0c97cea55c`, tree `3c51c9d3635c22939df6cab60259521f885eff92`, corrective successor of `257c854aa65fe82d316968109b9d084deefd2a49`; the acceptance authorizes no further architectural extension); earlier: `PROPOSED`
- Date: 2026-10-10
- Phase: `PRE-G9B-R6-plus-E3`
- Author decisions: [E3 author-decision record](../validation/pre_g9b_r6_plus_e3_author_decisions_record.md) (`DQ-E3-1` to `DQ-E3-16`; §5 amendment of 2026-10-10)
- Specification: [`geocedg/specs/sheets/iso-a-border.md`](../../geocedg/specs/sheets/iso-a-border.md)
- Evidence: [characterization report](../validation/pre_g9b_r6_plus_e3_preparation_characterization_report.md) (`K0`–`K21`), [reconciled design](../architecture/pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md) (`DESIGN — AUTHOR APPROVED`), [candidate report](../validation/pre_g9b_r6_plus_e3_candidate_report.md), [E3-R1 corrective candidate report](../validation/pre_g9b_r6_plus_e3_r1_candidate_report.md)

## Context

GeoCeDG drawings are laid out on ISO 216 sheets at an engineering scale. The
legacy `Templatev7` macros `sheetISOAnLand` and `sheetISOAnVert` approximated
this with an unrounded size series, a title block and no link to the export
area. The unit system (unit-system §18.4) fixes the physical meaning of a
model length; `B` (`AQ-X2`) fixed one session export-area authority with an
explicit-producer precedence; `C` gave physical export at the session
`drawingScale`. A sheet must be a real construction — dependent on a point,
recomputed, persisted, reachable from commands and scripts — whose paper
boundary can serve as the export area without introducing persistent export
state, a new object type or a second export-area mechanism, and without the
command reading units, scale or the view (`AGENTS.md` §10).

## Decision

1. **Representation R2** (`DQ-E3-1`): one shared-kernel algorithm
   `IsoABorder` with three fixed-arity outputs of existing types — `PAPER`
   (closed `GeoPolyLine`, hidden by default, the authoritative sheet), `FRAME`
   (closed `GeoPolyLine`, undefined when disabled or impossible) and `LABEL`
   (`GeoText` `A<n> — a:b`). Association is the parent algorithm plus the fixed
   output index; corners are internal and reachable through `Vertex`. No new
   `GeoElement` type, `GeoClass`, XML element, attribute or reader change.
2. **Explicit inputs only** (`DQ-E3-4`): point, ISO index, orientation, the
   captured scale pair `a:b`, the captured model units per millimetre `u` and
   the frame flag, all ordinary inputs; the tool writes literals that
   round-trip bit-exactly. The command reads no unit, scale, viewport, zoom or
   DPI.
3. **Canonical conversion** (`DQ-E3-5`, amended 2026-10-10):
   `conv(L) = ((L·b)/a)·u` in binary64, in exactly this order, used by the
   paper, the frame (from `W_f`, `H_f`), the label anchor, `ISO_A_SELECTION`
   and the coherence computation through one pure helper. Obligations: A
   deterministic bit-exact evaluation; B relative error `< 2^-51.99` against
   the captured `u` when `fl(q·u)` is normal; C `< 2^-50` against the physical
   unit definition when `u` and `fl(q·u)` are normal. Not representable
   results are undefined (never clamped); representable results with a
   subnormal `u` or product are defined but `UNGUARANTEED`.
4. **Inner frame** (`DQ-E3-2`, `DQ-E3-3`): margins 20/10/10/10 mm, exact
   integer validity, default on for A0–A4 and off for A5–A10, never reduced;
   A10 portrait is impossible and yields an undefined `FRAME`.
5. **Export-area producers** (`DQ-E3-8` to `DQ-E3-10`): two new session
   producers of the single `ExportArea` authority. `ISO_A_BORDER` links one
   sheet only by explicit user consent and resolves live to `PAPER`, never to
   `FRAME`; `ISO_A_SELECTION` stores an ISO-sized rectangle computed once at
   the session scale and unit. The `AQ-X2` precedence is unchanged; nothing is
   serialized; New and Open reset both.
6. **Link validity by identity**: the link is valid only while an identity
   (`==`) scan of the live construction finds the linked algorithm with its
   `PAPER` output, and `PAPER` is defined. Undo, redo, deletion and rebuilding
   redefinitions make it stale; a stale link is dropped with a notice and never
   re-resolved by label, index, coordinate or XML position.
7. **Coherence as derived presentation** (`DQ-E3-6`, `DQ-E3-12`):
   physical-size coherence (`COHERENT` / `INCOHERENT` / `NOT_DETERMINABLE`,
   with an explicit uncertainty bound) and scale-label coherence (`MATCH` /
   `DIFFERENT` / `NOT_APPLICABLE`) are computed for the linked sheet in Desktop
   presentation only; "Use sheet scale" is the only route that changes the
   session scale, and only on the user's action. A unit change never rescales
   geometry.
8. **Surfaces** (`DQ-E3-7`, `DQ-E3-13`, `DQ-E3-14`, `OTQ-E3-1`): the command
   is shared and ungated, available in GeoCeDG and in the Classic profile of
   this fork; the menu-only tool (mode 144), its unit gate, dialog, activation
   question, File actions, producers and indicators are GeoCeDG Desktop only.

## Consequences

- Sheets are ordinary constructions: typed or scripted commands, DAG
  dependents, undo, save and reopen behave as for any multi-output command; the
  label is recomputed and language-independent.
- Physical export of a linked sheet at its captured scale yields the nominal
  ISO page in every picture, print and LaTeX route; DXF writes the polylines
  and reports `resolved_producer` `iso_a_border` or `iso_a_selection`, without
  text.
- Older GeoCeDG builds and upstream GeoGebra do not know the command: they
  drop the outputs and their dependents and lose them on re-save (documented
  limitation).
- Legacy sheet macros and `Templatev7.ggb` are unchanged and coexist; no
  migration.
- The command adds one ADR 0031 identity: the gate inventory grows to 567
  stored names, 516 with an English head and 489 displayed; the R5-B
  fingerprint fixture grows only by the additive `IsoABorder` / `MarcoISOA`
  entries.

## Alternatives not selected

R1 (segments), R3 (polygon) and a dedicated object type; trim-only and
title-block sheet contents; a margin argument; automatic or absent export-area
participation; snapshot, freeze, label re-resolution and persisted links;
rescaling geometry on a unit change.
