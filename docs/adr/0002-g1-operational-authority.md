# ADR 0002: G1 operational authority and manifest format

- Status: **Accepted**
- Date: 2026-08-09
- Scope: GeoCeDG-owned operational infrastructure only
- Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`

## Context

G1 must provide a reproducible agent workflow, machine-readable manifests,
basic CI, and initial regression/benchmark support without adding geometric
behavior or dependencies to the inherited GeoGebra build.

The baseline supplies Gradle and application tests but no GeoCeDG prompt
system, manifest parser, benchmark harness, or CI workflow. PowerShell is
already the repository's Windows verification authority. PowerShell 7 has a
built-in JSON parser but no built-in YAML parser.

## Decision

1. `AGENTS.md` remains the governing repository authority.
2. `.github/prompts/` contains canonical prompts, task prompts, and review
   prompts. `ai-shell/prompts/` contains only short profiles that point to
   those authorities.
3. `tools/agent/verify.ps1` is the top-level executable authority. It composes
   the G1 structural verifier and the G0 baseline verifier instead of copying
   their commands.
4. Initial files with a `.yml` manifest name use the JSON-compatible subset of
   YAML 1.2. They are parsed with `ConvertFrom-Json`, require no downloaded
   module, and may not use YAML comments, anchors, tags, or implicit typing.
5. Versioned JSON Schemas under `geocedg/specs/operations/` describe the
   manifest contracts. The PowerShell verifier enforces the G1 subset needed
   by the checked-in manifests.
6. Benchmark budgets are initially informational. A benchmark command failure
   is a verification failure; exceeding a timing budget is reported but does
   not fail G1.
7. Generated benchmark and regression evidence belongs under `artifacts/` and
   is ignored. Only `artifacts/README.md` is durable source.
8. Basic CI runs on Windows, calls `tools/agent/verify.ps1`, and uploads its
   temporary logs. CI does not launch the interactive Desktop application.

## Rationale

JSON-compatible YAML preserves the planned `.yml` locations while avoiding a
new parser dependency or an incomplete custom YAML implementation. The format
can later be broadened through a versioned migration if full YAML syntax has a
demonstrated benefit.

Separating structure validation, baseline validation, and benchmark execution
keeps each entry point focused. Prompts can name the top-level verifier while
specialized diagnostics remain independently runnable.

## Consequences

- All checked-in manifests are deterministic UTF-8 text and parse with stock
  PowerShell 7.
- Prompt profiles cannot silently replace repository rules or specifications.
- CI and local verification share the same commands.
- Performance comparisons have provenance and repeat counts, but their G1
  thresholds are not release gates.
- Future schema changes require a new `schema_version` and migration policy.

## Explicit non-decisions

This ADR does not decide product configuration, toolbar contents, geometric
commands, Locus semantics, spatial identity, projection schemas, serialization
extensions, packaging, or licensing of GeoCeDG-authored material. ADR 0001
remains Proposed, and the spatial proposal remains non-normative.
