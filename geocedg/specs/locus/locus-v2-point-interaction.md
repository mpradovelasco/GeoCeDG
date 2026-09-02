# Locus V2 semantic point interaction

- Status: **NORMATIVE — PASS — AUTHOR APPROVED**
- Version: 1
- Phase: **G9U0-R6**
- Architectural layer: shared Java kernel
- Decision record:
  [ADR 0019 — Accepted](../../../docs/adr/0019-semantic-locus-point-interaction-support.md)

## 1. Scope

This contract defines inverse semantic-address resolution and normal-DAG point
creation/edit support for current `GeoLocusV2` sources, including SplineV2 and
supported R5 similarity images. It complements, but does not replace, exact
forward evaluation and the existing exact command form
`Point(L,"branch",u)`.

R6 is kernel capability for a future ordinary Point tool. It does not add that
frontend tool, generic `Path`, candidate markers, a workspace action, or a new
public command.

## 2. Semantic model

Let branch/component (D_{b,c}) be an oriented valid subset of the explicit
semantic parameter domain, and let

\[
F_{b,c}:D_{b,c}\rightarrow\mathbb{R}^2
\]

be current semantic evaluation. For a transient geometric request (q), an
inverse interaction query seeks current addresses ((b,c,u)) whose evaluated
image satisfies the declared bounded interaction evidence.

The query result is not itself a constructed geometric object. A point is
created only after one candidate is explicitly selected. Its authority is:

```text
durable semantic source + branch/component + canonical parameter
```

The request coordinate and its residual cease to be authority after selection.

## 3. Typed query result

The result shall distinguish at least these semantic outcomes:

| Outcome | Meaning |
|---|---|
| no candidate | bounded supported search established no admissible preimage |
| unique candidate | exactly one candidate satisfies the declared interaction contract |
| multiple candidates | two or more distinct semantic preimages remain admissible |
| unresolved | bounded numerical work could not establish the required result |
| invalid source | the source or semantic snapshot is undefined/noncurrent |
| degenerate image | the image cannot determine a unique new address, including ordinary `COLLAPSED_IMAGE` queries |
| unsupported | the source/domain capability lies outside R6 |

Each candidate records, as applicable:

- source durable identity and semantic revision;
- branch and component;
- canonical semantic parameter/address;
- periodic/seam canonicalization;
- evaluated finite point;
- world-space request residual;
- local regularity and isolation/uniqueness evidence;
- numerical guarantee and work-limit status; and
- typed diagnostics.

No status is inferred from how Swing or Euclidian presentation displays the
source.

## 4. Interaction distance is not identity

World-space distance may bound a query, seed refinement and rank candidates for
future presentation. It is transient policy and is never serialized into the
point, source identity, branch identity or semantic parameter.

Pixel tolerance, zoom, DPI, viewport, stroke tessellation and screen position
belong to future frontend hit-testing and are not inputs to semantic identity.

## 5. General finite evaluator capability

For a supported finite evaluator-only source, resolution shall:

1. enumerate declared branches and valid components canonically;
2. partition only semantic domains, never render segments;
3. use bounded deterministic exclusion/localization;
4. refine candidates through the semantic evaluator and differential capability
   where available;
5. canonicalize provider parameters and boundaries;
6. forward-evaluate every candidate independently; and
7. publish explicit unresolved/work-limit state when required evidence is not
   established.

Unbounded sources require a separately justified bounded interaction region or
provider capability. R6 shall not hide an unbounded global nearest-point solve.

The implementation recognizes one narrow complete provider certificate for
structurally affine loci, `F(u)=a u+b`. That certificate is captured from the
reconstructible semantic expression itself, or propagated algebraically by an
R5 similarity; it is never fitted from samples. Searching every requested
finite component under that certificate establishes complete requested-scope
coverage and may therefore publish a definitive zero- or one-preimage result.
Certificate propagation through a similarity is optional evidence rather than
semantic-locus definedness. If transformed affine coefficients cannot be
represented finitely, capture fails atomically to unavailable. The transformed
locus remains defined; ordinary semantic evaluation reports `NON_FINITE` at an
affected address, and a later finite recompute may recapture the certificate.

The general evaluator-only fallback has deliberately weaker authority. It may
discover and forward-verify local candidates, but its bounded sampling and
refinement do not exclude an unsampled narrow minimum. Consequently zero or one
candidate under `BOUNDED_EVALUATOR_SEARCH` is **unresolved**, not definitive no
preimage or unique preimage. Several distinct forward-verified candidates may
still be reported as multiple. Local resolution may support an existing point
edit inside its current branch/component, but lack of global evidence must
never be mislabeled as unique.

## 6. SplineV2 capability

SplineV2 resolution consumes G9S1's current piecewise-polynomial model. On each
canonical span (C(u)), the squared distance to request (q) is

\[
d_q(u)=\|C(u)-q\|^2.
\]

Span endpoints and roots of (d'_q(u)) provide the exact polynomial candidate
structure available to the bounded floating implementation. Provider bounds
may reject spans. Accepted candidates are refined and verified in the original
semantic parameter.

Interior knots follow the existing canonical ownership rule. Adjacent spans do
not duplicate one semantic knot address. Distinct preimages at a
self-intersection remain multiple even when their evaluated coordinates match.

Arithmetic evidence must remain truthful. A floating residual is not an exact
or interval-certified inverse proof unless a separately approved capability
establishes it.

Polynomial capability access returns the x/y coefficient pair from one
coherent captured snapshot. Similarity composition propagates that pair once
per level, so coefficient retrieval is linear in composition depth rather than
duplicating the complete traversal for each coordinate. The captured
composition depth is available in constant time. Nested semantic evaluation
and coefficient access are bounded both by the query policy and by the shared
hard safety ceiling of 128 levels. Limit exhaustion is unresolved evidence,
not a stack failure or partial success. These structural polynomial
coefficients remain floating implementation evidence; neither paired retrieval
nor a structural certificate claims exact arithmetic.

## 7. Interactive semantic point state

An interactive semantic point is an ordinary `GeoPoint` produced by one normal
algorithm. Its inputs include the semantic source and reconstructible explicit
semantic-address state selected by the implementation after host seam
characterization.

The durable selected/last-accepted semantic address and its current revision
binding are distinct. `getSemanticAddress()` exposes the retained selector;
`getCurrentSemanticAddress()` and the metric-position binding expose only a
currently revalidated address/certificate. If current component, topology or
evaluation evidence fails, the point and current binding become undefined and
coordinates are cleared, while the retained selector prevents retargeting.
Exact later revalidation may reactivate the same point. A retained selector is
never by itself evidence of current admissibility.

An address edit:

- keeps the same point/durable identity;
- changes only explicit semantic state through a normal transaction;
- triggers ordinary DAG recomputation;
- does not redefine the source;
- does not create/delete a point per pointer event; and
- is undoable/redoable as semantic state.

The candidate implementation performs the complete address-input mutation,
DAG publication and postcondition inside the existing host `Construction`
snapshot/restore boundary. A runtime failure restores the pre-edit construction
or terminates as a catastrophic rollback failure; partial address state is not
published. Taking the host snapshot costs O(N) in construction size per edit
and does not itself add an undo step: the future gesture owner must still group
successful edits through ordinary undo history. Because rollback reconstructs
the construction, Java object instances may be replaced; a caller must abort
the failed gesture and reacquire objects rather than retaining stale instances.

The frontend never owns the authoritative mutable parameter.

For a versioned interaction-owned address, the encoded state owns the exact
canonical parameter, lift and seam evidence. The accompanying hidden numeric
must reconstruct exactly as `canonical + lift * period` (or `canonical` for a
nonperiodic source). Re-canonicalizing that lifted floating value must not
replace the encoded canonical bits: ordinary IEEE-754 addition/subtraction can
otherwise reject a valid seam crossing. This reconstruction rule is not a
tolerance, nearest-parameter rule or alternate identity authority.

## 8. Dragging and ambiguity

The existing point's current source, branch/component and address are valid
semantic edit constraints and performance seeds. The prior screen/Cartesian
location is not identity.

Dragging remains within the same branch/component whenever that unique
semantic continuation is established. A request with several incompatible
preimages, an invalid gap, branch ambiguity, unresolved singularity or
insufficient evidence fails closed or returns explicit candidates for future
UI disambiguation. It never jumps to the nearest arbitrary preimage.

## 9. Periodic domains

Periodic sources use the declared half-open fundamental interval and provider
canonicalization. Equivalent seam parameters yield one candidate. The point's
canonical address and provider seam policy, not pointer motion, govern crossing.

A unique seam continuation retains the same point and address authority. If the
current semantic structure cannot establish it, resolution is ambiguous or
unresolved. R6 does not weaken R4 periodic intersection-token quarantine.

Acceptance requires bidirectional crossing on a canonical closed periodic
source, including the canonical `u=0.98 -> 0.02` crossing with
`periodicLift=1`, the same point/source/branch/component on both sides, exactly
one seam candidate, continued motion away from the seam and path-independent
final semantic state. A separately unresolved or ambiguous seam request leaves
the existing point/state unchanged and never retargets by Cartesian proximity.

## 10. Multiple branches, gaps and singular regions

- Ranks/candidates remain branch/component local.
- Components separated by an invalid interval cannot be bridged by Cartesian
  proximity.
- Equal images from distinct branches/preimages remain distinct candidates.
- Cusps, zero derivative, repeated images, zero-length spans and locally
  noninjective regions require explicit regularity/uniqueness evidence.
- Unsupported or exhausted searches leave no stale candidate.

## 11. Similarity images

An invertible R5 similarity may map the world request through its mathematical
inverse to reuse source capabilities. Final evidence is forward-verified on the
transformed semantic evaluator. The selected point belongs to the transformed
source and retains that source's durable identity context.

Negative dilation preserves the semantic parameter/orientation contract; it
does not reverse parameter identity merely because ambient orientation changes.

For finite `k=0`, `COLLAPSED_IMAGE` retains all valid source addresses at one
image point. A new query therefore cannot choose one arbitrarily and returns a
degenerate/multiple result. An already constructed point retains its selected
address, evaluates at the collapsed image and recovers when the transform is
nonzero again.

## 12. Persistence, copy and redefine

Transient query results and caches are not XML. A final point persists only
normal reconstructible semantic inputs/state:

- durable point identity;
- semantic source binding;
- branch/component;
- canonical address; and
- current definedness derived by recomputation.

Copy produces a new point identity and remaps the semantic source through
existing exact copy rules. Rename has no semantic effect. Compatible redefine
uses G9A authority; a missing address after a valid source change makes the
point undefined rather than causing coordinate attachment.

Native `.cedg` reopen and undo/redo reconstruct semantic state. No click,
pointer history, render segment or viewport is persisted.

When saved while dormant, the versioned address input reconstructs the durable
selector on reopen even though the current address/binding and point remain
undefined. Restoring a uniquely matching current component reactivates that
same point; malformed or incompatible state remains fail-closed and is never
repaired from coordinates.

The branch-state codec includes the exact persisted component-lineage key. At a
shared semantic endpoint that key selects the one matching component. If no
persisted lineage exists and more than one component contains the parameter, or
if the recorded lineage has no unique match, evaluation fails closed instead
of choosing the first component in a list.

The address `GeoText`/`GeoNumeric` pair is presentation-hidden only when both
conditions hold: the point carries the stable
`LOCUS_INTERACTION_POINT` identity role and the inputs are structurally
dedicated, independent, exclusive parents of that point. Codec-looking ordinary
user inputs are not hidden or reclassified. Persistent identity attachment on
reopen restores the auxiliary/restricted-visibility presentation for a valid
owned pair without making presentation state semantic authority.

## 13. Determinism and work evidence

For one source revision and query, candidate status and canonical candidate set
must be invariant under candidate enumeration, render policy, zoom, DPI,
viewport and event history.

Instrumentation records at least:

- semantic evaluations;
- branches/components or polynomial spans inspected;
- subdivisions;
- refinement iterations;
- canonical candidate count;
- local versus global fallback; and
- revision-cache hit/miss if caching exists.

Every limit is explicit and deterministic. A work limit returns unresolved
state rather than a partial result mislabeled as complete or unique.

## 14. Compatibility boundary

- `GeoLocusV2` remains non-`Path`.
- Existing exact Point, metric, intersection, R4 token and R5 transform
  semantics remain unchanged.
- Classic path/point behavior is unchanged.
- No R6-specific public command, runtime flag, localization or toolbar action is
  required.
- Future G9U1 supplies stroke hit-testing, candidate presentation and ordinary
  Point-tool frontend behavior only under its separate authorization.

## 15. Validation and status

The [R6 matrix](../../../docs/validation/g9u0_r6_semantic_locus_point_interaction_validation_matrix.md)
is the scenario authority. Its validated inventory contains 72 rows, including
the affine/evaluator coverage distinction, atomic rollback, ownership/reopen,
shared-endpoint lineage, bounded polynomial composition and the periodic-seam
drag acceptance. Both focused executions pass 55/55 with identical canonical
summary SHA-256
`8f48ab5ef1d8129fcb9ccce2c203daf524e41e2153f0d0b5c90d2e1e662277a2`,
followed by all relevant historical gates and full composed verification.

The retained risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains **OPEN / TRACKED**. Ordinary
R6 point persistence does not itself satisfy the missing quarantine-state
round trip.

This specification is **NORMATIVE — PASS — AUTHOR APPROVED**. R6 contains no
productive Desktop Point-tool consumer, so its accepted validation surface is
the kernel test-host/API and `manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN`.
This status does not authorize or execute G9U1.
