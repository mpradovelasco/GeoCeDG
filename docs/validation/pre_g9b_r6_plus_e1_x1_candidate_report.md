# PRE-G9B-R6-plus-E1-X1 technical candidate report

```text
TECHNICAL_CANDIDATE_STATE  = FROZEN with the candidate commit that contains this report
AUTHOR_DECISION            = NOT_RECORDED_IN_THIS_ARTIFACT

BASE                       = b3c78e6d6564b0400d1fea7cda55bb480778716c
SELECTED DESIGN            = CANDIDATE C — PRIMARY LAUNCHER EARLY CLASSIC DISPATCH
VERIFICATION_CLASS         = BOUNDED_PHASE (frozen by the author)
PRODUCT CHANGE             = YES — DIAGNOSTIC LAUNCH ROUTE
PACKAGING PRODUCT CHANGE   = NONE
SERIALIZATION CHANGE       = NONE
GEOMETRIC SEMANTICS CHANGE = NONE
DOCUMENT FORMAT CHANGE     = NONE
KERNEL CHANGE              = NONE
AUTHOR_SMOKE               = REQUIRED
selfApproved               = false
passClaimed                = false
```

This report records the technical candidate of `PRE-G9B-R6-plus-E1-X1`, the
correction of `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` authorized by
the author on 2026-10-07
([authorization record](pre_g9b_r6_plus_e1_x1_authorization_record.md)). The
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher.prompt.md)
is the contract. Its machine-readable mirror is
[`pre-g9b-r6-plus-e1-x1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-x1-candidate-evidence.json).
The candidate commit cannot name itself; its identity and the registered
`PHASE` run on it are reported outside this artifact.

## 1. Entry gate and history

```text
live remote main = local main = origin/main = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b
characterization = fdc1ade6 → b6172af4f319d297aa5780c9f516b310254d3e90 (E1-X1: Candidate A preferred then)
                            → b3c78e6d6564b0400d1fea7cda55bb480778716c (E1-X1-R1: Candidate C demonstrated)
                   linear; neither commit amended
branch           = phase/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher (local)
commits          = 51bb677e8b61c149dd3ae1e1c6a491e53732d5dc  authorization record, prompt AUTHORIZED
                   7fd6adbbdc6dcf7cc34b795c0225522744ff7397  implementation (tree fa259cd1377ee3f1672d7489df083dd1f6ec8835)
                   candidate                                  this report, evidence, roadmap, mini-track
```

`E1`, `E1-L` and `E1-P` stay `PASS — AUTHOR APPROVED — PUBLISHED`. Candidate A
is not implemented.

## 2. Changed production files

| File | Change |
|---|---|
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDG.java` | +22: the first-position dispatch before any GeoCeDG state |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java` | +18/−22: `openDiagnostic` keeps the chooser and confirmation and calls `launchDiagnostic`; the old `java.home` command is removed |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGClassicDiagnosticLaunch.java` | new (238 lines): environment, packaged and development commands, module-access filter, discarded streams, dispatch arguments, preference files, resource rule |

Supporting files: the two new test classes and two adapted
`G9U1ActionRegistryTest` cases; `geocedg/specs/ui/application-profile.md` (one
paragraph); `docs/upstream/modified-files.yml` (two purposes extended, three
entries added); the phase registration (§9). No packaging file, kernel, shared
module, serialization, document-format or upstream file changes.

## 3. Dispatch contract

`GeoCeDG.main` first tests `GeoCeDGClassicDiagnosticLaunch.isDispatch(args)`:
true only when the first argument is exactly `--classic-diagnostic`. The branch
then computes the isolated `classic-diagnostic.properties` beside the GeoCeDG
preference file, creates its directory, and calls the unchanged
`GeoGebra3D.main(classicDispatchArguments(args, preferences))`, then returns.
`classicDispatchArguments` removes only the marker and keeps every other
argument in order; it adds `--showSplash=false` and `--settingsfile=<isolated
Classic preferences>` only when the caller gave no splash or settings argument
(`DQ-E1X1-4`, narrow reading of the authorization record). `GeoCeDG` has no
static initializer, so nothing of GeoCeDG runs before the branch. The marker in
any other position, with another case or as part of a file name is not a
dispatch; the `.cedg` association passes an absolute document path first.

## 4. Packaged and development branching

```text
jpackage.app-path present (any value)  PACKAGED
    [<app-path>, --classic-diagnostic, --showSplash=false, --settingsfile=<route file>, <resource>?]
    blank, invalid, missing or non-regular path → IOException "Packaged GeoCeDG launcher is
    missing: …"; nothing started; never java.home, never PATH
jpackage.app-path absent               DEVELOPMENT
    [<java.home>\bin\javaw.exe | java.exe | java, <module-access options>, -cp, <java.class.path>,
     org.geogebra.desktop.GeoGebra3D, --showSplash=false, --settingsfile=<route file>, <resource>?]
    no launcher in java.home\bin → IOException
```

The resource is validated before either branch builds a command; the active
document has no input path into the command.

## 5. JVM-option filtering

The development child receives only the `--add-exports`, `--add-opens` and
`--enable-native-access` entries of the running JVM's
`RuntimeMXBean.getInputArguments()`, in order, in `=` form or as a family token
followed by its value. Agents (`-javaagent`, `-agentlib`), heap and thread
sizing (`-X…`, `-XX:…`), every `-D` property, `-ea`, `--add-modules`,
`--patch-module`, `--add-reads` and look-alikes such as `--add-exportsX` are not
forwarded (tested). The running JVM is the single authority: under Gradle it
carries `desktopJvmArgs`; a packaged child takes the options from `GeoCeDG.cfg`
because it is started through `GeoCeDG.exe`.

## 6. Stream policy

`processBuilder` sets `redirectOutput(Redirect.DISCARD)` and
`redirectError(Redirect.DISCARD)`; `redirectErrorStream` stays false. No pipe,
no `inheritIO`, no log file. A mutation of the candidate that returns a plain
`ProcessBuilder` (default pipes) makes the process test fail: no Laboratory
report for `Templatev7.ggb` within 150 s. The source was restored byte for byte.

## 7. Tests

| Class | Cases | Coverage of the contract (§17/§18 of the authorization) |
|---|---|---|
| `PreG9BR6PlusE1X1DiagnosticLaunchTest` | 17 | marker admissibility (1), marker removal and kept arguments (2), normal invocation not a dispatch (3), packaged command from `jpackage.app-path` (4), invalid packaged paths fail closed (5), no `java.home` fallback (6), development command and launcher order (7), module-access filter (8), unrelated VM arguments dropped (9), stdout discarded (10), stderr discarded (11), Classic preferences isolated (12), Laboratory preferences isolated (13), `.ggb` forwarded (14), `.ggt` forwarded (15), invalid, missing, directory and extension-less resources rejected (16), active document never forwarded (17) |
| `PreG9BR6PlusE1X1DiagnosticProcessTest` | 2 | real JVMs, no pipes, scratch `APPDATA`, every started process destroyed: the real `GeoCeDG.main` with the marker is genuine Classic (`App3D`, `AppConfigDefault`/`classic`, `GuiManager3D`, `GeoGebraFrame3D`, no `AppGeoCeDG`, `GeoCeDGFrame`, `GeoCeDGProfile` or `GeoCeDGActionRegistry` loaded), with the isolated Classic preferences, closes by itself with exit code 0 and never creates `preferences.properties`; a real GeoCeDG parent starts the Laboratory with `Templatev7.ggb` (23 steps, no blocking) and Open Classic through the real action, both as separate processes whose parent is the GeoCeDG JVM, the Laboratory child closes by itself while the parent survives, the parent survives with construction, saved state, current document and `preferences.properties` unchanged, the three preference stores are distinct, and the Open Classic child survives the parent's exit |
| `G9U1ActionRegistryTest` | 14 (2 adapted) | the two former `diagnosticCommand` cases now call the development command with the same assertions |

Evidence printed by the process test (one run):

```text
dispatch: test JVM 18336, Classic JVM 22268 (App3D, AppConfigDefault, classic-diagnostic.properties)
parent 20476 (AppGeoCeDG): Laboratory child 11364 (Templatev7.ggb, 23 steps, laboratory.properties,
  exited by itself), Open Classic child 30556 (no current file, classic-diagnostic.properties,
  alive after the parent's exit); constructionUnchanged, savedStateUnchanged,
  currentFileUnchanged, mainPreferencesUnchanged = true
```

## 8. Process, preference and Laboratory evidence summary

- Separate processes: every Classic child PID differs from the GeoCeDG JVM PID,
  in the process tests and in the packaged smoke.
- Genuine Classic: `App3D`, `AppConfigDefault` (`classic`), `GuiManager3D`,
  `GeoGebraFrame3D`, Classic menus and AppUserModelID `geogebra.AppId` (GeoCeDG:
  `org.geocedg.desktop`); none of `AppGeoCeDG`, `GeoCeDGFrame`, `GeoCeDGProfile`
  or `GeoCeDGActionRegistry` is loaded in a Classic JVM. Compared with the R1
  prototype child, the packaged child of the implementation loads exactly one
  more `org.geocedg` class, `GeoCeDGClassicDiagnosticLaunch` (43 against 42).
- Preferences: `classic-diagnostic.properties` and `laboratory.properties`
  beside `preferences.properties`, which stays byte-identical while children
  start and exit.
- Laboratory: `.ggb` and `.ggt` forwarded and opened; `.txt`, missing files,
  directories and extension-less files rejected before launch with the existing
  message; no canonical-legacy trust.
- Document isolation: construction XML and saved state unchanged in every route
  invocation of the process tests and the packaged smoke. The non-reproduced
  full-XML observation of R1 did not reappear.

## 9. Registration

- Phase selection `PRE-G9B-R6-PLUS-E1-X1`: `compile.shared.semantic`,
  `compile.desktop.semantic`, `junit.desktop.pre-g9b-r6-plus-e1-x1.semantic`
  (the `PRE-G9B-R6-PLUS-C-X1` shape); producer and projection nodes.
- JUnit selection `pre-g9b-r6-plus-e1-x1.desktop` (43 identities:
  `PreG9BR6PlusE1X1DiagnosticLaunchTest`, `PreG9BR6PlusE1X1DiagnosticProcessTest`,
  `G9U1ActionRegistryTest`, `PreG9BR6PlusE1LBundledCatalogTest`), input identity
  `51bb677e`.
- Official updater: discovery dry runs of both modules (Desktop 1861 → 1880,
  shared count unchanged) and the executed passing selection; registry pin
  `junit_inventory` `3a63be8b…` (reproduced from the HEAD blob) →
  `5de49549dd9d82b81ce75ff60b742e6e49c840231dfe627e5d67355edf7e5a55`;
  registry-shape pins: 41 inventory selections, 49 PHASE selections, the new
  selection count 43.
- `final.desktop` executed: 1 874 tests, one failure outside this delta (§12);
  the official updater refuses failing evidence, so the `final.desktop`
  inventory stays at its base values. No hash was entered by hand.
- No verifier, check, schema, profile or static-contract change beyond this
  normal phase registration.

## 10. Packaged technical smoke

Artifacts were built from a clean scratch worktree of the implementation commit
`7fd6adbb` with the **unmodified** builder (`-Target All`, `INTERNAL` and `NC`;
manifest `source_revision = 7fd6adbb…`), through a `subst` drive because the
scratch path exceeds WiX's `MAX_PATH`. The candidate commit differs from
`7fd6adbb` only by this report, its evidence JSON, the roadmap and the
mini-track plan. The scratch javaagent of the characterization drove the real
actions; every launch used a scratch `APPDATA` and settings file.

| Environment | Open Classic | Laboratory `.ggb` (Templatev7) | Laboratory `.ggt` | invalid / missing | launcher | isolated prefs | no `java.exe` |
|---|---|---|---|---|---|---|---|
| `INTERNAL` app-image | Classic, separate PID, 3D view without exception | 23 steps, 24 macros, no blocking | 1 macro | rejected before launch | `GeoCeDG.exe --classic-diagnostic` | yes | yes |
| `INTERNAL` ZIP (extracted) | yes | yes | yes | — | same | yes | yes |
| `NC` app-image | yes | yes | yes | rejected before launch | same | yes | yes |
| `NC` ZIP (extracted) | yes | yes | yes | — | same | yes | yes |

In the `INTERNAL` app-image the children of Open Classic and of the Laboratory
closed by themselves while the parent stayed responsive with its construction,
saved state and `preferences.properties` unchanged, and an Open Classic child
survived its parent's exit (then closed by the test). The app-image and ZIP
trees were byte-identical before and after the launches; the author's
`%APPDATA%\GeoCeDG\5.4` was unchanged; no installer was run.

## 11. Packaging invariance

| Item | Result |
|---|---|
| packaging contract (`packaging-product.ps1 -RequireArtifacts`, unmodified) | `CONTRACT_SATISFIED` 42/42 for `INTERNAL` and for `NC` |
| app-image inventory against `E1-P` | 432 files with the same names; `GeoCeDG.exe`, `app\GeoCeDG.cfg`, `app\.jpackage.xml` byte-identical; one `.exe`, one launcher `.cfg` |
| differing app-image files | `desktop.jar` (`GeoCeDG.class`, `GeoCeDGActionRegistry.class`, the two new `GeoCeDGClassicDiagnosticLaunch` classes, build provenance, one schema resource differing only by carriage returns of the scratch checkout), `common-jre.jar` (three command property files differing only by carriage returns), `legal\resolved-runtime-components.json` |
| MSI / EXE | 432 files; `GeoCeDG.exe`, `GeoCeDG.cfg`, `jvm.cfg`; shortcuts `GeoCeDG` desktop and Start menu only; EXE-embedded MSI at offset 568 076 with the same shape |
| file association | `.cedg` open verb on `GeoCeDG.exe`, unchanged |
| SBOM | 117 components, identity set identical to `E1-P` |
| `NC` against `INTERNAL` | distribution notice and launcher version resource only, as designed |
| `package.yml`, builder, packaging checks, ADR 0004 | unchanged |

## 12. Observations

- **Pre-existing failure outside the delta.**
  `PreG9BR6PlusE1LCuratedLibraryTest.e1l09NoPackagingRouteNoBuildChangeAndUnchangedActionCatalog`
  asserts (line 355) that `tools/release/build-windows-package.ps1` does not
  contain `ggt-library`; `E1-P` (`dcda4075`) added it. The test file is
  identical at `fdc1ade6` and in the candidate, the builder contains the string
  at `fdc1ade6` and not at `47b39e5f` (`E1-L` closeout). It is outside every
  `PHASE` selection and fails only in `final.desktop`. Recorded as
  `OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION` for author disposition; not
  phase scope, not fixed.
- **Scratch build location.** WiX `WIX0001` (`0x80070003`) for payload paths
  longer than `MAX_PATH` under the scratchpad; resolved with `subst`, not a
  product issue.
- **jpackage launcher.** A `GeoCeDG.exe` started by a test harness may host its
  JVM in a second `GeoCeDG.exe` process; the separate-process property holds
  either way.
- **Concurrent sessions of one route** still share that route's preference file
  (pre-existing, unchanged).

## 13. Verification

| Check | Result |
|---|---|
| focused unit tests | 17/17 |
| process tests | 2/2 (mutation with default pipes: fails as expected) |
| executed selection `pre-g9b-r6-plus-e1-x1.desktop` | 43/43 passing |
| `final.desktop` executed | 1 874 tests, 1 pre-existing failure (§12) |
| Checkstyle main and test | no finding in the touched files |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b…` | passed, 973 registered files |
| development `INFRA_UNIT` (staged tree) | `verification-701a3e0a393f4cbf80b4573834d016ad`, `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -PlanOnly` | `COMPLETE`, plan `59ec02958e51287e927a7088d2db0f62a21493e0963b0ddb3eb05703e2595a74` |
| registered `PHASE` on the candidate | run after freezing; reported outside this artifact |

## 14. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain or environment contract
changes; the packaged route uses the existing launcher and the development route
the running JDK.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL (phase registration only: one
PHASE selection, one JUnit selection, inventory and registry pins, registry-shape
pins)
GUIDE_IMPACT = NONE (the guides describe a separate Classic process with isolated
preferences, which stays true)
SERIALIZATION CHANGE = NONE
PACKAGING PRODUCT CHANGE = NONE
```

## 15. Author-smoke checklist

From the portable ZIP and from an installation **in a test location or after
the author's own backup** (this activity did not install anything):

1. **File → Open Classic diagnostic session** opens a separate Classic window
   (title "GeoGebra Classic 5"), without the upstream splash.
2. **Automation → Legacy laboratory** with `Templatev7.ggb` (or another large
   legacy model) opens it in Classic without hanging; the same with a `.ggt`.
3. An invalid or missing file is rejected with the existing message and starts
   nothing.
4. The GeoCeDG document, its title and its saved state are unchanged.
5. Closing Classic leaves GeoCeDG open; closing GeoCeDG leaves Classic open.
6. Classic settings changed in the diagnostic session do not appear in
   GeoCeDG, and the Laboratory keeps its own.
7. No new Start-menu or desktop entry appears after installation.
