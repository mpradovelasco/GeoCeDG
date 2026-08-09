# ADR 0001: Dedicated GeoCeDG product profile

- Status: **Proposed**
- Date: 2026-08-09
- Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
- Decision owners: pending human review

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

## Proposed decision

Introduce a dedicated GeoCeDG launch path and configuration, without changing
the default Classic launch path:

1. Add a GeoCeDG `AppConfig` implementation in the shared application
   configuration layer.
2. Add a dedicated Desktop launcher/frame/application path that selects this
   config before `AppD` performs any config-dependent initialization.
3. Add the smallest compatible constructor/factory seam needed to inject the
   config immediately after `App` construction and before the first
   `getConfig()` use. Preserve every existing constructor and its Classic
   behavior.
4. Let the profile select a GeoCeDG default perspective and feature/tool
   policy. Keep an explicit diagnostic route to Upstream Classic.
5. Make a future GeoCeDG UI manifest the durable toolbar source; compile it to
   the existing toolbar-string adapter rather than maintaining divergent
   hard-coded strings.
6. Give GeoCeDG its own application code and preference namespace before any
   user-facing release.

This ADR does not approve class names, constructor signatures, toolbar
contents, branding assets, commands, or feature flags. Those details require
a focused implementation design and tests.

## Consequences if accepted

- Classic 5 remains buildable and launchable through
  `org.geogebra.desktop.GeoGebra3D`.
- GeoCeDG can own layout and availability policy without deleting upstream
  functionality.
- Shared semantic code remains frontend-independent.
- The constructor seam is a small upstream diff but must be tested because
  configuration is already read during initialization.
- GeoCeDG preferences and assets can be audited independently from upstream
  materials.

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

## Required validation before acceptance

- Classic launcher behavior is byte-for-byte/behaviorally unchanged where
  applicable.
- GeoCeDG config is installed before its first read.
- Preference namespaces do not collide.
- perspective and toolbar selection are deterministic and manifest-driven;
- serialization compatibility is unchanged by selecting the profile;
- Desktop compile, launch smoke, focused profile tests, and baseline verifier
  pass;
- licensing review approves every bundled GeoCeDG/upstream asset.

## Open questions

- Exact application code and preference key format.
- Whether the minimal injection seam belongs in `AppD`, a factory, or a new
  pre-initialization parameter object.
- Exact feature/profile manifest schema.
- Whether a later independent Gradle application module reduces release
  coupling enough to justify its maintenance cost.
