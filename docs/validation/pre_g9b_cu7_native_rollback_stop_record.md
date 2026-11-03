# CU7 — Demonstrated native rollback stop

2026-11-03, Europe/Madrid. **Blocking semantic/transactional limitation.**
`selfApproved=false`. This record invokes the author's section 11 stop condition
for an exact host-state guarantee that needs a broader native checkpoint or
serialization/correspondence change. It does not stop nominal point interaction,
the physical smoke, source delivery or truthful canonical verification attempts.

## What is demonstrated

The existing host construction rollback serializes native XML and reloads it.
For a valid, freshly constructed native point on a nonzero-centred circle, that
round trip changes the driver's native `PathParameter.t`, not just a drawing cache.

The concrete h fixture uses:

```text
I=(0.4965866445356883,1.7854818601338847)
J=(-0.5082406249102692,2.5763181370126467)
f=Circle(I,J)
D=Point(f)
g=Line(D,xAxis)
K=Intersect(g,yAxis)
M=Midpoint(K,D)
h=LocusV2(M,D)
```

The first saved/reopened state changes:

| Field | Before | After |
| --- | --- | --- |
| Native driver t | `-0x1.d7919666c3e74p0` | `-0x1.d7919666c3e75p0` |
| D.x | `0.15395091539221334` | `0.15395091539221306` |
| D.y | `0.5535319360850213` | `0.5535319360850215` |

Free I/J inputs and durable ID sets remain identical. The derived g/K/M values
change with D. The following five cycles are bitwise stable in this fixture.
That establishes a particular canonical saved state; it does **not** establish
exact restoration of an arbitrary fresh live state. The exact XML oracles are
retained and no tolerance, expected skip or new allowlist hides this difference.

The external evidence directory
`artifacts/pre-g9b-cu7-general-point-interaction/` preserves:

* `h1-native-roundtrip-probe/process-evidence.json` and its JUnit/stdout: six
  actual cycles with XML, coordinate, parameter and free-input/ID comparisons.
* `atomic-native-rollback-static-review.md`: independent read-only source trace.
* The `publication-window-probe` process/JUnit records: five original failures
  demonstrating stale reference/source publication, callback side-effect leakage,
  an overwritten output coordinate and a hidden parameter changed without cascade.
* Current-source shared test receipts: exact H1 rollback assertions remain failures.

## Governing source and why this is semantic

The source behavior is unchanged from pinned upstream
`9b93256b7df401ff056c37b502d82df4d72b1522`:

* `source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java`
  around 2700–2732: snapshot and rollback use native XML and
  `NativeLoadPolicy.ROLLBACK_RESTORE`.
* `source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoPoint.java`
  around 1731: `curveParam` is written for Cartesian parametric curves, not native
  conic points. Its coordinate-load route invokes path reprojection.
* `source/shared/common/src/main/java/org/geogebra/common/kernel/kernelND/GeoConicND.java`
  around 450–468: circle reprojection computes an angle and sin/cos coordinates.
* `docs/adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md`
  around 211–227 separates exact live-to-internal-copy fidelity from persisted
  fidelity and forbids the reverse inference.
* `ReconstructionFidelity2D` and `ReconstructionExactState2D` treat native t as an
  exact reconstruction input. `ReconstructibleLocusEvaluator2D.DriverState` has
  an exact raw/native-state repair for its **isolated copy**, not a generic live
  host rollback facility.

Native/spatial redefine and Undo use the same XML restoration path. Existing
host-state comparison snapshots and procedural-order snapshots are not a generic
exact native-state restorer. Repairing D alone would not prove preservation of
unrelated native path points in the same construction. Unregistered ordinary
points also require an approved host correspondence policy; labels or coordinates
must not be invented as durable semantic identity.

## What the focal implementation closes

The new publication helper now owns one normal transaction for each concrete
R1, H1 or individual-forward selection commit. The facade passes its source/Q
freshness guard into that same transaction. There is no nested facade snapshot.
After output-label callbacks, it checks current source/reference/authority,
actual finite coordinates against the normal binding, exact encoded address/raw
state, current binding, registry ID, role and dedicated-state ownership. Observable
state mismatches produce the operation's typed refusal.

This closes the demonstrated stale publication and partial existing-state
modification window under the host's normal restoration mechanism. It does not
make that host mechanism preserve every fresh native path argument bit. Two
strict direct-H1 callback tests expose the remaining gap. Successful nominal
creation/dragging, pre-mutation refusals, identity and normal saved-state workflows
remain distinct demonstrated capabilities, not proof of this stronger guarantee.

## Stop and delivery consequence

No broad host checkpoint, native conic-point persistence change, upstream edit or
implicit canonicalization is introduced. No model is silently saved/reopened to
make a failed exact oracle pass. The implementation and candidate amendments
remain reviewable, but **the integrated candidate is not accepted**.

The new PHASE remains deliberately INCOMPLETE until a legitimate passing measured
catalogue exists. Frozen STATIC, PHASE, INFRA_UNIT and FINAL results must retain
their actual exits and causes. An incomplete profile or a failed JUnit producer
cannot be replaced with a legacy wrapper or a fabricated passing receipt.

Recommendation: **do not close CU7, including reduced closure, on this candidate**.
Resolve the exact native rollback input/correspondence contract under a separately
approved scope, or establish an explicit enforceable safe transaction perimeter;
then rerun the strict regressions and complete frozen acceptance, independent
review and author smoke. A stable saved h witness may be additional evidence,
but it cannot silently redefine the original live-state guarantee.

Main, the book, releases, M08-D, M09/I2, I2G and I3 remain outside this task.

## Author-authorized resumption — 2026-11-03

After reviewing the localized cause, the author explicitly authorized precise
changes in the inherited sources to persist and restore the native conic-point
argument. The stop above records the earlier scope and evidence; it is not an
instruction to abandon the newly authorized repair. The original failed receipts
and exact first-load oracles remain unchanged.

Before production edits, the candidate semantic/load amendment was recorded in
`docs/architecture/pre_g9b_cu7_native_conic_state_candidate.md`. The proposed
repair is additive shared native state for free `Point(full 2D conic)` and its
existing load lifecycle, with old untagged and explicit-parameter behavior kept
separate. Implementation and validation of that repair are pending at this entry.
Only later actual results can discharge the demonstrated blocker; authorization
itself does not discharge it or approve the integrated candidate.

## Demonstrated repair within the guarded perimeter

On clean code/test checkpoint `9d4f2a59ee00ec50ad085e13be9f2a59c275d80c`, the
canonical shared selection passes 152/152 (inner exit 0) and Desktop passes
110/110 (inner exit 0). Receipts are the external `focus-shared-native-05` and
`focus-desktop-native-01` producers. The original two H1 whole-XML rollback
oracles and fresh h parameter witness are unchanged and pass. The new native
test performs first-load and six-cycle exact comparisons without pre-normalizing.

This discharges the specific fresh-circle defect under the new codec. It does
not relabel the historical failures or certify all native documents. New
individual-forward/local-chart permissions and native Q publication refuse the
explicit unsupported host states before mutation; original R1/R6/H1 retain
their historical contracts. Coordinate-projected hyperbola caches, undefined
conic inactive matrices and other unproved native free places remain excluded
from the new strong perimeter. See the candidate amendment and delivery report.
Final frozen verification, independent review and author smoke remain separate.
