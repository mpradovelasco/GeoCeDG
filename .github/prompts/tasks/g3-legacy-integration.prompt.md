# Objective

Maintain the G3 controlled integration of legacy CeDG resources and scientific
references without promoting geometric behavior.

# Authority and evidence hierarchy

1. `AGENTS.md`.
2. `docs/adr/0003-controlled-legacy-integration.md`.
3. `geocedg/specs/legacy/controlled-integration.md` and G1 manifest contracts.
4. `docs/roadmap/geocedg_roadmap.md`.
5. G2 profile contracts and current pinned source.
6. Registered scientific and legacy evidence.

# Scope

Original-artifact preservation, provenance, deterministic inventory, curated
classification, public-corpus metadata, opt-in Laboratory loading, focused G3
verification, and promotion workflow metadata.

# Explicitly forbidden scope

No G4 packaging, stable-toolbar migration, Java geometric command, Locus
change, native locus length/intersection, spatial/projection semantics, DSL,
DXF, layer/visibility redesign, serialization change, or upstream reorganization.

# Architectural placement

Place original models and tools under `models/legacy/`, scientific references
under `docs/references/cedg/`, durable contracts under `geocedg/specs/`, and
deterministic orchestration under `tools/legacy/` and `tools/agent/`.

# Required design/specification

Follow ADR 0003 and the controlled-integration contract. Keep original,
curation, derived inventory, Laboratory access, and regression metadata as
separate layers.

# Geometric invariants and degeneracies

Legacy validity and degeneracy information may be recorded but not repaired or
reinterpreted. Sampled locus length remains approximate research evidence and
must not be presented as Locus V2.

# Compatibility and serialization

Preserve source bytes and SHA-256, do not rewrite internal `.ggb` XML, keep
Classic diagnostics, and keep the G2 application toolbar byte-for-byte
equivalent in meaning and order.

# Required tests and commands

Run `tools/agent/verify-legacy.ps1`, deterministic ingest `-Check`, Laboratory
resolution and launch, both Desktop routes, schema validation,
`tools/agent/verify.ps1 -RunBenchmarks`, `git diff --check`, and output cleanup.

# Required artifacts

Maintain the resource manifest, curation, generated inventory, scientific and
public-corpus catalogs, legacy intake README, Laboratory loader, operational
verification, and G3 validation report.

# Stop conditions

Stop if integration requires a geometric or fundamental serialization change,
modifies `Locus`, changes the pinned baseline, promotes an unreviewed resource,
or requires an unapproved rights or normative decision.
