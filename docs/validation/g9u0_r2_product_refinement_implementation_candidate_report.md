# G9U0-R2 product-refinement implementation closeout report

## Disposition

```text
G9U0-R2 = PASS — AUTHOR APPROVED
G9U0-R2 IMPLEMENTATION = PASS — AUTHOR APPROVED
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualAuthorSmoke = PASS
installedMsiRegistrySmoke = NOT_REQUESTED

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

- Entry/base: `ce022b756b51fe12497e1932ba3ae58093dd1405`
- Pre-correction candidate checkpoint:
  `bd7b6a5d128d5ac64222e55a76bcd91d8bb992e7`
- Branch: `feature/g9u0-r2-product-refinement`
- Planning authority: annotated `geocedg-g9u0-r2-planning-pass`
- Source boundary: **FROZEN** against the entry commit; 51 exact candidate
  paths, including all 26 changed `source/` paths registered upstream
- Focused automated validation: **PASSED — 31 R2 + 31 support JUnit**
- Deterministic focused rerun: **PASSED — canonical summaries match**
- Historical regressions: **PASSED — R2-R01 through R2-R06**
- Static scaffold, packaging, Checkstyle and Git diff checks: **PASSED**
- Full composed verification: **PASSED — exit 0; global verification terminal recorded**
- Original manual author smoke: **EXECUTED_FAILED_RENDER_CONTINUITY**
- Complete manual author smoke/re-smoke: **PASS — AUTHOR APPROVED**
- Installed MSI/registry smoke: **NOT_REQUESTED**

This is the author-approved implementation closeout. The protected checkpoint
freezes the earlier 49-path/24-source candidate and its completed automated
evidence. The bounded R2-L11 correction introduced two newly changed paths
relative to the planning entry, so the historical `PENDING correction rerun`
state correctly invalidated every earlier automated tuple. Replacement focused
A/B, historical, ancillary and composed evidence passes for the final 51-path
implementation; old generated logs remain historical/superseded artifacts, not
evidence for these bytes.
The author executed the manual smoke. Ordinary styles, Properties, copy,
undo/redo, native `.cedg`, non-destructive `.ggb` transition, corrupt-native
fail-closed behavior and GeoCeDG Classic preservation passed. The original
R2-L11 render-continuity requirement failed and remains preserved as historical
evidence. After the bounded correction, the author re-smoke confirmed periodic
continuity in real interactive use, including ordinary crossings, moderate zoom
and `.cedg` reopen, and approved the complete R2 phase.

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

The author smoke then exposed a distinct presentation seam: one periodic
branch whose sole valid component is the complete half-open declared driver
period was rendered with the ordinary open-endpoint inset, leaving the last
presentation vertex short of the periodic alias of the first. The bounded
correction suppresses that inset only under the exact **full-period render
predicate**:

1. the semantic provider is periodic;
2. the branch declares `PERIODIC`;
3. the branch has exactly one valid domain component;
4. that component equals the branch's declared driver domain; and
5. that component equals the provider's declared domain.

This remains derived presentation: it neither changes canonical parameter
evaluation nor joins semantic components. R2-L11 retains nonperiodic half-open,
genuinely disconnected and unrelated line/circle/conic negative controls under
fixed and adaptive policies. The exact 13,301-byte author reproduction is now
the tracked fixture
`source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r2/locusFromMidpoint.cedg`,
SHA-256
`47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c`.

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
| `R2-L01`–`R2-L15` | 15 | PASSED |
| `R2-D01`–`R2-D16` JUnit | 16 | PASSED |
| `R2-D17` packaging static | 1 | PASSED |
| supporting G9U0/G9A persistence JUnit | 31 | PASSED |
| focused R2 JUnit | 31 | PASSED |
| focused executed JUnit including support | 62 | PASSED; zero failures/errors/skips |
| deterministic canonical-summary comparison | exact match required | PASSED; SHA-256 `28a138b650636a032e7c61750ccd77b050772ba206b722051b55ce50aa61adc8` |
| G9U0-R1 | 6 | PASSED |
| historical G9U0 | 93 | PASSED |
| G9X1 | 62 | PASSED |
| G5 | 10 | PASSED |
| relevant G9A | 253 | PASSED |
| legacy Locus | 76 | PASSED |
| Checkstyle | four tasks / zero errors | PASSED |
| Git diff checks | clean | PASSED; saved exit 0 captures |
| `R2-R07` full `tools/agent/verify.ps1` | exit 0 required | PASSED; global terminal recorded |

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

The R2-L11 render correction invalidated the previously saved automated tuple.
Replacement durable machine evidence is complete for the corrected candidate:

| Evidence record | Command | Exit code | Count/hash | Log path(s) |
|---|---|---:|---|---|
| static scaffold | focused verifier with `-SkipBuild` under `final-closeout/static-preflight` | 0 | PASSED | `static-scaffold.log` |
| focused pass 1 — shared + Desktop | focused verifier under `final-closeout/focused-a` | 0 | PASSED; 62 JUnit | two Gradle logs plus canonical summary |
| focused pass 2 — shared + Desktop | focused verifier under `final-closeout/focused-b`, comparing pass 1 | 0 | PASSED; deterministic match | two Gradle logs plus canonical summary |
| `R2-R01` | G9U0-R1 focused verifier | 0 | PASSED; 6 | two historical logs |
| `R2-R02` | historical G9U0 focused verifier | 0 | PASSED; 93 | two historical logs |
| `R2-R03` | G9X1 focused verifier | 0 | PASSED; 62 | historical log |
| `R2-R04` | G5 DXF verifier | 0 | PASSED; 10 | historical log |
| `R2-R05` | G9A3 spatial lifecycle verifier | 0 | PASSED; 253 | historical log |
| `R2-R06` | legacy Locus V2 verifier | 0 | PASSED; 76 | two historical logs |
| packaging static / optional artifact | packaging verifier embedded by the R2 static gate / artifact not requested | 0 | PASSED / NOT_REQUESTED | `g9u0-r2-packaging.log` |
| Checkstyle | embedded focused A/B shared/Desktop commands | 0 | PASSED; four clean reports | focused A/B logs and Checkstyle XML |
| `git diff --check` / `git diff --cached --check` | exact commands | 0 / 0 | PASSED | two `static-preflight` logs |
| `R2-R07` composed without `-SkipBuild` | `.\tools\agent\verify.ps1 -KeepBuildOutputs -LogDirectory artifacts\g9u0-r2\final-closeout\composed` | 0 | PASSED; summary SHA matches A/B | `artifacts/g9u0-r2/final-closeout/verify-composed.log` |

Generated logs belong below `artifacts/g9u0-r2/` and are not tracked. Durable
machine evidence is
`geocedg/validation/g9u0-r2/g9u0-r2-product-refinement-evidence.json`.

## Manual author smoke — failed attempt preserved; re-smoke accepted

The author reported the following areas as passed: ordinary Locus V2 visual
styles, Properties behavior, copy/undo/redo, native `.cedg` save/reopen,
non-destructive `.ggb` compatibility transition, corrupt `.cedg` fail-closed
behavior and GeoCeDG Classic preservation. No installer artifact result is
inferred.

The original continuity observation in step 3 failed: a visible gap appeared while the
generator moves continuously through a semantically defined interval. The
original authoritative reproduction remains the ignored generated artifact
`artifacts/smoke-test-g9u0-r2/locusFromMidpoint.cedg`, 13,301 bytes, SHA-256
`47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c`.
Its exact bytes are also preserved as the tracked test-only fixture named
above; the original artifact is not modified or staged. The historical manual
state `EXECUTED_FAILED_RENDER_CONTINUITY` is retained. Replacement automated
validation is complete, and the author accepted the corrective re-smoke as
`PASS — AUTHOR APPROVED`.

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

The author marked the complete applicable checklist passed, accepted the
corrective continuity re-smoke and set the final phase disposition to
`PASS — AUTHOR APPROVED`. The optional real installed MSI/registry probe was
`NOT_REQUESTED`; no result is inferred for it.

## Retained risks and boundaries

- the shared presentation-refresh hook delegates to historical
  `updateRepaint()` by default; its host-wide Properties callers remain a
  regression-sensitive boundary covered by replacement focused A/B and
  historical and composed evidence;
- the bounded full-period render predicate is guarded by the byte-exact author
  fixture plus periodic/open/disconnected/crossing controls; its focused,
  deterministic, historical, static and Checkstyle evidence passes;
- native local and remote archive preflight materializes the complete byte
  array without an explicit size cap;
- `CLOSED_BY_BOUNDED_TRANSACTIONAL_CORRECTION`: the bounded transaction
  prepares the replacement undo baseline before mutation, commits it only after
  all fallible publication steps, discards stale asynchronous stores by
  generation and restores the prior construction plus undo/redo state on
  injected parse or undo-commit failure; this design seam remains closed and
  its replacement aggregate regression evidence passes;
- the original author smoke failure remains historical evidence; the accepted
  corrective re-smoke closes that defect without rewriting the failed attempt;
- external upstream GeoGebra remains unsupported for unknown GeoCeDG-native
  persisted types; no downgrade was added;
- an installer association claim requires an explicit generated-artifact
  probe, while static package-profile validation is mandatory;
- the internal jpackage MIME input is not a cross-platform format identity;
- public redistribution remains blocked by the existing licensing/assets gate;
- no G9U1, G9B, G9C, G9U2 or productive G10 work is included.

G9U0-R2 closes at `PASS — AUTHOR APPROVED` with `selfApproved=false`,
`authorApproved=true` and `passClaimed=true`. G9U1 remains
`DESIGNED — NOT AUTHORIZED` and requires a separate author authorization; do
not continue into it from this closeout.
