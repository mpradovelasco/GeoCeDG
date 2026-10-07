# PRE-G9B-R6-plus-E1-X1 — packaged Classic diagnostic launcher

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-07. That
instruction authorized the focal characterization and design preparation of the
registered pre-existing debt `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER`
under the name `PRE-G9B-R6-plus-E1-X1`, named the published `E1` baseline below,
fixed the questions X1-Q1 to X1-Q8, the semantic requirements, the
architectural boundary and the stop conditions, and stated that **no product
implementation is authorized**. The evidence behind the characterization below
is in the
[E1-X1 characterization report](../../../docs/validation/pre_g9b_r6_plus_e1_x1_preparation_characterization_report.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names the activity and
the exact prepared candidate or base, and that disposes of the requested
decisions `DQ-E1X1-1` to `DQ-E1X1-9`. That instruction may authorize, as the
first tracked edit of the phase, an amendment of this prompt to the authorized
state, following the `C-X1` and `E1-P` precedent. This file is an execution
contract, not a second policy document: the verification classes are defined
once in `geocedg/specs/operations/verification-levels.md` §12.8, the packaging
contract once in `geocedg/specs/packaging/windows-packaging.md`, the
observation once in the
[E1-P and E1 closeout record](../../../docs/validation/pre_g9b_r6_plus_e1_p_closeout_record.md),
and the probe evidence once in the characterization report; this prompt cites
them and does not restate them differently.

```text
PRE-G9B-R6-plus-E1-X1 =
CHARACTERIZED / IMPLEMENTATION PREPARED — PREPARED — NOT AUTHORIZED

identifier               = named by the author for the characterization; its use for
                           the implementation PROPOSED — NOT AUTHOR APPROVED (DQ-E1X1-1)
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP FRONTEND LAUNCH ROUTE + PACKAGING LAUNCHER INTEGRATION;
                           NO KERNEL, NO GEOMETRY, NO SERIALIZATION, NO DOCUMENT FORMAT
DEPENDS_ON               = PRE-G9B-R6-plus-E1 = PASS — AUTHOR APPROVED — PUBLISHED
                           (P_R6PLUS_E1 fdc1ade6; E1-L and E1-P closed; not reopened)
RESOLVES                 = OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER and, if
                           DQ-E1X1-6 includes them, the same-seam findings X1-F2 and
                           X1-F3; final disposition is the author's, after smoke
PRECONDITION             = author dispositions of DQ-E1X1-1 to DQ-E1X1-9
NEXT_SUBPHASE            = none implied; E2, E3, F1, F2, F3, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this activity has
been author-approved. Technical verification never creates author approval.
Once authorized, the activity stops with one exact technically verified
candidate pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement only, for the two users of the shared diagnostic route —
**Open Classic diagnostic session** (`diagnostic.open-classic`, target
`geocedg.classic`) and **Legacy laboratory** (`automation.legacy-laboratory`,
target `cedg.laboratory.legacy`):

1. a dedicated jpackage additional launcher for the Classic entry point
   (`main-class org.geogebra.desktop.GeoGebra3D`, main JAR `desktop.jar`,
   shared runtime and classpath, inherited JVM options and icon), declared in
   the package profile and built by the packaging script for every built
   profile;
2. an explicit launch-environment seam in `openDiagnostic`: packaged mode when
   `jpackage.app-path` is present (start the declared sibling launcher; fail
   closed when it is absent), development mode otherwise (the Java fallback with
   the declared JVM options);
3. discarded child standard streams (`X1-F2`), and the declared JVM options in
   the development fallback (`X1-F3`), as decided by `DQ-E1X1-6`/`DQ-E1X1-7`;
4. the packaging verifier contract, the packaging specification amendment, the
   tests, the phase registration and the evidence of its class.

Invariants the candidate must establish and test:

```text
INV-E1X1-1  In a jpackage launch (jpackage.app-path present), both routes start
            exactly <dir(jpackage.app-path)>\<declared launcher>.exe with
            --showSplash=false, --settingsfile=<route preferences> and, for the
            Laboratory, the validated resource as the last argument; the command
            never names java.home, runtime\bin\java(w).exe, -cp or a main class.
INV-E1X1-2  If the declared launcher is not a regular file in packaged mode, the
            route fails with an explicit message and starts no process; it never
            falls back to java.home.
INV-E1X1-3  Without jpackage.app-path, the route starts <java.home>\bin\javaw.exe,
            java.exe or java (first regular file) with the declared JVM options,
            -cp java.class.path and org.geogebra.desktop.GeoGebra3D.
INV-E1X1-4  Open Classic uses classic-diagnostic.properties, the Laboratory
            laboratory.properties, both beside GeoCeDG's default preferences file;
            neither is GeoCeDG's preference file; no document path or construction
            state of the active GeoCeDG document is passed or changed.
INV-E1X1-5  The child's standard output and error never block it (discarded, per
            DQ-E1X1-6).
INV-E1X1-6  The resource is validated (existing regular file, .ggb or .ggt) before
            any process is started; no canonical-legacy trust is created.
```

```text
before  openDiagnostic: java = java.home\bin\javaw.exe | java.home\bin\java
          ProcessBuilder([java, -cp, java.class.path, GeoGebra3D, args…]).start()
          (packaged: CreateProcess error=2; dev: pipes never drained)

after   mode = jpackage.app-path present ? PACKAGED : DEVELOPMENT
          PACKAGED     [<app dir>\<launcher>.exe, args…]
          DEVELOPMENT  [javaw.exe|java.exe|java, <JVM options>, -cp, cp, GeoGebra3D, args…]
          ProcessBuilder(command).redirectOutput(DISCARD).redirectError(DISCARD).start()
```

### Authorities and author decisions this prompt implements

- The author disposition at the `E1-P` closeout (2026-10-07):
  `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` = open, pre-existing,
  reproduced by the author in the `E1-P` smoke and in a mid-September build,
  not caused by `E1-L`/`E1-P`, not `E1`-reopening; only its focal
  characterization authorized next.
- The author's characterization instruction of 2026-10-07 (report §0):
  separate process, Classic entry point, isolated preferences, no silent
  active-document transfer, legacy resource explicitly selected by the user, no
  promotion of legacy semantics; Desktop frontend and packaging launcher
  integration only; preferred hypothesis a jpackage additional launcher, to be
  validated, not decided.
- `AGENTS.md` §4 (installer and GUI concerns outside the kernel), §6 (optional
  diagnostic Classic access), §7 (no upstream trademarks or installer), §14
  (packaging smoke when packaging changes), §16.

### Architecture decision: where the fix lives

The defect is an application-launcher problem: the Desktop route assumes a JDK
filesystem layout that the jpackage runtime does not have, and starts the child
with undrained pipes. The owner is the GeoCeDG Desktop launch route together
with the packaging profile that defines which launchers exist.

| Option | Shape | Disposition |
|---|---|---|
| **A — jpackage additional launcher (recommended)** | `--add-launcher` for `GeoGebra3D`, sibling discovery from `jpackage.app-path`, explicit development fallback | supported jpackage mechanism; shared runtime, classpath, options and icon; main launcher byte-identical; no general Java tool shipped; validated end to end on scratch `INTERNAL` and `NC` images (X3, X6) |
| B — runtime with `java.exe`/`javaw.exe` | `--jlink-options` without `--strip-native-commands` | rejected: ships 21 general JDK executables for one diagnostic; child loses JVM options and `jpackage.app-path`; pipe defect remains (X7) |
| C — relaunch `GeoCeDG.exe` with a mode argument | `GeoCeDG.main` dispatches to `GeoGebra3D.main` | rejected: widens the product entry point and the `.cedg` open-verb target with a Classic mode, shared executable identity, still needs the Java fallback (X7) |
| D — other | in-process Classic, JNI, `PATH` discovery | rejected (X7) |

Option A is recommended. `DQ-E1X1-2` asks the author to confirm it; the
preparation does not select it on the author's behalf.

### Characterization results at the base

Evidence, commands and raw records are in the characterization report; this
section states the conclusions the contract relies on.

#### X1. Runtime layout

Development: `java.home` is the Gradle JDK 25.0.4 (with `bin\javaw.exe`),
58 classpath entries, no `jpackage.app-path`. App-image, ZIP and installed
image: `java.home = <root>\runtime`, 51 classpath entries from
`app\GeoCeDG.cfg`, `jpackage.app-path = <root>\GeoCeDG.exe`, and **no
executable in `runtime\bin`** — normal, because jpackage runs jlink with
`--strip-native-commands` by default and the product passes no
`--runtime-image`/`--jlink-options`. Product MSI: 432 files, no
`java(w).exe`.

#### X2. Current route

`openDiagnostic` (`GeoCeDGActionRegistry.java:521-565`, introduced by
`b49219408`, 2026-09-04) starts `<java.home>\bin\javaw.exe` or `…\java`.
Packaged: `Cannot run program "<root>\runtime\bin\java": CreateProcess
error=2` for Open Classic and for the Laboratory (`.ggb` and `.ggt`), in the
`E1-P` `INTERNAL` app-image, its ZIP and an `NC`-metadata image; a `.txt` is
rejected before launch. Further fragility, pre-existing: undrained child
pipes block a verbose Classic session (`X1-F2`, reproduced in development with
`Templatev7.ggb`); the development child gets none of the declared JVM options
(`X1-F3`).

#### X3. Supported secondary launcher

`--add-launcher <name>=<properties>` with `main-class
org.geogebra.desktop.GeoGebra3D` creates `<root>\<name>.exe` and
`app\<name>.cfg`, shares the runtime, copies the classpath, inherits the four
JVM options and the icon, and leaves `GeoCeDG.exe`/`GeoCeDG.cfg` byte-identical.
`description` replaces the inherited distribution marker in `FileDescription`
(omit it). Runtime arguments are passed verbatim; default `arguments` apply
only without arguments and expand `$APPDATA`. `--settingsfile` and a trailing
`.ggb`/`.ggt` work. Present in app-image, ZIP, MSI and EXE (embedded MSI); the
`.cedg` verb stays on `GeoCeDG.exe`.

#### X4. Installer shortcuts

In the two-step build (installers from `--app-image`, ADR 0004 decision 3)
JDK 25.0.4 creates desktop and Start-menu shortcuts for **every** launcher when
`--win-shortcut`/`--win-menu` are given: `.jpackage.xml` keeps only `name` and
`service` per additional launcher, `--add-launcher` is invalid at the installer
step, and the shortcuts come from the non-customizable `bundle.wxf`. A
single-step installer from `--input` honors per-launcher
`win-shortcut=false`/`win-menu=false`. See `DQ-E1X1-4`.

#### X5. Discovery

Sibling of `jpackage.app-path` with a declared name is application-owned and
already the repository's packaged-launch signal (`E1-L`); `java.home`
inference and `PATH` discovery are rejected.

#### X6. Prototype (scratch only)

From the packaged GeoCeDG, a scratch route `[<app dir>\GeoCeDG-Classic.exe,
--showSplash=false, --settingsfile=…, resource]` started Open Classic, a `.ggt`
(1 macro) and `Templatev7.ggb` (23 steps, with discarded streams) as separate
`App3D` processes with all JVM options and isolated preferences, in `INTERNAL`
and `NC` images; the active construction digest and saved state stayed
unchanged.

#### X7. Rejected candidates

Candidate B, exercised: the current route then starts, but without JVM options
or `jpackage.app-path`, and still blocks on the pipe; 21 general executables
(`javac`, `jshell`, `jwebserver`, `jrunscript`, `jdb`, `keytool`, …), +0.7 MB;
jlink cannot keep only `java`/`javaw`. Candidate C, static: entry-point
widening and identity sharing. No other viable candidate.

#### X8. Upstream

The pinned baseline `9b93256b` and current upstream `main` `325faee1` have no
packaged-launcher abstraction, no jpackage configuration and no Classic
relaunch path; `GeoGebra3D.main` and the `--showSplash`/`--settingsfile`/file
argument handling are reused unchanged. No upstream file changes.

### Decisions requested before authorization

| ID | Question | Preparation recommendation |
|---|---|---|
| `DQ-E1X1-1` | Identifier of the implementation activity | keep `PRE-G9B-R6-plus-E1-X1`, registry phase id `PRE-G9B-R6-PLUS-E1-X1`; `E1` stays closed |
| `DQ-E1X1-2` | Repair candidate | option A, exactly as in *Objective* |
| `DQ-E1X1-3` | Name of the secondary launcher (executable, `.cfg`, installer shortcut text if any) | `GeoCeDG-Classic-Diagnostic`; never a name containing `GeoGebra` |
| `DQ-E1X1-4` | Installer shortcuts under the JDK 25.0.4 limitation (X4) | (a) keep the two-step build and accept a desktop and Start-menu entry for the secondary launcher, made safe by `DQ-E1X1-5`, with the exact shortcut set pinned by the MSI contract; alternatives: (b) single-step MSI/EXE from `--input` (amends ADR 0004 decision 3 and the specification; installer payload no longer derived from the ZIP's app-image); (c) custom WiX override — rejected (`bundle.wxf` is not customizable). Under (a), the visible upstream-Classic entry point is a branding question for the author |
| `DQ-E1X1-5` | Default launcher arguments for a bare start (shortcut, double click) | `--showSplash=false --settingsfile=$APPDATA/GeoCeDG/5.4/classic-diagnostic.properties`; the product route always passes its complete explicit argument list |
| `DQ-E1X1-6` | Scope and form of the child-stream fix (`X1-F2`) | in scope (same seam, both routes, pre-existing); `Redirect.DISCARD` for stdout and stderr; alternative: a per-route log file beside the preferences |
| `DQ-E1X1-7` | JVM options of the development fallback (`X1-F3`) | in scope; one declared constant equal to `package.yml` `application.jvm_options`, pinned by a test; fallback order `javaw.exe`, `java.exe`, `java` |
| `DQ-E1X1-8` | Verification class | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, impact `PHASE_LOCAL`, with the acceptance set of *Required tests and commands*; alternative `INTEGRATED_PHASE`; not inherited from `E1-P` |
| `DQ-E1X1-9` | Governance artifacts and closure rule | amend `windows-packaging.md` (diagnostic launcher section); no ADR under `DQ-E1X1-4` (a), an ADR 0004 amendment under (b); the observation becomes resolved only after an author smoke of the packaged product (portable and installed) for both routes |

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
P_R6PLUS_E1 = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b
tree        = cc613c0337d3d75fdf14e55d20db5cb896af2b52
```

The implementation branch starts from the exact commit the authorizing
instruction names: either `P_R6PLUS_E1` or the preparation candidate that
contains this prompt (documentary only, no product delta). Entry gate: local
`main`, `origin/main` and the live remote `main` agree with the named base, the
worktree is clean, and `E1`, `E1-L`, `E1-P` are still `PASS — AUTHOR APPROVED
— PUBLISHED`.

## Authority and evidence hierarchy

1. `AGENTS.md`, then current code, build, packaging contracts and tests at the
   base.
2. `geocedg/specs/packaging/windows-packaging.md`, ADR 0004, ADR 0016,
   `geocedg/specs/operations/verification-levels.md` and the typed registry.
3. The author instructions: the `E1-P` closeout disposition, the
   characterization instruction and the authorizing instruction of this
   activity.
4. The characterization report and its JSON mirror (evidence, not authority).
5. Scratch probe outputs, scratch images and session notes (lowest rank; never
   a substitute for re-establishing a fact).

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Desktop (GeoCeDG-owned only)

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java`:
  `openDiagnostic` and `diagnosticCommand` only — the launch-environment seam,
  the packaged and development command construction, the fail-closed packaged
  check, the stream redirection and an injectable process starter for tests.
  The chooser, the confirmation text, the preference file names, the resource
  validation and the message on failure keep their behavior.
- One new package-private helper in `org.geocedg.desktop` if the seam is
  clearer outside the registry (for example the launch environment and the
  declared launcher name and JVM options).

### Packaging

- `packaging/windows/package.yml`: one declaration of the diagnostic launcher
  (name, main class, default arguments, shortcut intent per `DQ-E1X1-4`).
- `geocedg/specs/operations/package-profile.schema.json`: admit exactly that
  declaration.
- `tools/release/build-windows-package.ps1`: generate the launcher properties
  in the work directory from the profile, pass `--add-launcher` to the
  app-image invocation for every built profile, assert the launcher and its
  `.cfg`, record the launcher set in the build manifest; under `DQ-E1X1-4` (b)
  only, the single-step installer invocation.
- `tools/agent/checks/packaging-product.ps1`: `packaging.launcher-config`
  reads `app\GeoCeDG.cfg` by exact name; a `packaging.diagnostic-launcher`
  subcontract (executable, `.cfg` with `GeoGebra3D`, classpath and JVM options
  equal to the main launcher, `.jpackage.xml` entry, no `runtime\bin\*.exe`);
  MSI inspection of the launcher files and of the exact shortcut set decided by
  `DQ-E1X1-4`; the `.cedg` verb still targets `GeoCeDG.exe` only.
- `geocedg/specs/packaging/windows-packaging.md`: the diagnostic-launcher
  amendment (`DQ-E1X1-9`).

### Supporting changes

- New Desktop tests under `source/desktop/desktop/src/test/java/org/geocedg/desktop/`
  and the adjustment of the two existing `diagnosticCommand` tests of
  `G9U1ActionRegistryTest` to the new signature, without weakening them.
- Packaging-check tests and verifier pins that the contract change moves.
- The phase selection, the JUnit inventory update through the official
  mechanism, and the registry-shape pins that registration moves.
- The candidate report, its machine-readable evidence, and the roadmap and
  mini-track status lines; `GUIDE_IMPACT` per `DQ-E1X1-4` (a): a one-line
  mention of the installed entry in the bilingual guides §14.1, otherwise none.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Shared kernel, geometry, construction semantics, dependency graph,
  serialization, document format, Locus V2, spatial semantics, export bytes.
- Any upstream file (`GeoGebra3D`, `GeoGebra`, `GeoGebraFrame`,
  `CommandLineArguments`, `LoggerD`, `AppD`, …) and `docs/upstream/modified-files.yml`.
- `GeoCeDG.main` and the main launcher identity, main class, JVM options, icon,
  association and `.cedg` verb target; any Classic mode in the product entry
  point (Candidate C).
- A general-purpose Java executable in the runtime, `--runtime-image`, or a
  change of the default `--jlink-options` (Candidate B).
- Discovery through `java.home` inference in packaged mode, `PATH`,
  environment variables, the working directory or an executable search.
- Transferring the active document, its path or its construction to a child;
  promoting legacy semantics; canonical-legacy trust or hash authority in the
  GUI route; changes to `tools/legacy/open-laboratory.ps1`.
- The preference file names and locations, and the isolation model, beyond
  `DQ-E1X1-5`.
- The feature-gating of `automation.legacy-laboratory` (report §14), the
  sharing of one preference file by concurrent sessions of the same route, and
  any other open observation or enhancement.
- `COMMERCIAL` packaging, licensing records, distribution markers and notices.
- `PRE-G9B-R6-plus-E2`, `E3`, `F1`, `F2`, `F3` implementation, `G`,
  `PRE-G9B-R7`, `G9B`.

## Architectural placement

Desktop frontend (GeoCeDG application layer: the diagnostic route) and
packaging/release layer (the set of packaged launchers), per `AGENTS.md` §4
"Own installer" and "GUI layout or product profile". Not kernel, not document
model, not export service, not upstream.

## Required design/specification

The amendment of `geocedg/specs/packaging/windows-packaging.md` above. ADR 0004
decision 3 ("one app-image; ZIP, MSI and EXE derived from it") is preserved by
`DQ-E1X1-4` (a); option (b) requires an ADR 0004 amendment authorized by the
author before editing. If the authorizing instruction requires another ADR, it
names it; the agent does not create one.

## Geometric invariants and degeneracies

Not applicable: no geometric object, domain, branch, tolerance or dependency is
read or changed.

## Compatibility and serialization

```text
SERIALIZATION CHANGE      = NONE
DOCUMENT FORMAT CHANGE    = NONE
PREFERENCES CHANGE        = NONE (same files, same isolation)
MAIN LAUNCHER CHANGE      = NONE (GeoCeDG.exe and app\GeoCeDG.cfg byte-identical in
                            launch content; .cedg association unchanged)
PACKAGE CONTENT CHANGE    = one launcher executable and its .cfg (app-image, ZIP,
                            MSI, EXE); installer shortcuts per DQ-E1X1-4
DEVELOPMENT BEHAVIOUR     = same route, now with the declared JVM options and
                            discarded child streams
CLASSIC BEHAVIOUR         = same GeoGebra3D entry point, arguments and preferences
```

<!-- geocedg-field: required_checks -->
## Required tests and commands

| ID | Obligation | Kind |
|---|---|---|
| `T-E1X1-DEV-COMMAND` | development command: Java path selection order, declared JVM options, `-cp`, `GeoGebra3D`, `--showSplash=false`, `--settingsfile`, resource last (`INV-E1X1-3`) | unit |
| `T-E1X1-PACKAGED-COMMAND` | packaged command from a synthetic `jpackage.app-path`: sibling launcher first, no `java`, `-cp`, main class or `java.home` (`INV-E1X1-1`) | unit |
| `T-E1X1-PACKAGED-MISSING` | packaged mode with an absent sibling fails explicitly and starts nothing, even when a `java.home\bin\javaw.exe` exists (`INV-E1X1-2`) | unit |
| `T-E1X1-NO-RUNTIME-JAVA` | no packaged command references `runtime\bin\java(w)`; the packaging contract proves `runtime\bin` has no executable | unit + packaging |
| `T-E1X1-PREFERENCES` | Classic and Laboratory use distinct files beside the default preference file; neither equals it (`INV-E1X1-4`) | unit |
| `T-E1X1-RESOURCE` | `.ggb`/`.ggt` forwarded as the last argument in both modes | unit |
| `T-E1X1-RESOURCE-INVALID` | missing file, directory and wrong extension rejected before the starter is called (`INV-E1X1-6`) | unit |
| `T-E1X1-STREAMS` | the started builder discards stdout and stderr (`INV-E1X1-5`) | unit |
| `T-E1X1-NO-TRANSFER` | both registry actions with a stub starter: construction XML and saved state unchanged, no document path in the command | Desktop (headless) |
| `T-E1X1-DECLARATIONS` | product launcher name and development JVM options equal the `package.yml` declarations | unit |
| `T-E1X1-PROCESS` | process-isolated: the development route starts a `GeoGebra3D` child with isolated settings and a verbose `.ggb` that reaches its window without blocking; the child halts itself, bounded timeout, never the author's `%APPDATA%` | process |
| `PKG-E1X1-LAUNCHER` | built app-image: launcher executable and `.cfg`, `GeoGebra3D`, classpath and options equal to the main launcher, `.jpackage.xml` entry, exact `GeoCeDG.cfg` check, no runtime executable | packaging contract |
| `PKG-E1X1-INSTALLER` | decompiled MSI: both executables in `INSTALLDIR`, `.cedg` verb only on `GeoCeDG.exe`, exact shortcut set per `DQ-E1X1-4`; EXE built by the same invocation family | packaging contract |
| `PKG-E1X1-PROFILES` | `INTERNAL` and `NC` artifact sets both satisfy the launcher contract | packaging contract |
| `SMOKE-E1X1-TECHNICAL` | unmodified built `INTERNAL` and `NC` app-image and ZIP: both routes from the real actions start a separate secondary-launcher process with `App3D`, isolated preferences and an unchanged GeoCeDG construction; app-image digest unchanged by the launches | packaged smoke (scratch agent) |
| `T-SMOKE` | author smoke (*Required artifacts*); the agent does not perform it | author |

Harness rules: unit tests construct commands from injected values and never
read the real `jpackage.app-path` or start a real process; the process test
uses an isolated settings file, a scratch `APPDATA` and a bounded wait; never
touch the author's `%APPDATA%\GeoCeDG`, never install or uninstall the MSI
(it shares the author's upgrade code); follow the existing Desktop-test rules
(per-test `LoggerD`, modal dialogs mocked, asynchronous undo store, heap budget
of `final.desktop` — run app scenarios in child JVMs).

Adjacent regressions before freezing (development evidence, not acceptance):
`G9U1ActionRegistryTest`, `GeoCeDGProfileTest`, `PreG9BR6PlusE1LBundledCatalogTest`,
`PreG9BR6PlusCX1ClassicPictureProcessTest`; Checkstyle
`:desktop:desktop:checkstyleMain` and `:desktop:desktop:checkstyleTest` (read
the XML reports; ASCII escapes in non-test sources);
`tools/agent/verify-packaging.ps1`; `Assert-GeoCeDGUpstreamBoundary
-ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522`; `git diff --check`.

Registration and catalog: register the phase selection
`PRE-G9B-R6-PLUS-E1-X1` with `compile.shared.semantic`,
`compile.desktop.semantic`, `junit.desktop.pre-g9b-r6-plus-e1-x1.semantic`,
`packaging.product` and, if the packaging-check change requires it,
`infra.contract-boundary` (the `E1-L` and `E1-P` shapes). Discovery dry-run
evidence and **executed** selection evidence through
`tools/agent/checks/gradle-test-evidence-producer.ps1`, then
`tools/agent/update-verification-junit-inventory.ps1` in-session with
`-DiscoveryEvidencePath` and `-SelectionEvidencePath`, the canonical pin
reproduced with `Get-VerificationCanonicalTextSha256` before repinning, and
absolute paths. No hash is entered by hand.

Acceptance (the proposed `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` /
`PHASE_LOCAL` contract), on one clean immutable committed candidate, with fresh
`INTERNAL` and `NC` artifact sets built from that exact candidate:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-X1 -PlanOnly
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-X1 -LogDirectory <log root>
tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory <log root>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -LogDirectory <log root>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -VerifyPackagingArtifacts
    -PackagingArtifactRoot <INTERNAL set> -LogDirectory <log root>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -VerifyPackagingArtifacts
    -PackagingArtifactRoot <NC set> -LogDirectory <log root>
```

Each run `ACCEPTED / COMPLETE` on the exact commit and tree; no further commit
afterwards. `INTEGRATION` and `FINAL` are not required by this class and are
not run; a request to run either is a `VERIFICATION_ESCALATION_REQUEST` for the
author. If product, packaging or test code changes after acceptance, that
candidate is no longer the accepted candidate: stop and report. The standing
diagnostics are pre-existing and do not affect acceptance. A failure proven to
predate the candidate and lie outside its delta is retained baseline debt per
the task template, not phase scope. Report exact commands, exit codes, run ids,
plan and result hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

This prompt authorizes nothing. Its existence records a prepared contract.
Execution requires an explicit author instruction naming the activity, the
exact branch start, the frozen class and the dispositions of `DQ-E1X1-1` to
`DQ-E1X1-9`; that instruction may authorize local implementation, local
commits, local scratch packaging builds, technical verification and one frozen
technical candidate for author review and smoke. No instruction derived from
this file authorizes self-approval, author smoke by the agent, installing or
uninstalling the MSI/EXE, an `INTEGRATION` or `FINAL` run, or any change of the
class.

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
`OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` (`DQ-E1X1-9`). Closeout and
publication are separately authorized documentary steps.

## Required artifacts

- The Desktop and packaging delta, the tests, their registration and the
  specification amendment.
- A candidate report under `docs/validation/` with: entry-gate evidence; X1–X8
  re-established at the implementation base with every correction; the exact
  diff; the command before and after for both routes and both modes; the
  packaged launcher layout of the built `INTERNAL` and `NC` sets; the decompiled
  installer shortcut set; the obligation-to-test map; residual risks; the
  author-smoke checklist.
- The author-smoke checklist covers at least: from the portable ZIP and from
  the installed MSI or EXE, **File → Open Classic diagnostic session** opens a
  separate Classic window; **Automation → Legacy laboratory** with a `.ggb`
  (including a large one such as a legacy model) and with a `.ggt`; the
  GeoCeDG document and its saved state unchanged; the Classic window uses its
  own preferences; repeated sessions; under `DQ-E1X1-4` (a) the installed
  shortcut opens an isolated Classic session without the upstream splash.
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale (expected: no change; no new
  workstation prerequisite — the same JDK 25.0.4 `jpackage` and WiX 5.0.2),
  the verification-infrastructure-impact assessment (packaging-check contract
  and phase registration, `PHASE_LOCAL`), `GUIDE_IMPACT` per `DQ-E1X1-4`,
  `SERIALIZATION CHANGE = NONE`, and the exact commands, exit codes and log
  paths.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- re-establishing X1–X8 at the base contradicts the characterization;
- the fix needs a kernel, serialization, document-format or upstream change;
- Classic cannot be started as a separate packaged process through the
  additional launcher, or the launcher cannot share the runtime, classpath or
  JVM options;
- jpackage behavior differs between `INTERNAL` and `NC`;
- preference isolation or the no-transfer invariant cannot be preserved;
- the Laboratory would need a route different from Open Classic;
- a general-purpose Java launcher becomes necessary;
- the installer shortcut behavior differs from X4 or cannot be pinned as
  decided by `DQ-E1X1-4`;
- the packaging verifier change reaches beyond the packaging checks (global
  verification infrastructure), which would change the class;
- a `DQ-E1X1` question is reached without an author disposition;
- any acceptance run is rejected for a cause attributable to the candidate, or
  its coverage is incomplete or untrusted;
- product, packaging or test code would change after acceptance.
