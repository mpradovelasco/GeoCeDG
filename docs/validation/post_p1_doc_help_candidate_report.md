# POST-P1-DOC-HELP — bilingual user guide and in-app help integration

```text
POST-P1-DOC-HELP = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
selfApproved   = false
authorApproved = false
passClaimed    = false
```

This is a documentation, frontend/help and packaged-resource track. It is not a
GeoCeDG geometric phase, not `G9B` and not a BOOK phase. It changes no kernel,
geometry, dependency graph, serialization, semantic identity, metric,
intersection, DXF algorithm, packaging policy or version.

## 1. Entry baseline

| Fact | Value |
|---|---|
| `HEAD` at entry | `0f578fd9b58db14fcecf457ed01fa7a8f1b8c785` |
| `main` at entry | `0f578fd9b58db14fcecf457ed01fa7a8f1b8c785` |
| `origin/main` after `git fetch --prune` | `0f578fd9b58db14fcecf457ed01fa7a8f1b8c785` |
| Worktree / index at entry | clean |
| Divergence | none; `HEAD == main == origin/main` |

Published state confirmed from the living roadmap and the live profile,
unchanged by this track:

```text
PRE-G9B-P1        = PASS — AUTHOR APPROVED
AUTHOR SMOKE      = PASS
GeoCeDG version   = 1.0.0
PROFILE NC        = APPROVED / PACKAGE-READY
PROFILE COMMERCIAL= NOT AUTHORIZED
```

No historical SHA was assumed: the P1 candidate `cac345ae2…` / tree
`71153c3b9…` is read as the closed-phase identity, and `0f578fd9b` is the
published authority this branch descends from.

## 2. Branch

```text
feature/post-p1-bilingual-user-guide-help
```

created from the clean published `main`. No work was done on `main`.

## 3. Candidate identity

| Field | Value |
|---|---|
| Branch | `feature/post-p1-bilingual-user-guide-help` |
| Candidate commit | `8c54fbfbc5f11aa1c3b5f41bf60f7d5163fd73b9` |
| Candidate tree | `c2276578daa9dcb7c26430c7fec77c9347a38512` |
| Parent | `0f578fd9b58db14fcecf457ed01fa7a8f1b8c785` |

A follow-up commit adds this identity block and the post-commit re-verification
of section 12 to the present report; that commit is the effective candidate tip
and is named in section 12.

The candidate is not promoted, not tagged, not pushed and not merged.

## 4. The two official guides

```text
docs/user/geocedg_user_guide_en.md
docs/user/geocedg_user_guide_es.md
```

Both carry the same 17 stable logical section identifiers, in the same order,
as discreet HTML comments so that visible numbering is never the identity:

```text
<!-- geocedg-guide-section: semantic-curves -->
```

| Logical ID | English title | Título en español |
|---|---|---|
| `about-this-guide` | GeoCeDG user guide | Guía de usuario de GeoCeDG |
| `what-is-geocedg` | 1. What is GeoCeDG | 1. Qué es GeoCeDG |
| `getting-started` | 2. Getting started | 2. Primeros pasos |
| `interface-and-workflow` | 3. Interface and construction workflow | 3. Interfaz y flujo de construcción |
| `documents` | 4. Documents | 4. Documentos |
| `basic-geometry` | 5. Basic geometry | 5. Geometría básica |
| `locus-v2` | 6. Locus V2 | 6. Locus V2 |
| `spline-v2` | 7. Spline V2 | 7. Spline V2 |
| `semantic-points-intersections-materialization` | 8. Semantic points, intersections and materialization | 8. Puntos semánticos, intersecciones y materialización |
| `lengths-and-measurements` | 9. Lengths and measurements | 9. Longitudes y medidas |
| `transformations` | 10. Transformations | 10. Transformaciones |
| `dxf-export` | 11. DXF export | 11. Exportación DXF |
| `presentation-and-visualization` | 12. Presentation and visualization | 12. Presentación y visualización |
| `user-tools-and-automation` | 13. User tools and automation | 13. Herramientas de usuario y automatización |
| `classic-compatibility-and-diagnostics` | 14. Classic compatibility and diagnostic tools | 14. Compatibilidad Classic y herramientas de diagnóstico |
| `known-limitations` | 15. Known limitations | 15. Limitaciones conocidas |
| `command-and-workflow-reference` | 16. Command and workflow reference | 16. Referencia de comandos y flujos |

### 4.1 Size

| Edition | Lines | Bytes | Sections |
|---|---|---|---|
| English | ~1 336 | ~55.8 KB | 17 |
| Español | ~1 385 | ~61.8 KB | 17 |

The Spanish edition is longer only because Spanish prose expands; it adds no
section, no command and no claim.

### 4.2 Deliberate language differences

Everything below is the complete list of intended divergence. Nothing else
differs.

1. **Titles and prose** are idiomatic in each language, not literal
   translations.
2. **Three fenced blocks are annotation, not code**, so their annotation words
   are translated while the surrounding identifiers are not:
   `.cedg = native GeoCeDG document` / `documento nativo de GeoCeDG`;
   `Length(...) ordinary number` / `número ordinario`;
   `Length(S,A,C) undefined` / `indefinido`.
   All other 17 fenced blocks are byte-identical across editions.
3. **§16.3** lists the localized command aliases the Spanish input help offers
   (`LugarGeométricoV2`, `LongitudLugarGeométrico`, `Longitud`, `Interseca`,
   `Punto`, `Refleja`). The subsection exists in both editions with the same
   content, because it is a fact about the product, not about the reader.
4. **§11.7 and §15** state in both editions that the DXF export dialog is
   presented in English regardless of the product language. That is a factual
   product limitation, reported in both, not a translation gap.

Command identifiers, branch keys (`"generator.main"`, `"spline-v2/main"`),
exact tokens, file extensions and code literals are never translated.

## 5. GeoCeDG authorities audited

Product behaviour was derived, in this order, from code/tests/build, then
approved specifications and ADRs, then the living roadmap, then author-approved
phase reports. Existing guides were starting material only.

| Authority | Used for |
|---|---|
| `apps/geocedg/application-profile.yml` (live v2 profile, 112 actions, 18 clusters, `localized_text`) | exact menu, group, action and dialog labels EN/ES; language policy; continuity policy; materialization policy; serialization extensions; feature defaults |
| `geocedg/features/experimental.yml` | which features are default on/off |
| `source/.../commands/command.properties`, `command_en.properties`, `command_es.properties` | exact public command syntax and the Spanish aliases |
| `source/.../CmdLength.java` | `Length(L)` and `Length(L,P,Q)` over a semantic curve |
| `source/.../AlgoSplineV2.BRANCH_KEY`, `SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY` | `"spline-v2/main"`, `"generator.main"` |
| `source/.../GeoCeDGActionRegistry.java`, `GeoCeDGMenuBar.java`, `GeoCeDGWorkspaceController.java` | help seam, Classic/laboratory routes, workspace restore |
| `source/.../GeoCeDGDxfExportController.java`, `DxfExportPreflightPresentation.java` | the real DXF dialog fields, defaults and the `ESTIMATED_ERROR` guarantee |
| `geocedg/specs/locus/locus-v2-semantics.md`, `-metrics.md`, `-intersections.md`, `-public-surface.md`, `-similarity-transformations.md` | Locus V2 structure, metrics, intersection admissibility, supported transformations |
| `geocedg/specs/curves/semantic-spline-2d.md`, `spline-v2-pair-materialization.md` | Spline V2 contract and the pair-intersection boundary |
| `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md` | exact/approximate mapping table, fidelity outcomes, reason codes, guarantee axis, sidecar, strict default |
| `geocedg/specs/ui/native-document-identity.md` | `.cedg` / `.ggb` policy |
| `geocedg/specs/ui/application-presentation-theme.md`, `application-presentation-sizing.md` | the three themes and the six presentation sizes with their defaults |
| `geocedg/specs/ui/g9u1-construction-interaction.md` | point-on-curve interaction, inspector, user tools |
| `geocedg/specs/operations/documentation-maintenance.md` | claim vocabulary and the `GUIDE_IMPACT` protocol |
| `docs/roadmap/geocedg_roadmap.md` | current status of P0/P1, `TD-P1-SEMANTIC-LENGTH-INTERSECTION`, and what remains unauthorized |
| Desktop tests `G9U1MetricReviewTest`, `G9S1NativeArchivePersistenceTest`, `G9U1IntersectionSessionTest`, `G9U0R5NativeArchivePersistenceTest`, `PreG9BP1PublicSurfaceTest`, `PreG9BP0PresentationThemeTest`, `G9X1DesktopPreflightContractTest` | the worked examples and their expected values |

The pre-writing audit is versioned as
[`post_p1_doc_help_audit_matrix.md`](post_p1_doc_help_audit_matrix.md), with a
row per guide section carrying claim, source authority, current status, EN/ES
wording, test or code evidence, known limitation and book relevance, plus the
stale-wording table and the translation glossary.

## 6. Historical documents used only as material

| Document | Taken | Rejected |
|---|---|---|
| `docs/user/geocedg_user_guide.md` (former content) | capability inventory, DXF boundary wording, limitation list | phase history, governance status, receipts, candidate framing, developer/workstation material |
| `docs/user/geocedg_construction_quick_guide.md` (former content) | reproducible worked examples, menu routes, pedagogy for semantic points and metrics | `--enableLocusV2=true` launch instruction, "candidato en revisión" framing, author-side diagnostic fixtures and the re-smoke checklist |

Every statement carried over was re-checked against code, specification or test
before it was written into either edition.

## 7. Worked examples

All examples are small, reproducible, and validated by existing desktop tests
rather than by a second semantic engine:

| Example | Expected | Test that reproduces it |
|---|---|---|
| `LocusV2` over `dom={false,{0,4,true,true}}`, `Length(L)`, `Length(L,U,V)` | `LL=4`, `LP=2` | `G9U1MetricReviewTest.ordinaryLocusQuickGuideUsesTheSameSemanticMetricAuthority` |
| `SplineV2({A,B,C,D},3)`, `Point(S,"spline-v2/main",u)`, `Length(S)`, `Length(S,P,Q)` | `M=4`, `MP=2` | `G9U1MetricReviewTest` (`createSemanticPartial`), `G9U1ScriptWorkflowTest` |
| `Length(S,A,C)` with constructor points | undefined | `G9U1MetricReviewTest.foreignOrCoincidentPointsNeverReceiveInferredMetricPreimages` |
| `c=Circle((0,0),1)`, `R=Intersect(S,c)` | two admissible roots | `G9S1NativeArchivePersistenceTest` |
| `T=Dilate(S,k,O)` | length scales by `abs(k)` | `G9U0R5NativeArchivePersistenceTest` |

No figures or screenshots were added. A figure atlas belongs to a separate task.

## 8. `Help → GeoCeDG user guide`

The existing seam was modified; no second help system was created. The action,
its catalog binding and its labels are unchanged:

```text
Help  → GeoCeDG user guide          (geocedg.help.UserGuide.name, en)
Ayuda → Guía de usuario de GeoCeDG  (geocedg.help.UserGuide.name, es)
action id  help.user-guide  →  target  geocedg.help.user-guide
```

What changed in `GeoCeDGActionRegistry`:

- a new package-visible pure selector

  ```java
  static String userGuideResource(String language)
  ```

  implementing the documented policy, with the same fallback discipline
  `GeoCeDGProfile.getText` already uses:

  ```text
  es       -> /org/geocedg/desktop/geocedg_user_guide_es.md
  en       -> /org/geocedg/desktop/geocedg_user_guide_en.md
  anything else, empty or null -> en
  ```

- `showUserGuide()` now resolves that resource from
  `app.getLocale().getLanguage()` instead of reading the quick guide;
- the read-only presentation was factored into one `showReadOnlyText(text, rows,
  columns)` helper. `message(...)` keeps its historical 8×54 size; the new
  `document(...)` uses 30×100 for a long document. The viewer is the existing
  `JTextArea` in a `JScrollPane`: read-only, vertical scroll as needed,
  selectable and copyable, word-wrapped at word boundaries, caret at the top,
  and rendered in `app.getPlainFont()` so it follows the presentation typography.
  No Markdown renderer and no new dependency were added, and no external browser
  is opened.

This is presentation/frontend only. It touches no kernel, geometry, DAG,
serialization or semantic identity.

## 9. Resource packaging

`source/desktop/desktop/build.gradle.kts` `processResources` now copies exactly
the two tracked sources:

```text
docs/user/geocedg_user_guide_en.md -> org/geocedg/desktop/geocedg_user_guide_en.md
docs/user/geocedg_user_guide_es.md -> org/geocedg/desktop/geocedg_user_guide_es.md
```

The former `geocedg_user_guide.md` and `geocedg_construction_quick_guide.md`
resources are no longer packaged. There is no second, hand-edited copy under
`src/main/resources`; the tests assert that such a copy does not exist.

Byte identity was verified twice — against the classpath resource by test, and
against the built jar:

```text
desktop.jar!org/geocedg/desktop/geocedg_user_guide_en.md
  sha256 3a71b3b3f07f91a7ea6d041d5296ac7ab415419e881d4ed1201de81c91a0fb92 == docs/user/geocedg_user_guide_en.md
desktop.jar!org/geocedg/desktop/geocedg_user_guide_es.md
  sha256 5bc9da40b7cee90776b5af28c28ecf2e7b478956388727f948b85fb2a4f39a9c == docs/user/geocedg_user_guide_es.md
```

and the jar contains no other guide entry.

## 10. Disposition of the two former guides

### `docs/user/geocedg_user_guide.md`

Replaced by a short index (≈ 50 lines) that keeps the path working and carries
no product statement of its own. It points to the two editions, the
mathematical reference, the operations manual, the developer guide and the
governing authorities, and marks the quick guide as superseded. A test asserts
it cannot grow back into a third manual.

Its **developer and operational** material — workstation requirements, clone
preparation, build and run, packaging and installation for internal evaluation,
running GeoCeDG and Classic, identifying the running application, the
development quick reference, the technical references and the maintenance
authority — was **relocated unchanged** to a new

```text
docs/developer/geocedg_operations_manual.md
```

so that nothing operational was lost. All its links were already
parent-relative and resolve identically from `docs/developer/`. Its remaining
sections (phase status blocks, "Can I use Locus V2 now?", the GUI section, the
DXF section, the maturity/capability/phase-status sections and the old
limitation list) were not carried forward: their product content is now owned by
the two guides, and their status content is owned by the roadmap, the
specifications and `docs/validation/`. Their text remains in Git history; no
`docs/validation/` or `geocedg/validation/` evidence was edited.

### `docs/user/geocedg_construction_quick_guide.md`

Reduced to a short **SUPERSEDED / LEGACY** marker that redirects to the two
editions and records why its former content was stale. It is no longer the
source of `Help → GeoCeDG user guide` and is no longer packaged. A test asserts
it cannot grow back into a manual.

### Reference updates

`README.md` (4 links, re-anchored to the Spanish edition),
`docs/architecture/geocedg_documentation_architecture.md`,
`docs/architecture/locus_v2_implementation.md`,
`docs/architecture/native_2d_geometry_export.md`,
`docs/architecture/cedg_workspace_architecture.md`,
`docs/developer/geocedg_developer_guide.md`,
`docs/developer/repository_map.md`,
`docs/roadmap/geocedg_roadmap.md` (living link only — no status text changed),
and the documentation-class table of
`geocedg/specs/operations/documentation-maintenance.md`.

`tools/agent/tests/g9u1-lifecycle.Tests.ps1` needed no change: its
`$ExpectedCloseoutPaths` names `docs/user/geocedg_user_guide.md`, which still
exists, and it compares a policy key set rather than document content.

## 11. Focused tests

New: `source/desktop/desktop/src/test/java/org/geocedg/desktop/PostP1BilingualUserGuideTest.java`
(15 cases). It covers the twelve required contracts:

| # | Required contract | Case |
|---|---|---|
| 1 | both guides exist | `bothOfficialGuidesExistAndAreSubstantial` |
| 2 | both are packaged | `bothGuidesArePackagedAsByteCopiesOfTheirTrackedSources`, `processResourcesPackagesExactlyTheTwoTrackedGuides` |
| 3 | ES resource for locale `es` | `helpSelectsTheEditionOfTheActiveProductLanguage` |
| 4 | EN resource for locale `en` | same |
| 5 | documented, tested fallback | `helpFallsBackToEnglishForAnyOtherLanguage` (`fr`, `de`, empty, `ES`, `null`) |
| 6 | identical section-ID set | `bothEditionsDeclareTheSameStableSectionIdentifiers` |
| 7 | main examples/commands in both | `bothEditionsCarryTheSameCommandsAndWorkedExamples`, `bothEditionsStateTheSameCurrentLimitations` |
| 8 | no known stale wording | `noKnownStaleWordingSurvivesInAnyLiveUserDocument`, `guidesNeverPresentCompatibilityInputAsTheNativeFormat` |
| 9 | Help no longer uses the quick guide | `helpNoLongerDependsOnTheSupersededQuickGuide`, `theSupersededGuidesAreNoLongerPackaged` |
| 10 | packaged copy comes from the versioned source | `bothGuidesArePackagedAsByteCopiesOfTheirTrackedSources` |
| 11 | Classic unchanged | `thisTrackChangesNoClassicOrPersistenceContract` (plus the retained `PreG9BP1PublicSurfaceTest.classicNeverReachesThePromotedCreationPolicy`) |
| 12 | geometry/persistence unchanged | `thisTrackChangesNoClassicOrPersistenceContract` asserts the unchanged `.cedg`/`.ggb`/`classic` serialization declarations and the unchanged help action binding and labels |

It also asserts, per language, that the resolved resource really is that
language's edition and that fenced examples survive packaging intact. The tests
check structure and contracts, not full prose.

Updated (contract preserved, target relocated):

- `PreG9BP1PublicSurfaceTest.packagedGuideIsAByteCopyOfTheTrackedSourceGuide` and
  `…userGuideDescribesThePromotedDefaultsAndTheDxfBoundary` now apply the same
  P1 contract to **both** editions. The language-dependent sentence
  "not a certified global error bound" moved to the focused test, which checks
  it per edition; the P1 test keeps the language-independent `ESTIMATED_ERROR`,
  the verbatim DXF boundary block, the two retained overrides in their disabling
  direction, the absence of both `=true` forms, and `1.0.0`.
- `G9U1WorkspaceSurfaceTest.guideIsPackagedWithDeclaredSourceContent` now checks
  both packaged editions.
- `G9U1IconReviewTest` now asserts the packaged EN guide instead of the
  superseded quick guide (an incidental resource assertion in an icon test).

## 12. Verification executed

Classification: **documentation + frontend/help + packaged resources**. Not a
geometric/kernel modification. The cheapest applicable independent profiles were
run; no `PHASE`, `INTEGRATION`/`COMPOSED` or `FINAL` campaign was launched,
because the live registry does not require one for this class and those runs
carry closeout semantics.

| Run | Command | Exit | Verdict | Evidence |
|---|---|---|---|---|
| Focused desktop tests | `.\gradlew.bat :desktop:desktop:test --tests org.geocedg.desktop.PostP1BilingualUserGuideTest --tests …PreG9BP1PublicSurfaceTest --tests …G9U1IconReviewTest --offline` | 0 | BUILD SUCCESSFUL | Gradle test report |
| Focused desktop tests | `… --tests …G9U1WorkspaceSurfaceTest --tests …G9U1ActionRegistryTest --offline` | 0 | BUILD SUCCESSFUL | Gradle test report |
| Whole desktop module | `.\gradlew.bat :desktop:desktop:test --offline` | 1 | 1 498 tests, 1 failure — `org.geogebra.io.XmlTest.emptyAppTest` | see §12.1 |
| Checkstyle | `.\gradlew.bat :desktop:desktop:checkstyleMain :desktop:desktop:checkstyleTest --offline` | 0 | BUILD SUCCESSFUL | — |
| STATIC (first) | `.\tools\agent\verify.ps1 -Profile STATIC` | 0 | `ACCEPTED / COMPLETE`, 2 diagnostic findings | `geocedg-static-bdf63942…` |
| INFRA_UNIT (first) | `.\tools\agent\verify.ps1 -Profile INFRA_UNIT` | 3 | `REJECTED_VERIFICATION_CORE` — pinned hash of `build.gradle.kts` | `geocedg-infra-080027b7…` |
| INFRA_UNIT (after repin) | `.\tools\agent\verify.ps1 -Profile INFRA_UNIT` | 0 | `ACCEPTED / COMPLETE`, 0 diagnostics | `geocedg-infra2-710352fa…` |
| STATIC (final) | `.\tools\agent\verify.ps1 -Profile STATIC` | 0 | `ACCEPTED / COMPLETE`, 1 diagnostic finding | `geocedg-static3-734c7b06…` |
| `git diff --check` | — | 0 | no whitespace findings | — |
| STATIC (post-commit, on the frozen candidate) | `.	oolsgenterify.ps1 -Profile STATIC` | 0 | `ACCEPTED / COMPLETE`, 1 diagnostic finding | `geocedg-static5-8c138aff…` |
| INFRA_UNIT (post-commit, on the frozen candidate) | `.	oolsgenterify.ps1 -Profile INFRA_UNIT` | 0 | `ACCEPTED / COMPLETE`, 0 diagnostics | `geocedg-infra3-d7bbae40…` |

The last two rows were executed on the committed candidate
`8c54fbfbc5f11aa1c3b5f41bf60f7d5163fd73b9` / tree
`c2276578daa9dcb7c26430c7fec77c9347a38512`; the follow-up commit that records
this identity and these rows changes only the present report.

Verification log roots are session-temporary and are not versioned.

### 12.1 Two verification facts that required action, and one that did not

**Static-contract identity (fixed).** `verification-static-contracts.json` pins
the canonical-LF SHA-256 of `source/desktop/desktop/build.gradle.kts`, which this
track changes. `infra.static-semantic` correctly rejected the stale pin. The pin
was re-derived and the registry's `static_contracts` catalog identity re-pinned
in turn, exactly as `1d75c191b` did for `PRE-G9B-P1`:

```text
source/desktop/desktop/build.gradle.kts
  42a8e814…3017213 -> 73f09895…3df769a1
geocedg/specs/operations/verification-static-contracts.json
  d34d0c44…153af57f1 -> 25bcb508…2a7fd613
```

No product code was changed to satisfy a verifier.

**Documentation diagnostic (fixed).** `tools/agent/diagnostics/documentation-diagnostic.ps1`
required the canonical `WORKSTATION` command to appear in
`docs/user/geocedg_user_guide.md`, whose workstation material moved to the
operations manual. The diagnostic's document set and the registry's
`impact_paths` for `diagnostic.documentation` now name
`docs/developer/geocedg_operations_manual.md`. The final STATIC run reports
`DIAGNOSTIC_CLEAR` for it.

**Governance diagnostic (not an action item).** The single remaining STATIC
finding is the standing
`"Legacy governance remains historical context during the temporary recovery
protocol."` note of `VERIFICATION_INFRASTRUCTURE_RECOVERY_PROTOCOL_V1`. It is
pre-existing, unrelated to this track, and diagnostic-class, so it changes no
acceptance verdict.

### 12.2 Environment failure, reported not absorbed

`org.geogebra.io.XmlTest.emptyAppTest` fails with:

```text
schema_reference.4: Failed to read schema document
'https://www.geogebra.org/apps/xsd/ggb.xsd'
```

This is an inherited upstream test that validates XML against a **remote** XSD.
This session has no outbound network: the same limitation made a non-`--offline`
Gradle run fail earlier while resolving `io.sf.carte:carte-util` from
`repo.geogebra.net`. The failure is an environment/permission condition, not a
product defect and not attributable to this track, which touches no XML, no
serialization and no shared-kernel code. It was **not** worked around and no
product code was changed for it. The remaining 1 497 desktop tests pass.

### 12.3 Open verification debt created by this track

Adding `PostP1BilingualUserGuideTest` changes the desktop discovery identity
pinned in `geocedg/specs/operations/verification-junit-inventory.json`
(`discovered_identity_count`, `discovered_identities_sha256`, and the derived
selection identities), and therefore the registry's `junit_inventory` catalog
hash.

It was **not** re-derived in this session, for two reasons:

1. re-derivation runs `tools/agent/checks/gradle-test-evidence-producer.ps1 -TestDryRun`,
   whose recorded `effective_arguments` deliberately contain no `--offline`; it
   requires network access this session does not have;
2. the inventory pins `base_commit`/`base_tree`, so it must be re-derived
   against the **exact** candidate identity — which is what `cac345ae2` did at
   the end of `PRE-G9B-P1`.

**Required before any `FINAL` run on this candidate:** re-derive the JUnit
inventory for both modules against the frozen candidate commit/tree with
`tools/agent/update-verification-junit-inventory.ps1`, then re-pin the
`junit_inventory` catalog hash in `verification-registry.json`. No hash was
fabricated.

## 13. Packaging / resource smoke

`:desktop:desktop:jar` was built and inspected: it contains exactly
`org/geocedg/desktop/geocedg_user_guide_en.md` and
`…_es.md`, byte-identical to their tracked sources (§9), and no other guide
entry. Because `jpackage` bundles this jar, the app-image inherits that content.

A full `PACKAGING` app-image gate was **not** run: packaging inputs, profiles,
policy and `package.yml` are untouched by this track, and an app-image build
requires the full JDK Desktop and WiX toolchain. The remaining items of the
installed-build checklist — EN opens the EN guide and ES the ES guide after a
language change and reopening Help, legibility, scrolling, text copying, and
fenced code blocks arriving unmutilated — are covered mechanically by
`PostP1BilingualUserGuideTest` at the selection and content level, and their
visual confirmation belongs to the author smoke.

## 14. Obsolete claims detected and removed

Detected in the former documents and eliminated from every live user document:

| Stale claim | Where it was | Current fact |
|---|---|---|
| `--enableLocusV2=true` required to create semantic curves | quick guide §1 | default ON since `PRE-G9B-P1`; only `=false` is documented |
| `PRE-G9B-P1: IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW` | former user-guide header | `PASS — AUTHOR APPROVED` |
| "candidato en revisión", `TestBasic1.cedg` / `Revision2.cedg` fixtures, author re-smoke checklist | quick guide title and §1, §9 | author-session evidence, not user instructions |
| pre-P1 menu/route wording and phase-status prose mixed into product explanation | both | phase status belongs to the roadmap |

Actively searched for and confirmed **absent** from both editions and from both
live redirects: `--enableLocusV2=true`, `--enableExtendedDxf=true`,
`P1 pending`, `P0 pending`, LocusV2 default OFF, G9X1 default OFF,
`PUBLIC REDISTRIBUTION = BLOCKED PENDING LICENSE/ASSET APPROVAL` as the state of
PROFILE NC, exact DXF `SPLINE` implemented, `.ggb` as the native format, Classic
creating Locus V2, and any G9B/G9C/G9U2/G10 capability described as present.
`noKnownStaleWordingSurvivesInAnyLiveUserDocument` fails if any of them returns.

## 15. User-facing limitations documented

Stated in both editions, §15 and in their own sections:

- `Length(S,P,Q)` may be undefined when an endpoint was materialized from a
  Spline V2 × Spline V2 intersection — described as a provenance-consumption
  boundary, explicitly **not** a numerical failure of the spline, with **no**
  proximity workaround suggested and **no** implementation date promised
  (`TD-P1-SEMANTIC-LENGTH-INTERSECTION`);
- exact DXF `SPLINE` is not implemented; Spline V2 exports as approximate
  `LWPOLYLINE` under the current G9X1 contract;
- the DXF approximation guarantee is `ESTIMATED_ERROR`, not a certified global
  error bound;
- no DXF import, viewport export, physical-unit contract, text export, legacy
  `Locus` export, implicit-curve contouring or 3D export;
- Locus V2 / Spline V2 keep the maturity `experimental` although default on,
  are GeoCeDG-only, and Locus V2 is not a generic `Path`;
- a tangent, ambiguous, stale or insufficiently certified intersection root
  stays rich-only;
- spatial semantics is unavailable and the dihedral-procedures workspace is
  visible but not authorized;
- Windows is the only validated workstation platform;
- branding is textual and provisional; archives keep the inherited `classic`
  application code;
- the DXF export dialog is English-only regardless of the product language.

Recovery receipts, CI hash pins, verification debt and internal governance are
deliberately **not** in the guides.

## 16. Exact file list

**Added**

```text
docs/user/geocedg_user_guide_en.md
docs/user/geocedg_user_guide_es.md
docs/developer/geocedg_operations_manual.md
docs/validation/post_p1_doc_help_audit_matrix.md
docs/validation/post_p1_doc_help_candidate_report.md
source/desktop/desktop/src/test/java/org/geocedg/desktop/PostP1BilingualUserGuideTest.java
```

**Modified**

```text
README.md
docs/architecture/cedg_workspace_architecture.md
docs/architecture/geocedg_documentation_architecture.md
docs/architecture/locus_v2_implementation.md
docs/architecture/native_2d_geometry_export.md
docs/developer/geocedg_developer_guide.md
docs/developer/repository_map.md
docs/roadmap/geocedg_roadmap.md
docs/user/geocedg_construction_quick_guide.md
docs/user/geocedg_user_guide.md
geocedg/specs/operations/documentation-maintenance.md
geocedg/specs/operations/verification-registry.json
geocedg/specs/operations/verification-static-contracts.json
source/desktop/desktop/build.gradle.kts
source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java
source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1IconReviewTest.java
source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1WorkspaceSurfaceTest.java
source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BP1PublicSurfaceTest.java
tools/agent/diagnostics/documentation-diagnostic.ps1
```

**Deleted:** none.

### 16.1 A byte-integrity incident, corrected

An intermediate scripted edit normalized `docs/roadmap/geocedg_roadmap.md` to
uniform CRLF. The file is committed with **mixed** line endings and is not
covered by a `.gitattributes` normalization rule, so this rewrote 1 784 lines
and `repository.whitespace-diagnostic` correctly reported it. The file was
restored with `git checkout --` and the two-line link update re-applied
byte-exactly; its diff is now 4 insertions / 2 deletions and `git diff --check`
is clean. No other file was affected: the remaining scripted edits touched files
that were already uniformly CRLF or LF.

## 17. GUIDE_IMPACT

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS  = docs/user/geocedg_user_guide_en.md;
               docs/user/geocedg_user_guide_es.md;
               docs/user/geocedg_user_guide.md;
               docs/user/geocedg_construction_quick_guide.md;
               docs/developer/geocedg_operations_manual.md;
               docs/developer/geocedg_developer_guide.md;
               docs/developer/repository_map.md;
               README.md
```

## 18. Worktree and divergence

Entry and exit both clean apart from the tracked change set above. The branch
descends from `0f578fd9b` with no rebase, no force operation, no history
rewrite, no tag and no push. `git diff --check` reports nothing.

## 19. What this track did not do

It did not change the version `1.0.0`, PROFILE NC, PROFILE COMMERCIAL, G9B,
G9C, G9U2, G10, G12, the kernel, Locus V2 semantics, metrics, intersections, the
DXF algorithm or packaging policy. It did not create a third full manual, delete
validation evidence, mark anything approved or PASS, run BOOK-P1, or modify
`geocedg_book` in any way. It did not promote anything to `main`.

## 20. Suggested BOOK-P1 source mapping

This section is an **informative consumption recommendation for a future,
separately authorized BOOK-P1**. It versions nothing in `geocedg_book`, changes
no editorial authority, and does not declare BOOK-P1 started or authorized.
The book repository was consulted read-only
(`editorial/roadmap/BOOK_ROADMAP.md`, `editorial/master-structure.md`), which
records `BOOK-P1 = NOT AUTHORIZED` and `BOOK MANUSCRIPT EXECUTION = PARKED`.

Throughout:

```text
user-guide statement != book claim authority
```

A guide section is a reliable record of **observable product behaviour,
terminology and reproducible examples**. It is never a substitute for the
scientific literature, the normative specifications, the code, the tests or the
validation evidence that an academic claim requires. No bibliography is implied
or invented by anything in these guides.

| Guide section | Candidate BOOK unit(s) | What it can support | What it must NOT be used to claim | Primary GeoCeDG authorities behind it |
|---|---|---|---|---|
| `what-is-geocedg` | II-01, I-04 | mission, non-CAD boundary, the CeDG principles as the product realises them, upstream relation | that the product *proves* any CeDG principle; foundational theory | `AGENTS.md` §1; roadmap head |
| `getting-started`, `presentation-and-visualization` | II-03 | product profile, languages, themes, presentation sizing, the presentation/semantics separation | that presentation state has any geometric meaning | presentation theme and sizing specs; live profile |
| `interface-and-workflow`, `documents`, `classic-compatibility-and-diagnostics` | II-03 | profile and menu surface, controlled legacy boundary, `.cedg`/`.ggb` interoperability, Classic containment | that `.ggb` round-trips GeoCeDG-only types; that diagnostic routes are product features | `native-document-identity.md`; live profile; `PreG9BP1PublicSurfaceTest` |
| `basic-geometry`, `interface-and-workflow` | III-01, III-02 | object/procedure grammar as exposed, explicit parameters, domains, orientation | general CeDG grammar beyond what is exposed | live profile; command properties |
| `locus-v2` | IV-01, IV-02 | semantic locus versus sampled polyline, generator/domain/branch/component, render as derived, currentness in user terms | evaluation, revision or render internals as a public surface | `locus-v2-semantics.md`, `locus-v2-public-surface.md` |
| `spline-v2` | IV-01, IV-05 | semantic spline creation, constructor points versus semantic points, points by explicit address | that Classic `Spline` shares its semantics | `semantic-spline-2d.md`; `AlgoSplineV2` |
| `lengths-and-measurements` | IV-03 | scalar versus rich metric surface, total versus partial length, endpoint provenance requirement, the spline-pair endpoint limitation | an integral, tolerance or error-bound claim; that the limitation is a numerical spline failure | `locus-v2-metrics.md`; `CmdLength`; `TD-P1-SEMANTIC-LENGTH-INTERSECTION` |
| `semantic-points-intersections-materialization` | IV-04, IV-05 | the *workflow*: rich result, local admissibility versus global completeness, exact token, materialization, dormant/reactivated lifecycle, inspector affordances | the full intersection mathematics, deterministic-identity theory, phase/rank or monodromy — `IV-04` owns that exposition and must derive it from its own sources | `locus-v2-intersections.md`; ADR 0017; `spline-v2-pair-materialization.md`; ADR 0021/0022 |
| `transformations` | IV-05, VI-02 | the seven supported similarity forms, new semantic identity, dynamic behaviour, the `k=0` collapsed image | generalisation to inversion, shear, affine/projective or 3D | `locus-v2-similarity-transformations.md` |
| `dxf-export` | IX-02, III-04 | exact-versus-approximate boundary, fidelity/reason taxonomy, `ESTIMATED_ERROR`, preflight, strict default, sidecar, viewport independence | a certified global error bound; an exact `SPLINE` entity; publication or redistribution conclusions | `dxf-curve-fidelity-and-approximation.md`; ADR 0014 |
| `user-tools-and-automation` | II-03, AP-E | persistent user-tool scope, library/document/invocation separation, fail-closed limits | a macro or scripting API contract | `g9u1-construction-interaction.md`; `G9U1UserToolLibraryTest` |
| `command-and-workflow-reference` | AP-E | current public command and workflow surface per release | that the surface is stable, or that any future API exists | `command.properties`; live profile |
| `known-limitations` | AP-G, AP-H | the per-claim limitation and provenance map, with the audit matrix as its source index | that a limitation is closed, dated or scheduled | roadmap technical-debt table; `post_p1_doc_help_audit_matrix.md` |
| `known-limitations` (experimental/gated rows), `locus-v2`, `spline-v2` | AP-C | rich-result states, guarantee levels, experimental/internal/public distinctions | that default-on implies stable maturity | `geocedg/features/experimental.yml`; ADR 0013 |

Not mapped on purpose: `V-*` (spatial semantics is unavailable in the product,
so the guides say nothing a book unit could cite), `VII-*`/`VIII-*` (no study,
optimization or DSL capability exists), and `AP-D` (persistence internals are
deliberately outside a user guide).

Four BOOK-P1 entry checks recorded in `BOOK_ROADMAP.md` remain the book's own
obligation and are untouched here — in particular, refreshing and deliberately
accepting a current editorial technical baseline, and re-querying
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` before publishing periodic
intersection material.

---

```text
POST-P1-DOC-HELP = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
selfApproved   = false
authorApproved = false
passClaimed    = false
```

Promotion to `main`, any tag and any release remain separate explicit author
decisions.
