# G8 Locus V2 2D intersections — scientific traceability

| Field | Value |
|---|---|
| Status | **G8A/G8B-R1/G8B TRACEABILITY — AUTHOR APPROVED** |
| Catalog authority | [`docs/references/cedg/catalog.yml`](../references/cedg/catalog.yml) |
| Existing pilot authority | [`scientific-pilots.yml`](../../geocedg/validation/locus-v2/scientific-pilots.yml) |
| Approved matrix | [`g8_locus_v2_intersection_validation_matrix.md`](g8_locus_v2_intersection_validation_matrix.md) |
| Executed evidence | G8A [`g8a_locus_v2_intersection_traceability_matrix.md`](g8a_locus_v2_intersection_traceability_matrix.md); G8B [`g8b_locus_v2_intersection_traceability_matrix.md`](g8b_locus_v2_intersection_traceability_matrix.md) |
| Date | 2026-08-14 |

This matrix translates the versioned CeDG corpus into G8 requirements and the
executed reduced G8A/G8B pilots. The papers, book, and legacy models establish
scientific relevance, constructive topology, and historical limitations. They
do not define numeric G8 answers, solver tolerances, root-identity algorithms,
or complete root sets. Current G6/G7 normative contracts, Accepted ADRs, and
actual source architecture remain the software authority.

## Fundamental CeDG requirement traced

The corpus shows that locus-defined projection curves are intermediate
geometric entities in later descriptive-geometry procedures, not disposable
drawings. G8 must therefore enable this construction chain for each supported
ordinary 2D target family:

```text
CeDG construction -> Locus V2 projection -> identified intersection solution
    -> downstream CeDG construction -> normal dynamic propagation
```

Each finite solution must preserve constructive source, branch/component and
semantic-preimage provenance plus explicit dynamic identity/topology status.
The required internal token-selected point consumer carries an admissible
solution into later normal-DAG construction without becoming authority,
silently retargeting or hiding ambiguity. An anonymous coordinate snapshot
cannot satisfy this requirement. The
scientific sources justify the capability and regression scenarios; they do
not prescribe a solver, isolating interval, continuation token, or merge/split
algorithm. Family coverage remains incremental.

G8B-R1 makes this chain available when one solution is rigorously established
even though exhaustive global enumeration has not been proved. That scientific
need does not weaken completeness reporting: the downstream point retains its
parent rich-result provenance, and the construction never implies that an
`INCOMPLETE` or `NOT_ESTABLISHED` set is exhaustive.

## 1. Sources inspected

| Catalog ID | Versioned source | SHA-256 | G8-relevant evidence | Rights boundary |
|---|---|---|---|---|
| `cedg.reference.lsim-preprint-2022` | [`CeDGLocusIntersect_INGEGRAF2022_vFinal_PrePrin.pdf`](../references/cedg/locus-and-intersections/CeDGLocusIntersect_INGEGRAF2022_vFinal_PrePrin.pdf), especially PDF pages 6–10 and 14 | `264f20967cf3c6e2a08ac252ba82d2689433273f514214716d10d061e80b6913` | Construction-owned locus parameter, multileaf surface intersections, configuration-dependent topology, and focal sphere/cone cases with tangent/secant transitions | Catalog says redistribution terms unreviewed; local research source only for this plan |
| `cedg.reference.intersection-flattening-2023` | [`symmetry-15-00984-with-cover.pdf`](../references/cedg/locus-and-intersections/symmetry-15-00984-with-cover.pdf), especially PDF pages 7–14 | `099fc7a25260f017420d7ee3f07bc50b05f6115a3228b9a75c7948fbb104a871` | Four-leaf cone–cylinder examples, bite-to-penetration changes, disconnected pieces, projected-leaf correspondence, and downstream flattening | Catalog records CC-BY-4.0 |
| `cedg.reference.book-2023` | [`Modelado-parametrico-computacional-v2.pdf`](../references/cedg/book/Modelado-parametrico-computacional-v2.pdf), especially PDF pages 51–54 and 69–72 | `81b86cea1a31691d6b24ef4ade4fa2a95c120d984a685a4c399213871a2af012` | LSIM procedure, multiple leaves, tangent configurations, historical Locus-intersection limitations, focal sphere/separatrix construction, and sampled-perimeter limitations | Catalog records restricted local knowledge use |
| `cedg.reference.tools-and-oblique-cone-2025` | [`978-3-031-72829-7_81.pdf`](../references/cedg/developments/978-3-031-72829-7_81.pdf) | `9aff3956f30163ece6485bfe0caa77449ab2e99739768f04c7f41f6227072218` | Historical custom-tool/nested-locus and sampled post-processing cost context; supports a bounded-work requirement, not an intersection algorithm | Catalog records restricted local research use |

Page references above are PDF page indices used during the repository audit,
not a claim that displayed/printed page numbers are identical. The catalog
hashes are the identity authority.

## 2. Existing executable/static evidence

| Evidence ID | Versioned artifact | Role in G8 | Limitation |
|---|---|---|---|
| `cedg.legacy.inter-cil-cono-oblique` | [`InterCilConoOblique.ggb`](../../models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb), SHA-256 `b1cb614f1a4c414144fbff29349ddebda92d1026acb4c535990a2895c589fa27` | Three-level pathological nested-Locus reference and topology/profiling scenario | Legacy sampled loci, rights-blocked, not a semantic/numeric oracle |
| `cedg.legacy.inter-cil-cono-oblique-two-levels` | [`InterCilConoObliqueTwoLevels.ggb`](../../models/legacy/inter-cil-cono-oblique-two-levels/original/InterCilConoObliqueTwoLevels.ggb), SHA-256 `587328a8e5b6474aee3169bb6af2fe2a711e98e000a423a96bba6e38274fb2b6` | Functional two-level control for repeated/nested cost characterization | Legacy sampled result and static provenance only |
| `C-CONE-CYLINDER` | [`scientific-pilots.yml`](../../geocedg/validation/locus-v2/scientific-pilots.yml) | Existing approved requirement mapping for constructive leaves, bite/penetration topology, and multiple projected branches | G6 evidence, not a G8 test result |
| `C-FOCAL-SPHERE-CONE` | same manifest | Existing requirement for multiple leaves under focal geometry | Local curated model is not yet identified there |

G8A built small test-private typed reductions, as G6 did for nested evaluation.
It did not convert legacy sampled points into expected G8 root constants or
make the local models build dependencies.

## 3. Requirement-to-source-to-test matrix

| G8 requirement | Scientific evidence | Characterization / approved regression |
|---|---|---|
| Treat a locus-defined projection as first-class input to later CeDG construction | LSIM methodology in preprint/book; Symmetry article continues intersection leaves into flattening | `D-FIRSTCLASS-01` plus `D-NEST-01`: required token-selected point consumer drives a downstream construction and updates through the normal DAG |
| Preserve the constructive parameter/preimage rather than only a point coordinate | LSIM preprint, PDF pp. 6–10; book, PDF pp. 51–54 | `B-SELF-01`, `B-RETRACE-01`, and all rich solution records retain branch/component/semantic parameter |
| Support several constructive leaves/components | LSIM preprint, PDF pp. 6–10; Symmetry article, PDF pp. 7–14 | `B-BRANCH-01`, `B-COMP-01`, reduced `S-LSIM-01` pilot |
| Represent topology transitions explicitly without assuming universal genealogy | Bite/penetration cases in the Symmetry article, PDF pp. 8–14; LSIM preprint, PDF pp. 8–10 | `B-TOPO-01`, `I-MERGE-01`, `I-SPLIT-01`, `I-REVERSE-01`, `I-SYMMETRIC-01`; record established lineage, ambiguity, or identity discontinuity |
| Detect tangent/even-multiplicity contact | Sphere/cone tangent versus secant configurations in LSIM preprint, PDF p. 14; book, PDF pp. 69–72 | `A-LINE-02`, `A-CIR-02/03`, `C-TAN-01`, `N-EVEN-01`, focal `S-FOCAL-01` |
| Preserve distinct spatial/constructive solutions whose projection coordinates can coincide | Focal sphere/separatrix discussion in the book, PDF pp. 69–72 | `A-CIR-06`, `B-SELF-01`; use two semantic preimages and never coordinate dedup |
| Distinguish finite roots from overlap/infinite solution sets | Surface generators/sections can coincide in limiting configurations across LSIM material | `C-OVER-01/02`, `C-INF-01`; query-level overlap result, not sampled points |
| Downstream procedures must consume construction-linked results | Symmetry article links intersection leaves to flattening, PDF pp. 11–14 | Rich normal-DAG result and `D-FIRSTCLASS-01`; no detached report/GUI truth |
| No silent sampled approximation | Book describes historical sampled perimeter and Locus limitations, PDF pp. 51–54; Springer chapter supplies sampled-tool context | Zero legacy/render authority assertions, verified residuals, explicit guarantee/completeness axes |
| Distinguish verified roots from an exhaustive root set | Multileaf and topology-changing scientific cases make missing leaves scientifically material | `K-COMP-*`, tangency, unbounded and evaluator-only cases report verified-root count and independent completeness evidence |
| Consume an established constructive root without falsely requiring or claiming exhaustive enumeration | Downstream LSIM/flattening procedures need identified intermediate points while multileaf coverage may remain unresolved | G8B-R1 `K-PROJECTION*` and `D-FIRSTCLASS-01`: local isolation/identity admits the selected token while parent completeness remains visible and unchanged |
| Bound repeated and nested work | Legacy two-/three-level cone–cylinder models and Springer tool context | `D-NEST-01/02`, `S-NEST-01`; evaluator, isolation, refinement, and retained-state counters |
| Dynamic edits must not leave stale geometry | Dynamic bite/penetration and flattened-locus examples in the Symmetry article | `I-STALE-01`, `I-FAIL-01`, `I-RECOVER-01`, normal DAG invalidation |

## 4. Executed reduced scientific pilots

### Pilot S-FOCAL-01 — focal sphere/cone projection

G8A built a small test-private 2D semantic fixture derived from the published
construction roles: a Locus V2 projection and the authoritative separatrix
circle. Sweep the source parameter through secant, tangent, and empty regimes.

Required evidence:

- two verified roots, one tangent root, and complete empty only when completeness
  is established;
- merge/split genealogy tested as a hypothesis and rejected as universal, with
  explicit ambiguity or identity discontinuity when lineage cannot be established;
- repeated coordinate/preimage preservation where the construction demands it;
- target equation scaling and geometry scaling invariance; and
- an independent analytic or high-precision reference generated from the
  reduced fixture, not read from the paper figure or legacy samples.

**Outcome:** `F_mu(t)=(t,mu)` against the unit circle produced two transverse
roots at `mu=0.6`, one double tangent at `mu=1`, and complete empty at
`mu=1.2`. The analytic discriminant is reproduced independently at 80 digits.

This is the preferred first scientific pilot because it maps directly to the
approved circle minimum and exercises tangency/identity.

### Pilot S-LSIM-01 — reduced cone–cylinder section

Build a reduced test-private projection/section fixture that preserves
multiple semantic leaves and a controlled bite-to-penetration topology change.
Intersect the relevant semantic branch with one approved Level A target; do not
attempt native surface–surface or 3D semantics.

Required evidence:

- multiple branch/component bindings;
- creation/loss or merge/split events across a deterministic parameter sweep;
- no coordinate-based identity or leaf ordering by label;
- independent reference for the reduced 2D equations; and
- clear documentation of which scientific topology was preserved and which 3D
  construction details were intentionally omitted.

**Outcome:** two semantic branches
`F_sigma(t)=(t,sigma*(t^2-lambda))`, `sigma=±1`, against `y=0` produced four
constructive leaves, two tangent preimages at one coordinate, then two complete
empty branch results. The proxy preserves multileaf/topology requirements and
explicitly omits 3D surface solving.

### Pilot S-NEST-01 — nested evaluation control

Reuse the *dependency shape* characterized by the hash-pinned two-/three-level
legacy cone–cylinder pair, through a small typed semantic fixture. Measure
candidate isolation/refinement and downstream consumption at depths 1–3.

Required evidence:

- no whole-locus regeneration per candidate;
- no render/sample access;
- bounded evaluator session and cycle behavior;
- deterministic functional scaling; and
- correct innermost invalidation/recovery.

**Outcome:** depth 1–3 with ten consumers produced exactly 10/20/30 semantic
evaluator calls and zero whole-locus regeneration. A separate normal-DAG probe
used one identified rich solution as a downstream CeDG-style input and
recovered after injected failure.

## 5. Reference generation policy

The new numeric references are stored in the G8A validation bundle
with:

```text
fixture and formula identifiers
source citation/catalog IDs
script source and SHA-256
interpreter/runtime and library versions
arithmetic precision and rounding policy
input manifest and SHA-256
raw output and SHA-256
independent residual/check method
```

Analytic formulas are preferred where they cover the actual reduced fixture.
High-precision references must use a numerically independent implementation;
rerunning the candidate Java solver with more iterations is not independent.

## 6. Promotion boundary

The scientific cases justify semantic requirements and regressions, not wider
product scope. They do not authorize:

- 3D surface-intersection semantics;
- G9 spatial identity or projection reconstruction;
- conversion of historical LSIM loci into native V2 objects;
- locus–locus support in the minimum G8B;
- use of screenshots, figures, or stored sampled points as expected constants;
  or
- redistribution of rights-restricted source material.

Any conflict between a pilot's necessary semantics and the approved Level A
2D architecture returns to the author decision gate rather than expanding G8B
silently.

The productive G8B regressions preserve four LSIM constructive preimages at
two coordinates and the focal-inspired `2 -> 1 -> 0` circle topology trace.
They exercise generic rich-result, identity and DAG semantics only; no
pilot-specific solver, sampled historical oracle, 3D surface semantics or
Level C support entered productive code. Those four extended families now form
the authorized but not-started G8C design scope; the corpus does not prejudge
their productive phase subdivision or solver architecture.
