# Objective

Maintain the G2 GeoCeDG application profile and Desktop frontend foundation
without changing geometric or serialization semantics.

# Authority and evidence hierarchy

1. `AGENTS.md`.
2. `docs/adr/0001-geocedg-product-profile.md`.
3. `geocedg/specs/ui/application-profile.md` and its manifest/schema.
4. `docs/roadmap/geocedg_initial_plan.md`.
5. Current pinned source and the G0/G1/G1R reports.

# Scope

GeoCeDG product identity, early `AppConfig` selection, initial perspective,
manifest-driven toolbar, Desktop launcher, Classic diagnostic coexistence,
focused frontend tests, and the minimum operational verification needed for
those contracts.

# Explicitly forbidden scope

No geometric command, kernel semantic change, Locus change, projection/3D
semantic layer, `.ggb` serialization change, legacy tool import, installer,
DXF, packaging, final branding, or upstream source reorganization.

# Architectural placement

Place durable profile metadata in `apps/geocedg/` and `geocedg/specs/ui/`.
Place the minimum Java adapter and launcher in the existing shared/Desktop
modules under the `org.geocedg` namespace. Register every change under
upstream-owned paths in `docs/upstream/modified-files.yml`.

# Required design/specification

Follow ADR 0001 and the application-profile v1 contract. Keep the manifest as
the single source for perspective and toolbar contents and fail on invalid or
missing profile data.

# Geometric invariants and degeneracies

No geometric definition, dependency, tolerance, exactness claim, or
degeneracy policy may change. Toolbar entries expose only existing baseline
modes.

# Compatibility and serialization

Retain `classic` as the persisted app code, preserve existing Classic
constructors/launcher/task, and isolate GeoCeDG preferences. A new persisted
application code is a stop condition for G2.

# Required tests and commands

Run the focused shared/Desktop profile tests, the composed
`tools/agent/verify.ps1 -RunBenchmarks` authority, both direct G1 verifiers,
Desktop build, both interactive launch routes, manifest validation,
`git diff --check`, and generated-output cleanup checks.

# Required artifacts

Maintain the ADR, spec/schema/manifest, stable feature entries, upstream
modification register, launcher/config/profile code, focused tests, operational
verification, README/architecture documentation, and the G2 validation report.

# Stop conditions

Stop if the profile requires geometric semantics, persisted-format changes,
an unapproved normative decision, unavailable or unlicensed new branding
assets, or loss of the Classic diagnostic route.
