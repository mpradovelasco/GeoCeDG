# PRE-G9B-S1 — DXF correctness and bounded stabilization

## Authority and status

This is the canonical execution prompt for the author-authorized
`PRE-G9B-S1` implementation. Its exact entry authority is commit
`f79234c2c69fc9e30d5fc8e786909ada10909c6e`, tree
`3440f45335bd7b95c5d0ecb75fe3fb27128e8067`. Follow `AGENTS.md`, the
[approved pre-G9B design](../../../docs/architecture/pre_g9b_stabilization_deployability_design.md),
the G5 export specification and the G9X1 specification/ADR.

S1 is productive and uses bounded `PHASE -Phase PRE-G9B-S1` acceptance. A
technical candidate remains pending author review. `selfApproved=false`.

## Bounded objective

Reproduce the author DXF evidence through the real GeoCeDG load/export path,
repair only demonstrated G5/G9X1 regressions, and complete the bounded S1
presentation items: producer-aware SplineV2 type text, Midpoint/Text toolbar
taxonomy, reproducible persistent-user-tool alignment if established, and the
author-approved About text. Package version remains `0.9.0`.

Use immutable author originals from the task-authorized local inbox. Record
filename, size and SHA-256 before inspection. Generated DXF stays ignored;
version only the minimum byte-exact source fixtures and explicit provenance.

The export boundary remains read-only:

```text
authoritative construction
-> immutable export snapshot / strict preflight
-> exact entity or explicit tolerance-bearing export-only approximation
-> deterministic DXF and required fidelity sidecar
```

Never derive authority from rendering, viewport, zoom, DPI or proximity. Never
feed an approximation into the construction. A strict invalid/unsupported
component may not be hidden by partial output.

SplineV2 may use the approved G9X1 semantic-curve approximation only when its
existing branches/components/domains suffice. If new exact DXF `SPLINE` or new
fidelity/error/topology semantics are required, stop that subtask and record
`PRE-G9B-S1-SPLINE-DXF — REQUIRED — PENDING SEPARATE AUTHOR REVIEW/AUTHORIZATION`.

## Forbidden scope

Do not implement S2, S3, S4, D1, P1, G9B, G9C, G9U2, further G12 or productive
G10. Do not alter Text-property transactions, construction Text zoom, UI sizing
preferences, licensing/package composition, feature defaults, public version,
DXF import, Web or 3D.

## Evidence and stop boundary

Add the minimum artifact-backed regression corpus and focused tests for exact
ellipse output, LocusV2 strict preflight, SplineV2 disposition, sidecar/handle
integrity, determinism and view/construction immutability. Retain affected G5,
G9X1, profile/toolbar/About regressions and checkstyle. Update the verification
inventory/registry additively, run `INFRA_UNIT` when required, create one clean
candidate, then run exactly one `PHASE -Phase PRE-G9B-S1` against it.

Do not publish or tag. End at:

```text
PRE-G9B-S1 — IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
```
