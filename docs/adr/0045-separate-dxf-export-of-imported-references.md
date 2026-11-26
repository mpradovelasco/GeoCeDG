# ADR 0045 — Separate DXF export of imported references as `SPLINE`

- Status: **ACCEPTED — AUTHOR APPROVED (2026-11-26)** with `cedg-reference-export/v1`
  revision R4 (written by `PRE-G9B-MAINT-A-N2` on 2026-11-25, refined by
  `PRE-G9B-MAINT-A-N2-R1`, `PRE-G9B-MAINT-A-N2-R2`, `PRE-G9B-MAINT-A-N2-R3` and
  `PRE-G9B-MAINT-A-N2-R4`; [approval record](../validation/pre_g9b_maint_a_n2_author_approval_record.md)).
  The accepted text is that of commit `9c7e612294a2a0521a2c21d8f9c4580cb3b34efb`.
  `selfApproved=false`. Acceptance does not authorize `MAINT-A-DXF0-EXP`. The words «proposed»
  and «when accepted» below record the stage at which they were written.
- R1 changes: decision 4 records state 4 as alternative A (D-R1-6) and removes any failure
  state (D-R1-5); decision 6 fixes the conformance readers (D-R1-7); decision 7 records the
  availability rule (D-R1-10).
- R4 changes: decision 4 names the two informative reads of the provenance block (presentation
  of the state and default inclusion), amendment A-BS-1 (audit m-1).
- R3 changes: decision 5 distinguishes situations A, B and C for the sidecar; the consequences
  name the references-only export amendment of the foundation (pre-adoption audit M2, M3).
- R2 changes: decision 6 records the levels of evidence and the AutoCAD acceptance criteria
  (RX-D18) and decision 8 the non-portable exponent rule (RX-D19), both decided by the author.
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
   `bspline-2d.md` §10.5). The same block may also be read informatively to present to the
   user the conversion or approximation state (`dxf-spline-approximation/v1` §14); these are
   the only two reads of its meaning (the codec and the persistence self-check only verify its
   bytes), and neither is a geometric dependency or changes the DAG (R4,
   amendment A-BS-1).
   A reference is excluded by default only while a defined construction validly represents
   it (applicable provenance naming it); a modified related construction does not, so the
   reference is included by default and the modified construction is exported from
   `Construction` (author decision D-R1-6). A failed approximation is a transient operation
   result, never an export state, and leaves nothing in the document (D-R1-5).
5. **Sidecar and rejection.** With the reference option on, a completed export writes DXF and
   sidecar as a pair whenever a reference is written (situation A) or excluded, by the user, by
   preference or by the approved policy, including by the user after `EXPONENT_NOT_PORTABLE`,
   which never excludes a reference automatically (situation B);
   the sidecar records every written reference with its fidelity and every exclusion with its
   cause. An operation rejected because an included reference is `NOT_WRITABLE` (situation C)
   produces neither DXF nor sidecar, presents nothing partial as a success, and gives the cause
   in its result and report. The paired-output transaction of ADR 0014 decision 7 is unchanged.
6. **External conformance with two fixed readers.** AutoCAD Core Console and ezdxf (a pinned
   external validation tool, never a product dependency or geometric authority) check each
   golden file in two separated parts: reading of the definition as written, and
   mathematical evaluation, recording any normalization a reader's math object applies. The
   evidence binds reader versions and fixtures and claims no universal interoperability
   (D-R1-7). Three levels of evidence stay distinct: DXF format compliance; the fidelity class,
   whose correctly rounded re-reading ezdxf must reproduce exactly; and AutoCAD conformance,
   accepted when every value is within 4 ulp (an initial test criterion for AutoCAD 2026,
   `ACADVER` 25.1, only; not a product or reader
   bound, motivated by the 1–3 ulp misreadings AutoCAD 2026 showed in the R1 pilot) **and** the
   spline structure (degree, knots and multiplicities, domain, closure, weight conditions) and
   its evaluated geometry are preserved within the test tolerances; otherwise no conformance is
   declared. The fidelity class is unaffected by that tolerance: it keeps referring to the binary64
   data represented in the DXF and their correctly rounded re-reading. Reader, version, fixtures,
   observed maxima and test limits are recorded (RX-D18).
7. **No novelty flag.** The option belongs to the existing DXF export action, in its extended
   paired-output flow (`cedg.export.dxf.extended`; the legacy G5 route is not extended, so with
   that flag off no reference can be exported); it adds no
   activation flag and no «experimental» wording; it inherits the formal AGENTS §5 state of the
   existing DXF export action, and the author declares its state at the closeout of the
   implementing increment (D-R1-10).
8. **Non-portable exponents are not written silently.** A reference with a coordinate, knot or
   weight whose decimal token would carry a three-digit exponent (`|x| < 10^-99` non-zero, or
   `|x| ≥ 10^100`, either sign), which AutoCAD 2026 truncates silently, is
   `NOT_WRITABLE`/`EXPONENT_NOT_PORTABLE`; it is never replaced by a polyline or rewritten;
   the user may exclude it and the diagnostic is kept. Writing plain decimals is a later
   improvement, adopted only on conformance evidence (RX-D19). The limitation is AutoCAD's, not
   the DXF format's nor ezdxf's.

## Consequences

- Candidate amendments of `dxf-curve-fidelity-and-approximation.md` (§3, §4, §7, §8, §9 and
  a new §12), `geometry-export-foundation.md` (boundary, type table, layers, entity order,
  export area, and the «no file when the neutral model is empty» rule),
  `dxf-spline-reference.md` §0, §10, §14, §17 and `bspline-2d.md` §10.5.
- The preflight writability rule admits a references-only export when at least one reference
  remains writable; references never enter the neutral model; with no entity and no writable
  reference no file is written.
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
