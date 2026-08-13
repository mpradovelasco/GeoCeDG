# G8 Locus V2 2D intersections — scientific traceability

| Field | Value |
|---|---|
| Status | **PLANNING EVIDENCE — NO G8 EXECUTION CLAIM** |
| Catalog authority | [`docs/references/cedg/catalog.yml`](../references/cedg/catalog.yml) |
| Existing pilot authority | [`scientific-pilots.yml`](../../geocedg/validation/locus-v2/scientific-pilots.yml) |
| Proposed matrix | [`g8_locus_v2_intersection_validation_matrix.md`](g8_locus_v2_intersection_validation_matrix.md) |
| Date | 2026-08-13 |

This matrix translates the versioned CeDG corpus into G8 requirements and
future characterization pilots. The papers, book, and legacy models establish
scientific relevance, constructive topology, and historical limitations. They
do not define numeric G8 answers, solver tolerances, or complete root sets.

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

Future G8A may build small test-private typed reproductions, as G6 did for
nested evaluation. It must not convert legacy sampled points into expected G8
root constants or make the local models build dependencies.

## 3. Requirement-to-source-to-test matrix

| G8 requirement | Scientific evidence | Proposed characterization / regression |
|---|---|---|
| Preserve the constructive parameter/preimage rather than only a point coordinate | LSIM preprint, PDF pp. 6–10; book, PDF pp. 51–54 | `B-SELF-01`, `B-RETRACE-01`, and all rich solution records retain branch/component/semantic parameter |
| Support several constructive leaves/components | LSIM preprint, PDF pp. 6–10; Symmetry article, PDF pp. 7–14 | `B-BRANCH-01`, `B-COMP-01`, reduced `S-LSIM-01` pilot |
| Represent topology transitions explicitly | Bite/penetration cases in the Symmetry article, PDF pp. 8–14; LSIM preprint, PDF pp. 8–10 | `B-TOPO-01`, `I-MERGE-01`, `I-SPLIT-01`; deterministic parameter trace across transition |
| Detect tangent/even-multiplicity contact | Sphere/cone tangent versus secant configurations in LSIM preprint, PDF p. 14; book, PDF pp. 69–72 | `A-LINE-02`, `A-CIR-02/03`, `C-TAN-01`, `N-EVEN-01`, focal `S-FOCAL-01` |
| Preserve distinct spatial/constructive solutions whose projection coordinates can coincide | Focal sphere/separatrix discussion in the book, PDF pp. 69–72 | `A-CIR-06`, `B-SELF-01`; use two semantic preimages and never coordinate dedup |
| Distinguish finite roots from overlap/infinite solution sets | Surface generators/sections can coincide in limiting configurations across LSIM material | `C-OVER-01/02`, `C-INF-01`; query-level overlap result, not sampled points |
| Downstream procedures must consume construction-linked results | Symmetry article links intersection leaves to flattening, PDF pp. 11–14 | Rich normal-DAG result and selected downstream test; no detached report/GUI truth |
| No silent sampled approximation | Book describes historical sampled perimeter and Locus limitations, PDF pp. 51–54; Springer chapter supplies sampled-tool context | Zero legacy/render authority assertions, verified residuals, explicit guarantee/coverage axes |
| Bound repeated and nested work | Legacy two-/three-level cone–cylinder models and Springer tool context | `D-NEST-01/02`, `S-NEST-01`; evaluator, isolation, refinement, and retained-state counters |
| Dynamic edits must not leave stale geometry | Dynamic bite/penetration and flattened-locus examples in the Symmetry article | `I-STALE-01`, `I-FAIL-01`, `I-RECOVER-01`, normal DAG invalidation |

## 4. Proposed scientific pilots

### Pilot S-FOCAL-01 — focal sphere/cone projection

Build a small test-private 2D semantic fixture derived from the published
construction roles: a Locus V2 projection and the authoritative separatrix
circle. Sweep the source parameter through secant, tangent, and empty regimes.

Required evidence:

- two verified roots, one tangent root, and complete empty only when coverage
  is established;
- explicit merge/split lineage;
- repeated coordinate/preimage preservation where the construction demands it;
- target equation scaling and geometry scaling invariance; and
- an independent analytic or high-precision reference generated from the
  reduced fixture, not read from the paper figure or legacy samples.

This is the preferred first scientific pilot because it maps directly to the
proposed circle minimum and exercises tangency/identity.

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

## 5. Reference generation policy

Any new numeric reference must be stored under a future G8A validation bundle
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

