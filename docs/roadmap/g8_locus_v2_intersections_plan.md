# G8 — Native Locus V2 2D intersections execution plan

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| G8A state | **PASS — AUTHOR APPROVED** |
| G8B state | **AUTHORIZED / NOT STARTED** |
| Approved stages | G8A characterization and author decisions; G8B minimum internal kernel |
| Specification state | **NORMATIVE — AUTHOR APPROVED** |
| ADR state | ADR 0008 **Accepted** |
| Author-review date | 2026-08-14 |
| Product maturity | Locus V2 remains experimental, internal, and disabled by default |

The author approved this planning architecture, reviewed the completed G8A
characterization, incorporated decisions D1–D17 into the normative contract,
accepted ADR 0008 and authorized a separately invoked G8B. This closeout does
not execute G8B or add productive/observable intersection behavior.

## 1. Authority and preflight record

The initial planning review used the repository root
`C:\DesarrolloyDatos\Areas\ProyectosNoFinanciados\CeDG\GeoCeDG`, branch
`main`, and HEAD `d1fe15568fa838b1fdcc1a4ba2412197668cdc40`.
`origin/main` was fetched without merge, reset, rebase, or history rewrite and
resolved to the same commit. The worktree was clean before planning edits.

The versioned upstream authority remains GeoGebra `5.4.928.0` at
`9b93256b7df401ff056c37b502d82df4d72b1522`, annotated by
`geogebra-baseline-5.4.928.0`.

The current authorities agree that:

- G6 and G6R are closed;
- G7A-R1, G7A, and G7B are `PASS — AUTHOR APPROVED`, and G7 is `PASS`;
- ADR 0006 and ADR 0007 are Accepted;
- `locus-v2-semantics.md` and `locus-v2-metrics.md` are normative;
- no productive G8 intersection package, algorithm, command, or dispatch exists;
- G9 has not started.

The author-review closeout started on branch `feature/g8-planning-closeout` at
HEAD `39353ca627103d0158cda35617077f939cae03b4`; fetched `origin/main` resolved
to the same commit and the worktree was clean. This closeout changes only
versioned planning, proposed-contract, architecture, validation, prompt,
roadmap, guide, and policy-required integrity metadata.

The repository has canonical governance and verification prompts. It has no
`.github/copilot-instructions.md` and no separate canonical planning, style, or
quality prompt; the canonical governance and verification prompts, task
template, change-review prompt, root `AGENTS.md`, and current source therefore
form the applicable instruction chain. The repository also has no
`docs/research/` directory; existing scientific traceability matrices live in
`docs/validation/`, which is retained for this package.

Minor historical prose drift was found in the user guide, but no normative or
source contradiction was found. The planning patch corrects only statements
that still claimed the approved internal G7B kernel did not exist.

## 2. Objective and architectural layer

G8 introduces native, representation-independent intersection semantics
between a semantic Locus V2 and explicitly supported two-dimensional analytic
objects. It belongs in the shared Java kernel because the result must:

- participate in the normal Construction dependency graph;
- preserve source, branch, component, parameter, and revision identity;
- expose geometric absence, degeneration, uncertainty, and failure explicitly;
- remain independent of rendering and of any frontend; and
- update atomically when either source changes.

### Fundamental CeDG capability

First-class intersection/incidence participation is fundamental to the complete
development of CeDG, not a UI convenience or merely compatibility with an
upstream intersection command. Locus-defined projection curves are genuine
geometric results. For every supported ordinary 2D target family, each finite
intersection must therefore be semantically identifiable and, whenever
continuation is unambiguous, usable as a stable input to later construction
steps through the normal dynamic dependency model:

```text
CeDG construction
    -> Locus V2 geometric projection
    -> native intersection with another 2D geometric entity
    -> identified intersection solution(s)
    -> downstream CeDG construction
    -> normal dynamic propagation
```

The intersection cannot be reduced to an anonymous coordinate computed at one
instant. It must preserve constructive traceability, branch/component
provenance, semantic parameterization, dynamic update, solution identity, and
explicit topology changes and degenerations. This structural requirement does
not widen the first supported target set: families are promoted incrementally
only after their semantic and numerical contracts pass characterization.

Characterization fixtures, high-precision references, diagnostics, counters,
and scientific-pilot reduction remain in validation-owned or test-private code.
They are not geometric authority and do not belong in the productive kernel.

## 3. Hard scope boundary

G8 planning covers the semantic/result model, supported target authority,
numerical capability hierarchy, dynamic identity, lifecycle, validation,
functional counters, and the smallest candidate productive impact.

It excludes:

- changes to legacy `GeoLocus` or Classic intersection behavior;
- public `Intersect`/new-command or `AlgoDispatcher` integration;
- public `Path`, point-on-Locus V2, or arbitrary incidence APIs;
- XML, `GeoFactory`, persistence, migration, or saved-file contracts;
- 3D dispatch, spatial/projection identity, or any G9 behavior;
- G5 export changes, Python DSL, GUI/product-profile work, or packaging;
- implicit conversion of unsupported objects merely to claim wider coverage;
- locus–locus and generic parametric–parametric production support in the
  minimum candidate; and
- any render, viewport, zoom, DPI, pixel, `myPointList`, legacy-sample, or
  render-cache authority.

## 4. Author-approved phase structure

The author approves two mandatory stages. No G8C is predeclared.

### G8A — intersection characterization and author decisions

**State:** `PASS — AUTHOR APPROVED`; executed through the separately invoked
versioned G8A prompt, with no productive code.

G8A may add only test-private probes, validation scripts/data, documentation,
and reproducible evidence. It must not add a productive intersection API.

Work packages:

1. freeze the source/API audit and source hashes needed for evidence;
2. implement test-private target residual adapters for the candidate Level-A
   families without changing their productive classes;
3. compare candidate isolation, refinement, tangency, verification,
   deduplication, continuation, and bounded-work strategies;
4. characterize closed rich-result axes and atomic lifecycle publication;
5. characterize semantic root identity through ordinary motion, monotone
   reparameterization, orientation reversal where allowed, seam, boundary,
   branch-lineage, undefined, and recovery events; test merge/split genealogy
   as a hypothesis rather than assuming it;
6. measure evaluator/session reuse and compare query-local state against a
   dedicated revision-scoped intersection index only if repeated-query evidence
   warrants that comparison;
7. execute analytic, independently referenced, topology, lifecycle, and bounded
   scientific pilots;
8. propose measured tolerance and work-budget values without promoting them;
9. update the decision table with evidence and recommendations; and
10. request explicit author approval.

The execution requirements and author decision package are satisfied. The
author approved D1–D17, made the specification normative, accepted ADR 0008
and closed G8A.

### G8B — minimum internal productive 2D intersection kernel

**State:** `AUTHORIZED / NOT STARTED`; execution remains a separate task.

The authorized minimum is an internal, feature-gated kernel service over finite
Locus V2 valid components and a closed Level-A target set. It publishes a rich,
revision-bound result through the normal DAG and includes one required internal
derived point consumer selected by semantic root token. That point is never
intersection authority. There is no public command, `Path`, or persistence
surface.

G8B entry conditions:

- G7 focused gates still pass;
- G8A is `PASS — AUTHOR APPROVED`;
- the intersection specification is explicitly normative;
- ADR 0008 is Accepted or superseded by an Accepted ADR;
- tolerance normalization/values, work ceilings, completeness claims, point
  lifecycle, and minimum target families are author approved; and
- a separate task explicitly invokes the G8B prompt.

G8B exits only after productive tests prove semantic/reference equality,
truthful completeness, tangency support, dynamic identity, atomic invalidation,
bounded state/work, zero forbidden-authority reads, and Classic non-regression.

### Possible later stage

Do not pre-authorize a G8C/G8R. Later evidence may recommend one if it shows that
general conics, hard topology continuation, or performance hardening cannot be
closed safely in the minimum kernel. Functions, general implicit curves, and
locus–locus remain separately gated Level-C extensions even if the eventual
roadmap keeps them under the G8 umbrella.

## 5. Author-approved staged coverage

| Family | G8A characterization | Approved G8B disposition | Reason |
|---|---|---|---|
| line | mandatory | include | authoritative homogeneous equation and unbounded support |
| segment | mandatory | include | line residual plus authoritative limited-path membership and endpoint classification |
| ray | mandatory | include | line residual plus one-sided authoritative membership |
| circle | mandatory | include | stable specialized conic residual and required tangency cases |
| nondegenerate full conics | characterized | defer | G8A found no uniform closed completeness/degeneration contract across subtypes |
| conic parts/arcs | characterize only if needed | defer by default | additional endpoint/limited-path and seam policies |
| functions | Level C | defer | path bounds are view-dependent and discontinuity/domain semantics need a separate authority |
| polynomial implicit curves | Level C | defer | `GeoImplicit` has an authoritative evaluator, but complete isolation and normalization are not a Level-A consequence |
| non-polynomial implicit curves | Level C | defer | evaluator exists, but no general completeness certificate is implied |
| Locus V2–Locus V2 | Level C | defer | two-parameter solving, overlap, dual topology, and identity require a separate contract |
| generic `Path` | out of minimum | defer | `Path` alone does not expose the required semantic domains, branches, or guarantees |

The approved minimum is line, segment, ray, and circle. Full nondegenerate
conics require new explicitly approved evidence for a scale-invariant residual
contract, degenerate-type handling, tangency detection and honest root-set
completeness. Nominal breadth is not a reason to promote a family.

## 6. Author-approved semantic architecture

The approved data flow is:

```text
GeoLocusV2 + supported target GeoElement
        | normal AlgoElement inputs
        v
intersection target adapter + LocusEvaluationSession2D
        v
candidate isolation -> refinement -> residual/domain verification
        v
immutable LocusIntersectionResult2D
        v
non-numeric rich result Geo in the normal DAG
        |
        +--> required internal GeoPoint consumer selected by semantic root token
```

The rich immutable result contains orthogonal query-level computation,
completeness, geometry-kind, currentness, support, and numeric-guarantee axes,
source identities and revisions, policy versions, work evidence, diagnostics,
and zero or more immutable solutions. Each solution retains the source branch,
resolved valid component, canonical semantic parameter, evaluated coordinate,
target parameter when meaningful, isolation/refinement evidence, normalized
residual evidence, guarantee/support level, contact classification, domain
location, currentness, and root-lineage identity.

Geometric existence and numerical confidence are separate axes. In particular:

- `EMPTY` is legal only with `IntersectionCompleteness.COMPLETE` evidence;
- verified roots with `INCOMPLETE` or `NOT_ESTABLISHED` completeness are not a
  complete finite intersection set;
- exhausted work or unresolved tangency is not `NO_INTERSECTION`;
- overlap/infinitely many points is a query-level geometric result, never an
  arbitrary finite point list; and
- ordinary geometric absence does not throw.

The detailed model is in
[the normative specification](../../geocedg/specs/locus/locus-v2-intersections.md)
and the
[semantic architecture](../architecture/locus_v2_intersection_semantic_model.md).

## 7. Author-approved numerical strategy

G8A compared and the author approved this capability hierarchy:

1. authoritative exact/analytic intersection capability when both sources
   genuinely provide it;
2. certified interval/bounds isolation and interval-safe refinement where
   available;
3. safeguarded derivative-aware one-dimensional isolation/refinement;
4. evaluator-only adaptive isolation, which may verify individual roots but
   must report `INCOMPLETE` or `NOT_ESTABLISHED` completeness when it cannot
   exclude additional roots;
5. optional semantic world-coordinate bounds as broad phase only; and
6. two-parameter methods only in deferred Level C.

An analytic target equation does not make a root exact when `F(t)` is evaluated
with uncertified floating-point arithmetic. G8 reuses the G6
`NumericGuarantee` vocabulary and adds intersection-specific method,
completeness, and residual evidence rather than inventing a second exactness
scale.

Sign changes isolate odd-multiplicity candidates but cannot cover tangencies.
Every productive strategy must also use applicable stationary/residual minima,
normalized derivative/contact evidence, endpoint roots, or interval-safe
alternatives. A root is published only after semantic re-evaluation,
target-domain checking and residual verification.

The accepted `g8b-initial-normalized/v1` policy derives from the G8A measured
values. Target adapters expose model-distance-equivalent residuals where
correct or family-specific typed residual/tolerance contracts otherwise;
equation rescaling cannot change the decision. Root/dedup/continuation
tolerances remain provider-parameter quantities, tangency uses a normalized
contact indicator, and coordinate tolerance is verification-only. The exact
values and provisional deterministic ceilings are normative in the
[G8 specification](../../geocedg/specs/locus/locus-v2-intersections.md).

## 8. Author-approved dynamic identity contract

A solution's durable runtime identity is a semantic continuation
token scoped to the source pair, constructive intersection lineage, applicable
branch lineage, and topology/continuation context. It is not its coordinate,
output index, current parameter value, or current isolating interval.

G8 keeps two evidence classes separate:

- durable/continuation information: source-pair identity, constructive
  intersection lineage, applicable branch lineage, topology/continuation
  context, and an explicit continuation relation when established; and
- revision-scoped numerical/localization evidence: current source revisions,
  semantic parameter, isolating parameter interval, residual, method, and
  solver state or certificate.

A root isolating interval is revision-scoped localization/certification
evidence. It is not, by itself, fundamental durable identity. Equivalent
monotone reparameterization must not automatically create a new geometric
intersection merely because its parameter value or interval changes. G8A
established the supported subset for known monotone maps, permitted orientation
reversal and declared periodic-seam representations. Cases without a rigorous
invariant contract return an explicit unsupported, ambiguous, or
not-established identity status; coordinates are never the fallback.

Within unchanged topology, predictor/corrector evidence, mapped semantic
intervals, provider canonicalization, branch lineage, and a proven
reparameterization map are candidate continuation evidence. Cartesian distance
may be logged only as a diagnostic.

The `2 simple roots -> 1 tangent/multiple root -> 2 simple roots` hypothesis
failed as a universal genealogy in forward/reverse symmetric cases. The
accepted narrower contract preserves identity only when continuation is
geometrically unique, records topology events and candidate parent/child
relations when robustly established, and exposes ambiguity or identity
discontinuity otherwise.

Endpoint, invalid-gap, branch-loss, seam, stale, failure, and recovery events
remain explicit. No event may retain stale coordinates as current or infer
continuation from screen order, output order, labels, or coordinate proximity.

## 9. G7 reuse boundary

Legitimate reuse candidates are:

- `LocusDefinition2D`, branch/component/domain metadata, and semantic revision;
- `LocusEvaluationSession2D` for bounded coherent evaluation and cycle guards;
- G6 `NumericGuarantee` vocabulary; and
- normal `AlgoElement` dependency, invalidation, removal, and atomic-publication
  patterns established by G7.

The G7 metric component state and `LocusMetricIndex2D` are not intersection
state and must remain separate. The author-approved G8B state is query-local
and contains no shared intersection owner. A later phase may propose a
dedicated owner only if new repeated-query counters justify it. Any such state
must be bounded, keyed by every result-affecting source/policy/version field,
limited to current revisions, cache-disabled equivalent, and released through
the normal source/consumer lifecycle. It cannot become a global registry or a
second dependency graph.

## 10. Scientific pilots

The recommended bounded pilots are:

1. a focal sphere–cone projection reduced to the documented locus–separatrix
   circle incidence, testing two constructive preimages that may share one
   projection coordinate; and
2. a reduced cone–cylinder LSIM bite/penetration fixture, testing multiple
   leaves and a topology change from connected bite to two disconnected pieces.

The supplied PDFs establish the constructive requirements. The hash-pinned
legacy `.ggb` pair supplies static/manual comparison and nested-cost evidence.
Neither historical samples nor publication claims of exactness are root or
tolerance authority. Full model conversion is not required for G8A or G8B.

## 11. Validation and functional-performance gates

The approved matrix covers analytic/reference correctness, per-root residuals
and an independent result-set completeness axis,
viewport/zoom/DPI independence, reparameterization, scale/translation,
tangency and higher multiplicity, endpoints and seams, distinct preimages at
one coordinate, branches/components/discontinuities, overlap, empty and
unresolved results, creation/annihilation, identity continuation, invalidation,
recovery, exceptions, determinism, bounded work/state, repeated/nested use,
zero forbidden reads, and Classic non-regression.

Hard performance evidence is functional. G8A records semantic evaluations,
derivative evaluations, candidate intervals/boxes, subdivisions, refinement
iterations, residual checks, continuation operations, index builds/hits/misses,
retained entries, and evictions. It must propose separate deterministic maxima
for each work dimension. Wall-clock data remains informational until repeated,
runner-specific evidence and author approval justify a threshold.

See the [validation matrix](../validation/g8_locus_v2_intersection_validation_matrix.md)
and [benchmark plan](../validation/g8_locus_v2_intersection_benchmark_plan.md).

## 12. Authorized productive impact

The authorized G8B implementation should concentrate new code under
`org.geocedg.common.kernel.locus.intersection`, add one focused GeoCeDG
`AlgoElement`, one nonnumeric rich Geo, the required internal token-selected
point consumer, and focused tests. One append-only `GeoClass` member and its
exhaustive drawable/type tests are authorized if required by the rich Geo.
`GeoLocusV2` does not acquire an intersection-owner lease in the query-local
minimum.

`CmdIntersect`, `AlgoDispatcher`, `GeoFactory`, XML handlers, legacy locus
classes, Classic intersection algorithms, 3D commands, and product UI are not
authorized G8B edits. Public integration is a later author decision.

The [upstream impact map](../architecture/locus_v2_intersection_upstream_impact.md)
records the audited files and risks.

## 13. Stop conditions

Stop G8A or G8B and report instead of weakening the contract when:

- the approved G7 baseline does not reproduce;
- a root-set completeness claim cannot be supported;
- tangency depends only on sign changes;
- identity depends on coordinate proximity or output order;
- topology changes cannot be represented explicitly as established lineage,
  ambiguous continuation, or identity discontinuity;
- target residual normalization or tolerances require unmeasured magic values;
- render/sample/view state becomes geometric authority;
- a failure can expose a stale or partially published current result;
- work, retained state, or output history cannot be bounded;
- an owner hides dependency edges or retains obsolete revisions;
- the minimum requires public command, `Path`, XML, 3D, G9, or legacy semantic
  changes; or
- the scientific requirement and actual kernel lifecycle require an unresolved
  author decision.

## 14. Author decision table

The author reviewed the 65-probe G8A package and closed every entry decision:

| ID | Evidence/question | Author-approved disposition | Impact of replacement |
|---|---|---|---|
| D1 | G8A characterization and verification complete | `G8A = PASS — AUTHOR APPROVED` | Reopen characterization and block G8B |
| D2 | Point-only cannot carry set/completeness/overlap truth | Immutable rich set plus normal-DAG nonnumeric rich Geo is authority | Requires a new lifecycle architecture with equivalent rich semantics |
| D3 | Downstream CeDG point-consuming construction passed test-private DAG probe | Require one internal point consumer selected by semantic token; undefined on absence/stale/ambiguity, no retarget, same-token recovery only | G8B would not satisfy first-class downstream CeDG use |
| D4 | Verified roots do not prove exhaustive isolation | Require independent `COMPLETE`/`INCOMPLETE`/`NOT_ESTABLISHED` plus method evidence | Point/scalar consumers could hide missing roots |
| D5 | Even/fourth-order roots defeat sign-change-only search | Analytic/certified/normalized derivative or minimum evidence; undetermined when unproved | Narrow support; false transverse/empty remains forbidden |
| D6 | Coincident/collapsed cases have no canonical finite sample | Typed overlap/infinite/unsupported-overlap result | Sampling or empty remains forbidden |
| D7 | Capability comparison separated truthful guarantees | Analytic/exact, certified, safeguarded derivative-aware, then evaluator-only with weaker completeness | Narrow guarantees or target scope |
| D8 | G8A measured seven independent quantities | Accept as `g8b-initial-normalized/v1`: residuals typed/model-distance-equivalent, parameters provider-scoped, tangency normalized, coordinates verification-only | Recharacterize affected adapters; raw scaling reuse is forbidden |
| D9 | Typed exhaustion and measured nested/repeated work | Provisionally accept exact G8A deterministic ceilings; wall clock informational | Rerun bounded-work evidence for changed budgets |
| D10 | Reparameterization changed parameters/intervals without changing the root | Durable source-pair/token/lineage/topology identity separated from revision evidence | Narrow identity status; coordinate fallback forbidden |
| D11 | Symmetric/reverse `2 -> 1 -> 2` has no canonical descendants | No universal genealogy; use events/candidate relations and explicit ambiguity/discontinuity | A narrower terminate/recreate policy is possible; arbitrary inheritance is not |
| D12 | Query-local 1/3/10/100 and depth 1–3 stayed bounded with zero retained entries | Query-local G8B; no G7 state, global cache, shared owner or index | Later sharing needs new measurements and approval |
| D13 | Core-four adapters closed; full-conic subtype completeness did not | Require line, segment, ray and circle | Any enlargement needs new family evidence |
| D14 | Function/implicit domains and locus–locus two-parameter topology remain open | Defer full conics, functions, general implicit curves and locus–locus | Separate characterization/approval required |
| D15 | No public surface was needed or characterized | Internal only; no command, generic `Path`, point-on API, XML, persistence, legacy/Classic, 3D/G9 or Python | Separate compatibility/serialization phase required |
| D16 | Rich Geo may need exhaustive type identity | Permit one append-only dedicated `GeoClass` only if required | Reusing `DEFAULT`/another class productively is disallowed |
| D17 | G8A resolves the proposal without contradiction | G8 spec normative/author-approved; ADR 0008 Accepted; G8B authorized/not started | Any substantive alternative requires a new author decision |

## 15. G8A characterization outcome

G8A executed 65 test-private probes plus independent 80-digit references. The
author-approved G8B policy is:

- retain the immutable rich set and normal-DAG nonnumeric rich Geo as
  authority, with the required token-selected internal point consumer;
- require the independent `COMPLETE` / `INCOMPLETE` / `NOT_ESTABLISHED` axis;
- promote only line, segment, ray and circle to the minimum G8B candidate;
- defer full conics, functions, implicit curves and locus–locus;
- begin G8B query-local, with no G7 metric state and no shared intersection
  owner;
- distinguish durable constructive/topology identity from revision parameter,
  interval and residual evidence;
- reject universal merge/split child inheritance in favor of event tokens,
  candidate parent/child sets and explicit ambiguity/discontinuity; and
- retain all public command, `Path`, XML, 3D/G9 and legacy boundaries.

The tolerance values are initial versioned defaults only through the normalized
quantity contract. The work ceilings are provisional implementation defaults.
Both retain explicit G8A provenance.

## 16. Planning package and execution prompts

This package comprises:

- this execution plan;
- the normative intersection specification;
- semantic and implementation architecture;
- upstream impact map;
- validation matrix and benchmark/counter plan;
- scientific traceability;
- Accepted ADR 0008; and
- the executed G8A prompt plus its
  [characterization report](../validation/g8a_locus_v2_intersection_characterization_report.md),
  [traceability matrix](../validation/g8a_locus_v2_intersection_traceability_matrix.md)
  and machine-readable evidence; and
- the authorized but unexecuted canonical G8B prompt.

The G8A execution added no observable feature. The G8B prompt is now authorized
for separate execution but remains unexecuted. The user guide records
development state and the absence of public G8 behavior.

```text
G7 = PASS

G8 PLANNING =
PASS — AUTHOR APPROVED

G8A =
PASS — AUTHOR APPROVED

G8B =
AUTHORIZED
NOT STARTED

G8 SPEC =
NORMATIVE / AUTHOR APPROVED

ADR 0008 =
ACCEPTED

G8 PRODUCTIVE IMPLEMENTATION =
NOT STARTED

G9 =
NOT STARTED
```
