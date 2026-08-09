# G1 operational layer report

Status: **PASS**
Date: 2026-08-09
Branch: `feature/g1-operational-layer`
Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
GeoGebra: `5.4.928.0`

## Objective and authority

G1 establishes reproducible GeoCeDG-owned operational infrastructure for
later roadmap gates. Authority was applied in this order: `AGENTS.md`, the G1
sections of `docs/roadmap/geocedg_initial_plan.md`, relevant G0 artifacts,
the non-normative spatial proposal only as a deferred-scope constraint, and
the real baseline code/build.

The affected layer is repository operations, governance, validation metadata,
CI, and benchmark orchestration. No inherited source or geometric semantic
layer is modified.

## Scope decisions

ADR 0002 accepts these G1 operational decisions:

- canonical prompts live under `.github/prompts/`;
- `ai-shell/prompts/` profiles remain short references;
- `tools/agent/verify.ps1` composes specialized executable authorities;
- version 1 `.yml` manifests use JSON-compatible YAML and stock PowerShell
  parsing, without a new dependency;
- feature and regression catalogs begin empty;
- the model manifest is a template and no model/tool is imported;
- benchmark budgets are informational while command failures remain gating;
- generated evidence belongs under ignored `artifacts/`;
- basic CI runs the same Windows PowerShell authority used locally.

This is the smallest coherent G1 structure. Empty roadmap directories for
product apps, resources, packaging, Python, geometric tests, release tooling,
and future specialized verifiers are not created.

## Durable changes

### Agent workflow

- canonical governance and verification prompts;
- a complete task template and the reproducible G1 task prompt;
- a change-review prompt;
- `ask`, `plan`, `verify`, `refactor`, and `architect` profiles.

### Specifications and manifests

- operational manifest contract and versioned JSON Schemas;
- empty stable and experimental feature sets;
- model manifest template and empty import catalog;
- empty regression catalog pinned to the upstream baseline;
- specification template for later approved gates.

### Verification and CI

- static `tools/agent/verify-operational.ps1`;
- top-level `tools/agent/verify.ps1` that reuses
  `verify-baseline.ps1` rather than duplicating Gradle commands;
- Windows GitHub Actions workflow with complete history/tags and Java 22;
- `git diff --check`, upstream-boundary, prompt, manifest, and no-import gates.

### Benchmark and regression support

- safe PowerShell benchmark runner with warm-up, repeated measurements,
  timeout, provenance, and JSON evidence;
- operational smoke suite with an informational median budget;
- three disabled stress-model planning descriptors and no model assets;
- ignored generated-artifact boundary.

## Semantic and compatibility effect

Geometric semantic effect: none. Locus effect: none. Serialization effect:
none. Classic Desktop behavior: unchanged. No feature is enabled, no toolbar
is changed, no command or model is introduced, and G2 has not started.

## Validation evidence

| Gate | Command | Exit code | Evidence |
|---|---|---:|---|
| PowerShell syntax | PowerShell AST parser over four verification/benchmark scripts | `0` | all scripts parsed without errors |
| JSON-compatible YAML | `ConvertFrom-Json` over 11 schema/manifest files | `0` | all documents parsed |
| Independent schema check | Python `jsonschema` Draft 2020-12 over four schemas and five instances | `0` | additional non-authoritative environment check; no dependency added |
| Operational contracts | `.\tools\agent\verify-operational.ps1` | `0` | prompts, profiles, manifests, CI, text hygiene, no-import and upstream boundaries passed |
| Informational benchmark smoke | `.\tools\benchmark\run.ps1` | `0` | final durations 547.043, 538.588, and 567.558 ms; median 547.043 ms; 5000 ms warning threshold not exceeded |
| Pinned baseline | invoked by `.\tools\agent\verify.ps1` | `0` | shared compile `BUILD SUCCESSFUL in 27s`; Desktop compile `BUILD SUCCESSFUL in 1m` |
| Composed authority | `.\tools\agent\verify.ps1 -RunBenchmarks` | `0` | final staged-tree gate completed in 98.2 seconds and restored repository status |
| Whitespace | `git diff --check` plus G1 text hygiene | `0` | no trailing whitespace or extra EOF line |

The final composed logs and benchmark result are outside the repository under
`C:\Users\usuario\AppData\Local\Temp\geocedg-g1-validation\final`.
The benchmark recorded source revision `1d893089a`, a dirty pre-commit status,
37 changed-path entries, and SHA-256 hashes for its suite, runner, and measured
script. This is informative development evidence, not a clean performance
baseline.

The baseline verifier removed every generated Gradle `build`, `.gradle`, and
`.kotlin` directory created by the run; the post-run generated-output count was
zero. No upstream-owned path differs from the pinned baseline.

Four development diagnostics exited `1` before the final results were
accepted: two exposed empty-collection/EOF handling defects in the new static
verifier, one exposed positional parameter forwarding in the new orchestrator,
and one independent schema check exposed that `$schema` was not allowed by
schemas using `additionalProperties: false`. All were corrected in G1-owned
files and the affected gates were rerun successfully.

G1 is **PASS**: all required local gates exit `0`, budgets remain explicitly
informational, no model or feature is imported, the baseline remains unchanged,
and generated evidence is outside the durable source tree.

## Limitations and pending matters

- Full shared/Desktop test suites remain optional unless a focused failure or
  later implementation makes them necessary.
- CI configuration is validated locally but its hosted run cannot be claimed
  until GitHub executes it.
- G1 records no kernel/render performance baseline; the only measured case is
  operational harness overhead.
- Stress-model entries are disabled planning metadata without `.ggb` assets or
  expected geometric results.
- License selection and root legal records remain unresolved G0/G1 release
  blockers and are not inferred here.
- ADR 0001 remains Proposed. Product profile work begins only under G2
  authorization.
- Spatial identity and canonical projection decisions remain deferred to G9.
