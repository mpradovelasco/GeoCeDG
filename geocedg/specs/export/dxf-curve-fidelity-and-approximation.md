# Extended DXF curve fidelity and approximation

| Field | Value |
|---|---|
| Status | **NORMATIVE / AUTHOR APPROVED** |
| Version | `1.0` |
| Phase | G9X1 design; implementation not authorized |
| Scope | Read-only 2D DXF fidelity, approximation, preflight, and sidecar contract |
| Hard dependencies | [G5 geometry export foundation](geometry-export-foundation.md) and approved internal G6-G8 semantic source contracts |
| Recommended predecessor | G9U0 for public/persistent Locus V2 integration; not a semantic dependency |
| Decision | [Accepted ADR 0014](../../../docs/adr/0014-export-only-dxf-approximation-and-sidecar.md) |

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

The current writer emits unitless Cartesian 2D coordinates (`$INSUNITS=0`).
It transports layer, RGB, and current visibility, but not line weight/style,
fill, opacity, point size, or labels. Hidden objects are included and marked.
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
`SPLINE` remains outside the baseline until mathematical exactness and
independent reader-conformance evidence are both available.

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
- hidden sources remain included but visibly reported.

The dialog must show unitless coordinates, tolerance/guarantee, work limits,
explicit domains, sidecar creation, and every warning that can affect fidelity.

## 8. Conditional mandatory sidecar and paired output

A deterministic UTF-8 `<drawing>.dxf.manifest.json` is mandatory whenever an
export operation contains `APPROXIMATE` geometry, omitted or partial geometry,
an unsupported requested component that was not emitted, work-limit
termination, or any other fidelity reduction. A wholly `EXACT` export may omit
the sidecar unless another product policy requires it. When present, it records:

- schema, application/build version, embedded repository commit, DXF AC1015,
  coordinate system, units, and DXF SHA-256;
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
DAG. Recommended internal staging is:

1. X1A: result taxonomy, preflight, conditional sidecar, paired writes, and executable G5
   corpus;
2. X1B: bounded Locus V2/function/parametric adaptive polylines; and
3. X1C: implicit contouring and/or exact rational `SPLINE`, only under a later
   evidence-backed authorization.

Entry requires this normative specification and Accepted ADR 0014 plus green G5 and
G6-G8 gates. X1 may consume an internal revision-addressable Locus V2 semantic
snapshot without waiting for its public command/persistence surface; the
any required/present sidecar must report the source's truthful `id_scope`.
Executing G9U0 before X1
remains the recommended product-integration schedule because it supplies a
normal public source and cross-save identity. G9U0 is not a hard semantic X1
dependency, and implementation remains designed but not authorized after G9P.
