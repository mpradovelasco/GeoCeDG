# PRE-G9B-R6-plus-E1-X1 — packaged Classic diagnostic launcher

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

This prompt was prepared at the author's instructions of 2026-10-07 for the
focal characterization (`PRE-G9B-R6-plus-E1-X1`) and the A-versus-C
micro-characterization (`PRE-G9B-R6-plus-E1-X1-R1`) of the registered
pre-existing debt `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER`. The
author's explicit instruction of 2026-10-07, typed in the session, accepts the
R1 recommendation, selects Candidate C, names `b3c78e6d` as the exact
implementation base, freezes `BOUNDED_PHASE` and authorizes the implementation
and technical verification of the correction. It is recorded, versioned, in
the [E1-X1 authorization record](../../../docs/validation/pre_g9b_r6_plus_e1_x1_authorization_record.md),
and requires this amendment, as the first tracked edit of the phase, so that
the prompt becomes the executable contract of the phase. Where the prepared
prompt (blob `fb2c245e679fdd3fcd6082d5c6ecd722c561608f`) and the record differ,
the record prevails; every other scope, forbidden-scope and stop rule of the
prepared prompt is kept. The evidence is in the
[E1-X1 characterization report](../../../docs/validation/pre_g9b_r6_plus_e1_x1_preparation_characterization_report.md)
and the
[E1-X1-R1 launcher comparison report](../../../docs/validation/pre_g9b_r6_plus_e1_x1_r1_launcher_comparison_report.md).

`E1`, `E1-L` and `E1-P` stay closed and are not reopened. This file is an
execution contract, not a second policy document: the verification classes are defined
once in `geocedg/specs/operations/verification-levels.md` §12.8, the Desktop
launch contract once in `geocedg/specs/ui/application-profile.md`, the
observation once in the
[E1-P and E1 closeout record](../../../docs/validation/pre_g9b_r6_plus_e1_p_closeout_record.md),
and the probe evidence once in the two reports; this prompt cites them and does
not restate them differently.

```text
PRE-G9B-R6-plus-E1-X1 =
AUTHORIZED FOR IMPLEMENTATION
PRE-G9B-R6-plus-E1-X1-R1 = CHARACTERIZATION COMPLETE

identifier               = PRE-G9B-R6-plus-E1-X1, registered phase
                           PRE-G9B-R6-PLUS-E1-X1 (author decision, DQ-E1X1-1)
selected design          = CANDIDATE C — PRIMARY LAUNCHER EARLY CLASSIC DISPATCH
                           (author decision, DQ-E1X1-2); Candidate A history only
VERIFICATION_CLASS       = BOUNDED_PHASE (frozen; DQ-E1X1-7)
selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (E1-X1 implementation and technical verification only)
passClaimed              = false
PHASE_KIND               = DESKTOP FRONTEND LAUNCH ROUTE; NO PACKAGING, NO KERNEL,
                           NO GEOMETRY, NO SERIALIZATION, NO DOCUMENT FORMAT
DEPENDS_ON               = PRE-G9B-R6-plus-E1 = PASS — AUTHOR APPROVED — PUBLISHED
                           (P_R6PLUS_E1 fdc1ade6; E1-L and E1-P closed; not reopened)
RESOLVES                 = OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER and the
                           same-seam findings X1-F2 and X1-F3; final disposition is
                           the author's, after smoke
NEXT_SUBPHASE            = none implied; E2, E3, F1, F2, F3, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this activity has
been author-approved. Technical verification never creates author approval.
The activity stops with one exact technically verified candidate pending
author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement only, for the two users of the shared diagnostic route —
**Open Classic diagnostic session** (`diagnostic.open-classic`, target
`geocedg.classic`) and **Legacy laboratory** (`automation.legacy-laboratory`,
target `cedg.laboratory.legacy`):

1. an early Classic dispatch in `org.geocedg.desktop.GeoCeDG.main`: when the
   first argument is the private diagnostic token (`DQ-E1X1-3`), and before any
   GeoCeDG preference, profile or frame initialization, remove only that
   token, keep every remaining argument unchanged, add `--showSplash=false`
   and the isolated `classic-diagnostic.properties` only when the caller gave
   no splash or settings argument (`DQ-E1X1-4`, narrow reading of the
   authorization record), call the unchanged
   `org.geogebra.desktop.GeoGebra3D.main` and return;
2. an explicit launch-environment seam in `GeoCeDGActionRegistry.openDiagnostic`:
   packaged mode when `jpackage.app-path` is present (start that same
   executable with the private token; fail closed when it is not a regular
   file), development mode otherwise (the Java fallback with the running JVM's
   module-access options, `DQ-E1X1-6`);
3. discarded child standard output and error in both modes (`X1-F2`,
   `DQ-E1X1-5`);
4. the one-sentence launch-contract amendment of
   `geocedg/specs/ui/application-profile.md`, the tests, the phase registration
   and the evidence of its class.

Invariants the candidate must establish and test:

```text
INV-E1X1-1  In a jpackage launch (jpackage.app-path present), both routes start
            exactly [<jpackage.app-path>, <token>, --showSplash=false,
            --settingsfile=<route preferences>, <validated resource>?]; the command
            never names java.home, runtime\bin\java(w).exe, -cp or a main class.
INV-E1X1-2  If jpackage.app-path does not name a regular file, the route fails with
            an explicit message and starts no process; it never falls back to
            java.home.
INV-E1X1-3  Without jpackage.app-path, the route starts <java.home>\bin\javaw.exe,
            java.exe or java (first regular file) with the --add-exports,
            --add-opens and --enable-native-access entries of the running JVM's
            input arguments, -cp java.class.path and org.geogebra.desktop.GeoGebra3D.
INV-E1X1-4  GeoCeDG.main with the token as first argument creates no GeoCeDG
            preference, profile, frame or application state and reaches
            GeoGebra3D.main with the remaining arguments unchanged (plus the
            only-when-absent defaults of DQ-E1X1-4); the token in any other
            position, or its absence, leaves the normal GeoCeDG start-up
            unchanged.
INV-E1X1-5  Open Classic uses classic-diagnostic.properties, the Laboratory
            laboratory.properties, both beside GeoCeDG's default preferences file;
            neither is GeoCeDG's preference file; no document path or construction
            state of the active GeoCeDG document is passed or changed.
INV-E1X1-6  The child's standard output and error are discarded and can never block
            it.
INV-E1X1-7  The resource is validated (existing regular file, .ggb or .ggt) before
            any process is started; no canonical-legacy trust is created.
```

```text
before  openDiagnostic: java = java.home\bin\javaw.exe | java.home\bin\java
          ProcessBuilder([java, -cp, java.class.path, GeoGebra3D, args…]).start()
          (packaged: CreateProcess error=2; development: pipes never drained,
           no JVM options)

after   PACKAGED     [<jpackage.app-path>, <token>, args…]
                     → GeoCeDG.main: token first → GeoGebra3D.main(args…)
        DEVELOPMENT  [javaw.exe|java.exe|java, <module-access options>, -cp, cp,
                      GeoGebra3D, args…]
        ProcessBuilder(command).redirectOutput(DISCARD).redirectError(DISCARD).start()
```

### Authorities and author decisions this prompt implements

- The author disposition at the `E1-P` closeout (2026-10-07):
  `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` = open, pre-existing,
  reproduced by the author in the `E1-P` smoke and in a mid-September build,
  not caused by `E1-L`/`E1-P`, not `E1`-reopening.
- The author's `E1-X1` and `E1-X1-R1` instructions of 2026-10-07: separate
  process, Classic entry point, isolated preferences, no silent active-document
  transfer, legacy resource explicitly selected by the user, no promotion of
  legacy semantics; Desktop frontend and launcher integration only; avoid
  packaging or ADR 0004 changes unless necessary; the R1 decision rule `C1`–`C11`.
- The author's implementation authorization of 2026-10-07 and its dispositions
  ([authorization record](../../../docs/validation/pre_g9b_r6_plus_e1_x1_authorization_record.md)).
- `AGENTS.md` §4 (GUI and launcher concerns outside the kernel), §6 (optional
  diagnostic Classic access), §7 (no upstream splash or trademarks in the
  product), §16; ADR 0001 (explicit diagnostic route to upstream Classic;
  unchanged Classic launch path).

### Characterization history

The first characterization (`E1-X1`) established `X1-F1` (no `java(w).exe` in
the jpackage runtime, by construction), `X1-F2` and `X1-F3`, proved preference
isolation, and recommended **Candidate A**, a jpackage `--add-launcher` for
`GeoGebra3D`, on the evidence then available; it found that the two-step
app-image → MSI/EXE build of JDK 25.0.4 adds Classic desktop and Start-menu
shortcuts that cannot be suppressed without changing the installer
architecture, and it evaluated Candidate C statically only. `E1-X1-R1` built
and ran a scratch prototype of **Candidate C** and met every decision-rule
condition `C1`–`C11` without any packaging change. Candidate A remains the
documented alternative; it is not the implementation plan.

### Architecture decision: where the fix lives

| Option | Shape | Disposition |
|---|---|---|
| **C — primary launcher early Classic dispatch (selected by the author)** | `GeoCeDG.exe <token> …` → `GeoCeDG.main` → `GeoGebra3D.main`; development Java fallback | Desktop-only; packaged app-image, ZIP, MSI and EXE unchanged (432 files, one launcher, one `.cfg`, same shortcuts, same association, packaging contract 42/42 with the unmodified checker); separate process; genuine Classic; R1 §3–§14 |
| A — jpackage additional launcher | `--add-launcher` for `GeoGebra3D`, sibling discovery | alternative: second executable and `.cfg`; unsuppressible Classic installer shortcuts in the current two-step build; packaging profile, builder, verifier and specification changes |
| B — runtime with `java.exe`/`javaw.exe` | `--jlink-options` without `--strip-native-commands` | rejected (first characterization): 21 general JDK executables; JVM options and `jpackage.app-path` lost; `X1-F2` remains |
| D — other | in-process Classic, JNI, `PATH` discovery | rejected |

The author selected C (`DQ-E1X1-2`). Candidate A is not implemented; switching
to it is a stop condition.

### Characterization results at the base

Evidence, commands and raw records are in the two reports; this section states
the conclusions the contract relies on.

#### X1. Runtime and current route

Development: `java.home` is the Gradle JDK 25.0.4, no `jpackage.app-path`.
App-image, ZIP and installed image: `java.home = <root>\runtime` with no
executable in `runtime\bin`, `jpackage.app-path = <root>\GeoCeDG.exe`.
`openDiagnostic` (`GeoCeDGActionRegistry.java:521-565`, since `b49219408`) fails
in every packaged form with `CreateProcess error=2` on `<runtime>\bin\java`,
for Open Classic and for the Laboratory.

#### X2. Early dispatch

`GeoCeDG` has no static initializer (compile-time constants only), so a branch
placed first in `main` runs before any GeoCeDG state. In the prototype the
Classic child had `App3D`, `GeoGebraFrame3D`, `GuiManager3D`, `AppConfigDefault`
(`classic`), the Classic menu bar, AppUserModelID `geogebra.AppId`, and exactly
the same 42 loaded `org.geocedg.*` classes as a child started directly on
`GeoGebra3D`. All parents started normally through the patched `main`.

#### X3. Separate process and discovery

Every child PID differed from the parent JVM; a child closing by itself left
the parent responsive and unchanged, and a child survived its parent's exit.
`jpackage.app-path` named the running `GeoCeDG.exe` in `INTERNAL` and `NC`
app-images and ZIPs; a missing path failed closed.

#### X4. Streams (`X1-F2`)

With default pipes `Templatev7.ggb` blocks the child (main thread in
`FileOutputStream.writeBytes`), whether GeoCeDG was started by a harness or by
the Windows shell; `Redirect.DISCARD` starts it everywhere; `inheritIO` passes
GeoCeDG's own handles on and is only as safe as they are; a log file works but
truncates per session and stores user paths. Merged output: about 4.8 KB for an
empty session with the 3D view, 11.3 KB for `Templatev7.ggb`.

#### X5. JVM options (`X1-F3`)

Without options the development child logs JOGL `GLException: Unable to
determine GraphicsConfiguration` and JNA restricted-method warnings; with the
four declared options it is clean; without only the `java.base/java.lang`
export nothing different was observed. No narrower set is proven safe. The
running JVM's module-access input arguments are the single runtime authority:
they equal `desktopJvmArgs` under Gradle, and the packaged child already gets
the options from `GeoCeDG.cfg`.

#### X6. Packaging invariance

With the unmodified builder and checker, the prototype's `INTERNAL` and `NC`
sets kept the `E1-P` launcher inventory (`GeoCeDG.exe`, `GeoCeDG.cfg` and
`.jpackage.xml` byte-identical), installer payloads, shortcuts, `.cedg`
association and SBOM shape, and satisfied the packaging contract 42/42.

### Author dispositions

The [authorization record](../../../docs/validation/pre_g9b_r6_plus_e1_x1_authorization_record.md)
is the authority; this table restates it for execution.

| ID | Question | Disposition |
|---|---|---|
| `DQ-E1X1-1` | Identifier | `PRE-G9B-R6-plus-E1-X1`, registered phase `PRE-G9B-R6-PLUS-E1-X1`; `E1` stays closed |
| `DQ-E1X1-2` | Repair candidate | Candidate C, exactly as in *Objective*; Candidate A characterization history only |
| `DQ-E1X1-3` | Private dispatch marker and position | `--classic-diagnostic`, honored only as the first argument; internal launcher protocol, not a public command or document feature |
| `DQ-E1X1-4` | Dispatch defaults | not stated separately by the author; narrow reading recorded for author review: no caller argument is removed or altered; `--showSplash=false` and `--settingsfile=<default preferences directory>\classic-diagnostic.properties` (creating its directory) are added only when the caller supplied no splash or settings argument |
| `DQ-E1X1-5` | Child stream policy (`X1-F2`) | `Redirect.DISCARD` for stdout and stderr in both modes; no default pipes, no `inheritIO`, no persistent log file |
| `DQ-E1X1-6` | Development JVM options (`X1-F3`) | the running JVM is the authority; only the `--add-exports`, `--add-opens` and `--enable-native-access` families of `RuntimeMXBean.getInputArguments()` are forwarded; agents, heap sizing, harness options, arbitrary `-D` properties and unrelated flags are not; packaged children take the options from `GeoCeDG.cfg` |
| `DQ-E1X1-7` | Verification class | `BOUNDED_PHASE` (frozen): one registered `PHASE` plus the focused, process and packaged technical-smoke evidence; no `FINAL`; `INTEGRATION` only after a `VERIFICATION_ESCALATION_REQUEST` |
| `DQ-E1X1-8` | Documentation and closure | the minimum `geocedg/specs/ui/application-profile.md` wording for the internal dispatch; no ADR change; the observation is resolved only with the author's approval after author smoke |

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
P_R6PLUS_E1           = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b
tree                  = cc613c0337d3d75fdf14e55d20db5cb896af2b52
T_R6PLUS_E1_X1_CHAR   = b6172af4f319d297aa5780c9f516b310254d3e90   (documentary only)
IMPLEMENTATION_BASE   = b3c78e6d6564b0400d1fea7cda55bb480778716c   (E1-X1-R1; documentary)
                        tree dca7daaa27f9aca392c8c84a80db61b369fc3d68
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher
                        (local; no rebase)
```

The implementation candidate is a linear descendant of `b3c78e6d`; neither
characterization commit is amended. Entry gate: local `main`, `origin/main`
and the live remote `main` agree with `P_R6PLUS_E1`; `fdc1ade6 → b6172af4 →
b3c78e6d` is linear; the worktree is clean; `E1`, `E1-L`, `E1-P` are still
`PASS — AUTHOR APPROVED — PUBLISHED`.

## Authority and evidence hierarchy

1. `AGENTS.md`, then current code, build and tests at the base.
2. `geocedg/specs/ui/application-profile.md`, ADR 0001, ADR 0004,
   `geocedg/specs/packaging/windows-packaging.md` (unchanged here),
   `geocedg/specs/operations/verification-levels.md` and the typed registry.
3. The author instructions: the `E1-P` closeout disposition, the `E1-X1` and
   `E1-X1-R1` instructions and the authorizing instruction of this activity.
4. The two characterization reports and their JSON mirrors (evidence, not
   authority).
5. Scratch probe outputs, the scratch prototype patch, scratch packages and
   session notes (lowest rank; never a substitute for re-establishing a fact).

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Desktop (GeoCeDG-owned only)

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDG.java`: the
  first-position dispatch, its token constant and the `DQ-E1X1-4` guarantees;
  the rest of `main` unchanged.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java`:
  `openDiagnostic` and `diagnosticCommand` only — the launch-environment seam,
  the packaged and development command construction, the fail-closed packaged
  check, the stream redirection, and injectable launch inputs (application
  path, Java home, class path, input arguments, process starter) for tests. The
  chooser, the confirmation text, the preference file names, the resource
  validation and the failure message keep their behavior.
- One new package-private helper in `org.geocedg.desktop` if the seam is clearer
  outside the registry.

### Specification

- `geocedg/specs/ui/application-profile.md`, section "Launcher and
  configuration": record the private first-position dispatch and its
  guarantees; the "Classic diagnostic route" section keeps its meaning.

### Supporting changes

- New Desktop tests under `source/desktop/desktop/src/test/java/org/geocedg/desktop/`
  and the adjustment of the two existing `diagnosticCommand` tests of
  `G9U1ActionRegistryTest` to the new signature, without weakening them.
- The phase selection, the JUnit inventory update through the official
  mechanism, and the registry-shape pins that registration moves.
- The candidate report, its machine-readable evidence, and the roadmap and
  mini-track status lines. `GUIDE_IMPACT` expected `NONE` (the guides describe
  a separate Classic process with isolated preferences, which stays true).

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Every packaging input and check: `packaging/windows/**`,
  `tools/release/build-windows-package.ps1`, `tools/agent/checks/packaging-product.ps1`,
  `tools/agent/verify-packaging.ps1`, `geocedg/specs/packaging/**`,
  `geocedg/specs/operations/package-profile.schema.json`, ADR 0004; no
  `--add-launcher`, `--runtime-image` or `--jlink-options` change (Candidates A
  and B).
- Any upstream file (`GeoGebra3D`, `GeoGebra`, `GeoGebraFrame`,
  `CommandLineArguments`, `LoggerD`, `AppD`, …) and `docs/upstream/modified-files.yml`.
- Shared kernel, geometry, construction semantics, dependency graph,
  serialization, document format, Locus V2, spatial semantics, export bytes, the
  curated GGT library.
- Any GeoCeDG state, profile, frame, action catalog or workspace in the Classic
  child; in-process profile switching; any other meaning of the token.
- Discovery through `java.home` inference in packaged mode, `PATH`,
  environment variables, the working directory or an executable search.
- Transferring the active document, its path or its construction to a child;
  promoting legacy semantics; canonical-legacy trust or hash authority in the
  GUI route; changes to `tools/legacy/open-laboratory.ps1`.
- The preference file names and locations and the isolation model, beyond
  `DQ-E1X1-4`; a diagnostic log file (`DQ-E1X1-5`).
- The feature-gating of `automation.legacy-laboratory`, the sharing of one
  preference file by concurrent sessions of the same route, and every other open
  observation or enhancement.
- The visible version `1.0.0` and the future `1.1.0` target.
- `PRE-G9B-R6-plus-E2`, `E3`, `F1`, `F2`, `F3` implementation, `G`,
  `PRE-G9B-R7`, `G9B`.

## Architectural placement

Desktop frontend: the GeoCeDG application entry point and its diagnostic route
(`AGENTS.md` §4 "GUI layout or product profile"; ADR 0001 explicit diagnostic
route to upstream Classic). Not packaging, not kernel, not document model, not
export service, not upstream.

## Required design/specification

The "Launcher and configuration" amendment of
`geocedg/specs/ui/application-profile.md` above. No ADR change: ADR 0001 keeps
the explicit diagnostic route and the unchanged Classic launch path, and ADR
0004 is untouched. If the authorizing instruction requires an ADR, it names it;
the agent does not create one.

## Geometric invariants and degeneracies

Not applicable: no geometric object, domain, branch, tolerance or dependency is
read or changed.

## Compatibility and serialization

```text
SERIALIZATION CHANGE      = NONE
DOCUMENT FORMAT CHANGE    = NONE
PREFERENCES CHANGE        = NONE (same files, same isolation)
PACKAGING CHANGE          = NONE (package.yml, builder, jpackage invocation, launcher
                            inventory, installers, shortcuts, association, SBOM shape
                            and packaging contract unchanged)
PRODUCT ENTRY POINT       = one private first-position token in GeoCeDG.main; every
                            other invocation unchanged
DEVELOPMENT BEHAVIOUR     = same route, now with the running JVM's module-access
                            options and discarded child streams
CLASSIC BEHAVIOUR         = unchanged GeoGebra3D entry point, arguments and preferences
```

<!-- geocedg-field: required_checks -->
## Required tests and commands

| ID | Obligation | Kind |
|---|---|---|
| `T-E1X1-DISPATCH` | the dispatch decision as a pure function: token first → Classic arguments without the token plus the `DQ-E1X1-4` guarantees; token elsewhere, absent, `null` or empty arguments → normal start-up; caller-supplied settings file kept (`INV-E1X1-4`) | unit |
| `T-E1X1-DISPATCH-PROCESS` | process-isolated: a child JVM runs `GeoCeDG.main` with the token and an isolated settings file; the application is `App3D` with `AppConfigDefault`, the frame `GeoGebraFrame3D`; no GeoCeDG frame, profile or action-registry class is loaded; GeoCeDG's default preference file is not created or changed; the child halts itself on a bounded timeout | process |
| `T-E1X1-PACKAGED-COMMAND` | packaged command from an injected `jpackage.app-path`: that path, the token, `--showSplash=false`, `--settingsfile`, resource last; no `java`, `-cp`, main class or `java.home` (`INV-E1X1-1`) | unit |
| `T-E1X1-PACKAGED-MISSING` | packaged mode with a missing path fails explicitly and starts nothing, even when a `java.home\bin\javaw.exe` exists (`INV-E1X1-2`) | unit |
| `T-E1X1-DEV-COMMAND` | development command: Java path order, forwarded module-access options from injected input arguments (other entries such as `-D`, `-X` and `-javaagent` not forwarded), `-cp`, `GeoGebra3D` (`INV-E1X1-3`) | unit |
| `T-E1X1-PREFERENCES` | Classic and Laboratory use distinct files beside the default preference file; neither equals it (`INV-E1X1-5`) | unit |
| `T-E1X1-RESOURCE` | `.ggb`/`.ggt` forwarded as the last argument in both modes | unit |
| `T-E1X1-RESOURCE-INVALID` | missing file, directory and wrong extension rejected before the starter is called (`INV-E1X1-7`) | unit |
| `T-E1X1-STREAMS` | the started builder discards stdout and stderr in both modes (`INV-E1X1-6`) | unit |
| `T-E1X1-NO-TRANSFER` | both registry actions with a stub starter: construction XML and saved state unchanged, no document path in the command | Desktop (headless) |
| `T-E1X1-PROCESS` | a real GeoCeDG parent in a child JVM launches the routes: parent PID differs from the Classic child PID; the child is genuine Classic; `Templatev7.ggb` through the Laboratory reaches its window without blocking; the child can close while the parent survives and the parent can close while the child survives; the parent construction and saved state are unchanged; the three preference files are distinct; no orphan process is left; bounded timeouts; never the author's `%APPDATA%` | process |
| `SMOKE-E1X1-TECHNICAL` | `INTERNAL` and `NC` sets built from the exact candidate with the unmodified builder: app-image and extracted ZIP, both routes from the real actions start a separate `GeoCeDG.exe` Classic process with isolated preferences and an unchanged GeoCeDG construction; launcher inventory equal to `E1-P` (`GeoCeDG.exe`, `GeoCeDG.cfg`, `.jpackage.xml`); `packaging-product.ps1 -RequireArtifacts` satisfied on both sets | packaged smoke (scratch agent; phase evidence, not acceptance) |
| `T-SMOKE` | author smoke (*Required artifacts*); the agent does not perform it | author |

Harness rules: unit tests construct commands from injected values and never read
the real `jpackage.app-path` or start a real process; process tests use an
isolated settings file, a scratch `APPDATA` and a bounded wait; never touch the
author's `%APPDATA%\GeoCeDG`; never install or uninstall the MSI/EXE (they share
the author's upgrade code); follow the existing Desktop-test rules (per-test
`LoggerD`, modal dialogs mocked, asynchronous undo store, heap budget of
`final.desktop` — run app scenarios in child JVMs). Scratch packaging builds
need a short build path (WiX `MAX_PATH`).

Adjacent regressions before freezing (development evidence, not acceptance):
`G9U1ActionRegistryTest`, `GeoCeDGProfileTest`, `PreG9BR6PlusE1LBundledCatalogTest`,
`PreG9BR6PlusCX1ClassicPictureProcessTest`, `GeoCeDGSplashWindowTest`; Checkstyle
`:desktop:desktop:checkstyleMain` and `:desktop:desktop:checkstyleTest` (read the
XML reports; ASCII escapes in non-test sources); `Assert-GeoCeDGUpstreamBoundary
-ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522`; `git diff --check`.

Registration and catalog: register the phase selection
`PRE-G9B-R6-PLUS-E1-X1` with `compile.shared.semantic`,
`compile.desktop.semantic` and `junit.desktop.pre-g9b-r6-plus-e1-x1.semantic`,
following the `PRE-G9B-R6-PLUS-C-X1` shape (Desktop-only phase). Discovery
dry-run evidence and **executed** selection evidence through
`tools/agent/checks/gradle-test-evidence-producer.ps1`, then
`tools/agent/update-verification-junit-inventory.ps1` in-session with
`-DiscoveryEvidencePath` and `-SelectionEvidencePath`, the canonical pin
reproduced with `Get-VerificationCanonicalTextSha256` before repinning, and
absolute paths. No hash is entered by hand. When registry-shape pins change,
run a development `INFRA_UNIT` on the staged tree before freezing.

Acceptance (the frozen `BOUNDED_PHASE` contract), on one clean immutable
committed candidate:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-X1 -PlanOnly
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-X1 -LogDirectory <log root>
```

One `ACCEPTED / COMPLETE` run on the exact commit and tree; the technical
packaged smoke on artifacts built from that same commit; no further commit
afterwards. `INTEGRATION`, `FINAL` and `PACKAGING` acceptance runs are not
required by this class and are not run; a request to run one is a
`VERIFICATION_ESCALATION_REQUEST` for the author. If product or test code
changes after acceptance, that candidate is no longer the accepted candidate:
stop and report. The standing diagnostics are pre-existing and do not affect
acceptance. A failure proven to predate the candidate and lie outside its delta
is retained baseline debt per the task template, not phase scope. Report exact
commands, exit codes, run ids, plan and result hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's instruction of 2026-10-07 authorizes: local implementation on the
implementation branch from `b3c78e6d`, local commits, tests, local packaged
artifacts built with the unchanged pipeline for the technical smoke, technical
verification and one frozen technical candidate with its candidate report, for
author review and smoke. It does not authorize self-approval, author smoke by
the agent, installing or uninstalling the MSI/EXE or overwriting the author's
installation, an `INTEGRATION`, `FINAL` or `PACKAGING` acceptance run, or any
change of the class.

This activity authorizes nothing that follows it. `E2`, `E3`, `F1`, `F2`, `F3`,
`G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does every open
observation and enhancement not named here. Author approval is never created
by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local implementation branch from the exact branch start and local commits
only. Push, branch publication, merge, promotion to `main`, rebase, squash,
amend after freeze, force push, tag, release and binary or package publication
are forbidden; each needs a separate explicit author instruction naming the
exact candidate SHA. Acceptance evidence never grants publication authority.

## Acceptance and closeout

The activity stops with one technically verified candidate pending author
review and author smoke, with `selfApproved = false`. Author approval is an
explicit decision naming the exact accepted commit. The candidate report and
its evidence record `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later
closeout record is the sole authority for approval and for the disposition of
`OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` (`DQ-E1X1-8`). Closeout and
publication are separately authorized documentary steps.

## Required artifacts

- The Desktop delta, the tests, their registration and the specification
  sentence.
- A candidate report under `docs/validation/` with: entry-gate evidence; X1–X6
  re-established at the implementation base with every correction; the exact
  diff; the command before and after for both routes and both modes; the
  packaged smoke results and launcher-inventory comparison for the `INTERNAL`
  and `NC` sets; the obligation-to-test map; residual risks; the author-smoke
  checklist.
- The author-smoke checklist covers at least: from the portable ZIP and from the
  installed MSI or EXE, **File → Open Classic diagnostic session** opens a
  separate Classic window; **Automation → Legacy laboratory** with a `.ggb`
  (including a large one such as a legacy model) and with a `.ggt`; the GeoCeDG
  document and its saved state unchanged; the Classic window uses its own
  preferences and shows no upstream splash; closing Classic leaves GeoCeDG open
  and closing GeoCeDG leaves Classic open; repeated sessions; no new Start-menu
  or desktop entry after installation.
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale (expected: no change; no
  workstation prerequisite changes), the verification-infrastructure-impact
  assessment (phase registration only), `GUIDE_IMPACT` (expected `NONE`),
  `SERIALIZATION CHANGE = NONE`, `PACKAGING CHANGE = NONE`, and the exact
  commands, exit codes and log paths.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- re-establishing X1–X6 at the base contradicts the characterization;
- the dispatch cannot run before every GeoCeDG preference, profile or frame
  initialization, or the child is not genuine Classic (`App3D`,
  `AppConfigDefault`, no GeoCeDG frame or profile);
- the fix needs a packaging, kernel, serialization, document-format or upstream
  change, or an ADR change;
- `jpackage.app-path` does not name the running `GeoCeDG.exe` in any built set,
  or the Classic child is not a separate process;
- jpackage behavior differs between `INTERNAL` and `NC`;
- preference isolation or the no-transfer invariant cannot be preserved;
- the Laboratory would need a route different from Open Classic;
- a general-purpose Java launcher or a secondary packaged launcher becomes
  necessary;
- the packaged launcher inventory, installers, shortcuts or packaging contract
  of the smoke sets differ from `E1-P` beyond the expected Desktop classes,
  build provenance and jlink modules;
- the narrow reading of `DQ-E1X1-4` conflicts with any other requirement;
- the implementation needs a persistent log file, in-process Classic or a
  profile switch, a silent fallback from an invalid packaged launcher to the
  development Java, or a return to Candidate A;
- the bounded class proves insufficient (stop with
  `VERIFICATION_ESCALATION_REQUEST`);
- the `PHASE` run is rejected for a cause attributable to the candidate, or its
  coverage is incomplete or untrusted;
- product or test code would change after acceptance.
