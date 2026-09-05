# ADR 0001: Dedicated GeoCeDG product profile

- Status: **Accepted for G2**
- Date: 2026-08-10
- Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
- Decision owners: GeoCeDG project owner

## Context

GeoCeDG needs its own application identity, preference namespace, default
perspective, feature selection, and toolbar organization while preserving
upstream Classic behavior.

The checked-out Desktop application is launched by
`org.geogebra.desktop.GeoGebra3D`
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/GeoGebra3D.java:26-34`).
It creates `GeoGebraFrame3D`, which creates `App3D`
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/app/GeoGebraFrame3D.java:31-38`).
The shared `App` field is initialized to `AppConfigDefault`
(`source/shared/common/src/main/java/org/geogebra/common/main/App.java:472`),
and `AppD` consults that configuration while its constructor is still
initializing the file version, kernel settings, and UI
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java:381-430`).
Calling `setConfig()` only after construction would therefore be too late for
a clean product boundary.

The default layouts are created centrally by
`Layout.initializeDefaultPerspectives()`
(`source/shared/common/src/main/java/org/geogebra/common/gui/Layout.java:74-123`),
and Desktop applies the chosen/default perspective during `AppD`
initialization (`AppD.java:494-497`). Toolbar definitions are parsed from the
existing numeric grammar rather than being independent widget declarations.

## Decision

Introduce a dedicated GeoCeDG launch path and configuration, without changing
the default Classic launch path:

1. Add a GeoCeDG `AppConfig` implementation in the shared application
   configuration layer. It has a dedicated product profile ID, visible name,
   and preference key.
2. Add a dedicated Desktop launcher/frame/application path that selects this
   config before `AppD` performs any config-dependent initialization.
3. Add the smallest compatible constructor/factory seam needed to inject the
   config immediately after `App` construction and before the first
   `getConfig()` use. Preserve every existing constructor and its Classic
   behavior.
4. Let the profile select a GeoCeDG default perspective and feature/tool
   policy. Keep an explicit diagnostic route to Upstream Classic.
5. Make `apps/geocedg/application-profile.yml` the durable perspective and
   toolbar source; copy it as a runtime resource and translate it to the
   existing toolbar-string adapter rather than maintaining divergent
   hard-coded strings.
6. Use profile ID `geocedg-desktop`, preference key `geocedg`, and Windows app
   ID `org.geocedg.desktop`. Isolate Desktop preferences using the existing
   settings-file mechanism.
7. Preserve `classic` as the app code serialized in `.ggb` during G2. A new
   persisted GeoCeDG app code requires a separate compatibility decision.
8. Use textual branding only. Suppress the inherited splash and frame icon in
   the GeoCeDG launcher; do not add or claim rights over upstream assets.

The exact accepted contract is
`geocedg/specs/ui/application-profile.md`; the manifest is version 1.

## Consequences

- Classic 5 remains buildable and launchable through
  `org.geogebra.desktop.GeoGebra3D`.
- GeoCeDG can own layout and availability policy without deleting upstream
  functionality.
- Shared semantic code remains frontend-independent.
- The constructor seam is a small upstream diff but must be tested because
  configuration is already read during initialization.
- GeoCeDG preferences can be audited independently from upstream materials.
- G2 files remain serialization-compatible with Classic because their header
  app code does not change.
- Final branding and distributable asset replacement remain release blockers.

## Alternatives considered

### Mutate `AppConfigDefault`

Rejected in the proposal because it globally changes Classic behavior and
increases upstream synchronization risk.

### Call `App.setConfig()` after constructing `App3D`

Rejected in the proposal because `AppD` has already read the default config
for file version, settings, kernel defaults, layout, and toolbar-related
initialization.

### Create a wholly separate Desktop Gradle module now

Deferred. The existing application module already provides the launcher,
native dependencies, tests, and distribution tasks. A separate module would
duplicate build wiring before its benefit is demonstrated.

### Assemble GeoCeDG only through scripts/macros

Rejected for the product boundary. Scripts and user tools may remain
experimental content, but they cannot own application identity or kernel
semantics.

## Acceptance validation

- Classic launcher behavior remains equivalent; the shared hooks retain their
  previous Classic defaults.
- GeoCeDG config is installed before its first read.
- Preference namespaces do not collide.
- perspective and toolbar selection are deterministic and manifest-driven;
- serialization compatibility is unchanged by selecting the profile;
- Desktop compile, launch smoke, focused profile tests, and baseline verifier
  pass;
- no new branded asset is bundled; remaining inherited-asset questions stay in
  the licensing matrix and G2 report.

## Deferred questions

- Whether a future file-format policy should introduce a persisted GeoCeDG app
  code and migration/compatibility rules.
- Whether a later independent Gradle application module reduces release
  coupling enough to justify its maintenance cost.
- Which owned and reviewed translations, icons, styles, and other assets will
  replace inherited baseline materials before redistribution.

## G9U1 Round-3 presentation supersession note

Decision item 8 and its G2 validation statement describe the historical G2
cohort; they are not a permanent ban on later provenance-cleared GeoCeDG-owned
assets. The G9U1 Round-3 implementation candidate now consumes two explicitly
author-authorized sources: `helixTopBar.png` for application/window and Windows
package icons, and `helixSnapshot.png` for startup presentation. Both are
promoted byte-exact into a versioned GeoCeDG resource namespace; every runtime
derivative is deterministic, recorded in the asset manifest and validated
without depending on the ignored ingestion directory. Classic remains unchanged
and no upstream branding is used as fallback.

The same bounded product launch seam prepares the GeoCeDG splash before it
synchronously constructs and initializes the GeoCeDG frame/application on Swing
EDT. This aligns Construction ownership with the thread used by ordinary
Algebra gestures without changing the original two-argument Classic launch path
or relaxing kernel thread confinement.

This note records an additive application-presentation evolution under ADR 0012
and the current workspace/profile specs. It does not change G2 kernel or
serialization semantics, does not declare G9U1 PASS, and does not clear the
independent public-redistribution/license gate.
