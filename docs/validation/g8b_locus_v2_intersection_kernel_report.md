# G8B Locus V2 2D intersection kernel report

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| G8B-R1 | **PASS — AUTHOR APPROVED** |
| Execution date | 2026-08-14 |
| Entry commit | `6529a4ebfafa5dc9dca3cc1b4c3e7a89ebcba375` |
| Branch | `feature/g8b-locus-v2-intersections-kernel` |
| GeoGebra baseline | 5.4.928.0, `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Normative contract | [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) |
| ADR | [`0008-locus-v2-intersection-result-and-continuation.md`](../adr/0008-locus-v2-intersection-result-and-continuation.md), Accepted |
| Canonical prompt | [`g8b-locus-v2-intersection-kernel.prompt.md`](../../.github/prompts/tasks/g8b-locus-v2-intersection-kernel.prompt.md) |
| Machine evidence | [`g8b-intersection-kernel-evidence.json`](../../geocedg/validation/locus-v2/g8b/g8b-intersection-kernel-evidence.json) |
| Traceability | [`g8b_locus_v2_intersection_traceability_matrix.md`](g8b_locus_v2_intersection_traceability_matrix.md) |
| Focused refinement | [`g8b_r1_locus_v2_intersection_point_admissibility_report.md`](g8b_r1_locus_v2_intersection_point_admissibility_report.md) |

G8B implements the minimum internal shared-kernel capability authorized after
G8A. It does not expose a command, generic `Path`, persistence, migration,
Classic overload, 3D route, Level C family, or G9 behavior. The author approved
the verified G8B candidate and its R1 refinement on 2026-08-14.

## 1. Implemented semantic chain

```text
GeoLocusV2 revision + line/segment/ray/circle GeoElement
        -> coherent query and authoritative target adapter
        -> strongest supplied semantic capability, or evaluator-only fallback
        -> independent semantic evaluation + residual + membership verification
        -> immutable rich intersection set
        -> nonnumeric GeoLocusIntersectionResult in the normal Construction DAG
        -> internal root-token-selected AlgoLocusIntersectionPointV2
        -> downstream construction
```

The immutable rich set and its rich Geo are the authority. The ordinary point
is only a derived consumer. Under the author-approved G8B-R1 contract it is
defined for one current, successful finite solution whose selected semantic
token is unique and whose verification, local isolation, provenance, and
identity/continuation are established. Parent completeness remains visible but
is not a veto. The point does not solve, own a token, choose by list position,
retarget by coordinates, or imply exhaustive root enumeration.

## 2. Productive implementation

The additive package
`org.geocedg.common.kernel.locus.intersection` contains 33 immutable/query-local
types covering:

- closed result axes for computation, geometry kind, completeness, support,
  guarantee, currentness, classification, identity, lineage, and diagnostics;
- revision-bound query/source records and separately typed durable identity
  versus revision-scoped localization/residual evidence, including explicit
  solution-local isolation status;
- target adapters for line, segment, ray, and circle;
- a capability interface, an honest evaluator-only adaptive implementation,
  candidate deduplication, independent verification, and bounded continuation;
- the approved normalized tolerance policy and deterministic work budgets; and
- functional counters, including explicit zero counters for every forbidden
  render, legacy-sample, viewport, pixel, metric-index, and whole-locus source.

`AlgoLocusIntersectionV2` registers the Locus V2 source, target, and any
explicit capability dependencies through the normal algorithm inputs. It
invalidates the rich Geo before private work and atomically publishes one
immutable current snapshot on success, invalid input, unsupported target,
budget exhaustion, or exception. It retains at most one prior successful finite
snapshot solely for the bounded two-epoch continuation comparison; only roots
with established local identity/isolation participate in matching.

`GeoLocusIntersectionResult` is nonnumeric, non-drawable, non-editable,
internal, and nonpersistent. `GeoClass.LOCUS_INTERSECTION_RESULT` is the only
upstream-owned productive edit and is append-only at priority 132.
The existing Locus V2 classification and exhaustive drawable tests were
updated only to recognize that append-only type and prove that its rich Geo
has no Euclidian drawable; no render implementation was changed.

## 3. Target authority and normalization

| Family | Authoritative adapter | Limited membership | Residual quantity |
|---|---|---|---|
| line | current homogeneous `GeoLine` coefficients | not applicable | signed perpendicular model-coordinate distance |
| segment | current support-line coefficients and endpoints | projection parameter in the captured segment | signed perpendicular model-coordinate distance |
| ray | current support-line coefficients and start/direction | nonnegative captured ray parameter | signed perpendicular model-coordinate distance |
| circle | verified current circular `GeoConic` center/radius state | full circle | signed radial model-coordinate distance |

Equation rescaling cannot change the normalized line decision. Residual type,
units, normalization provenance, characteristic-scale policy, raw value,
normalization scale, normalized value, and characteristic scale are retained.
The normalized contact indicator divides target residual change by semantic
source speed when regular differential evidence exists. Raw equation or source
parameter derivatives are never compared as if dimensionless; singular or
missing differential evidence leaves contact classification unestablished.

## 4. Capability and completeness boundary

`LocusIntersectionCapability2D` is the productive seam for authoritative
analytic or certified isolation. A capability may claim `COMPLETE` only when
its evidence covers every current semantic component; the solver independently
checks the declared component set and every candidate. The tests inject
deterministic analytic/reference capabilities through this exact productive
seam and then execute the productive solver, rich result, continuation, Geo,
and point consumer.

The productive `EvaluatorOnlyIntersectionCapability2D` uses semantic
evaluation, sign-change bisection, and local minima of absolute normalized
residual. It can therefore find and independently verify even-root candidates
without sign-change-only logic. Because G6 providers expose deterministic
evaluation but no universal derivative bounds or interval certificate, this
fallback always reports completeness `NOT_ESTABLISHED`, contact/multiplicity
unestablished, and floating-point-uncertified support. It never converts a
stable count or convergence into complete empty. Its localization-only
candidates carry `LocalIsolationStatus.NOT_ESTABLISHED`, so they remain
point-inadmissible even after residual verification. In contrast, an injected
authoritative capability may establish an individual root's local isolation
while honestly leaving global completeness `INCOMPLETE` or `NOT_ESTABLISHED`;
that root is point-admissible. No provider contract was strengthened and no
completeness claim was widened.

Typed overlap/infinite evidence may coexist with independently verified
isolated finite roots in the rich result, but the current rich geometry taxonomy
does not decompose an overlap-kind set into an independently projectable finite
subresult, so no ordinary-point projection is admissible for an overlap-kind
set. Full conics, functions, implicit curves,
and locus–locus remain unsupported Level C scope.

## 5. Identity and lifecycle

Durable identity consists of source-pair identity, opaque root token,
constructive intersection lineage, established branch lineage, topology
context, and an explicitly established continuation relation. Semantic
parameter, isolating interval, lifted seam parameter, source revisions,
residual, solver method, and numeric guarantee are revision-scoped evidence.

Continuation preserves a token only for one unique explicit continuation key
inside the same constructive/source-family/branch/topology context. Monotone
parameter scaling, permitted orientation reversal, and periodic seam
canonicalization therefore change evidence without changing the token when
the capability establishes the relation. A topology-context change produces
an identity discontinuity. Merge/split candidates carry candidate parent
tokens where established, but symmetric or nonunique correspondence is
explicitly ambiguous; no universal genealogy or Cartesian nearest-neighbour
selection exists.

The point consumer becomes undefined for absence, stale/unpublished state,
failure, overlap, missing token, duplicate token, missing local isolation,
unsupported evidence, or ambiguous continuation. `INCOMPLETE` and
`NOT_ESTABLISHED` do not themselves invalidate an admissible token. A retained
semantic continuation baseline allows the same proven token to recover after a
temporary complete empty or coherent numerical failure. It never selects a
different root with the same or nearest coordinate. Discovery of an additional
root and deterministic result reordering leave existing established tokens
unchanged.

## 6. Tolerances, work, and state

Productive policy `g8b-initial-normalized/v1` records the G8A-derived defaults:

| Quantity | Value | Meaning |
|---|---:|---|
| root parameter | `1e-12` | provider-declared semantic parameter units |
| absolute residual | `2e-12` | typed normalized model-coordinate distance |
| relative residual | `2e-12` | typed normalized residual characteristic scale |
| tangency | `1e-10` | normalized contact indicator |
| deduplication | `4e-12` | semantic parameter within one branch/component/key |
| continuation | `1e-8` | provider semantic evidence only; never coordinates |
| coordinate verification | `4e-12` | independent reference check only |

The exact approved ceilings are 32,768 semantic evaluations, 16,384
derivative evaluations, 32,768 target evaluations, 8,192 candidate intervals,
8,192 subdivisions, depth 40, 80 refinement iterations per candidate, 1,024
residual verifications, 512 candidates, 4,096 continuation comparisons, 256
published finite solutions, zero retained intersection-index entries, and two
retained topology epochs. Wall clock remains informational. No G7 metric owner,
metric index, cumulative metric state, shared intersection owner, or global
cache is used.

## 7. Productive tests and references

The focused suite executes 49 tests with no failure, error, or skip after R1:

| Suite | Tests | Coverage |
|---|---:|---|
| `G8BIntersectionKernelTest` | 25 | adapters, normalization, line/segment/ray/circle, complete/incomplete/empty, tangency, seams, branches/components, overlap, budgets |
| `G8BIntersectionLifecycleTest` | 15 | rich Geo, complete/incomplete/not-established point admissibility, DAG propagation, new-root/order stability, equal-coordinate tokens, no-retarget, recovery, stale/atomic failure, reparameterization, seam and merge/split, copy/XML/remove/bounds |
| `G8BIntersectionTopologyAndScientificTest` | 9 | viewport/render independence, self/cusp/collapse, mixed overlap, transforms, LSIM/focal pilots, repeated queries |

Analytic formulas and G8A independent reference values are test or validation
oracles only; productive code has no dependency on Python, PDFs, legacy `.ggb`
samples, generated reports, or the CeDG catalog. The reduced LSIM pilot retains
four constructive preimages at two coordinates, while the focal
illumination-inspired circle pilot records the deterministic `2 -> 1 -> 0`
topology transition.

## 8. Compatibility and upstream impact

No command processor, `AlgoDispatcher`, Classic `AlgoIntersect*`, legacy
`GeoLocus`, `Path`, factory, XML tag, persistence, migration, frontend, export,
3D, Python, or G9 source is changed. The internal rich Geo deliberately emits
no XML element. Existing Locus V2 remains experimental, internal, and disabled
by default. Exact source ownership is recorded in
[`modified-files.yml`](../upstream/modified-files.yml) and the focused verifier
rejects any productive path outside the authorized set.

Four existing compatibility tests are the only non-G8B-prefixed Java test edits:
`LocusV2KernelIntegrationTest`, `LocusMetricProductiveLifecycleTest`, and
`G8AIntersectionKernelLifecycleCharacterizationTest` verify append-only class
ordering while preserving their earlier contracts; `DrawablesTest` verifies
the new rich type is exhaustively classified as non-drawable. None creates a
public or persisted route or changes historical scientific evidence.

## 9. Verification record

Entry gates passed before editing: operational (194 registered upstream
files), Locus V2 (73 shared + 3 desktop tests), G7A (51 tests), and G7B
(62 shared + 3 desktop tests). Final R1 saved-source verification passed with:

- `tools/agent/verify-g8b-intersections.ps1`: 49 G8B tests and both
  checkstyles, exit 0;
- `tools/agent/verify-locus-v2.ps1`: 73 shared plus 3 desktop Locus V2
  tests, exit 0;
- `tools/agent/verify-g7b-metrics.ps1`: 62 productive plus 3 laboratory
  tests, exit 0; and
- `tools/agent/verify.ps1` without `-SkipBuild`: complete composed authority,
  exit 0.

The machine evidence records the exact commands and log directories. The
composed logs are under
`%TEMP%\geocedg-g8b-r1-final-composed`; its focused G8B log is
`g8b-intersections\g8b-common.log`. Environment-only retries used no weakened
test contract and did not alter measured evidence.

## 10. Final author closeout

The author approved the following bounded implementation:

1. retain the immutable rich set plus nonnumeric normal-DAG rich Geo as
   authority and the internal semantic-token point as a strict derived consumer;
2. accept the line/segment/ray/circle normalized adapters and append-only
   `GeoClass` entry;
3. accept completeness as independent and preserve typed mixed
   overlap/isolated-root evidence without point sampling;
4. accept the narrow explicit-key continuation contract, including explicit
   ambiguity/discontinuity and no universal merge/split genealogy;
5. retain query-local state, the approved policies/budgets, and all public,
   persistence, Path, Classic, 3D, Level C, and G9 boundaries; and
6. approve the R1 capability boundary: evaluator-only results remain
   `NOT_ESTABLISHED` and localization-only roots remain non-consumable, while an
   individually verified and locally established root may feed the token point
   without a global completeness proof.

The resulting phase boundary is:

```text
G8B-R1 = PASS — AUTHOR APPROVED
G8B = PASS — AUTHOR APPROVED
G8C DESIGN = AUTHORIZED / NOT STARTED
G8C IMPLEMENTATION = NOT AUTHORIZED / NOT STARTED
G8 = IN PROGRESS
G9 = NOT STARTED
```
