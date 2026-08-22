# G9U0-R2 product-refinement implementation map

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Entry: `ce022b756b51fe12497e1932ba3ae58093dd1405`
- Branch: `feature/g9u0-r2-product-refinement`
- Planning authority: `geocedg-g9u0-r2-planning-pass`
- Correction validation: **AUTOMATED COMPLETE** — replacement focused A/B,
  historical, ancillary and full composed evidence is clean; manual author
  smoke remains pending
- Manual author smoke: **PENDING AUTHOR**

This document maps the bounded implementation to the accepted ADR and specs.
It does not claim implementation PASS, author approval, release readiness or
authorization of G9U1.

## 1. Placement and preserved authority

```text
GeoElement visual style --------------------> GeoLocusV2 presentation capability
semantic Locus V2 --------------------------> unchanged source/revision/results
LocusRenderCache2D -------------------------> derived view tessellation

GeoCeDG Desktop document policy ------------> .cedg/.ggb lifecycle routing
existing GFileHandler/MyXMLio ZIP/XML ------> unchanged content machinery
Windows package profile --------------------> installer-only .cedg association
```

The implementation adds no generic `Path` conformance, parallel style store,
semantic render cache, archive dialect, XML app code, filename-derived
geometric identity or compatibility downgrade. Workspace/profile v2 remains a
later client.

## 2. Locus V2 presentation seam

`GeoLocusV2` declares the existing line-properties capability directly while
remaining a non-`Path` `GeoElement`. Its supported line style is written by the
ordinary `GeoElement` XML style contract. Presentation refresh is contained at
the drawable/repaint boundary so color, thickness, line type, visibility,
labels and transient selection/highlight do not publish a new Locus semantic
definition or revision.

The narrow host seam is `GeoElement.updatePresentationRepaint()`: its default
delegates to the historical `updateRepaint()` cascade, so ordinary objects do
not change behavior. Existing Properties setters for thickness and applicable
caption/label presentation call that hook; only `GeoLocusV2` narrows it to
refresh itself and request repaint without cascading semantic dependents. This
is an overridable presentation notification, not a style store or geometric
revision authority.

`DrawLocusV2` continues to derive its path only from
`LocusRenderCache2D`. Applicable label placement is selected from derived
render vertices and does not feed back into the semantic object or subpath
topology. An unrelated line, circle or conic never becomes a render-cache
input; fixed-policy topology tests compare vertices and `startsSubpath`
markers before and after crossings. Separate semantic components remain
separate.

Focused authorities:

- `G9U0R2LocusPresentationTest`: `R2-L01`–`L08`, `L13`–`L15`;
- `G9U0R2LocusRenderContinuityTest`: `R2-L09`–`L12`.

## 3. Native document state machine

`FileExtensions.GEOCEDG` is the shared suffix token. The normal GeoCeDG
Desktop route owns a small policy adapter rather than changing serialization:

```text
new/unsaved       --Save/Save As--> lowercase .cedg
opened .cedg      --Save----------> same .cedg
opened .ggb       --Save----------> distinct .cedg through native Save As
cancel/failure    ----------------> source bytes and live construction retained
```

Open filters, omitted-extension fallback, drag/drop and direct/multi-window
routes accept native `.cedg` and compatibility `.ggb`. Successful native Save
As publishes the new path only after the write succeeds. A compatibility
source is not an ordinary save destination.

The implementation uses two narrow Desktop I/O helpers:

- `DocumentArchivePreflight` validates immutable candidate bytes in a
  disposable application before live publication, so corrupt native input can
  fail closed;
- `AtomicDocumentFileWriter` prepares a sibling temporary file and promotes it
  only after the writer completes, preserving the prior complete target on a
  failed preparation.

The native preflight currently materializes the complete local or remote
archive byte array without an explicit size cap. It rejects corrupt,
truncated and structurally invalid archives before live mutation. For a
preflight-admitted archive, the native-only no-commit reader
`GFileHandler.loadPreflightedNativeXML` separates parsing from path,
saved-state and undo publication.
`AppD` snapshots the prior construction archive plus current file/path, saved,
recent-files and loading state. `UndoManagerD.prepareUndoBaseline()` serializes
a disposable replacement without changing history; only after the live parse
and fallible publication steps succeed does `commitUndoBaseline()` atomically
replace undo/redo history. An injected parse or pre-commit failure restores the
snapshot and leaves the old history authoritative. Prepared-baseline cleanup is
nonthrowing, and a history generation discards stale asynchronous stores.
Rollback failure raises `NativeDocumentRollbackException` instead of continuing
with partial state. Automated correction validation, including composed
verification, is complete. The no-size-cap point remains a P2
implementation risk, not new format semantics or authority to expand R2.

These helpers do not define a new format. The current ZIP entries,
`geogebra.xml`, semantic versions and `app="classic"` remain authoritative.
The extension classifies an I/O route only.

The separate fork Classic route recognizes `.cedg` and preserves an opened
native path through ordinary Save As selection. Its config, preference
namespace, default new-document `.ggb` identity and creation policy remain
separate. This is not a claim that external upstream GeoGebra supports
GeoCeDG-native types.

Focused authority:

- `GeoCeDGDocumentLifecycleTest`: `R2-D01`–`D16`;
- `G9U0PersistenceCompatibilityTest` provides the exact Locus generator,
  rich-result/token, corrupt-input, Classic and no-downgrade round trips used
  by `D09`–`D11`, `D14` and `D15`;
- `G9A3SpatialNativeCompatibilityTest` and
  `G9A3SpatialCompatibilityXmlTest` provide the durable identity, atomic
  rejection, Classic and external-boundary authorities used by `D09`, `D12`,
  `D14` and `D15`; and
- the full spatial/frame/system/map/relation/binding corpus remains the
  historical `R2-R05` G9A authority. A Desktop suffix-routing assertion alone
  is not treated as proof of semantic persistence.

## 4. Windows association

The Windows package profile now declares `.cedg` only for MSI/EXE output. The
portable app-image and ZIP remain association-free and GeoCeDG does not claim
`.ggb`. JDK 25 `jpackage` requires a MIME field for its file-association input;
the implementation therefore records the narrow internal, unregistered
GeoCeDG-owned value `application/x-geocedg-cedg`. It does not reuse the
upstream GeoGebra MIME identity or make that value a cross-platform document
semantic.

`tools/agent/verify-packaging.ps1` owns `R2-D17`. Static profile validation is
always required. Registry/MSI inspection is claimed only when package
artifacts are explicitly supplied; public redistribution remains blocked by
the existing licensing/assets gate.

## 5. Operational integration

The single existing verification architecture is extended by:

- `geocedg/validation/g9u0-r2/g9u0-r2-product-refinement-scenarios.json` — all
  39 `R2-L*`, `R2-D*` and `R2-R*` rows;
- `tools/agent/verify-g9u0-r2-product-refinement.ps1` — planning-authority,
  exact-inventory, source-boundary, focused JUnit, packaging, deterministic
  summary, historical-regression and evidence checks;
- the paired R2 block in `tools/agent/verify.ps1`, after G9X1 and before the
  standalone packaging and any future G9U1 block;
- candidate evidence and its canonical-LF hash manifest; and
- the immutable `.ggb` compatibility corpus and its hash manifest.

The focused verifier writes generated logs and a canonical summary only under
the requested generated artifact root. Two focused runs compare the summary,
not ZIP timestamps. `R2-R01`–`R06` must be executed and recorded first; both
identical focused passes then use `-HistoricalRegressionsAlreadyRecorded` so
the regression suite is not interleaved with the deterministic comparison.
The summary carries the complete candidate path inventory but hashes only the
stable implementation/test/package/tool subset. Mutable closeout documents,
scenario execution status, evidence JSON and its hash manifest are named as
excluded-from-hash inputs; this prevents recording a summary hash in evidence
from changing the summary it describes. Scenario IDs, test counts and
normalized corpus archive-entry/XML hashes remain inside the comparison.
The composed verifier rejects partial artifact presence,
open/unsealed evidence and any frozen evidence without focused,
deterministic, regression, Checkstyle, diff and packaging records.

## 6. Upstream impact

The exact candidate inventory is `FROZEN` against the entry commit at 49
paths: 21 productive, 3 focused-test, 5 validation, 12 documentation, 4
packaging and 4 operational paths. All 24 changed `source/` paths are
registered exactly once in `docs/upstream/modified-files.yml` against
Accepted ADR 0016 or one of the two normative R2 specs. The boundary is
limited to the ordinary Locus presentation capability, Desktop file
classification/open/save transaction seams and focused tests. No serializer,
kernel metric/intersection, legacy Locus, web, 3D semantic, G9U1 or later
productive path is in scope.

## 7. Remaining author closeout

The prepared undo-baseline correction invalidated every prior automated tuple.
Replacement focused A/B, historical regressions, packaging/static validation,
relevant Checkstyle, both Git diff checks and full composed verification are
clean and recorded. The automated candidate is technically ready for review.
The manual author smoke and author disposition remain exclusively author-owned.
The report therefore retains:

```text
selfApproved = false
authorApproved = false
passClaimed = false
manualAuthorSmoke = PENDING_AUTHOR

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

Only the author can execute and accept the manual smoke or close the
implementation as PASS. G9U1 remains separately unauthorized.
