# Objective

Maintain the standalone Windows package pipeline and its internal-evaluation
release boundary.

# Authority and evidence hierarchy

Follow `AGENTS.md`, ADR 0004, the packaging specification and manifest, the
roadmap, and executable verification authority in that order.

# Scope

Packaging/release scripts, package metadata, legal-status records, SBOMs,
hashes, onboarding, and validation evidence.

# Explicitly forbidden scope

Do not change geometry, the kernel, `Locus`, serialization, G5 functionality,
or upstream installer/branding assets. Do not publish internal artifacts.

# Architectural placement

Durable inputs belong in `packaging/`, release orchestration in
`tools/release/`, and executable gates in `tools/agent/`.

# Required design/specification

Apply `docs/adr/0004-standalone-windows-packaging.md` and
`geocedg/specs/packaging/windows-packaging.md`.

# Geometric invariants and degeneracies

Packaging has no geometric semantic effect. Preserve all existing kernel,
dependency, projection, and Locus behavior exactly.

# Compatibility and serialization

Use the G2 GeoCeDG launcher and preserve Classic `.ggb` serialization. The
installer association changes shell routing only, not file content.

# Required tests and commands

Run the package builder, focused package gate, composed verifier, launcher
smoke test, safe install/uninstall test, `git diff --check`, and output cleanup.

# Required artifacts

For validation, generate app-image, ZIP, MSI, EXE, build manifest, CycloneDX
SBOM, SHA-256 list, and the G4 report. Keep binaries ignored.

# Stop conditions

Stop before public release, unapproved asset reuse, substantial unplanned
toolchains, upstream architecture changes, or any semantic/serialization
change.
