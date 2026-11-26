# Extended DXF curve fidelity and approximation

| Field | Value |
|---|---|
| Status | **NORMATIVE / AUTHOR APPROVED** (version `1.0`); the version `1.1` amendment below is a `PRE-G9B-R6-plus-C` technical candidate pending author review |
| Version | `1.2` (2026-10-21: certified topology of Locus V2 components, section 10, `PRE-G9B-CU2`); `1.1` (2026-10-04: units, hidden layers, explicit export area and sidecar schema version 2; author decisions `DQ-C2`, `DQ-C3`, `DQ-C11`, `DQ-C13`) |
| `CU2` amendment status | Section 10: **ARCHITECTURE AUTHOR APPROVED (2026-10-21); PASS — AUTHOR APPROVED WITH THE CU2 CLOSEOUT (2026-10-24)** |
| Conditioned use amendment status | Section 11 (ADR 0042): **AUTHOR APPROVED AS CONTRACT (2026-11-22), IMPLEMENTATION AUTHORIZED IN INCREMENT I4, NOT YET APPROVED AS PRODUCT** |
| Reference export amendment status | Section 12 and the amendments A-EXP-1 to A-EXP-5 in §3, §4, §7, §8 and §9: **AUTHOR APPROVED (2026-11-26)** with `cedg-reference-export/v1` and ADR 0045 (`PRE-G9B-MAINT-A-N2`, [approval record](../../../docs/validation/pre_g9b_maint_a_n2_author_approval_record.md); exact wordings in the [amendment set](../../../docs/architecture/pre_g9b_maint_a_n2_candidate_amendments.md)); the text before them is that of `main` `a9ee803bf`; implementation (`MAINT-A-DXF0-EXP`) not authorized |
| Phase | G9X1 design; implementation not authorized |
| Scope | Read-only 2D DXF fidelity, approximation, preflight, and sidecar contract |
| Hard dependencies | [G5 geometry export foundation](geometry-export-foundation.md) and approved internal G6-G8 semantic source contracts |
| Recommended predecessor | G9U0 for public/persistent Locus V2 integration; not a semantic dependency |
| Decision | [Accepted ADR 0014](../../../docs/adr/0014-export-only-dxf-approximation-and-sidecar.md) |

PRE-G9B-S1-R1 later refines complete-construction population and clarifies that
Locus V2 component validity must come from shared semantic authority. The
author approved this direction; the dependent-construction component
certificate remains incomplete and cannot be replaced by export sampling.

This normative contract extends the G5 export boundary without changing
geometric truth. It does not authorize implementation. Existing exact G5 mappings remain the
compatibility baseline.

## 1. Authority and invariants

The current construction and its semantic evaluators are authoritative. An
export approximation is an ephemeral value in a read-only export snapshot. It
must never become a `GeoElement`, enter `Construction`, appear in the
construction protocol, be serialized in `.ggb`, or participate in the normal
dependency graph.

Neither `LocusRenderCache2D`, a legacy locus point list, screen tessellation,
viewport bounds, zoom, DPI, nor graphical proximity may control the result.
For a fixed source revision and request, DXF and any sidecar bytes must be
deterministic.

## 2. Current source-backed baseline

The G5 pipeline is:

```text
GeoCeDG menu/controller
    -> GeometryExportService
    -> GeoElementGeometryExportAdapter
    -> immutable GeometryExportModel
    -> DxfExporter
    -> ASCII DXF AC1015
```

The writer emits Cartesian 2D model coordinates, numerically unchanged, with
`$INSUNITS` from the effective construction unit (`mm` 4, `cm` 5, `m` 6;
`UNSPECIFIED_MODEL_UNIT` and `usm` 0), as defined in the
[geometry export foundation](geometry-export-foundation.md) and the
[unit-system specification](../units/unit-system.md) §15.2 (version 1.1; until
`PRE-G9B-R6-plus-C` every export was unitless). It transports layer, RGB,
current visibility and the OFF state of persistently hidden GeoCeDG layers, but
not line weight/style, fill, opacity, point size, or labels. Individually
hidden objects are included and marked (`60 = 1`).
The existing source identifier is construction-revision scoped and can depend
on label/ordinal; it is not a durable cross-save identity.

The present `EXACT`/`APPROXIMATE` enum is only structural: production creates
exact entities only, and the writer does not encode approximation evidence.
No approximate source may be enabled until the result model, preflight, writer,
and sidecar expose fidelity truthfully.

## 3. Fidelity and outcome axes

Fidelity is assigned per source component, not once per file:

| Fidelity | Meaning |
|---|---|
| `EXACT` | The native DXF entity represents the resolved model-coordinate object over its declared domain without intentional discretization. This is not a claim of symbolic arithmetic. |
| `APPROXIMATE` | Export-only discretization is intentional and accompanied by method, tolerance, guarantee, achieved estimate/bound, and work evidence. |
| `UNSUPPORTED` | The source is valid, but no approved mapping exists for its family/domain. |
| `INVALID` | The source or request is undefined, non-finite, stale, ill-domainated, over budget, or cannot meet the requested evidence. |

Reason codes are orthogonal and include at least `MISSING_DOMAIN`,
`NON_FINITE`, `DISCONTINUITY_UNRESOLVED`, `TOLERANCE_NOT_ESTABLISHED`,
`WORK_LIMIT`, `STALE_SOURCE_REVISION`, and `UNSUPPORTED_FAMILY`. One source may
produce several emitted and omitted component outcomes. Omission is never
silent.

Approximation evidence has a separate guarantee axis:

- `CERTIFIED_ERROR_BOUND`: justified by interval, curvature, enclosure, or an
  equivalent proof contract;
- `ESTIMATED_ERROR`: deterministic sample/derivative evidence without a global
  proof; and
- `FLOATING_POINT_UNCERTIFIED`: diagnostic only and not export-admissible by
  default.

The population of imported references (§12) has its own fidelity axis,
`REFERENCE_DEFINITION`: transfer of an external definition read back to the same binary64
data. It is never `EXACT`, `APPROXIMATE`, `UNSUPPORTED` or `INVALID` of a construction
source and never counts as CeDG geometry.

## 4. Capability and fidelity matrix

| Source family | Initial G9X1 representation | Fidelity/domain policy |
|---|---|---|
| point | `POINT` | retain G5 exact mapping |
| segment | `LINE` | retain G5 exact endpoints |
| ray | `RAY` | retain G5 exact unbounded mapping |
| line | `XLINE` | retain G5 exact unbounded mapping |
| circle | `CIRCLE` | retain G5 exact mapping |
| circular arc | `ARC` | retain G5 exact oriented interval |
| ellipse / elliptic arc | `ELLIPSE` | retain G5 exact parameter interval |
| polygon / upstream polyline | `LWPOLYLINE` | retain exact vertex/boundary mapping |
| parabola / hyperbola | bounded `LWPOLYLINE` | approximate only over an explicit finite semantic interval |
| bounded function / parametric curve | bounded `LWPOLYLINE` | approximate; explicit finite domain and declared gap partition required |
| regular polynomial implicit curve | none initially | unsupported until a separately approved topology-aware bounded contourer exists |
| Locus V2 | one `LWPOLYLINE` per branch and valid component | approximate unless the source supplies an approved typed native descriptor |
| periodic curve | native entity or component polyline | close only with a full-period/closure certificate |
| other unbounded curve | none without request domain | explicit closed semantic parameter subdomain required; viewport clipping forbidden |
| legacy `GeoLocus` | none | remains unsupported; its display samples are not authority |

Coincident constructive components remain distinct. Invalid-domain gaps are
never connected. Endpoint proximity alone does not establish periodic closure.
For construction geometry, `SPLINE` remains outside the baseline until
mathematical exactness and independent reader-conformance evidence are both available
(`MAINT-A3`). Imported references are written as `SPLINE` only under §12.

## 5. Required read-only model

The owning service should evolve to:

```text
GeoElement adapter
    -> immutable semantic export snapshot
    -> export-only adaptive approximation builder
    -> GeometryExportModel v2
    -> DxfExporter
    -> DxfEncodingResult (neutral ID -> DXF handle)
    -> deterministic sidecar writer
```

Minimum concepts:

- `GeometryExportRequest`: explicit tolerance, allowed guarantee levels,
  semantic domains, deterministic evaluation/depth/vertex budgets,
  approximation permission, and partial-output permission;
- `SourceExportOutcome` plus `ComponentAddress`;
- `ApproximationEvidence`: method, requested tolerance, achieved bound or
  estimate, guarantee, evaluations, segments/control points, and maximum depth;
- `AdaptiveCurveApproximationBuilder2D` outside the writer and kernel truth;
- `DxfEncodingResult` carrying actual handles without contaminating the neutral
  model; and
- preflight and paired-output result objects for the desktop controller.

The runtime build must embed repository provenance. Export must not invoke Git.

## 6. Conservative approximation baseline

The first approved baseline shall be deterministic oriented dyadic refinement
to `LWPOLYLINE` in model coordinates:

1. partition at every declared invalid interval, discontinuity, or semantic
   branch/component boundary;
2. evaluate endpoints, midpoint, and quarter points in deterministic order;
3. test chordal deviation and, when available, tangent/curvature evidence;
4. subdivide by exact interval bisection until evidence satisfies the request;
5. stop by evaluation, depth, component-vertex, and total-vertex counts, never
   by wall-clock time; and
6. emit no component whose requested evidence was not established before its
   work limit.

Sample-based chord tests establish `ESTIMATED_ERROR`, not a certified global
Hausdorff bound. Open endpoints and asymptotes require a closed export subdomain
unless a later contract defines a finite endpoint limit.

## 7. Preflight and user decision

Before choosing or overwriting output, preflight reports exact, approximate,
unsupported, invalid, and omitted component counts. The approved defaults are:

- an approved typed curve family may use its approved approximation strategy as
  normal export behavior, but preflight and the completion report must identify
  the drawing as approximate and disclose tolerance/guarantee evidence;
- partial component output is disabled and strict reject/stop is the default;
- any future partial-output option requires explicit user intent for that
  operation, a visible warning, and a mandatory sidecar; and
- hidden sources remain included but visibly reported; sources on a
  persistently hidden GeoCeDG layer remain included on their layer, which is
  written OFF and reported (version 1.1, `DQ-C2`, `DQ-C3`).

When the separate reference option of §12 is chosen, preflight also reports references
included, excluded by state, excluded by the user, outside the export area and not
writable. The operation is writable when the construction contributes at least one entity
or at least one reference is included and writable after resolving preferences, states, user
choices and the export area (§12).

An explicit export area (`MANUAL`, `EXPORT_POINTS_EXPLICIT`,
`EXPORT_POINTS_AUTOMATIC`) selects whole sources and certified components by
the `B1` participation rule of the geometry export foundation (version 1.1,
`DQ-C13`); the `VISIBLE_VIEWPORT` fallback is no boundary. Proposed amendment
of the `PRE-G9B-R6-plus-E3` technical candidate (not author approved;
`geocedg/specs/sheets/iso-a-border.md` §11): the explicit producers
`ISO_A_SELECTION` and `ISO_A_BORDER` (the linked sheet's paper boundary) are
explicit export areas under the same rule, reported as `iso_a_selection` and
`iso_a_border`. A non-participating
source or component is an `OUTSIDE_EXPORT_AREA` population outcome: it is
neither unsupported nor invalid, is never a fidelity reduction, never blocks
the strict request and never makes the sidecar mandatory by itself. The strict
writability rule above is unchanged for every participating component.

For a complete-construction request, the input to strict preflight is the
versioned typed population `geocedg-dxf-geometric-2d/v1`, not every labeled
construction participant. Lists, numeric parameters, rich Locus intersection
results and Text without an approved DXF mapping are outside that population
and are reported as such before preflight. This is not partial output. Every
eligible geometric source remains subject to strict exact/approximate,
unsupported and invalid classification. Current selection is explicit and is
never filtered by this population rule.

The dialog must show the coordinate system with its unit and `$INSUNITS` code
(`unitless` for `UNSPECIFIED_MODEL_UNIT`; a visible custom-unit warning for
`usm`), tolerance/guarantee, work limits, explicit domains, the hidden layers
written OFF, the consumed export area and the sources outside it, sidecar
creation, and every warning that can affect fidelity.

## 8. Conditional mandatory sidecar and paired output

A deterministic UTF-8 `<drawing>.dxf.manifest.json` is mandatory whenever an
export operation contains `APPROXIMATE` geometry, omitted or partial geometry,
an unsupported requested component that was not emitted, work-limit
termination, or any other fidelity reduction, and whenever the effective
construction unit is `usm` (version 1.1, `DQ-C11`: the core DXF stays unitless
and only the sidecar carries the declared physical meaning), and whenever the reference
option of §12 is on and the export completes with at least one reference written or excluded
(§12.6, situations A and B). A wholly `EXACT` export may omit
the sidecar unless another product policy requires it, such as the `usm` rule
above; export-area filtering alone never requires it. The new sidecar requirement comes only
from the reference option: the filtering of construction sources by the export area alone
still never makes the sidecar mandatory. When present, it
records:

- schema `org.geocedg.dxf.fidelity-manifest` with `schema_version` 2 (version
  1.1; a version-1 sidecar keeps its original meaning, unitless coordinates,
  and is never reinterpreted), application/build version, embedded repository
  commit, DXF AC1015, coordinate system, source and target unit, `$INSUNITS`
  and DXF SHA-256. A sidecar written under §12 uses `schema_version` 4, with a
  section `external_references`; a sidecar written without the reference option keeps the
  current version and bytes; versions 2 and 3 keep their meaning. The encoding validator
  counts construction entities and reference entities separately;
- versioned unit metadata (`org.geocedg.dxf.unit-metadata` version 1): the
  unit state (`physical` or `unspecified_model_unit`), the construction-unit
  token, the canonical binary64 `meters_per_unit` (none when unspecified), and
  for `usm` the token `usm`, its canonical `meters_per_unit`, name and symbol;
- the persistent hidden GeoCeDG layers and the DXF layers written OFF;
- the export-area record: the participation rule, the resolved producer,
  whether a boundary applied, its bounds and source view, and every source or
  component outside the area with its identifier, scope and address;
- the complete request policy and deterministic work limits;
- source identifier plus `id_scope` (`persistent` or
  `construction-revision`), source family/label/revision;
- branch key, revision-local component, semantic interval, neutral entity ID,
  DXF handle and entity type;
- fidelity, approximation method, requested tolerance, achieved bound/estimate,
  guarantee, evaluations, vertices/control points, and depth; and
- structured warnings, every omission, and work-limit outcome.

When a sidecar is required or requested, DXF and manifest are prepared in
same-directory temporary files, validated, then promoted under a defined
rollback policy. Collision checks cover both destinations. The sidecar DXF hash
detects a mismatched pair; no universal two-file atomicity is claimed. Exact-only
DXF still uses safe temporary-file promotion without inventing a temporary
construction object.

## 9. Compatibility, staging, and gate

G9X1 is experimental and separately gated. It does not change G5 exact bytes,
legacy `GeoLocus`, `.ggb` persistence, command semantics, or the construction
DAG. With the reference option of §12 off, or without references, the bytes of every export
are unchanged. Recommended internal staging is:

1. X1A: result taxonomy, preflight, conditional sidecar, paired writes, and executable G5
   corpus;
2. X1B: bounded Locus V2/function/parametric adaptive polylines; and
3. X1C: implicit contouring and/or exact rational `SPLINE` of construction geometry, only
   under a later evidence-backed authorization; the `SPLINE` of imported references is
   governed by §12.

Entry requires this normative specification and Accepted ADR 0014 plus green G5 and
G6-G8 gates. X1 may consume an internal revision-addressable Locus V2 semantic
snapshot without waiting for its public command/persistence surface; the
any required/present sidecar must report the source's truthful `id_scope`.
Executing G9U0 before X1
remains the recommended product-integration schedule because it supplies a
normal public source and cross-save identity. G9U0 is not a hard semantic X1
dependency, and implementation remains designed but not authorized after G9P.

## 10. PRE-G9B-CU2 amendment: certified topology of Locus V2 components

Normative text of amendment A2.6 of the
[`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
approved as architecture by the author on 2026-10-21 with ADR 0038 and decision D-CU2-4
([approval record](../../../docs/validation/pre_g9b_cu2_author_architecture_approval_record.md)).
It refines §3 to §8 for the existence structure of
[`locus-v2-existence-components.md`](../locus/locus-v2-existence-components.md) §15 and was approved with `PRE-G9B-CU2` (`PASS — AUTHOR APPROVED`, 2026-10-24; [closeout record](../../../docs/validation/pre_g9b_cu2_closeout_record.md)). DXF keeps the accepted
strict G9X1 fidelity contract; the approximation tolerance stays in model units.

**Export topology.** An export approximation follows the render topology: one open or closed
path per certified component, in arc order across the seam, closed only for a certified closed
periodic component, and ending at certified boundary images or one-sided limits. A component
with an open end is exported, not dropped. An end that cannot be evaluated is exported up to the
last evaluated vertex and declared in the sidecar, and the export is then incomplete. Certified
singular parameters are exported as exact vertices. Without `COMPLETE`, export uses incomplete
fidelity with its mandatory sidecar, and every exported component remains strict. A point
component is never exported as a path: the sidecar declares it, with its parameter enclosure and
the status of its position, and the export is then incomplete.

For DXF this means: one `LWPOLYLINE` per certified component; the closed flag (group code 70)
only for a certified closed periodic component; the closure residual measured against the
closure certificate; exploratory evidence never exported; unresolved bands never interpolated.

**Implementation precision (`CU2` step T10, 2026-10-21).** The sidecar states these facts in
`schema_version` 3 of `org.geocedg.dxf.fidelity-manifest`: each outcome lists its representation
declarations (`END_NOT_EVALUATED`, `OPEN_END_APPROACHED`, `SINGULAR_VERTEX_NOT_EXACT`), each of
which is also a `representation_declaration` warning; a point component is an outcome of
fidelity `declared` with reason `degenerate_source`, which never blocks the strict request; the
preflight counts the declared components; and the approximation evidence of a closed path
carries its closure residual. A version-2 sidecar keeps its meaning.

**Implementation precision (`CU2` step T11, author decision D-CU2-T11-R1, 2026-10-21).** A point
of a certified component whose evaluation fails is a computational fact, not a gap of
existence, and never discards the component by itself: the failure is located by bisection up
to the resolution of the parameter (component length × 2⁻⁵²), the parts on both sides are
approximated separately, one `LWPOLYLINE` each, never joined or interpolated across the failure
and never closed, and every part carries the declaration `COMPUTATIONAL_GAP` (a computational
failure) or `EVALUATION_GAP` (any other undefined evaluation) with the parameters of the gap and
the evaluation status and diagnostic, which makes the output incomplete with the mandatory
sidecar. At most sixteen gaps are cut in one component; beyond them, or when the approximation
fails for any other reason, the component keeps the failure of the shared builder. A failure
that no evaluation of the export reaches is not detected.

## 11. Conditioned use amendment (ADR 0042): conditioned regions are omitted and declared

Amendment approved by the author on 2026-11-22 with
[ADR 0042](../../../docs/adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md)
and the [conditioned-use contract](../locus/candidates/post-r1-conditioned-use.md), §10.
Increment I4. Sections 1 to 10 are unchanged: certified components are exported as before.

A semantic export of GeoCeDG 1.2.2 omits the conditioned regions of a Locus V2
([existence components](../locus/locus-v2-existence-components.md) §19). The omission is
declared, as §10 already declares a source without components: one outcome of fidelity
`declared` and reason `EXISTENCE_NOT_ESTABLISHED`, whose message lists each omitted region
as `CONDITIONED_REGION` with its parameter interval and each band between them with its kind
and its interval, beside the outcomes of the certified components of the same source. The
export is then incomplete and is not blocked. Exploratory evidence stays never exported, and
a picture export of a view keeps exporting what the view draws.

The semantic export of conditioned regions is future work of the line on derived
representations, DXF import and extended DXF export (contract §10, point 3). In that line a
spline, a polyline or another derived representation never silently replaces the
authoritative CeDG object.

## 12. `cedg-reference-export/v1` amendment: export of imported references

Status: **AUTHOR APPROVED (2026-11-26)** with [`cedg-reference-export/v1`](dxf-reference-export.md)
and ADR 0045, accepted (`PRE-G9B-MAINT-A-N2`,
[approval record](../../../docs/validation/pre_g9b_maint_a_n2_author_approval_record.md)).
Approval is not implementation: `MAINT-A-DXF0-EXP` is not authorized.

12.1 The export of imported references is a capability separate from the semantic export
of `Construction`, run only by explicit user choice in the operation. Without it, §1 to §11
govern unchanged and the output is byte-identical.

12.2 Its population is the records of `cedg-reference-store/v1`, never `GeoElement`s.
Exporting mutates neither the construction, its XML, its Undo history, the store nor the
saved state.

12.3 Each included reference is one planar, non-periodic `SPLINE` without fit points, with
the degree, knots and transformed control points of its record and, when some effective
weight differs from 1, the rational flag and the effective weights. No rational reference
is approximated or replaced by a polyline.

12.4 Fidelity `REFERENCE_DEFINITION`: the entity's data, read back to the nearest binary64,
equal the record's data under `==`; checked before promotion. It claims nothing about the
source file or about other readers and confers no CeDG authority.

12.5 Default inclusion is decided by the explicit provenance of derived constructions and
the store, never by proximity, coordinates, data digests, labels or indices; a reference
validly represented by a current derived construction is not duplicated automatically;
the user's choice prevails, with a warning on duplication.
A reference whose related construction was modified is no longer validly represented and
is included by default, while the modified construction is exported by §1 to §11. A failed
approximation is never an export state.

12.6 With the reference option on: (A) when at least one reference is written, DXF and
sidecar are produced as a pair; (B) when the export completes and a reference is excluded, by
the user, by preference or by the approved policy (including by the user after
`EXPONENT_NOT_PORTABLE`, which never excludes a reference automatically), the
sidecar is produced and records each exclusion and its cause, also when no reference is
written; (C) when an included reference is `NOT_WRITABLE`, the strict rule rejects the
operation: no DXF and no sidecar, nothing partial presented as a success, the cause given by
the result and report of the operation.

12.7 Derived constructions are exported by §1 to §11 with their guarantees; this section
authorizes no `SPLINE` for constructions (`MAINT-A3`).

12.8 External conformance is checked with AutoCAD Core Console and a pinned ezdxf, reading
of the definition separated from mathematical evaluation; the evidence covers its fixtures
and reader versions only. DXF format compliance, the fidelity class (exact re-reading,
reproduced by ezdxf) and AutoCAD conformance (each value within 4 ulp as an initial test
criterion for AutoCAD 2026, `ACADVER` 25.1, only, not a bound of the product or of the
reader, plus preserved structure and geometry) are distinct levels of evidence; the AutoCAD
tolerance does not change the fidelity class.

12.9 A reference whose coordinates, knots or weights would be written with a three-digit
exponent is `NOT_WRITABLE` (`EXPONENT_NOT_PORTABLE`); it is never rewritten or replaced by a
polyline, and the user may exclude it with the diagnostic kept.
