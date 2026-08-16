# Objective

Implement G9A3 spatial binding lifecycle and migration hardening on
`feature/g9a3-spatial-lifecycle-migration`.

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

# Mandatory entry gate

Require author-approved G9A2 PASS, approved lifecycle/migration contract, clean
branch, exact prompt hash, green G8 and composed authority.

# Hard dependencies

Author-approved G9A2 and the refined source-backed lifecycle/redefine contract
are hard dependencies. No public workspace, DXF or G9B phase is required.

# Recommended execution predecessor

Execute directly after A2 so hostile lifecycle paths are closed before U0 or
G9B consume the substrate.

# Global/release gate

A3 author approval closes G9A and is the branch point for G9B/G9C and G9U0.
Both tracks still require their own prompts and join only at global closeout.

# Authority and evidence hierarchy

Read current implemented G9A1/A2 source and tests first, then approved spatial
spec/ADRs, lifecycle matrix and legacy corpus.

# Scope

Harden binding/system/map/relation mutation, copy closure/remapping, undo/redo,
reopen, every host redefine route, delete/recreate, malformed references,
legacy unassociated files, explicit migration and deterministic recovery.
Distinguish recomputation, explicit compatible redefine, true replacement,
type/schema/role-incompatible redefine, copy and snapshot restoration.

# Explicitly forbidden scope

No label/geometric inference, G9B primitives, G9C composites, public U0/U1/U2,
DXF, universal merge/split genealogy, or silent repair.

# Editable boundaries

Implemented G9 spatial lifecycle/XML/copy seams, migration service/UI-neutral
API, fixtures/tests, verifier, evidence and docs only.

# Productive versus test-private permissions

Approved lifecycle and explicit migration are productive. Synthetic corrupt
files and future primitive bindings remain test-private.

# Architectural placement

Reference integrity and migration belong to the shared document/semantic model;
user prompts belong to later frontends and cannot define identity.

# Required design/specification

Apply the complete rename/copy/undo/reopen/redefine/delete/invalid/recovery table.
Compatible redefine retains durable ID only through the explicit target-based
transaction and approved compatibility predicate, increments definition
revision and changes topology revision only when declared; all transfer or
invalidation is atomic and provenance-recorded.

# Geometric invariants and degeneracies

Invalid or underdetermined objects preserve identity but publish no stale
solution. Recovery is deterministic and never proximity-based.

# Compatibility and serialization

Old files remain valid and unassociated. New files round-trip all IDs, roles,
frames and schema versions. The GeoCeDG Classic diagnostic path preserves,
recomputes, saves and reopens supported native spatial records with exact IDs
and bindings under the same kernel while creation remains disabled. Characterize
external upstream distributions that do not know the persisted types as an
unsupported-open boundary; never add silent label/list/coordinate downgrade as
a compatibility workaround. Unknown future versions fail truthfully.

# Required tests and commands

Run in-place/soft/no-child/full-XML-rebuild redefine routes, compatible and
incompatible cases, multi-output/cardinality unsupported cases, system/map/
binding changes, copy subsets/closures, delete/recreate, undo/redo, collisions,
malformed/unknown XML, old-file corpus, deterministic reopen and atomic failure
recovery; GeoCeDG Classic native round trips; external-upstream unsupported-open
fixtures with zero lossy conversion; then focused G9A3 and composed verification
without `-SkipBuild`.

# Required artifacts

Source/tests, migration fixtures, compatibility matrix, counters, verifier,
machine evidence, traceability and closeout report for G9A.

# Stop conditions

Stop if migration requires inference, copy leaves external dangling bindings,
undo changes serialized identity, compatible redefine needs label/coordinate/
index heuristics, or a failed load retains stale spatial truth.
