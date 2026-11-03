# CU7 R3 — Corrective candidate and review protocol

2026-11-04. Technical candidate under the author's authorization of F1–F7.
This versioned record describes the implementation and verification contract.
The exact frozen SHA, verdicts, timings and evidence hashes are supplied by the
external R3 delivery manifest after verification. This document does not claim
author approval, D1 adoption, publication or CU7 closure.

## Baseline and authority

Base commit `68b14d544bafd41f5da7b131a120942d4eab95f1`, tree
`a89f61b23c4cb5cd68ebe4e325ff01841723bcde`; corrective branch
`codex/pre-g9b-cu7-general-point-interaction-r3`. The original candidate branch
and approved main `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17` remain preserved.
The pinned upstream is `9b93256b7df401ff056c37b502d82df4d72b1522`.

Sources inspected include repository AGENTS, approved R6/M08-S-R2 and ADR0019/0041,
the live repository and upstream pin, the native XML/transaction contracts,
ConstructionBoxModel2D and v3 predicates/jets/selectors, the public semantic
point operations, Desktop event/placement routes, the current verification
registry and all independent audit F1–F8 probes/results. The audit's recovered
final report and section C native family matrix are preserved separately with
source-chat provenance. Its original 48 artifacts and 45 declared hash checks
are recorded in `source-baseline-and-audit-integrity.json`; they are not edited.

Before corrective code, the unchanged IndependentNativeAncestorAuditTest was
rerun against 68b14: one test failed with `Invalid native conic point state`.
The original failure remains in `baseline-f1-ancestor-01`.

## Implemented design

The design-before-code record is
[the integrated R3 contract](../architecture/pre_g9b_cu7_general_point_interaction_r3_candidate.md).
The additional mathematical argument is in
[local projection continuation](../architecture/pre_g9b_cu7_r3_local_projection_design.md).
The subsequently demonstrated native Undo gap and its design-before-code record
are in [the focal Undo companion design](../architecture/pre_g9b_cu7_r3_native_undo_companion_design.md).

F1/F3 separate positive transitive FILE fidelity from exact typed MEMORY recovery.
Unknown or lossy native ancestors suppress the optional public conicPointState
field and retain the historical readable payload. This is a readability fallback,
not an exact file-parameter promise for that excluded closure. File classification
is fresh for every serialization pass and includes defining inline/list inputs.

Atomic construction mutation still uses its existing XML/DAG lifecycle. A focal
native complement captures raw point caches, parameter/fallback/type, animation
and typed source/domain state for ordinary line, ray, segment, conic and arc
places. Existing durable IDs provide correspondence when available. Otherwise
an opaque one-use snapshot key is carried only by the private rollback XML; it
does not allocate a durable ID or match by labels, coordinates or XML position.
Restoration occurs before coordinate reprojection, is consumed through the normal
DAG and is verified exactly. Missing/foreign/duplicate/replayed keys and failed
verification reject safely; unsupported native states retain their typed reason.
Undo/Redo uses an owned private memory companion to the existing AppState when
the native table is positive. XML and payload are captured together before the
asynchronous store; each load receives a fresh scoped restore session. Normal
public FILE serialization stays distinct. Prepared baselines and held history
keep their existing ownership and cursor semantics. Exact native Undo is tested
on the fresh projected hyperbola chain, not inferred from a semantic address test.
Exact typed Undo requires both the current pre-state and the target companion to
be admitted. Crossing an unsupported native history state retains historical Undo
through a digest-checked ordinary XML fallback; it does not disable the native
tool or claim exact native bits for that excluded transition. New CU7 edits still
require their own positive host-state gate.

F2 retains structural affine-circle, historical R6 and Spline contracts and adds
an optional local projection chart using the same certified construction model.
For g=(F-q)·F', prove positive speed, uniform validity/selection, gu>=mu>0 and
existence over one chart and the entire accepted-target segment. The next chart
contains the previous certified root enclosure and preserves equation, component
and selector. Root enclosures, canonical rounding and lifted-period errors enter
the declared parameter bound. The numerical representative receives ordinary
forward evaluation; numerical proximity remains FLOATING_POINT_UNCERTIFIED.
No global nearest point, global uniqueness or exact orthogonal projection is
claimed. C2 is sufficient for this optional provider only.

Included genuine DOMAIN_END boundaries permit proved unilateral KKT continuation.
Internal chart edges and unresolved/structural edges do not. Periodic crossing
requires the existing certified component/selector correspondence and a lifted
chart; coincident images do not create a seam identity. Zero speed, tangency,
invalid gaps, undecided selectors and exhausted proof budgets retain barriers.

F4–F6 carry one immutable Desktop request from the mouse/Q capture to publication:
world coordinates, actual world radius and policy, document/view/tool context and
source evidence. Native offers use typed defining geometry/domain, rather than
source XML. Freshness is checked after chooser callbacks and within the single
atomic publication, including callbacks during DAG/view notification. Same-view
zoom/pan retains the captured request. A changed context rejects without partial
publication. Allow dragging defaults true; an interactively locked point retains
its dynamic source dependency. Drag uses the same guarded kernel publication.

F7 rejects duplicate attributes before parser map normalization and requires an
empty self-closing exact-attribute leaf for the new fields. Opt-in is confined to
the construction reader; unrelated QDParser clients retain legacy behavior. The
existing D1 DOM validator is separately tested against duplicate attributes,
children, text, paired-empty and CDATA payloads. Its candidate exception is not
adopted as an author decision.

## Inherited-source impact

Nine upstream files are changed, with purpose/authority in
`docs/upstream/modified-files.yml`: Construction (the existing transaction),
GeoPoint and AlgoPointOnPath (typed state/load consumption), ConsElementXMLHandler
and MyXMLHandler (binding/reader scope), DocHandler and QDParser (the focal lexical
grammar), plus UndoManager and UndoManagerD (default-preserving capture/store/load
extension seams for the existing GeoCeDG manager). Shared semantic state and serialization cannot correctly live in a
Desktop-only geometry algorithm. No package moves, parser-wide strict mode,
new upstream baseline or durable identity redesign is introduced.

## Functional perimeter and permanent regressions

Creation retains the authorized COMPLETE reconstructible v3 forward permission,
explicit choice, canonical address, finite forward evaluation, freshness, budget
and atomicity. Click and Q consume the same discovery policy. Existing R6/H1
admission remains distinct. The new continuation adapts v3 operations rather than
adding an evaluator for each example: circle/affine compositions, segment and arc
drivers, regular LINE_CIRCLE, UNIT_VECTOR/DOT/MEET, a rational hyperbolic MEET
construction and direct admitted intervals are regression families. SplineV2
retains its historical provider. A missing proof is not geometric impossibility.

Native drivers and derived geometric families are distinct. The construction
capture currently has typed circle, segment, circular-arc and scalar-affine
drivers. The following missing capabilities are explicit technical boundaries:

| Family | Current R3 representation | Concrete next capability |
| --- | --- | --- |
| Circle, segment, circular arc | Direct typed v3 drivers and admitted derived programs | Extend only where a new construction operation lacks predicates/jets. |
| Line, ray | Native tools preserved; a finite straight interval can be represented by an admitted segment/scalar-affine program | A typed native driver adapter with explicit oriented finite/unbounded domain and end policy; no generic Path parameter assumption. |
| Ellipse, parabola, hyperbola | Geometric derived examples through admitted v3 operations; native tools preserved | Typed native conic driver capture with forward parameter correspondence, frame/orientation, connected branches, domains and interval jets. Passing native movement alone does not supply this bridge. |
| Parametric curve, function | Native tools preserved; a direct semantic provider may have an admitted certified interval | Capture the original expression/argument/domain into a reconstructible certified evaluator, including invalid-set predicates and serialized structural selectors. |
| Native spline | Native movement preserved; SplineV2 retains its own semantic constructor/provider | Explicit conversion from defining control data with provenance and parameter correspondence; no automatic identity from a sampled shape. |
| Polyline, implicit curve, legacy locus | Existing native behavior preserved; no new durable V2 bridge | Respectively typed oriented edge/vertex identity, certified local root/topology correspondence, or recovery of the original reconstructible construction. Render proximity supplies none of these. |

Nested v3/semantic similarity compositions also need certified transport of jets,
predicates and component lineage; the new direct-program adapter does not pretend
to provide it. Proof depth and work budgets can refuse a regular region. Such a
refusal records missing evidence/resources and is not geometric nonexistence.

The synthetic ellipse helper called h in a kernel stress test is not the author's
document h. Actual author-document evidence uses the unchanged M08-S resource and
a copy of C:/Temp/TestM08-S.cedg. The original file SHA256 is
`4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d`.

R3NativeStateRecoveryTest separates file/repeated load, ancestor changes, native
rollback, identity/binding and exact registry/XML checks. R3NativeStateGrammarTest
tests lexical compatibility. R3PointContinuationTest tests continuation, charts,
ends, seams, repeated images, parameter bounds, budgets and reopen.
R3PointInteractionDesktopTest tests captured radius, source/view/callback staleness,
click/Q/native creation, lock, drag, Undo and exact rejection. The historical
R6/H1/M08/Spline suites remain selected. R3NativeFamilyCompatibilityTest exercises
thirteen native families with a free membership witness, unchanged native parent
and DAG dependents. R3NativeUndoRecoveryDesktopTest separates exact native Undo
from file fidelity and atomic rollback; R3NativeUndoLifecycleDesktopTest covers
the companion's existing history lifecycle. That is kernel compatibility, not physical mouse evidence or
a new durable Locus V2 correspondence for implicit curves, polylines or old loci.

R3PointInteractionPerformanceTest measures circle, segment and LINE_CIRCLE with
100/1000/10000 unrelated numeric objects, a native place without a durable ID and
40 dependents. It reports resolution, native capture, XML capture, DAG, no-op and
forced restoration separately. Warmups and sample count are explicit; duration
never changes acceptance. GUI is explicitly absent from headless timings.

## Verification and independent handoff

The effective phase is PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R3. It selects all eight
new R3 classes and the historical regression closure. Count/fingerprint metadata
must be measured from successful real executions with the canonical updater.
Development probes, discovery, PHASE, FINAL and physical smoke are separate.
Failed development runs remain failed records, even after correction.

Required final checks: STATIC, INFRA_UNIT, this exact PHASE and FINAL at a frozen
clean SHA. FINAL includes the composed obligations by its typed execution plan;
its receipt claims only the profile actually established. Use a contained ignored
output directory for FINAL/PACKAGING. Preserve diagnostics independently from
acceptance, including missing historical sources; do not call them PASS.

The external delivery must provide commit/tree/worktree state, full delta,
original and new evidence hashes, actual result/receipt paths, family coverage,
timing observations and physical smoke chronology. An automatic accepted result
does not approve D1, F8, the author smoke or CU7 closure.

## Remaining decisions and author smoke

[F8 is a separate disabled proposal](../architecture/pre_g9b_cu7_r3_partial_forward_permission_proposal.md):
LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION distinguishes certified local existence
and structural identity from global inventory completeness. The existing forward
permission still requires COMPLETE. D1's narrowly validated payload exception and
the declared same-runtime native codec portability boundary remain author review
items. No unproved durable bridge for implicit curves, polylines or old loci is
invented.

Author smoke on the exact delivered candidate:

1. Case A: construct the radius-three locus from Circle/N/Vector/Q; create by
   Point click near (3,0) and by a nearby Q, without a parameter entry. Verify
   default Allow dragging, both directions, a seam crossing and live dependents.
2. Repeat with Allow dragging off; edit a source input and verify the point still
   follows its construction. Check the same choice for a native segment/circle.
3. Case B: open a copy of TestM08-S.cedg, retain h/ReferenceR1, create and move on
   regular admitted regions. Record any precise semantic barrier; do not force it.
4. Exercise regular LINE_CIRCLE, v3 segment/arc ends and SplineV2. Select points
   with real mouse events, reverse direction and return from admitted endpoints.
5. Repeat click/Q at low zoom, change view during chooser/preimage callbacks,
   and verify stale offers publish nothing. Pan/zoom in the same view must not
   reinterpret the captured world click or radius.
6. Check one Undo/Redo per changed gesture, save/reopen, copy/remap, durable
   identity and dependents. Reopen mixed native chains and repeat load cycles.
7. Attempt gaps, disconnected coincident components, singularities, stale source
   revisions and exhausted budget; verify last valid state and a brief diagnostic.

Recommendation remains a reduced CU7 technical candidate only within the proved
provider/native restoration table. Exclude unresolved barriers, unrepresented
durable native families, cross-runtime bitwise portability, F8 and pending D1
adoption. Stop after independent focal audit and author smoke preparation.

BOOTSTRAP IMPACT: NO CHANGE REQUIRED. No prerequisite, runtime, dependency or
workstation assumption changes. GUIDE_IMPACT: UPDATED, EN/ES point-interaction
guides now describe request capture, transverse families, lock and persistence.
VERIFICATION_INFRASTRUCTURE_IMPACT: TEST_SELECTION only. FINAL is required by
shared native persistence/loading and atomic reconstruction changes.
