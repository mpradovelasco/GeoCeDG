# PRE-G9B-R6-plus-E1-X1 focal characterization report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (post-E1 focal characterization)
TECHNICAL_CANDIDATE_STATE = FROZEN with the preparation candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

OBSERVATION               = OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER
ACTIVITY                  = PRE-G9B-R6-plus-E1-X1 (named by the author instruction)
PROMPT STATE              = PREPARED — NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
PACKAGING CHANGE          = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the characterization encoded in the
canonical
[`E1-X1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher.prompt.md)
(sections X1–X8 and `DQ-E1X1-1` to `DQ-E1X1-9`). The prompt holds the resulting
contract; this report does not restate it and carries no authority of its own.
Its machine-readable mirror is
[`pre-g9b-r6-plus-e1-x1-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-x1-preparation-characterization.json).

## 0. Preparation inputs

The author's instruction of 2026-10-07 is the authority of this
characterization. It is recorded here, instead of in a separate author-decision
record, because it fixes no product decision; every open decision is a
`DQ-E1X1` row of the prompt.

- Baseline: `PRE-G9B-R6-plus-E1 = PASS — AUTHOR APPROVED — PUBLISHED`,
  `P_R6PLUS_E1 = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b`, tree
  `cc613c0337d3d75fdf14e55d20db5cb896af2b52`. `E1-L`, `E1-P` and `E1` keep
  their published dispositions; `E1` is not reopened and its history is not
  rewritten.
- Observation: `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER = OPEN —
  PRE-EXISTING`, reproduced by the author in the `E1-P` packaged smoke and in a
  GeoCeDG build of mid-September 2026
  ([E1-P and E1 closeout record](pre_g9b_r6_plus_e1_p_closeout_record.md)).
- Authorized: characterization and design preparation only, both users of the
  shared route (`diagnostic.open-classic`, `automation.legacy-laboratory`),
  scratch builds only, this report, a machine-readable mirror, a canonical
  implementation prompt in `PREPARED — NOT AUTHORIZED` when the governance
  pattern supports it (it does: the `C-X1` preparation `50baef73`), and minimal
  roadmap and mini-track status. Not authorized: any product or packaging fix,
  publication, merge, tag, release, `E2`.
- Preliminary hypothesis to validate, not an author decision: a jpackage
  additional application launcher.

## 1. Entry gate

```text
local main         = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b
origin/main        = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b
live remote main   = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b   (git ls-remote origin refs/heads/main)
HEAD tree          = cc613c0337d3d75fdf14e55d20db5cb896af2b52
worktree           = clean
preparation branch = phase/pre-g9b-r6-plus-e1-x1-prompt (local, from fdc1ade6; never pushed)
```

## 2. Method

- **Static reading** at `P_R6PLUS_E1` of every cited seam:
  `GeoCeDGActionRegistry` (`:330-331`, `:389-390`, `:521-565`), `GeoCeDG`
  (`:33-61`), `GeoCeDGBundledToolCatalog.defaultDirectory` (`:83-90`),
  `LocusV2Laboratory`, `source/desktop/desktop/build.gradle.kts`
  (`:140-145`, `:199-203`, `:225-234`), `packaging/windows/package.yml` and
  its schema, `tools/release/build-windows-package.ps1` (`:656-731`),
  `tools/agent/checks/packaging-product.ps1` (`:330-365`, `:554-588`,
  `:707-713`), `geocedg/specs/packaging/windows-packaging.md`, ADR 0004,
  `GeoGebraFrame.init` (`:331-334`), `LoggerD.print` (`:160-170`).
- **Packaged inputs (read-only).** The ignored `artifacts/packaging/windows`
  set built by `E1-P` from `T_R6PLUS_E1_P` `dcda4075` (`INTERNAL`); between
  `dcda4075` and `fdc1ade6` only closeout documents change. The app-image tree
  digest was identical before and after every probe launch
  (`1C40EF4B…C079`); the portable ZIP (`E0E213AC…AFE7`) was extracted into the
  scratchpad.
- **One scratch javaagent**, never in product code, compiled with the Gradle JDK
  25.0.4 against the packaged JARs. It is attached through `JAVA_TOOL_OPTIONS`
  to unmodified jpackage launchers and through a Gradle init script to
  `:desktop:desktop:runGeoCeDG`. In a GeoCeDG process it invokes the real
  registry actions `diagnostic.open-classic` and
  `automation.legacy-laboratory` on the EDT, drives the real `JFileChooser`
  and confirmation dialog, reads the resulting message dialog, records the new
  child processes and compares the construction-XML digest and the saved state
  before and after. In a child it records application and frame class,
  preferences file, current file, construction steps, macro count and window
  title; on a start-up timeout it dumps the `main` and EDT stacks.
- **Isolation.** Every launch used a scratch `APPDATA` and an explicit
  `--settingsfile`; the author's `%APPDATA%\GeoCeDG\5.4` digest was identical
  before and after (`153FB6FC…3F31`). Children were destroyed or halted
  themselves; no process was left running.
- **Scratch jpackage images** built with JDK 25.0.4 `jpackage` from the JARs of
  the `E1-P` app-image, with the product's name, icon, version, vendor, marker
  and four JVM options: `INTERNAL` and `NC`-metadata images with
  `--add-launcher`, one argument-semantics launcher, one image with native
  commands (`--jlink-options` without `--strip-native-commands`), MSI and EXE
  from an app-image (two-step, as the product), and one single-step MSI. None
  installed or published.
- **Installer payloads** were inspected with WiX 5.0.2 `wix msi decompile`
  (product MSI, scratch MSIs, the MSI embedded in the scratch EXE at byte offset
  568 076). Nothing was installed.
- **jpackage contract**: `jpackage --help`, the JDK 25 `jpackage` manual page,
  `jpackage --verbose`, and `javap` of `jdk.jpackage.internal.AppImageFile`.
- **Upstream**: the pinned baseline `9b93256b` in-tree; GitHub
  `geogebra/geogebra` `main` at `325faee17884c7ced41d360d0f08ae919a38a955`
  (2026-10-06), read-only.
- No product, test, build, packaging, registry, schema, verifier,
  specification or ADR file was changed.

## 3. X1-Q1 — runtime layout

| | Gradle/dev | jpackage app-image | portable ZIP | MSI / EXE payload |
|---|---|---|---|---|
| `java.home` | `~/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2` (full JDK) | `<app-image>\runtime` | `<extract>\GeoCeDG\runtime` | `INSTALLDIR\runtime` |
| `java.class.path` | 58 entries (Gradle runtime classpath) | 51 entries, `<app-image>\app\*.jar` from `app\GeoCeDG.cfg` | same as app-image | same as app-image |
| `jpackage.app-path` | absent | `<app-image>\GeoCeDG.exe` | `<extract>\GeoCeDG\GeoCeDG.exe` | `INSTALLDIR\GeoCeDG.exe` (layout; not run) |
| application launcher | `<JDK>\bin\java.exe` (Gradle `JavaExec`) | `GeoCeDG.exe` | `GeoCeDG.exe` | `GeoCeDG.exe` |
| `runtime\bin\*.exe` | `java.exe`, `javaw.exe`, … | **none** | **none** | **none** (432 files, no `java(w).exe`) |

The jpackage launcher also adds `-Djpackage.app-version=1.0.0` and
`-Djpackage.app-path=<launcher>` to the JVM options of `app\GeoCeDG.cfg`.

**The absence of `java.exe`/`javaw.exe` is normal jpackage/jlink behavior.**
When no `--runtime-image` is given, JDK 25 `jpackage` runs `jlink` with the
default options `--strip-native-commands --strip-debug --no-man-pages
--no-header-files` (`jpackage --help`; JDK 25 manual page), and the product
passes neither `--runtime-image` nor `--jlink-options`
(`build-windows-package.ps1:656-677`). The product runtime is therefore a
launcher-less runtime by construction; a development JDK is not a model of it.
`jpackage.app-path` is not described by the JDK 25 manual page; it is the
implementation property the launcher sets, already relied upon by `E1-L`
(`GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY`).

## 4. X1-Q2 — the current route

`openDiagnostic` (`GeoCeDGActionRegistry.java:521-550`) builds
`<java.home>\bin\javaw.exe`, falls back to `<java.home>\bin\java`, and starts
`diagnosticCommand` (`:552-565`):

```text
<java> -cp <java.class.path> org.geogebra.desktop.GeoGebra3D
       --showSplash=false --settingsfile=<APPDATA>\GeoCeDG\5.4\<route>.properties [resource]
```

through `new ProcessBuilder(...).start()` with default redirection. It works in
development because `java.home` is a JDK with `bin\javaw.exe` and
`java.class.path` is complete. It fails in every packaged form because
`runtime\bin` contains no executable; the fallback name `java` does not exist
either, so `CreateProcess` reports error 2 on `<runtime>\bin\java`, exactly the
registered observation. The form was introduced by `b49219408` (2026-09-04,
`G9U1`); every packaged build since carries it, consistent with the author's
mid-September reproduction. `E1-L` and `E1-P` did not touch it.

Even with a Java executable present, the strategy carries further packaged and
non-packaged assumptions, all confirmed below:

1. **Undrained child streams (`X1-F2`, pre-existing, both routes).** The child
   inherits the default stdout/stderr pipes, and the parent never reads them.
   Classic logs to the console (`LoggerD.print:168`, `System.out.println`).
   Once the pipe buffer is full the child blocks. Reproduced on the
   **unmodified development route**: Legacy laboratory with
   `Templatev7.ggb` never shows a window; the child's `main` thread is
   `RUNNABLE` in `FileOutputStream.writeBytes` ← `PrintStream.println` ←
   `LoggerD.print`. The same file opens in 23 construction steps when launched
   directly, and in a scratch packaged route with `Redirect.DISCARD`, and blocks
   again with default pipes. A small `.ggt` and an empty Classic session start,
   so the defect depends on how much the session logs.
2. **JVM-option drift (`X1-F3`, pre-existing).** The development child receives
   none of the four options declared by `runGeoCeDG` and `package.yml`
   (`--add-exports` ×3, `--enable-native-access=ALL-UNNAMED`): its input
   arguments are empty apart from the probe agent. A packaged general-Java route
   (Candidate B) loses them too, together with `jpackage.app-path`.
3. **Classpath coupling.** `-cp <java.class.path>` reproduces the parent's
   classpath, which in the packaged form is the launcher's private
   `app.classpath` list; it is correct only while the parent and the child are
   meant to share it and the parent was itself started with that list.
4. **Executable naming.** `java` without `.exe` is the POSIX name; on Windows the
   fallback can never match a regular file.

## 5. X1-Q3 — supported jpackage secondary launcher

`jpackage --add-launcher <name>=<properties>` is supported by JDK 25.0.4 with the
keys `module`, `main-jar`, `main-class`, `description`, `arguments`,
`java-options`, `app-version`, `icon`, `launcher-as-service`, `win-console`,
`win-shortcut`, `win-menu`, `linux-*` (`jpackage --help`). Scratch result with
`main-class=org.geogebra.desktop.GeoGebra3D` (main JAR inherited,
`desktop.jar`):

- **Path/name.** `<app-image>\GeoCeDG-Classic.exe` and
  `<app-image>\app\GeoCeDG-Classic.cfg` beside the main launcher;
  `app\.jpackage.xml` records `<add-launcher name="GeoCeDG-Classic"
  service="false">`.
- **Main launcher unchanged.** `GeoCeDG.exe` and `app\GeoCeDG.cfg` are
  byte-identical to the `E1-P` product files.
- **Shared runtime.** One `runtime\`; its `release` module list is identical to
  the product's; no executable is added to `runtime\bin`.
- **Classpath and options.** The `.cfg` carries the same `app.classpath` lines
  as the main launcher and inherits all four JVM options and
  `-Djpackage.app-version`; at run time `jpackage.app-path` names
  `GeoCeDG-Classic.exe`. No duplication is needed.
- **Icon and metadata.** The launcher inherits the `--icon` bitmap (identical
  extracted icon). Its `FileDescription` inherits the distribution marker unless
  the properties set `description` (which replaces it); `LegalCopyright` always
  inherits the marker. No separate icon or metadata is required.
- **Arguments.** Runtime arguments are passed verbatim. Default `arguments`
  from the properties apply **only when the launcher receives no argument**
  (launcher with `arguments=--showSplash=false`: bare start shows
  `--showSplash=false`; start with `--settingsfile=… small.ggb` shows exactly
  those two). `$APPDATA` is expanded in default arguments (verified).
- **Dynamic preferences and resource.** `--settingsfile=<per-user path>` and a
  trailing `.ggb`/`.ggt` passed at launch are honored: `Templatev7.ggb` opened
  with 23 construction steps and title `Templatev7.ggb`; `CirclebyD.ggt` loaded
  one macro; the child is `App3D`/`GeoGebraFrame3D` with the given preferences
  file.
- **Payloads.** App-image and ZIP contain the launcher (ZIP is the normalized
  app-image). In the MSI built from the app-image, `GeoCeDG-Classic.exe` and its
  `.cfg` sit in `INSTALLDIR` next to `GeoCeDG.exe` (434 files instead of 432);
  the `.cedg` open verb still targets `GeoCeDG.exe` only. The EXE's embedded MSI
  has the same content.
- **Installer shortcuts (limitation).** Although the properties set
  `win-shortcut=false` and `win-menu=false`, the two-step MSI and EXE create a
  **desktop and a Start-menu shortcut for the secondary launcher**. In JDK
  25.0.4 `AppImageFile` persists only `name` and `service` for each
  `add-launcher` in `.jpackage.xml` (`javap` constant pool), so the installer
  step, which only reads the app-image, applies the global `--win-shortcut` and
  `--win-menu` to every launcher. `--add-launcher` is rejected at that step
  (`Option [--add-launcher] is not valid with type [msi]`), and the shortcuts
  are generated in `bundle.wxf`, which is not a `--resource-dir` resource
  (`--verbose` lists `main.wxs`, `overrides.wxi`, `os-condition.wxf`, the
  `.wxl` files and the XSL only). A single-step MSI built from `--input` with
  `--add-launcher` honors both keys (no Classic shortcut), but it is no longer
  derived from the same app-image (ADR 0004 decision 3, `windows-packaging.md`).

## 6. X1-Q4 — discovering the secondary launcher

| Option | Assessment |
|---|---|
| **A. sibling of `jpackage.app-path`** | **Recommended.** Application-owned: the property is set by the launcher that started GeoCeDG, and the secondary launcher is declared by the same package profile and built in the same jpackage invocation, so `resolveSibling(<declared name>.exe)` is a packaging contract, not a search. Same relative layout in app-image, ZIP and installed image. Already the repository's packaged-launch signal (`E1-L` library). Fail closed when the property is present and the sibling is not a regular file. |
| B. other application-owned location | No jpackage-defined alternative exists beyond `$APPDIR`/`$ROOTDIR` tokens in `.cfg` files; a GeoCeDG-owned system property injected through `java-options` (for example the launcher path) would duplicate what `jpackage.app-path` already gives. Possible but redundant. |
| C. inference from `java.home` | Rejected: `java.home` describes the runtime, not the application; it is exactly the assumption that failed, and `<runtime>\..\` inference is a heuristic layout guess. |
| D. `PATH`/environment | Rejected: would select an arbitrary installed JVM or executable unrelated to the package. |

`ProcessHandle.current().info().command()` equals `jpackage.app-path` in the
packaged runs and `java.exe` in development; it identifies the current
executable, not a declared secondary one, so it adds nothing to A.

## 7. X1-Q5 — development fallback

The cleanest explicit and testable seam is a launch environment chosen from
one fact, not from filesystem state:

```text
jpackage.app-path present  -> PACKAGED: <dir(app-path)>\<declared launcher>.exe
                              absent sibling -> explicit failure; never java.home
jpackage.app-path absent   -> DEVELOPMENT: <java.home>\bin\javaw.exe | java.exe | java
                              + declared JVM options + -cp java.class.path + GeoGebra3D
```

The command construction becomes a pure function of (mode, launcher or Java
path, JVM options, class path, preferences, resource), unit-testable with
synthetic values; `openDiagnostic` only reads the two system properties, the
preferences path and starts the process with discarded standard streams.
`runGeoCeDG`, `runLocusV2Laboratory` and IDE runs keep working: none of them sets
`jpackage.app-path`.

## 8. X1-Q6 — preference isolation

| Route | File | Observed in |
|---|---|---|
| Open Classic | `<APPDATA>\GeoCeDG\5.4\classic-diagnostic.properties` | dev child, scratch packaged child (`--settingsfile` and Classic's `PROPERTY_FILEPATH`) |
| Legacy laboratory | `<APPDATA>\GeoCeDG\5.4\laboratory.properties` | dev child |
| GeoCeDG | `preferences.properties` (or the explicit `--settingsfile`) | never passed to a child |

GeoCeDG's main preferences are never reused; the two routes never share a file;
in all 23 route invocations across eight probed GeoCeDG processes the active
construction digest and the saved state were unchanged. Two concurrent sessions of the **same** route
share that route's file (pre-existing, unchanged by every candidate).
`LocusV2Laboratory` uses a `laboratory.properties` inside its own temporary
directory, not this file. Bare invocation of a packaged secondary launcher (a
shortcut or a double click) has no `--settingsfile`; a default
`arguments=--showSplash=false --settingsfile=$APPDATA/GeoCeDG/5.4/classic-diagnostic.properties`
keeps it isolated (verified expansion).

## 9. X1-Q7 — Legacy laboratory

The resource reaches the child as the last application argument in every
environment. `diagnosticCommand` validates it before any launch: in the
packaged image `not-a-model.txt` was rejected with "Diagnostic resource must be
an existing GGB/GGT file" and no process was attempted, while `.ggb` and `.ggt`
reached the launch step. The validation is a GUI admission check only; it
creates no canonical-legacy trust or hash authority (the confirmation text says
so and points to `tools/legacy/open-laboratory.ps1`). The Laboratory needs no
architecture different from Open Classic: same process route, plus the
resource argument.

## 10. X1-Q8 — candidates

| | A — jpackage additional launcher | B — runtime with `java(w).exe` | C — relaunch `GeoCeDG.exe` with a mode | D |
|---|---|---|---|---|
| product code delta | `GeoCeDGActionRegistry` launch seam (packaged/dev), stream redirection | stream redirection only (still needs option forwarding to fix `X1-F3`) | `GeoCeDG.main` mode dispatch to `GeoGebra3D.main` before preferences setup, plus the dev fallback | — |
| packaging delta | profile declaration, schema, `--add-launcher` in the builder, verifier subcontract, spec amendment; installer shortcut decision | `--jlink-options` or `--runtime-image`, verifier and spec change | none | — |
| size / attack surface | +547 840 bytes launcher, +2 633 bytes `.cfg`; no new runtime executable | +0.7 MB; 21 general JDK executables in `runtime\bin`, including `javac`, `jshell`, `jwebserver`, `jrunscript`, `jdb`, `keytool` | none, but the product entry point (and the `.cedg` open-verb target) gains a Classic mode | — |
| development | explicit fallback, unchanged behavior | unchanged | still needs the Java fallback | — |
| portable | works (same layout) | works, with `X1-F2`/`X1-F3` unless also fixed | works | — |
| installer | works; Classic shortcuts appear in the two-step flow (`DQ-E1X1-4`) | works | works | — |
| preference isolation | preserved (explicit arguments; isolated default arguments for bare starts) | preserved | preserved | — |
| testability | pure command function; packaging contract checks the launcher, its `.cfg`, classpath and options | command function; packaging check of a general runtime | entry-point dispatch tests; process identity shared | — |
| upstream impact | none (no upstream file) | none | none | — |
| maintenance risk | jpackage launcher contract (supported option); `jpackage.app-path` is implementation-defined but already relied upon; shortcut behavior may change in later JDKs | jlink content drifts with modules; general tools shipped for one diagnostic | mode argument becomes part of the product command line; dual-identity executable | — |

Candidate D: no further viable option was found. Running Classic in-process
violates the separate-process requirement; starting a JVM through `jvm.dll` or
JNI is not a supported contract; `PATH`/environment discovery selects an
arbitrary JVM. Candidate B was exercised: with `javaw.exe` present the current
route starts Classic and the `.ggt`, the child has no JVM options and no
`jpackage.app-path`, and `Templatev7.ggb` still blocks on the pipe. jlink cannot
keep only `java`/`javaw` (`--strip-native-commands` is all-or-nothing);
deleting files from a linked image is not a supported operation. Candidate C
was evaluated statically only.

## 11. Reproduction matrix

| Environment | Open Classic | Legacy laboratory | Launcher observed | Preferences isolated |
|---|---|---|---|---|
| Gradle/dev | starts: separate `javaw.exe`, `App3D`, "GeoGebra Classic 5" | `.ggt` starts (1 macro); `Templatev7.ggb` **hangs** (`X1-F2`) | `<JDK>\bin\javaw.exe -cp <58> GeoGebra3D …`, no JVM options | yes |
| jpackage app-image (`E1-P` `INTERNAL`) | **fails**: `CreateProcess error=2` on `<app-image>\runtime\bin\java` | **fails** identically (`.ggb`, `.ggt`); `.txt` rejected before launch | none | not reached; no preference file touched |
| portable ZIP (`E1-P` `INTERNAL`) | **fails**: same on `<extract>\GeoCeDG\runtime\bin\java` | **fails** identically | none | not reached |
| packaged `INTERNAL`/`NC` equivalence | **fails** identically in the `NC`-metadata image | **fails** identically | main `.cfg` byte-identical to the product's | not reached |
| MSI/EXE (decompiled, not installed) | not run; payload has no `runtime\bin\java(w).exe` | not run | not applicable | not reached |
| scratch Candidate A (`INTERNAL`, `NC`) | starts as `GeoCeDG-Classic.exe`; options and `jpackage.app-path` present | `.ggt` starts; `.ggb` starts with discarded streams, hangs with pipes | `GeoCeDG-Classic.exe --showSplash=false --settingsfile=… [resource]` | yes |

The packaged profiles differ only in `--description`/`--copyright`, the notice
file and installer association metadata (`build-windows-package.ps1`); jpackage
behavior does not differ between `INTERNAL` and `NC`.

## 12. Upstream comparison

The pinned baseline `9b93256b` has no packaged-launcher abstraction, no
jpackage/install4j configuration and no supported Classic relaunch path:
`GeoGebra3D.main` calls `GeoGebra.doMain(args, GeoGebraFrame3D::new)`, and
`GeoGebraFrame.init` handles `--settingsfile` (`:331-334`) and the file
arguments. GeoCeDG's `GeoGebra3D.java` is unmodified from the pin. Current
upstream `main` `325faee1` (2026-10-06) has the same `GeoGebra3D.main`; code
search finds no `jpackage` and no desktop `ProcessBuilder`. The only reusable
upstream surface is the existing argument handling (`--showSplash`,
`--settingsfile`, document arguments), which every candidate uses unchanged.

## 13. Root cause, confidence and uncertainty

Primary cause, high confidence: a development-JDK filesystem assumption
(`<java.home>\bin\javaw.exe`/`java`) applied to a jlink runtime built with
`--strip-native-commands`; reproduced in the app-image, the ZIP and the
`NC`-metadata image, and explained by the jpackage default. Second defect in the
same seam, high confidence: undrained child pipes (`X1-F2`), reproduced on the
unmodified development route and isolated by the `DISCARD` comparison.
Uncertainties: MSI/EXE behavior was established from payloads, not from an
installed product; `jpackage.app-path` and the installer shortcut behavior are
JDK-implementation facts of 25.0.4, not manual-page contracts; the exact pipe
capacity and the set of sessions that log enough to block were not measured.

## 14. Observations recorded, not acted on

- `automation.legacy-laboratory` declares feature `cedg.laboratory.legacy`
  (`enabled_by_default: false`), but `GeoCeDGActionRegistry.unavailableReason`
  gates only the Locus V2 feature, so the action is available in the product.
  Pre-existing; not in scope.
- `packaging.launcher-config` (`packaging-product.ps1:707-713`) reads the first
  `*.cfg` of `app\`; a second launcher `GeoCeDG-*.cfg` sorts first and the check
  would fail. Any Candidate A implementation must make it exact.
- Concurrent sessions of the same diagnostic route share one preferences file.

## 15. Verification classification

Candidate A changes GeoCeDG-owned Desktop launch code, the package profile and
its schema, the packaging builder, the packaging verifier contract and the
packaging specification; no shared kernel, serialization, document format,
export or upstream file. The acceptance obligations are therefore Desktop unit
tests of the command construction (registered `PHASE`), verifier-contract tests
(`INFRA_UNIT`) and packaging evidence on rebuilt `INTERNAL` and `NC` artifacts
(`PACKAGING -CheckToolchain -VerifyPackagingArtifacts`), plus a packaged
process smoke. `BOUNDED_PHASE` would not cover the packaging contract;
`INTEGRATION` adds no packaging coverage, and no cross-module integration
obligation exists. The proposal is `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`
with impact `PHASE_LOCAL`, chosen from these facts and not inherited from
`E1-P`; `INTEGRATED_PHASE` is recorded as the alternative (`DQ-E1X1-8`).

## 16. Changed paths of the preparation

- `.github/prompts/tasks/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher.prompt.md` (new)
- `docs/validation/pre_g9b_r6_plus_e1_x1_preparation_characterization_report.md` (new, this report)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-x1-preparation-characterization.json` (new)
- `docs/roadmap/geocedg_roadmap.md` (one status row; documental version 4.74)
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` (status lines only)

The prompt is not added to `geocedg/specs/operations/prompt-contracts.json`,
following the `PRE-G9B-R6-plus` precedent. The `STATIC` run of the frozen
preparation candidate is reported outside this artifact, because it cannot name
its own commit.
