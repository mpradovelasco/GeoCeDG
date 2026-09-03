# Compatibility prerequisites discovered by FULL verification

Status: **IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW**. This is a bounded
correctness prerequisite to the operational verification-performance task, not
a new product phase, phase approval or performance optimization. The shared
and Desktop repairs are applied. Their focused 85-case shared and 34-case Desktop
runs passed, followed by technical FULL02: 6,417 shared + 1,179 Desktop cases,
7,585 PASS and 11 retained SKIP, zero failures/errors, with the original phase/
reference assertions and four Checkstyles. This covers the recorded 60-path
checkpoint, not later documentation or benchmark changes. Clean-output FULL
subsequently passed on that same source60 with matching saved coverage/context
and six byte-identical canonical summaries. Bootstrap03 subsequently completed
its normal path and nested COMPOSED on source61; its separate timestamp-link
envelope failed instrumentally and remains preserved. Final CI-profile01 later
failed at operational benchmark precheck on a fixed-default evidence collision,
not these Java contracts; its root remains FAILED. Replacement final02 validation
remains pending at this documentary checkpoint; no author acceptance
or promotion is inferred.

## Evidence before any source optimization

The unchanged canonical checkout at
`3942af594e4507e479f2c75019cef62e3d9fea6f` passed its original bootstrap/composed
path, but `tools/agent/verify.ps1 -FullTests -KeepBuildOutputs` failed in the
unfiltered shared-JRE suite: 6,408 tests, six failures and ten skips. Unfiltered
Desktop did not execute. The original FULL elapsed time was 5,594.184 seconds
(including 7.053 seconds of external evidence archival). Its failed logs and
archived XML are retained under
`artifacts/verification-performance-bootstrap/baseline/full-run1/`.

Five failures concern generic upstream command tests that do not distinguish
the default-off V2 creation gate from command registration or argument
validation. The sixth is an ordinary spreadsheet paste/redefinition failure.
The latter reproduced alone on the same unchanged source, with one selected
method, one failure and the same stack:

`KernelCellDragPasteHandlerTest.testDragPasteShouldResultInNonEmptySpreadsheetCells2`
→ `Construction.processCollectedRedefineCalls`
→ `SpatialIdentityRegistry.completeRedefineHostOperations`.

The isolated diagnostic is archived under
`artifacts/verification-performance-bootstrap/experiments/original-full-spreadsheet-isolated-repro/`.
Its runner returned 1 and its Gradle stream reports BUILD FAILED. The runner's
recorded numeric native exit of 0 is an instrument scope-capture defect, not
product success; the original evidence is preserved without rewriting it.

## Classification, authority and minimal scope

This prerequisite is implementation/debugging in the shared Java integration
and test layers. It is not a Conda, permission, JDK, numerical-reference or
performance failure. The user's operational task explicitly permits necessary
product corrections in its upstream-minimality exception. The governing
contracts remain:

- [Spatial/projection semantics](../../geocedg/specs/spatial/g9-spatial-projection-semantics.md):
  unassociated legacy geometry remains usable; association is explicit.
- [ADR 0013](../adr/0013-public-locus-v2-surface-and-token-selection.md):
  experimental V2 creation is default-off and enforced by the runtime gate.
- [ADR 0018](../adr/0018-semantic-spline-2d-capability.md): SplineV2 reuses that
  opt-in; Classic Spline and non-V2 behavior remain unchanged.
- [R6 interaction contract](../../geocedg/specs/locus/locus-v2-point-interaction.md):
  existing host-wide atomic mutation and rollback behavior remains unchanged.

The source of truth is current Java code/tests and these accepted contracts.
Raw logs, XML and this report are evidence, not replacements for those sources.
No historical evidence manifest or approved phase record needs a hash refresh:
G9A1/A2/A3 inventories and R6 tagged-descendant source hashes bind their approved
commits. The empty-index requirement remains applicable during current tests.

## Ordinary collected redefinitions

`startCollectingRedefineCalls` initializes an empty spatial transaction map.
An unassociated target has no spatial transaction and is queued only in the
ordinary redefine map. The previous post-rebuild condition checked solely that
the spatial map was non-null, then called the registry with an empty collection.
The registry correctly rejects an empty spatial completion batch.

The minimal correction bases completion on the spatial publication contexts
captured before the rebuild. An ordinary batch has none, so there is no spatial
host operation to complete. A participating batch still follows its original
commit/completion, lease, rollback and failure paths. Using the captured context
set, rather than merely accepting any currently empty map, retains failure
detection if a participating batch were to lose all its completion contexts
after capture. This guard does not add a new partial-context-loss invariant.

Preserved contracts:

- The construction dependency graph still owns replacement and recomputation.
- No identity is inferred or allocated for an unassociated ordinary geo.
- Participating spatial IDs, revisions, publication leases and transaction
  completion remain authoritative and fail-closed.
- Empty registry completion is still an error; it is not converted to a no-op.
- No public API, serialized format, projection rule, metric, tolerance, solver,
  parameter domain or exact/approximate classification changes.

New GeoCeDG regression coverage checks two successive ordinary batches with a
dependent value and continued absence of spatial identity, plus direct rejection
of empty spatial completion. The original spreadsheet regression is retained.
Existing G9A1 and G9A3 retained-batch/rollback/transaction tests remain required.

## Command availability and coverage

Retain every original failing test. Explicitly distinguish Classic-OFF from an
opted-in GeoCeDG application in `CommandDispatcherTest`, `CommandFilterTest`
and `CommandsValidationTest`. OFF checks must verify denied creation; ON checks
must still exercise registration, no-CAS filtering and argument validation.
Do not exclude the three V2 commands or turn their production gate on globally.

`SelfTest` is a reflective command-coverage inventory, not a command evaluator.
Extend its discovered test-class list with genuine executable GeoCeDG
`cmdLocusV2`, `cmdLocusLength` and `cmdSplineV2` tests. Their names alone are not
evidence; the new tests must run successfully in focused and exhaustive suites.
The `org.geocedg.*` location also includes them in the implemented composed union.

## Desktop tool-resource prerequisite exposed after shared FULL passed

The first candidate FULL reached unfiltered Desktop and failed only
`ResourceAvailabilityTest.checkToolIcons`: 1,171 XML cases, one failure and one
pre-existing skip. Its four missing resources are the PNG paths synthesized
for modes `LocusV2`, `LocusV2.Point`, `LocusLength.Total` and
`LocusLength.Partial`. The 252 in the assertion is the missing-path string
length, not a count of failures. The isolated unchanged test class reproduces
the failure in `after/dev-desktop-resource-repro-01/` under the performance
artifact root. Original FULL never reached unfiltered Desktop, so this is not
an executed original-Desktop baseline or a performance comparison.

This is a Desktop resource-integration defect, not geometric or licensing
semantics. `ImageManagerD.getToolImageResource` assumes every mode has a PNG;
the four modes instead have a GeoCeDG-owned SVG already recorded in
`geocedg/resources/assets-manifest.yml`. Merely returning that SVG path is not
enough: the inherited Toolkit image loader does not decode SVG. The actual
toolbar and `GuiManagerD.getToolImageURL`/`ToolImage` consumers must receive a
loaded raster, not just a resource URL that satisfies the inventory test.

Authority is section 15 of
[the public Locus V2 contract](../../geocedg/specs/locus/locus-v2-public-surface.md)
and the existing owned asset. The
[R3 UI hardening contract](g9u0_r3_public_locus_ui_hardening.md) retains the
text-based menu and does not authorize a new default toolbar or stable-feature
promotion. No resource artwork, rights metadata, localization, menu, feature
flag, kernel object, serialization, metric or numerical tolerance changes.

The smallest coherent change is one GeoCeDG-owned `ImageResourceD` adapter,
two closed integration seams in `ImageManagerD`, a new GeoCeDG Desktop test
class and this living provenance/design record:

1. Preserve the existing lowercase normalization. Match only the four exact
   mode names; all other names retain their existing p32/p64 PNG paths.
2. Return the existing `mode_locusv2.svg` resource identifier for those modes.
   Decode it with the existing Desktop `JSVGIcon` implementation, with a
   private Graphics object, into a fresh 64-by-64 ARGB raster. No dependency,
   generic SVG-loader extension or build-time generator is introduced.
3. Expose a fully loaded Toolkit image from that raster through the existing
   public `getImageResource(ImageResourceD)` seam. This intentionally preserves
   the inherited image type behavior: `addBorder` fills a buffered destination
   before drawing its input; passing the same BufferedImage as both would erase
   the icon when a white background is requested. No general border rewrite
   or shared mutable raster is needed.
4. Leave cache ownership, `ImageManager3D` delegation and responsive scaling
   unchanged. A fixed 64-pixel raster avoids filename-only cache staleness when
   maximum size changes. As with existing p64 PNGs, ratio 2 does not promise
   more than 64 physical pixels or 32 logical pixels from that raster.

Required new coverage: all four exact modes and real resource URLs; real 2D
and 3D decoding with the asset's blue/red pixels (not merely dimensions or a
black parser fallback); bordered and raw image independence; existing cache
reuse; 32/64 scaling and public pixel-ratio transitions 1 to 2 to 1; toolbar
and ToolImage image creation; unchanged legacy and unmatched-name paths. Only
the GraphicsConfiguration transform may use the already available Mockito
test dependency; rendering and application consumers remain real/headless.
`ResourceAvailabilityTest` remains byte-for-byte unchanged, with all four
original assertions. Existing R3 localization/menu regressions, Desktop
Checkstyle, COMPOSED and unfiltered FULL remain required.

## Rejected alternatives

- Suppressing any of the six failures, skipping their tests, changing the
  scientific baseline or treating FULL failure as a warning.
- Catching the spatial exception in spreadsheet code or weakening the registry
  contract to accept an empty completion collection.
- Enabling V2 globally, changing feature-disabled errors into argument errors,
  or adding command exclusions to the coverage inventory.
- Rewriting approved phase hashes, source manifests, tags or approval records.
- Removing the new modes from the resource inventory, mapping them to the
  inherited legacy Locus icon, or accepting a null/generic toolbar fallback.
- Adding eight p32/p64 binary derivatives and a generator when the bounded
  Desktop adapter can use the already owned, registered source SVG.
- Globally changing Toolkit SVG support, border behavior, cache keys, DPI
  policy or the application menu/toolbar composition for this resource fix.

## Validation and performance interpretation

Required fresh checks are the affected command classes and new GeoCeDG command
tests; the complete spreadsheet drag/paste class; the new ordinary batch tests;
G9A1/G9A3 redefine host and transaction regressions; shared Checkstyle; and the
current-source COMPOSED/FULL gates. A failure must remain a failure at every
wrapper boundary. Record exact commands, native/wrapper codes and archived XML.

This repair is separate from execution consolidation. All old tests stay and
new coverage is additive. Preserve the original failed FULL measurement as a
failed baseline, not a successful before/after pair. No percentage improvement
may compare its incomplete scope with a later successful exhaustive run.
Any performance comparison that includes this repair must disclose the source
and test-scope difference; matched original-source Gradle controls remain the
direct evidence for incremental/cache/process choices.

### Saved applied evidence and remaining boundary

- Shared DEV focus: `artifacts/vp-java-focus-01/verification-result.json`, exit0,
  85 tests in 11 selected classes, zero failures/errors/skips. The ordinary
  collected-batch, original spreadsheet and participating-spatial regressions
  remain present. This focus did not itself run Checkstyle or global gates.
- Desktop DEV focus: `artifacts/vp-desktop-resource-focus-01/verification-result.json`,
  exit0, 34 tests = 8 additive adapter + 4 unchanged resource-inventory +
  16 inspector + 6 menu cases, zero failures/errors/skips.
- FULL02: `artifacts/verification-performance-bootstrap/after/full-applied-02/`,
  final native/measurement/root/environment exit0; 7,596 unfiltered cases and
  the complete composed assertion chain. Root SHA-256
  `cf853c675af567f2c634e19b4f987bb26c1290848cf1acdc1ccc4424d13ff57a`.
  Its intermediate receipt still records phase assertions pending; that is not
  the final completion authority.
- The saved v2 OriginalFullShared comparison has zero unexplained differences:
  exactly the six authorized FAILED→PASSED transitions and nine additive shared
  cases, preserving ten shared skips and 19 legitimate duplicate raw-display
  occurrences. Summary SHA-256
  `03ff7948aa57d3cc6cf65348f96fbaba743d36fabbb501b54a2bd0612417c91e`.
  No original FULL Desktop baseline exists. The [performance report](../validation/verification_performance_report.md)
  retains exact run/analysis paths, hashes, times and scope differences.

The representative DEV49 pair independently confirms bounded repeated fresh-test
identity/outcome/context evidence; it is not extra execution of the correctness
focuses or universal numerical determinism. The clean-output FULL run completed
with 7,596 cases / 11 retained skips, all four Checkstyles executed and 23 generated
paths cleared/reported restored; its profile differs from retained-output FULL02,
so the matching archived coverage is not a timing comparison or CandidateRepeat.
The first two candidate bootstraps failed in the new operational Conda identity
and native-output parameter binding, before Java compilation or product
verification. Their corrected 150- and 158-assertion workstation focuses are
separate from these Java repair results. Bootstrap03 then completed the actual
normal path and nested COMPOSED with 1,096 cases and no failures/skips; six saved
canonical summaries are byte-identical to FULL02. Its original r3 linked envelope
remains exit 2 because of mixed local/UTC DateTime comparison. Separate archived
reconciliation passed with the original phase union plus 16 named additions and
no differences, retaining that failure and identifying the nested-result digest
as a post-run review pin, not a manufactured historical link. Final CI-profile01
then failed at delegated operational benchmark precheck: generated-state correctly
rejected an old summary in the fixed implicit log directory. Native/measurement/
root exit 1 and no benchmark JSON remain preserved; its receipt is not FULL PASS.
The [bounded operational correction](../../artifacts/verification-performance-bootstrap/inventory/final01-benchmark-evidence-collision-design.md)
uses invocation-unique default evidence paths, preserves explicit-path/no-overwrite
behavior and adds one operational regression. Its focused 111 runtime cases plus
18 generated-state cases/143 assertions passed, fake-first operational evidence
only, as recorded in the linked reports. It changes no Java, geometry, resource, persistence or reference
contract described here. Replacement final02 remains unexecuted at this documentary
checkpoint; its source evolution from bootstrap03 is seven existing paths (five
documents, operational entrypoint and runtime fixture), not five and not a new
membership beyond 61. A local CI-profile FULL is not a remote workflow
run, and no duplicate successful standalone benchmark or second bootstrap is
required merely to repeat that work. Normal bootstrap uses explicit log paths,
so the default-only change does not invalidate its recorded earlier cohort or
require a new bootstrap behavior. The reports preserve final01 measurement/root
and TEMP archive evidence; the unexecuted real-run success-only auditor21ce is
not an authority for that failed run.

### Documentary checkpoint and final execution record

This source is frozen before replacement exact-source FULL attempt
`final-full-ci-profile-02`; its outcome was not yet executed at this documentary
checkpoint and no exit 0 is predicted. Attempt `final-full-ci-profile-01` remains
FAILED at the delegated benchmark precheck; an intermediate receipt does not
replace its failed root. Later completion is recorded in the designated
[measurement](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/measurement-result.json),
[final root result](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/verification-result.json),
[delegated benchmark JSON](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/operational-benchmark.json)
and [final candidate closeout](../../artifacts/verification-performance-bootstrap/inventory/final-candidate-closeout.md),
not by rewriting the tested source after execution. A missing or failed record
does not satisfy the gate. These ignored local artifacts require the retained
artifact tree or a separately supplied bundle; they do not confer author approval.

Update only the living upstream-modification inventory for modified upstream
files, referencing this design. Current technical validation is not author
approval, and does not advance the spatial roadmap.
