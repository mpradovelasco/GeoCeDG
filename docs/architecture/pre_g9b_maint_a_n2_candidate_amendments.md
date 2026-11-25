# PRE-G9B-MAINT-A-N2 — candidate amendments of approved texts

| Field | Value |
|---|---|
| Status | **CANDIDATE — READY FOR AUTHOR ADOPTION (revision R2).** `selfApproved=false`. No approved text is changed by this document or by the increments that wrote it (`PRE-G9B-MAINT-A-N2`, refined by `PRE-G9B-MAINT-A-N2-R1` and `PRE-G9B-MAINT-A-N2-R2`) |
| R2 changes | A-EXP-6 §12.8 and the new §12.9 with the author decisions RX-D18 and RX-D19; no other amendment changes |
| R1 changes | A-IMP-7 without store derivation entries; A-IMP-9 and A-NDI-1 with the configurable resource policy instead of a fixed 4 MiB cap; A-NDI-1 also with the atomic transition in every route and the Classic rule; A-EXP-6 with state 4 and the conformance readers; the A-N2 texts are kept in commit `874286e95` |
| Base | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` |
| Purpose | the exact amendments that the three A-N2 candidate contracts need in texts already approved, kept apart from those texts until the author decides |
| Application rule | each amendment is grouped under the contract that needs it, so that it can be approved together with that contract; once approved, a documentary descendant applies it to the target file as an additive, dated amendment section or a marked replacement, without rewriting the approval history of the target |

The contracts are [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md)
(group R), [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md)
(group X) and [`dxf-spline-approximation/v1`](../../geocedg/specs/import/dxf-spline-approximation.md)
(group A). Quotations of approved text are the exact current wording; «proposed» is the
replacement or addition. Inside proposed text, «link to `path`» marks a link whose path is
relative to the target file, written out so that it does not resolve from this document.

## 1. Inventory of conflicts

| Approved text | Clause | Conflict | Amendment | Group |
|---|---|---|---|---|
| `dxf-spline-reference.md` | §2 item 2 «owned by the document session (§11)» | `R-c` makes the store document data | A-IMP-1 | R |
| `dxf-spline-reference.md` | §11 `R-a` table («Save: references are not written», «New, Open, Close: cleared», «Reopen requires a new import») | superseded by the store lifecycle | A-IMP-2 | R |
| `dxf-spline-reference.md` | §12 «`R-c`: later priority target … not specified by `v1`» | specified by the store contract | A-IMP-3 | R |
| `dxf-spline-reference.md` | §0 «It does not change the approved DXF export contracts» | a separate export capability now writes references | A-IMP-4 | X |
| `dxf-spline-reference.md` | §10 «outside the population of the approved semantic exports» | still true for the semantic population; the separate option must be named | A-IMP-5 | X |
| `dxf-spline-reference.md` | §17 stop condition «the reader would need a DXF writer change or an approved export-contract change» | scope it to the reader | A-IMP-6 | X |
| `dxf-spline-reference.md` | §14 «The record is not modified and gains no link to the result.» | the relation is recorded outside the record | A-IMP-7 | A |
| `dxf-spline-reference.md` | §14.1 step 2 (interval arithmetic) and the last paragraph (contract of `MAINT-A-DXF1` or `MAINT-A-DXF1b`) | admit exact rational arithmetic; name the contract and the split | A-IMP-8 | A |
| `bspline-2d.md` | §10.5 «Passive: … never read by evaluation, metrics, intersections or export» | the export resolver reads the block informatively | A-BS-1 | X |
| `bspline-2d.md` | §10.5 «whose exact attribute grammar the approximation contract of `MAINT-A-DXF1` fixes before code»; «Applicability» | grammar fixed; states named; malformed `v1` block | A-BS-2 | A |
| `dxf-curve-fidelity-and-approximation.md` | §3 fidelity axes | the reference population has its own axis | A-EXP-1 | X |
| `dxf-curve-fidelity-and-approximation.md` | §4 last paragraph «`SPLINE` remains outside the baseline until mathematical exactness and independent reader-conformance evidence are both available.» | scope it to construction geometry | A-EXP-2 | X |
| `dxf-curve-fidelity-and-approximation.md` | §7 preflight counts; population | reference counts and writability | A-EXP-3 | X |
| `dxf-curve-fidelity-and-approximation.md` | §8 when the sidecar is mandatory; schema | reference section, schema version 4 | A-EXP-4 | X |
| `dxf-curve-fidelity-and-approximation.md` | §9 «It does not change G5 exact bytes» | holds only with the option off | A-EXP-5 | X |
| `dxf-curve-fidelity-and-approximation.md` | (new) | the capability itself | A-EXP-6 (§12) | X |
| `geometry-export-foundation.md` | Boundary (pipeline from GeoElements), type table (no `SPLINE`), layers (`0`, `GEOCEDG_L<n>`), entity order | a separate reference population with its own layer and order | A-GEF-1 | X |
| `native-document-identity.md` | «Serialization and `app_code: classic`» (existing entries; CU4 amendment) | a new optional entry | A-NDI-1 | R |
| ADR 0014 | decision 8 | refined for references by ADR 0045 | A-ADR-1 (status pointer) | X |
| ADR 0043 | decision 11 «never evaluation authority»; consequences | refined by ADR 0045 decision 4 | A-ADR-2 (status pointer) | X |
| ADR 0043 | decision 10 «session-only in the first implementation (`R-a`)» | `R-c` persists references | A-ADR-3 (status pointer) | R |
| `dxf-spline-reference.md` | §10 last bullet «Visibility of a reference is a session presentation state» | visibility becomes persistent document presentation | A-IMP-10 | R |
| `dxf-spline-reference.md` | §4.1 `SPLINE_ENTITIES_MAX` (10 000) and §4.4 `N_MAX_IMPORT` (1 024) | admission into the document is also bounded by the configurable store budget, which bounds memory and never the number of references below the importer's | A-IMP-9 | R |
| ADR 0016 | «new archive content needs a compatibility decision» | the decision is ADR 0044 | none (ADR 0044 is that decision) | R |
| roadmap | dependencies of `A-DXF0-RC`, `A-DXF1`, `A3` | new increments and order | A-RM-1 (additive record) | all |
| A-N1 validation matrix (candidate, not contract) | §3 AP-1 to AP-6, in particular AP-2 «residual `≤` estimate `≤` certified bound» | ordering withdrawn by observation AP-2 | superseded by the A-N2 matrix; the A-N1 matrix is not edited | A |
| A3 candidate prompt (planning) | «`SPLINE` racional … depende de `MAINT-A-R`»; manifest schema | the rational restriction concerns constructions only; schema coordination | note only (A3 is re-planned when authorized) | X |

ADR 0043 needs no new ADR for A-DXF1: its decision 11 already places the provenance block;
ADR 0045 records the one refinement (the informative read by the export resolver).

## 2. Group R — with `cedg-reference-store/v1`

### A-IMP-1 — `dxf-spline-reference.md` §2, item 2

Current: «immutable records (the binary64 data as read, the transformed data, provenance and
diagnostics) owned by the document session (§11), never by `Construction`.»

Proposed: «immutable records (the binary64 data as read, the transformed data, provenance and
diagnostics) owned by the document store `cedg-reference-store/v1` (§11), never by
`Construction`.»

### A-IMP-2 — `dxf-spline-reference.md` §11

Proposed new heading and first paragraph, keeping the `R-a` table as history:

> ## 11. Persistence
>
> From the version that implements `cedg-reference-store/v1`
> (link to `reference-store.md`), records are persisted in the native document
> outside `Construction` and follow that contract's lifecycle (save, reopen, New, Close,
> failed open, Undo/Redo, copy/paste, Insert File). The `R-a` rules below describe the
> session-only behavior of `MAINT-A-DXF0` and remain in force until then. In every version,
> import is not a construction step, adds no Undo entry and leaves `Construction`, its XML
> and its Undo history unchanged; Undo/Redo never touch records; copy/paste never copies a
> reference into a construction; no parallel DAG exists.

### A-IMP-3 — `dxf-spline-reference.md` §12

Proposed replacement of the first paragraph: «`R-c` is specified by
`cedg-reference-store/v1` (link to `reference-store.md`), whose §17 answers the seven points below.
The list is kept as the record of what had to be established.»

### A-IMP-9 — `dxf-spline-reference.md` §4.1, after the table

Proposed addition (R1): «These limits govern the reading of a DXF file. Admission of the
admitted records into the document store is additionally bounded by the configurable storage
budget of `cedg-reference-store/v1` §5.3, which never bounds the number of references below
`SPLINE_ENTITIES_MAX` and bounds memory (proposed initial value: about 3 000 000 binary64
values); an import that
would exceed the budget is rejected as a whole, with no prefix admitted, for the same reason
as `SPLINE_ENTITIES_MAX`.»

### A-IMP-10 — `dxf-spline-reference.md` §10, last bullet

Current: «Visibility of a reference is a session presentation state; it is not a layer of the
construction's layer domain and does not mean existence or geometric visibility.»

Proposed: «Visibility of a reference is a presentation state of the document, persisted by
`cedg-reference-store/v1` (session-only under `R-a`); it is not a layer of the construction's
layer domain and does not mean existence or geometric visibility.»

### A-ADR-3 — ADR 0043, status

Add one line: «Decision 10 («session-only in the first implementation») is followed by
ADR 0044 (`R-c`), when accepted; references stay outside `Construction` and the DAG.»

### A-NDI-1 — `native-document-identity.md`, after the CU4 amendment

> **`R-c` compatibility amendment (`cedg-reference-store/v1`, ADR 0044).** GeoCeDG may write
> one optional ZIP entry, `geocedg_references.xml`, holding the imported DXF references of the
> document outside `Construction`. Its content carries the contract `cedg-reference-store/v1`;
> it is read with bounded inflation under a configurable storage budget, and validated
> completely, including bit-for-bit transformation and digest checks, before it is installed.
> An absent entry is an empty store. An invalid entry, an unknown version, an over-budget store
> or an unknown `geocedg_*` root entry is a preflight failure: the previous document is kept and
> nothing is truncated. The store is installed together with the construction at the commit of
> the document transition, in every open and replacement route. Existing archive entries,
> format version, app code and construction semantics keep their format. Older readers,
> GeoCeDG 1.2.2 included, are not supported for such documents: they ignore the entry and drop
> it on save, and release notes say so. The GeoCeDG Classic diagnostic preserves the entry
> opaquely on every save route or, if it cannot, refuses to save such a document; it never
> loses it silently.

## 3. Group X — with `cedg-reference-export/v1`

### A-IMP-4 — `dxf-spline-reference.md` §0, second sentence

Current: «It does not change the approved DXF export contracts (…), ADR 0014 or any SplineV2
contract.»

Proposed: «It does not change the approved DXF export contracts (…), ADR 0014 or any SplineV2
contract; the separate export of references is defined by
`cedg-reference-export/v1` (link to `../export/dxf-reference-export.md`) and ADR 0045.»

### A-IMP-5 — `dxf-spline-reference.md` §10, fourth bullet

Current: «References are not `GeoElement`s, so they are outside the population of the
approved semantic exports (DXF, semantic LaTeX); GeoCeDG never rewrites a foreign entity as
its own.»

Proposed: «References are not `GeoElement`s, so they are outside the population of the
approved semantic exports (DXF, semantic LaTeX); GeoCeDG never rewrites a foreign entity as
its own. The DXF export may write them, as external definitions and never as CeDG geometry,
only through the explicit, separate capability `cedg-reference-export/v1`.»

### A-IMP-6 — `dxf-spline-reference.md` §17, last stop condition

Current: «the reader would need a DXF writer change or an approved export-contract change.»

Proposed: «the reader would need a DXF writer change or an approved export-contract change;
writing references is governed only by `cedg-reference-export/v1`.»

### A-BS-1 — `bspline-2d.md` §10.5, bullet «Passive»

Current: «**Passive**: it is never a DAG input, never identity, never read by evaluation,
metrics, intersections or export.»

Proposed: «**Passive**: it is never a DAG input, never identity, never read by evaluation,
metrics, intersections or the export of the construction's geometry. The disposition
resolver of `cedg-reference-export/v1` reads `referenceId`, `operation`,
`referenceDataSha256` and the applicability below, informatively, only to decide whether
the **reference** is included in a DXF export; it never changes how the construction is
exported and creates no dependency (ADR 0045).»

### A-EXP-1 — `dxf-curve-fidelity-and-approximation.md` §3, after the guarantee axis

> The population of imported references (§12) has its own fidelity axis,
> `REFERENCE_DEFINITION`: transfer of an external definition read back to the same binary64
> data. It is never `EXACT`, `APPROXIMATE`, `UNSUPPORTED` or `INVALID` of a construction
> source and never counts as CeDG geometry.

### A-EXP-2 — §4, last paragraph

Current: «`SPLINE` remains outside the baseline until mathematical exactness and independent
reader-conformance evidence are both available.»

Proposed: «For construction geometry, `SPLINE` remains outside the baseline until
mathematical exactness and independent reader-conformance evidence are both available
(`MAINT-A3`). Imported references are written as `SPLINE` only under §12.»

### A-EXP-3 — §7, after the first list

> When the separate reference option of §12 is chosen, preflight also reports references
> included, excluded by state, excluded by the user, outside the export area and not
> writable. The operation is writable when the construction contributes at least one entity
> or at least one reference is included.

### A-EXP-4 — §8, first paragraph and schema bullet

Add to the list of conditions: «and whenever at least one imported reference is written
(§12)». Replace «with `schema_version` 2 (version 1.1; …)» by the next free version
(`schema_version` 4 under `cedg-reference-export/v1`, with a section `external_references`;
versions 2 and 3 keep their meaning) and state that the encoding validator counts construction
entities and reference entities separately.

### A-EXP-5 — §9, first sentence

Proposed addition: «With the reference option of §12 off, or without references, the bytes
of every export are unchanged.»

### A-EXP-6 — new §12

> ## 12. `cedg-reference-export/v1` amendment: export of imported references
>
> Status: candidate of `PRE-G9B-MAINT-A-N2` (ADR 0045, proposed).
>
> 12.1 The export of imported references is a capability separate from the semantic export
> of `Construction`, run only by explicit user choice in the operation. Without it, §1 to §11
> govern unchanged and the output is byte-identical.
> 12.2 Its population is the records of `cedg-reference-store/v1`, never `GeoElement`s.
> Exporting mutates neither the construction, its XML, its Undo history, the store nor the
> saved state.
> 12.3 Each included reference is one planar, non-periodic `SPLINE` without fit points, with
> the degree, knots and transformed control points of its record and, when some effective
> weight differs from 1, the rational flag and the effective weights. No rational reference
> is approximated or replaced by a polyline.
> 12.4 Fidelity `REFERENCE_DEFINITION`: the entity's data, read back to the nearest binary64,
> equal the record's data under `==`; checked before promotion. It claims nothing about the
> source file or about other readers and confers no CeDG authority.
> 12.5 Default inclusion is decided by the explicit provenance of derived constructions and
> the store, never by proximity, coordinates, data digests, labels or indices; a reference
> validly represented by a current derived construction is not duplicated automatically;
> the user's choice prevails, with a warning on duplication.
> A reference whose related construction was modified is no longer validly represented and
> is included by default, while the modified construction is exported by §1 to §11. A failed
> approximation is never an export state.
> 12.6 The sidecar is mandatory when a reference is written.
> 12.7 Derived constructions are exported by §1 to §11 with their guarantees; this section
> authorizes no `SPLINE` for constructions (`MAINT-A3`).
> 12.8 External conformance is checked with AutoCAD Core Console and a pinned ezdxf, reading
> of the definition separated from mathematical evaluation; the evidence covers its fixtures
> and reader versions only. DXF format compliance, the fidelity class (exact re-reading,
> reproduced by ezdxf) and AutoCAD conformance (each value within 4 ulp as an initial test
> criterion, not a bound of the product or of the reader, plus preserved structure and
> geometry) are distinct levels of evidence; the AutoCAD tolerance does not change the fidelity
> class.
> 12.9 A reference whose coordinates, knots or weights would be written with a three-digit
> exponent is `NOT_WRITABLE` (`EXPONENT_NOT_PORTABLE`); it is never rewritten or replaced by a
> polyline, and the user may exclude it with the diagnostic kept.

### A-GEF-1 — `geometry-export-foundation.md`

- Boundary: add «A separate population of imported references may follow the construction
  entities under `cedg-reference-export/v1`; it does not enter the neutral model.»
- Type policy: add a row «imported reference (store record) → `SPLINE`, fidelity
  `REFERENCE_DEFINITION`, only under `cedg-reference-export/v1`».
- Layers: add «references are written on the fixed layer `GEOCEDG_REF`, added to the layer
  table only when a reference is written».
- Entity order: «reference entities follow every construction entity; construction handles
  never change».

### A-ADR-1 — ADR 0014, status

Add one line: «Decision 8 is refined for imported references (not for constructions) by
ADR 0045, when accepted.»

### A-ADR-2 — ADR 0043, status

Add one line: «Decision 11 is refined by ADR 0045 decision 4 (informative read of the
provenance block by the reference export resolver), when accepted.»

## 4. Group A — with `dxf-spline-approximation/v1`

### A-IMP-7 — `dxf-spline-reference.md` §14, first paragraph, last sentence

Current: «The record is not modified and gains no link to the result.»

Proposed (R1): «The record is not modified. The relation is recorded only by the result's
passive provenance block (`referenceId`, `bspline-2d.md` §10.5); it is not a dependency. The
store keeps no history of conversions or approximations, and a failed attempt leaves nothing
in the document.»

### A-IMP-8 — `dxf-spline-reference.md` §14.1

- Step 2, add: «Exact rational arithmetic on the binary64 data is admissible as the
  degenerate case of zero-width intervals.»
- Table row «Numerical error estimate»: keep «a distinct, denser verification set» and add
  «no ordering with the sample residual is asserted».
- Last paragraph: replace «the approximation contract of `MAINT-A-DXF1` (or `MAINT-A-DXF1b`,
  D-A-9)» by «`dxf-spline-approximation/v1` (§7: implementation by `MAINT-A-DXF1b`, author
  decision D-R1-9)».

### A-BS-2 — `bspline-2d.md` §10.5

- First paragraph: replace «whose exact attribute grammar the approximation contract of
  `MAINT-A-DXF1` fixes before code» by «whose exact attribute grammar
  `dxf-spline-approximation/v1` §10 fixes».
- Bullet «Applicability», add: «The states are `APPLICABLE`,
  `NOT_APPLICABLE_DEFINITION_CHANGED`, `NOT_APPLICABLE_UNDEFINED` and `NOT_INTERPRETABLE`; a
  `version="1"` block that violates the grammar is kept byte for byte and is
  `NOT_INTERPRETABLE`, without rejecting the document; an unknown version still fails closed.»
- After the first paragraph, add: «The same block records an exact conversion
  (`operation="EXACT_CONVERSION"`, no measures); the name «approximation provenance» covers
  both operations.»
- Bullet «`MAINT-A2` obligation»: unchanged.

## 5. Candidate roadmap record (A-RM-1)

Text proposed for an additive, dated record of the canonical roadmap, to be written only
after the author's decision:

> **2026-11-25 — `PRE-G9B-MAINT-A-N2`: normative design of `R-c`, reference DXF export and
> A-DXF1.** Candidates: `cedg-reference-store/v1`, `cedg-reference-export/v1`,
> `dxf-spline-approximation/v1`, ADR 0044 and ADR 0045 (proposed), the amendment set and the
> validation matrix. Proposed increments, none authorized: `MAINT-A-DXF0-RC-S0`,
> `MAINT-A-DXF0-RC`, `MAINT-A-DXF0-EXP`, `MAINT-A-DXF1-S0`, `MAINT-A-DXF1`,
> `MAINT-A-DXF1b`. Strict dependencies: RC after RC-S0; EXP after the store model types of
> RC (milestone RC-M); the A-DXF1 service after RC and DXF1-S0; DXF1b after DXF1; EXP and
> `MAINT-A3` never in parallel. Integration order RC → EXP → DXF1 → DXF1b. G9B stays
> `DESIGNED — NOT AUTHORIZED`.
