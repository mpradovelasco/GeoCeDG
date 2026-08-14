# G8 — Native Locus V2 2D intersections execution plan

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| G8A state | **AUTHORIZED / NOT STARTED**; execution requires a separate task |
| G8B state | **NOT AUTHORIZED**; blocked on G8A `PASS — AUTHOR APPROVED` |
| Approved stages | G8A characterization and author decisions; G8B minimum internal kernel |
| Specification state | **PROPOSED — NOT NORMATIVE** |
| ADR state | ADR 0008 **Proposed** |
| Author-review date | 2026-08-14 |
| Product maturity | Locus V2 remains experimental, internal, and disabled by default |

The author approves this planning architecture and authorizes only a separately
invoked G8A characterization task. This closeout does not execute G8A, make the
proposed specification normative, accept ADR 0008, authorize G8B, or authorize
productive work. G8B remains blocked until G8A passes a second explicit author
review and every productive entry gate below is satisfied.

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

**State:** `AUTHORIZED / NOT STARTED`; execute only through a separate task
that explicitly invokes the versioned G8A prompt.

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

G8A exits only when all evidence is reproducible, every result claim has an
honest support/guarantee level, the minimum G8B object-family scope is closed,
the proposed specification is ready to become normative, ADR 0008 is ready for
acceptance or replacement, and the author records `G8A = PASS — AUTHOR
APPROVED`.

### G8B — minimum internal productive 2D intersection kernel

**State:** `NOT AUTHORIZED / NOT STARTED`; blocked until the G8A author gate
passes.

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

## 5. Author-approved staged coverage

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

The preferred minimum is line, segment, ray, and circle. Full nondegenerate
conics join that minimum only if G8A proves a scale-invariant residual contract,
explicit degenerate-type handling, tangency detection, and honest root-set
completeness with the same bounded architecture. Nominal breadth is not a
reason to promote a family.

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
Every promoted strategy must also characterize stationary/residual minima,
derivative evidence, endpoint roots, and interval-safe alternatives. A root is
published only after semantic re-evaluation, target-domain checking, and
residual verification.

## 8. Dynamic identity characterization contract

A solution's candidate durable runtime identity is a semantic continuation
token scoped to the source pair, constructive intersection lineage, applicable
branch lineage, and topology/continuation context. It is not its coordinate,
output index, current parameter value, or current isolating interval.

G8A must keep two evidence classes separate:

- durable/continuation information: source-pair identity, constructive
  intersection lineage, applicable branch lineage, topology/continuation
  context, and an explicit continuation relation when established; and
- revision-scoped numerical/localization evidence: current source revisions,
  semantic parameter, isolating parameter interval, residual, method, and
  solver state or certificate.

A root isolating interval is revision-scoped localization/certification
evidence. It is not, by itself, fundamental durable identity. Equivalent
monotone reparameterization must not automatically create a new geometric
intersection merely because its parameter value or interval changes. G8A must
establish exactly which monotone reparameterizations, orientation reversals,
and periodic-seam representations preserve identity. Cases without a rigorous
invariant contract return an explicit unsupported, ambiguous, or
not-established identity status; coordinates are never the fallback.

Within unchanged topology, predictor/corrector evidence, mapped semantic
intervals, provider canonicalization, branch lineage, and a proven
reparameterization map are candidate continuation evidence. Cartesian distance
may be logged only as a diagnostic.

The `2 simple roots -> 1 tangent/multiple root -> 2 simple roots` genealogy is
a strong **G8A hypothesis**, not an approved universal identity semantic. G8A
must trace forward and reverse traversal, symmetric cases with intrinsically
ambiguous descendants, periodic-seam interaction, and simultaneous
branch/component change. The preferred hypothesis preserves identity when
continuation is geometrically unique, records parent/child merge/split lineage
when robustly established, and exposes ambiguity or identity discontinuity
otherwise. If the universal genealogy fails, G8A must recommend a narrower
rigorous identity contract.

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
state and must remain separate. The author-approved starting point is
query-local intersection state. G8A may propose a dedicated per-locus
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

The proposed matrix covers analytic/reference correctness, per-root residuals
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

Author approval closes the planning choices marked approved below. It does not
make the proposed specification normative or accept ADR 0008. Characterization
questions remain open until a second author review after G8A.

| Question | Evidence and alternatives | Author-review disposition / advantages and disadvantages | Impact if rejected or changed | Required before G8A? | Required before G8B? |
|---|---|---|---|---|---|
| Phase subdivision | One phase risks premature semantics; G8A/G8B separates evidence from production; a predeclared G8C adds unjustified process | **Approved:** G8A then G8B, no predeclared G8C | Reopen roadmap gates and evidence ownership | resolved | yes, G8A must pass |
| Fundamental first-class capability | CeDG uses locus-defined projections as intermediate geometry; anonymous coordinates lose constructive dependency | **Approved requirement:** rich identified solutions must support downstream normal-DAG construction where continuation is unambiguous; coverage still grows family by family | G8 would no longer satisfy the structural CeDG objective | resolved | binding |
| Rich result and ordinary points | Point-only is familiar but loses completeness/status/lineage; immutable set + rich Geo preserves semantics; points remain ergonomic consumers | **Approved planning architecture:** immutable rich set, normal-DAG nonnumeric rich Geo, optional derived points only after a separate decision | Any alternative requires a new lifecycle/GeoClass audit without weakening rich semantics | resolved | lifecycle details and point scope |
| Completeness axis | Correct returned roots do not prove exhaustive root isolation | **Approved mandatory axis:** `COMPLETE`, `INCOMPLETE`, `NOT_ESTABLISHED`, orthogonal to computation, guarantee, residual, identity, and result kind | Scalar/point projection cannot be approved if it hides set incompleteness | resolved | establishment rules |
| Durable root identity | Coordinates/slots are nonconstructive; parameter and intervals change with revision/reparameterization | **Approved constraint:** durable source-pair/constructive/branch/topology context is separate from revision-scoped parameter, interval, residual, and solver evidence | Narrow support or report identity `NOT_ESTABLISHED`; never fall back to coordinates | resolved | exact invariant subset |
| Merge/split genealogy | Parent/child events express topology, but symmetric split correspondence may be intrinsically ambiguous | **G8A hypothesis:** compare explicit genealogy through forward/reverse `2 -> 1 -> 2`, seam, symmetry, and nearby branch changes | Adopt a narrower rigorous contract with ambiguity/discontinuity states | no | **yes** |
| Tangency and multiplicity | Sign changes miss even roots; derivative/minimum/interval evidence has different guarantees | **Approved principle:** never sign-change-only; distinguish tangent/transverse/multiplicity established from classification undetermined | Unsupported cases remain explicit | resolved | method/evidence policy |
| Overlap/infinite intersections | A continuum is not a finite point array | **Approved principle:** typed `OVERLAP`/`INFINITELY_MANY`/`UNSUPPORTED_OVERLAP`-equivalent outcomes; exact taxonomy remains open | Overlap remains unsupported, never sampled or empty | resolved | exact taxonomy |
| Numeric capability hierarchy | Analytic target data plus floating evaluator is not exact; one generic solver overclaims | **G8A recommendation:** compare exact/certified first, derivative-aware next, evaluator-only with truthful completeness | Narrow guarantees or object-family scope | no | **yes** |
| Tolerance policy | Domain, metric, render, and pixel tolerances have different meanings | **G8A recommendation:** measure independent root-isolation, residual abs/rel, tangency, deduplication, and continuation quantities | Return to characterization; no magic/inherited values | no | **yes** |
| Candidate isolation | Uniform/sign sampling misses contacts; interval subdivision costs more; bounds can accelerate but must be conservative | **G8A recommendation:** compare query-local interval/subdivision plus tangency candidates; bounds are broad phase only | Reduce completeness claims or supported scope | no | **yes** |
| Cache/index ownership | G7 metric state is semantically unrelated; shared state adds lifecycle/staleness risk | **Approved starting point:** query-local, no G7 metric state; a dedicated owner needs measured G8A evidence and later author approval | Accept bounded recomputation or authorize a separately characterized owner | resolved for G8A | **yes** if any owner ships |
| Minimum object families | Core four share a manageable 1D residual model; conics add degeneration/normalization | **Approved preference:** line, segment, ray, circle; conics conditional on evidence | Author may approve a narrower/larger matrix after G8A | resolved for G8A | **yes** |
| Functions, implicit, locus–locus | Domains, completeness, two-parameter solving, overlap, and dual topology widen the problem | **Approved Level C treatment:** characterize; do not promote without G8A evidence | Promotion expands solver/identity evidence and requires explicit author review | resolved for G8A | **yes** |
| Public command/`Path`/serialization | These surfaces create compatibility, migration, label, and persistence contracts | **Boundary closed:** no public command/dispatcher, generic `Path`, point-on API, XML, persistence, migration, 3D/G9, or Classic/legacy changes | Requires a separate approved phase/contract | resolved | binding |

## 15. Planning package and execution prompts

This package comprises:

- this execution plan;
- the proposed intersection specification;
- semantic and implementation architecture;
- upstream impact map;
- validation matrix and benchmark/counter plan;
- scientific traceability;
- Proposed ADR 0008; and
- an author-authorized, separately invoked G8A execution prompt; and
- a future G8B prompt that remains blocked.

This closeout authorizes only a separate execution of the G8A prompt; it does
not execute that prompt. The G8B prompt remains a deliverable only and provides
no implementation authorization.
The planning-only review does not add an observable feature, so the user guide
records only the absence of G8 behavior and links the proposed package.

```text
G7 = PASS

G8 PLANNING =
PASS — AUTHOR APPROVED

G8A =
AUTHORIZED

G8B =
NOT AUTHORIZED
BLOCKED ON G8A PASS — AUTHOR APPROVED

G8 SPEC =
PROPOSED / NOT NORMATIVE

ADR 0008 =
PROPOSED

G8 PRODUCTIVE IMPLEMENTATION =
NOT STARTED

G9 =
NOT STARTED
```
