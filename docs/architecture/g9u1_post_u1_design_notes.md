# G9U1 author-review: deferred semantic requests

- Status: **ANALYSIS ONLY — DEFERRED BEYOND G9U1**
- Recorded: 2026-09-05
- Round-3 reconciliation: installed/embedded macro equivalence moved into the
  bounded G9U1 candidate; only broader detach/version policy remains deferred
- Productive implementation: **not authorized by this record**
- Source: the author-reviewed
  [`g9u1_author_resmoke_checklist.md`](../validation/g9u1_author_resmoke_checklist.md)

This note separates author requests that need a later kernel or public-contract
decision from the bounded G9U1 frontend stabilization. None of the alternatives
below may infer semantic identity from coordinates, labels, result order,
render samples, proximity, or Construction Protocol navigation.

## POST-U1-1 — spline interpolation nodes as metric endpoints

The current metric contract accepts endpoints with exact semantic position on
the same source. A free input point does not acquire a preimage merely because
its coordinates lie on a SplineV2 image.

A viable later extension would make the SplineV2 constructor publish typed
provenance from each interpolation-input occurrence to the exact semantic
address or addresses induced by that occurrence. The occurrence, rather than
the `GeoPoint` label or coordinate, is necessary because the same point may be
used more than once. Repeated nodes, coincident nodes, self-intersections,
periodic seams, knot ownership, topology revision and transformed sources must
produce either a unique current address or an explicit ambiguity. The concrete
address and its source revision would need normal persistence/copy/remap rules.

This is a shared-kernel extension affecting SplineV2 construction provenance,
`Length`, rich `LocusLength`, Point and transform covariance. It requires a
normative metric/persistence amendment and dedicated ambiguity tests. It is not
a frontend projection or nearest-point problem.

## POST-U1-2 and POST-U1-3 — oriented and forced-positive length

G7 currently defines length as a non-negative magnitude; `FORWARD` and
`REVERSE` select a route rather than an algebraic sign. The internal
`WRAP_TO_START` policy already models a non-negative traversal through the
positive-domain boundary, but is not a frozen public command surface.

Later design must compare:

1. changing `Length` endpoint reversal to an oriented scalar, with its large
   backward-compatibility cost;
2. retaining `Length` as magnitude and adding a typed direction/policy argument
   to the rich metric surface;
3. adding a separate oriented-length command or overload; and
4. exposing selected `FORWARD`, `REVERSE` and `WRAP_TO_START` policies without
   leaking internal enum names prematurely.

The decision must define open boundaries, periodic seams, gaps, same-position
endpoints, total length, scalar-versus-rich results and save/reopen. No name or
arity is selected here, and G7 is unchanged.

## POST-U1-4 — redefine or replace SplineV2/LocusV2

The applicable G9A rule remains: only an explicitly compatible redefine may
preserve durable identity, through the approved compatibility predicate and one
atomic kernel transaction. An incompatible replacement has a new durable
identity. `delete + recreate` cannot impersonate the old object.

A later design must specify downstream rewiring, construction revision,
undo/redo, save/reopen, copy/remap and failure rollback. A frontend may initiate
the transaction but cannot own its identity semantics. Until that contract is
approved, the current V2/rich definition inspector remains read-only.

## POST-U1-5 — Construction Protocol and construction position

Construction Protocol is a view of the procedure, not an authority for editing
the dependency graph. Literally stepping backward, redefining and stepping
forward is therefore rejected as the semantic mechanism.

The legitimate underlying question is whether a compatible replacement must
retain a meaningful construction position. A later kernel review should use
the existing replace/redefine and construction-index seams, verify topological
dependency order, and decide whether an atomic transaction already preserves
the required position. No Protocol-navigation state should be persisted as
object identity.

## POST-U1-6 — installed and document-local macro collision

An `AlgoMacro` document object requires the macro definition needed to rebuild
its construction. An installed package is an application preference and may be
absent or changed on another workstation; it cannot silently replace that
document authority.

Round 3 implements the bounded application/persistence reconciliation authorized
inside G9U1: the raw `.ggt` digest remains package identity, while a versioned
digest of every host-parsed macro definition proves equality after reopen. Only
the non-semantic `showInToolBar` flag is normalized. Complete equality adopts
the already embedded Macro objects for installed-tool presentation; partial or
unequal definitions fail closed. The embedded definition is never removed or
replaced, so portable reopen and `.ggb`/`.cedg` reconstruction do not depend on
application preferences.

What remains POST-U1 is only a distinct optional **expand/detach** operation, if
the host can convert macro results to ordinary construction steps without loss,
and any broader cross-version semantic-equivalence policy beyond the exact
versioned digest contract. Neither is implemented or implied by Round 3.

## POST-U1-7 — optional `branchKey`

A future overload may omit the branch only when the source publishes exactly
one unambiguous principal branch under a normative predicate. The command must
materialize and persist the concrete branch/component/address selected by that
predicate. Zero, multiple or changing eligible branches must fail explicitly;
there is no `first branch` fallback. The current public command remains
unchanged.

## G12 — precise configurable zoom

The live roadmap already reserves cursor-centred zoom, a configurable key,
precise navigation, extreme-scale handling, ZoomWindow, previous view and named
views for G12. G9U1 retains its basic ZoomWindow and `Ctrl`+`+` / `Ctrl`+`-`
surface. No productive G12 code is brought forward by this review.

## Tool-created degree helper and orphan cleanup

The current Spline V2 action prepares Algebra input; the host's auto-slider
machinery may create an ordinary `GeoNumeric`, but it records no exclusive
dialog ownership. Consequently the number remains an editable author object and
cannot safely be hidden, reclassified or garbage-collected merely because one
SplineV2 consumer disappears. A later cleanup policy would require explicit
creation provenance, an exclusive-consumer predicate and transaction-safe
undo/save semantics. G9U1 may improve help, but does not invent that ownership.
