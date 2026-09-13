# PRE-G9B-S1 DXF and bounded-stabilization closeout report

- Status: **PASS — AUTHOR APPROVED**
- Entry commit: `f79234c2c69fc9e30d5fc8e786909ada10909c6e`
- Entry tree: `3440f45335bd7b95c5d0ecb75fe3fb27128e8067`
- Verification class: bounded `PHASE`, selector `PRE-G9B-S1`
- Product version: unchanged at `0.9.0`
- Self approval: `false`
- Successor semantic authority: `PRE-G9B-S1-R2 — PASS — AUTHOR APPROVED`

## Author evidence intake

The author supplied four immutable originals at the exact local intake path
`C:\DesarrolloyDatos\Areas\ProyectosNoFinanciados\CeDG\GeoCeDG\artifacts\GeoCeDG-author-inbox\PRE-G9B-S1-DXF\`.
That ignored local directory is intake evidence, not versioned authority.
Filename, role, byte length and SHA-256 were captured before opening:

| File | Role | Bytes | SHA-256 |
|---|---|---:|---|
| `TestExport1.cedg` | source construction | 25,350 | `3ad3900ccfd76ccb751f802f44764808ff26f6f4cdc58d26506f4e97c9b80612` |
| `geocedg-export.dxf` | author-observed output | 4,187 | `09337cb17183e01b7b20116a1cf4e3b22eaadab2d1c184dd3981d7eece6555ed` |
| `Elipse.cedg` | source construction | 16,379 | `4ec66797984fe68cb6baf43fd141484ce45907f9a52e883ccfe0933dc411cea6` |
| `Elipse.dxf` | author-observed output | 1,480 | `2b2f402000ef95da0e8b0874452226a7f421d84b3b32661c2a57ba23112ef697` |

Only byte-exact copies of the two source constructions were admitted to
[`models/regression/pre-g9b-s1-dxf`](../../models/regression/pre-g9b-s1-dxf/manifest.yml).
The author DXFs and every regenerated output remain unversioned. Their hashes
and semantic observations are recorded in
[`expected-results.yml`](../../models/regression/pre-g9b-s1-dxf/expected-results.yml).

## Reproduction and disposition

### Ellipse

The reported successful-but-empty file is **not reproduced as an encoder or
population defect**. The real GeoCeDG load plus G5 export path finds the two
visible ellipse sources, creates two exact neutral ellipse entities and writes
two AC1015 `ELLIPSE` entities. Their major axes and minor/major ratios match the
construction. The author-observed `Elipse.dxf` itself contains four `POINT` and
two valid `ELLIPSE` entities and passes an independent `ezdxf 1.4.2` audit with
zero errors. Autodesk's DXF contract makes omitted group 210 legal because the
extrusion defaults to `(0,0,1)`.

The remaining external-reader visibility discrepancy is reserved for the
author smoke; changing exact ellipse semantics without a reproducible exporter
defect would be incorrect.

### LocusV2 and SplineV2

`geocedg-export.dxf` identifies itself as the **G5 exact-only** stream and
contains 21 `POINT`, four `XLINE` and one `CIRCLE`; G5 intentionally has no
semantic-curve representation. Extended DXF remains opt-in under the already
approved feature policy, whose default disposition belongs to `PRE-G9B-P1`.

The real G9X1 preflight sees three semantic curves in `TestExport1.cedg`:

| Source | Producer | Result at tolerance 0.001 | Disposition |
|---|---|---|---|
| `IL1` | `SplineV2` | one `APPROXIMATE` component | writable `LWPOLYLINE` plus mandatory fidelity sidecar |
| `IL2` | `SplineV2` | one `APPROXIMATE` component | writable `LWPOLYLINE` plus mandatory fidelity sidecar |
| `m` | `LocusV2` | `INVALID / DISCONTINUITY_UNRESOLVED` | strict rejection; no silent partial publication |

SplineV2 is therefore **Case A**: its existing semantic branches/components and
domains are sufficient for the approved G9X1 export-only approximation. No new
native `SPLINE`, tolerance, closure or topology claim is needed, so
`PRE-G9B-S1-SPLINE-DXF` is **not required**. The invalid `m` is not converted
from render samples and cannot be bridged merely to match what is visible.

The requested corrective continuation disproved the suspected excluded-endpoint
cause: `pi` canonicalizes to `-pi` and evaluates successfully, while the first
dyadic quarter at `-pi/2` and a broad internal negative interval return
`DEPENDENCY_UNDEFINED`. The DXF seam has no semantic point from which to build a
closed full-period representation without bridging an invalid interval. No
productive correction was made. Exact evidence, the 15-source unsupported
complete-construction inventory, and the separate population-policy proposal
are recorded in the [bounded characterization](../architecture/pre_g9b_s1_periodic_locus_and_complete_population_characterization.md).

The subsequent S1-R1 author disposition approved continuous finite valid
components and typed complete population. The architecture review established
that `getValidDomainComponents()` is shared semantic authority, while the
dependent producer supplies no global validity/boundary certificate for its
durably selected intersection root. Pointwise sampling cannot prove a complete
partition, so at that R1 checkpoint the Locus workstream stopped before kernel
semantics were invented.

The subsequent [R2 authority](../architecture/pre_g9b_s1_r2_locus_existence_components_design.md)
defines and implements the shared certificate and consumer migration. It
preserves this historical evidence and does not convert the observed probes
into component boundaries; its author-approved closeout is recorded below.

The independent policy-B correction is implemented as
`geocedg-dxf-geometric-2d/v1`. Complete construction excludes the fixture's two
lists, two numeric parameters, five rich intersection results and six Text
token inputs before strict preflight. These 15 sources receive
`OUTSIDE_GEOMETRIC_POPULATION` diagnostics rather than unsupported outcomes;
26 exact and two approximate components remain eligible. `m` remains an
invalid eligible source, so publication remains blocked. Current selection is
unchanged and an explicitly selected auxiliary remains unsupported.

## Bounded stabilization result

- SplineV2 Algebra/type presentation now follows its `AlgoSplineV2` producer
  and says `Semantic Spline V2`; ordinary LocusV2 remains `Semantic Locus V2`.
- Midpoint is in the existing Point/Intersection toolbar group. Construction
  Text is in the existing Parameters/Controllers toolbar group. Command and
  mode identity are unchanged.
- About uses the exact approved English product statement, upstream baseline
  `5.4.928.0`, and `Manuel Prado-Velasco, Universidad de Sevilla` through the
  existing localized profile.
- Persistent user-tool vertical offset remains `NOT_REPRODUCED`: current
  component tests show the pinned controls clone the live native button's
  border, margin, alignment and min/preferred/max dimensions. No pixel offset
  or speculative layout change is introduced.

## Boundaries and smoke

No exporter, geometric, persistence, feature-default or package-version
semantics changed. S2–S4, D1, P1 and G9B+ remain untouched and unauthorized.

`GUIDE_IMPACT=YES`: the guide records only the implemented type description,
toolbar placement and already-approved extended-DXF behavior.
`BOOTSTRAP_IMPACT=NO`: S1 adds no workstation prerequisite, runtime, package,
environment variable or bootstrap assumption. Verification infrastructure
impact is limited to the additive `PRE-G9B-S1` JUnit inventory and PHASE
selection, requiring `INFRA_UNIT` plus the bounded final PHASE.

Author smoke checklist:

1. Open `Elipse.cedg`, export through the same workflow, and confirm both exact
   ellipses are visible and correct in the external reader.
2. Open `TestExport1.cedg` with extended DXF enabled; select `IL1` and `IL2`,
   export, and confirm two curve polylines plus the required sidecar.
3. Select `m`, or all three semantic curves, and confirm strict preflight
   reports the invalid discontinuity rather than publishing a partial file.
4. Confirm UI exact/approximate/invalid counts match the output and sidecar.
5. Inspect `Semantic Spline V2` in Algebra/properties.
6. Confirm Midpoint is in Point/Intersection and Text in Parameters/Controllers.
7. Inspect pinned user tools for alignment; report DPI/theme if displacement
   persists.
8. Inspect the approved About statement, baseline and author line.

## Final author decision and bounded UI closeout

The author completed the real extended-DXF smoke and reported the S1/R1/R2
functional and scientific behavior resolved. That human observation remains
author evidence and is not represented as an automated verifier result.

The approved technical candidate
`1b7256d798182cbf76d28503da39d96582213071`, tree
`33069ad1b7658fb2a7a81b14edcd0e00b4fbc22c`, adds only a bounded Desktop
presentation correction after the semantic candidate: all preflight,
approximation, diagnostic and completion text is preserved verbatim in a
read-only selectable report area with word wrapping, vertical scrolling, no
horizontal scrollbar and bounded row/column-derived preferred dimensions.
It changes no export model, fidelity, identity, geometry, manifest content or
publication policy. `GUIDE_IMPACT=NO` for this final wrapping correction.

Final verification evidence:

- focused extended-DXF Desktop regressions: 25 tests, zero failures/errors/skips;
- Desktop main/test checkstyle and `git diff --check`: pass;
- `INFRA_UNIT` run `verification-696070fa34ed44db9b3dd15e01e857b5`,
  result `1b511df0b3ac6cb9db995db437791861d9ecceb67f2812a4bc615af9bbd08033`,
  `ACCEPTED / COMPLETE`, zero diagnostics;
- final `PRE-G9B-S1` run
  `verification-149aec6d4615461799ccf3ce406c72d1`, plan
  `ed93c886418ff8673f61c00dd52c2fd36b3bdd63b81241d05d4cb36f32e76d5b`,
  result
  `9287718a09912d7a86f5d43c6c50c72c7617875d4c57707c26dbd5aa1051ab30`:
  400 tests, zero failures/errors/skips, zero diagnostics,
  `ACCEPTED / COMPLETE`.

The approval basis is the prior automated scientific evidence, successful
author smoke, final bounded UI regression and the author's explicit closeout
authorization. It is not agent self-approval. S2 and every later gate remain
unauthorized.

```text
PRE-G9B-S1-R2 = PASS — AUTHOR APPROVED
PRE-G9B-S1-R1 = PASS — AUTHOR APPROVED
PRE-G9B-S1 = PASS — AUTHOR APPROVED
selfApproved = false
```
