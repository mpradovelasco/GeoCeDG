# PRE-G9B-R6-plus-E1-X1-R1 micro-characterization report: primary launcher versus jpackage secondary launcher

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (focal continuation of E1-X1)
TECHNICAL_CANDIDATE_STATE = FROZEN with the documentary R1 candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

OBSERVATION               = OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER
ACTIVITY                  = PRE-G9B-R6-plus-E1-X1-R1 (named by the author instruction)
PROMPT STATE              = PREPARED — NOT AUTHORIZED (reconciled to Candidate C)
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
PACKAGING CHANGE          = NONE
SERIALIZATION CHANGE      = NONE
```

This report is an addendum. It does not rewrite the first characterization,
which stays the record of what was known then: the
[E1-X1 characterization report](pre_g9b_r6_plus_e1_x1_preparation_characterization_report.md)
recommended Candidate A (a jpackage additional launcher) on the evidence then
available and evaluated Candidate C only statically. R1 tests C with a scratch
prototype and compares A and C directly. The canonical
[`E1-X1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher.prompt.md)
is reconciled to the result; this report holds the evidence and carries no
authority of its own. Its machine-readable mirror is
[`pre-g9b-r6-plus-e1-x1-r1-launcher-comparison.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-x1-r1-launcher-comparison.json).

## 0. Inputs

The author's instruction of 2026-10-07 is the authority of R1: characterization
and design reconciliation only; a scratch prototype of Candidate C allowed
(temporary worktree, uncommitted patch, scratch builds and packages); no
production commit of the prototype; only documents, evidence and the revised
`PREPARED` prompt may be tracked; no push, merge, tag, release, fix or `E2`.

`E1-L`, `E1-P` and `E1` stay `PASS — AUTHOR APPROVED — PUBLISHED`; `E1` is not
reopened. The first-characterization findings stay valid and are not
re-derived: `X1-F1` (the jpackage runtime has no `java.exe`/`javaw.exe`),
`X1-F2` (undrained child pipes can block Classic start-up), `X1-F3` (the
development child does not receive the product JVM options), and the proven
preference isolation.

## 1. Entry gate

```text
live remote main         = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b   (git ls-remote)
local main = origin/main = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b
T_R6PLUS_E1_X1_CHAR      = b6172af4f319d297aa5780c9f516b310254d3e90
                           tree   0f98fe7a091c45c89ad97092215dbdfcfadecfab
                           parent fdc1ade6… (single commit, linear)
branch                   = phase/pre-g9b-r6-plus-e1-x1-prompt (local; never pushed)
worktree                 = clean
```

The characterization commit was present and unchanged and is the documentary
starting point of R1.

## 2. Method

- **Scratch prototype.** A temporary detached `git worktree` of `b6172af4` in
  the session scratchpad, with an uncommitted patch of two GeoCeDG-owned
  Desktop files (`GeoCeDG.java` +8 lines, `GeoCeDGActionRegistry.java` +56/−4;
  patch SHA-256 prefix `77A41EDF116A6F43`). The patch contained, besides the
  candidate behavior, scratch-only selectors (`x1.streams`, `x1.jvmoptions`)
  used to compare stream and JVM-option policies. The worktree was removed
  with `git worktree remove --force` after the runs; the production tree was
  never edited.
- **Packages.** The **unmodified** builder `tools/release/build-windows-package.ps1
  -Target All` produced complete `INTERNAL` and `NC` sets (app-image, ZIP, MSI,
  EXE, SBOM, manifest, hashes; `source_revision = b6172af4`) from the patched
  worktree. The worktree path inside the scratchpad exceeded WiX's `MAX_PATH`
  (a 290-character payload path; `WIX0001 … 0x80070003`), so the worktree
  parent was mapped with `subst W:` for the duration of the builds and the
  mapping was removed afterwards. This is an artifact of the test location,
  not of the candidate.
- **Packaging contract.** The **unmodified** `tools/agent/checks/packaging-product.ps1
  -RequireArtifacts` on both sets.
- **Probe.** The scratch javaagent of the first characterization, extended to
  record the active `AppConfig`, the GUI manager, menu titles, the Windows
  AppUserModelID, every loaded `org.geocedg.*` class, standard-error volume,
  uncaught exceptions, the 3D view, the active-document full XML, construction
  XML and saved state before and after each route, the main and diagnostic
  preference-file digests, and child self-exit. Packaged runs attach it through
  `JAVA_TOOL_OPTIONS` to the unmodified launchers; development runs through a
  Gradle init script on `:desktop:desktop:runGeoCeDG` of the worktree.
- **Isolation.** Every run used a scratch `APPDATA`; the author's
  `%APPDATA%\GeoCeDG\5.4` digest was identical before and after. No installer
  was run.
- **Totals.** 19 probed GeoCeDG processes, 48 route invocations, 40 Classic
  child reports.

## 3. C-Q1 — early dispatch

The prototype branch is the first statement of `GeoCeDG.main`, before
`CommandLineArguments`, preference selection or `GeoGebra.doMain`:

```java
if (effectiveArguments.length > 0
        && CLASSIC_DIAGNOSTIC_ARGUMENT.equals(effectiveArguments[0])) {
    org.geogebra.desktop.GeoGebra3D.main(
            Arrays.copyOfRange(effectiveArguments, 1, effectiveArguments.length));
    return;
}
```

- **No GeoCeDG state before the branch.** `javap -p` of `GeoCeDG.class` at the
  base and in the prototype shows no static initializer: its only static fields
  are compile-time constants. The `GeoCeDGFrame`, `GeoCeDGBrandingResource` and
  preference code are reached only after the branch.
- **Only the private argument is removed.** The child command line is
  `GeoCeDG.exe --classic-diagnostic --showSplash=false --settingsfile=<file>
  [resource]`; `sun.java.command` in the child confirms that Classic received
  exactly the remaining arguments.
- **No extra GeoCeDG classes.** The set of loaded `org.geocedg.*` classes when
  the Classic window is ready is identical (42 classes, no difference) between
  a Candidate C packaged child (`GeoCeDG.main` → dispatch) and a development
  child started directly on `GeoGebra3D`. The shared classes come from GeoCeDG
  seams inside the shared kernel and upstream Desktop code and from the probe's
  own references; none is `GeoCeDGFrame`, `AppGeoCeDG`, `GeoCeDGProfile`,
  `GeoCeDGActionRegistry` or a workspace class.
- **Normal start-up unaffected.** All 19 parents started normally through the
  same patched `GeoCeDG.main` (`AppGeoCeDG`, `GeoCeDGFrame`,
  `AppConfigGeoCeDG`, AppUserModelID `org.geocedg.desktop`).

## 4. C-Q2 — true Classic identity

Every Candidate C child (packaged `INTERNAL`, `NC`, ZIP, and development):
`App3D`, `GeoGebraFrame3D`, `GuiManager3D`, active config `AppConfigDefault`
with app code `classic`, Classic menu bar `File | Edit | View | Options | Tools |
Window | Help`, window title `GeoGebra Classic 5` (or the opened file name),
Windows AppUserModelID `geogebra.AppId` (the parent's is
`org.geocedg.desktop`, so the taskbar keeps the two identities apart), 3D view
shown without an uncaught exception. No GeoCeDG frame, action catalog,
workspace or profile is present.

## 5. C-Q3 — separate process

- **Distinct processes.** Every child PID differs from the parent JVM PID and
  names the parent JVM as its parent (for example parent 27672, children 24340,
  30352, 23284). No in-process profile switch occurs.
- **Child closes alone.** With the child calling Classic's own `exit()`, the
  parent observed `22156` and `30532` "exited by itself (not destroyed by the
  parent)"; the parent stayed responsive with its document, construction and
  saved state unchanged and its `preferences.properties` digest unchanged
  (`7cda66e5…/89`) before and after.
- **Parent closes alone.** With the parent halting and not touching its child,
  the Classic child `28452` (`GeoCeDG.exe --classic-diagnostic …`) was still
  running after both the parent JVM and its outer launcher had exited; the test
  then closed it.
- **Launcher observation.** When the test harness started `GeoCeDG.exe`
  directly, the jpackage 25.0.4 Windows launcher hosted the JVM in a second
  `GeoCeDG.exe` process (also without the probe); the `GeoCeDG.exe` started by
  GeoCeDG's `ProcessBuilder` ran the JVM in the started process. Either way the
  Classic JVM is a different process from the GeoCeDG JVM.

## 6. C-Q4 — packaged launcher discovery

`jpackage.app-path` named the actual running `GeoCeDG.exe` in the `INTERNAL` and
`NC` app-images and in both extracted ZIPs (and, as recorded by the first
characterization, in the `E1-P` app-image and ZIP). Candidate C starts exactly
that path; it never derives the executable from `java.home`, the working
directory, `PATH` or the repository. With `jpackage.app-path` set to a missing
file, both routes failed closed with "Packaged GeoCeDG launcher is missing:
<path>" and started nothing. Without the property, the route takes the
development branch.

## 7. C-Q5 — development fallback

The single discriminator is the presence of `jpackage.app-path`, which only a
jpackage launcher sets; no filesystem state is consulted:

```text
PACKAGED     (jpackage.app-path present): [<app-path>, --classic-diagnostic, args…]
                                          missing <app-path> → explicit failure
DEVELOPMENT  (absent): [<java.home>\bin\javaw.exe | java.exe | java,
                        <module-access options of the running JVM>,
                        -cp, <java.class.path>, org.geogebra.desktop.GeoGebra3D, args…]
```

Development with the prototype: Open Classic, `Templatev7.ggb` (23 steps) and
`CirclebyD.ggt` (1 macro) started through `javaw.exe` with the four options in
three separate Gradle runs.

## 8. X1-F2 — child stream policy

| Policy | Packaged result (`Templatev7.ggb`) | Diagnosis value | User state | Bounded? |
|---|---|---|---|---|
| default pipes | **blocks**: child `main` `RUNNABLE` in `FileOutputStream.writeBytes`, window never shown (test-harness start and shell start) | none | none | no |
| `Redirect.DISCARD` | starts (23 steps) in every environment | none | none | **yes** |
| `inheritIO` | starts when GeoCeDG's own handles are drained (harness) or absent (shell start) | output reaches a console only in development | none | no: it hands GeoCeDG's own handles to the child, so a launcher whose pipe is not drained blocks both |
| log file beside the preferences | starts; one merged log | yes | a file under `%APPDATA%\GeoCeDG\5.4` holding user paths and file names; `Redirect.to` truncates per session, so only the last session survives (3 080 bytes after a `.ggt` session followed a `.ggb` one); concurrent sessions of one route share the file | yes, with lifecycle and privacy obligations |

Volumes measured with the development fallback (merged stdout and stderr):
4 824 bytes for an empty Classic session with the 3D view, 11 342 bytes for
`Templatev7.ggb`; standard error alone was 0, 150 and 2 814 bytes for the empty,
`.ggt` and `.ggb` sessions. Both streams can carry kilobytes, so they need the
same treatment. **Recommendation: `Redirect.DISCARD` for stdout and stderr** —
the smallest policy that is reliable regardless of how GeoCeDG itself was
started, creates no file and exposes no data. A diagnostic log is a possible
later enhancement, not needed to fix the defect.

## 9. X1-F3 — development JVM options

| Development child options | Evidence |
|---|---|
| none (current route) | JOGL `GLException: Unable to determine GraphicsConfiguration: WindowsWGLGraphicsConfiguration…` (the issue cited by the upstream comment on `applicationDefaultJvmArgs`), and JNA's restricted-method warnings ("Use --enable-native-access=ALL-UNNAMED … will be blocked in a future release") |
| all four | no warning, no exception, empty standard error for an empty session; 3D view shown |
| all but `--add-exports=java.base/java.lang=ALL-UNNAMED` | no warning or exception observed in the same scenarios |

The three options kept in the last row (the two `java.desktop` exports and
`--enable-native-access`, tested together, not one by one) remove both the JOGL
failure — the failure upstream attributes to the missing exports — and the JNA
warning that announces a future block; the `java.base/java.lang` export was
neither shown necessary nor shown superfluous. No narrower set is proven safe.

There is no runtime-readable single declaration of the list: it exists in
`source/desktop/desktop/build.gradle.kts` (`desktopJvmArgs`, the upstream
`applicationDefaultJvmArgs`) and in `packaging/windows/package.yml`
(`jvm_options`), which the packaged launcher already applies to the Candidate C
child through `GeoCeDG.cfg`. The clean authority for the development child is
the running GeoCeDG JVM itself: the module-access entries (`--add-exports`,
`--add-opens`, `--enable-native-access`) of its
`RuntimeMXBean.getInputArguments()`, which in development are exactly the
options Gradle applied to GeoCeDG. The prototype forwarded them and the child
received the four options with no hard-coded copy. An IDE run without the
options gives a child without them, consistent with its parent.

## 10. Preference isolation and Laboratory

- Open Classic used `<APPDATA>\GeoCeDG\5.4\classic-diagnostic.properties`, the
  Laboratory `laboratory.properties`, in every environment; neither child ever
  used `preferences.properties`, whose digest stayed unchanged while children
  ran and exited (§5). After a Classic and a Laboratory session the directory
  held three distinct files with distinct digests.
- Across 48 route invocations the saved state never changed; the construction
  XML was unchanged in all 40 invocations where it was captured; the full
  application XML was unchanged in 47. The one exception, in the first
  development run (a `.ggt` step, before construction-XML capture was added),
  was not reproduced in seven later development steps (three of them repeating
  the same sequence) nor in any packaged step, and no data path exists from the child process to
  the parent. It is recorded as an unexplained, non-reproduced observation.
- Laboratory resources: `Templatev7.ggb` opened with 23 steps and 24 macros,
  `CirclebyD.ggt` installed one macro, `not-a-model.txt` and a missing `.ggb`
  were rejected before any launch with "Diagnostic resource must be an existing
  GGB/GGT file". The confirmation text is unchanged; no canonical-legacy trust
  is created.

## 11. Packaged matrix (Candidate C prototype)

| Environment | Classic diagnostic | Laboratory | Same launcher | Isolated prefs | No `java.exe` dependency |
|---|---|---|---|---|---|
| Gradle/dev | yes (`javaw.exe`, four options) | `.ggb`, `.ggt` yes | n/a | yes | n/a (development JDK) |
| `INTERNAL` app-image | yes | `.ggb`, `.ggt` yes; invalid and missing rejected | yes (`jpackage.app-path` = `GeoCeDG.exe`) | yes | yes |
| `NC` app-image | yes | `.ggb`, `.ggt` yes; invalid rejected | yes | yes | yes |
| portable ZIP (`INTERNAL`, `NC`, extracted) | yes | `.ggb`, `.ggt` yes | yes | yes | yes |
| MSI / EXE (decompiled) | not installed | not installed | the payload installs the same single `GeoCeDG.exe` | n/a | no `java(w).exe` in the payload |

## 12. Packaging invariance

| Item | Result |
|---|---|
| `package.yml`, `build-windows-package.ps1`, the jpackage invocations | not edited; the unmodified builder produced both sets |
| app-image launcher inventory | 432 files with the same names as the `E1-P` app-image; `GeoCeDG.exe`, `app\GeoCeDG.cfg` and `app\.jpackage.xml` byte-identical; one `.exe`, one `.cfg` |
| differing app-image files | `desktop.jar` (the two prototype classes, build provenance, and `application-profile.schema.json` differing only by carriage returns of the scratch checkout), `common-jre.jar` (three command property files differing only by carriage returns), `legal\resolved-runtime-components.json` (hash records), `runtime\lib\modules` (jlink non-determinism recorded by ADR 0004) |
| MSI / EXE launcher inventory | 432 files; `GeoCeDG.exe`, `GeoCeDG.cfg`, `jvm.cfg`; EXE-embedded MSI at offset 568 076 identical in shape |
| file association | `.cedg` open verb `"[GeoCeDG.exe]" "%1" %*`, unchanged |
| Start-menu and desktop shortcuts | one each, `GeoCeDG` only, unchanged |
| SBOM | 117 components, identical identity set; no launcher component |
| `packaging.launcher-config` and the whole packaging contract | `CONTRACT_SATISFIED`, 42/42 subcontracts, for `INTERNAL` and for `NC`, with the unmodified checker |
| ADR 0004 | unchanged; still one app-image with MSI/EXE derived from it |
| `INTERNAL` versus `NC` | same inventory except the distribution notice and the marker in the launcher's version resources, as designed |

Documentary consequence outside packaging: the Desktop launch contract in
`geocedg/specs/ui/application-profile.md` ("Launcher and configuration") states
what `org.geocedg.desktop.GeoCeDG` does; Candidate C adds one private,
first-position diagnostic dispatch to it, which that specification should
record. No ADR changes: ADR 0001 keeps "an explicit diagnostic route to
Upstream Classic" and the unchanged Classic launch path, and C reuses
`GeoGebra3D.main` unmodified.

## 13. A versus C

| Dimension | Candidate A — add-launcher | Candidate C — primary launcher dispatch |
|---|---|---|
| Separate process | yes | yes (distinct PIDs; independent exit both ways) |
| True Classic entry point | `GeoGebra3D` as launcher main class | `GeoGebra3D.main` reached before any GeoCeDG state; identical loaded-class set to a direct Classic start |
| Packaged runtime compatible | yes | yes (`INTERNAL`, `NC`, ZIP) |
| Dynamic settings path | yes | yes |
| GGB/GGT forwarding | yes | yes |
| JVM options | inherited from the main `.cfg` | inherited from `GeoCeDG.cfg` (same process image) |
| Extra executable | `GeoCeDG-Classic(-Diagnostic).exe` | none |
| Extra `.cfg` | yes | none |
| Installer shortcuts | JDK 25.0.4 two-step build adds Classic desktop and Start-menu shortcuts that cannot be suppressed without a single-step installer | unchanged (GeoCeDG only) |
| ADR 0004 impact | none with accepted shortcuts; amendment if installers become single-step | none |
| Packaging verifier impact | `packaging.launcher-config` must become exact; new launcher subcontract; MSI shortcut pinning; spec and profile/schema changes | none (42/42 unchanged) |
| Product entry-point delta | none | one first-position private argument in `GeoCeDG.main`; the UI specification records it |
| Development fallback | explicit Java fallback | same explicit Java fallback |
| X1-F2 resolution | `DISCARD` (same policy) | `DISCARD` (proven in every environment) |
| X1-F3 resolution | forward options (declared list) | forward the running JVM's module-access options (no duplicate list) |
| Maintenance risk | jpackage per-launcher shortcut behavior; second launcher to keep in profile, builder, verifier, SBOM review | the private argument is part of the product command line (any user can start Classic from a command line with it); relies on `jpackage.app-path` as A does |
| Testability | unit command tests plus packaging contract changes | unit command tests, a dispatch test, a process test; packaging contract unchanged |

Candidate C's residual risk is mitigated in the implementation contract: the
argument is honored only in first position (an Explorer or association start
passes an absolute document path first, never the bare token), and the
dispatch itself guarantees the diagnostic invariants for any caller — it
supplies `--showSplash=false` and the isolated `classic-diagnostic.properties`
when the caller did not provide a settings file, so a manual invocation cannot
reach the upstream splash or default upstream preferences.

## 14. Decision rule

| | Condition | Result |
|---|---|---|
| C1 | dispatch before GeoCeDG profile/frontend initialization | met (§3) |
| C2 | genuine Classic | met (§4) |
| C3 | separate-process semantics | met (§5) |
| C4 | preference isolation | met (§10) |
| C5 | `.ggb`/`.ggt` forwarding | met (§10) |
| C6 | packaged app-image and ZIP | met (§11) |
| C7 | no secondary packaged launcher | met (§12) |
| C8 | no packaging contract or ADR change | met (§12); one UI-specification sentence documents the dispatch |
| C9 | no `runtime\bin\java` dependency | met (§6, §11) |
| C10 | bounded reliable stream policy | met: `DISCARD` (§8) |
| C11 | bounded development fallback | met: `jpackage.app-path` discriminator and mirrored options (§7, §9) |

All eleven conditions are demonstrated. **Recommended: Candidate C — primary
launcher early Classic dispatch.** Candidate A remains the documented
alternative. The recommendation is not an author decision.

## 15. Verification-class reconsideration

With C the implementation is limited to GeoCeDG-owned Desktop launcher and
diagnostic code (`GeoCeDG.main`, `GeoCeDGActionRegistry`), focused unit and
process tests, the UI-specification sentence, and documentation. No
packaging input, packaging check, verifier, registry contract beyond the
ordinary phase registration, schema, kernel or upstream file changes; the
packaging contract was shown to stay satisfied. `BOUNDED_PHASE` (a registered
`PHASE` is sufficient) fits: there is no concrete cross-module integration
obligation that `INTEGRATION` would add, and nothing in the verification
infrastructure changes, so `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` (proposed
for A because A changed the packaging contract) no longer applies. The packaged
process smoke remains necessary as phase evidence — the defect only shows in a
packaged launch — but it is a technical smoke on artifacts built from the
candidate, not a change of verification infrastructure.

## 16. Changed paths of R1

- `docs/validation/pre_g9b_r6_plus_e1_x1_r1_launcher_comparison_report.md` (new, this report)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-x1-r1-launcher-comparison.json` (new)
- `.github/prompts/tasks/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher.prompt.md` (reconciled to Candidate C; still `PREPARED — NOT AUTHORIZED`)
- `docs/roadmap/geocedg_roadmap.md` (the `E1-X1` row; documental version 4.75)
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` (status lines only)

The first characterization report and its JSON mirror are unchanged. The
`STATIC` run of the frozen R1 candidate is reported outside this artifact,
because it cannot name its own commit.
