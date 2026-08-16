# Objective

Implement G9A2 spatial semantic core and projection-defined point pilot on
`feature/g9a2-spatial-semantic-point-pilot`.

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

# Mandatory entry gate

Require G9A1 PASS/author approval, approved point/frame/binding/status contract,
Accepted ADRs 0010/0011, clean branch, prompt hash, and composed PASS.

# Hard dependencies

G9A1 identity/XML PASS and the approved projection-system/point contract are
hard semantic dependencies. G9O1 and every frontend/product phase are not.

# Recommended execution predecessor

Execute immediately after A1 to validate its persistent system records before
other consumers accumulate; this recommendation does not bypass author review.

# Global/release gate

A2 contributes the point/system pilot to G9A and global closeout. It does not
authorize A3, G9B or a procedure workspace automatically.

# Authority and evidence hierarchy

Read current coordinate systems, 2D/3D geos, construction DAG and serialization;
then approved G9 spatial spec/ADRs and analytic validation plan.

# Scope

Implement projection frames, roles/bindings, projection systems, typed diagram
maps and hinge/change-of-plane relations, independent status axes,
projection-defined spatial point reconstruction, intrinsic and common-diagram
reprojection, system/object certificate publication, normal DAG propagation,
and a one-way derived 3D view.

# Explicitly forbidden scope

No general primitive schemas, composed objects, implicit bidirectional editing,
label/proximity repair, public procedures, U0/X1/U1, or 3D-view authority.

# Editable boundaries

Additive GeoCeDG shared spatial packages, minimum approved factories/dispatch,
point pilot tests/models, docs/evidence/verifier, and registered upstream edits.

# Productive versus test-private permissions

Only point semantics and reusable core types are productive. Other primitive
fixtures remain test-private and cannot imply G9B support.

# Architectural placement

Spatial identity, reconstruction and certificates belong in the shared kernel
DAG; views are derived adapters.

# Required design/specification

Enforce one authority per object/revision, defining/derived roles, frame/system
rank and nondegeneration, `q_i=pi_i(x)` versus `p_i=delta_i(q_i)`, pullback of
diagram bindings through valid `delta_i^-1`, common-gauge invariance, atomic
publication and explicit stale/invalid states.

# Geometric invariants and degeneracies

Two known nonparallel orthographic frames normally define a point; rank loss,
inconsistent rays and undefined projections yield typed states with no stale
spatial result.

# Compatibility and serialization

Use the G9A1 identity/XML substrate. Legacy files remain unassociated. Derived
3D objects do not become independent editable truth.

# Required tests and commands

Test generic/on-plane points, intrinsic-to-diagram round trips, line-of-ground/
hinge consistency, orientation/fold side, common-gauge invariance, parallel/
near-parallel frames, invalid maps/system inconsistency, dynamic degeneration/
recovery, rename/copy/undo/reopen and zoom/DPI/camera independence. Run focused
G9A2 and full composed verification.

# Required artifacts

Source/tests, canonical point models, analytic evidence, counters/benchmarks,
focused verifier, traceability and report.

# Stop conditions

Stop on any authority cycle, stale geometry after failure, a diagram map that
depends on view/screen state, inability to separate intrinsic and diagram
coordinates, or identity inferred from labels/coordinates.
