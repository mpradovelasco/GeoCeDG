# DXF export of imported references — `cedg-reference-export/v1`

| Field | Value |
|---|---|
| Status | **APPROVED — NORMATIVE CONTRACT `cedg-reference-export/v1`, REVISION R4, AUTHOR APPROVED (2026-11-26)** with ADR 0045 accepted and the group-X amendments applied to the approved export contracts ([approval record](../../../docs/validation/pre_g9b_maint_a_n2_author_approval_record.md)). The approved text is that of commit `9c7e612294a2a0521a2c21d8f9c4580cb3b34efb` (tree `7faccc1fc`). `selfApproved=false`. Approval is not implementation. `MAINT-A-DXF0-EXP` is not authorized; the external conformance evidence (RX-16, RX-17, RX-19) remains an obligation of that increment. Where the sections below call this document a candidate, proposed or ready for adoption, or speak of amendments «when accepted», that records the stage at which they were written and is superseded by this status |
| Revision | R4 (2026-11-26, independent R3 audit m-1 to m-3): the two informative reads of the provenance block (§6); RX-D19 not extended to references excluded for other causes (author decision); the empty-population guard of the current controller recorded (§2) with the references-only route as an obligation of `MAINT-A-DXF0-EXP` (§9.1, RX-15 (e)); the output normal `0/0/1` is never attributed to the source DXF (§4). R1 (2026-11-25): state 4 fixed as alternative A (D-R1-6); no failure state and no store derivation history (D-R1-5); resolved-state model §6.4 with the eight cases mapped in §6.5; conformance with AutoCAD Core Console and ezdxf (D-R1-7, §12.1); no novelty flag (D-R1-10). R2 (2026-11-25): RX-D18 (levels of evidence and AutoCAD acceptance criteria, §12.3) and RX-D19 (`EXPONENT_NOT_PORTABLE`, §5.2) decided by the author. R3 (2026-11-25, pre-adoption audit): sidecar situations A, B and C (§10, M2); references-only export bounded by writable references (§9, M3); 4 ulp tied to AutoCAD 2026; feature identifiers. The A-N2 text is commit `874286e95`; the R1 text is commit `228d91d44`; the R2 text is commit `101f574ab` |
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
| (R4) `GeoCeDGDxfExportController.showExportDialog` runs the legacy G5 flow (`showLegacyExportDialog`, no preflight or sidecar) unless the extended flag is on, and then the extended paired-output flow (`showExtendedExportDialog`, request dialog `chooseRequest`). On an empty chosen `GeoElement` population both show «The selected export population is empty.» before any model or preflight: the legacy flow ends, the extended flow reopens its request dialog | a references-only export never reaches the preflight of §9 through the current controller; the legacy G5 route is not extended (§8), so the reference option and the route of §9.1 belong to the extended flow only, which `MAINT-A-DXF0-EXP` must add |
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
| 210/220/230 | `0.0 / 0.0 / 1.0` | XY plane; written or omitted as fixed by the conformance evidence (decision RX-D6). It is the output normal of the 2D curve, never the source groups 210/220/230 that the store records (`cedg-reference-store/v1` §5.2 item 11); those are never written into the DXF nor attributed to it (R4) |
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

**Portable exponents (author decision RX-D19 of 2026-11-25, R2).** The rule is on the token: a
value is non-portable when the decimal token that the writer would emit has an exponent of three
digits. With the `Double.toString` of the product's Java runtime this happens exactly for
non-zero values with `|x| < 10^-99` (negative exponent, `E-100` to `E-324`) or
`|x| ≥ 10^100` (positive exponent, `E100` to `E308`), for either sign of the value; the
range is explanatory and the token test decides.
Such a token is valid DXF and is read correctly by ezdxf and by the GeoCeDG reader, but the R1
pilot showed that AutoCAD 2026 silently truncates it to two exponent digits (`4.9E-324` read as
`4.9e-32`, `1.0E100` as `1e10`). The initial, conservative policy is:

1. Before writing, every numeric field of the entity that the writer emits from data (control
   point coordinates `x` and `y`, knots, weights) is checked; the fixed fields (`z = 0.0`, the
   normal `0/0/1`) never need it. A reference with at least one value whose token would carry a
   three-digit exponent, whatever the sign of the exponent or of the value, is `NOT_WRITABLE`
   with reason `EXPONENT_NOT_PORTABLE`; the diagnostic names the field kind, the index and the
   value.
2. It is never written silently, never rewritten in another decimal form, never replaced by a
   polyline, never approximated. The strict rule of §7.3 applies: while such a reference is
   included, the operation is rejected (situation C of §10: no DXF and no sidecar; the cause is
   in the operation result and report); once the user excludes it, the export can complete and
   the sidecar records the exclusion with its cause (situation B of §10: `USER_EXCLUDED` with
   the diagnostic `EXPONENT_NOT_PORTABLE`).
3. The rule is a portability policy for the AutoCAD destination, not a DXF format violation and
   not a limitation of every reader. A plain-decimal writing (without exponent) is a later
   improvement, adopted only if conformance tests prove both its AutoCAD compatibility and its
   fidelity; the R1 pilot found plain decimals correct for subnormal values but a hang of
   AutoCAD on a plain-decimal `Double.MAX_VALUE`.

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
of the construction (candidate amendment A-BS-1). The only other informative read of these
attributes is the presentation of the same state to the user (`dxf-spline-approximation/v1`
§14); neither is a geometric dependency or changes the DAG. Everything is computed from the current
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
`REFERENCE_DEFINITION` makes the operation rejected (strict default, ADR 0014 decision 6;
situation C of §10): no DXF and no sidecar are produced, nothing partial is presented as a
success, and the result and report of the operation give the cause; the user may change the
selection and export again. Such a reference is never omitted silently and never replaced by a
polyline. Every record admissible under `dxf-spline-reference/v1` is writable in principle,
except a record with non-portable exponents (§5.2); a rejection can only come from §5.2
(`WRITE_ROUND_TRIP_FAILED`, `EXPONENT_NOT_PORTABLE`) or from a conformance restriction
fixed later.

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
by preference, outside the export area, and not writable. The writability rule becomes:
writable when the construction contributes at least one entity **or** at least one reference is
included and writable after resolving preferences, states, user choices and the export area. A
references-only export is therefore possible; references never enter the neutral model as
model entities. When neither an entity nor a writable reference remains, no file is written
(the approved rule of the foundation, amendment A-GEF-1). With the option off nothing changes.

### 9.1 Controller route for a references-only export (R4, implementation obligation)

The current controller rejects an empty `GeoElement` population before the preflight in both
of its flows (§2). The legacy G5 flow is not extended (§8): it offers no reference option, and
its guard, message and behavior stay exactly as today. `MAINT-A-DXF0-EXP` must add, **in the
extended paired-output flow only**, a route in which, **with the reference option chosen**, an
empty construction population (an empty selection or a document without exportable
`GeoElement`s) does not end the operation before the reference population is resolved: the
operation continues to the preflight of §9 and is writable when at least one reference is
included and writable; when none is, the operation ends without writing a file and with a
message that states both populations. **With the option off**, the guard of the extended flow,
its message and its timing stay exactly as today, and the exports of construction geometry are
unchanged in both flows. The discriminating case is RX-15 (e).

## 10. Sidecar

The rule distinguishes three situations of an operation with the reference option on (decision
RX-D12):

| Situation | Outcome | Sidecar |
|---|---|---|
| **A.** at least one reference admitted and written | DXF and sidecar, paired | mandatory: every written reference with its definition and fidelity; every excluded one with its cause |
| **B.** the export completes and at least one reference is excluded, by the user, by preference or by the approved policy (represented, outside the area, or excluded by the user after `EXPONENT_NOT_PORTABLE`; that cause alone never excludes a reference automatically: while it stays included the situation is C) | DXF (construction entities and any written references) and sidecar, paired | mandatory: records every exclusion and its cause, also when no reference is written |
| **C.** the operation is rejected because an included reference is `NOT_WRITABLE` (§7.3) | no DXF and no sidecar; nothing partial presented as a success | none: the rejection and its cause are given by the result and report of the operation; the user may change the selection and retry |

In A and B the sidecar is mandatory because the DXF alone cannot say that an entity is external
data, under which transformation, or which references were left out and why. When no entity and
no writable reference remain, no file is written (§9). Schema
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
  `PARTICIPATION_CONSERVATIVE`, and `EXPONENT_NOT_PORTABLE` (field kind, index, value) for a
  reference excluded by the user for that reason.

The DXF SHA-256 link, paired output and rollback follow the DXF contract §8 unchanged.

## 11. User interface

Compact (D-A-N2-5). In the existing DXF dialog of the extended paired-output flow (the legacy
G5 route is not extended, §8 and §9.1), and only when the document has references (with
`cedg.export.dxf.extended` off, which is on by default and turned off by `--enableExtendedDxf=false`,
only the legacy G5 flow runs, and no reference can be exported):

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
  AGENTS §5 (proposed): the option inherits the formal state of that action (profile action
  `export.dxf-2d`, governed by the existing feature flags `cedg.export.dxf.2d` and
  `cedg.export.dxf.extended`, both `experimental` in `geocedg/features/experimental.yml`; these flags
  predate this contract and are not novelty flags); while `MAINT-A-DXF0-EXP` is open
  the option exists only on its branch, and its state is declared by the author at closeout.

## 12. Validation obligations

Round trip and binary64 comparison (cases RX-1 to RX-18 of the
[A-N2 validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md)):

- re-import with the GeoCeDG reader: the new record's `sourceData` equals the exported
  `data` under `==`; nature equal; weights explicit iff written;
- extreme values whose tokens keep at most two exponent digits (from `1.0E-99` to
  `Math.nextDown(1.0E100)`, both signs), `-0.0`, values needing 17 significant digits;
  subnormals, `Double.MAX_VALUE` and every other value with a three-digit exponent are covered by
  the refusal of §5.2 (RX-19);
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
| **Definition reading** | entity type, degree, original knots, original control points, weights, flags, counts, domain `[u_p, u_{n+1}]` as read from the entity data (AutoLISP `entget`; ezdxf entity attributes, never its derived math object) | equal to the written decimals under the nearest-binary64 rule, every value compared exactly and as an ulp distance; any difference is a failure for ezdxf; AutoCAD follows §12.3 |
| **Mathematical evaluation** | points on the curve at declared parameters, behavior at a multiple knot, rational arcs on the unit circle, orientation (start `P_0`, end `P_n`), closure | the reader's evaluation (ezdxf `ezdxf.math.BSpline` built from the raw lists, never `construction_tool()`; AutoCAD `vlax-curve-getPointAtParam`) against an independent exact-rational reference within a tolerance relative to the largest control-point magnitude; subnormal-width spans excluded |

A reader's math object may normalize or correct data when it builds its curve (for example
knot rescaling, weight normalization, flag inference). Every such transformation is
recorded and compared with the rules of §4; it never counts as a reading of the definition.
Evidence binds the reader name, version and fixtures; it covers those fixtures only. Passing
it is not a claim of universal interoperability. A reader that rejects a rational `SPLINE`
definition, or reads it outside the acceptance criteria of §12.3, is a stop condition (§14).

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

### 12.3 Levels of evidence and acceptance criteria (author decision RX-D18, R2)

Three levels are kept distinct; the limitations of one reader are never attributed to the
others:

| Level | What it establishes | Criterion |
|---|---|---|
| **DXF format compliance** | the entity follows §4 and the DXF reference structure | checked on the written text (groups, counts, flags, order); independent of any reader |
| **Fidelity `REFERENCE_DEFINITION`** | the written decimals denote the record's binary64 values | the correctly rounded (nearest-binary64) re-reading of every value equals the record (§5.1, §5.2); verified by the GeoCeDG write check and by **ezdxf**, whose definition reading must be **exact** for every value |
| **AutoCAD conformance** | AutoCAD Core Console reads and evaluates the entity acceptably | the criteria below |

AutoCAD conformance criteria (alternative A of RX-D18, with the author's conditions):

1. The 1–3 ulp differences observed in AutoCAD 2026 are an **empirical limitation of that
   reader's decimal parsing** (the fourth rounding of §5.3). They do not change the fidelity
   class, which keeps referring to the binary64 data represented in the DXF and to their
   correctly rounded re-reading.
2. **Per-value criterion:** every value read through `entget` is within **4 ulp** of the
   written binary64 value. This threshold is the initial acceptance criterion of the AutoCAD
   conformance tests for AutoCAD 2026 (`ACADVER` 25.1) only; it is neither a bound of the
   product nor a property of the reader. Another AutoCAD version reopens the criterion on its own
   evidence.
3. **Structural criterion**, required in addition: the degree; the number of knots and of
   control points; the knot multiplicities (values equal in the record stay equal, distinct
   values stay distinct, the order is preserved); the domain `[u_p, u_{n+1}]` stays non-empty
   with the same clamped structure; closure (exact equality of `P_0` and `P_n` in the record is
   preserved, and inequality is not turned into equality); the weight conditions (positivity,
   all-equal weights stay all-equal, unit weights stay unit, the rational or polynomial nature
   unchanged). AutoCAD's own normalizations recorded in §12.2 (inferred closure reported as
   `70 = 15` without periodic treatment, ignored 42/43) are accepted only as listed there.
4. **Geometric criterion**, required in addition: the points AutoCAD evaluates at the declared
   parameters agree with the exact-rational reference within the test tolerance (relative to
   the largest control-point magnitude), including at multiple knots and at the ends.
5. If a difference changes a structural property or produces a geometric deviation outside
   the test tolerance, AutoCAD conformance is **not** declared for that fixture, even when every
   value is within 4 ulp.
6. The evidence records the reader name and version, the fixtures and their SHA-256, the
   maximum observed ulp distance per field kind (coordinates, knots, weights), the maximum
   geometric deviation, and the limits of the test (fixtures covered, excluded subnormal-width
   spans, the values checked by §5.2 that are never written).

## 13. Risks

| Id | Risk | Mitigation |
|---|---|---|
| X-1 | readers that read flag 1 as periodic or reject the position of code 41 | conformance before fixing RX-D4 and RX-D5; stop condition |
| X-2 | the user takes a reference for CeDG geometry | layer, `999` comment, notices, sidecar |
| X-3 | duplication forced by the user | warning |
| X-4 | `schema_version` collision with `MAINT-A3` | coordinate in the amendment; serialize EXP and A3 |
| X-5 | an exact conversion exported as an approximate polyline until `MAINT-A3` while its reference is excluded | stated by the dialog; it is the author's rule (D-A-N2-3) |
| X-6 | stale state after Undo | states are computed, never stored |
| X-7 | AutoCAD misreads some decimals by 1–3 ulp and truncates three-digit exponents (R1 pilot) | acceptance criteria of §12.3 (RX-D18) and `EXPONENT_NOT_PORTABLE` (§5.2, RX-D19); recorded per reader version |
| X-8 | a closed reference re-saved by AutoCAD carries `70 = 15` (bit 2) and the approved import rejects it as `PERIODIC_UNSUPPORTED` | outside this contract: separate maintenance debt DT-2 (R1 report, annex R2) |
| X-9 | the approved ordinary DXF export also writes `Double.toString` and has the same exponent issue with AutoCAD | outside this contract: separate maintenance debt DT-1 (R1 report, annex R2) |

## 14. Stop conditions

Stop if: a conformance reader rejects the rational `SPLINE`, or reads it outside §12.3 (any inexact definition reading for ezdxf; any per-value, structural or geometric failure for AutoCAD); writing would need
transforming or recomputing data beyond formatting; the ordinary export changes one byte;
an association would need proximity, data digests, labels or handles; the
correspondence would need a DAG dependency; the implementation would need the construction
`SPLINE` writer of `MAINT-A3`.

## 15. Open decisions

| Id | Question | Recommendation | Consequence |
|---|---|---|---|
| RX-D1 | Surface | check box in the existing DXF dialog of the extended paired-output flow (`chooseRequest`; the legacy G5 route is not extended) | no new menu action |
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
| RX-D12 | Sidecar | mandatory with references | paired output in situations A and B of §10; in situation C neither DXF nor sidecar (R3) |
| RX-D13 | Sequence | strict: this contract, the store model types of `R-c` (milestone RC-M) and the read-only provenance parser with applicability (milestone DXF1-M); integration after `MAINT-A-DXF0-RC` in the author's order; may precede A-DXF1 (before it only `NO_CEDG_REPRESENTATION`, case 7 and user choices occur) | aligned with the implementation plan §2 |
| RX-D16 | Conformance readers | AutoCAD Core Console and ezdxf (pinned), definition reading separated from evaluation (§12.1) | **fixed by the author (D-R1-7)** |
| RX-D17 | Failed approximations | never an export state; no persisted history | **fixed by the author (D-R1-5)** |
| RX-D18 | AutoCAD definition-reading rule | alternative A with the author's conditions: three levels of evidence; ezdxf exact; AutoCAD within 4 ulp per value as an initial test criterion for AutoCAD 2026 (`ACADVER` 25.1), plus structural and geometric criteria; recorded maxima and limits (§12.3) | **fixed by the author** (instruction R2 of 2026-11-25) |
| RX-D19 | Values with three-digit exponents | `NOT_WRITABLE`/`EXPONENT_NOT_PORTABLE` for coordinates, knots and weights, both exponent signs; no polyline, no silent rewrite; user exclusion keeps the diagnostic; plain-decimal writing only as a later improvement proved by tests (§5.2); not extended to references excluded for other causes | **fixed by the author** (instruction R2 of 2026-11-25; non-extension decided in instruction R4 of 2026-11-26) |
| RX-D15 | References with identical data (case 7) | both included by default as distinct references; the summary line and «Review…» flag them with `SAME_DATA_AS_REFERENCE`; the user may exclude either | deduplicating by data would be an association by coordinates (D-A-N2-3); the automatic-duplication rule concerns a reference and a construction that represents it |
| RX-D14 | Normative form | this contract + amendments + ADR 0045 | — |
