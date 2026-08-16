# G9X1 DXF validation and benchmark plan

**Status: AUTHOR-APPROVED VALIDATION DESIGN / NOT EXECUTED. No productive G9X1 capability exists.**

## Gate structure

1. X1A preserves every G5 exact mapping and makes the versioned regression
   corpus executable end to end: construction -> export -> parsed DXF and,
   when required/requested, manifest
   -> expected entities.
2. X1B adds only approved bounded adaptive curve families.
3. X1C is a later optional gate for topology-aware implicit curves or exact
   rational splines.

Each gate runs its focused verifier and the composed verifier without
`-SkipBuild`. Exact G5 regressions remain mandatory.

## Required fixtures

| Area | Cases |
|---|---|
| exact baseline | point, segment, ray, line, circle/arc, ellipse/arc, polygon, polyline; metadata and byte determinism |
| analytic approximation | bounded parabola/hyperbola, parametric ellipse, sine, cusp, self-intersection, discontinuity, periodic curve, collapsed image |
| Locus V2 | multiple branches/components, invalid gap, coincident multiplicity, open endpoint, periodic seam, unbounded reject/explicit-domain accept, dynamic revision |
| policy | approved typed approximation as explicit/reported normal behavior, all-exact optional sidecar, every mandatory-sidecar trigger, strict default reject, explicit-intent partial option, unsupported/invalid mix, hidden sources, units, overwrite and paired collisions |
| failure | non-finite evaluation, stale revision, unmet tolerance, each deterministic work limit, write failure and rollback/cleanup |

## Invariants

- no connection across an invalid gap;
- separate entities for constructive multiplicity;
- closure only from semantic evidence;
- stable branch/component ordering;
- no construction/protocol/undo mutation;
- no render-evaluator access;
- zoom, DPI, viewport, and screen-transform invariance;
- translation and scale sweeps in model coordinates;
- deterministic DXF and, when present, manifest bytes for one source revision/request;
- every approximation, omission, partial/unsupported-not-exported component,
  work-limit termination or other fidelity reduction has a mandatory sidecar;
- an all-exact export may omit the sidecar under product policy;
- any present manifest's DXF hash and handle mapping agree with parsed output; and
- exact/approximate/unsupported/invalid counts match all source components.

## Functional counters

Normative limits use counts, not wall time: source evaluations, interval boxes,
subdivisions, maximum depth, vertices per component, total vertices, emitted
entities, omitted components, bytes, and filesystem attempts. Every counter is
initialized and retained at zero when unused. Benchmarks characterize elapsed
time and peak memory but must not make output machine-speed dependent.

Benchmark scales include 1/10/100 sources, 1/10/100 semantic components, and
tolerance/scale sweeps. Repeated runs must produce equal counters and bytes.

## Independent evidence

Before any `SPLINE` mapping is called exact, verify the mathematical
representation and round-trip/conformance in at least one independent CAD
reader. A successful visual opening is insufficient. Sample-based adaptive
polylines remain `ESTIMATED_ERROR` unless an independent certified bound is
available.

## Stop conditions

Stop rather than promote if any curve requires render tessellation, hidden
`GeoElement`s, viewport clipping, coordinate/label identity, nondeterministic
  budgets, silent partial output, a partial option without explicit intent/
  warning/sidecar, a missing mandatory sidecar, or a fidelity claim stronger
  than its evidence.
