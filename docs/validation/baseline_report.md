# GeoCeDG baseline verification report

Status: **baseline accepted; bootstrap characterization complete**
Verification date: 2026-08-09
Upstream baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
GeoGebra version: `5.4.928.0`

This report records the reproducible evidence for the first repository
mission. It does not authorize a GeoCeDG product feature or change any
upstream source.

## Outcome

The adopted commit compiles the shared canvas/renderer and Desktop modules and
starts GeoGebra Classic 5 successfully. The `bootstrap/geocedg-baseline`
branch was reconstructed from that exact commit by replaying only the two
pre-existing GeoCeDG bootstrap documentation commits. The annotated tag
`geogebra-baseline-5.4.928.0` peels directly to the upstream commit.

No commit after the baseline contributes a file under `source/`, `gradle/`,
`doc/dev/`, or the root Gradle files. G1R later moved the exact upstream README
to `docs/upstream/GEOGEBRA_README.md` and made the root README GeoCeDG-owned;
the executable gate checks the archived file against the baseline tag blob.

## Identity and provenance

| Item | Verified value |
|---|---|
| Upstream remote | `https://github.com/geogebra/geogebra.git` |
| GeoCeDG remote | `https://github.com/mpradovelasco/GeoCeDG` |
| Baseline SHA | `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Commit subject | `Update translation files` |
| Immediate successor excluded | `52f608d` and all later upstream changes |
| GeoGebra version constant | `5.4.928.0` |
| Runtime banner | `GeoGebra 5.4.928.0 28 July 2026 Java 25.0.4-64bit` |
| Annotated tag | `geogebra-baseline-5.4.928.0` |
| Tag target | `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Bootstrap branch | `bootstrap/geocedg-baseline` |

The public GitHub repository identifies itself as a mirror of GeoGebra's
private GitLab development repository
(`docs/upstream/GEOGEBRA_README.md:4-8`) and gives the GitLab clone URL to
contributors (`docs/upstream/GEOGEBRA_README.md:38-46`). GeoCeDG therefore
uses the public mirror for fetching and immutable SHA pinning, not as evidence
that a moving GitHub branch is an approved baseline or contribution endpoint.

## History reconstruction

The discarded branch had an upstream base of `b20c13cee` followed by two
GeoCeDG-only commits:

```text
13aec3d  AGENTS.md, FIRST_AGENT_TASK.md,
         docs/roadmap/geocedg_roadmap.md
656718d  docs/architecture/proposed_spatial_projection_semantics.md
```

Those two changes were replayed, without their former upstream ancestry, onto
the adopted SHA. Git assigned these replacement commits:

```text
9fe5ab35b  Add GeoCeDG bootstrap planning and agent instructions
f28efa347  State pre first task to build GeoCeDG
9b93256b7  Update translation files
```

This is an intentional local history reconstruction. The remote branch was
not rewritten or pushed as part of this task.

## Gradle topology and canonical Desktop route

The repository root is a composite build that includes build logic, shared,
Desktop, and Web builds (`settings.gradle.kts:2-6`). Task discovery at the
baseline established these selectors:

```powershell
# Composite root
.\gradlew.bat :desktop:desktop:compileJava
.\gradlew.bat :desktop:desktop:run

# Included build directly
Set-Location .\source\desktop
..\..\gradlew.bat :desktop:compileJava
..\..\gradlew.bat :desktop:run
```

Only the root wrapper is tracked. Invoking it through `..\..\gradlew.bat`
while the current directory is an included build makes that included build the
Gradle project root.

The upstream README's root selector `:desktop:run` is stale for this composite
layout (`docs/upstream/GEOGEBRA_README.md:28-34`). It is a documentation
discrepancy, not a baseline failure. No upstream source or Gradle file was
modified to hide or correct it.

The Desktop included build resolves shared GeoGebra module coordinates to the
local `source/shared` included build. In particular, local project dependency
substitution supplies `org.geogebra:canvas-base`; no later published
`canvas-base` artifact is needed for this baseline. The project topology and
evidence are detailed in `docs/architecture/upstream_module_map.md`.

## Toolchain

| Layer | Actual toolchain |
|---|---|
| Gradle wrapper | Gradle `9.4.1` |
| Gradle launcher/daemon JVM | Oracle Java `22.0.2` |
| Desktop `run` task request | Java language version `25`, from `source/desktop/desktop/build.gradle.kts:20-24` |
| Resolved application JVM | Eclipse Temurin `25.0.4+7-LTS`, 64 bit |
| Resolved application executable | `C:\Users\usuario\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2\bin\java.exe` |

The Java 25 installation was already present in the Gradle toolchain cache.
The verifier disables automatic toolchain download by default; it will not
silently install software.

## Verification evidence

### Isolated candidate worktree

Before reconstructing the bootstrap branch, the commit was tested in an
isolated detached worktree. All commands exited `0`:

| Working directory | Command | Result | Log |
|---|---|---|---|
| `source/shared` | `..\..\gradlew.bat :canvas-base:compileJava :renderer-base:compileJava --rerun-tasks --no-build-cache --stacktrace` | `BUILD SUCCESSFUL in 30s` | `C:\Users\usuario\AppData\Local\Temp\geocedg-baseline-search-9b93256\shared-compile.log` |
| repository root | `.\gradlew.bat :desktop:desktop:compileJava --rerun-tasks --no-build-cache --stacktrace` | `BUILD SUCCESSFUL in 1m 35s` | `C:\Users\usuario\AppData\Local\Temp\geocedg-baseline-search-9b93256\desktop-compile.log` |
| repository root | `.\gradlew.bat :desktop:desktop:run` | application window opened and responded; Gradle completed after controlled close | `C:\Users\usuario\AppData\Local\Temp\geocedg-baseline-search-9b93256\desktop-run-interactive.log` |

### Reconstructed primary tree

The same gates were repeated after the history reconstruction. All commands
exited `0`:

| Working directory | Command | Result | Log |
|---|---|---|---|
| `source/shared` | `..\..\gradlew.bat :canvas-base:compileJava :renderer-base:compileJava --rerun-tasks --no-build-cache --no-daemon --stacktrace` | `BUILD SUCCESSFUL in 25s`; 11 tasks executed | `C:\Users\usuario\AppData\Local\Temp\geocedg-rebuilt-baseline-verification\shared-compile-clean-escalated.log` |
| repository root | `.\gradlew.bat :desktop:desktop:compileJava --rerun-tasks --no-build-cache --no-daemon --stacktrace` | `BUILD SUCCESSFUL in 1m 29s`; 23 tasks executed | `C:\Users\usuario\AppData\Local\Temp\geocedg-rebuilt-baseline-verification\desktop-compile-clean-escalated.log` |
| repository root | `.\gradlew.bat :desktop:desktop:run` | window title `GeoGebra Classic 5`; process responsive; banner matched the pinned version; Gradle `BUILD SUCCESSFUL in 40s` after controlled close | `C:\Users\usuario\AppData\Local\Temp\geocedg-rebuilt-baseline-verification\desktop-run-escalated.log` |

The launch log contains warnings that user preference, default, and macro files
were absent in the local roaming profile. They did not prevent initialization,
view registration, menu creation, a responsive window, or a successful Gradle
result. They are non-blocking user-environment defaults, not source failures.

### Final automated gate

After completing the bootstrap deliverables, the staged tree was verified with:

```powershell
.\tools\agent\verify-baseline.ps1
```

The command exited `0` in 89.7 seconds. Its direct shared build was
`BUILD SUCCESSFUL in 26s` with 11 executed tasks, and its root-composite
Desktop build was `BUILD SUCCESSFUL in 55s` with 23 executed tasks. Logs are
outside the repository at:

- `C:\Users\usuario\AppData\Local\Temp\geocedg-verify-baseline\java-version.log`;
- `C:\Users\usuario\AppData\Local\Temp\geocedg-verify-baseline\gradle-version.log`;
- `C:\Users\usuario\AppData\Local\Temp\geocedg-verify-baseline\shared-compile.log`;
- `C:\Users\usuario\AppData\Local\Temp\geocedg-verify-baseline\desktop-compile.log`.

The script removed the output directories created by that run and verified
that the final Git status exactly matched the initial staged status.

Two development preflights exited `1` before any new baseline result was
accepted. The first exposed trailing whitespace in the replayed bootstrap
documents and an error-reporting defect; the second exposed the incorrect
assumption that each included build had its own wrapper plus an empty-set
binding defect in cleanup. Both defects were corrected in bootstrap-owned
files. The successful gate above is the authoritative automated result.

### Windows sandbox classification

One clean `renderer-base` rebuild inside the restricted Windows sandbox failed
because its isolated Java worker could not read already compiled local
`canvas-base` classes. Dependency inspection showed the correct local project
substitution, the class files existed, and they were readable from the parent
process. A clean rerun under the managed host permission boundary passed, as
did Desktop compilation and launch.

This is recorded as a Windows sandbox/worker permission symptom. It is not
accepted as a known upstream compilation failure and no source or Gradle file
was patched to compensate for it.

## Executable verification authority

Default meaningful gate:

```powershell
.\tools\agent\verify-baseline.ps1
```

Optional expensive tests and interactive launch:

```powershell
.\tools\agent\verify-baseline.ps1 -FullTests
.\tools\agent\verify-baseline.ps1 -LaunchDesktop
```

The script verifies the pin, tag, version, Java 25 request, upstream-tree
immutability, whitespace, Java/Gradle versions, direct shared compilation, and
composite Desktop compilation. It snapshots repository state, writes logs only
under the OS temporary directory, removes only newly created `build`,
`.gradle`, or `.kotlin` directories inside the repository, and requires the
final status to equal the initial status.

No optional baseline timing switch was added. The pinned source has profiler
classes and isolated wall-clock assertions, but no deterministic reusable
benchmark harness suitable for a non-gating baseline mode without adding new
infrastructure or dependencies.

## Gate summary

| Gate | Status | Evidence or reason |
|---|---|---|
| Exact upstream SHA recorded | PASS | `docs/upstream/BASELINE_COMMIT.txt` |
| Version `5.4.928.0` recorded and observed | PASS | source constant and runtime banner |
| Annotated tag peels to upstream SHA | PASS | Git object/tag inspection |
| No later upstream code in bootstrap history | PASS | path-restricted diff against baseline |
| Shared canvas/renderer clean compilation | PASS | two independent runs above |
| Desktop clean compilation | PASS | two independent runs above |
| Desktop interactive launch | PASS | responsive window, matching banner, clean close |
| Meaningful deterministic verifier | PASS | `tools/agent/verify-baseline.ps1` |
| Full shared/Desktop test suites | NOT RUN | outside the minimum baseline gate; exposed by `-FullTests` |
| Packaging smoke | NOT RUN | no packaging change and no upstream `jpackage` task |
| Reusable benchmark timing | DEFERRED | no suitable checked-in deterministic harness |
| License/asset source inventory | PASS WITH RELEASE BLOCKERS | `docs/licensing/component-matrix.md` |

## Files and architectural effect

The task creates only bootstrap documentation and the verification entry point:

- `UPSTREAM.md` and the machine-readable baseline pin;
- architecture/module/extension-point characterization;
- a Proposed product-profile ADR;
- licensing and asset inventory;
- this validation report;
- `tools/agent/verify-baseline.ps1`.

Affected layer: repository governance, architecture documentation, licensing
inventory, and validation tooling. Semantic effect: none. Serialization and
runtime compatibility effect: none. No Locus, intersection, spatial object,
projection, toolbar, branding, export, installer, dependency, or upstream code
was changed.

## Deferred work and proposed next task

Before product implementation, maintainers should review the Proposed product
profile ADR and authorize the next narrow bootstrap task: choose the license
for GeoCeDG-authored material, create the root legal/provenance records from
verified sources, and decide whether to establish a deterministic benchmark
harness. No frontend or geometric-semantic work should begin until those gates
and the baseline report are accepted.
