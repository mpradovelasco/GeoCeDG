# PRE-G9B-S1-R2 — existence/component implementation gate

- Status: **PASS — AUTHOR APPROVED**
- Entry: `e5260fc7dd4924f0599ac3ac2700025bf5e48330`
- Design: [R2 architecture](../architecture/pre_g9b_s1_r2_locus_existence_components_design.md)
- Specification: [R2 normative candidate](../../geocedg/specs/locus/locus-v2-existence-components.md)
- Decision: [accepted ADR 0027](../adr/0027-locus-v2-existence-and-continuous-valid-components.md)
- `selfApproved=false`

The author approved the R2 design and bounded I1--I3 implementation. Permanent
evidence is provided by `PreG9BS1R2ExistenceStructureTest`, the author-backed
`PreG9BS1AuthorDxfReproductionTest`, and the retained Locus V2, metric,
intersection, render, G9X1 and S1 regressions in the R2/S1 phase selections.
Technical PASS remains distinct from author approval. The author subsequently
reported the functional S1/R1/R2 smoke successful and explicitly approved the
scientific/product result. The final report-layout correction changes no
semantic evidence or DXF content.

## Required semantic cases

| ID | Required assertion | Evidence layer |
|---|---|---|
| R2-D01 | Canonical domain, existence coverage, continuous-valid components and completeness are separate typed axes. | shared value model |
| R2-D02 | Open, closed and half-open endpoints retain exact semantic ownership. | interval model |
| R2-D03 | `COMPLETE` requires coverage of the entire canonical domain and finite maximal components. | certificate validation |
| R2-D04 | `NOT_ESTABLISHED` retains local certificates without an empty or complete claim. | certificate validation |
| R2-D05 | Existence, nonexistence and unresolved coverage are pairwise disjoint; complete coverage has no silent holes. | certificate validation |
| R2-D06 | Provider periodicity applies to canonical parameterization, not whole-domain dependent existence. | generator descriptor |
| R2-D07 | A full periodic component closes only with complete periodic-component evidence. | semantic component |
| R2-D08 | Semantic signature/revision changes when authoritative coverage changes; point evaluation and rendering do not advance revision. | lifecycle |

## Root and boundary certification

| ID | Required assertion | Evidence layer |
|---|---|---|
| R2-R01 | Interval certificate refers to the exact selected root token/selector and allocates no new token. | root capability |
| R2-R02 | Same-root interval existence and unique continuation are proved without coordinates, proximity or solver order. | root capability |
| R2-R03 | One-state root germ/address/isolation evidence alone cannot certify an interval. | negative contract |
| R2-R04 | Tangency, multiplicity, ambiguous continuation and merge/split return not established. | root negatives |
| R2-R05 | Temporary root loss preserves the selector/locus identity but divides current component coverage. | lifecycle |
| R2-R06 | Recurrence does not join components or reuse lineage without explicit continuation evidence. | lifecycle |
| R2-B01 | Structural/provider event boundaries take precedence over numerical discovery. | certifier |
| R2-B02 | Certified interval predicate may isolate a boundary deterministically with recorded enclosure/work. | certifier |
| R2-B03 | Point samples can falsify a proposed interval but cannot prove its interior or global completeness. | negative contract |
| R2-B04 | Work exhaustion, nonfinite arithmetic and unresolved enclosures publish `NOT_ESTABLISHED`. | resource/negative |
| R2-B05 | A safe local subinterval is labelled non-maximal and leaves boundary uncertainty explicit. | certificate validation |

## Author witness `m`

| ID | Required assertion | Evidence layer |
|---|---|---|
| R2-M01 | Fixture loads with stable `m` identity, `circle-point/v1`, branch `generator.main` and canonical `[-pi,pi)`. | native fixture |
| R2-M02 | `pi` canonicalizes to `-pi`; both evaluate `VALID`; the periodic seam is not the observed defect. | evaluator |
| R2-M03 | `0` and `pi/2` are valid; `-pi/2` and the observed wider internal negative region are `DEPENDENCY_UNDEFINED`. | evaluator |
| R2-M04 | Exact token-selected `S` becomes undefined, then midpoint `U` becomes undefined, without retargeting. | DAG/root consumer |
| R2-M05 | The pre-R2 whole-domain component claim is rejected as continuous existence evidence. | migration regression |
| R2-M06 | Final intervals/count are derived from interval certificates, never hard-coded from fixture probes. | implementation review |
| R2-M07 | Every published component is finite, oriented, deterministic and separated from certified-invalid/unresolved regions. | certificate + fixture |
| R2-M08 | No component bridges the internal undefined interval; source identity/revision remain coherent. | lifecycle/export |
| R2-M09 | If the selected-root family cannot certify complete boundaries, global state remains `NOT_ESTABLISHED`. | fail-closed fixture |

## Consumer policy

| ID | Required assertion | Evidence layer |
|---|---|---|
| R2-C01 | Rendering draws only certified intervals and does not join unresolved gaps or alter semantic evidence. | render cache |
| R2-C02 | Viewport, zoom, DPI and render tolerance do not change certificates or component identity. | render/semantic regression |
| R2-C03 | Component-local metric can succeed under global `NOT_ESTABLISHED`; total locus length cannot claim completeness. | metric |
| R2-C04 | Intersection may publish an independently admissible local root; empty/all-roots requires domain and solver completeness. | intersection |
| R2-C05 | Exact point address within a certified component works; global nearest/route inference fails without sufficient coverage. | point/path |
| R2-C06 | Nested/transform producers propagate incompleteness and never strengthen it implicitly. | composition |
| R2-C07 | Principal/component selection does not use component ordinal as durable identity. | selector negative |
| R2-C08 | Existing direct analytic/static providers retain equivalent complete behavior. | G6/G7/G8 regression |
| R2-C09 | Legacy `GeoLocus`, Classic commands and old files remain unchanged. | compatibility |

## DXF and retained S1

| ID | Required assertion | Evidence layer |
|---|---|---|
| R2-X01 | Each finite certified component produces one independent neutral curve component and `LWPOLYLINE`. | G9X1 adapter/parser |
| R2-X02 | No invalid or unresolved interval contributes vertices or a connecting segment. | G9X1 negative |
| R2-X03 | Approximation fidelity/tolerance/work remain export-only and explicit. | G9X1 evidence |
| R2-X04 | Sidecar binds source identity, branch, component address/lineage, interval openness, revision and completeness. | manifest |
| R2-X05 | Global `NOT_ESTABLISHED` is visible in preflight/sidecar and never described as the complete locus. | Desktop + manifest |
| R2-X06 | Failed approximation of any claimed component preserves strict atomic DXF/sidecar publication. | G9X1 rollback |
| R2-X07 | Repeated runs produce deterministic component order, handles, DXF and sidecar hashes. | deterministic rerun |
| R2-X08 | IL1/IL2 SplineV2 approximation and G5 exact entity mappings remain unchanged. | retained G9X1/G5 |
| R2-X09 | `geocedg-dxf-geometric-2d/v1` population and strict Current selection behavior remain unchanged. | R1 regression |

## Persistence and lifecycle

| ID | Required assertion | Evidence layer |
|---|---|---|
| R2-L01 | Certificate is bound to the exact locus/dependency revision vector and stale evidence is rejected. | currentness |
| R2-L02 | Save/reopen recomputes the same certificate from durable inputs/policies; no sample cache is authority. | native persistence |
| R2-L03 | Undo/redo restores/recomputes corresponding existence/currentness state. | host lifecycle |
| R2-L04 | Copy/remap follows existing fresh-ID rules and recomputes certificate against remapped dependencies. | G9A regression |
| R2-L05 | Compatible redefine revalidates evidence; it does not preserve a stale certificate for continuity. | G9A3 regression |
| R2-L06 | Old files receive no fabricated component lineage or semantic associations. | compatibility negative |
| R2-L07 | Temporary invalidity does not allocate a new Locus V2 or root persistent identity. | lifecycle |
| R2-L08 | Dynamic component lineage is revision-local unless an explicit unique transition is proved. | lineage |

## Candidate verification gates

The author-authorized I1--I3 implementation uses focused shared-kernel,
consumer, persistence, G9X1 and author-fixture tests. The registered R2
selection at the scientific candidate contained 241 shared and 35 Desktop
identities; its accepted receipt executed 276 tests. The final combined S1
selection contains 250 shared and 150 Desktop identities after adding one
bounded report-layout regression. The immutable implementation candidate ran:

1. `git diff --check` and applicable shared/Desktop checkstyle;
2. registry/inventory consistency;
3. `INFRA_UNIT` exactly once if adding the R2 phase selector changes executable
   verification inventory;
4. exactly one `PHASE -Phase PRE-G9B-S1-R2`; and
5. before author closeout, exactly one combined
   `PHASE -Phase PRE-G9B-S1` on the final UI-corrected candidate.

Final combined evidence is bound to
`1b7256d798182cbf76d28503da39d96582213071`, tree
`33069ad1b7658fb2a7a81b14edcd0e00b4fbc22c`: run
`verification-149aec6d4615461799ccf3ce406c72d1`, plan
`ed93c886418ff8673f61c00dd52c2fd36b3bdd63b81241d05d4cb36f32e76d5b`
and result
`9287718a09912d7a86f5d43c6c50c72c7617875d4c57707c26dbd5aa1051ab30`.
It executed 400 tests with zero failures, errors, skips or diagnostics and
reported `ACCEPTED / COMPLETE`.

`INTEGRATION` is not automatic. It becomes an escalation only if the actual
implementation changes shared lifecycle/persistence beyond the bounded R2
inventory or leaves a concrete cross-phase obligation uncovered. `FINAL` is not
authorized by this matrix.

## Implementation stop conditions

Stop before product publication if:

- the selected-root family cannot provide interval-wide identity/uniqueness
  evidence without redesigning root identity;
- component boundaries require sampling as authority;
- a consumer cannot distinguish local admissibility from global completeness;
- the old accessor cannot be migrated without a period of semantic ambiguity;
- a certificate would duplicate another existing authority; or
- deterministic bounded work and fail-closed outcomes cannot be established.
