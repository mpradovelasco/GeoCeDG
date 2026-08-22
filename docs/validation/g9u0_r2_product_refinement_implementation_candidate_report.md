# G9U0-R2 product-refinement implementation candidate report

## Disposition

```text
G9U0-R2 = IMPLEMENTATION CANDIDATE — AUTHOR SMOKE FOUND RENDER-CONTINUITY DEFECT
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

- Entry/base: `ce022b756b51fe12497e1932ba3ae58093dd1405`
- Branch: `feature/g9u0-r2-product-refinement`
- Planning authority: annotated `geocedg-g9u0-r2-planning-pass`
- Source boundary: **FROZEN** against the entry commit; 49 exact candidate
  paths, including all 24 changed `source/` paths registered upstream
- Focused automated validation: **PASSED — 31 R2 + 31 support JUnit**
- Deterministic focused rerun: **PASSED — canonical SHA match**
- Historical regressions: **PASSED — R2-R01 through R2-R06 saved**
- Static scaffold, packaging, Checkstyle and Git diff checks: **PASSED**
- Full composed verification: **PASSED — exit 0; durable outer log saved**
- Manual author smoke: **EXECUTED BY AUTHOR — RENDER CONTINUITY FAILED**

This remains an implementation-candidate report, not a phase PASS claim. The
prepared undo-baseline correction invalidated every prior automated tuple.
Its replacement focused A/B, historical, static, packaging, Checkstyle and
diff records now carry exact commands, counts, exit codes, logs and matching
canonical summaries. Full composed verification also exited 0 with one global
success terminal, one R2 candidate terminal and one wrapper exit marker in its
durable outer log. The earlier `PENDING correction rerun` reset label is
therefore superseded by this automated-complete candidate state.
The author executed the manual smoke. Ordinary styles, Properties, copy,
undo/redo, native `.cedg`, non-destructive `.ggb` transition, corrupt-native
fail-closed behavior and GeoCeDG Classic preservation passed. The R2-L11
render-continuity requirement failed, so the candidate remains unapproved and
requires a bounded correction followed by author re-smoke.

## Entry authority

The implementation began only after the author separately invoked the exact
canonical R2 prompt. Entry verification established the clean planning commit,
the annotated planning tag and its ancestry, Accepted ADR 0016, both normative
R2 specs, the unchanged prompt/spec/ADR blobs and the required historical
G9U0/G9U0-R1/G9X1/G9A/G5/legacy authorities.

## Characterization and implementation

The existing Locus V2 drawable already consumed ordinary color and stroke and
its render cache already derived subpaths from semantic components. The narrow
missing seams were line-Properties applicability, ordinary line-style XML and
applicable label drawing. The candidate supplies those seams without `Path`
or a second style model and adds fixed-policy crossing regressions.

The existing ZIP/XML reader and writer were already suffix-independent. The
unsafe product boundary was Desktop routing: `.ggb` was the only document
extension and normal Save could target an opened compatibility file. The
candidate centralizes `.cedg` classification, native Save As, compatibility
source protection, corrupt-load preflight and complete-target publication
without changing `app="classic"` or archive content.

The correction closes the remaining preflight-admitted live-parse and undo
publication seams in the implementation candidate.
`GFileHandler.loadPreflightedNativeXML` reads an already admitted native ZIP
without publishing path, saved state or undo history. `AppD` snapshots the
prior live archive and host document state. `UndoManagerD.prepareUndoBaseline()`
builds a disposable replacement while the old history remains authoritative;
after all fallible publication steps, `commitUndoBaseline()` replaces the
history atomically and increments a generation that discards stale asynchronous
stores. Prepared-baseline cleanup is nonthrowing. A parse or pre-commit failure
restores the prior construction and host state without clearing its undo/redo
history, while rollback failure remains a hard `NativeDocumentRollbackException`.
The native route does not call `kernel.initUndoInfo()`. `R2-D09` now checks
construction, path, recent-file, saved/loading and undo/redo preservation for
corrupt preflight, admitted live-parse and injected undo-commit failures.

Windows packaging now claims only `.cedg` for MSI/EXE and keeps portable
outputs association-free. Its jpackage-required MIME input is an internal
GeoCeDG-owned value, not the upstream MIME identity or a new serialization
contract.

## Validation authority and recorded results

| Gate | Expected | Recorded result |
|---|---:|---|
| `R2-L01`–`R2-L15` | 15 | PASSED — zero failures/errors/skips |
| `R2-D01`–`R2-D16` JUnit | 16 | PASSED — zero failures/errors/skips |
| `R2-D17` packaging static | 1 | PASSED — recorded by focused/static authority |
| supporting G9U0/G9A persistence JUnit | 31 | PASSED — zero failures/errors/skips |
| focused R2 JUnit | 31 | PASSED — zero failures/errors/skips |
| focused executed JUnit including support | 62 | PASSED — exit 0 in A and B |
| deterministic canonical-summary comparison | exact match | PASSED — `545c072c1a5c01e9fd91c10abb35571cbca492ce9f10a9ba0033bbbf3b4e0ce4` |
| G9U0-R1 | 6 | PASSED — zero failures/errors/skips |
| historical G9U0 | 93 | PASSED — zero failures/errors/skips |
| G9X1 | 62 | PASSED — zero failures/errors/skips |
| G5 | 10 | PASSED — zero failures/errors/skips |
| relevant G9A | 253 | PASSED — zero failures/errors/skips |
| legacy Locus | 76 | PASSED — zero failures/errors/skips |
| Checkstyle | four tasks / zero errors | PASSED in focused A and B |
| Git diff checks | clean | PASSED — both exit 0; saved logs |
| `R2-R07` full `tools/agent/verify.ps1` | exit 0 | PASSED — durable outer log; summary matches A/B |

The document-persistence rows are joint authorities rather than 16 isolated
Desktop suffix-policy tests:

| Matrix row | Required semantic support in addition to the Desktop marker |
|---|---|
| `R2-D09` | `GeoCeDGDocumentLifecycleTest.corruptNativeArchiveIsRejectedBeforeLiveLoad` covers disposable-preflight rejection plus preflight-admitted live-parse and injected undo-commit rollback while preserving construction and undo/redo; G9U0 `p15`/`p16` and G9A3 XML `xml06`–`xml08` retain semantic atomic-rejection authority |
| `R2-D10` | `G9U0PersistenceCompatibilityTest.p01EveryPublicGeneratorSaveAndReopenRestoresExactDescriptor`, `.p05NativeSaveReopenSaveIsByteIdentical`; `G9U0R2LocusPresentationTest.completePresentationPersistsAcrossTwoNativeXmlReopens`, `.everyOrdinaryLineTypePersistsWithoutChangingSubpaths` |
| `R2-D11` | `G9U0PersistenceCompatibilityTest.p02MetricQuerySaveAndReopenRestoresRichParent`, `.p03EveryIntersectionFamilyReopensWithExactQueryAndLedger`, `.p04BoundAndTokenPointsSaveAndReopenWithExactAddresses`, `.p16CorruptOrPartialTokenLedgerFailsAtomically` |
| `R2-D12` | `G9A3SpatialNativeCompatibilityTest.compat01GeoCeDGLoadsRecomputesSavesAndReopensNativePointExactly`; `G9A3SpatialCompatibilityXmlTest.xml10RepeatedReopenProducesOneCanonicalIdentityGraph`; full G9A corpus through `R2-R05` |
| `R2-D14` | `GeoCeDGDocumentLifecycleTest.classicKeepsClassicDefaultWhileNativeExtensionIsSupported`; `G9U0PersistenceCompatibilityTest.p13GeoCeDGClassicPreservesNativeV2WithoutEnablingCreation`; G9A3 native compatibility `compat02` and `compat03` |

The scenario JSON also binds all `D09` failure paths and external no-downgrade
(`D15`) to their Desktop/G9U0/G9A authorities. The focused verifier runs
all 31 supporting persistence tests and validates these method-level links;
the short `D10`–`D12` routing markers are never counted as sufficient alone.

The prepared undo-baseline correction invalidated the previously saved
automated tuple. Durable machine evidence now records the replacement focused,
deterministic, historical, ancillary and composed results below. Only manual
smoke and author disposition remain pending:

| Evidence record | Command | Exit code | Count/hash | Log path(s) |
|---|---|---:|---|---|
| static scaffold | focused verifier with `-SkipBuild` and transactional-final static root | 0 | PASSED | `transactional-final/static/static-scaffold.log` |
| focused pass 1 — shared + Desktop | focused verifier with recorded histories, `focused-a` root and canonical summary | 0 | 62 JUnit; SHA `545c072c…e0ce4` | A shared/Desktop Gradle logs and summary |
| focused pass 2 — shared + Desktop | same current candidate under `focused-b`, comparing to A | 0 | 62 JUnit; exact SHA match | B shared/Desktop Gradle logs and summary |
| `R2-R01` | G9U0-R1 focused verifier | 0 | 6; zero outcomes | transactional-final R01 shared/Desktop logs |
| `R2-R02` | historical G9U0 focused verifier | 0 | 93; zero outcomes | transactional-final R02 common/Desktop logs |
| `R2-R03` | G9X1 focused verifier | 0 | 62; zero outcomes | transactional-final R03 log |
| `R2-R04` | G5 DXF verifier | 0 | 10; zero outcomes | transactional-final R04 log |
| `R2-R05` | G9A3 spatial lifecycle verifier | 0 | 253; zero outcomes | transactional-final R05 log |
| `R2-R06` | legacy Locus V2 verifier | 0 | 76; zero outcomes | transactional-final R06 shared/Desktop logs |
| packaging static / optional artifact | embedded focused/static packaging verifier / artifact not requested | 0 | `R2-D17` PASSED / NOT_REQUESTED | static wrapper log plus A/B canonical summaries |
| Checkstyle | embedded current focused A/B shared/Desktop commands | 0 + 0 | four reports / zero errors | A/B Gradle logs and four XML reports |
| `git diff --check` / `git diff --cached --check` | exact commands | 0 + 0 | PASSED | transactional-final static diff logs |
| `R2-R07` composed without `-SkipBuild` | `.\tools\agent\verify.ps1 -KeepBuildOutputs -LogDirectory artifacts\g9u0-r2\transactional-final\composed-final` | 0 | terminal candidate; summary SHA `545c072c…e0ce4` | `transactional-final/verify-composed-final.log` (SHA `32ecaa14…87aaf`) |

Generated logs belong below `artifacts/g9u0-r2/` and are not tracked. Durable
machine evidence is
`geocedg/validation/g9u0-r2/g9u0-r2-product-refinement-evidence.json`.

## Manual author smoke — executed by the author; corrective iteration required

The author reported the following areas as passed: ordinary Locus V2 visual
styles, Properties behavior, copy/undo/redo, native `.cedg` save/reopen,
non-destructive `.ggb` compatibility transition, corrupt `.cedg` fail-closed
behavior and GeoCeDG Classic preservation. No installer artifact result is
inferred.

The continuity criterion in step 3 failed: a visible gap appears while the
generator moves continuously through a semantically defined interval. The
authoritative reproduction is the ignored generated artifact
`artifacts/smoke-test-g9u0-r2/locusFromMidpoint.cedg`, 13,301 bytes, SHA-256
`47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c`.
The file must not be modified in place or staged. A bounded R2 corrective
iteration and focused author re-smoke are required before disposition.

The original checklist was:

1. Launch GeoCeDG with the existing explicit Locus V2 opt-in and create the
   approved circle-driven public Locus V2 workflow.
2. Open Properties; change color, thickness, continuous/dashed line type,
   show/hide and applicable label presentation. Select, deselect and hover the
   locus. Confirm metric/intersection results and construction identity do not
   change.
3. Move the driver and dependent construction. Cross the locus with a line,
   then a circle/conic; move and restyle both objects. Confirm no artificial
   gap, component or subpath appears. Inspect a genuine disconnected-component
   fixture and confirm its real gap remains.
4. Copy the styled locus and exercise every applicable style undo/redo path.
5. Save As without a suffix. Confirm one lowercase `.cedg` is added, reopen it
   and inspect Locus style/generator, rich metric/intersection results, exact
   token point and G9 durable identities.
6. Open a copy of a legacy `.ggb`; invoke Save, cancel once, then save to a
   distinct `.cedg`. Compare the original `.ggb` path and SHA-256 before and
   after; they must be unchanged.
7. With a valid document already live, attempt to open a corrupt `.cedg`.
   Confirm the diagnostic is visible and the live document remains intact.
8. Open/save/reopen a supported `.cedg` through the separate GeoCeDG Classic
   diagnostic route. Confirm native types are preserved, experimental creation
   remains disabled and a new Classic document still defaults to its pre-R2
   identity.
9. If an MSI/EXE artifact was explicitly built, install it in a controlled
   Windows environment, shell-open `.cedg`, inspect the GeoCeDG-owned ProgID
   and confirm no `.ggb` association was claimed. Confirm portable ZIP/app-image
   use creates no association.

The author has not marked the overall checklist passed. Only the author may
accept the corrective re-smoke and final phase disposition.

## Retained risks and boundaries

- the shared presentation-refresh hook delegates to historical
  `updateRepaint()` by default; its host-wide Properties callers remain a
  regression-sensitive boundary covered by the final focused A/B and historical
  reruns and full composed verification;
- native local and remote archive preflight materializes the complete byte
  array without an explicit size cap;
- `CLOSED_BY_BOUNDED_TRANSACTIONAL_CORRECTION`: the bounded transaction
  prepares the replacement undo baseline before mutation, commits it only after
  all fallible publication steps, discards stale asynchronous stores by
  generation and restores the prior construction plus undo/redo state on
  injected parse or commit failure; focused A/B and supporting histories are
  clean, including full composed verification;
- automated technical evidence for the protected candidate is complete, but
  author smoke found the open R2-L11 render-continuity defect, so a bounded
  correction and author re-smoke are required and no PASS claim is made;
- external upstream GeoGebra remains unsupported for unknown GeoCeDG-native
  persisted types; no downgrade was added;
- an installer association claim requires an explicit generated-artifact
  probe, while static package-profile validation is mandatory;
- the internal jpackage MIME input is not a cross-platform format identity;
- public redistribution remains blocked by the existing licensing/assets gate;
- no G9U1, G9B, G9C, G9U2 or productive G10 work is included.

STOP FOR BOUNDED RENDER-CONTINUITY CORRECTION. Final re-smoke and disposition
remain author-owned. Do not self-approve or continue into G9U1.
