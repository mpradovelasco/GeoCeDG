# DXF export of imported references — `cedg-reference-export/v1`

| Field | Value |
|---|---|
| Status | **CANDIDATE — PENDING AUTHOR REVIEW.** Normative design written by `PRE-G9B-MAINT-A-N2`. `selfApproved=false`. It authorizes no code: `MAINT-A-DXF0-EXP` is not authorized. It does not change the approved export contracts until the author approves the candidate amendments that reference it |
| Contract identifier | `cedg-reference-export/v1` (policy and population); fidelity class `REFERENCE_DEFINITION` |
| Base | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` |
| Architectural layer | Export adapter outside the geometric kernel (AGENTS §4, §13); a separate read-only reference adapter beside the construction adapters |
| Depends on | [`cedg-reference-store/v1`](../import/reference-store.md) (reference identity, store snapshot, derivation entries, export preference); [`dxf-spline-approximation/v1`](../import/dxf-spline-approximation.md) for the states that involve a derived construction; it does **not** presuppose `MAINT-A3` |
| Amends (candidates) | [`dxf-curve-fidelity-and-approximation.md`](dxf-curve-fidelity-and-approximation.md), [`geometry-export-foundation.md`](geometry-export-foundation.md), [`dxf-spline-reference.md`](../import/dxf-spline-reference.md) §0, §10, §14, §17, [`bspline-2d.md`](../curves/bspline-2d.md) §10.5, ADR 0014 decision 8 (by [ADR 0045](../../../docs/adr/0045-separate-dxf-export-of-imported-references.md), proposed); texts in the [candidate amendment set](../../../docs/architecture/pre_g9b_maint_a_n2_candidate_amendments.md) |
| Author directions | D-A-N2-1, D-A-N2-3, D-A-N2-4, D-A-N2-5 (instruction of 2026-11-25) |

## 0. Boundary

The ordinary semantic export of `Construction` does not change: with the reference option
off, or with no reference in the document, the DXF bytes and the sidecar are exactly those
of the approved route. The export of references transfers **external data** from the
document store; it is not CeDG geometry, creates no `GeoElement`, evaluates nothing new for
the output, solves nothing, and mutates neither the construction, its XML, its Undo history,
the store nor the saved state. Constructions converted or approximated from a reference are
exported from `Construction` by the approved route and with the guarantees that route
offers (today `bspline-2d` outputs travel as approximate `LWPOLYLINE` with
`ESTIMATED_ERROR`; `bspline-2d.md` §13); this contract never changes how a construction is
exported.

## 1. Authority

AGENTS §1 (1, 3, 8), §4, §13, §17; `dxf-curve-fidelity-and-approximation.md` §1 to §9;
`geometry-export-foundation.md`; ADR 0014; `dxf-spline-reference.md` §3 to §6, §9, §10,
§14; `bspline-2d.md` §10.5, §12.3, §13; `cedg-reference-store/v1`;
`dxf-spline-approximation/v1`.

## 2. Code facts at the base

Verified on `a9ee803bf` (characterization report §3):

| Fact | Consequence |
|---|---|
| Pipeline `GeoCeDGDxfExportController → GeometryExportService → adapters (GeoElementGeometryExportAdapter, G9X1GeometryExportAdapter, SemanticCurveExportAdapter2D) → GeometryExportModel → DxfExporter`; `AC1015`; entity handles from `0x100` in model order; `$HANDSEED` follows the entity count | references are appended after every construction entity, so construction handles never change |
| Every entity writes `0`, `5`, `100 AcDbEntity`, `8`, `420`, `999 GeoCeDG source …` and `60 1` when hidden; reals by `Double.toString`, `-0.0` written as `0.0`, non-finite forbidden | the reference writer reuses the same pair formatter |
| No `SPLINE` writer exists; `BSpline` reaches the exporters as a `GeoLocusV2` and is written as approximate `LWPOLYLINE` | references need their own `SPLINE` writer; `MAINT-A3` (constructions) stays separate |
| The population comes only from `Construction` or the selection; references are absent from semantic exports because they are not `GeoElement`s | the new population is added explicitly, never by turning references into model entities |
| Fidelity enums: entity `Exactness {EXACT, APPROXIMATE}`, outcome `Fidelity {EXACT, APPROXIMATE, UNSUPPORTED, INVALID, DECLARED}`, guarantee `{CERTIFIED_ERROR_BOUND, ESTIMATED_ERROR, FLOATING_POINT_UNCERTIFIED}` | `REFERENCE_DEFINITION` is a separate axis, never one of these values |
| Preflight is writable only when the construction contributes at least one entity | a references-only export needs an amended writability rule (§9) |
| The DXF must be pure ASCII; handles and layers of `AC1021+` source files may be non-ASCII | original strings go only to the UTF-8 sidecar (§4) |
| Sidecar `org.geocedg.dxf.fidelity-manifest`, `schema_version` 3; the validator requires the encoded entity count to equal the model's | a new schema version and a separate count for references (§10) |
| Paired DXF + sidecar: same-directory temporaries, no-replace promotion, rollback; «two-file atomicity is not claimed» | unchanged; this contract claims no more |
| The A-DXF0 reader re-reads exporter output: ignores `999`, `420`, `60`, `5`, `100`; reals to the nearest binary64; code 41 by order of occurrence; code 70 bit 2 rejected | usable for round-trip tests, but **not** an independent reader (shared library) |
| Picture exports of Graphics 1 draw visible references as presentation (`MAINT-A-DXF0-R1`, Q-DXF-3) | unchanged; picture outputs are not semantic exports |

## 3. A separate capability

- **Population «external references»**, distinct from the semantic population
  `geocedg-dxf-geometric-2d/v1` and from the selection. Its members are store records
  identified by `referenceId`; they never mix with construction outcomes or neutral model
  entities.
- It runs only by **explicit user choice** in the export operation (§11).
- Each reference gets a **state** (§6), computed from explicit identifiers only, and a
  **default inclusion** (§7) that the user may invert.
- Picture exports (PNG, SVG, PDF, EMF, print, clipboard) keep the presentation rule of
  `dxf-spline-reference.md` §10; semantic LaTeX keeps excluding references.

## 4. `SPLINE` mapping (planar, `AC1015`)

One admitted reference gives exactly one entity, written from the store record, **never
from a tessellation** (the render tessellation is never an export source):

| Code | Value | Rule |
|---|---|---|
| 0 | `SPLINE` | — |
| 5 | handle | sequential, after every construction entity; `$HANDSEED` updated |
| 100 | `AcDbEntity` | — |
| 8 | `GEOCEDG_REF` | one fixed ASCII layer, added to the layer table only when a reference is written (decision RX-D7) |
| 420 | reference presentation color | presentation, not semantics |
| 999 | `GeoCeDG imported reference <referenceId> (dxf-spline-reference/v1); not CeDG construction geometry` | ASCII; traceability in the DXF itself |
| 60 | `1` | only when the reference is hidden in the document |
| 100 | `AcDbSpline` | — |
| 210/220/230 | `0.0 / 0.0 / 1.0` | XY plane; written or omitted as fixed by the conformance evidence (decision RX-D6) |
| 70 | `8` (planar) `+ 4` when weights are written `+ 1` when exactly closed | never `2` (periodic) nor `16`; closure only by binary64 `P_0 == P_n` (import §4.4) |
| 71 | `p` | — |
| 72 | `n + p + 2` | — |
| 73 | `n + 1` | — |
| 74 | `0` | no fit points |
| 42/43 | — | omitted unless the conformance evidence requires them; never used to merge anything |
| 40 × (n+p+2) | knots `u_0..u_m` | unchanged: never renormalized, trimmed or reversed |
| 41 × (n+1) | effective weights | only when some effective weight differs from 1 (decision RX-D3); position fixed by conformance (decision RX-D4) |
| 10/20/30 × (n+1) | `x_i`, `y_i` of `data`, `0.0` | `z = 0` as every 2D export; the source plane `z = c` is reported in the sidecar |

Consequences by nature (import §3): `RATIONAL` → flag 4 and its unequal weights, **never**
a polynomial approximation or a polyline; `RATIONAL_EQUAL_WEIGHTS` → flag 4 and the equal
weights as read; `POLYNOMIAL` → no flag 4 and no code 41. The representation details of the
source (flag as read, explicit unit weights) are reported in the sidecar, not reproduced.

Original layer names, handles and file names are **never** written into the DXF (ASCII
rule); they are sidecar data.

## 5. Fidelity class `REFERENCE_DEFINITION`

### 5.1 Definition

A `SPLINE` entity has fidelity `REFERENCE_DEFINITION` when its degree, its knot vector, its
control points `(x, y)` and, when written, its weights, read back by the decimal rule of
import §4.5 (nearest binary64, ties to even), are equal under binary64 `==` to the record's
`data`, knots and effective weights, with `z = 0`, no fit points and the flags of §4.

### 5.2 What it rests on and what it checks

`Double.toString` writes a decimal that converts back to the same binary64; the only
pattern change is `-0.0 → 0.0`, equal under `==` and without structural or evaluation
effect. The writer does not rely on this silently: before promotion it parses every
written real of every reference entity and compares it with the source value; a mismatch
makes that reference `NOT_WRITABLE` (an outcome of the reference axis, never the
construction outcome `UNSUPPORTED`) with reason `WRITE_ROUND_TRIP_FAILED` and blocks the
strict operation (§7.3).

### 5.3 Four roundings kept apart

| Rounding | Where | Claim |
|---|---|---|
| reading | import: decimal text of the source file → binary64 (import §4.5) | declared, never measured |
| transformation | import: `T` applied to control points (import §6) | declared; zero when `T` is the identity |
| writing | this contract: binary64 → decimal text | zero under `==` by §5.2, checked |
| external interpretation | another reader's parsing, evaluation, tessellation, display | outside the claim; addressed by the conformance obligation (§12) |

### 5.4 What it does not claim

Nothing about the bytes or decimal text of the original DXF (binary64 equivalence is not
byte identity); nothing about how another reader evaluates or draws the entity; no CeDG
authority. It is neither `EXACT` (which qualifies CeDG objects) nor `APPROXIMATE`: it is the
fidelity axis of the reference population and never counts as exact construction geometry
in the preflight or the sidecar.

### 5.5 Coordinate system and units

`data` are already in model units through the explicit import transformation `T`; the
export writes them unchanged, with `$INSUNITS` of the construction unit like every other
entity, and applies no inverse transformation and no scaling. The sidecar reports `T`, the
source `$INSUNITS`, the document unit at import and the source plane `z = c`. Writing the
untransformed `sourceData` in the source system is a different option, out of `v1`
(decision RX-D11).

## 6. Correspondence and states

### 6.1 Principle

No association between a reference and a construction is established by proximity,
coordinate equality, digests of data, labels, handles, indices or order. Only explicit
identifiers written when the relation was created establish it (D-A-N2-3). The data digest
recorded in the block is used only as a consistency check on an association that a
`referenceId` already established (§6.3); it never creates one.

### 6.2 Minimal correspondence record

1. **Construction side** (written by `MAINT-A-DXF1`): the passive block
   `approximationProvenance` of `<geocedgBSpline>`; the resolver reads exactly
   `referenceId`, `operation`, `referenceDataSha256` and `definitionDigest` (for
   applicability) (`dxf-spline-approximation/v1` §10); `derivationId` links the block to its
   store entry for the report only. It travels with copy, Undo/Redo and persistence; it is
   never a DAG input.
2. **Reference side** (store, outside Undo): the append-only derivation entries
   (`derivationId`, operation, outcome `CREATED`/`FAILED`, cause, best indicator) and the
   export preference `AUTO | INCLUDE | EXCLUDE` (`cedg-reference-store/v1` §5.2, items 6
   and 7). The default **inclusion** never depends on derivation entries; they only select
   the state **label** among 1, 5 and 6, all of which include the reference.

The durable identity of a construction (`geocedgId`) is **not** part of the resolution:
a copy pasted in the same document keeps the block and still represents the reference
after the original is deleted, and identity survival through redefinition is decided by
ADR 0026, not by this contract.

### 6.3 Resolution

At each export, for reference `r`, let `L(r)` be the defined `BSpline` outputs of
`Construction` whose block is interpretable and whose `referenceId` equals `r.referenceId`
(exact string equality). A member of `L(r)` **validly represents** `r` when its
applicability (`dxf-spline-approximation/v1` §11) is `APPLICABLE` and its
`referenceDataSha256` equals `r`'s `dataDigest`. The reading of these attributes by the
resolver is informative: it decides only the inclusion of the reference, never the export
of the construction (candidate amendment A-BS-1).

### 6.4 Exhaustive inclusion table (option «include references» on)

| # | State | Condition | Reference by default | Derived construction | Duplication and its avoidance | Explicit user selection | Diagnostic |
|---|---|---|---|---|---|---|---|
| 1 | never converted | `L(r)` empty and no derivation entry | **included** | none | none | may exclude | — |
| 2 | converted exactly | some member validly represents `r` with `operation = EXACT_CONVERSION` | excluded | exported from `Construction` by its route and guarantees | avoided by excluding the reference | may include: both are written, with warning `DUPLICATE_REPRESENTATION` | `REPRESENTED_BY_EXACT_CONVERSION` (names the construction sources) |
| 3 | approximated satisfactorily | some member validly represents `r` with `operation = APPROXIMATION` (and none with 2) | excluded | as in 2 | as in 2 | as in 2 | `REPRESENTED_BY_APPROXIMATION` |
| 4 | derived construction modified | `L(r)` non-empty, no member validly represents `r`, at least one member `NOT_APPLICABLE_DEFINITION_CHANGED` | **PENDING AUTHOR DECISION RX-D2**; recommended: **included** | exported from `Construction` (it is the user's current object) | not a duplication of the same definition; notice `DERIVED_CONSTRUCTION_MODIFIED` | may exclude | `DERIVED_CONSTRUCTION_MODIFIED` |
| 5 | derived construction deleted | `L(r)` is empty (`L(r)` holds defined outputs only), no condition of row 8 holds for `r`, and some derivation entry is `CREATED` (deleted, undone, replaced by another family, or undefined) | **included** | none written (deleted or undefined) | none | may exclude | `DERIVED_CONSTRUCTION_ABSENT` |
| 6 | approximation failed | `L(r)` empty, derivation entries only `FAILED` | **included** | none | none | may exclude | `APPROXIMATION_FAILED` (cause and best indicator in the sidecar) |
| 7 | reference copied or reimported | a reimport (or a future duplicate operation) creates a new record `r'` with a new `referenceId`; constructions derived from `r` name `r`, not `r'` | `r'` resolved on its own (state 1 unless it has its own derivations); `r` keeps its own state | derived constructions of `r` never represent `r'` | possible when `r` and `r'` are both included; never resolved by data; informative notice `SAME_DATA_AS_REFERENCE` when the data digests are equal, used for no decision | may exclude either | `SAME_DATA_AS_REFERENCE` (informative) |
| 8 | provenance not current or association unresolvable | a construction names `r` but its block is `NOT_INTERPRETABLE`, or its `referenceDataSha256` differs from `r`'s `dataDigest`; or a construction names a `referenceId` absent from the store | for `r`: treated as having no valid representation → **included**; orphan constructions affect no reference | exported from `Construction` normally | not resolved automatically; notice | may exclude | `ASSOCIATION_UNRESOLVABLE` on `r`; `ORPHAN_REFERENCE_PROVENANCE` on the construction (sidecar) |

Precedence when several conditions meet: user `INCLUDE`/`EXCLUDE` (operation toggle, then
persistent preference) > 2 > 3 > 4 > 8 > 5 > 6 > 1. The underlying state is always reported,
also when the user overrides it.

**The historical existence of an approximation is not a current representation.** States
5 and 6 depend only on the absence of a defined member that validly represents `r`;
derivation entries only name the history in the diagnostic.

### 6.5 State 4: alternatives for the author (RX-D2)

| Alternative | Default for the reference | Argument | Risk |
|---|---|---|---|
| **A (recommended)** include | written beside the modified construction | the modified construction no longer validly represents the reference (D-A-N2-3: «construcción que la representa válidamente»); D-A-N2-4 never loses the reference | two related curves in the DXF; the notice says why |
| B exclude with warning | not written | respects the user's edit as the drawing's intent | the external definition disappears from an export the user asked to «include references» |
| C no default | export blocked until the user chooses per reference | strict and explicit | one more step in a common case |

Under every alternative an undefined derived construction counts as absent (state 5): an
undefined object writes nothing, so excluding its reference would lose both.

### 6.6 Transitions

All transitions result from recomputing §6.3; nothing is synchronized.

| Action | Effect |
|---|---|
| import | state 1 |
| A-DXF1 creates a conversion or approximation | one Undo step in the construction and a `CREATED` entry → 2 or 3 |
| A-DXF1 fails | no construction and a `FAILED` entry → 6 |
| delete the derived construction | 5; Undo → 2 or 3 |
| Undo the creation | 5; Redo → 2 or 3 |
| move a control point, compatible redefinition | 4 while not applicable; Undo or an exact return → 2 or 3 |
| redefine into another family | the block disappears with the algorithm → 5 if no other member remains |
| copy/paste the derived construction in the same document | the copy keeps the block and represents `r` |
| paste the derived construction into another document | the block names an absent reference → 8 there (orphan); no reference affected |
| remove the reference | the reference and its entries disappear; derived constructions keep their passive block and become orphans (8) |
| reopen, Save As | both sides persist: same states |
| reimport the same file | a new reference, state 1 (7); never linked by data |

## 7. Inclusion policy

### 7.1 Defaults

With the option on: states 1, 4 (subject to RX-D2), 5, 6, 7 and 8 include the reference;
states 2 and 3 exclude it. The system never writes a reference and a construction that
validly represents it by an automatic decision. A user selection that does so is allowed
with the warning `DUPLICATE_REPRESENTATION`.

### 7.2 Hidden references, export area and selection

- Hidden references follow the hidden-source rule of the DXF contract §7: included,
  marked `60 = 1` and reported.
- Explicit export area (`geocedg-export-area-participation-b1/v1`): a reference is left
  out only when the convex hull of its control polygon is proved disjoint from `R` by
  exact predicates (the curve lies in that hull by positive weights and partition of
  unity); otherwise it participates and is written whole, never clipped. Possible
  over-inclusion is declared `PARTICIPATION_CONSERVATIVE`. A non-participating reference
  is `OUTSIDE_EXPORT_AREA` and never blocks.
- Selection export: references are not selectable; they enter only through the option.

### 7.3 Strictness

A reference included by default or by the user that cannot be written as `SPLINE` with
`REFERENCE_DEFINITION` blocks the operation (strict default, ADR 0014 decision 6) until the
user excludes it; it is never omitted silently and never replaced by a polyline. Every
record admissible under `dxf-spline-reference/v1` is writable in principle; a block can
only come from §5.2 or from a conformance restriction fixed later.

## 8. Extension point

A minimal extension, without turning references into model entities:

1. **Request.** The DXF dialog carries the option and the per-reference toggles in the
   request; the controller takes an immutable snapshot of the store (records, preferences,
   derivation entries) and of the resolver inputs, with its own staleness check, between
   the preflight and the encoding.
2. **Separate read-only adapter.** A reference adapter maps store records to immutable
   reference-export items (not `GeometryExportModel.Entity`); the resolver of §6 runs on
   the snapshot and on the construction's interpretable blocks.
3. **Encoding.** `GeometryExportService` encodes the construction model as today and then
   the reference items; `DxfExporter` appends the `SPLINE` entities after the construction
   loop, continues the handle sequence, updates `$HANDSEED` and adds `GEOCEDG_REF` to the
   layer table. With an empty reference set every byte is the current one.
4. **Result and sidecar.** `DxfEncodingResult` reports the reference handles separately;
   the sidecar validator counts model entities and reference entities separately.

The construction adapters (`GeoElementGeometryExportAdapter`, `G9X1GeometryExportAdapter`,
`SemanticCurveExportAdapter2D`) are not changed. The legacy G5 route is not extended.

## 9. Preflight

Preflight counts, separately from construction outcomes: references included, excluded by
state (2, 3, 4 under alternative B), excluded by the user, outside the export area, and not
writable. The writability rule becomes: writable when the construction contributes at least
one entity **or** at least one reference is included. A references-only export is
therefore possible.

## 10. Sidecar

Mandatory whenever at least one reference is written (decision RX-D12): the DXF alone
cannot say that an entity is external data, nor under which transformation. Schema
`org.geocedg.dxf.fidelity-manifest` with the next free `schema_version` (4, coordinated with
any `MAINT-A3` amendment; version 3 keeps its meaning). New section `external_references`:

- `policy` = `cedg-reference-export/v1`, `store_contract`, option state;
- per written reference: `reference_id`, `record_contract`, `nature`, `state`,
  `selection` (`DEFAULT`, `USER_INCLUDED`, `PREFERENCE_INCLUDED`), `fidelity`
  (`reference_definition`), `dxf_handle`, `entity_type`, `layer`, `weights_written`,
  `closed_flag`, `representation` (flag as read, weight provenance), `file_sha256`,
  `file_base_name`, `source_entity_handle`, `source_entity_layer`, `source_entity_ordinal`,
  `source_insunits`, `document_unit_at_import`, `source_plane_z`, `transform`,
  `source_digest`, `data_digest`, `derivations`;
- per excluded reference: `reference_id`, `state`, `reason` (`REPRESENTED_BY_EXACT_CONVERSION`,
  `REPRESENTED_BY_APPROXIMATION`, `DERIVED_CONSTRUCTION_MODIFIED`, `USER_EXCLUDED`,
  `PREFERENCE_EXCLUDED`, `OUTSIDE_EXPORT_AREA`) and, when represented, the construction
  sources that represent it;
- diagnostics `DUPLICATE_REPRESENTATION`, `ASSOCIATION_UNRESOLVABLE`,
  `ORPHAN_REFERENCE_PROVENANCE`, `SAME_DATA_AS_REFERENCE`, `PARTICIPATION_CONSERVATIVE`.

The DXF SHA-256 link, paired output and rollback follow the DXF contract §8 unchanged.

## 11. User interface

Compact (D-A-N2-5). In the existing DXF dialog, and only when the document has references:

- a check box **«Include imported DXF references»**, off by default (the ordinary export
  does not change);
- when on, one line: «3 will be included · 2 are already represented by constructions and
  are not duplicated» and a **«Review…»** link;
- «Review…» lists name · nature · state («No construction», «Converted», «Approximated»,
  «Derived construction modified», «Derived construction absent», «Approximation failed»,
  «Association unresolved») · an «Include» box with the default of §7. Toggles apply to the
  operation (decision RX-D10); the persistent preference is set in the reference manager;
- short notices: «References are written as SPLINE with their imported definition; they
  are not CeDG geometry.» and «A sidecar file will be created.»;
- **Details…**, the report and the sidecar: definition, transformation, provenance,
  identifiers, digests, derivations, fidelity and what it does not claim.

## 12. Validation obligations

Round trip and binary64 comparison (cases RX-1 to RX-16 of the
[A-N2 validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md)):

- re-import with the GeoCeDG reader: the new record's `sourceData` equals the exported
  `data` under `==`; nature equal; weights explicit iff written;
- extreme values: subnormals, the largest finite values, `-0.0`, values needing 17
  significant digits;
- ordinary export byte identity with the option off or without references;
- states 1 to 8 and the transitions of §6.6 built by identifiers; a construction identical
  to a reference but without block is state 1 for that reference.

External conformance is a **verifiable obligation** of `MAINT-A-DXF0-EXP`:

- readers: AutoCAD Core Console (AUDIT, entity data through AutoLISP) and a second reader
  designated by the author; the GeoCeDG reader does not count as independent;
- checks per golden file: the file opens without repair; the entity is a `SPLINE` with the
  written degree, knot and control-point counts; the reader's knots, control points and
  weights equal the written decimals under the nearest-binary64 rule; points evaluated by
  the reader at declared parameters agree with the library evaluation within a declared
  reading tolerance;
- scope and limits: evidence binds the reader name and version; it covers the golden
  fixtures only; it says nothing about a reader's own tessellation, display or later
  edits; a reader that reparametrizes or normalizes knots is recorded as such;
- a non-conforming reader for rational `SPLINE` is a stop condition (§14).

## 13. Risks

| Id | Risk | Mitigation |
|---|---|---|
| X-1 | readers that read flag 1 as periodic or reject the position of code 41 | conformance before fixing RX-D4 and RX-D5; stop condition |
| X-2 | the user takes a reference for CeDG geometry | layer, `999` comment, notices, sidecar |
| X-3 | duplication forced by the user | warning |
| X-4 | `schema_version` collision with `MAINT-A3` | coordinate in the amendment; serialize EXP and A3 |
| X-5 | an exact conversion exported as an approximate polyline until `MAINT-A3` while its reference is excluded | stated by the dialog; it is the author's rule (D-A-N2-3) |
| X-6 | stale state after Undo | states are computed, never stored |

## 14. Stop conditions

Stop if: a conformance reader rejects or misreads the rational `SPLINE`; writing would need
transforming or recomputing data beyond formatting; the ordinary export changes one byte;
an association would need proximity, data digests, labels or handles; the
correspondence would need a DAG dependency; the implementation would need the construction
`SPLINE` writer of `MAINT-A3`.

## 15. Open decisions

| Id | Question | Recommendation | Consequence |
|---|---|---|---|
| RX-D1 | Surface | check box in the existing DXF dialog | no new menu action |
| RX-D2 | State 4 | alternative A (include) | see §6.5 |
| RX-D3 | Weights | canonical: effective weights only when some differs from 1 | the source representation is reported, not reproduced |
| RX-D4 | Position of code 41 | the form that passes conformance; candidates: one 41 after each control point, or a block after the knots | the GeoCeDG reader accepts both |
| RX-D5 | Flag 1 | only on exact closure | subject to conformance |
| RX-D6 | 210/220/230 and 42/43 | the minimum conformance requires | — |
| RX-D7 | Layer | fixed `GEOCEDG_REF` | alternative: original layer names (not ASCII-safe; provenance turned into presentation) |
| RX-D8 | Hidden references | included with `60 = 1` | coherent with DXF contract §7 |
| RX-D9 | Export area | conservative control-polygon hull | alternatives: ignore the area, or exclude all references |
| RX-D10 | Dialog toggles | per operation; persistent preference only in the manager | exporting never dirties the document |
| RX-D11 | Coordinate system | model (`data`) | a source-system option later |
| RX-D12 | Sidecar | mandatory with references | always paired output |
| RX-D13 | Sequence | strict: this contract and the store model types of `R-c` (milestone RC-M); integration after `MAINT-A-DXF0-RC` in the author's order; may precede A-DXF1 (only states 1, 7 and user selection occur before it) | aligned with the implementation plan §2 |
| RX-D15 | References with identical data (state 7) | both included by default as distinct references; the summary line and «Review…» flag them with `SAME_DATA_AS_REFERENCE`; the user may exclude either | deduplicating by data would be an association by coordinates (D-A-N2-3); the automatic-duplication rule concerns a reference and a construction that represents it |
| RX-D14 | Normative form | this contract + amendments + ADR 0045 | — |
