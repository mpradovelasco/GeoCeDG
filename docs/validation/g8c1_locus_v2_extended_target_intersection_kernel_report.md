# G8C1 extended-target intersection kernel report

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| Baseline | G8C design closeout `16c7c795c1e95881aeb497b8980480ef68ab5f7a` |
| Canonical prompt | `.github/prompts/tasks/g8c1-locus-v2-extended-target-intersections.prompt.md` |
| Prompt canonical-text SHA-256 | `c096a069b10b85a27e8ac96223ca679d788441f2a684403991e66a1ffbcfabaa` |
| Product maturity | Internal, experimental, disabled by default, nonpersistent |

## 1. Result

G8C1 extends the single-semantic-parameter G8B pipeline with three closed,
authoritative target adapters:

- nondegenerate `GeoConic` ellipse, parabola and hyperbola;
- real `GeoFunction` with an explicit finite semantic interval;
- finite-coefficient polynomial `GeoImplicitCurve` at regular promoted roots.

The implementation reuses the immutable rich result, normal-DAG rich Geo and
strict token-selected point consumer. It adds no second result framework and
does not implement Locus V2 × Locus V2.

## 2. Target and residual contracts

`LocusIntersectionTargets2D.assess()` makes a typed closed support decision
before capture. Unsupported subtypes, missing function domains, undefined
targets and unavailable implicit normalization are ordinary typed outcomes.

Conics and regular polynomial implicit curves use the signed first-order normal
residual `G/||grad G||`. It has local model-coordinate length semantics and is
invariant under nonzero equation scaling; it is not exact Euclidean distance.
The function adapter uses `y-f(x)`, a vertical model-coordinate residual. A
finite derivative contributes only normalized contact evidence. Missing or
nonfinite derivative evidence degrades classification without changing graph
incidence.

Function values outside the captured interval, poles and nonfinite evaluations
are hard isolation barriers. They are reported as target-domain/undefined
evidence, never bridged and never reinterpreted as a proof of no intersection.
Singular implicit candidates cannot pass the regular residual adapter.

## 3. Numerical capability

`ExtendedTargetIntersectionCapability2D` is query-local and bounded. For each
declared finite Locus V2 component it uses 256 initial semantic subdivisions,
safeguarded sign-change refinement and absolute-local-minimum refinement for
even contacts. Every candidate is evaluated again through the target's
independent residual and membership contracts by the common solver.

The default adaptive capability deliberately reports global completeness as
`NOT_ESTABLISHED`: finite adaptive coverage is not an exhaustive proof.
Individually verified, locally isolated and unambiguous roots remain admissible
under Option B. An explicitly injected analytic/certified capability may report
`COMPLETE` when its own coverage evidence establishes that claim; a focused
test covers a complete empty result.

Tangency does not depend on sign changes. A normalized source-tangent/target-
normal factor supports transverse or tangent classification when both sides are
regular. Numeric multiplicity remains `NOT_ESTABLISHED`. A tangent candidate
found by a local minimum remains locally unisolated unless stronger uniqueness
evidence exists, so it is not silently made point-admissible.

## 4. Identity and lifecycle

The existing G8B/G8B-R1 identity contract is unchanged. Semantic parameter,
isolating interval, coordinate, result order, revision, residual and global
completeness remain evidence rather than durable identity. A uniquely isolated
component root receives an explicit component-local continuation key; multiple
or ambiguous roots do not receive fabricated lineage.

Tests demonstrate source and target motion, three downstream DAG levels,
appearance/disappearance/recovery, root discovery and result reordering,
identical coordinates with distinct semantic preimages, `2 -> 1 -> 2`
tangency ambiguity, and atomic exception recovery. The selected point never
retargets by coordinate or list position and never exposes stale success.

## 5. Functional evidence

The focused suites contain 38 tests:

| Suite | Tests | Failures/errors/skipped |
|---|---:|---:|
| `G8C1ExtendedTargetKernelTest` | 22 | 0 |
| `G8C1ExtendedTargetLifecycleTest` | 10 | 0 |
| `G8C1ExtendedTargetFunctionalBenchmarkTest` | 6 | 0 |

The representative unique-ellipse query has the versioned counter vector:

```text
semantic evaluations                 414
source derivative evaluations          1
target candidate evaluations          411
target derivative evaluations           1
target domain evaluations               1
invalid target evaluations              1
candidate intervals                     2
isolation subdivisions                256
refinement calls                        2
refinement iterations                  95
residual verifications                  1
membership checks                       1
deduplication comparisons               1
continuation comparisons                0
verified solutions                      1
retained intersection-index entries     0
```

One, ten and one hundred compatible queries are deterministic and linear.
One hundred derived point consumers perform no new solve and retain no shared
intersection state. Render, legacy-sample, viewport, pixel-tolerance, metric-
index and whole-locus-regeneration counters remain zero.

## 6. Independent references

G8C1 reuses the design-phase independent reference generator and manifest:

- `geocedg/validation/locus-v2/g8c/generate_extended_intersection_references.py`;
- `geocedg/validation/locus-v2/g8c/extended-intersection-reference-values.json`.

They record formulas, CPython 3.12.13, mpmath 1.4.1, 80 decimal digits and
source/output hashes. The G8C1 verifier regenerates and compares them; these
values are validation evidence, never productive authority.

## 7. Scope audit

No public command/dispatcher overload, generic `Path`, XML/persistence,
legacy `GeoLocus`, Classic semantics, frontend, export, 3D, Python DSL, G8C2,
G9, shared owner, global cache or G7 metric-index access was added. The only
productive edits are the minimum GeoCeDG-owned one-parameter intersection
package and existing internal algorithm seam. Every upstream-tree path is
listed in `docs/upstream/modified-files.yml`.

## 8. Author closeout

On 2026-08-15 the author approved all six review decisions:

1. **D1 — closed target subsets:** nondegenerate ellipse/parabola/hyperbola,
   explicit-finite-domain real functions, and regular finite-coefficient
   polynomial implicit roots are the complete G8C1 productive scope;
2. **D2 — conservative completeness:** evaluator-only finite discovery keeps
   global completeness `NOT_ESTABLISHED` without exhaustive coverage proof;
3. **D3 — tangency versus point admissibility:** established geometric
   tangency does not make an unisolated root consumable;
4. **D4 — component-local continuation:** only a unique locally isolated root
   with coherent semantic lineage may continue;
5. **D5 — query-local counters:** the measured 1/10/100-query and 100-consumer
   baseline is accepted, with no shared owner, cache, retained index, or G7
   metric-state reuse; and
6. **D6 — closeout:** G8C1 is `PASS — AUTHOR APPROVED`.

These are author-approved policy conclusions derived from, but not alterations
to, the measured G8C1 evidence in sections 5–6. G8C2 implementation and G9
remain outside this closeout.
