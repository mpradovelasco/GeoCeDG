# CU7 — Native free-conic point state: candidate amendment

2026-11-03, Europe/Madrid. **CANDIDATE; implementation and independent review pending.**
This design is recorded before the inherited-source patch. The author explicitly
authorized precise inherited-source changes after the exact native rollback defect
was demonstrated. That authorization permits this repair; it does not approve its
eventual implementation, verification results or CU7 closure.

## Classification and authority

Implementation/debugging in shared kernel native serialization and loading. The
source of truth is the actual `GeoPoint`, `AlgoPointOnPath`, XML loader and normal
construction transaction, compared with pinned upstream
`9b93256b7df401ff056c37b502d82df4d72b1522`. No renderer, Desktop geometry solver,
new checkpoint framework, durable identity scheme or upstream baseline change.
The earlier failure and strict oracles remain in
`docs/validation/pre_g9b_cu7_native_rollback_stop_record.md` and their original logs.

## Semantic clause

For an eligible native free `Point(c)` on a full two-dimensional conic, its native
argument `PathParameter.t` and `pathType` are persisted inputs. Saved homogeneous
coordinates are its accompanying finite/undefined representation. Loading newly
tagged state must preserve that pair and representation exactly, including the
first load of a fresh live construction. It must not infer the saved argument by
reprojecting its saved coordinates. The ordinary parent and DAG remain unchanged.

An additive versioned conic-state tag belongs only to the free, one-input native
point-on-path algorithm. Explicit `Point(c, numeric)` remains governed by its
numeric input. Limited conic parts, three-dimensional paths and unrelated path
types retain their existing formats and behavior. Absence of the new tag takes
the historical coordinate-load route. The old Cartesian `curveParam` contract
must not acquire a new meaning. Newly recognized invalid/inapplicable state must
have an explicit loader policy; it is never permission to bypass an incompatible
definition or silently substitute a default native argument.

## Minimal lifecycle design

The writer and reader carry the native parameter/type with the standard raw
coordinates. A point-owned restoration transition updates the raw coordinates,
inhomogeneous caches, native parameter, undefined-state fallback parameter and
animation origin coherently, without calling the conic's coordinate projection.

The existing loader performs a construction-wide recomputation after leaving
loading mode. A parser-only repair would be overwritten by that recomputation,
especially when path handling is OFF. Therefore a narrowly scoped, one-use load
state may bridge that existing final recomputation. It is reused only while the
actual full conic has the exact structural/numerical state captured at restoration.
The first completed post-load native point computation consumes it. A changed
source discards it and follows the normal path computation. It cannot permanently
override future source edits, native dragging, animation or explicit definitions.

No transaction nesting, tolerance-based repair, pre-normalized fixture, native
point identity inferred from coordinates or late Desktop repair is introduced.
The XML reconstruction still resolves ordinary parent/output correspondence in
its existing order; this tag adds state to that output rather than a new identity.

The initial concrete format, fixed before its production edit, is
`conicPointState` version `1`, preceding the ordinary `coords`, with `t`,
`pathType`, exact `x/y/z`, `tempT`, `tempPathType` and `animation` attributes.
Double values use string encodings that retain signed zero; the existing generic
double XML formatter is not changed. The normal coordinates remain available to
older readers. For the new reader they must agree numerically with the tagged
coordinates (the legacy formatter may remove a zero sign); the tagged bits prevail.
Unknown versions/fields, missing/duplicate state or incompatible definitions are
recognized errors, not fallback permissions. New state is restricted to the exact
native 2D full-conic class and the free one-input algorithm. Finite regular circle,
ellipse, parabola and hyperbola state must reconstruct coherently through its
native parameter. The undefined case preserves the all-NaN representation and its
remembered parameter on an undefined source. Unsupported, nonunit homogeneous or
incoherent live states are not silently certified by the writer.

The one-use correspondence check compares actual native source type/defined state,
matrix, axes, eigenvectors, translation and parabola parameter. It does not use a
label, visual proximity or viewport. Later source changes always return to normal
computation. This deliberately bounded format is not a guarantee that every
unrelated native path or legacy degenerate representation has exact roundtrip.

## Bounded host-state gate for the new permissions

Recorded before this additional guard's implementation. The strict original
circle/h and H1 failure oracles pass with the new codec. Additional native
regressions identify two deliberate exclusions: coordinate-projected hyperbola
caches need not equal native forward(t) bitwise, and an undefined conic can reload
with different inactive matrix caches. The failed development receipts remain.
Neither is silently canonicalized, tolerated or advertised as exact host restore.

The new individual-forward creation and new local-chart continuation permissions
must therefore check their host's existing free-place input perimeter before any
live mutation/snapshot. Use the already defined native free-place classification
of `ReconstructionExactState2D`: a point with the one-input native point-on-path
algorithm or a point in a region. In this first new-operation table only a finite
tagged-coherent native free point on a defined full 2D conic is admitted. Other
native free paths/regions, limited or incoherent conic states and undefined conic
sources refuse with an explicit unsupported-host diagnostic before mutation.
Unproved three-dimensional free-place states are excluded too. A source driver
or unrelated free-place input is subject to the same check. This is a conservative
availability boundary, not a geometric nonexistence claim or a new checkpoint.
The same new-transaction gate applies to the additional native reference-Q
placement route. Known undefined/nonfinite native conics in the host are refused
even without a free-place dependent, because their inactive matrix is another
demonstrated roundtrip difference. Finite checks concern the conic's active native
representation, not unrelated unused fields. Ordinary inherited native Point
behavior is not retroactively assigned this new permission or guarantee.
The gate also excludes known native input encodings already characterized as
lossy by the existing CU2 fidelity tests: an angular `MyDouble` literal without
its original text (degree serialization changes its binary64 value), and signed
zero inputs whose standard native encoding loses the sign. Reuse existing exact
input/literal classification; do not repair those other codecs in this task.
The guard visits native roots and unlabelled inputs, not only visible objects.
It is a restrictive host-state capability check, never a Boolean permission
that promotes a globally UNRESOLVED inverse result.

Historical R1/R6 and H1 retain their existing admission contracts and normal host
restoration mechanism; this independent host gate must not silently rewrite those
permissions. The publication-callback strengthening and corrected conic codec
apply through their existing normal paths, but no universal exact native-host
claim is inferred for every legacy path/region. The new strong guarantee belongs
to the explicitly guarded new permissions and demonstrated host perimeter.

The native codec's undefined-state unit test may establish exact point argument,
fallback, animation and representation recovery while explicitly recording the
different inactive conic matrix. A separate new-operation regression must prove
the corresponding host is refused before mutation with exact unchanged XML.
Original fresh h/full-XML and direct-H1 rollback oracles remain unchanged.
No failed whole-host oracle is relabelled as passing evidence.

The guard and the XML writer each perform a native forward consistency check for
an admitted finite conic point. Those two checks are explicit freshness/snapshot
work, additional to the bounded inverse/point-binding budget; no hidden cache or
new runtime counter framework is introduced merely to remove that small repetition.

## Runtime portability boundary

Exact native host restoration is a same-runtime guarantee in the tested perimeter.
The tag stores the parameter bits portably, but the reader also demands bitwise
coherence of its native forward evaluation with the saved raw representation.
Java Math permits implementation-dependent sin/cos results within its accuracy
contract. A different compliant JVM/CPU may therefore refuse a newly tagged state
whose forward bits differ there. This is a declared interoperability limit, not
permission to silently reproject, alter the saved argument or certify cross-runtime
raw fidelity. Old untagged inputs retain their historical route. Current successful
test and product runs do not, by themselves, prove transfer between every runtime.
Cross-runtime native compatibility remains an independent review/adoption concern;
this task does not rewrite the inherited trigonometric implementation or add an
unproved tolerance-based acceptance rule.

## Validation gate and compatibility

Keep exact first-roundtrip and rejected-transaction XML/parameter/coordinate
assertions on the original fresh circle/h fixture. Add native ellipse/parabola/
hyperbola and undefined/recovery cases as supported; test old untagged input,
explicit numeric definitions, excluded limited conics, subsequent source edits,
animation-origin coherence, repeated reload and invalid new state. No softened
tolerance or pre-save/reopen is an acceptable replacement for these oracles.

Register the native regressions in the effective CU7 shared PHASE selector. Repeat
the creation/drag shared suite and Desktop compatibility suite, refresh the measured
catalog only from successful real runs, then execute the required frozen canonical
profiles. Keep development and final-source provenance separate. Physical mouse
smoke and independent/author review remain separate obligations.

Required impact: shared inherited serialization/load infrastructure, hence FINAL
(including composed obligations), INFRA_UNIT for selection metadata, and the exact
CU7 PHASE. BOOTSTRAP: NO CHANGE REQUIRED; no prerequisite, runtime, dependency or
workstation assumption changes. GUIDE_IMPACT: UPDATE the candidate's persistence
scope/status after results are established. No main, book, release or CU7 closeout.
