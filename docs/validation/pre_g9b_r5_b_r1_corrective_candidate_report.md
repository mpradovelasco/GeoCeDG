# PRE-G9B-R5-B-R1 — runtime language switch corrective candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R5-B, authorized corrective descendant PRE-G9B-R5-B-R1
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE (unchanged; no escalation)
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile INTEGRATION

INITIAL T_R5B                  = 7bc113afb22bb6b97b2ad5e1e3f00117c8022be2
INITIAL T_R5B tree             = d47aafdfaace0aa7919f66dde9ff24ec3d1dac65
INITIAL TECHNICAL VERIFICATION = ACCEPTED / COMPLETE
                                 (INTEGRATION verification-cf57f99b1a6f4ebe8398837769747b2f, 19/19;
                                  STATIC verification-dc45cec7497c4993b76e84b0bb3f62db, 3/3)
INITIAL AUTHOR SMOKE           = CORRECTIVE FINDING
INITIAL AUTHOR APPROVAL        = NOT GRANTED
T_R5B_R1                       = the commit that contains this artifact; its parent is
                                 T_R5B; not nameable inside it, reported separately

GUIDE_IMPACT              = NONE
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical commit
is the sole authority for approval. The
[initial candidate report](pre_g9b_r5_b_candidate_report.md) and its
[evidence](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-candidate-evidence.json)
stay unchanged as history. Machine-readable evidence of this descendant:
[`pre-g9b-r5-b-r1-corrective-evidence.json`](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-r1-corrective-evidence.json).
The post-commit `INTEGRATION` evidence of the descendant is reported outside this
file.

## 1. Entry identity and authority

| Identity | Value |
|---|---|
| corrective base = parent of this descendant (not amended) | `T_R5B` `7bc113afb22bb6b97b2ad5e1e3f00117c8022be2`, tree `d47aafdfaace0aa7919f66dde9ff24ec3d1dac65` |
| implementation base of `PRE-G9B-R5-B` | `P_DOTXML` `504a19f7e655ef50c4bfa8095a07f94c11b27a01`, tree `63e7f0cd79aa9e1cf1259b6466e8ac16dc261704` |
| local `main` = `origin/main` = live remote `main` after `git fetch` | `504a19f7e655ef50c4bfa8095a07f94c11b27a01` |
| local branch (not pushed; not identity) | `phase/pre-g9b-r5-b-canonical-english-command-surface` |
| worktree at entry | `HEAD` = `T_R5B`; one untracked author file, `source/desktop/desktop/hs_err_pid32224.log` (§3) |

The author authorized `PRE-G9B-R5-B-R1` on 2026-10-01 as a bounded corrective
descendant of `T_R5B` in response to an author-smoke finding on runtime language
switching and Input Help. It is not a new phase and does not reopen ADR 0031. The
author recorded the disposition of the initial candidate: technically accepted,
author smoke `CORRECTIVE FINDING`, author approval not granted, superseded only if
this descendant is successfully verified. The author also recorded that the manual
`T-SHADOW` smoke passes and that `Circle=5` followed by fresh `Circle(...)` input is
ordinary `USER` semantics, not the defect. The class stays `INTEGRATED_PHASE` with
`INTEGRATION` acceptance; no escalation was requested or needed.

## 2. Reported finding

Start in English, create an ordinary construction (`Circle(...)`), use Input Help,
then change **Options → GeoCeDG options → Product language…** to **Español** in the
same session: Input Help stops responding or behaves incorrectly; continued use can
degrade the application; in one author run the application froze and an exception
was emitted. The author treated it as a runtime localization lifecycle / Input Help
refresh regression with no evidence of a shadowing cause.

## 3. Author-smoke artifacts

| Artifact | Fact |
|---|---|
| `artifacts/pre-g9b-r5/testCircle.cedg` | SHA-256 `915f2b070e71e4fd47f15441e0ff2aae7f7795ab74863b7871faf1d0d6afd110`, 7 013 bytes; untracked and ignored (`.gitignore` `/artifacts/*`); not modified. A scratch copy was opened by the windowed probes. It is the `T-SHADOW` smoke document (`f = PerpendicularLine((1, 1), xAxis)`, `d = Circle((0, 0), sin(1°))`, `Circle = 5`, `u = Circle * ((0, 0), 4)`). The lifecycle defect reproduces without it; it is not promoted into the committed fixtures. |
| `artifacts/pre-g9b-r5/testCircle2.cedg` | SHA-256 `2291223948a151abde0164f54f50a22b541ba219b0875c4f9b7e388dc8e9c93f`; untracked and ignored; not named by the author and not used. |
| `hs_err_pid32224.log` | SHA-256 `8309c05a8e8388a90fe3edbb8975fd3f9a76523e7fdd67a86d205acdd36f539d`, 94 353 bytes, written 2026-10-01 00:39:33 by the author's `runGeoCeDG` session. Found untracked at `source/desktop/desktop/`, where it made the upstream-boundary check fail; moved unchanged (same SHA-256) to `artifacts/pre-g9b-r5/hs_err_pid32224.log`, ignored. |
| console output, other logs | none recoverable: no console capture, no other `hs_err`, no product log; the GeoCeDG preferences folder holds only preferences. |

## 4. Exception record

The only recoverable exception is the JVM fatal-error log above. It records a
native `EXCEPTION_ACCESS_VIOLATION` (reading address `0x64`) in `awt.dll` on the
`AWT-Windows` thread, inside Windows message dispatch through `COMCTL32` and the
FlatLaf native window subclass, after 782 s of session, on Temurin 25.0.4. The
`AWT-EventQueue-0` thread was also in native code. The log has no Java frame of
GeoCeDG or GeoGebra code and no Java exception; its recent-exception events are
JVM-internal. No first product frame can be identified from it, and no stack trace
is invented here. Five automated windowed runs (§5) did not reproduce a freeze, an
uncaught exception or a crash, so the native crash is **not attributed and not
claimed fixed** (§17).

## 5. Reproduction

Scratch probes (not committed; run through a Gradle init script from the session
scratchpad) drove the real Desktop lifecycle:

- **Embedded host.** One `AppGeoCeDG` per scenario on the event-dispatch thread:
  build the application panel, create `c=Circle((0,0),2)`, show Input Help, then the
  real `settings.product-language` action, with only its chooser dialog answered.
  Scenarios: EN start → ES; EN start → ES by the host `setLanguage`; ES start → EN;
  EN → ES → EN; EN → ES → EN → ES.
- **Real windowed product.** `org.geocedg.desktop.GeoCeDG.main` with an isolated
  `--settingsfile`. The run uses the real frame, the real **Product language…**
  menu item (`doClick`) and the real modal chooser, answered through its own combo
  box and OK button. Clicks, typing and hovering are dispatched to the Swing
  components, with no operating-system input. An EDT watchdog (3 s), an
  uncaught-exception handler, console capture and screenshots recorded the run.
  Runs: `T_R5B` and `P_DOTXML` on Java 17 (EN → ES → EN → ES with tree clicks,
  syntax, autocomplete and Input Help toggles); `T_R5B` on Temurin 25.0.4, the
  author's runtime (same sequence); `T_R5B` on 25.0.4 with a scratch copy of
  `testCircle.cedg`, 16 switches and a save after each; the corrected tree on 25.0.4.
- **First-use lookup.** The first `USER` lookup right after the action, at both
  identities.

Minimal deterministic sequence: start in English; create `c=Circle((0,0),2)`; show
Input Help; choose **Product language… → Español**; then (a) read the Input Help
captions and labels, or (b) type a Spanish alias such as `Circunferencia(...)`
before anything else touches the command dictionary.

## 6. Behaviour at `P_DOTXML` and at `T_R5B`

After the product-language action:

| Observation | `P_DOTXML` | `T_R5B` |
|---|---|---|
| Input Help tree rebuilt | no: captions and leaves of the previous language | no: captions and entries (aliases) of the previous language |
| Input Help title, **Paste**, **Show Online Help**; view titles; input-bar label | previous language | previous language |
| first `USER` lookup of a new-language alias (`Circunferencia` after EN → ES) | rejected | rejected |
| first `USER` lookup of an old-language alias after ES → EN | still accepted | still accepted |
| focus of an Input Help entry by command | fails: the tree holds previous-language names | works: entries carry identity |
| clicking a stale leaf | previous-language name, new-language head and body (`Circle` → `Circunferencia( <Punto>, … )`) | canonical head, new-language body |
| **Paste** | inserts the previous-language name | inserts the canonical head |
| real input-bar autocomplete in Spanish | `Circ` → no completion | current-language completions |
| freeze, EDT stall > 3 s, uncaught exception, crash | none | none |
| construction XML | unchanged | unchanged |
| same scenario through the host `setLanguage` | coherent | coherent |

Mechanically: the committed regression (§14) fails on `T_R5B` in 3 of 4 tests at
the first-use lookup (`expected: <Circle> but was: <null>`, and the converse after
ES → EN). A scratch variant that reproduces `T_R5B`'s action body verbatim fails in
the same 3 tests at the Input Help captions (`expected: <Funciones Matemáticas> but
was: <Mathematical Functions>`).

## 7. Causality classification

```text
CLASSIFICATION = B. PRE-EXISTING DEFECT MADE OBSERVABLE BY R5-B (NOT WORSENED)
```

- **Pre-existing.** The root cause (§8) is the GeoCeDG product-language action
  introduced by the G9U1 construction workspace (`b4921940`, 2026-09-04). R5-B did
  not touch it. The stale reverse table reproduces identically at `P_DOTXML`, and
  the Input Help staleness reproduces there with more symptoms.
- **Not worsened.** Every `T_R5B` symptom also occurs at `P_DOTXML`. Identity-carrying
  entries and canonical completion remove three of `P_DOTXML`'s symptoms (focus,
  mismatched heads, dead autocomplete).
- **Made observable against R5-B's own contract.** During an ordinary supported
  EN ↔ ES switch, `T_R5B` fails accepted R5-B obligations. Localized aliases are not
  accepted in the current UI language on first use (`T-INPUT`). Input Help
  captions, labels and entry aliases are not those of the current UI language
  (ADR 0031 decisions 1 and 9; guide §16.3; `T-SYNTAX`, `T-AUTOCOMPLETE`). The
  defect is causally external but not harmless to the R5-B contract, so under the
  author's rule it is an R5-B blocker.
- **Stop conditions not met.** The behaviour is not identical at `P_DOTXML`, and the
  impact on the R5-B contract is direct. The correction needs no change to the
  parser, lookup precedence, reverse-table semantics, XML or policy. The root cause
  is a GeoCeDG-owned Desktop action, not a wider upstream lifecycle defect.

## 8. Root cause

`GeoCeDGActionRegistry.chooseLanguage()` applied the choice with
`AppD.setLocale(Locale)`. That setter swaps the localization bundles, the fonts and
the language flags, then runs `GuiManagerD.updateFonts()`, which rebuilds the
toolbar and menu bar and refreshes fonts and the component tree. It never runs the
host runtime language change, `AppD.setLanguage(Locale)`:

```text
setLocale(locale)
Kernel.updateConstructionLanguage()
setLabels():
    GuiManagerD.setLabels()      menus, InputBarHelpPanelD.setLabels() -> setCommands(),
                                 Algebra view, Algebra input, docks, dialogs
    kernel.setViewsLabels(), updateLocalAxesNames()
    updateCommandDictionary()
setOrientation()
```

Consistency was lost at the return of the action. The `Localization` was already
the new generation, while Input Help (tree, captions, entries, labels) and the
command tables were still the old one:

1. **Input Help.** Its tree is built only by `setCommands()`, so it kept the
   previous language's captions and entries. Its syntax pane, autocomplete and
   error text read the new bundles on every call. The result is a
   mixed-generation surface.
2. **Command tables.** These are refilled lazily, on the next call that checks
   `Localization.isCommandChanged()`: `getCommandDictionary()`,
   `getSubCommandDictionary()` or `getOfferedCommands()`.
   `App.getReverseCommand()` does not check it, because `initTranslatedCommands()`
   refills only when the table is null. So the first `USER` lookups after the
   switch used the previous language's reverse table, until an unrelated call
   refreshed it.

The reported "stops responding / behaves incorrectly" matches an Input Help that no
longer follows the product language, and Spanish input refused right after the
switch. The freeze is §4.

## 9. Hypotheses

**Primary hypothesis: ordering inside `AppD.setLanguage` — rejected as a cause.**
`setLabels()` does call `GuiManagerD.setLabels()` before `updateCommandDictionary()`.
But `InputBarHelpPanelD.setCommands()` (both the inherited and the R5-B canonical
branch) starts with `app.getSubCommandDictionary()`, which refills every
dictionary first whenever `isCommandChanged()`. The trailing
`updateCommandDictionary()` is then a no-op. Measured after the host `setLanguage`:
the Input Help entries equal those of a fresh start in the target language, and the
reverse table equals the fresh-start table (983 entries for ES, 532 for EN), equal
to the `P_DOTXML` fixture. No mixed generation arises on that path. The defect is
that the product action never enters it.

| Other hypothesis | Disposition |
|---|---|
| stale `CanonicalCommandEntry` objects after a locale change | consequence of §8.1, not an independent cache; entries are rebuilt by `setCommands()` |
| identity rebuilt from a stale display string | excluded: leaves carry identity; focus, selection and paste used it in every run |
| stale `LowerCaseDictionary` | excluded for `T_R5B` completion, which recomputes offered entries per request; `P_DOTXML`'s real input bar kept a stale dictionary (no completion) |
| stale Input Help nodes after dictionary regeneration | confirmed; §8.1 |
| stale syntax-provider state | excluded: syntax reads the current bundles; the pane showed the new body |
| stale alias / search index | confirmed for the `USER` reverse table (§8.2); excluded for completion |
| stale command-head recording state | excluded: the recorder is restored in `finally`; `E(k)` reads a locale-independent bundle |
| CAS-filled vs fresh table state | pre-existing and identical at `P_DOTXML`: the CAS group appears in Input Help only if CAS is ready when the tree is rebuilt (§17) |
| EDT / Swing mutation during refresh | not observed: all work on the EDT; no stall > 3 s; maximum latency ≤ 576 ms |
| null selection or tree-node assumptions after `setLabels()` | excluded: selection and focus work after every rebuild |
| repeated listener registration, replaced models | excluded: the model is reused; no duplicate leaves; no latency growth over 16 switches |
| `focusCommand` / `HelpOnKeywordPanel` interaction | excluded at `T_R5B`; `P_DOTXML` focus fails after the bare switch |
| R5-B typed "no syntax" result | excluded: no focused command has an empty syntax |

## 10. Architectural placement

| Layer | Decision |
|---|---|
| shared localization lifecycle | unchanged: it is coherent, since the tables refill on first checked access, and the `setLocale` semantics stay as they are for Classic and every other caller |
| Desktop command-dictionary refresh lifecycle | owner of the fix, at its entry point: the GeoCeDG product-language action, which must enter the host runtime language change instead of the bare locale setter |
| Desktop Input Help consumer | unchanged: correct when notified (`setLabels()` → `setCommands()`); no retry, catch or consumer-side refresh |
| R5-B canonical-entry cache | none exists; entries are rebuilt from the current generation |

The host `AppD.setLanguage(Locale)` is the authoritative runtime language seam, the
one the upstream language menu uses. Using it keeps one lifecycle for every
consumer and changes no upstream-owned file. It needs no new GeoCeDG copy of host
label code and no Desktop lifecycle state in the shared kernel. It also satisfies
the existing requirement of `geocedg/specs/ui/g9u1-construction-interaction.md`
§9: repeated English/Spanish switching must leave help and UI state current
without stale strings.

## 11. Correction

`source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java`,
`chooseLanguage()`:

```java
if (choice != null) {
    // PRE-G9B-R5-B-R1: the host language change, not the bare locale setter, so that
    // labels, Input Help and the command tables change language together. The
    // document content does not change, so an unmodified document stays saved.
    boolean saved = app.isSaved();
    app.setLanguage("Espa\u00f1ol".equals(choice) ? new Locale("es") : Locale.ENGLISH);
    if (saved) {
        app.setSaved();
    }
}
```

The `AppGeoCeDG.setLocale` EN/ES product-language policy still applies: `setLanguage`
calls it. Choosing the current language is a no-op. Observable effects are those of
the host language change:
- every view, dialog and Input Help label, and the command tables, switch at once;
- the construction is recomputed in the new language, with XML, values and
  dependencies verified unchanged;
- the active tool returns to Move, as with the host language menu.

The document's saved state is explicitly preserved, because a language change
alters no document content. No undo point is stored.

## 12. Changed paths

| Path | Change |
|---|---|
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java` | the correction (GeoCeDG-owned) |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR5BR1RuntimeLanguageSwitchTest.java` | new regression (4 tests) |
| `docs/upstream/modified-files.yml` | new test entry; phase-tagged purpose on the action registry entry |
| `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json` | catalog data from the official updater (§16) |
| `docs/roadmap/geocedg_roadmap.md` | status only: version 4.28; initial candidate disposition and this descendant |
| this report and its evidence mirror | new |

No other product, kernel, shared, parser, lookup, serialization, guide, prompt, ADR
or governance file changes. The initial candidate's report and evidence are not
edited.

## 13. Upstream provenance

No upstream-owned file is newly modified, and no already-recorded R5-B upstream
entry is rewritten. `GeoCeDGActionRegistry.java` is GeoCeDG-owned (`change: added`);
its purpose gains a `PRE-G9B-R5-B-R1` sentence. The new test is registered as
`added`. `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b…` reports
`Controlled upstream boundary: 857 registered file(s).`

## 14. Regression tests

`PreG9BR5BR1RuntimeLanguageSwitchTest`, Desktop. It uses an embedded application
started in a product language as `--language` does, and the real
`settings.product-language` action with only its chooser answered:

| Test | Covers |
|---|---|
| `englishToSpanishRebuildsInputHelpAndAliasesForSpanish` | see below |
| `spanishToEnglishRebuildsInputHelpAndAliasesForEnglish` | see below |
| `englishSpanishEnglishAndBackStayCoherentInOneSession` | see below |
| `switchingLanguageKeepsTheConstructionAndItsCanonicalPresentation` | see below |

The first three tests switch EN → ES, ES → EN and EN → ES → EN → ES with an existing
construction. After each switch they check:

- **First-use lookup (`T-INPUT`).** The new-language alias resolves before anything
  else runs, and the old one no longer does.
- **Input Help captions.** Every caption and label is from the current language.
- **Input Help entries (`T-DISPLAY`).** The *All Commands* entries equal the
  current offered entries: identity, canonical head and current alias.
- **Focus and syntax (`T-SYNTAX`, `T-GEOCEDG`).** Focus by identity, selection and
  the syntax pane are correct for `OrthogonalLine`, `Circle`, `Mirror`, `LocusV2`,
  `LocusLength`, `SplineV2` and `Length`. Each shows the canonical head with
  placeholders of the current language, and never `.Syntax`.
- **Autocomplete (`T-AUTOCOMPLETE`).** It finds Spanish aliases only in Spanish and
  inserts canonical heads.
- **Construction.** The XML and the displayed definition stay unchanged.

The fourth test takes the 42-object R5-B witness construction through ES, EN and ES
and checks:

- **XML and saved state.** The XML is byte-identical, and the document stays saved.
- **Definitions (`T-DISPLAY`, `T-GEOCEDG`).** The displayed and editable
  definitions are canonical. Only the localized function name changes (`sin`/`sen`).
- **Reverse table (`T-INPUT`).** It equals the `P_DOTXML` fixture table of that
  language.
- **Aliases and placeholders (`T-L01`).** `LocusV2` and `LocusLength` aliases and
  placeholders are those of the current language.
- **Errors (`T-ERROR`).** The canonical head appears with that language's prose,
  matching the base fixture.
- **Dynamic text (`T-DYNTEXT`).** The classification is unchanged.
- **Undo.** No undo point is stored, and the history size is unchanged.
- **Shadow guard (`T-SHADOW`).** After `Circle=5`, it still raises
  `CANONICAL_HEAD_SHADOWED`.

| Run | Result |
|---|---|
| on the unchanged `T_R5B` source | 3 failures (first-use lookup), 1 pass |
| scratch variant with `T_R5B`'s action body | 3 failures (Input Help captions), 1 pass |
| with the correction | 4 passed |

The original R5-B tests are unchanged.

## 15. Preserved R5-B invariants and original regressions

`E(k)` authority, `USER`/`SCRIPT`/`XML` lookup, reverse-table semantics, the parser,
serialization and the command identifiers are untouched: the correction is a
GeoCeDG Desktop action change and no shared file changes. Localized aliases,
canonical English presentation, the typed `CANONICAL_HEAD_SHADOWED` outcome and
identity-carrying autocomplete are preserved, and are re-asserted across the switch
by §14.

Complete Desktop suite on the corrected tree: **1 634 tests, 0 failures,
0 errors, 1 skipped**. This is 1 630 at `T_R5B` plus the 4 new tests. It includes,
unchanged:
- all original R5-B classes: gate, display, re-entry (`T-SHADOW`), help and script;
- the adjacent `PreG9BR4SplineV2AuthoringTest`, `G9U1MetricReviewTest`,
  `G9U1ProductPolicyTest` and `PostP1BilingualUserGuideTest`;
- `DotXmlLocaleIndependentPersistenceTest`;
- `G9U0LocalizationHelpTest.l01CommandNamesAndSyntaxAreLocalizedInEnglishAndSpanish`.

The shared modules are byte-unchanged from `T_R5B`. Their complete `final.shared`
selection runs again in the `INTEGRATION` campaign of the descendant.

## 16. JUnit inventory and registry pin

The 4 new Desktop tests change `discovery.desktop` and `final.desktop` only. No
phase selection filter matches the new class, and no shared test changed. The
official updater regenerated the inventory from real evidence:
- `discovery.shared` and `discovery.desktop` dry runs;
- an executed, passing `final.desktop` run: 1 628 tests, 0 failures, 0 errors,
  1 allowlisted skip.

`final.shared` is unchanged. The registry `junit_inventory` pin was reproduced for
the parent inventory (`cc5ab6c7…`) and then recomputed with
`Get-VerificationCanonicalTextSha256`.

| Record | Before | After |
|---|---|---|
| `discovery.desktop` identities | 1 630, `2ff7a5be…` | 1 634, `9dbc37a2318e7b11802616de45766a34d84ab2cc176e3dd4d25563574bcf7aab` |
| `final.desktop` identities | 1 624, `2f7fab36…` | 1 628, `2f52a04499eebcfaf38a038bc50b8616fb8b9d3400e8df60e80e39b70486a841` |
| module discovery-evidence digests (shared, desktop) | `9e210ea4…`, `6d22a123…` | `53300434…`, `b05567b7…` (identities unchanged for shared) |
| registry `junit_inventory` pin | `cc5ab6c77de6486c64e099b6d50c4b48bde9f7b2a986a83548e4105025fa909a` | `ff4f70b53ccfca49b08dff23daceb2d9c4128d51849eb8ea902a617dbbb4b3fc` |

Every other selection is unchanged. No hash was edited by hand.

## 17. Retained observations and debt

| Identifier | Disposition |
|---|---|
| `OBS-R5B-R1-NATIVE-AWT-CRASH-UNATTRIBUTED` | the author's native `awt.dll` access violation (§4) is not reproduced by five automated windowed runs, including the author's runtime and a 16-switch stress run; not attributed, not claimed fixed; the author smoke (§20) watches for it |
| `OBS-R5B-R1-SYNTHETIC-INPUT` | the windowed probes dispatch events to Swing components; operating-system input, focus and IME paths were not driven |
| `OBS-R5B-R1-LANGUAGE-CHANGE-RETURNS-TO-MOVE` | changing the product language now returns the active tool to Move, as the host language change does |
| `OBS-R5B-R1-CAS-GROUP-READINESS` | the Input Help CAS group appears only if CAS is initialized when the tree is rebuilt; pre-existing and identical at `P_DOTXML` |
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | retained unchanged |
| R5-B observations (`HZ-R5B-ALIAS-CAS-STATE`, `OBS-R5B-CAS-TABLE-VERBATIM-HEADS`, `OBS-R5B-HARDCODED-SYNTAX-HEADS`, `OBS-R5B-PROVER-DETAILS-CAPTIONS`, `OBS-R5B-INPUT-BAR-INSERTED-TEXT`, `HZ-R5B-HARNESS-TABLE-FRESHNESS`) | carried forward unchanged |

## 18. Verification plan

Pre-candidate checks on the staged tree. These are development diagnostics, not
acceptance:

| Check | Result |
|---|---|
| Checkstyle `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 findings (505 and 124 files) |
| `Assert-GeoCeDGUpstreamBoundary` | 857 registered files, no unregistered path |
| `git diff --cached --check` | clean; new files LF; mixed-EOL files keep their bytes, new lines LF |
| `verify.ps1 -PlanOnly` | `INTEGRATION` `4502c4bd…`, `STATIC` `25dfacc8…`, `FINAL` `3d8752b9…`: unchanged from `T_R5B` |
| `verify.ps1 -Profile INFRA_UNIT` | `ACCEPTED / COMPLETE`, 22/22, 0 diagnostics (`verification-62515cfa30354d36967a485ea37f7f66`) |
| complete Desktop suite; executed `final.desktop` producer | 1 634 / 0 failures / 1 skipped; 1 628 / 0 failures / 1 skipped |

`INTEGRATION` runs fresh on the exact frozen descendant, because product code
changed after the initial campaign. The parent's `INTEGRATION` is not reused.
`STATIC` is not required: no guide, prompt, ADR or specification changes.
`INTEGRATION` already executes the documentation, style and whitespace diagnostics
over the changed records. The `STATIC`-only leaves (`prompt.execution-safety`,
`diagnostic.guide-structure`) read inputs this descendant does not change. FINAL is
not required by the frozen class and is not requested.

## 19. Impact declarations

```text
GUIDE_IMPACT = NONE
Rationale: the guides already state that the product language changes menus,
  dialogs and in-application help; the correction makes that documented runtime
  behaviour reliable and changes no policy.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, packaging,
  download or build/verification entry point changed.

VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY
Rationale: the JUnit inventory and its registry pin were regenerated through the
  official updater for the new Desktop tests; no verifier, profile, leaf, schema or
  phase selection changed.

PRODUCT_PHASE_EFFECT = BOUNDED — the GeoCeDG product-language action uses the
  host runtime language change; R5-B command policy unchanged
```

## 20. Focused author smoke

Runtime language lifecycle, one session, no restart:

1. Start in **English**, type `c=Circle((0,0),2)`, open Input Help and select
   `Circle` and `PerpendicularLine`: English placeholders.
2. **Options → GeoCeDG options → Product language… → Español**. Immediately:
   - the Input Help title, captions (*Todos los comandos*, *Funciones Matemáticas*,
     *Geometría*…), **Pega** and **Ayuda en línea** are Spanish;
   - selecting `Circle` shows `Circle( <Punto>, <Número o valor numérico (radio)> )`;
   - typing `Circunf` in the input bar suggests `Circle( <Punto>, … )`;
   - `k=Circunferencia((1,1),1)` is accepted and shown as `Circle(...)`.
3. **Product language… → English**: Input Help and the views are English again;
   `Circunferencia(...)` is no longer a known command; selecting
   `PerpendicularLine` shows `<Point>, <Line>`.
4. **Product language… → Español** again, then repeat step 2 briefly. Watch for any
   freeze or crash; if one happens, keep the `hs_err_pid*.log` and the console
   output.

R5-B spot checks:
5. `LugarGeom` in the input bar suggests `LocusV2( <Punto dependiente>, … )`.
6. Redefine `c` after `Circle=5` without editing the command: refused with
   `CANONICAL_HEAD_SHADOWED`; nothing changes.
7. Save as `.cedg`, reopen in English and in Spanish: same objects and values.

## 21. State

```text
INITIAL T_R5B          = TECHNICALLY ACCEPTED
                         SUPERSEDED BY CORRECTIVE DESCENDANT IF R1 SUCCEEDS
                         NOT AUTHOR APPROVED
PRE-G9B-R5-B-R1        = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTHOR SMOKE           = PENDING
AUTHOR APPROVAL        = PENDING
promotion              = NONE
PRE-G9B-R6             = NOT AUTHORIZED
PRE-G9B-R7             = NOT AUTHORIZED
G9B                    = NOT AUTHORIZED
```

Push, branch publication, merge, promotion, tag, release and binary publication
each require a separate explicit author instruction naming the exact candidate SHA.
