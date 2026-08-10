# G1 manifest contracts

- Status: Stable for operational metadata
- Contract version: 1
- Scope: manifests only; no geometric semantics
- Decision: `docs/adr/0002-g1-operational-authority.md`

## Serialization profile

Files named `*.yml` in the G1 operational layer use the JSON-compatible subset
of YAML 1.2. Each file must therefore also be valid JSON. The profile forbids
YAML-only comments, anchors, aliases, tags, merge keys, and implicit scalar
typing. Files are UTF-8 and end with one newline.

Every manifest has an integer `schema_version`. Version 1 files may reference
one of the JSON Schemas in this directory with `$schema`. Unknown versions
must fail verification rather than being interpreted approximately.

## Feature sets

`geocedg/features/stable.yml` and `experimental.yml` declare only feature
metadata. A feature entry requires a stable ID, maturity, specification path,
default state, and optional dependencies. Empty sets are valid in G1; they do
not enable a product feature.

## Model manifests

`models/manifests/model-manifest.template.yml` defines provenance, required
GeoGebra baseline, inputs/outputs, validity domain, degeneracies, reference
models, expected metrics, default loading, license evidence, and replacement
candidate. It is a template, not an imported model. Actual `.ggb`, `.ggt`, or
script files require a separate authorized import task. G3 uses the optional
version-1 `artifact`, `source_environment`, `derived_artifacts`,
`implementation`, `publications`, and `laboratory` fields for controlled legacy
packages. Their meaning is governed by
`geocedg/specs/legacy/controlled-integration.md` and does not alter geometric
semantics.

## Regression catalog

`geocedg/validation/regression/catalog.yml` records approved case manifests
and their immutable baseline. G1 intentionally contains no cases. A later gate
must add the governing geometric specification before adding a case.

## Benchmark suites and stress catalog

Benchmark suites declare a repository-relative PowerShell command, warm-up and
measurement iteration counts, a timeout, and informational budgets. Commands
are executed as argument arrays, never evaluated as shell text. The stress
catalog may contain disabled planned descriptors without model assets; this is
planning metadata, not an imported model or a measured baseline.

## Versioning

Adding optional fields may preserve version 1. Renaming a field, changing its
meaning, broadening the serialization profile, or changing identifier rules
requires a new version and an explicit migration.
