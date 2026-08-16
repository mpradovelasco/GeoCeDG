# ADR 0013: Public Locus V2 surface and token-selected points

- Status: **Accepted**
- Accepted: 2026-08-16
- Phase: G9P design; G9U0 implementation not authorized
- Normative contract: `geocedg/specs/locus/locus-v2-public-surface.md`

## Context

G6-G8 established internal Locus V2 semantics, revision-scoped rich metrics,
rich intersections and exact-token point consumption. They deliberately
excluded public commands, public tools, generic Path behavior and persistence.

The current public `Locus` processor always constructs legacy Locus, while
legacy `Length[locus]` returns sampled point count. Existing Intersect overloads
use numeric index or an initial point for several baseline families. Redirecting
those contracts would silently change old files and would not provide durable
V2 identity.

The internal V2 factory still accepts injected evaluator functions, V2 copy/set
is unsupported, V2/rich geos emit no XML, and single-target root tokens are
currently transient `opaque-root-N` values. A public button before those gaps
close would create persistent-looking but nonpersistent objects.

The initial G9P proposal limited public creation to a bounded slider or a point
on one finite segment and deferred ordinary `Length[GeoLocusV2]`. R1 records
the author's broader requirement: scalar state must be independent of slider
presentation; point generators must cover segment, circle, circular arc and a
supported Locus V2 branch/component; `LocusV2 -> point -> LocusV2` must be
an initial public design case; and a guarded standard Length adapter may coexist
with the authoritative rich query. This is an explicit refinement of the
proposal, not a claim that the first draft already contained it.

## Decision

1. Preserve legacy `Locus`, mode `47`, old XML and legacy Length behavior
   unchanged.
2. Introduce an explicit experimental creation command, recommended spelling
   `LocusV2`. Normalize its accepted point/scalar overloads to a typed
   one-dimensional generator \(\mathcal{G}:D\to S\). `SemanticLocus` remains
   the naming alternative; overloading legacy `Locus` is rejected for
   experimental maturity. G9P does not freeze mapped-scalar argument spelling:
   G9U0 must inspect actual GeoGebra command/overload conventions, evaluate the
   alternatives, preserve the semantic generator contract, and present the
   selected command surface for author review.
3. Require a reconstructible dependent-construction evaluator, explicit
   domain/orientation/periodicity, normal DAG dependencies and durable
   locus/generator identity before public creation is enabled. A scalar form
   declares its state, true one-dimensional driver, domain and deterministic
   mapping; slider visibility is not identity and a dependent state is not
   mutated directly.
4. Start with closed typed point providers for a constrained point on a finite
   segment, circle, circular arc or supported Locus V2 branch/component. Do not
   infer an unrestricted generic `Path` provider, and reject a bare support
   that does not identify the constrained state point.
5. Represent points on V2 through two explicit layers: a durable preimage
   address (source identity, provider, branch lineage, canonical parameter and
   periodic lift/seam evidence) plus a source-revision/component/continuation
   binding. Distinct preimages at one Cartesian coordinate remain distinct.
6. Support acyclic `LocusV2 -> bound point -> dependent construction ->
   LocusV2` nesting through ordinary `AlgoElement`/Construction dependencies.
   Reject direct/indirect cycles on create, redefine and load through the normal
   DAG; session reentry detection is defense in depth, not a second graph.
7. Re-establish a bound point only through explicit provider/branch/component
   continuation. Branch loss or ambiguous continuation makes it and dependent
   loci undefined/noncurrent; nearest-Cartesian repair is prohibited.
8. Introduce a dedicated experimental rich metric command, recommended spelling
   `LocusLength`, for total and between-position queries. A standard
   `Length[GeoLocusV2]` must be exposed as a guarded child/reuse of that
   authoritative rich query and publishes solely when scalar admissibility is
   established. Otherwise the scalar is undefined or uses the
   repository-consistent typed failure behavior.
9. Extend the general `Intersect` command and tool for V2 operands. Its output is
   always a rich intersection result; existing non-V2 dispatch is unchanged.
10. Materialize an ordinary dynamic point only from an exact admissible result
   token. Recommended syntax is `Intersect[R,"token"]`; a dedicated
   `IntersectionPoint` spelling remains the alternative.
11. Permit graphical proximity only to rank already established candidates.
   Persist the selected token, never proximity or list order. Ambiguous/stale
   continuation makes the point undefined rather than retargeting it.
12. Use a Properties/result inspector as the primary rich diagnostic surface.
   Do not add a string/list diagnostics command in G9U0.
13. Keep `cedg.locus.v2` experimental and default-off. A runtime feature service
    must enforce command, tool and menu creation consistently while allowing an
    independently approved existing-file preservation path.
14. In the GeoCeDG Classic diagnostic launcher/path, load/preserve/recompute,
    save/reopen, and keep V2/rich types plus IDs/tokens/bindings native while
    creation UI is disabled. Do not migrate old loci or downgrade new types.
    External upstream distributions that do not know the persisted types are
    outside the compatibility guarantee; characterize that unsupported-open
    behavior without silent lossy conversion.
15. Persist reconstructible generator/support inputs, scalar mapping/domain,
    durable preimage/continuation and token lineage; recompute revision-bound
    point/numeric snapshots on reopen. Copy rewrites owned identity, undo/redo
    restores the same operation identity, and unknown providers fail typed.

## Consequences

- Old constructions retain their meaning and experimental V2 syntax is
  unmistakable.
- Rich coverage, error, overlap, isolation, continuation and work-limit states
  remain visible instead of being flattened into a number or list.
- Algebraic scalar maps and point supports share one extensible typed semantic
  generator contract; the GUI representation of a numeric object is irrelevant.
- A point on V2 retains preimage identity through self-intersections and periodic
  seams, enables acyclic nested loci, and becomes undefined when topology
  continuation is not established.
- A derived point is deterministic while its topology/lineage remains valid and
  becomes undefined when it cannot be re-established.
- Public implementation depends on a durable general identity/lifecycle slice,
  evaluator reconstruction, XML factories, copy/set/delete/undo semantics,
  localization/help/icons and runtime flags.
- A workspace can expose the approved actions but cannot activate them or own
  their semantics.
- Generic Path, arbitrary generator inference, automatic legacy migration,
  unbounded V2×V2 and broader target families remain separately gated.

## Alternatives considered

### Redirect `Locus[Q,P]` under a feature flag

Rejected. The same command text would construct different semantic object types
according to profile/runtime state, weakening compatibility and reproducibility.

### Return a numeric value directly from the first public length command

Rejected as sole authority. G7 rich status includes completeness,
rectifiability, traversal, guarantee and work evidence. A standard scalar
surface is allowed only as a guarded child/reuse of the reconstructible rich
query, never as an independent calculation, and legacy sample-count dispatch is
unchanged.

### Return a list of intersection points

Rejected for V2. List order cannot express branch/preimage identity, overlap,
incomplete coverage, locally unisolated roots or continuation events.

### Select by closest point on reopen

Rejected. Proximity is presentation state and can silently retarget across a
tangency, merge/split or branch change.

### Persist computed result snapshots as authority

Rejected. Results are revision-bound. Reconstructible inputs and policy are
persisted; current numeric evidence must be recomputed and verified.

### Make V2 a generic upstream Path immediately

Rejected for G9U0 because multibranch preimages, discontinuities, unbounded
domains and semantic positions require a narrower explicit contract first.

### Enumerate slider and segment as unrelated driver cases

Rejected by R1. It cannot represent a dependent scalar state with a distinct
true coordinate, a periodic point support or Locus-on-Locus nesting without
repeating identity, persistence and cycle rules.

### Infer or mutate the free ancestor of a dependent scalar

Rejected. A dependent scalar may have several free ancestors; slider visibility
is presentation state. The command must receive an explicit true coordinate,
domain and mapping and vary only an isolated reconstructible copy.

### Maintain a generator dependency graph beside Construction

Rejected. It would hide invalidation and cycles from the kernel. All generator,
support, bound-point and outer-locus edges belong to the normal DAG.

## Acceptance record and implementation gate

G9P closeout accepted the semantic generator contract, mandatory guarded total
`Length[GeoLocusV2]` adapter, GeoCeDG Classic diagnostic policy, and identity
sequencing. Exact scalar-mapping, semantic-position, point-on-support, and
token-point spellings remain G9U0 API choices that must be grounded in actual
GeoGebra conventions and presented for author review; the semantic contract is
not reopened by that choice. Acceptance selects an architecture, not code.
Productive G9U0
requires a normative spec and persistence ADR/contract, a canonical prompt,
green G6-G8 authorities, the full 22-case generator/nesting/cycle/lifecycle
suite, focused compatibility/scientific tests, and the composed verifier.
