# G1R repository onboarding and workstation reproducibility report

Status: **PASS**
Date: 2026-08-09
Branch: `feature/g1-operational-layer`
G1 commit: `89a66c8773e0a17f1210c7cdd9946a05bbc39e78`
Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
GeoGebra: `5.4.928.0`

## Scope

G1R extends the approved operational layer with repository onboarding and a
reproducible Windows workstation entry point. It changes documentation and
GeoCeDG-owned operational scripts only. It does not alter inherited source,
the Gradle build, geometric semantics, serialization, `Locus`, or product
behavior.

## Files created and modified

Created:

- `.gitattributes`;
- `docs/upstream/GEOGEBRA_README.md`;
- `tools/bootstrap/bootstrap-windows.ps1`;
- this report.

Modified:

- root `README.md`;
- `UPSTREAM.md` and baseline/licensing references to the archived README;
- `tools/agent/verify.ps1`, `verify-baseline.ps1`, and
  `verify-operational.ps1`.

`.github/workflows/verify.yml` was reviewed and did not require a change. It
already uses Windows, fetches full history and tags, selects Java 22, and calls
the composed authority. Running workstation bootstrap in CI would mix
onboarding with repository verification.

## Bootstrap design and ADR 0002

`tools/bootstrap/bootstrap-windows.ps1` owns onboarding and environment
diagnostics. It resolves the repository root, checks clone identity, inspects
`origin`, adds only the exact official `upstream` when absent, fetches refs and
tags, verifies the annotated baseline tag, checks external commands and
toolchains, and prints `PASS`, `PASS WITH WARNINGS`, or `FAIL`.

It delegates all repository gates to `tools/agent/verify.ps1`, preserving ADR
0002's top-level authority. `verify-operational.ps1` and
`verify-baseline.ps1` remain subordinate. The compatible `-SkipBuild` path
continues to run static provenance, version, wrapper and toolchain checks; it
only omits compilation and cannot be combined with interactive launch.

Supported bootstrap switches are limited to `-SkipFetch`, `-SkipBuild`,
`-RunBenchmarks`, and `-LaunchDesktop`. Default execution fetches and compiles
without opening a graphical application.

## Workstation requirements and manual steps

- Windows;
- Git for Windows;
- PowerShell 7 or newer;
- JDK 22 selected through `JAVA_HOME`/`PATH` for the Gradle launcher;
- a locally detectable JDK 25 toolchain for Desktop run;
- network access for fetch unless `-SkipFetch` is intentionally used.

The user must install or select missing tools manually. The bootstrap does not
install Git, PowerShell, Java, or Gradle; modify global environment or Git
configuration; store credentials; modify `origin`; change branch or history;
or publish anything. Desktop launch remains an explicit interactive action.

## README transition and upstream preservation

The inherited root README was replaced by a concise GeoCeDG onboarding entry
point. It now describes the independent experimental fork, exact baseline,
Windows bootstrap, verification and correct composite Desktop route, remotes,
branch workflow, repository authorities, operational directories, and the
license/branding boundary.

The upstream baseline README is preserved at
`docs/upstream/GEOGEBRA_README.md`. Its Git blob is checked against
`geogebra-baseline-5.4.928.0:README.md`; `.gitattributes` fixes LF checkout and
exempts only its inherited trailing spaces from whitespace diagnostics. The
archived document retains the stale root `:desktop:run` instruction. GeoCeDG
documents the validated `:desktop:desktop:run` selector without changing
upstream build logic.

## Idempotence and validation results

The default bootstrap was executed twice consecutively against the same
staged tree. Both runs inspected the same `origin`, found the already-correct
single `upstream`, resolved the same annotated tag, detected the same
toolchains, compiled from regenerated outputs, returned `PASS`, restored zero
Gradle output directories, and preserved the same 11 staged status entries.
No remote was duplicated and no working-tree file changed.

| Gate | Command | Exit code | Result |
|---|---|---:|---|
| Bootstrap run 1 | `.\tools\bootstrap\bootstrap-windows.ps1` | `0` | `PASS` in 118.8 s; shared `BUILD SUCCESSFUL in 23s`; Desktop `BUILD SUCCESSFUL in 1m 1s` |
| Bootstrap run 2 | `.\tools\bootstrap\bootstrap-windows.ps1` | `0` | `PASS` in 122.0 s; shared `BUILD SUCCESSFUL in 27s`; Desktop `BUILD SUCCESSFUL in 1m 3s` |
| Composed authority and benchmark | `.\tools\agent\verify.ps1 -RunBenchmarks` with temporary log/output paths | `0` | shared `24s`; Desktop `1m 5s`; operational median 573.406 ms, within 5000 ms informational budget |
| Baseline component | `.\tools\agent\verify-baseline.ps1` with a temporary log path | `0` | shared `23s`; Desktop `1m 6s`; archived README blob and toolchains verified |
| Operational schemas/manifests | `.\tools\agent\verify-operational.ps1` | `0` | prompt, onboarding, JSON-compatible YAML/schema subset, catalogs, CI, text and upstream boundaries passed |
| Whitespace | `git diff --check` and `git diff --cached --check` | `0` | GeoCeDG-owned text clean; exact upstream README exception is path-scoped |
| Generated outputs | Git enumeration of untracked/ignored `build`, `.gradle`, and `.kotlin` directories | `0` | zero residual directories |

The bootstrap and final composed logs are outside the repository at
`C:\Users\usuario\AppData\Local\Temp\geocedg-bootstrap` and
`C:\Users\usuario\AppData\Local\Temp\geocedg-g1r-verify`. Independent
baseline logs are under
`C:\Users\usuario\AppData\Local\Temp\geocedg-g1r-baseline`.

Observed toolchains were Gradle 9.4.1 launched by Oracle Java 22.0.2 and an
Eclipse Temurin 25.0.4+7-LTS Desktop toolchain at
`C:\Users\usuario\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2`.
Automatic toolchain download remained disabled.

One earlier bootstrap diagnostic was terminated by an external five-minute
command timeout after a slow fetch; shared compilation had passed and Desktop
was still compiling. That incomplete attempt was not accepted as evidence.
Its 20 known regenerable directories were inspected, removed, and followed by
the two successful full bootstrap executions above.

## Limitations

- Only Windows is validated.
- The default bootstrap detects the Java 25 toolchain but does not launch the
  GUI; `-LaunchDesktop` is required to exercise the application JVM.
- Fetch requires whatever GitHub network access and credentials are already
  configured by the user.
- Full optional test suites, packaging, and G2 functionality are outside G1R.

## Gate conclusion

G1R is **PASS**. The repository has a concise GeoCeDG entry point, the exact
upstream README remains reproducible from the pinned tag, onboarding is
idempotent on the validated Windows workstation, all required authorities
pass, and generated outputs are absent. G2 has not started.
