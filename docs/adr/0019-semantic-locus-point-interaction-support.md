# ADR 0019: Semantic inverse-address resolution and interactive point state

- Status: **Accepted — PASS — AUTHOR APPROVED**
- Date: 2026-09-01
- Phase: G9U0-R6 — Semantic Locus Point Interaction Support
- Decision owner: GeoCeDG author
- Normative specification:
  [Locus V2 point interaction](../../geocedg/specs/locus/locus-v2-point-interaction.md)

## Context

The approved public Locus V2 stack evaluates an exact semantic address and can
construct `Point(L,"branch",u)`. G9S1 extends that forward authority to
SplineV2 and R5 preserves it through similarity images. The future ordinary
Point tool also needs the inverse interaction operation: given a selected
semantic source and an approximate geometric request, find evidenced semantic
preimages and let an explicit selection become a normal constructed point.

Legacy `Path` machinery is not suitable authority. It is designed around a
mutable path parameter and nearest-point interaction contract that does not
carry Locus V2 branch/component, provider canonicalization, periodic seam,
SplineV2 span/knot or transformed-source semantics. A render polyline is also
only presentation and cannot establish a preimage.

## Decision

1. Add a shared-kernel typed inverse semantic-address capability. It consumes
   one current semantic source and one transient geometric request and returns
   zero, one or several candidates plus status and numerical/topological
   evidence.
2. Preserve two distinct concepts:
   interaction residual/proximity may bound and rank a request; source identity,
   branch/component and canonical semantic parameter own the constructed point.
3. Keep `GeoLocusV2` outside generic `Path`. No pixel, render segment, Cartesian
   coordinate, solver enumeration or event history becomes durable identity.
4. Use bounded provider-aware resolution for general finite evaluator loci and
   the stronger piecewise-polynomial distance-stationary structure for
   SplineV2. Every accepted candidate is re-evaluated from semantic authority.
   A provider-owned structural affine certificate supplies complete finite-
   component coverage without fitting samples. The evaluator-only fallback
   cannot establish global exclusion: zero or one locally discovered candidate
   remains unresolved, never definitive none/unique. Affine certification is
   optional evidence: if transformed coefficients are nonfinite, certificate
   capture is unavailable without invalidating the semantic transform, whose
   ordinary evaluation reports the typed nonfinite state.
5. Represent an interactively created point through normal DAG dependencies
   containing an explicit editable semantic address. Dragging edits that state;
   it does not replace the point, redefine the locus or place mutable authority
   in a frontend controller.
6. Treat multiple preimages as explicit ambiguity. An existing point may use
   its own branch/component/address as an edit constraint, never its previous
   Cartesian position as identity.
7. Canonicalize periodic seam equivalents and SplineV2 knot ownership through
   provider authority. Invalid gaps and branches remain distinct.
8. For invertible R5 similarities, inverse mapping may accelerate semantic
   search. The resolved address belongs to the transformed locus. At
   `COLLAPSED_IMAGE`, a new query is generally nonunique while an existing point
   retains its already selected semantic address through collapse/recovery.
9. Serialize only the final point's normal semantic parents/state. Transient
   candidates, click coordinates, caches and interaction history are not XML.
10. G9U1 remains the frontend consumer. R6 adds no final Point tool, marker,
    workspace or public inverse-resolution command.
11. Mutate an interaction-owned address through the existing host Construction
    snapshot/restore boundary. The address inputs, DAG update and postcondition
    are one atomic mutation; failure restores the complete prior construction
    or reports catastrophic rollback failure. This reuses host authority rather
    than inventing an R6 graph snapshot.
12. Hide address auxiliaries only when stable identity role and structural
    exclusive ownership agree. Restore that presentation after persistent
    identity attachment on reopen; codec-shaped ordinary user inputs remain
    ordinary visible inputs.
13. Keep the durable last-accepted semantic selector separate from its current
    revision binding/certificate. Temporary component, topology or evaluation
    failure makes the point undefined and clears current coordinates/binding,
    but does not retarget or erase the retained selector. Only exact current
    revalidation may reactivate the same point.
14. For versioned interaction-owned state, preserve the encoded canonical
    parameter/lift/seam tuple as exact authority and validate its hidden numeric
    as the exact lifted reconstruction. Do not re-identify the encoded address
    from a modular floating round trip whose bits may differ at a valid seam.

## Evidence model

The typed result keeps status separate from candidate evidence. At minimum it
distinguishes none, unique, multiple, unresolved, invalid source, degenerate
image and unsupported capability. A candidate may record residual and local
regularity, but neither a low residual nor visual plausibility proves unique
semantic identity.

For creation, a candidate must be uniquely established under the declared
bounded interaction contract. For dragging, the current semantic address may
constrain a local edit inside the same branch/component. Any unresolved
crossing, branch change or self-intersection ambiguity fails closed.

## Determinism and performance

Candidate sets are canonical for one source revision and request. Enumeration,
render tessellation, zoom, DPI, viewport and UI event history have zero effect.
The resolver records bounded work counters and may use only revision-scoped,
non-authoritative performance caches. It does not repeat a full global solve
for each materialized point.

Piecewise-polynomial providers return a coherent x/y coefficient pair and an
O(1) captured composition depth. Similarity propagation is linear in nesting;
query policy and a shared 128-level safety ceiling bound coefficient/evaluator
composition. Exhaustion is typed unresolved evidence. Structural coefficients,
stationary roots and residuals are floating numerical evidence, not a claim of
exact arithmetic.

The host snapshot used for one interactive edit is O(N) in construction size.
It creates no undo record by itself; a successful future frontend gesture must
still use ordinary undo grouping. Rollback may reconstruct Java instances, so
the failed caller must abort and reacquire them. These costs and caveats are
accepted for the bounded R6 contract in preference to partial publication.

## Compatibility and persistence

- Existing exact `Point(L,branch,u)` remains valid and authoritative.
- Classic `Path` objects and ordinary Classic point-on-path behavior remain
  unchanged.
- Existing Locus V2, R4 intersection-token, R5 transform and G9S1 spline XML
  semantics remain unchanged.
- Copy/remap, rename, compatible redefine, undo/redo and `.cedg` reopen preserve
  semantic parents and address or fail closed; no coordinate repair occurs.
- A dormant native reopen hydrates the durable selector from its versioned DAG
  input while leaving the current certificate and point undefined until exact
  revalidation restores the same point.
- Persisted component lineage disambiguates a shared endpoint. Missing or
  multiply matching lineage fails closed rather than selecting by component
  list order.
- The dedicated address inputs are owned only by the stable
  `LOCUS_INTERACTION_POINT` role plus structural exclusivity. Reopen restores
  their auxiliary/restricted visibility after identity attachment; ordinary
  inputs that merely resemble the codec are not claimed.
- The separate risk
  `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains open unless its exact
  missing quarantine-state round trip is supplied.

## Rejected alternatives

### Implement `Path`

Rejected. It imports a parallel mutable parameter contract without the required
semantic evidence and risks changing legacy classification/dispatch.

### Snap to the render polyline

Rejected. Render resolution and viewport state are presentation, not geometry
or durable preimage authority.

### Persist click coordinates or nearest-point history

Rejected. The result would depend on interaction history and could jump between
semantic preimages at a self-intersection.

### Add a public resolver command

Rejected for R6. The capability is a shared-kernel service for a separately
authorized frontend tool; the exact semantic Point command already supplies the
scriptable deterministic surface.

## Consequences

- Future G9U1 can implement ordinary stroke hit-testing separately and pass the
  selected source/world request into one kernel authority.
- R6 acceptance uses the kernel test-host/API because this ADR deliberately adds
  no productive Point-tool consumer. End-to-end Point-tool creation, dragging,
  seam crossing and ambiguity choice remain a future G9U1 manual smoke.
- A self-intersection may require a chooser because visually coincident points
  can be different semantic addresses.
- Some evaluator-only, unbounded or singular cases truthfully remain unresolved
  rather than receiving an approximate durable point.
- Interactive address state becomes explicit normal construction state, so it
  participates in undo, copy and persistence without frontend ownership.

## Approval and successor boundary

The author accepted this decision with G9U0-R6 `PASS — AUTHOR APPROVED`.
R6 intentionally has no productive Desktop Point-tool consumer; the accepted
R6 diagnostic surface is the kernel test-host/API, and the end-to-end manual GUI
smoke is deferred to G9U1 by design. This acceptance does not authorize or
execute G9U1.

The distinct retained risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains **OPEN / TRACKED**. The R6
periodic point-interaction evidence does not supply the missing native
intersection-quarantine round trip.
