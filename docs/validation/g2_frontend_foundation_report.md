# G2 GeoCeDG frontend foundation report

Status: **PASS**
Date: 2026-08-10
Branch: `feature/g2-geocedg-frontend`
Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
GeoGebra: `5.4.928.0`

## Scope and architecture analysis

G2 establishes an application-layer product profile over the pinned Classic 5
Desktop runtime. The implementation follows the observed construction chain
`GeoGebra3D -> GeoGebraFrame3D -> App3D -> AppD`, installs a dedicated config
before `AppD` first reads it, and adapts the existing `Perspective` and toolbar
grammar. It does not fork the kernel, duplicate the Desktop application module,
or reinterpret GUI state as geometry.

The durable authorities are ADR 0001,
`geocedg/specs/ui/application-profile.md`, its versioned schema, and
`apps/geocedg/application-profile.yml`. The G2 task prompt records the exact
scope and stop conditions. Stable feature manifests register the product
profile and the Classic diagnostic route.

## Profile, launcher, perspective, and toolbar

`AppConfigGeoCeDG` supplies the textual name `GeoCeDG`, profile ID
`geocedg-desktop`, and preference key `geocedg`. It deliberately returns the
upstream `classic` app code, so G2 creates no new `.ggb` serialization contract.
Its command-filter factory remains unrestricted and is only a seam for a later
approved policy.

`org.geocedg.desktop.GeoCeDG` is the explicit launcher selected by Gradle task
`:desktop:desktop:runGeoCeDG`. It suppresses the inherited splash and frame icon
and uses an isolated settings file below `%APPDATA%\GeoCeDG\5.4` unless the user
supplies `--settingsFile`. `GeoCeDGFrame` and `AppGeoCeDG` preserve the profile
for new windows and template helpers.

The manifest-defined initial perspective shows Algebra and the primary 2D
Euclidian view, keeps other inherited views available but closed, shows axes
with unit ratio, hides the grid, and keeps the input panel. Saved/document
perspectives take precedence.

The initial toolbar is generated from six ordered manifest categories using
only baseline modes: selection/construction, primitives/incidence, curves,
intersections/locus, transformations, and measurement/validation. Planned
projection, development, CeDG-tool, and import/export categories remain
explicitly `not-implemented`; G2 adds no operation, command, macro, or legacy
tool.

## Coexistence with Classic

The upstream `org.geogebra.desktop.GeoGebra3D` main class and
`:desktop:desktop:run` task remain the process-level Classic diagnostic route.
The small shared hooks retain their old Classic title, AppUserModelID, config,
icon, and constructor defaults. Separate launchers avoid preference or profile
mixing in one JVM.

## Controlled inherited-tree changes

Thirteen files below the inherited tree are added or minimally modified and
registered with purpose and authority in `docs/upstream/modified-files.yml`.
The modified upstream files are:

- `AppD.java` and `App3D.java`: protected early-config constructor seam and
  optional frame-icon hook;
- `GeoGebraFrame.java` and `GuiManagerD.java`: profile-aware title/AppID hooks
  with Classic defaults;
- `source/desktop/desktop/build.gradle.kts`: packaged profile resource and
  launcher task;
- the inherited checkstyle contract: accept GeoCeDG-owned SPDX headers while
  preserving all other checks.

The remaining registered paths are new `org.geocedg` source/test files. The
baseline SHA is unchanged and no source from a later upstream commit is
included. Both baseline and operational verifiers compare the actual inherited
tree diff with this exact allowlist.

## Operational integration and files

Created durable assets include the application manifest, UI schema/spec,
frontend task prompt, shared/Desktop profile code and tests, controlled-upstream
schema/register/helper, focused frontend verifier, and this report. README,
UPSTREAM, architecture maps, ADRs, stable feature manifest, Desktop build, and
the composed verifiers are updated coherently.

`tools/agent/verify.ps1` remains the composed authority. It still runs the G1
operational and G0 baseline verifiers, then delegates focused G2 tests to
`verify-frontend.ps1`; benchmark behavior remains unchanged. CI needs no
workflow expansion because it already calls that authority and does not launch
interactive applications.

## Semantic and compatibility effect

Geometric semantic effect: none. Kernel dependency effect: none. Locus effect:
none. Projection/spatial semantics effect: none. `.ggb` app-code effect: none.
Classic remains available for comparison and existing default constructors
retain their behavior. G3 legacy-tool import and every later roadmap feature
remain outside this change.

## Validation evidence

| Gate | Command or method | Exit code | Result/evidence |
|---|---|---:|---|
| Operational contracts | `.\tools\agent\verify-operational.ps1` | `0` | schemas/manifests, prompts, CI, text hygiene, no G3 import, and exact 13-file inherited-tree register passed |
| Independent schema validation | Python `jsonschema` Draft 2020-12 over feature sets, application profile, and upstream modification register | `0` | four schema/instance pairs passed; no dependency was added |
| Direct baseline | `.\tools\agent\verify-baseline.ps1` | `0` | shared compile `BUILD SUCCESSFUL in 23s`; Desktop compile `BUILD SUCCESSFUL in 1m 17s` |
| Focused Checkstyle/tests | shared `AppConfigGeoCeDGTest` and Desktop `GeoCeDGProfileTest`, composed by `verify-frontend.ps1` | `0` | Checkstyle main/test passed; 3 shared and 4 Desktop tests, zero failures/errors/skips |
| Final composed authority | `.\tools\agent\verify.ps1 -RunBenchmarks` | `0` | all gates passed in 285.3 seconds; shared build 23s and Desktop build 58s |
| Informational benchmark | composed `verify-operational` benchmark | `0` | median 925.491 ms, within the 5000 ms informational budget |
| GeoCeDG launch | `:desktop:desktop:runGeoCeDG` with temporary settings | `0` | title `GeoCeDG`, AppID log `org.geocedg.desktop`, Algebra+Graphics layout, six toolbar groups, axes shown, grid hidden |
| Classic diagnostic launch | `:desktop:desktop:run` with temporary settings | `0` | title `GeoGebra Classic 5`, upstream toolbar/perspective retained |
| Whitespace/status/outputs | `git diff --check`, verifier status transaction, and generated-directory enumeration | `0` | no whitespace error, status restored, zero residual `build`/`.gradle`/`.kotlin` directories |

Gradle used Oracle Java 22.0.2 for the wrapper/daemon and Eclipse Temurin
25.0.4+7-LTS for both Desktop launch tasks. Final logs and benchmark evidence
are outside the repository under
`C:\Users\usuario\AppData\Local\Temp\geocedg-g2-final\final-composed`.
Visual captures and launch logs are under its sibling `visual` directory:
`geocedg-window-corrected.png` and `classic-window.png`.

The first GeoCeDG visual diagnostic exposed that upstream treats perspective
ID `0` as a document-defined perspective and intentionally skips axes/grid
policy. The GeoCeDG adapter was corrected to identify its manifest perspective
as a product default while retaining ID `geocedg-initial`; a focused assertion,
Checkstyle, tests, composed verification, and visual launch were rerun. The
corrected capture shows the manifest-required grid-off state.

## Limitations and pending matters

- Branding is textual and provisional. No GeoCeDG logo, icon, translation,
  style, installer, or final distributable resource is supplied.
- The application still consumes inherited Classic UI strings and assets at
  runtime. Redistribution remains subject to the recorded license/brand audit.
- Preference isolation is process-level and Windows-only validation; Linux and
  macOS are not claimed as validated platforms.
- G2 intentionally defines no command filter and loads no experimental tool.
- Packaging, user-selectable in-process profile switching, and a new persisted
  GeoCeDG app code remain deferred decisions.
- CI configuration is locally checked, but only the hosted service can provide
  a hosted-run result.

G2 has not started G3, Locus V2, spatial/projection semantics, DXF, packaging,
or any geometric feature. All required local gates and both visual launch
checks pass. G2 is **PASS**.
