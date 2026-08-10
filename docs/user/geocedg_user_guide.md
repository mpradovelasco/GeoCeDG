# Manual operativo vivo de GeoCeDG

- Tipo de documento: manual operativo vivo
- Puerta actual del proyecto: G3 **PASS**
- Baseline: GeoGebra 5.4.928.0 at
  `9b93256b7df401ff056c37b502d82df4d72b1522`
- Plataforma validada: únicamente Windows
- Última revisión: 2026-08-10

This guide is the practical entry point for the GeoCeDG author/developer. It
describes only the behavior available through G3. It does not replace the
[repository README](../../README.md),
[roadmap](../roadmap/geocedg_initial_plan.md), ADRs, specifications, or
architecture documentation.

## Run GeoCeDG now

From an already prepared clone, open PowerShell 7 at the repository root and
run:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

Close the application window normally to let Gradle finish successfully.

For a new workstation, use:

```powershell
git clone https://github.com/mpradovelasco/GeoCeDG.git
cd GeoCeDG
.\tools\bootstrap\bootstrap-windows.ps1
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

## 1. Workstation requirements

The validated workstation profile is:

| Requirement | Current contract |
|---|---|
| Operating system | Windows 11 is validated; Linux and macOS are not currently claimed as validated |
| Git | Git for Windows, available as `git` |
| Shell | PowerShell 7 or newer, available as `pwsh` |
| Gradle launcher JVM | JDK 22 on `PATH`; the validated installation was Oracle JDK 22.0.2 |
| Desktop toolchain | A JDK 25 discoverable by Gradle; the validated runtime was Eclipse Temurin 25.0.4 |
| Gradle | Use only `gradlew.bat` from the repository; do not install or invoke a system Gradle |
| Network | Required by the normal first bootstrap to fetch `origin`, `upstream`, and tags |

The Desktop task requests Java language version 25, while Gradle itself is
launched with Java 22. Automatic toolchain download is disabled by the normal
verification path, so install or expose JDK 25 manually if Gradle cannot find
one.

The bootstrap never installs Git, PowerShell, Java, or Gradle. It does not
modify global environment variables, global Git configuration, credentials,
`origin`, branches, or history.

## 2. Clone and bootstrap

The normal onboarding sequence is:

```powershell
git clone https://github.com/mpradovelasco/GeoCeDG.git
cd GeoCeDG
.\tools\bootstrap\bootstrap-windows.ps1
```

The bootstrap:

- verifies that the directory is a GeoCeDG Git clone;
- inspects `origin` without changing it;
- adds `upstream` only when absent, using exactly
  `https://github.com/geogebra/geogebra.git`;
- refuses to overwrite a different existing `upstream` URL;
- fetches both remotes and tags;
- checks the annotated tag `geogebra-baseline-5.4.928.0` against the pinned
  baseline SHA;
- reports the launcher JVM and Desktop toolchain;
- delegates repository validation to `tools/agent/verify.ps1`;
- preserves the initial worktree status and reports `PASS`,
  `PASS WITH WARNINGS`, or `FAIL`.

It is idempotent: a second normal execution should produce the same logical
state without duplicating remotes or changing tracked files.

Useful options are:

```powershell
.\tools\bootstrap\bootstrap-windows.ps1 -SkipFetch
.\tools\bootstrap\bootstrap-windows.ps1 -SkipBuild
.\tools\bootstrap\bootstrap-windows.ps1 -RunBenchmarks
.\tools\bootstrap\bootstrap-windows.ps1 -LaunchDesktop
```

`-SkipFetch` uses existing local refs. `-SkipBuild` provides only static and
toolchain evidence. `-RunBenchmarks` adds the informational operational
benchmark. `-LaunchDesktop` launches **GeoGebra Classic**, because that option
belongs to the pinned-baseline gate; it does not launch GeoCeDG.

See the [root README](../../README.md) for the short repository overview and
[UPSTREAM.md](../../UPSTREAM.md) for baseline provenance.

## 3. Verify and build

Run commands from the repository root unless stated otherwise.

### Composed verification authority

```powershell
.\tools\agent\verify.ps1
```

This is the canonical local gate. It composes operational contracts, the G3
legacy catalog, the pinned baseline build, and focused GeoCeDG frontend tests.
It writes logs below `%TEMP%\geocedg-verify` by default and normally removes
the Gradle outputs it created.

Include the current informational benchmark with:

```powershell
.\tools\agent\verify.ps1 -RunBenchmarks
```

Optional expensive or interactive variants are:

```powershell
.\tools\agent\verify.ps1 -FullTests
.\tools\agent\verify.ps1 -LaunchDesktop
```

As with the bootstrap option, `verify.ps1 -LaunchDesktop` exercises the
baseline Classic launcher. Close its window normally to complete the gate.

### Focused verifiers

```powershell
.\tools\agent\verify-operational.ps1
.\tools\agent\verify-baseline.ps1
.\tools\agent\verify-frontend.ps1
.\tools\agent\verify-legacy.ps1
```

Use the composed verifier for an acceptance result. Use a focused verifier to
diagnose its corresponding layer.

### Desktop build

The current verified minimum Desktop compilation is:

```powershell
.\gradlew.bat :desktop:desktop:compileJava
```

There is no GeoCeDG installer or standalone packaged application yet. G4 owns
that work; at present GeoCeDG runs from the source tree through the wrapper.

## 4. Run GeoCeDG and Classic

### GeoCeDG

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

This launches `org.geocedg.desktop.GeoCeDG`, uses the GeoCeDG profile and the
`geocedg` preference namespace, and suppresses the inherited branded splash
and frame icon.

### GeoGebra Classic diagnostic reference

```powershell
.\gradlew.bat :desktop:desktop:run
```

This launches the unchanged baseline entry point
`org.geogebra.desktop.GeoGebra3D`. Use it for regression, upstream comparison,
and diagnosis. GeoCeDG and Classic use separate launch paths and preference
contexts; selecting a Classic-looking perspective inside GeoCeDG is not a
substitute for this process-level reference launch.

### Known Gradle documentation discrepancy

Some historical/upstream documentation, including the archived upstream
README and an explanatory section of the roadmap, shows `:desktop:run` from
the composite root. That selector is stale for the pinned build. The actual
root commands are:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
.\gradlew.bat :desktop:desktop:run
```

No source or Gradle file has been changed merely to hide that discrepancy.

## 5. Identify the running application

| Signal | GeoCeDG | Baseline Classic |
|---|---|---|
| Window title | `GeoCeDG` | `GeoGebra Classic 5` |
| Windows application ID in diagnostics | `org.geocedg.desktop` | `geogebra.AppId` |
| First-run layout | Algebra + 2D Graphics | Upstream Classic layout/preferences |
| Default toolbar | Six conservative GeoCeDG groups | Full upstream Classic toolbar |
| Branding | Textual GeoCeDG name; inherited splash/icon suppressed | Upstream Classic identity |
| Preferences | GeoCeDG `geocedg` namespace | Classic namespace |

A loaded document or saved preferences can override the first-run perspective.
When the title says `GeoCeDG` but the panels or toolbar differ, first determine
whether a document or saved preference supplied that layout; do not infer the
application identity from icons alone.

## 6. Current GeoCeDG GUI

The initial GeoCeDG perspective contains:

- the Algebra view and primary 2D Graphics view;
- visible axes with a unit axes ratio;
- no grid;
- the input panel and input help;
- Spreadsheet, CAS, Properties, and 3D views available but initially closed.

The stable toolbar is generated from
`apps/geocedg/application-profile.yml`. Its six current groups contain only
existing upstream modes:

| Group | Available tools |
|---|---|
| Selection and construction | Move |
| Primitives and incidence | Point, line, segment, ray, vector, polygon, parallel, perpendicular |
| Curves | Circle through two points, circle through three points, conic through five points |
| Intersections and locus | Intersection and the existing upstream Locus tool |
| Transformations | Reflection in a line, translation by vector, rotation by angle |
| Measurement and validation | Angle and distance/length |

The toolbar organization is GeoCeDG-owned, but these are not new CeDG
algorithms. G2 added no geometric mode or command. The profile currently
installs no command filter, so inherited Classic kernel commands remain
available through the normal application mechanisms even when they are not in
the reduced default toolbar.

The planned toolbar categories for descriptive projections, plane changes and
developments, CeDG tools, and import/export are explicitly **not implemented**
and are not emitted into the toolbar.

## 7. CeDG Laboratory

G3 provides an experimental, opt-in Laboratory for registered non-stable
resources. The current default resource is the preserved `Templatev7.ggb`.

Validate it without opening a window:

```powershell
.\tools\legacy\open-laboratory.ps1 -ValidateOnly
```

Open it in GeoCeDG:

```powershell
.\tools\legacy\open-laboratory.ps1
```

Open the same resource in Classic for comparison:

```powershell
.\tools\legacy\open-laboratory.ps1 -Classic
```

The loader verifies registration, maturity, original SHA-256, and deterministic
inventory before launch. It prints `EXPERIMENTAL / NON-STABLE RESOURCE` and
uses temporary Laboratory preference files.

`Templatev7.ggb` contains 24 historical macros and seven custom toolbar
groups. Its document-owned toolbar is the authoritative **legacy interface
reference** and appears when the document is loaded. It does not replace the
stable G2 toolbar globally, define the future GeoCeDG toolbar, or promote any
macro to a native GeoCeDG command.

In particular, `postLocus`, `listLength`, `listLength12`, and `SplineLength`
are research evidence based on legacy/sampled procedures. They are not Locus
V2 and are not exact or native metric authority.

## 8. Maturity states

| State | Meaning in GeoCeDG |
|---|---|
| `legacy` | Preserved historical resource awaiting sufficient characterization; not stable product behavior |
| `research` | Scientific or exploratory behavior without an approved stable public contract |
| `experimental` | Integrated behind explicit opt-in or a feature flag, with a specification and tests, but not enabled as stable default behavior |
| `stable` | Documented, tested, backward-compatible repository/product contract approved for normal use |
| `deprecated` | Retained only for compatibility, with a documented replacement or migration path |

The current stable feature manifest contains the GeoCeDG frontend profile,
the Classic diagnostic route, and the **ingest infrastructure**. The Laboratory
itself is experimental and disabled by default. An infrastructure feature can
be stable while the resources it manages remain legacy or research.

Promotion follows `legacy -> research -> experimental -> stable`, or
`experimental -> deprecated`; it is always an explicit review decision.

## 9. Capabilities by completed phase

| Phase | Capability | Current status | What that means now |
|---|---|---|---|
| G0 | Pinned GeoGebra 5.4.928.0 baseline, tag, provenance, license inventory, shared/Desktop build and Classic launch | Available; infrastructure | Reproducible diagnostic foundation with no functional GeoCeDG change |
| G1/G1R | Prompts, manifests/schemas, CI definition, composed verifier, benchmark harness, Windows bootstrap and GeoCeDG README | Available; infrastructure | Reproducible development/onboarding; benchmark budgets remain informational |
| G2 | Dedicated GeoCeDG launcher, identity, preference namespace, initial perspective and manifest-driven toolbar | Available; stable | Recognizable GeoCeDG application over the Classic 5 runtime |
| G2 | Separate Classic launcher for diagnosis | Available; stable | Process-level baseline comparison remains possible |
| G2 | New CeDG geometry, command filtering, final branding or packaging | Pending | G2 deliberately introduced none of these |
| G3 | Immutable legacy ingest, hashes, schemas, catalogs, scientific references and `Templatev7` inventory | Available; stable infrastructure | Resources can be preserved and checked reproducibly |
| G3 | CeDG Laboratory loader | Experimental | Explicitly invoked and disabled by default |
| G3 | `Templatev7.ggb` and its 24 tools | Available as legacy/research material | Loadable for inspection and comparison, not promoted to stable GeoCeDG behavior |
| G3 | Public book-model corpus | Infrastructure only | Metadata for 71 remote models; no bulk download and no local regression promotion |
| G3 | Per-tool geometric correctness, validity domains, degeneracies and expected numerical results | Pending | Required before any tool can be promoted |

## 10. Planned phases not yet implemented

| Phase | Planned area | Current status |
|---|---|---|
| G4 | Own installer and packaging | Pending |
| G5 | Reproducible 2D DXF export | Pending |
| G6-G8 | Locus V2 characterization, native length and 2D intersections | Pending |
| G9 | Native spatial identity and canonical projection semantics | Pending |
| G10 | CeDG 3D DSL and workbench | Pending |
| G11 | Hierarchical layers and view states | Pending |
| G12 | Extended navigation, zoom and physical scales | Pending |
| G13 | Geometric visibility in projections | Pending |
| G14 | Derived bridge to the 3D view | Pending |
| G15 | Drawing sheets and advanced PDF/SVG output | Pending |
| G16 | Profile-driven performance and scalability work | Pending |

The existing Classic runtime already has many general 2D/3D facilities. Their
presence must not be reported as implementation of these future GeoCeDG
semantic contracts.

## 11. Current limitations

- GeoCeDG must currently be run from source; there is no G4 installer or
  standalone distribution.
- Windows is the only validated workstation platform.
- Branding is textual and provisional. There is no final GeoCeDG logo, icon,
  translation set, style, or installer resource.
- The runtime still uses inherited Classic strings and UI assets. Public or
  commercial redistribution requires the recorded license/brand review.
- `.ggb` serialization still uses the upstream `classic` app code for
  compatibility; GeoCeDG has no new persisted app code.
- No new geometric semantics, native CeDG command, Locus V2 behavior, spatial
  object/projection identity, DXF service, DSL, or packaging has been added.
- Legacy macros may have undocumented validity ranges, degeneracies, dynamic
  limitations, and sampled numerical approximations.
- The 71-model public corpus is an external metadata index, not a local mirror
  or build dependency.
- CI is defined to call the same Windows verifier, but a hosted CI result must
  be established by the hosting service and is not inferred from local checks.

## 12. Development quick reference

```powershell
# Repository state
git status --short

# Canonical gate
.\tools\agent\verify.ps1

# Canonical gate plus informational benchmark
.\tools\agent\verify.ps1 -RunBenchmarks

# Focused G3 catalog/ingest validation
.\tools\agent\verify-legacy.ps1

# Validate the Laboratory resource without GUI
.\tools\legacy\open-laboratory.ps1 -ValidateOnly

# Desktop compilation
.\gradlew.bat :desktop:desktop:compileJava

# GeoCeDG
.\gradlew.bat :desktop:desktop:runGeoCeDG

# Baseline Classic
.\gradlew.bat :desktop:desktop:run

# Whitespace before handoff
git diff --check
```

Do not commit Gradle outputs or generated benchmark evidence. Generated
evidence belongs outside durable sources or under the ignored `artifacts/`
boundary defined by the operational contracts.

## 13. Technical references

- Governance: [AGENTS.md](../../AGENTS.md)
- Baseline provenance: [UPSTREAM.md](../../UPSTREAM.md) and
  [G0 report](../validation/baseline_report.md)
- Roadmap: [GeoCeDG initial plan](../roadmap/geocedg_initial_plan.md)
- Operational authority: [ADR 0002](../adr/0002-g1-operational-authority.md),
  [G1 report](../validation/g1_operational_layer_report.md), and
  [G1R report](../validation/g1r_repository_onboarding_report.md)
- Product profile: [ADR 0001](../adr/0001-geocedg-product-profile.md),
  [profile specification](../../geocedg/specs/ui/application-profile.md),
  [runtime manifest](../../apps/geocedg/application-profile.yml), and
  [G2 report](../validation/g2_frontend_foundation_report.md)
- Legacy integration: [ADR 0003](../adr/0003-controlled-legacy-integration.md),
  [integration specification](../../geocedg/specs/legacy/controlled-integration.md),
  [Templatev7 manifest](../../models/legacy/template-v7/manifest.yml), and
  [G3 report](../validation/g3_controlled_legacy_integration_report.md)
- Current feature state:
  [stable manifest](../../geocedg/features/stable.yml) and
  [experimental manifest](../../geocedg/features/experimental.yml)
- Scientific sources and public models:
  [CeDG reference catalog](../references/cedg/catalog.yml) and
  [public model corpus](../references/cedg/public-model-corpus.yml)
- Build topology:
  [upstream module map](../architecture/upstream_module_map.md)
- Redistribution constraints:
  [component/license matrix](../licensing/component-matrix.md)

## 14. Maintenance rule

Update this guide whenever a roadmap phase closes. The update must reflect the
actual manifests, accepted ADRs, validation report, executable commands, GUI,
maturity states, completed capabilities, limitations, and newly pending work.
Planned behavior must remain labeled as pending until its gate passes.
