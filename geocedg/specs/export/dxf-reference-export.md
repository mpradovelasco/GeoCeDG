# DXF export of imported references — `cedg-reference-export/v1`

| Field | Value |
|---|---|
| Status | **CANDIDATE — READY FOR AUTHOR ADOPTION (revision R1).** Normative design written by `PRE-G9B-MAINT-A-N2` and refined by `PRE-G9B-MAINT-A-N2-R1`. `selfApproved=false`. It authorizes no code: `MAINT-A-DXF0-EXP` is not authorized. It does not change the approved export contracts until the author approves the candidate amendments that reference it |
| Revision | R1 (2026-11-25): state 4 fixed as alternative A (D-R1-6); no failure state and no store derivation history (D-R1-5); resolved-state model §6.4 with the eight cases mapped in §6.5; conformance with AutoCAD Core Console and ezdxf (D-R1-7, §12.1); no novelty flag (D-R1-10). The A-N2 text is commit `874286e95` |
| Contract identifier | `cedg-reference-export/v1` (policy and population); fidelity class `REFERENCE_DEFINITION` |
| Base | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` |
| Architectural layer | Export adapter outside the geometric kernel (AGENTS §4, §13); a separate read-only reference adapter beside the construction adapters |
| Depends on | [`cedg-reference-store/v1`](../import/reference-store.md) (reference identity, store snapshot, export preference); [`dxf-spline-approximation/v1`](../import/dxf-spline-approximation.md) for the states that involve a derived construction; it does **not** presuppose `MAINT-A3` |
| Amends (candidates) | [`dxf-curve-fidelity-and-approximation.md`](dxf-curve-fidelity-and-approximation.md), [`geometry-export-foundation.md`](geometry-export-foundation.md), [`dxf-spline-reference.md`](../import/dxf-spline-reference.md) §0, §10, §14, §17, [`bspline-2d.md`](../curves/bspline-2d.md) §10.5, ADR 0014 decision 8 (by [ADR 0045](../../../docs/adr/0045-separate-dxf-export-of-imported-references.md), proposed); texts in the [candidate amendment set](../../../docs/architecture/pre_g9b_maint_a_n2_candidate_amendments.md) |
| Author directions | D-A-N2-1, D-A-N2-3, D-A-N2-4, D-A-N2-5 (A-N2 instruction); D-R1-1, D-R1-5, D-R1-6, D-R1-7, D-R1-10 (R1 instruction) |

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

**Portable exponents (pending author decision RX-D19).** `Double.toString` writes values with
`|x| < 10^-99` (non-zero) or `|x| ≥ 10^100` with a three-digit exponent, and the R1 pilot
showed that AutoCAD 2026 silently truncates such exponents to two digits (`4.9E-324` read as
`4.9e-32`), while a plain-decimal form of `Double.MAX_VALUE` made it hang. Proposed rule: a
reference containing such a value is `NOT_WRITABLE` with reason `EXPONENT_NOT_PORTABLE`
(strict, never silently rewritten). Alternative: plain decimals for the small values and
refusal of the large ones.

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

### 6.2 Minimal correspondence record (R1)

1. **Construction side** (written by `MAINT-A-DXF1` on success only): the passive block
   `approximationProvenance` of `<geocedgBSpline>`. The resolver reads exactly
   `referenceId`, `operation`, `referenceDataSha256` and `definitionDigest` (for
   applicability) (`dxf-spline-approximation/v1` §10). It travels with copy, Undo/Redo and
   persistence; it is never a DAG input.
2. **Reference side** (store, outside Undo): only the export preference
   `AUTO | INCLUDE | EXCLUDE` (`cedg-reference-store/v1` §5.2). The store keeps **no**
   derivation history: failed attempts leave no trace (author decision D-R1-5), and
   successful ones are represented by their construction alone.

The durable identity of a construction (`geocedgId`) is **not** part of the resolution: a
copy pasted in the same document keeps the block and still represents the reference after
the original is deleted, and identity survival through redefinition is decided by ADR 0026,
not by this contract.

### 6.3 Resolution

At each export, for reference `r`, let `L(r)` be the defined `BSpline` outputs of
`Construction` whose block is interpretable and whose `referenceId` equals `r.referenceId`
(exact string equality). A block that is `NOT_INTERPRETABLE` but whose `referenceId` attribute is a
legible 32-hex identifier still **names** that reference (§6.4, `ASSOCIATION_UNRESOLVED`); a block
without a legible `referenceId` names no reference. A member of `L(r)` **validly represents** `r` when its
applicability (`dxf-spline-approximation/v1` §11) is `APPLICABLE` and its
`referenceDataSha256` equals the `dataDigest` of `r`. The reading of these attributes by the
resolver is informative: it decides only the inclusion of the reference, never the export
of the construction (candidate amendment A-BS-1). Everything is computed from the current
`Construction` and store at the moment of export; no state is stored.

### 6.4 Resolved states

| State | Condition (first that holds, in this order) | Reference by default (option on) | Diagnostic |
|---|---|---|---|
| `USER_CHOICE` | the user included or excluded `r` in the operation, else its persistent preference is `INCLUDE` or `EXCLUDE` | as chosen; the underlying state below is still reported | `DUPLICATE_REPRESENTATION` when an included `r` is validly represented |
| `REPRESENTED_EXACT` | some member of `L(r)` validly represents `r` with `operation = EXACT_CONVERSION` | **excluded** | `REPRESENTED_BY_EXACT_CONVERSION` |
| `REPRESENTED_APPROXIMATION` | some member validly represents `r` with `operation = APPROXIMATION` | **excluded** | `REPRESENTED_BY_APPROXIMATION` |
| `RELATED_CONSTRUCTION_MODIFIED` | `L(r)` non-empty, no member validly represents `r`, some member is `NOT_APPLICABLE_DEFINITION_CHANGED` | **included** (author decision D-R1-6, alternative A) | `RELATED_CONSTRUCTION_MODIFIED` |
| `ASSOCIATION_UNRESOLVED` | a construction names `r` but its block is `NOT_INTERPRETABLE`, or its `referenceDataSha256` differs from the `dataDigest` of `r` | **included** | `ASSOCIATION_UNRESOLVABLE` |
| `NO_CEDG_REPRESENTATION` | none of the above | **included** | — |

Constructions are never filtered by this table: every construction is exported from
`Construction` by its approved route, whatever the state of a reference it names.

### 6.5 The eight cases of the author instruction

| # | Case | Resolved state | Reference by default | Construction | Duplication and its avoidance | Explicit selection | Diagnostic |
|---|---|---|---|---|---|---|---|
| 1 | never converted | `NO_CEDG_REPRESENTATION` | included | none | none | may exclude | — |
| 2 | converted exactly | `REPRESENTED_EXACT` | excluded | exported from `Construction` by its route and guarantees | avoided by excluding the reference | may include: both written, warning | `REPRESENTED_BY_EXACT_CONVERSION`, then `DUPLICATE_REPRESENTATION` if included |
| 3 | approximated satisfactorily | `REPRESENTED_APPROXIMATION` | excluded | as in 2 | as in 2 | as in 2 | `REPRESENTED_BY_APPROXIMATION` |
| 4 | derived construction modified | `RELATED_CONSTRUCTION_MODIFIED` | **included** (D-R1-6) | exported from `Construction` (the current object of the user) | not a duplicate of the same definition; a short notice says that a modified related construction exists | may exclude | `RELATED_CONSTRUCTION_MODIFIED` |
| 5 | derived construction deleted (or undone, replaced by another family, or undefined) | `NO_CEDG_REPRESENTATION` | included | none written | none | may exclude | — (the history is not kept) |
| 6 | approximation failed | `NO_CEDG_REPRESENTATION` (the failure is a transient operation result, never a document state) | included | none | none | may exclude | — |
| 7 | reference copied or reimported | each record resolved on its own; a new record `r'` has a new `referenceId` and no construction names it | `r'` included unless represented by its own derivations; `r` keeps its state | constructions derived from `r` never represent `r'` | possible when `r` and `r'` are both included; never resolved by data | may exclude either | `SAME_DATA_AS_REFERENCE` (informative, flagged in «Review…») |
| 8 | provenance not current or association unresolvable | `ASSOCIATION_UNRESOLVED` for `r`; orphan constructions affect no reference | included | exported normally | not resolved automatically; notice | may exclude | `ASSOCIATION_UNRESOLVABLE` on `r`; `ORPHAN_REFERENCE_PROVENANCE` on the construction (sidecar) |

An undefined related construction writes nothing; it counts as absent (case 5), so that
excluding its reference would never lose both. The historical existence of a conversion or
approximation is never a current representation: only a defined construction whose block
is applicable represents a reference.

### 6.6 Transitions

All transitions result from recomputing §6.3; nothing is synchronized or stored.

| Action | Effect |
|---|---|
| import | `NO_CEDG_REPRESENTATION` |
| A-DXF1 creates a conversion or approximation | one Undo step in the construction → `REPRESENTED_EXACT` or `REPRESENTED_APPROXIMATION`; the store does not change |
| A-DXF1 fails | nothing changes; the cause is shown and kept in the session report only |
| delete the derived construction; Undo of its creation; redefinition into another family | `NO_CEDG_REPRESENTATION` if no other member remains; Undo/Redo restore the previous state |
| move a control point, compatible redefinition | `RELATED_CONSTRUCTION_MODIFIED` while not applicable; Undo or an exact return → represented again |
| copy/paste the derived construction in the same document | the copy keeps the block and represents `r` |
| paste the derived construction into another document | the block names an absent reference: orphan there; no reference affected |
| remove the reference | the reference disappears; derived constructions keep their passive block and become orphans |
| save and reopen, Save As | same states, recomputed from the reopened document |
| reimport the same file | a new reference (case 7); never linked by data |

## 7. Inclusion policy

### 7.1 Defaults

With the option on: references in `NO_CEDG_REPRESENTATION`, `RELATED_CONSTRUCTION_MODIFIED`
and `ASSOCIATION_UNRESOLVED` are included; references validly represented
(`REPRESENTED_EXACT`, `REPRESENTED_APPROXIMATION`) are excluded. The system never writes a
reference and a construction that validly represents it by an automatic decision. A user
selection that does so is allowed with the warning `DUPLICATE_REPRESENTATION`. With the option
off (the default of every export), no reference is written.

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
   request; the controller takes an immutable snapshot of the store (records and
   preferences) and of the resolver inputs, with its own staleness check, between
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

Preflight counts, separately from construction outcomes: references included, excluded as
validly represented (`REPRESENTED_EXACT`, `REPRESENTED_APPROXIMATION`), excluded by the user or
by preference, outside the export area, and not writable. The writability rule becomes: writable when the construction contributes at least
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
  `source_digest`, `data_digest`, and, for `RELATED_CONSTRUCTION_MODIFIED`, the related
  construction sources;
- per excluded reference: `reference_id`, `state`, `reason` (`REPRESENTED_BY_EXACT_CONVERSION`,
  `REPRESENTED_BY_APPROXIMATION`, `USER_EXCLUDED`, `PREFERENCE_EXCLUDED`,
  `OUTSIDE_EXPORT_AREA`) and, when represented, the construction sources that represent it;
- diagnostics `RELATED_CONSTRUCTION_MODIFIED`, `DUPLICATE_REPRESENTATION`,
  `ASSOCIATION_UNRESOLVABLE`, `ORPHAN_REFERENCE_PROVENANCE`, `SAME_DATA_AS_REFERENCE`,
  `PARTICIPATION_CONSERVATIVE`.

The DXF SHA-256 link, paired output and rollback follow the DXF contract §8 unchanged.

## 11. User interface

Compact (D-A-N2-5). In the existing DXF dialog, and only when the document has references:

- a check box **«Include imported DXF references»**, off by default (the ordinary export
  does not change);
- when on, one line: «3 will be included · 2 are already represented by constructions and
  are not duplicated» and a **«Review…»** link;
- «Review…» lists name · nature · state («No CeDG representation», «Converted»,
  «Approximated», «Related construction modified», «Association unresolved») · an «Include»
  box with the default of §7. For «Related construction modified» a short notice says that a
  modified construction derived from this reference exists and will also be exported. Toggles
  apply to the operation (decision RX-D10); the persistent preference is set in the
  reference manager. References with identical data are flagged (RX-D15);
- short notices: «References are written as SPLINE with their imported definition; they
  are not CeDG geometry.» and «A sidecar file will be created.»;
- **Details…**, the report and the sidecar: definition, transformation, provenance,
  identifiers, digests, related constructions, fidelity and what it does not claim;
- the option is part of the existing DXF export action, with no activation flag of its own
  (author decision D-R1-10); its visible texts never say «experimental». Reconciliation with
  AGENTS §5 (proposed): the option inherits the formal state of that action (`export.dxf-2d`,
  whose flag predates this contract and is not a novelty flag); while `MAINT-A-DXF0-EXP` is open
  the option exists only on its branch, and its state is declared by the author at closeout.

## 12. Validation obligations

Round trip and binary64 comparison (cases RX-1 to RX-18 of the
[A-N2 validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md)):

- re-import with the GeoCeDG reader: the new record's `sourceData` equals the exported
  `data` under `==`; nature equal; weights explicit iff written;
- extreme values: subnormals, the largest finite values, `-0.0`, values needing 17
  significant digits;
- ordinary export byte identity with the option off or without references;
- the eight cases of §6.5 and the transitions of §6.6 built by identifiers; a construction
  identical to a reference but without block is `NO_CEDG_REPRESENTATION` for that
  reference; a failed approximation changes no state, before or after save and reopen.

### 12.1 External conformance (author decision D-R1-7)

External conformance is a **verifiable obligation** of `MAINT-A-DXF0-EXP`, with two readers
fixed by the author:

1. **AutoCAD Core Console** (`accoreconsole`), version identified and recorded at run time;
2. **ezdxf**, a Python library used only as an external validation tool, with its version
   pinned in the evidence; it is never a geometric authority of GeoCeDG and never a product
   dependency.

The GeoCeDG reader does not count as independent (it shares the library). Each golden file
of the procedure (fixtures and scripts kept with the evidence, SHA-256 recorded) is checked
in two separated parts:

| Part | What is compared | Rule |
|---|---|---|
| **Definition reading** | entity type, degree, original knots, original control points, weights, flags, counts, domain `[u_p, u_{n+1}]` as read from the entity data (AutoLISP `entget`; ezdxf entity attributes, never its derived math object) | equal to the written decimals under the nearest-binary64 rule, every value compared exactly and as an ulp distance; any difference is a failure for ezdxf; for AutoCAD the rule of RX-D18 applies |
| **Mathematical evaluation** | points on the curve at declared parameters, behavior at a multiple knot, rational arcs on the unit circle, orientation (start `P_0`, end `P_n`), closure | the reader's evaluation (ezdxf `ezdxf.math.BSpline` built from the raw lists, never `construction_tool()`; AutoCAD `vlax-curve-getPointAtParam`) against an independent exact-rational reference within a tolerance relative to the largest control-point magnitude; subnormal-width spans excluded |

A reader's math object may normalize or correct data when it builds its curve (for example
knot rescaling, weight normalization, flag inference). Every such transformation is
recorded and compared with the rules of §4; it never counts as a reading of the definition.
Evidence binds the reader name, version and fixtures; it covers those fixtures only. Passing
it is not a claim of universal interoperability. A reader that rejects or misreads a rational
`SPLINE` definition is a stop condition (§14), subject to decision RX-D18 below.

### 12.2 Pilot evidence of R1 (2026-11-25)

The R1 pilot (R1 report §F; 20 hand-written fixtures, SHA-256 recorded) used AutoCAD Core
Console 2026 (`ACADVER` 25.1) and ezdxf 1.4.2:

- **ezdxf** reads every written definition exactly (degree, knots, control points, weights,
  flags as written); its `construction_tool()` rounds knots to the decimal places implied by
  code 42 (10 by default), so evaluation is built from the raw lists (`ezdxf.math.BSpline`),
  never through `construction_tool()`.
- **AutoCAD** opens every fixture with AUDIT clean and reads the golden rational fixtures
  exactly, but its decimal parse is not correctly rounded in general: in a sweep of 2 822
  values, 268 came back 1 ulp off and up to 3 ulp near `10^-50`; three-digit exponents are
  truncated (§5.2). It derives closure from the data, reports `70 = 15` for coincident end
  points without treating the curve as periodic, and ignores written 42/43.
- Both readers' evaluations agree with an exact rational evaluation within about `1.5e-16` on
  unit-scale fixtures, at a double knot, at the ends and on the unit circle. A span of
  subnormal width defeats both: such spans are excluded from evaluation claims.

**Pending author decision RX-D18 (AutoCAD reading rule).** Either (A, recommended) the
definition-reading check keeps exact equality for ezdxf and, for AutoCAD, records the ulp
distance of every value and accepts at most 4 ulp as a documented limitation of that reader's
decimal parsing (the fourth rounding of §5.3, outside the `REFERENCE_DEFINITION` claim); or
(B) AutoCAD's misreading is the stop condition of §14. The class `REFERENCE_DEFINITION` itself
is unaffected: it is defined by the nearest-binary64 rule, which ezdxf and the GeoCeDG reader
follow exactly.

## 13. Risks

| Id | Risk | Mitigation |
|---|---|---|
| X-1 | readers that read flag 1 as periodic or reject the position of code 41 | conformance before fixing RX-D4 and RX-D5; stop condition |
| X-2 | the user takes a reference for CeDG geometry | layer, `999` comment, notices, sidecar |
| X-3 | duplication forced by the user | warning |
| X-4 | `schema_version` collision with `MAINT-A3` | coordinate in the amendment; serialize EXP and A3 |
| X-5 | an exact conversion exported as an approximate polyline until `MAINT-A3` while its reference is excluded | stated by the dialog; it is the author's rule (D-A-N2-3) |
| X-6 | stale state after Undo | states are computed, never stored |
| X-7 | AutoCAD misreads some decimals by 1–3 ulp and truncates three-digit exponents (R1 pilot) | RX-D18, RX-D19; recorded per reader version |
| X-8 | a closed reference re-saved by AutoCAD carries `70 = 15` (bit 2) and the approved import rejects it as `PERIODIC_UNSUPPORTED` | outside this contract: an import-contract question for the author (R1 report §K) |

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
| RX-D2 | State 4 | alternative A: include the reference; the modified construction is exported from `Construction`; short notice | **fixed by the author (D-R1-6)** |
| RX-D3 | Weights | canonical: effective weights only when some differs from 1 | the source representation is reported, not reproduced |
| RX-D4 | Position of code 41 | one 41 after each control point (`10/20/30/41`), the form AutoCAD's own writer emits; both readers and the GeoCeDG reader accept both forms (R1 pilot) | proposed |
| RX-D5 | Flag 1 | only on exact closure; neither reader treats it as periodic; ezdxf exposes it as `.closed` (R1 pilot) | proposed |
| RX-D6 | 210/220/230 and 42/43 | write `0/0/1`; omit 42/43 (both readers accept; evaluation avoids ezdxf's knot rounding by using the raw lists) | proposed |
| RX-D7 | Layer | fixed `GEOCEDG_REF` | alternative: original layer names (not ASCII-safe; provenance turned into presentation) |
| RX-D8 | Hidden references | included with `60 = 1` | coherent with DXF contract §7 |
| RX-D9 | Export area | conservative control-polygon hull | alternatives: ignore the area, or exclude all references |
| RX-D10 | Dialog toggles | per operation; persistent preference only in the manager | exporting never dirties the document |
| RX-D11 | Coordinate system | model (`data`) | a source-system option later |
| RX-D12 | Sidecar | mandatory with references | always paired output |
| RX-D13 | Sequence | strict: this contract, the store model types of `R-c` (milestone RC-M) and the read-only provenance parser with applicability (milestone DXF1-M); integration after `MAINT-A-DXF0-RC` in the author's order; may precede A-DXF1 (before it only `NO_CEDG_REPRESENTATION`, case 7 and user choices occur) | aligned with the implementation plan §2 |
| RX-D16 | Conformance readers | AutoCAD Core Console and ezdxf (pinned), definition reading separated from evaluation (§12.1) | **fixed by the author (D-R1-7)** |
| RX-D17 | Failed approximations | never an export state; no persisted history | **fixed by the author (D-R1-5)** |
| RX-D18 | AutoCAD definition-reading rule | A: exact for ezdxf; AutoCAD within 4 ulp, recorded as a reader limitation; B: stop condition | **pending author decision** (§12.2) |
| RX-D19 | Values with three-digit exponents | refuse as `NOT_WRITABLE`/`EXPONENT_NOT_PORTABLE`; alternative: plain decimals for small values | **pending author decision** (§5.2) |
| RX-D15 | References with identical data (case 7) | both included by default as distinct references; the summary line and «Review…» flag them with `SAME_DATA_AS_REFERENCE`; the user may exclude either | deduplicating by data would be an association by coordinates (D-A-N2-3); the automatic-duplication rule concerns a reference and a construction that represents it |
| RX-D14 | Normative form | this contract + amendments + ADR 0045 | — |
