# CU7 R3 — Focal native Undo companion

2026-11-04. Candidate design recorded before this corrective code. This is part
of the authorized F1/F3 native recovery and criterion 11, not D1 adoption, a new
durable identity system or a universal snapshot framework.

## Demonstrated gap

The 218012c4352c2347751e8d31323e659577b52ceb development checkpoint reproduces
an additional exact-state gap in baseline-native-undo-02. A projected native
Hyperbola point H, dependent Circle(H,1), and free native point P coexist with
an unrelated newly created interactive semantic circle point. Source setup and
every stage of its controller gesture retain H/P exactly. One Undo and Redo
retain the semantic ID/address and dependents, but change H's argument bits,
P's argument/fallback/animation and raw coordinates. All four exact native
assertions fail. The original result is retained. The earlier baseline-01 was
a fixture error: its visibility loop deliberately called updateRepaint on native
ancestors and therefore recomputed them before the gesture; it remains failed.
Baseline-02 uses visibility-only setup and proves the fresh projected inputs
are unchanged and their FILE fields remain absent before the actual Undo.

The cause is local: NativeTransactionalState2D accompanies atomic rollback,
whereas UndoManagerD currently stores ordinary XML in FileAppState. Suppressing
an ineligible FILE field correctly restores public readability but cannot make
that historical XML a bitwise native Undo checkpoint.

## Bounded extension

Reuse NativeTransactionalState2D's positive typed table, immutable input/source
payloads and private bindings. Keep public FILE eligibility and XML unchanged.
The existing GeoCeDGUndoManager owns the integration; Classic retains default
behavior. Geometric/restoration truth remains in the shared kernel.

Add minimal protected seams to the inherited Undo managers: capture XML on the
caller before scheduling, construct the AppState in the existing worker/baseline
path, and perform the existing zip read within a typed companion scope. Default
implementations preserve the inherited behavior. Prepared document-open baselines
must use the same capture path. Capture native state and private XML once, together,
with listeners=true; never recapture a moving construction in the worker. A
pending identity-keyed builder handoff is ephemeral work correspondence, not a
geometric identity. Verify its frozen bytes/digest before adoption.

An owned FileAppState subclass carries the immutable template beside its existing
temporary file. No label, XML position, coordinate or Java reference becomes a
durable geo identifier. Existing IDs and snapshot-only opaque bindings retain
their prior roles. No public document receives a private binding. Empty or
unsupported native tables retain the historical route and its declared scope.

This historical boundary also applies to mixed history transitions. An ordinary
native edit can legitimately produce a later unsupported state B after an admitted
typed state A. Its presence must not disable the existing native Undo tool. Exact
native Undo requires an admitted current pre-state as well as the admitted target
companion. If the current state is unsupported, load A through its ordinary public
XML fallback with the inherited guarantee, rather than granting an unproved exact
transaction or refusing ordinary native Undo merely because A has a companion.
Derive that fallback only from the frozen matching checkpoint by removing its
known private bindings and verifying the retained ordinary digest. No identities
or parameters are guessed or repaired. The fallback may be materialized transiently
on this exceptional path; no second live construction capture is needed.
This does not relax the positive host-state gate of new CU7 edits, or claim exact
native argument preservation outside the table. Test this transition in both
directions and make its historical scope explicit in the delivery.

Each Undo/Redo creates a fresh restore session from immutable payload/IDs/keys;
never reuse old GeoPoints, consumed flags or mutable binding maps. Legitimate
repeated Undo/Redo is explicitly authorized by that owned AppState, not by a
replayed public XML key. Require exact manager/Construction ownership and matching
snapshot bytes. Private keys are authorized only inside that active session.
Keep the existing NATIVE_OR_UNDO_RESTORE identity policy, rather than pretending
Undo is a spatial redefine rollback.

The normal reader installs the native editable input before reprojection and
uses its normal DAG once. Verify native raw/source/domain state, durable bindings,
registry and the captured XML using the matching listeners flag. Clear active
context and re-enable listeners on every exit. A typed verification failure must
escape the inherited recursive fallback; it must not be reported as a successful
Undo. Reuse the existing atomic mutation boundary for recoverable typed loads,
and restore the original command objects/cursor if that load fails. Do not weaken
the existing exact atomic-rollback checks.

AppState.delete releases its companion with the file. Pruning, stale asynchronous
generation disposal and aborted prepared baselines use that ownership path.
Detach/attach keeps the original commands and companions, preserving labels,
grouping and Redo. Clear-only history transfer must not prematurely delete held
states. Scheduling/factory failures must clear unadopted pending handoffs.

## Validation and cost

Keep the exact fresh mixed-chain regression unchanged after reproduction. Extend
it to repeated Undo/Redo and test listeners, prepared baselines, held history,
pruning/disposal, stale/foreign or malformed state rejection and exact failure
recovery as relevant to the implemented seams. Include all added classes in the
effective R3 PHASE and calibrate from real executions. Existing document-open,
Insert File/history, native, H1, Spline and semantic Undo suites remain guards.

The companion adds O(native points) retained data per stored history state,
bounded by the existing history lifecycle. It does not take an Undo snapshot per
mouse movement: one changed gesture remains one normal Undo store. A pre-load
atomic safety snapshot is charged separately on Undo/Redo, not hidden as local
continuation work. No duration threshold, tolerant restore, reprojected replacement
or extra global DAG is a substitute for exact native recovery.

BOOTSTRAP IMPACT: NO CHANGE REQUIRED. No runtime, dependency or prerequisite
changes. GUIDE_IMPACT: UPDATE native Undo scope. Required verification remains
FINAL, the exact R3 PHASE and INFRA_UNIT for selector/catalog changes.
