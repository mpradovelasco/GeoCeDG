# ADR 0014: Export-only DXF approximation and conditional mandatory fidelity sidecar

- Status: **Accepted**
- Accepted: 2026-08-16
- Phase: G9P design; G9X1 implementation not authorized

## Context

Accepted ADR 0005 keeps DXF outside geometric truth and establishes exact G5
entities. The current data model contains an unused approximation marker, while
the writer and UI expose no tolerance or fidelity evidence. Enabling that seam
would produce plausible files with an unreported semantic loss. Locus V2 and
non-native curves also contain branches, gaps, domains, revisions, and work
limits that a flat entity list cannot explain.

## Decision

1. Keep approximation entirely inside a read-only export snapshot; it never
   creates a `GeoElement` or construction dependency and never uses render data.
2. Classify every source component independently as `EXACT`, `APPROXIMATE`,
   `UNSUPPORTED`, or `INVALID`, with orthogonal reasons and guarantee evidence.
3. Use deterministic bounded adaptive `LWPOLYLINE` refinement as the first
   non-native baseline. Sampling alone may claim an estimate, not a certified
   global error bound.
4. Require explicit finite semantic domains for non-native unbounded curves and
   preserve every branch, valid component, gap, and constructive multiplicity.
5. Emit a deterministic JSON sidecar mapping semantic source/components to
   actual DXF handles and recording provenance, tolerance, guarantee, work,
   warnings, omissions, and the DXF hash whenever the operation includes
   approximate, omitted, partial, unsupported-not-exported, work-limited, or
   otherwise fidelity-reduced content. The sidecar is optional for an entirely
   exact export unless another product policy requires it.
6. Preflight before writing. Approved typed curve families may use their
   approved approximation strategy as normal export behavior only when the UI
   and report identify the approximate result explicitly. Partial output is
   disabled by default; any future partial option requires explicit user intent,
   a visible warning, and the mandatory sidecar. Strict reject/stop is the
   default and silent omission is forbidden.
7. Prepare and validate same-directory temporary outputs under a documented
   paired promotion/rollback policy. Do not claim universal two-file atomicity.
8. Defer `SPLINE` exactness and implicit contouring until separately approved
   mathematical and interoperability evidence exists.
9. G9X1 has no hard semantic dependency on the G9U0 public surface. It may
   consume an approved internal G6-G8 semantic snapshot and must disclose
   whether source identity is persistent or construction-revision scoped.
   G9U0-before-G9X1 remains the recommended product-integration order, and the
   global closeout requires both approved deliverables.

## Consequences

The design reports fidelity honestly, is independent of viewport state, and
does not pollute the construction. It adds a versioned result model, sidecar,
UI preflight, failure policy, and substantially broader validation. Consumers
that ignore a required sidecar still receive a standards-compatible AC1015
file, but GeoCeDG will treat the pair as the controlled export artifact. A
wholly exact file may remain a single controlled artifact under product policy.

This R1 clarification removes a frontend/public-surface dependency from the
export semantics without weakening final product integration. An X1 result
validated only through an internal V2 source is not evidence that users can
create or reopen that source; U0 supplies and separately proves that contract.

## Alternatives considered

### Use render tessellation

Rejected because zoom, DPI, and presentation state would become geometric
authority.

### Create temporary construction polylines

Rejected because export would mutate dependencies, protocol, persistence, and
undo state.

### Emit approximate entities without a sidecar

Rejected because DXF alone cannot communicate source revision, semantic domain,
error guarantee, omissions, or work-limit outcomes truthfully.

### Prefer `SPLINE` immediately

Rejected because a fitted or sampled spline is not thereby exact and current
consumer-conformance evidence is absent.

## Acceptance record and implementation gate

G9P closeout accepted this ADR, including explicit approximation, conditional
mandatory sidecars, strict partiality, semantic domains for unbounded curves,
and zero Construction pollution. Productive G9X1 requires the normative
specification, green G5/G6-G8
authorities and a separately executed canonical prompt. G9U0 PASS is the
recommended integration predecessor and a global-closeout requirement, not a
hard semantic X1 entry gate. G9X1 remains designed and not authorized.
