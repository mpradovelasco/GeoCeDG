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

## Unit C — diagnose before selecting any productive correction

Only test observability is initially changed: retain and print the complete
exception cause/suppressed tree of each of the four publication rechecks, and
record the dragged point's passive resolution cause, retained/current address,
binding and defining XML. Do not recompute, initialize views or change adversaries.
All five C production paths remain Git-identical to S after B/A.

Compare the same five scenarios with 4fc using its preserved exact runtime and
test sources read from that Git commit. Verify the frozen classpath's file/member
hashes and report class origins; do not relabel the advanced S clone as 4fc or
copy protected files. Diagnostic compilation/output belongs to a new external
directory and uses the canonical neutral Gradle producer. No baseline source,
historical classpath or original evidence is changed.

No productive C patch is selected yet. An observed cause must justify its exact
scope; a new normative permission/relaxation stops the affected part for author
decision. C is the third unit started, with at most six overall and a possible
split only for demonstrated independent causes. No global gate is authorized
while these blocking interaction failures remain.

### C causal reconciliation and minimal origin correction

The preserved 4fc runtime passes all five original scenarios; the successor
reproduces all five failures. The four publication traces identify
UNSUPPORTED_VIEW_CLASS for view 1. The controller trace also links the drag's
interrupted recovery to that same exception, followed by INVALID_SOURCE and a
null current address. A second independent semantic cause is not established.

The exact captured view is 800x600 with live/settings (400,300,100,100) and
view bounds [-4,4] x [-3,3]. Its kernel cache is [-4,-4] x [-3,3], scales
(100,100); the native reader restores xmax=4. The strict R10 guard correctly
rejects that changed out-of-class cache. No new cache-repair class is proposed.

Origin: `Kernel.prolongGraphicsBoundArrays` copies `xmin` into the enlarged
`xmax` array. That line is identical in 4fc and S. Adding EV2 therefore corrupts
the existing first view's maximum. This is a preexisting storage-growth defect;
R10's rejection and the five broken consumer scenarios are the later integration
regression. Fix the origin by prolonging the original xmax array instead.
Kernel.java is the one additional productive file justified by this demonstrated
cause. This restores the existing cache-preservation invariant, not a normative
amendment, fallback or tolerance. The independent reviewer concurs with that
scope. NativeTransactionalState2D and the controller's temporary trace are
restored unchanged before the patch is verified.

Validation: a direct shared-kernel regression adds view 2 and a farther slot,
requiring exact preservation of all six prior cache values and exact new values;
then the five original R3 scenarios, full R3 adversaries and R10 consumers.
Keep exact R10 guards, ordinary single-DAG evaluation, FILE and identity intact.
No bootstrap prerequisite changes. GUIDE_IMPACT: recovery bug fix only; final
guide/status coverage follows the verified result. FINAL remains required for
the final kernel/persistence candidate; this unit uses focal checks only.

### C result

C-growth-red: both direct view-growth cases fail at the xmax bits (expected 4,
observed -4). After the one-line origin fix, C-green Desktop is 46/46 pass,
including all 24 R3 adversaries and R10/Undo consumers. The two-task diagnostic
used a shared XML directory, which the later Desktop task cleaned; no shared
JUnit count is inferred from that missing output. C-shared-green separately
retains 18/18 pass, including both direct growth cases; both producer exits are 0,
with no errors/skips. All temporary productive-file diagnostics are removed.

Independent review verifies the source pins and saved XML/logs, agrees with one
causal C unit and unchanged R10 authority. This restores the five consumer
regressions while fixing the inherited array-copy defect. D/E and global
acceptance remain pending. Three of six authorized units have been used; no C
split or extra normative authorization is necessary.

## Unit D — reconcile authorized action and guide expectations

Classification: verification/conformance and test maintenance. The live profile
already contains 137 actions; the only difference from the executable authorized
set is the author-approved `document.insert-clipboard-image`. Add precisely that
ID to the existing amendment mechanism, without copying its runtime definition
or admitting any other ID. Test missing/renamed action rejection explicitly.

The approved compositional section 6.8 adds one H3 in each guide. Update only the
stale expectations to 102 H3 / 119 outline entries, retaining H1/H2 counts,
bilingual vectors, anchors, stable markers and content assertions. The toolbar
test must expect 137 and explicitly find the approved clipboard action; its
existing grammar and conservative-mode assertions remain.

The derived JUnit inventory and registry pin are not hand-edited. Use the official
updater with authenticated discovery and complete successful selection evidence.
Do not reclassify the failed S projections or issue an acceptance receipt for S.
Validation is the four original failing methods (and their surrounding classes),
workspace-profile script tests, and the real workspace product leaf. Official
derivation and global evidence are separate later steps. No geometry, FILE,
prerequisite, runtime, profile action implementation or guide capability changes.

### D focal result and remaining derivation

D-desktop-focal: 56/56 pass in the four affected/conformance classes, inner exit
0, no errors/skips. D-infrastructure-native: 43/43 pass and the real workspace
product check satisfies both live-authority and packaged-resource contracts.
The restricted first attempt could not create a Git MSYS namespace object
(0xC0000022); managed escalation resolved that environment/permission failure
without a product change.

Independent review finds the exact approved-ID amendment and the retained guide
assertions coherent. D is **FOCAL PASSED — OFFICIAL INVENTORY DERIVATION PENDING**.
Both feature selections and broad FINAL selection counts require authentic
successful execution evidence; discovery alone cannot certify them. Complete
that official derivation after E focal blockers are removed, before freezing
the final tree and starting global acceptance. No inventory/hash is fabricated
and no failed S result is promoted. The source correction of D precedes E;
the pending derived catalog remains a separately evidenced part of unit D.

## Unit E — canonical Gradle project and short packaging paths

Classification: build orchestration/provenance and environment diagnosis. Confirm
the source-selection mechanism before changing code: run the actual builder from
a new foreign Gradle project, with the real absolute builder/wrapper, explicit
approved JDK25 and a new INTERNAL AppImage-only output. The old builder must not
be treated as using its own project merely because its wrapper path is absolute.
Retain the child command, cwd, logs and exit through the canonical child host.

If confirmed, pass `--project-dir $RepositoryRoot` at all three Gradle invocation
sites (toolchain discovery, installDist/export and reuse/export). Do not change
the exporter, hash join, runtime inputs, package identity or contracts. Verify
that resolved metadata, installDist and staged bytes have the same producer.

Use a short initially absent directory under the existing approved repository
artifacts boundary; do not expand that boundary or mutate S's nested checkout.
The current root is shorter than the historical nested packaging clone. A native
MSI-only focus will characterize WiX with unchanged pinned tools/licenses. It is
not All/PACKAGING acceptance, public distribution or proof that MAX_PATH was the
exact native cause of the historical pipe failure. Final INTERNAL/NC packages
and global acceptance remain gated on all focal blockers and official D derivation.

### E source-selection evidence

E-project-red invokes the real absolute builder from foreign-project and records
exit 1: Gradle reports root `geocedg-cwd-probe-host` when the requested Desktop
export task exists only in the intended repository. This demonstrates the
cwd-selection defect before any app-image is produced. E-invocation-red tests
the three actual source argument arrays/native helper: 0/3 pass, all selecting
the foreign root. With explicit project-dir at the three call sites,
E-invocation-green is 3/3 pass, including an intended path containing spaces
and unchanged cwd during/after invocation. The exporter, joins, hash values,
output-boundary contract and product metadata are unchanged.

Independent source review finds no blocking issue. Commit this source stage
before the real short-path MSI focus so embedded provenance refers to a clean
actual commit; that focus and final acceptance are still pending at this stage.
The lightweight fake regression is a supplemental focal script, not a new or
relaxed verification contract. It introduces no runtime/bootstrap prerequisite.
