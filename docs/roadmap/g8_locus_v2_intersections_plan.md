# G8 — Native Locus V2 2D intersections execution plan

| Field | Value |
|---|---|
| Status | **PLANNING READY FOR AUTHOR REVIEW** |
| G8 execution state | **NOT STARTED** |
| Proposed stages | G8A characterization and author decisions; G8B minimum internal kernel |
| Specification state | **PROPOSED — NOT NORMATIVE** |
| ADR state | ADR 0008 **Proposed** |
| Date | 2026-08-13 |
| Product maturity | Locus V2 remains experimental, internal, and disabled by default |

This document is a planning artifact. It does not authorize G8A probes or G8B
productive work. The author must separately authorize G8A and must explicitly
approve its decisions, the proposed specification, and ADR 0008 before any G8B
productive implementation begins.

## 1. Authority and preflight record

The planning review used the repository root
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

## 4. Recommended phase structure

The smallest sustainable structure is two mandatory stages. A third named
stage is not justified before characterization.

### G8A — intersection characterization and author decisions

**State:** `NOT STARTED`; separate authorization required.

G8A may add only test-private probes, validation scripts/data, documentation,
and reproducible evidence. It must not add a productive intersection API.

Work packages:

1. freeze the source/API audit and source hashes needed for evidence;
2. implement test-private target residual adapters for the candidate Level-A
   families without changing their productive classes;
3. compare candidate isolation, refinement, tangency, verification,
   deduplication, continuation, and bounded-work strategies;
4. characterize closed rich-result axes and atomic lifecycle publication;
5. characterize semantic root identity through continuation, merge, split,
   seam, boundary, branch-lineage, undefined, and recovery events;
6. measure evaluator/session reuse and compare query-local state against a
   dedicated revision-scoped intersection index only if repeated-query evidence
   warrants that comparison;
7. execute analytic, independently referenced, topology, lifecycle, and bounded
   scientific pilots;
8. propose measured tolerance and work-budget values without promoting them;
9. update the decision table with evidence and recommendations; and
10. request explicit author approval.

G8A exits only when all evidence is reproducible, every result claim has an
honest support/guarantee level, the minimum G8B object-family scope is closed,
the proposed specification is ready to become normative, ADR 0008 is ready for
acceptance or replacement, and the author records `G8A = PASS — AUTHOR
APPROVED`.

### G8B — minimum internal productive 2D intersection kernel

**State:** `NOT STARTED`; forbidden until the G8A author gate passes.

The candidate minimum is an internal, feature-gated kernel service over finite
Locus V2 valid components and a closed Level-A target set. It publishes a rich,
revision-bound result through the normal DAG. Ordinary point outputs, if the
author approves them, are derived projections of that result rather than its
authority. There is no public command, `Path`, or persistence surface.

G8B entry conditions:

- G7 focused gates still pass;
- G8A is `PASS — AUTHOR APPROVED`;
- the intersection specification is explicitly normative;
- ADR 0008 is Accepted or superseded by an Accepted ADR;
- tolerance values, work ceilings, completeness claims, output bounds, and
  minimum target families are author approved; and
- a separately issued implementation authorization invokes the G8B prompt.

G8B exits only after productive tests prove semantic/reference equality,
truthful completeness, tangency support, dynamic identity, atomic invalidation,
bounded state/work, zero forbidden-authority reads, and Classic non-regression.

### Possible later stage

Do not pre-authorize a G8C/G8R. G8A may recommend one if evidence shows that
general conics, hard topology continuation, or performance hardening cannot be
closed safely in the minimum kernel. Functions, general implicit curves, and
locus–locus remain separately gated Level-C extensions even if the eventual
roadmap keeps them under the G8 umbrella.

## 5. Candidate coverage

| Family | G8A characterization | Candidate G8B disposition | Reason |
|---|---|---|---|
| line | mandatory | include | authoritative homogeneous equation and unbounded support |
| segment | mandatory | include | line residual plus authoritative limited-path membership and endpoint classification |
| ray | mandatory | include | line residual plus one-sided authoritative membership |
| circle | mandatory | include | stable specialized conic residual and required tangency cases |
| nondegenerate full conics | mandatory | conditional include | same one-parameter problem, but residual normalization and degeneration/completeness must pass G8A |
| conic parts/arcs | characterize only if needed | defer by default | additional endpoint/limited-path and seam policies |
| functions | Level C | defer | path bounds are view-dependent and discontinuity/domain semantics need a separate authority |
| polynomial implicit curves | Level C | defer | `GeoImplicit` has an authoritative evaluator, but complete isolation and normalization are not a Level-A consequence |
| non-polynomial implicit curves | Level C | defer | evaluator exists, but no general completeness certificate is implied |
| Locus V2–Locus V2 | Level C | defer | two-parameter solving, overlap, dual topology, and identity require a separate contract |
| generic `Path` | out of minimum | defer | `Path` alone does not expose the required semantic domains, branches, or guarantees |

The recommended minimum is line, segment, ray, and circle. Full nondegenerate
conics join that minimum only if G8A proves a scale-invariant residual contract,
explicit degenerate-type handling, tangency detection, and honest root-set
coverage with the same bounded architecture. Nominal breadth is not a reason to
promote a family.

## 6. Proposed semantic architecture

The proposed data flow is:

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
        +--> optional bounded ordinary GeoPoint projections
```

The rich immutable result contains a query-level status/coverage/geometry kind,
source identities and revisions, policy versions, work evidence, diagnostics,
and zero or more immutable solutions. Each solution retains the source branch,
resolved valid component, canonical semantic parameter, evaluated coordinate,
target parameter when meaningful, isolation/refinement evidence, normalized
residual evidence, guarantee/support level, contact classification, domain
location, currentness, and root-lineage identity.

Geometric existence and numerical confidence are separate axes. In particular:

- `EMPTY` is legal only with complete coverage evidence;
- verified roots with incomplete coverage are a partial result, not a complete
  finite intersection set;
- exhausted work or unresolved tangency is not `NO_INTERSECTION`;
- overlap/infinitely many points is a query-level geometric result, never an
  arbitrary finite point list; and
- ordinary geometric absence does not throw.

The detailed model is in
[the proposed specification](../../geocedg/specs/locus/locus-v2-intersections.md)
and the
[semantic architecture](../architecture/locus_v2_intersection_semantic_model.md).

## 7. Numerical recommendation for G8A

G8A should compare, rather than prematurely select, a capability hierarchy:

1. authoritative exact/analytic intersection capability when both sources
   genuinely provide it;
2. certified interval/bounds isolation and interval-safe refinement where
   available;
3. safeguarded derivative-aware one-dimensional isolation/refinement;
4. evaluator-only adaptive isolation, which may verify individual roots but
   must report incomplete/unresolved coverage when it cannot certify absence;
5. optional semantic world-coordinate bounds as broad phase only; and
6. two-parameter methods only in deferred Level C.

An analytic target equation does not make a root exact when `F(t)` is evaluated
with uncertified floating-point arithmetic. G8 reuses the G6
`NumericGuarantee` vocabulary and adds intersection-specific method, coverage,
and residual evidence rather than inventing a second exactness scale.

Sign changes isolate odd-multiplicity candidates but cannot cover tangencies.
Every promoted strategy must also characterize stationary/residual minima,
derivative evidence, endpoint roots, and interval-safe alternatives. A root is
published only after semantic re-evaluation, target-domain checking, and
residual verification.

## 8. Dynamic identity recommendation

A solution's durable runtime identity is a semantic continuation token scoped
to the source pair and algorithm, not its coordinate or output index. It is
bound at each revision to the locus identity/revision, target runtime identity,
branch, valid component, canonical root parameter or isolating interval, and
topology epoch.

Within unchanged topology, continuation may use predictor/corrector evidence,
overlap/order of semantic isolating intervals, and provider-canonical parameter
continuity. Cartesian distance may be a diagnostic but never the association
rule.

Proposed topology policy:

- ordinary continuous motion with one isolated root preserves the token;
- a two-root merge creates an explicit merge event and a new tangent-event
  token with both parents;
- a tangent split creates two new child tokens with the tangent parent;
- a periodic seam uses provider canonicalization plus a lifted continuation
  coordinate so one preimage is not duplicated;
- arrival at an included component endpoint is a boundary event;
- crossing an invalid component gap terminates the old token; a root on the
  other side is new unless approved provider lineage proves otherwise;
- branch split/merge follows G6 typed branch lineage and never infers continuity
  from screen order or coordinates; and
- stale results are explicitly non-current and cannot leave old point
  coordinates presented as current.

The author must approve this policy after G8A evidence. If it cannot be proven
for a topology family, that family remains explicitly unsupported.

## 9. G7 reuse boundary

Legitimate reuse candidates are:

- `LocusDefinition2D`, branch/component/domain metadata, and semantic revision;
- `LocusEvaluationSession2D` for bounded coherent evaluation and cycle guards;
- G6 `NumericGuarantee` vocabulary; and
- normal `AlgoElement` dependency, invalidation, removal, and atomic-publication
  patterns established by G7.

The G7 metric component state and `LocusMetricIndex2D` are not intersection
state and must remain separate. The first productive candidate should use
query-local isolation state. G8A may compare a dedicated per-locus
intersection owner only if repeated-query counters justify it. Any such state
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

The proposed matrix covers analytic/reference correctness, normalized residuals,
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

## 12. Candidate productive impact after approval

The candidate G8B implementation should concentrate new code under
`org.geocedg.common.kernel.locus.intersection`, add one focused GeoCeDG
`AlgoElement`, one nonnumeric rich Geo, and focused tests. An append-only
`GeoClass` member and its exhaustive drawable test may be unavoidable if the
rich Geo architecture is approved. `GeoLocusV2` changes only if an approved,
measured owner lease is required.

`CmdIntersect`, `AlgoDispatcher`, `GeoFactory`, XML handlers, legacy locus
classes, Classic intersection algorithms, 3D commands, and product UI are not
candidate G8B edits. Public integration is a later author decision.

The [upstream impact map](../architecture/locus_v2_intersection_upstream_impact.md)
records the audited files and risks.

## 13. Stop conditions

Stop G8A or G8B and report instead of weakening the contract when:

- the approved G7 baseline does not reproduce;
- a root-set completeness claim cannot be supported;
- tangency depends only on sign changes;
- identity depends on coordinate proximity or output order;
- topology changes cannot be represented with explicit lineage;
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

No recommendation below is normative before explicit author approval.

| Question | Evidence | Alternatives | Recommendation and trade-off | If rejected | Before G8A? | Before G8B? |
|---|---|---|---|---|---|---|
| Phase subdivision | G6/G7 succeeded with characterization before productive code; G8 has larger topology uncertainty | one phase; G8A/G8B; add a predeclared hardening stage | Use G8A/G8B only; add no third stage until evidence justifies it | rewrite execution gates and evidence ownership | **yes** | yes |
| Rich result versus public points | G7 atomic rich Geo is a useful lifecycle precedent; upstream points lose residual/status/lineage | points only; rich Geo per solution; immutable set + one rich Geo + derived points | Immutable set + one nonnumeric rich Geo; derive bounded ordinary points only if separately approved | select another normal-DAG carrier and re-audit lifecycle/GeoClass | characterize | **yes** |
| Dynamic root identity | Classic uses coordinate-nearness/output permutations, including view-scale input; G6 provides semantic branches/parameters/lineage | coordinate/order matching; parameter continuation; no continuation | Parameter/isolation continuation scoped by source pair and topology, with explicit split/merge lineage | restrict G8B to static root sets or approve another non-coordinate model | characterize | **yes** |
| Tangency and multiplicity | even roots have no sign change; upstream special cases are target-specific | sign change only; derivative/minimum evidence; interval certification | Compare safeguarded derivative/minimum and interval-safe routes; claim multiplicity only with evidence | tangencies/higher roots remain unsupported | characterize | **yes** |
| Overlap/infinite intersections | a continuum cannot be represented by arbitrary points | finite sampling; typed overlap/infinite result; unsupported | Closed query-level `OVERLAP`/`INFINITE` result; no sampled point list | overlap remains explicit `UNSUPPORTED`, never empty | characterize | **yes** |
| Exact/analytic versus numerical hierarchy | analytic target equation plus floating `F(t)` is not exact; G6 vocabulary exists | one generic solver; capability hierarchy | exact/certified first, derivative-aware next, evaluator-only with truthful partial coverage | narrow supported claims/families | characterize | **yes** |
| Tolerance policy | G6 domain, G6 validation, G7 metric, render, and pixel tolerances have different dimensions | reuse existing constants; one intersection epsilon; versioned quantity-specific policy | independent root-isolation, normalized residual abs/rel, tangency, dedup, and continuation quantities; measure before values | return to characterization with revised policy | characterize | **yes** |
| Candidate isolation | upstream function root sampling is view-sensitive; render samples are forbidden | uniform evaluator sampling; interval subdivision; semantic bounds/index broad phase | compare query-local interval/subdivision plus tangency candidates; bounds only broad phase | reduce completeness claims or family scope | characterize | **yes** |
| Cache/index ownership | G7 metric state is semantically different; G6 sessions are bounded | no reuse; algorithm-local; metric-index reuse; dedicated intersection owner | query-local first; compare a dedicated current-revision owner only if counters prove need | accept recomputation if bounded, or authorize a different measured owner | no | **yes** if any cache ships |
| Minimum object families | line/segment/ray/circle share a manageable 1D residual model; conics add degeneration/normalization | line only; core four; core four + conics | core four; conditionally include full nondegenerate conics after G8A evidence | narrower/larger G8B matrix and schedule | characterize | **yes** |
| Implicit and locus–locus | actual `GeoImplicit` has evaluator/derivatives, but completeness and 2D continuation are separate problems | include now; defer both; include polynomial implicit only | defer both from minimum; characterize polynomial implicit as Level C | expand G8A/G8B and require new solver/identity evidence | no | **yes** |
| Public command/dispatcher | no public V2 command/path/persistence exists; G8 objective does not require UI | internal only; extend `Intersect`; new command | internal only in G8B; public dispatch is a later author gate | broaden upstream edits, compatibility tests, localization and persistence analysis | no | **yes** |
| Public `Path` and point-on-locus | intersection roots already retain a preimage but general path motion has different ambiguity | infer Path; add restricted incidence; keep private | keep no public `Path` and no general point-on API | create a separate approved semantic contract | no | **yes** |
| Serialization | runtime root identity can be construction-scoped; persistent identity needs a migration/version contract | persist now; no XML; transient diagnostic serialization | no XML/factory/persistence in G8 | open a separate compatibility and migration phase | no | **yes** |

## 15. Planning package and execution prompts

This package comprises:

- this execution plan;
- the proposed intersection specification;
- semantic and implementation architecture;
- upstream impact map;
- validation matrix and benchmark/counter plan;
- scientific traceability;
- Proposed ADR 0008; and
- draft G8A and G8B execution prompts.

The prompts are deliverables only. Their presence does not authorize execution.
The planning-only review does not add an observable feature, so the user guide
records only the absence of G8 behavior and links the proposed package.

```text
G7 = PASS — BASELINE CONFIRMED
G8 PLANNING = READY FOR AUTHOR REVIEW
G8A = NOT STARTED
G8B = NOT STARTED
G8 PRODUCTIVE IMPLEMENTATION = NOT STARTED
G8 SPEC = PROPOSED / NOT NORMATIVE
G9 = NOT STARTED
```
