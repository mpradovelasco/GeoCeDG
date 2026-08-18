# G9A2 spatial semantic core and point-pilot report

**Status:** IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW

**Author approved:** `false`

**Entry branch:** `feature/g9a2-spatial-semantic-point-pilot`

**Entry commit:** `5934d706fd9b30ea11b34d6ff0fe293e971cfc3f`

This is the living implementation-candidate report for the separately
authorized G9A2 phase. It records the completed candidate verification but is
not approval and does not authorize G9A3, any later G9 phase or productive G10
work.

## 1. Entry authority

The checked canonical task is
`.github/prompts/tasks/g9a2-spatial-semantic-point-pilot.prompt.md`, canonical-
LF SHA-256:

```text
d02553668cfa8800fa28428e5ee8f293504bc5519ec53f8ed2d54571d802d23e
```

The authorized entry state is the clean feature branch at
`5934d706fd9b30ea11b34d6ff0fe293e971cfc3f`, equal to `main` and
`origin/main`. G9A1 is author-approved at annotated tag
`geocedg-g9a1-pass`, object
`9b125d9e4d23ff8ce68ce0ad9c16e30a8de338c7`, peeled commit
`02e97ecc9a2e53aece913f7004c50c17fcc663e6`.

The normative spatial specification and Accepted ADR 0010/0011 retain their
approved canonical-LF hashes:

| Authority | SHA-256 |
|---|---|
| G9 spatial specification | `11e1327a6518a25178133a1bfc0720a6d73adabab7d127b5203b6da86b25ca56` |
| ADR 0010 | `25b85c8f29488df3c313f3a1e67cea1cb25714253aa01d13625f8791ad20586d` |
| ADR 0011 | `42fd3fdc0a7493f6bde28c1ba2c597e093e138b14fd73619204cb22d001ebf41` |

The author authorization in the invoking task supersedes the prompt's
pre-execution `NOT AUTHORIZED` marker only for G9A2. It changes neither the
normative geometry nor any later-phase gate. G10P remains planning context.

## 2. Authorized candidate scope

G9A2 is limited to:

- orthographic projection frames;
- oriented isometry/declared unit-similarity diagram maps;
- hinge and auxiliary change-of-plane relation evaluation;
- role-typed bindings and independent system/object status axes;
- projection-defined point reconstruction;
- intrinsic and composed common-diagram reprojection;
- normal-DAG invalidation and atomic no-stale publication;
- real G9A1 XML/lifecycle integration for the required baseline cases; and
- one-way derived 3D point behavior.

The admitted frame direction is derived only for the persisted orthographic
family from the ordered basis and handedness. All frames in one evaluated
subcontext must share a world/source unit; map `units` are the explicit
target/common-diagram unit. No implicit spatial-unit conversion is present.
Only `DEFINING` bindings drive reconstruction. `AUXILIARY`, `ANALYSIS`, and
`PRESENTATION` bindings may coexist but their value subgraphs remain outside
the required certificate subcontext; mixed `DERIVED` authority is rejected.

No other primitive, composed object, public command, UI, migration heuristic,
bidirectional 3D editing, Locus, DXF or productive G10 capability belongs in
this candidate.

## 3. Independent analytic evidence

The candidate's analytic inputs live under
`geocedg/validation/spatial/g9a2/`:

- `numeric-policy.json` records world-coordinate residual and rank policy;
- `generate_point_references.py` uses only Python `Decimal` at 80 digits and
  imports no candidate implementation;
- `point-reference-values.json` contains exact/exactly described frame, map,
  hinge, point, gauge and near-rank fixtures; and
- `reference-evidence.sha256` seals those UTF-8 canonical-LF inputs.

The persisted candidate policy and analytic data agree exactly on absolute
and relative residual tolerances `1e-10`, rank-relative tolerance `1e-12`, map
and hinge tolerances `1e-10`, and condition limit `1e12`. Its rank threshold is
`rankRelativeTolerance * max(rows, columns) * max(1, abs(sigmaMax))`; no
additional ULP floor is implied.

The reference is fake-first analytic evidence. It does not prove host XML,
normal-DAG or 3D-adapter behavior; those require the focused shared-kernel and
saved-file tests.

## 4. Required scenario disposition

Final machine evidence must contain exactly:

- `A2-SYS-01` through `A2-SYS-11`;
- `A2-POINT-01` through `A2-POINT-12`;
- `A2-DYN-01` through `A2-DYN-05`; and
- `A2-AUTH-01` and `A2-AUTH-02`.

Every case must be `PASSED` except `A2-POINT-07`, which is
`NOT_APPLICABLE`: the admitted linear point schema has no discrete predicate
that could yield several isolated candidates. Generic `AMBIGUOUS` state
representability remains covered.

## 5. Persistence and compatibility evidence

The final result must distinguish:

| Evidence | Required classification |
|---|---|
| frame/map/point analytic reference | fake-first, independent |
| shared-kernel point/DAG tests | real shared-kernel execution |
| valid/inconsistent/underdetermined reopen | real host XML/GGB round trip |
| copy/undo/rename | real G9A1 lifecycle substrate |
| derived 3D point | real headless shared-kernel adapter; desktop smoke separately classified |
| GUI/product workflow | absent |

Reopen must recompute the certificate from persisted inputs. Cached derived
coordinates cannot become a second authority. Legacy files remain
unassociated. The candidate intentionally does not claim the hostile lifecycle
matrix owned by G9A3.

The XML envelope stays at version 1. Legacy record-version 1 shapes remain
inert; strict record-version 2 shapes carry the admitted frame, map, relation,
system, binding and POINT inputs. Unknown versions/attributes reject rather
than silently downgrading. The deterministic canonical archive is
`models/regression/g9a2-spatial-point-pilot/g9a2-spatial-point-pilot.ggb`,
SHA-256 `3f150eaf05731b3907b5ba3e653ec4666ca1dcc6f999f1b609de305b98b2a3be`.

## 6. Focused and composed verification

The focused authority is:

```powershell
.\tools\agent\verify-g9a2-spatial-point.ps1 -KeepBuildOutputs `
    -LogDirectory artifacts\g9a2\candidate\focused-final
```

It runs every `org.geocedg.common.spatial.G9A2*` test, shared-main and
shared-test Checkstyle, exact source-boundary/inventory checks, analytic and
machine-evidence hashes, hard-zero scope/authority gates and both Git
whitespace checks. The deterministic full rerun used
`artifacts/g9a2/candidate/focused-rerun`.

The final composed authority is:

```powershell
.\tools\agent\verify.ps1 -KeepBuildOutputs `
    -LogDirectory artifacts\g9a2\candidate\composed-final
```

It must execute without `-SkipBuild`. Exact commands, exit codes, test totals,
log paths and evidence classifications from the saved executions are:

| Authority | Log directory | Exit | G9A2 tests |
|---|---|---:|---:|
| Focused final | `artifacts/g9a2/candidate/focused-final` | 0 | 64 passed; 0 failures/errors/skips |
| Focused deterministic full rerun | `artifacts/g9a2/candidate/focused-rerun` | 0 | 64 passed; 0 failures/errors/skips |
| Composed final, without `-SkipBuild` | `artifacts/g9a2/candidate/composed-final` | 0 | 64 passed; 0 failures/errors/skips |

The shared-kernel tests exercise the one-way derived 3D point adapter. The
composed authority also passed its desktop compile/profile gates. There was no
dedicated G9A2 3D-renderer smoke, so this candidate does not claim one.

An initial verification attempt encountered an ACL blocker on stale ignored
Gradle problems-report output. Removing that ignored stale output and retrying
the unchanged authority resolved it. This was classified as environment-only;
no product workaround or scope deviation was introduced.

## 7. Candidate paths and test totals

The candidate contains exactly 80 changed paths. The machine-owned exhaustive
inventory in `docs/validation/g9a2_spatial_point_evidence.json` partitions them
without duplication as follows:

| Partition | Paths |
|---|---:|
| Productive shared-kernel/host paths | 50 |
| Focused test classes | 7 |
| Private XML fixtures | 3 |
| Canonical model/catalog paths | 7 |
| Independent analytic-validation paths | 4 |
| Architecture/evidence/inventory/verifier paths | 9 |

The productive partition comprises eight compatible extensions to the G9A1
identity/XML substrate, 34 additive semantic-core classes, seven additive
runtime/DAG classes and the one minimum `Construction` integration seam. The
focused source set declares 64 tests:

| Focused class | Tests |
|---|---:|
| `G9A2ProjectionDefinedPointEvaluatorTest` | 13 |
| `G9A2ProjectionSystemEvaluatorTest` | 10 |
| `G9A2SpatialSemanticInstrumentationTest` | 4 |
| `G9A2SpatialSemanticRecordXmlTest` | 6 |
| `G9A2SpatialSemanticRuntimeTest` | 23 |
| `G9A2SpatialSemanticHostXmlTest` | 5 |
| `G9A2SpatialSemanticLifecycleTest` | 3 |
| **Total** | **64** |

The focused verifier rejects any path, class or count that differs from the
sealed evidence. The saved final focused run and its deterministic full rerun
confirmed all 64 tests with zero failures, errors and skips.

## 8. Current disposition

```text
G9A2 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
authorApproved = false
passClaimed = false
G9A3 AND LATER IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
G10 PRODUCTIVE IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
```

The source boundary, upstream inventory, canonical saved point model/hash,
analytic references, scenario matrix, confirmed focused totals and residual-risk
record are frozen in machine evidence. Focused final, deterministic full rerun
and composed verification without `-SkipBuild` all passed. The 3D evidence is
limited to the tested shared-kernel adapter and passed composed desktop
compile/profile gates; no dedicated G9A2 renderer smoke was run. No `PASS` or
author approval is claimed by this candidate report.
