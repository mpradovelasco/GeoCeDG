# Maintenance — locale-independent `Dot` XML serialization: candidate report

```text
TRACK                     = maintenance/dot-xml-locale-independent-serialization
TASK                      = DOT/XML SERIALIZATION MAINTENANCE
DEFECT                    = pre-existing upstream-inherited Dot XML localization leak
LAYER                     = shared kernel serialization
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT (frozen at track start)
PLANNED_ACCEPTANCE        = FINAL
PRODUCT_PHASE_EFFECT      = NONE
R5_B_EFFECT               = removes the last prerequisite blocker only after author
                            approval and publication
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
implementationAuthorized  = true
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

This is the separate bounded maintenance required by
[ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md)
(Decisions 11 and 17) before productive `PRE-G9B-R5-B`. It is not `R5-B`
implementation: `R5-B` stays `DESIGNED — NOT AUTHORIZED` and `BLOCKED_ON` the
published maintenance. The author authorized implementation and technical
verification on the published base `P_R5B_ADR`
`e16697d96858639becf2dc72f7c7beaca8fcbd24` (tree
`3269ce7e29c14e72ec2c52011a350387f276e36a`). No evidence of an earlier session,
scratch probe or phase is reused as acceptance.

## 1. Defect

With the UI in Spanish, `d = Dot(u, v)` (typed as `ProductoEscalar(u, v)` or as
`Dot(u, v)`) is saved as `<expression label="d" exp="ProductoEscalar(u, v)"/>`.
The XML reader resolves command heads only by internal name, so on reopen, in
either UI language, `d` becomes an independent number that keeps its last value
and no longer depends on `u` and `v`.

**Reproduction on the exact base.** The two new regression classes were run
against the unmodified base source (only the new test files present):

| Class | Base result |
|---|---|
| `DotXmlLocaleIndependentSerializationTest` (shared) | 8 run, 5 fail as characterized, 3 controls pass |
| `DotXmlLocaleIndependentPersistenceTest` (Desktop) | 3 run, 3 fail as characterized |

The five shared failures: Spanish XML head `ProductoEscalar(u, v)` instead of
`Dot(u, v)`; EN and ES expression elements differ (EN already
`Dot(u, v)` / `(2 * Dot(u, v))`, ES `ProductoEscalar(u, v)` /
`(2 * ProductoEscalar(u, v))`); `noLocalDefault` prints `ProductoEscalar(u, v)`;
after an XML reload in EN or ES `d` is independent; after an undo restore in ES `d`
is independent. The Desktop failures: the Spanish native `.cedg` archive written by
the real Save As, and the Spanish `.ggb` written by the Classic writer, contain
`ProductoEscalar` heads.

A scratch probe (outside the tree, not a committed test) drove the same product
path on the base and recorded the reopen outcome:

- ES native Save As returns `true`; the save preflight logs `Opening file failed ·
  error in <expression>: label=d, exp= ProductoEscalar(u, v)` through
  `ErrorHelper$SilentErrorHandler` and still accepts the archive;
- reopening in EN or ES: `loadFile` returns `true`; `d` and `e` are independent
  `GeoNumeric` objects without a parent algorithm, values 11 and 22; setting
  `u = (3, 4)` leaves them at 11 and 22;
- an EN-authored document round-trips correctly (`Dot(u, v)`, recomputes to 25
  and 50), and reopened in ES it displays `ProductoEscalar(u, v)`.

## 2. Root cause

`ExpressionSerializer`, `case DOT`, non-GIAC branch, passed
`loc.getCommand("Dot")` to `twoVar(...)` for every string template. `twoVar`
prints its `op` argument as the head for every non-MathML, non-PSTricks,
non-GIAC string type, `GEOGEBRA_XML` included. `xmlTemplate` sets
`localizeCmds = false`, but this site ignored
`StringTemplate.isPrintLocalizedCommandNames()`.

Every other command head already honours that flag:
`Command.toString` prints `loc.getCommand(name)` only when
`tpl.isPrintLocalizedCommandNames()`, and so do the `If` (`appendIfCommand`) and
`DataFunction` heads. The `DOT` site was the only exception (ADR 0031, fact 7).

On load, `MyXMLio` sets `CommandLookupStrategy.XML`; `Command` then keeps the head
verbatim, the dispatcher reports `Unknown command : ProductoEscalar`,
`MyXMLHandler.startExpressionElement` records the error, and the later
`<element type="numeric">` creates an independent number.

Provenance: the serializer blob at the base,
`1fd72720d688b10999df9041a9bdbe49ee6a7e6f`, is identical to the upstream baseline
`9b93256b7df401ff056c37b502d82df4d72b1522`; the defect is upstream-inherited.

## 3. Placement

Shared Java kernel, serialization path. Operation identity is a semantic property
of the expression; it must round-trip through the construction XML consumed by
every frontend, undo and copy/paste. A Desktop, action-registry, Python or
presentation-layer workaround would leave the kernel writing a
locale-dependent persisted head. The XML reader, lookup strategy and document
schema are not the defect and are unchanged.

## 4. Fix

One site in `ExpressionSerializer`:

```java
twoVar(sb, leftStr, rightStr,
        tpl.isPrintLocalizedCommandNames() ? loc.getCommand("Dot") : "Dot",
        "<scalarproduct/>", "Dot", "?", tpl, kernel, false, loc);
```

The head is localized only for templates that localize command names, exactly the
rule of `Command.toString` and the `If`/`DataFunction` heads. `twoVar` is not
changed, no other operation is touched, and the GIAC branch is untouched.
`"Dot"` is the internal `Commands` constant the XML strategy accepts, and it is
what the EN UI already wrote; no persisted syntax is invented.

**Rejected alternatives.**

| Alternative | Reason |
|---|---|
| unconditional `"Dot"` | would change localized presentation (Spanish Algebra view, editing, LaTeX) before `R5-B` |
| gate on `StringType.GEOGEBRA_XML` only | invents a gating rule no other head uses and leaves the other non-localizing internal templates leaking the UI language |
| teach the XML reader localized `Dot` names | locale-dependent XML parsing; contrary to ADR 0031 (XML-strategy strings accept exact-case enum constants, not localized aliases) |
| rewrite `twoVar` for all binary operations | no evidence of another defective site in scope |

## 5. Template roles for `Operation.DOT`

`isPrintLocalizedCommandNames()` returns `localizeCmds`.

| Template class | `localizeCmds` | ES before | ES after | EN |
|---|---|---|---|---|
| `defaultTemplate`, `editTemplate`, `editorTemplate`, `inputBoxTemplate`, `algebraTemplate` | true | `ProductoEscalar(u, v)` | unchanged | `Dot(u, v)` |
| `latexTemplate` and variants | true | `\operatorname{ProductoEscalar}` | unchanged | `\operatorname{Dot}` |
| screen reader, LibreOffice | true | localized | unchanged | English |
| `mathmlTemplate` | — | `<scalarproduct/>` | unchanged | same |
| GIAC templates | false | `Dot.2` CAS signature branch | unchanged (branch not modified) | same |
| `xmlTemplate`, `casCopyTemplate` | false | `ProductoEscalar(u, v)` | `Dot(u, v)` | `Dot(u, v)` |
| `noLocalDefault`, `prefixedDefault*`, `maxPrecision*`, `maxDecimals`, `numericNoLocal`, `casCompare`, `ogpTemplate`, `GgbAPI` non-localized, scientific templates | false | `ProductoEscalar(u, v)` | `Dot(u, v)` | `Dot(u, v)` |

The non-localizing templates are internal roles: construction XML and undo
(`xmlTemplate`), CAS copy, internal strings re-parsed by the kernel, some under
the XML strategy (`PolygonFactory`, `RelativeCopy`), spreadsheet copy/paste,
style-bar redefinition, CAS input,
PSTricks/PGF/Asymptote export, the prover and the `GgbAPI` non-localized output.
None is a localized user presentation. After the fix each prints exactly what it
printed under the English UI. Under EN, `loc.getCommand("Dot")` is `Dot`, so every
EN output is byte-identical before and after. `forceEnglishCommands` mode already
resolved commands to English and is unaffected.

Executed: default, edit, LaTeX and MathML (ES and EN); XML, `noLocalDefault`,
`maxPrecision` and `casCopyTemplate` (ES). GIAC was not executed; it is unchanged
by construction.

## 6. Persistence, lifecycle and DAG evidence

| Route | Executed evidence |
|---|---|
| XML string, shared kernel | ES XML `<expression label="d" exp="Dot(u, v)"/>`, `<expression label="e" exp="(2 * Dot(u, v))"/>`, no `ProductoEscalar`; identical to the EN elements; identical for ES localized and ES English input |
| XML reload, shared kernel | ES document reloaded under EN and ES: `d` parent `AlgoDependentNumber`, top operation `DOT`, inputs exactly `{u, v}`, value 11; `e` contains `DOT`, value 22 |
| undo, shared kernel | ES undo restore keeps the same dependency and recomputation |
| native `.cedg`, Desktop | ES and EN documents through `GuiManagerGeoCeDG.saveAsTo` (atomic write plus save preflight), reopened through `AppD.loadFile` in EN and ES after the product `setLanguage` |
| `.ggb`, Desktop | ES `.ggb` written by the Classic headless writer (`MyXMLioJre.writeGeoGebraFile`, same shared serializer), opened through the GeoCeDG `.ggb` compatibility-input route (transactional preflight) in EN and ES |

`.cedg` and `.ggb` carry the same `geogebra.xml` from the same writer
(`MyXMLio` → `xmlTemplate`); the extension is routing, not semantics
(`GeoCeDGDocumentLifecycleTest` R2-D12/R2-D13). GeoCeDG does not write `.ggb`:
`AppGeoCeDG.saveGeoGebraFile` rejects non-native targets (asserted in the test), so
`.ggb` coverage is compatibility input.

After every reopen, recomputation proves the DAG survived: with `u = (1, 2)`,
`v = (3, 4)`: `d = 11`, `e = 22`. Shared: `v = (2, -1)` gives 0 and 0; then
`u = (3, 4)`, `v = (-3, -4)` gives -25 and -50. Desktop: `u = (3, 4)` gives 25 and
50; `v = (-4, 3)` gives 0; `v = (-1, -2)` gives -11 and -22. `e = 2 Dot(u, v)` is the
nested case.

The `If` neighbour already follows the same rule: in ES a function
`If(x > 0, x)` displays `Si(…)` and persists `If[…]`.

## 7. Historical documents written by the defect

```text
HISTORICAL_DISPOSITION = B — not safely recoverable without locale-dependent
                         XML parsing or migration semantics
MIGRATION_IMPLEMENTED  = false
RETAINED_DEBT          = TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD
```

A document saved by an affected version under a non-English UI contains the
localized head of that UI. The shipped command bundles hold 50 distinct non-English
`Dot` names in 98 bundles. Recovering them would require the XML reader to accept
localized command names, which ADR 0031 excludes, or a new migration keyed on the
document's authoring language. A textual rewrite is also unsafe: the token is not
reserved in XML. A scratch probe (not committed) under the EN UI defined a user
function `ProductoEscalar(a, b) = a + 10 b` and `d = ProductoEscalar(1, 2)`; the
document persists `exp="ProductoEscalar(1, 2)"` and reloads it as a dependent
function application with value 21. A rewrite to `Dot` would silently retarget such
a valid expression. The only existing narrow
seam, the `PolyLine[…, true]` → `PenStroke[…]` rewrite in
`startExpressionElement`, maps an internal legacy name and does not generalize to
localized names.

The fix therefore changes no reader behaviour. The committed test
`historicalLocalizedHeadIsNotReinterpretedByTheXmlReader` pins it: an XML payload
with `ProductoEscalar(u, v)` still loads, in EN and in ES, as an independent number
11. Such a document can be repaired by redefining the object as `Dot(u, v)` and
saving it with a corrected version. The mandatory acceptance criterion of this
maintenance is that new saves are locale-independent and round-trip.

## 8. Unchanged

XML command lookup strategy, document schema and format version, durable object
identity and GeoCeDG identity records, construction ordering, the undo/redo
mechanism (its XML snapshot now keeps the `Dot` dependency because it uses the same
serializer), GGBScript, the ADR 0031 public naming policy, localized `USER`
aliases, reverse command tables, parser lookup precedence, `twoVar` and every
other operation. The `FREEHAND` function-name site noted in ADR 0031 is outside
this task.

## 9. Changes

| Path | Change |
|---|---|
| `source/shared/common/src/main/java/org/geogebra/common/kernel/arithmetic/ExpressionSerializer.java` | `DOT` head gated on `isPrintLocalizedCommandNames()` (one call site, with its rationale); new upstream modification |
| `source/shared/common-jre/src/test/java/org/geocedg/common/kernel/DotXmlLocaleIndependentSerializationTest.java` | new shared regression class (8 tests) |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/DotXmlLocaleIndependentPersistenceTest.java` | new Desktop lifecycle regression class (3 tests) |
| `docs/upstream/modified-files.yml` | three entries: the serializer (`modified`) and the two test classes (`added`); 824 registered files |
| `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json` | derived catalog data from the official updater, and the canonical pin |
| `docs/roadmap/geocedg_roadmap.md` | maintenance state recorded as a technical candidate pending author review |
| this report and its JSON mirror | candidate record |

No reader, parser, lookup, schema, identity, GGBScript, localization bundle, build,
Gradle, verifier, guide or ADR change.

## 10. Tests

`DotXmlLocaleIndependentSerializationTest` (shared, `BaseUnitTest`, language switch
via `setLocale` plus an explicit reverse-table refresh through
`getCommandDictionary()`, asserted by parsing `ProductoEscalar(…)`):

- `spanishXmlUsesTheInternalDotHead`
- `englishAndSpanishPersistTheSameDotExpressions`
- `localizedPresentationTemplatesKeepTheCurrentHead`
- `nonLocalizingTemplatesUseTheInternalHeadInEveryLanguage`
- `ifNeighbourAlreadyFollowsTheSameTemplateConvention`
- `reloadInEitherLanguageKeepsTheDependencyAndRecomputes`
- `undoRestoreKeepsTheDependency`
- `historicalLocalizedHeadIsNotReinterpretedByTheXmlReader`

`DotXmlLocaleIndependentPersistenceTest` (Desktop, `G9U1TestApp` host, product
`AppD.setLanguage`):

- `spanishNativeDocumentReopensAsTheSameDependencyInBothLanguages`
- `englishAndSpanishNativeDocumentsPersistTheSameDotExpressions`
- `spanishCompatibilityDocumentReopensAsTheSameDependency`

With the fix: 8/8 and 3/3 pass, 0 skipped.

Adjacent perimeter, one run on the fixed tree: shared `org.geogebra.common.io.*`,
`org.geogebra.common.kernel.arithmetic.*`, `CommandsTest`, `RedefineTest`,
`SyntaxLocalizationTest`, `LocalizationTest`, `AuralTextTest`, `CopyPasteTest`,
`G9U0PersistenceCompatibilityTest`, `org.geocedg.common.spatial.*XmlTest` and the
new class: 70 classes, 1 605 tests, 0 failures, 0 errors, 1 skip (the upstream
`@Disabled` `BernsteinPolynomial1DTest.testSpit`). Desktop
`GeoCeDGDocumentLifecycleTest`, `XmlTest`, `G9U0LocalizationHelpTest`,
`G9U1ProductPolicyTest`, `GeoSymbolicVectorTest`, `SymbolicVectorTest`,
`CreateSliderTest`, `G9S1NativeArchivePersistenceTest`,
`G9U1MacroNativeArchivePersistenceTest` and the new class: 10 classes, 74 tests, 0
failures, 0 skips.

Checkstyle `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest` and
`:desktop:desktop:checkstyleTest`: no finding. `Assert-GeoCeDGUpstreamBoundary
-ExpectedBaseline 9b93256b…`: pass, 824 registered files.

## 11. Catalog derivation

Before any repin, the current registry pin was reproduced from the unchanged
inventory with `Get-VerificationCanonicalTextSha256`
(`5da8c284055d01832461372e7cfb62c4d07632bc278fac451192964d46bd00f4`). The official
updater (`tools/agent/update-verification-junit-inventory.ps1`) then ran in-session
with new discovery dry-run evidence for both modules and executed selection evidence
for `final.shared` and `final.desktop` (`-SelectionEvidencePath`). Only the 11 new
identities enter the catalog; every narrow selection is unchanged.

| Value | Base | Candidate |
|---|---|---|
| `discovery.shared` / module shared | 5925, `28dd7430…` | 5933, `7ef39da530934b47b1b25056cbdde02f3a2e2daad60e9b046fae55c948198422` |
| `discovery.desktop` / module desktop | 1598, `973f5fae…` | 1601, `cbaaa22d509bfecaed62770070f283b4841ae0ed7de05fe6ecf81cc5153b65da` |
| `final.shared` | 6842, `558688f1…` | 6850, `6b1c2e97bfa2c81e355dcc3b2c32504d49d51c35a4200ddaafa905695b3d7b07` |
| `final.desktop` | 1592, `f85201a9…` | 1595, `58ad9f992d4c1c73ff8698ed3b6e1b65066dd24e20cc0e8dbf41e2e214beab3d` |
| inventory canonical hash / registry pin | `5da8c284…` | `02c9aa369a1e5b53e6d44e918732c373c37331af39b50af25d7ff0e2687c5381` |
| `FINAL` execution plan (`-PlanOnly`) | `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517` | unchanged; 68 nodes, `COMPLETE`, byte-identical plan output |

Producer evidence (kept locally under the ignored
`artifacts/agent/maintenance-dotxml-inventory/`):

| Selection | Evidence SHA-256 | Result |
|---|---|---|
| `discovery.shared` (dry run) | `6ae4da9f722a35d43da8b86520aeff667381b0676237a85b90d3e36b6c65a7e4` | completed, 687 JUnit files |
| `discovery.desktop` (dry run) | `258f6f208a9a2afbc6600e468f4e7de9c68a0d6f47d8d853df858b7fae5d263c` | completed, 109 JUnit files |
| `final.shared` (executed, `--rerun-tasks`) | `6739d0370d07598cd02e8fa14efa2a65a5787f0b206f0b44632eeb2f5916ba75` | 687 JUnit files, 6850 tests, 0 failures, 0 errors, 10 skips equal to its 10 allowlisted not-applicable identities |
| `final.desktop` (executed, `--rerun-tasks`) | `9f7026361b25d6b3e9e7c333d4a22db8bba4f168d8974f481b8e011a81157c9a` | 108 JUnit files, 1595 tests, 0 failures, 0 errors, 1 skip equal to its 1 allowlisted identity |

No derived hash was entered by hand.

## 12. Verification

The frozen class is `GLOBAL_IMPACT`: the change is in the shared expression
serializer used by persisted documents, undo and copy/paste; the new tests run
canonically only in the `final.shared` and `final.desktop` selections; and only
`FINAL` issues a receipt. Current §12.8 of
`geocedg/specs/operations/verification-levels.md` still maps `GLOBAL_IMPACT` to
FULL, whose canonical profile is `FINAL`. One normal `FINAL` runs on the committed
candidate; its identity and result are reported outside this artifact because the
artifact cannot name its own commit.

## 13. Impact

```text
PRODUCT_PHASE_EFFECT               = NONE (maintenance correction of a pre-existing
                                     upstream-inherited serialization defect)
SERIALIZATION_FORMAT_IMPACT        = NONE (schema, version and reader unchanged; the
                                     Dot head is written in the internal form EN
                                     already wrote)
IDENTITY_SCHEMA_IMPACT             = NONE
MIGRATION_IMPACT                   = NONE (historical localized heads retained as debt)
GUIDE_IMPACT                       = NONE
GUIDE_JUSTIFICATION                = The maintenance restores locale-independent
                                     persistence for an existing expression operation
                                     and does not introduce a new user-facing feature,
                                     command, workflow or file-format contract.
BOOTSTRAP IMPACT                   — NO CHANGE REQUIRED (no workstation, toolchain,
                                     Gradle, Conda, packaging, download or environment
                                     change)
VERIFICATION INFRASTRUCTURE IMPACT = METADATA ONLY (new JUnit identities and the
                                     registry pin from the official updater; no
                                     verifier code change)
UPSTREAM_BOUNDARY_IMPACT           = one new minimal upstream modification
                                     (ExpressionSerializer); two GeoCeDG test files
                                     registered; 824 files
```

The user guides do not document `Dot` serialization or its Spanish display; no
guide statement is contradicted.

## 14. Debt

| Identifier | State |
|---|---|
| `HZ-R5B-DOT-LOCALIZED-XML` (hazard in the `R5-B` compatibility evidence) | resolved for new saves by this candidate, pending author approval |
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | new, retained: documents already saved with a localized `Dot` head are not recovered (disposition B, §7) |

## 15. Authorization state

The author authorized implementation and technical verification of this
maintenance only. Author approval, closeout, promotion, push, tag, release, any
substantive ADR 0031 update, `R5-B` implementation and `R6` each require a separate
explicit author decision naming the exact candidate.
