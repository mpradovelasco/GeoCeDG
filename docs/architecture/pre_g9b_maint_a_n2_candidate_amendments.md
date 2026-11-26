# PRE-G9B-MAINT-A-N2 — candidate amendments of approved texts

| Field | Value |
|---|---|
| Status | **APPLIED — AUTHOR APPROVED (2026-11-26)** with the three contracts ([approval record](../validation/pre_g9b_maint_a_n2_author_approval_record.md)); the closeout `PRE-G9B-MAINT-A-N2` applied groups R, X and A to the approved texts as dated amendments, and A-RM-1 to the roadmap. `selfApproved=false`. Before that closeout no approved text was changed by this document or by the increments that wrote it (`PRE-G9B-MAINT-A-N2`, refined by `PRE-G9B-MAINT-A-N2-R1`, `PRE-G9B-MAINT-A-N2-R2`, `PRE-G9B-MAINT-A-N2-R3` and `PRE-G9B-MAINT-A-N2-R4`) |
| R4 changes | independent R3 audit: new A-IMP-12 (plane normal recorded per group with its presence, I-1); A-IMP-9 with the counter of the store §5.2 item 12 and the author-adopted provisional budget (I-2); A-BS-1 and A-ADR-2 with the two informative reads of provenance (m-1); A-GEF-1 with the empty-population guard of the current controller as an obligation of `MAINT-A-DXF0-EXP` (m-3); A-RM-1 as the single roadmap draft (m-4) |
| R3 changes | pre-adoption audit: A-EXP-3, A-EXP-4 and A-EXP-6 §12.6 with the sidecar situations A, B and C (M2); A-GEF-1 with the references-only export and the area rule (M3); A-EXP-5 with the X1C scope; A-IMP-7 with the reading list; A-IMP-8 with the per-span weight bound; new A-IMP-11; A-NDI-1 with the Classic opening exception; A-IMP-9 with the measured budget (M1) |
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
| `dxf-spline-reference.md` | §14 «It reads the immutable `data`, `nature`, `domain`, `closure` and provenance digests» and «The record is not modified and gains no link to the result.» | the operation also reads degree, knots, weights and the provenance fields the block copies; the relation is recorded outside the record | A-IMP-7 | A |
| `dxf-spline-reference.md` | §1, table, layer 3 «a declared fit» | the relation may also be an exact conversion | A-IMP-11 | A |
| `dxf-spline-reference.md` | §14.1 step 2 (interval arithmetic) and the last paragraph (contract of `MAINT-A-DXF1` or `MAINT-A-DXF1b`) | admit exact rational arithmetic; name the contract and the split | A-IMP-8 | A |
| `bspline-2d.md` | §10.5 «Passive: … never read by evaluation, metrics, intersections or export» | the block is read informatively to present the conversion or approximation state and by the export resolver for default inclusion (R4) | A-BS-1 | X |
| `bspline-2d.md` | §10.5 «whose exact attribute grammar the approximation contract of `MAINT-A-DXF1` fixes before code»; «Applicability» | grammar fixed; states named; malformed `v1` block | A-BS-2 | A |
| `dxf-curve-fidelity-and-approximation.md` | §3 fidelity axes | the reference population has its own axis | A-EXP-1 | X |
| `dxf-curve-fidelity-and-approximation.md` | §4 last paragraph «`SPLINE` remains outside the baseline until mathematical exactness and independent reader-conformance evidence are both available.» | scope it to construction geometry | A-EXP-2 | X |
| `dxf-curve-fidelity-and-approximation.md` | §7 preflight counts; population | reference counts and writability | A-EXP-3 | X |
| `dxf-curve-fidelity-and-approximation.md` | §8 when the sidecar is mandatory; schema | reference section, schema version 4 | A-EXP-4 | X |
| `dxf-curve-fidelity-and-approximation.md` | §9 «It does not change G5 exact bytes»; staging X1C «exact rational `SPLINE`» | holds only with the option off; X1C concerns construction geometry only | A-EXP-5 | X |
| `dxf-curve-fidelity-and-approximation.md` | (new) | the capability itself | A-EXP-6 (§12) | X |
| `geometry-export-foundation.md` | Boundary (pipeline from GeoElements), type table (no `SPLINE`), layers (`0`, `GEOCEDG_L<n>`), entity order, explicit export area, «writes no file when the neutral model contains zero exportable entities» | a separate reference population with its own layer, order and area rule; a references-only export (R3, M3) | A-GEF-1 | X |
| `native-document-identity.md` | «Serialization and `app_code: classic`» (existing entries; CU4 amendment); Classic «directly open `.cedg`» | a new optional entry; Classic refuses an over-budget store | A-NDI-1 | R |
| ADR 0014 | decision 8 | refined for references by ADR 0045 | A-ADR-1 (status pointer) | X |
| ADR 0043 | decision 11 «never evaluation authority»; consequences | refined by ADR 0045 decision 4 | A-ADR-2 (status pointer) | X |
| ADR 0043 | decision 10 «session-only in the first implementation (`R-a`)» | `R-c` persists references | A-ADR-3 (status pointer) | R |
| `dxf-spline-reference.md` | §4.3 row 210, 220, 230 «recorded» | the store must preserve the groups present and their presence; the current record fills absent components with `0.0` | A-IMP-12 (R4) | R |
| `dxf-spline-reference.md` | §10 last bullet «Visibility of a reference is a session presentation state» | visibility becomes persistent document presentation | A-IMP-10 | R |
| `dxf-spline-reference.md` | §4.1 `SPLINE_ENTITIES_MAX` (10 000) and §4.4 `N_MAX_IMPORT` (1 024) | admission into the document is also bounded by the configurable store budget, which bounds memory and never the number of references below the importer's | A-IMP-9 | R |
| ADR 0016 | «new archive content needs a compatibility decision» | the decision is ADR 0044 | none (ADR 0044 is that decision) | R |
| roadmap | dependencies of `A-DXF0-RC`, `A-DXF1`, `A3` | new increments and order | A-RM-1 (additive record) | all |
| A-N1 validation matrix (candidate, not contract) | §3 AP-1 to AP-6, in particular AP-2 «residual `≤` estimate `≤` certified bound» | ordering withdrawn by observation AP-2 | superseded by the A-N2 matrix; the A-N1 matrix is not edited | A |
| A3 candidate prompt (planning) | «`SPLINE` racional … depende de `MAINT-A-R`»; manifest schema | the rational restriction concerns constructions only; schema coordination | note only (A3 is re-planned when authorized) | X |

ADR 0043 needs no new ADR for A-DXF1: its decision 11 already places the provenance block;
ADR 0045 records the one refinement (the two informative reads of the block: presentation of the state and default inclusion in the export).

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

Proposed addition (R3): «These limits govern the reading of a DXF file. Admission of the
admitted records into the document store is additionally bounded by the configurable storage
budget of `cedg-reference-store/v1` §5.3, which never bounds the number of references below
`SPLINE_ENTITIES_MAX` and bounds memory (initial value 1 500 000 binary64 values counted by
`cedg-reference-store/v1` §5.2 item 12, adopted by the author as the provisional,
configurable product value backed by the R3 measurements); an import that
would exceed the budget is rejected as a whole, with no prefix admitted, for the same reason
as `SPLINE_ENTITIES_MAX`.»

### A-IMP-10 — `dxf-spline-reference.md` §10, last bullet

Current: «Visibility of a reference is a session presentation state; it is not a layer of the
construction's layer domain and does not mean existence or geometric visibility.»

Proposed: «Visibility of a reference is a presentation state of the document, persisted by
`cedg-reference-store/v1` (session-only under `R-a`); it is not a layer of the construction's
layer domain and does not mean existence or geometric visibility.»

### A-IMP-12 — `dxf-spline-reference.md` §4.3, row 210, 220, 230 (R4)

Current: «| 210, 220, 230 | plane normal | recorded; never used to transform coordinates |»

Proposed: «| 210, 220, 230 | plane normal | recorded per group with its presence: the record
keeps which of the three groups the entity carried and the value of each present group (a
group repeated within the entity keeps its last occurrence); an absent group is not a value
and is never recorded as `0` or `1`; the effective normal (absent 210 or 220 = `0`, absent 230
= `1`) is derived only for presentation; never used to transform coordinates |». The record
gains the presence of each group additively; admission, values, transformation, digests and
the rest of the import are unchanged. Implemented by `MAINT-A-DXF0-RC` (the current
`DxfSplineReference.normal()` fills an absent component with `0.0`: `cedg-reference-store/v1`
F22). **Authorized by the author decision D-R4-1 of 2026-11-26** for the scope of
`MAINT-A-DXF0-RC` (not yet as code), with six conditions: per component, presence and, when
present, the original binary64 value; geometric behavior, admission, diagnostics and
interpretation unchanged; presence never inferred from a value (a present `0.0` or `-0.0` is
present); the recorded normal never confused with the effective normal and no provenance
invented; references outside `Construction` and the DAG; tests of total absence, partial and
complete presence with explicit zeros and `-0.0`, persistence and reopening
(`cedg-reference-store/v1` §5.2 item 11, RC-25).

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
> loses it silently. As an exception to the Classic rule «directly open `.cedg`», Classic
> refuses, with a message, to open a document whose store entry exceeds its budget or a reader
> safety limit, so that it cannot lose it afterwards (R3).

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

Proposed (R4): «**Passive**: it is never a DAG input, never identity, never read by
evaluation, metrics, intersections or the export of the construction's geometry. It may be
read **informatively** for exactly two purposes: (1) to present to the user the state of
conversion or approximation of a reference and of the construction (reference manager,
construction inspector, details report of `dxf-spline-approximation/v1` §14); (2) by the
disposition resolver of `cedg-reference-export/v1`, which reads `referenceId`, `operation`,
`referenceDataSha256` and the applicability below to decide whether the **reference** is
included by default in a DXF export. Neither read is a geometric dependency, changes the
DAG, changes how the construction is evaluated or exported, or writes anything (ADR 0045). The
codec of the block and the persistence self-check of `dxf-spline-approximation/v1` §12.3, which
write, parse and re-read it to verify its bytes, are not reads of its meaning.»

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
> or at least one reference is included and writable after resolving preferences, states, user
> choices and the export area (§12).

### A-EXP-4 — §8, first paragraph and schema bullet

Add to the list of conditions (R3): «and whenever the reference option of §12 is on and the
export completes with at least one reference written or excluded (§12.6, situations A and B)».
Add after the schema bullet: «A sidecar written under §12 uses `schema_version` 4, with a
section `external_references`; a sidecar written without the reference option keeps the
current version and bytes; versions 2 and 3 keep their meaning.» State that the encoding
validator counts construction entities and reference entities separately. The new sidecar
requirement comes only from the reference option: the filtering of construction sources by
the export area alone still never makes the sidecar mandatory.

### A-EXP-5 — §9, first sentence

Proposed addition: «With the reference option of §12 off, or without references, the bytes
of every export are unchanged.» In the staging list, replace «X1C: implicit contouring and/or
exact rational `SPLINE`, only under a later evidence-backed authorization» by «X1C: implicit
contouring and/or exact rational `SPLINE` of construction geometry, only under a later
evidence-backed authorization; the `SPLINE` of imported references is governed by §12» (R3).

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
> 12.6 With the reference option on: (A) when at least one reference is written, DXF and
> sidecar are produced as a pair; (B) when the export completes and a reference is excluded, by
> the user, by preference or by the approved policy (including by the user after
> `EXPONENT_NOT_PORTABLE`, which never excludes a reference automatically), the
> sidecar is produced and records each exclusion and its cause, also when no reference is
> written; (C) when an included reference is `NOT_WRITABLE`, the strict rule rejects the
> operation: no DXF and no sidecar, nothing partial presented as a success, the cause given by
> the result and report of the operation.
> 12.7 Derived constructions are exported by §1 to §11 with their guarantees; this section
> authorizes no `SPLINE` for constructions (`MAINT-A3`).
> 12.8 External conformance is checked with AutoCAD Core Console and a pinned ezdxf, reading
> of the definition separated from mathematical evaluation; the evidence covers its fixtures
> and reader versions only. DXF format compliance, the fidelity class (exact re-reading,
> reproduced by ezdxf) and AutoCAD conformance (each value within 4 ulp as an initial test
> criterion for AutoCAD 2026, `ACADVER` 25.1, only, not a bound of the product or of the
> reader, plus preserved structure and
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
- Diagnostics and failure, replacing «The Desktop controller writes no file when the neutral
  model contains zero exportable entities.» (R3, M3): «The Desktop controller writes no file
  when the neutral model contains zero exportable entities and no imported reference is
  writable. The neutral model of `Construction` and the population of imported references
  are distinct: with the reference option of `cedg-reference-export/v1` explicitly chosen,
  an operation whose neutral model is empty is still exportable when at least one reference
  remains included and writable after resolving preferences, states, user choices and the
  export area; references are never added to the neutral model or turned into `GeoElement`s.
  The ordinary export keeps exactly its behavior; the preflight is unchanged except for the
  reference counts of A-EXP-3, and the diagnostic, paired-output and rollback rules are
  unchanged.»
- Explicit export area: add «an imported reference participates unless the convex hull of its
  control polygon is proved disjoint from `R` by exact predicates; it is written whole».
- Implementation note (R4, not text of the foundation): the current `GeoCeDGDxfExportController`
  shows its empty-population message before the model and the preflight in both flows (the
  legacy flow ends, the extended flow reopens its request dialog); the
  route, in the extended paired-output flow only (the legacy G5 route is not extended), that
  lets a references-only export reach the preflight is an obligation of `MAINT-A-DXF0-EXP`
  (`cedg-reference-export/v1` §9.1, RX-15 case (e)); with the option off the guard is unchanged.

### A-ADR-1 — ADR 0014, status

Add one line: «Decision 8 is refined for imported references (not for constructions) by
ADR 0045, when accepted.»

### A-ADR-2 — ADR 0043, status

Add one line: «Decision 11 is refined by ADR 0045 decision 4 (informative reads of the
provenance block to present the conversion or approximation state and to decide the default
inclusion of a reference in the DXF export), when accepted.»

## 4. Group A — with `dxf-spline-approximation/v1`

### A-IMP-7 — `dxf-spline-reference.md` §14, first paragraph

Current (second sentence): «It reads the immutable `data`, `nature`, `domain`, `closure` and
provenance digests.»

Proposed (R3): «It reads the immutable `data` with its degree, knots and effective weights,
`nature`, `domain`, `closure`, and the provenance fields that the approximation block copies
(digests, file digest and base name, entity handle and layer, transformation).»

Current (last sentence): «The record is not modified and gains no link to the result.»

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
- Steps 5 and 6, add (R3): «The per-span lower bound `min_k ω_k` of the Bernstein coefficients of
  `W` on a span is also valid and never smaller than `min_i w_i`, because those coefficients are
  convex combinations of the weights; either may be used.»

### A-IMP-11 — `dxf-spline-reference.md` §1, table, layer 3 (R3)

Current: «a new CeDG object of the `bspline-2d` family whose relation to a reference is a
declared fit (`MAINT-A-DXF1`)».

Proposed: «a new CeDG object of the `bspline-2d` family whose relation to a reference is a
declared exact conversion or fit (`MAINT-A-DXF1`)».

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

## 5. Candidate roadmap record (A-RM-1) — the single canonical draft (R4)

This is the only roadmap draft of `PRE-G9B-MAINT-A-N2`. It replaces the earlier wording of
this section and the draft in the R3 report §12.3. It is an additive, dated record of the canonical roadmap,
written in the roadmap's language and recorded **only** after the author's adoption decision,
by the closeout commit (R4 report §12). Until then the roadmap is not modified. The two
placeholders are filled from the author's decision and the R4 report §1:

> **`<fecha de la decisión autoral>` — `PRE-G9B-MAINT-A-N2`: adopción normativa de `R-c`,
> exportación DXF de referencias y A-DXF1.** Contratos `cedg-reference-store/v1`,
> `cedg-reference-export/v1` y `dxf-spline-approximation/v1` (revisión R4, candidato
> `<commit de contenido R4>`) y ADR 0044 y 0045 aceptados; enmiendas de los grupos R, X y A
> aplicadas a `dxf-spline-reference.md`, `bspline-2d.md`,
> `dxf-curve-fidelity-and-approximation.md`, `geometry-export-foundation.md`,
> `native-document-identity.md` y a las líneas de estado de ADR 0014 y ADR 0043. Presupuesto
> inicial configurable del almacén: 10 000 referencias y 1 500 000 valores binary64, valor
> provisional de producto respaldado por las mediciones de R3. Incrementos habilitados
> normativamente, **ninguno autorizado**: `MAINT-A-DXF0-RC-S0`, `MAINT-A-DXF0-RC`,
> `MAINT-A-DXF0-EXP`, `MAINT-A-DXF1-S0` y `MAINT-A-DXF1`; `MAINT-A-DXF1b`, `MAINT-A3` y
> `MAINT-A-R` siguen sin autorizar. **Puertas de cierre de `MAINT-A-DXF0-RC`:** C-4, el ciclo
> completo Open → Save → Reopen sobre la implementación en el presupuesto, incluido el caso
> al 100 % de `maxEntryBytes`, y C-5 en el presupuesto, con escáneres, trabajador experto e
> instantánea de recuperación aceptando el almacén. Dependencias estrictas: RC tras RC-S0; EXP
> tras el hito RC-M; el servicio A-DXF1 tras RC y DXF1-S0; DXF1b tras DXF1; EXP y `MAINT-A3`
> nunca en paralelo. Orden de integración RC → EXP → DXF1 → DXF1b. Deudas separadas DT-1
> (exponentes de la exportación ordinaria) y DT-2 (splines cerradas guardadas de nuevo por
> AutoCAD con `70 = 15`) registradas, no autorizadas. G9B sigue `DESIGNED — NOT AUTHORIZED`.
