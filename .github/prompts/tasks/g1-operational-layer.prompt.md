# Objective

Establish the reproducible G1 operational layer for later GeoCeDG development.

# Authority and evidence hierarchy

1. `AGENTS.md`.
2. `docs/roadmap/geocedg_roadmap.md`, especially sections 6, 10, and G1.
3. G0 artifacts and `FIRST_AGENT_TASK.md` where relevant.
4. `docs/architecture/proposed_spatial_projection_semantics.md` as
   non-normative G9 input only.
5. Current baseline code and build.

# Scope

Canonical prompts, lightweight profiles, operational specifications and
manifests, deterministic verification, basic CI, and initial informational
benchmark/regression support.

# Explicitly forbidden scope

No geometric feature, Locus change, G2 profile implementation, upstream source
reorganization, model import, installer, DXF, branding, or dependency update.

# Architectural placement

Use `.github/prompts/`, `ai-shell/prompts/`, `geocedg/`, `models/manifests/`,
`benchmarks/`, `tools/agent/`, `tools/benchmark/`, and `docs/` only.

# Required design/specification

Follow ADR 0002 and `geocedg/specs/operations/manifest-contracts.md`. ADR 0001
remains Proposed and does not authorize G2.

# Geometric invariants and degeneracies

Not applicable to implementation in G1. Operational metadata must not define
or approximate geometric truth.

# Compatibility and serialization

Do not alter `.ggb` serialization or upstream behavior. Version every
operational manifest and fail on unknown versions.

# Required tests and commands

Run `tools/agent/verify-operational.ps1`, the operational benchmark smoke,
`tools/agent/verify-baseline.ps1`, `tools/agent/verify.ps1`, and
`git diff --check` as applicable.

# Required artifacts

Operational sources, CI, an ignored generated-artifact boundary, and
`docs/validation/g1_operational_layer_report.md`.

# Stop conditions

Stop if G1 requires changing the baseline, upstream architecture, a geometric
contract, serialization, or an unresolved license decision.
