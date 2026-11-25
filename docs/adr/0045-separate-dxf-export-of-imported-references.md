# ADR 0045 — Separate DXF export of imported references as `SPLINE`

- Status: **PROPOSED — READY FOR AUTHOR ADOPTION (revision R1)** (written by
  `PRE-G9B-MAINT-A-N2` on 2026-11-25, refined by `PRE-G9B-MAINT-A-N2-R1`).
  `selfApproved=false`. Acceptance would not authorize `MAINT-A-DXF0-EXP`.
- R1 changes: decision 4 records state 4 as alternative A (D-R1-6) and removes any failure
  state (D-R1-5); decision 6 fixes the conformance readers (D-R1-7); decision 7 records the
  availability rule (D-R1-10).
- Date: 2026-11-25.
- Authorization of the design work: author instruction «GeoCeDG — PRE-G9B-MAINT-A-N2»
  (2026-11-25); execution contract
  [`pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md).
- Base: `a9ee803bf4be92c58ab7e120111ebacc70055487`.
- Candidate contract: [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md).
- Related: ADR 0014 (decisions 2, 6, 7, 8), ADR 0043 (decisions 10, 11), ADR 0044
  (proposed), AGENTS §4, §13.

## Context

ADR 0014 decision 8 defers `SPLINE` «until separately approved mathematical and
interoperability evidence exists», and the DXF fidelity contract §4 keeps `SPLINE` outside
the baseline until exactness and independent reader conformance are both available. That
decision concerns CeDG geometry: a `SPLINE` that claims to represent a construction. Imported
references are a different population: their `SPLINE` definition already exists as external
data, and writing it back transfers data instead of representing a construction.

`dxf-spline-reference/v1` §10 keeps references outside the semantic exports because they are
not `GeoElement`s; §14 says the record gains no link to an approximation. `bspline-2d.md`
§10.5 says the approximation provenance block is never read by export. The author decided on
2026-11-25 (D-A-N2-3, D-A-N2-4) that the DXF export gains an explicit option to include
references, that represented references are not duplicated by default, that the
correspondence between a reference and a construction is explicit and persistent, and that a
non-convertible reference may stay outside `Construction` indefinitely and still be
re-exported.

## Decision

1. **A separate population and capability.** With an explicit option, the DXF export writes
   store references as planar `SPLINE` entities after the construction entities. With the
   option off, the ordinary export is byte-identical.
2. **A distinct fidelity class `REFERENCE_DEFINITION`.** It states that the written degree,
   knots, control points and weights read back to the same binary64 values as the stored,
   transformed definition. It is neither `EXACT` (CeDG objects) nor `APPROXIMATE`, claims
   nothing about the original file's bytes or about other readers' evaluation, and confers
   no CeDG authority. Rational references are written rational; never approximated, never
   replaced by polylines.
3. **ADR 0014 decision 8 is refined, not reversed.** Its deferral of `SPLINE` stays in force
   for constructions (`MAINT-A3`, BS-14). For references, the evidence it requires is the
   write round-trip check of the contract and the external reader-conformance obligation of
   `MAINT-A-DXF0-EXP`.
4. **Inclusion by explicit correspondence.** The default inclusion of a reference is decided
   from the passive provenance block of derived constructions (`referenceId`, `operation`,
   definition-digest applicability, with the recorded data digest as a consistency check
   only) and from the store; no association is ever made from coordinates, digests of data,
   labels, handles, proximity or order. This is an informative read of the provenance
   block that decides only whether to include the reference; it never changes how a
   construction is exported and creates no dependency (refines ADR 0043 decision 11 and
   `bspline-2d.md` §10.5).
   A reference is excluded by default only while a defined construction validly represents
   it (applicable provenance naming it); a modified related construction does not, so the
   reference is included by default and the modified construction is exported from
   `Construction` (author decision D-R1-6). A failed approximation is a transient operation
   result, never an export state, and leaves nothing in the document (D-R1-5).
5. **Mandatory sidecar** whenever a reference is written.
6. **External conformance with two fixed readers.** AutoCAD Core Console and ezdxf (a pinned
   external validation tool, never a product dependency or geometric authority) check each
   golden file in two separated parts: reading of the definition as written, and
   mathematical evaluation, recording any normalization a reader's math object applies. The
   evidence binds reader versions and fixtures and claims no universal interoperability
   (D-R1-7). The R1 pilot showed exact reading by ezdxf and decimal misreadings of 1–3 ulp
   and truncated three-digit exponents by AutoCAD 2026; the reading rule for AutoCAD and the
   treatment of such exponents are pending author decisions RX-D18 and RX-D19 of the contract.
7. **No novelty flag.** The option belongs to the existing DXF export action; it adds no
   activation flag and no «experimental» wording; it inherits the formal AGENTS §5 state of the
   existing DXF export action, and the author declares its state at the closeout of the
   implementing increment (D-R1-10).

## Consequences

- Candidate amendments of `dxf-curve-fidelity-and-approximation.md` (§3, §4, §7, §8, §9 and
  a new §12), `geometry-export-foundation.md` (boundary, type table, layers),
  `dxf-spline-reference.md` §0, §10, §14, §17 and `bspline-2d.md` §10.5.
- The preflight writability rule admits a references-only export.
- `MAINT-A-DXF0-EXP` and `MAINT-A3` share exporter files and must not run in parallel.

## Alternatives

| Alternative | Reason for rejection |
|---|---|
| Convert references into model entities of `GeometryExportModel` | mixes external data with CeDG outcomes; touches validators, preflight and LaTeX consumers |
| Write references as polylines | silent approximation of a transferable definition; contrary to D-A-N2-4 |
| Decide representation by geometric coincidence | contrary to D-A-N2-3 and AGENTS §1.10, §17 |
| Wait for `MAINT-A3` | couples an external-data transfer to the exactness proof of a construction family |

## Status boundary

This ADR stays **proposed** until the author accepts it explicitly.
