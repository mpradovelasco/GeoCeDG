# CU7 R4 — Native fidelity memo and work budgets

2026-11-04. Candidate design before code, under the author-authorized R4 P2-D
correction. This is not an accepted ADR, D1 adoption or CU7 closure.

Classification: implementation/debugging in the shared native fidelity gate and
its existing transactional/Undo consumers. Authorities are the live R3 code and
tests at 00d65aa0c1cba9c4c0f673abebb1c446c65f98a7, the fixed upstream recorded
in docs/upstream/BASELINE_COMMIT.txt, and R4 section 6. The original independent
NativeMemo probe and its failed receipts remain unmodified outside this worktree.
The optional .github/copilot-instructions.md is absent in this checkout.

## Cause and units

R3 debits its 4096 counter before looking up a resolved GeoElement in the memo.
Thus repeated references to the same unchanged built-in axis spend fictitious
exploration work. The 4097th query loses its witness, and 4100 individually
supported native points falsely report UNSUPPORTED_SOURCE_CLOSURE.

One fresh FILE or MEMORY pass will distinguish top-level queries, memo hits,
new GeoElement nodes, new expression nodes, examined dependency/expression
edges, active-cycle references, and maximum newly explored depth. Identity maps
are evaluation-local memoization only, never durable geometric identity. No memo
is retained across native edits or restore sessions.

The effective default technical limits are 4096 distinct source nodes (Geo and
expression nodes combined), 65536 examined edges, and depth 256. The node and
depth limits preserve the existing bounds with honest units. The explicit edge
bound prevents a huge list of repeated references from evading every work bound.
An external O(1) memo query neither charges nodes nor edges. Following an edge
of a newly examined source charges that edge even if its destination is memoized.
Limits are immutable and may only be reduced for focused tests/explicit smaller
work policies; values above these hard ceilings are rejected. Production uses defaults.
No document-wide object cap is introduced. Host nodes are observed separately;
4100 native places sharing one axis require one new source node, not 4100.

## Outcomes and reuse

NativeSourceFidelity2D gains additive Outcome SUPPORTED, UNSUPPORTED and
RESOURCE_EXHAUSTED, an exhaustion kind (nodes, edges or depth), immutable Limits,
Metrics and Assessment, and assess(source). Metrics also distinguish exploration
attempts from distinct processed nodes: retrying an unfinished node does not
spend a second distinct-node unit, while its examined edges still cost work.
Existing Boolean methods mean only
Outcome.SUPPORTED. An active cycle is an unsupported constructive closure.
Memo lookup precedes exploration/depth charging. Resolved support and intrinsic
non-support may be memoized; resource exhaustion is never memoized as intrinsic
non-support. Previously proved memo entries remain usable after an unrelated
exhaustion. A new pass starts with fresh limits, counters and evidence.

NativeTransactionalState2D gains an immutable host assessment containing its
existing typed reason, host-node count and source metrics. Its distinct
SOURCE_FIDELITY_RESOURCE_EXHAUSTED reason must not become
UNSUPPORTED_SOURCE_CLOSURE. Capture reuses the already examined host closure
instead of walking it again. Existing point/source, serialization-slot, registry,
private-binding and exact-restoration guards remain unchanged.

## FILE, MEMORY, atomic rollback and Undo

FILE keeps its conservative readable legacy payload when the optional exact
field lacks a positive witness, whether intrinsically unsupported or resource
limited. Construction exposes the actual last FILE-pass metrics so that omission
is not reported as proof of intrinsic non-support. It does not emit a partial
exact field or change the strict reader.

MEMORY admits only a supported host. Construction's atomic and native-Undo
capture boundaries reject genuine resource exhaustion before mutation/capture;
they do not switch to historical restoration under a false unsupported label.
The existing historical route remains for genuinely unsupported native tables,
including the documented mixed-history transition. GeoCeDGUndoManager checks
the resource reason before choosing that route or advancing the history cursor.
The failure is the existing IllegalStateException family, identifying the typed
resource reason; no new exception hierarchy or fallback permission is introduced.
The existing creation Failure and movement Status remain UNSUPPORTED_HOST_STATE.
An additive native-host subreason travels with that rejection/result; only resource
exhaustion receives the distinct diagnostic SOURCE_FIDELITY_RESOURCE_EXHAUSTED.
Desktop transports this captured reason without re-running the host scan and
shows a localized native-restoration work-budget message for click, Q and drag.
It never substitutes the geometric continuation BUDGET_EXHAUSTED status. A focused
Desktop regression also checks an exhausted current host does not advance Undo's
cursor or alter its history/construction.
Frozen supported Undo templates still create fresh one-use restore sessions.
Their payloads and exact XML/registry/DAG verification do not change.

## Files and validation

Minimal editable set: this design, NativeSourceFidelity2D,
NativeTransactionalState2D, Construction, GeoCeDGUndoManager, and the new native
shared regression R4NativeFidelityBudgetTest and Desktop R4NativeBudgetDesktopTest.
The additional editable consumers are the existing creation/continuation Result,
public operations and reference/native frontend adapters, solely for this subreason.
Tests cover the auditor's repeated
axis/4100-point cases, real unsupported input, tiny injected node/edge/depth
budgets, free cached queries after exhaustion, new-pass recovery, FILE metrics,
and transactional/Undo resource preflight without mutation. Existing F1–F7,
native exact rollback/Undo, Classic and unsupported mixed-history tests remain
oracles. Root owns actual canonical runs and effective selection registration.

No geometric algorithm, tolerance, authority, native payload, identity, provider,
R6/H1 permission, D1 exception or F8 state changes. Bootstrap impact: NO_CHANGE;
no prerequisite/toolchain/launcher changes. Infrastructure impact: none in this
patch; root records registry/calibration changes separately. GUIDE_IMPACT is
reviewed by root for the observable diagnostic. Acceptance requires actual focal
and frozen canonical receipts, not this design or static inspection.
