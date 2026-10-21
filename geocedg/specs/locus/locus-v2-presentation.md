# Specification: Locus V2 ordinary presentation and render continuity

- Status: **NORMATIVE / AUTHOR APPROVED** (version 1); **PRE-G9B-CU2 VERSION 2
  AMENDMENT ("PRE-G9B-CU2 amendment (version 2)" below) — ARCHITECTURE AUTHOR
  APPROVED (2026-10-21); THE INCORPORATED TEXT BELONGS TO THE CU2 TECHNICAL
  CANDIDATE, PENDING AUTHOR REVIEW**
- Version: 2
- Owners: GeoCeDG project owner
- Roadmap gate: G9U0-R2 planning/design `PASS — AUTHOR APPROVED`;
  implementation not authorized / not started (statement of the version 1
  design gate; the roadmap records the later G9U0 refinements)
- Affected layer: shared `GeoElement` presentation capability, Euclidian render,
  Desktop Properties/style integration, persistence and lifecycle tests
- Productive implementation: **not authorized / not started**

## Objective

Make a public Locus V2 behave like an ordinary curve for color, line thickness,
line type, show/hide, applicable label presentation, Properties exposure,
selection/highlight, copying, undo/redo and saved visual style while preserving
every approved G6–G9U0 semantic contract. An unrelated graphical object
crossing a Locus V2 must not create a gap, component or render subpath.

This contract is presentation-only. It neither changes the mathematical locus
nor promotes Locus V2 to the generic upstream `Path` contract.

## Authority and dependencies

The semantic authorities remain:

- `AGENTS.md`, sections 11 and 14;
- `geocedg/specs/locus/locus-v2-semantics.md`;
- `geocedg/specs/locus/locus-v2-public-surface.md`;
- Accepted ADR 0013; and
- the author-approved G9U0 and G9U0-R1 source, tests and evidence.

The author approved this design authority at the G9U0-R2 planning closeout.
Future implementation requires G9U0-R1 and G9X1 to remain author-approved PASS
and requires a separately authorized G9U0-R2 prompt invocation.

## Scope

G9U0-R2 covers only:

- ordinary object color;
- ordinary line thickness;
- ordinary supported line types;
- ordinary show/hide behavior;
- applicable ordinary label visibility and label presentation;
- standard Properties/style applicability and interaction;
- normal selection and highlight presentation;
- persistence of those visual properties through native save/reopen;
- visual-style preservation through supported copy and undo/redo paths;
- continuous curve rendering through crossings with unrelated lines, circles
  and conics; and
- explicit evidence that genuine semantic components and invalid intervals
  still create the required distinct render subpaths.

## Forbidden scope

G9U0-R2 must not:

- change `LocusDefinition2D`, driver domains, branch/component identity,
  parameterization, orientation, invalid intervals or continuation;
- change semantic or topology revision rules;
- change metric, incidence, intersection, result-token or persistent-ID
  semantics;
- change the normal semantic dependency-DAG authority;
- make `GeoLocusV2` implement generic `Path` or a legacy locus interface;
- store a second GeoCeDG-specific style model;
- serialize render vertices, clipping, z-order, selection highlighting or a
  tessellation cache as object authority;
- infer a discontinuity from overlap with another drawable; or
- implement G9U1 workspaces, G9B/G9C spatial semantics or G9U2 procedures.

## Style authority and invariants

### One ordinary style model

`GeoElement` visual properties are the only style authority. A future change
may declare that Locus V2 supports the existing line-style UI capability, but
it must not add parallel color, thickness, dash or style-persistence fields.
Ordinary visibility and applicable label presentation likewise use existing
`GeoElement` authority. Selection/highlight remains transient view presentation
and must not become persisted semantic or style authority.

The following equality is a hard contract:

```text
presentation mutation
  => same durable semantic identity
  => same semantic revision
  => same topology/component revision
  => same driver and domain
  => same branches and valid components
  => same metrics and intersections
  => same solution tokens and semantic DAG
```

Only an actual semantic input change may advance the semantic revision.
Changing color, thickness, line type, visibility, label presentation or
selection/highlight may invalidate or repaint presentation state; it may not
invalidate semantic caches or publish a semantic definition.

### Properties and style applicability

Locus V2 must expose the ordinary line-style controls through the narrow host
capability used by Properties and style bars. Applicability is not geometric
path membership. The implementation must keep the approved non-`Path` public
surface and must not widen unrelated types merely to make the controls appear.

Unsupported presentation controls remain absent. The gate does not authorize
fill, point, trace, animation or generic path-position controls for Locus V2.

### Persistence, copy and undo

Persistent visual properties use the normal `GeoElement` XML/style contract.
Reopen must restore the exact supported style, visibility and applicable label
presentation without changing the reconstructed semantic parent or its durable
IDs. Supported copy must receive the ordinary persistent visual style while
following the existing G9U0 identity-remap contract. Undo/redo of a persistent
presentation change restores presentation and must not create a new semantic
revision. Transient selection/highlight follows ordinary host behavior and is
not serialized merely by this contract.

## Render continuity contract

`LocusRenderData2D` remains a derived, disposable presentation product of the
semantic branches/components and one view-owned tessellation policy. Style,
other drawables, hit order and z-order are not inputs to its subpath topology.

A render vertex may start a new subpath only when the governing semantic/render
contract already requires one, including:

- the start of a semantic branch or valid domain component;
- an invalid semantic evaluation that cannot be bridged truthfully; or
- an explicitly defined presentation-clipping boundary for an unbounded/open
  component, without changing the semantic component itself.

The presence, location, style, draw order or selection state of an unrelated
line, circle, conic or other drawable must not change:

- semantic revision;
- render vertex provenance;
- render vertex count for a fixed locus revision and view policy;
- `startsSubpath` count or positions; or
- metric/intersection results.

An ordinary overdraw may visually cover pixels where two objects cross. A dash
pattern may intentionally contain visible spaces, and viewport clipping may
hide out-of-view segments. Those presentation effects must remain
diagnostically distinguishable from a semantic invalid interval or separate
component. The gate does not promise that a lower-z-order curve is visible
through an opaque stroke drawn above it; it promises that the locus itself is
not split.

## Current source evidence and minimum future seam

The design is grounded in the current author-approved source:

- `GeoLocusV2` already extends `GeoElement`, uses `setVisualStyle` for copy and
  delegates public XML to `GeoElement`;
- `DrawLocusV2` already calls `updateStrokes`, uses line thickness to reset its
  path and draws with the ordinary object color and stroke;
- the default `GeoElement.showLineProperties()` returns `isPath()`, while
  Locus V2 deliberately is not a `Path`; and
- `LocusRenderCache2D` derives subpaths from semantic branches, valid
  components and evaluation validity, without consulting other drawables.

This evidence recommends a narrow Locus V2 presentation-capability seam plus
focused regressions. It does not authorize that code. If implementation shows
that a semantic-model, generic-Path or new style-storage change is required,
G9U0-R2 must stop for author review rather than expand silently.

## Validation

The future focused gate must cover all rows `R2-L01` through `R2-L15` in
`docs/validation/g9_public_workspace_validation_matrix.md`, including:

- color, thickness, line type, show/hide, applicable labels, ordinary
  selection/highlight and Properties exposure;
- XML/native-document persistence, copy and applicable undo/redo;
- hard assertions that style changes preserve semantic identity/revision,
  domains, branches, metrics and intersections;
- fixed-policy render-data comparison before and after crossing lines,
  circles and conics; and
- preservation of genuine disconnected-component subpaths.

The focused verifier must run twice and compare canonical semantic/render
evidence. It must then run the required G9U0-R1, historical G9U0, G9X1, G5,
relevant G9A, legacy Locus and composed regressions.

## Stop conditions and open decisions

Stop for author review if:

- ordinary style support would require generic `Path` conformance;
- a visual mutation changes any semantic revision or result;
- crossing continuity cannot be proved without merging genuine components;
- the renderer would need another object as tessellation input;
- supported copy/undo/XML cannot reuse ordinary `GeoElement` style authority;
  or
- implementation is not separately authorized or cannot preserve this
  author-approved contract.

This specification is approved normative design authority. It makes no
productive implementation or observable-product claim.

## PRE-G9B-CU2 amendment (version 2): render topology, tessellation and declared fidelity

Normative text of amendment A2.4-R of the
[`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
approved as architecture by the author on 2026-10-21 with ADR 0038 and the decisions D-CU2-4,
D-CU2-10, D-CU2-19 and E-1
([approval record](../../../docs/validation/pre_g9b_cu2_author_architecture_approval_record.md)).
It refines the render continuity contract above for the existence structure of
`locus-v2-existence-components.md` §15 and belongs to the `CU2` technical candidate until the
author reviews that candidate. The style contract of version 1 is unchanged.

**Render topology.** Rendering is derived from the existence structure. Each certified
component is tessellated up to its certified boundaries. No render segment crosses an
unresolved band, a certified nonexistence interval or a structural discontinuity. A curve is
closed only when its component is certified closed periodic. A new subpath starts only at the
start of a component. Rendering never falls back to uniform sampling and never infers a join, a
closure or a discontinuity from samples. A point that cannot be evaluated is never
interpolated: the path is cut there with a computational-gap mark. Exploratory evidence, when
its display is enabled, is drawn with a distinct style only inside unresolved bands and is
never joined to a component nor exported as authoritative geometry. A point component is never
drawn as a segment or a curve; presentation version 2 declares it, with its parameter enclosure
and the status of its position, and does not draw it.

**Tessellation policy.** The render policy is versioned and deterministic per view. A span is
accepted when its chord deviation and its turning angle with the adjacent span are both within
versioned limits, after a versioned minimum depth. The turning limit is waived only in a
minimal parameter band around a certified fold and at the vertex of a certified singular
parameter, which is drawn as an exact vertex. Near a certified discriminant transition the
tessellation uses a local square-root reparametrization and ends at the boundary image; at a
structural discontinuity it ends at the one-sided limit, or at the last evaluated vertex, with
a mark, when that limit cannot be evaluated. A vertex budget per view coarsens the result
deterministically, never crossing a boundary.

The approved ranges of the versioned constants (D-CU2-10) are: minimum depth 2 to 6, maximal
turning angle 5 to 20 degrees and vertex budget per view 1 024 to 16 384; the technical
candidate fixes their values with reproducible benchmark evidence. The turning criterion
applies to drawing; exports keep their approximation tolerance in model units and the topology
of the certificate.

**Declared fidelity.** Each rendering or export approximation declares its effective fidelity
per component (complete, partial or failed) with its causes, and fails explicitly when it
cannot represent a result. It never invents a point, a join or an end.
