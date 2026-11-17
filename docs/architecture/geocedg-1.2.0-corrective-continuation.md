# GeoCeDG 1.2.0 — Corrective continuation after G0

Status: **G0 PLAN APPROVED — IMPLEMENTATION AUTHORIZED WITH LIMITS**.
The author approved G0 on 2026-11-17. This is a new continuation, not an
extension or reinterpretation of the thirteen closed historical rounds.

Protected S: `0ae033ff324d3d3102667718e6894dafbe33c89a`, tree
`c55e00a4caf8b68ca5811c67f8c0126ba1a7061c`. The successor branch is
`codex/geocedg-1.2.0-corrective`. Product identity remains 1.2.0 / 2026-11-16.

The approved G0 plan and 24-case matrix are preserved in the external evidence
directory `geocedg-120-g0-20261117`. The author authorizes at most six corrective
units, ordered B, A, C, D, E (C may split only for demonstrated independent
causes). Each requires a minimal patch, focal tests, independent review and
evidence. No full PHASE, INTEGRATION, FINAL or PACKAGING while blocking focal
failures remain. No promotion, tags or public distribution is authorized.

## Unit B — captured literal state omitted by the plane writer

Classification: debugging/implementation. Layer: Desktop IMAGE recovery MEMORY
snapshot. Source authority: `GeoCeDGUndoManager.LazyDefaults`, the native plane
writer, and the approved G0 extension to the R12 helper. No geometric definition,
FILE semantics, public command or global default changes.

The captured DEFAULT_PLANE3D prototype can own an admitted literal animation
step. Its native writer intentionally emits no animation XML. A full native
reload reconstructs a different prototype with a null step. The old archive
guard rejects that null before the image registry certificate runs. The saved
literal, not an assumed 0.1, is the recovery authority.

Minimal repair class: archive recovery only; exact DEFAULT_PLANE3D type and
GeoPlane3D class; current prototype belongs to the captured Construction and
differs from the captured prototype; captured non-null step was an admitted
owned constant; current raw step is null. Prevalidate every prototype/field
before the first setter. Reconstruct an owned constant from captured raw bits
and defined state. Never reinstall an old GeoNumeric, normalize a different
non-null value, repair another unproven type or bypass XML equality. Same-instance
restoration and the separate ordinary-Undo LazyNulls scope remain unchanged.

Validation: real native archive reconstruction for null, 0.1, custom literals,
signed zero and both admitted constant representations; strict negatives for
same-instance mutation and unexpected non-null changes; ensure all fields are
validated before writing. Retain exact defaults XML/V2, resources and JavaScript
checks. Run the affected recovery cases separately from known A registry defects.
Record each newly observed field before expanding this repair class.

Bootstrap impact: none. This uses the existing Desktop JDK/Gradle/toolchain and
native reader; no dependency, prerequisite or runtime boundary changes.
Verification infrastructure impact: only additional focal test identities;
official derived inventory update is deferred to D. GUIDE_IMPACT: no new user
surface; final status and recovery guarantees will be documented after evidence.
Focal success alone is not final acceptance: kernel/MEMORY integration still
requires the final profiles selected by the canonical registry on the final tree.

## Evidence and budget

New evidence belongs to `geocedg-120-corrective-20261117` outside tracked source.
Unit B is the first of six available units. A–E results and review identities
will be appended here only as they are actually established. Historical reports,
original cone and protected files remain untouched.

### B result

The initial diagnostic did not reach JUnit: three compiler workers exhausted
native memory at JVM startup (`malloc`, allocation.cpp:44, 1048576 bytes).
The retained hs_err logs establish an environment failure. Invocation-only
`--max-workers=1 --no-parallel` resolved compilation; no product budget changed.

`B-red-serial`: 10 cases, 6 passed and the 4 captured non-null literal cases
failed at the old `UNEXPECTED_LAZY_DEFAULT_CHANGE` guard. `B-green`: 23/23
passed, 0 errors/skips, inner exit 0. This includes 11 new cases (also NaN /
undefined state) and 12 directly affected recovery invocations from G0 outside
the three known A failures. The native archive reader, exact defaults/resource
oracles and real IMAGE recovery are exercised; this is focal evidence only.

Independent reviewer `/root/corrective_review` (GPT-6, High) reread the design,
patch, test assertions and diff; no blocking findings. The reviewer did not run
Gradle or GUI. Its two oracle recommendations (new MyDouble representation and
dynamic plane negatives) were incorporated before GREEN. The three A cases,
global acceptance and packaging remain pending. Source hashes and process/XML
evidence are preserved under the external continuation directory.

## Unit A — retire the current image generation before reconstruction

The instrumented five-scenario focus after B reproduces exactly three failures.
The two real-reader failures leave the first reader's image default registered
after the next reader replaces it. The late-attach failure leaves its two labelled
images registered after the native reader clears the construction sets. The
observed `isInConstructionList` flag on these stale instances is insufficient.
`Construction.clearConstruction` clears its tables without calling image.remove.

Retire images only while they are current, immediately before each guarded
explicit reader attempt. Traverse the current construction outputs, inputs and
list members by identity, including the inactive protocol suffix. For FILE
reload also retire the current image default, which the reader reconstructs.
Use the native lifecycle and retain the resource cache. Snapshot the generation
before iterating and skip members already removed by a cascade. Before removing
a labelled image require the current label-table entry to be that exact instance:
native removeLabel otherwise removes by string. Never call remove on a retired
instance after reconstruction.

The recovery certificate compares the complete current registry by identity with
the current document image graph, current default and precisely captured baseline
registry-only auxiliaries. Old document images/defaults cannot be reclassified as
those auxiliaries. Detect missing registrations, duplicates and extra registered
instances whether labelled or anonymous. Preserve the existing exact snapshot,
resource, script, layer, protocol and history certificates.

Scope: Desktop insertion journal, its tests and this new design. No change to
GeoImage, the kernel reader or the historical source/evidence. Validate the three
original failures plus adjacent commit/store cases, baseline images sharing one
resource (including an inactive protocol suffix), anonymous partial creation,
normal image insertion, Undo/Redo and persistence. A baseline registry-only entry
is preserved as captured state; this is not a general cache/garbage-collection
feature. Independent design review found no blocking contradiction and explicitly
requires the removal identity and cascade guards above.

The added baseline-image adversary also demonstrates the same missing lifecycle
at the internal NativeAtomic rollback: that reader clears tables before explicit
recovery starts. A-registry-isolation passes the late-attach and both precise
registry-negative cases but rejects the inside-mutation baseline-image variant.
Retire the current document image generation inside the mutation catch, before
NativeAtomic replays its construction-only XML; retain the defaults in that route.
The explicit FILE route still retires its current default as well. This closes
the demonstrated second entry to the same generation-replacement mechanism,
without changing the kernel reader or removing stale objects after replay.

### A focal evidence

The intermediate records remain failed: A-diagnosis 5/2 pass/3 fail;
A-green 69/66 pass/3 new-test oracle failures; A-closure 16/14 pass/2 fail;
A-orphan-cause 2/0 pass/2 fail; A-registry-isolation 4/3 pass/1 fail. The
first new shared-image oracle incorrectly used the active label table for an
inactive suffix; the isolation oracle initially observed state after scripted
CLOSE. The first synthetic orphan also failed a prior exact certificate
condition (`certificate: []`), so it did not prove the registry predicate.
The revised adversary preserves a valid shared resource and proves that exact
registry cause. The remaining inside-mutation failure then demonstrates the
second generation-replacement entry described above. No historical test or
acceptance result is reclassified as passed.

A-final-focal: **84/84 pass**, 0 errors/skips, inner exit 0 (Recovery 71 and
Insertion 13). Product tests cover both replacement entries, baseline shared
resources/inactive steps, dependent-image removal cascades, anonymous and
labelled orphan refusals, the original exceptional cases, B, normal acquisition,
Undo/Redo, save and reopen. Independent source/evidence review is recorded
externally. This is one causal unit A (image-generation lifecycle and its
certificate), not new functionality or a kernel reader change. Global gates and
C–E remain pending.
