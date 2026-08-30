# G9U0-R5 Locus V2 2D similarity transformations validation matrix

| Field | Value |
|---|---|
| Phase | **G9U0-R5 — LOCUS V2 2D SIMILARITY TRANSFORMATIONS** |
| Status | **DESIGN CANDIDATE — PENDING AUTHOR REVIEW** |
| Implementation | **NOT AUTHORIZED — NOT STARTED** |
| Required predecessor | G9U0-R4 `PASS — AUTHOR APPROVED` |
| Draft contract | [`locus-v2-similarity-transformations.md`](../../geocedg/specs/locus/locus-v2-similarity-transformations.md) |
| Zero-scale policy | **AUTHOR DECISION REQUIRED:** Option A (recommended) valid source-domain-preserving `COLLAPSED_IMAGE`, or Option B explicit unsupported/undefined transformed state |
| Relevant accepted contracts | [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md), [`semantic model`](../architecture/locus_v2_semantic_model.md), [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md), [`metric model`](../architecture/locus_v2_metric_semantic_model.md), Accepted ADR 0017 intrinsic semantic phase/rank authority |

This matrix designs future deterministic validation. No row is claimed
executed or passed by the R5 planning candidate.

Before productive implementation, the author-approved R5 authority must select
exactly one zero-scale option. Only that option's conditional rows are then
active; the other branch remains design evidence and must not be implemented.

## 1. Public command routing

| ID | Scenario | Required assertion |
|---|---|---|
| R5-C01 | `Translate[L,v]` | Existing command creates one semantic `GeoLocusV2`; no parallel command or generic mutation output |
| R5-C02 | `Rotate[L,a]` | Existing two-argument rotation about origin creates one semantic output |
| R5-C03 | `Rotate[L,a,C]` | Existing centered rotation creates one semantic output with `a` and `C` as DAG inputs |
| R5-C04 | `Reflect/Mirror[L,P]` | Existing central-reflection aliases route to the same Locus V2 authority |
| R5-C05 | `Reflect/Mirror[L,g]` | Existing axial-reflection aliases route to the same Locus V2 authority |
| R5-C06 | `Dilate[L,k]` | Existing origin-dilation form creates one semantic output |
| R5-C07 | `Dilate[L,k,C]` | Existing centered-dilation form creates one semantic output |
| R5-C08 | `Reflect[L,circle]` | Explicitly outside R5; no accidental inversion-as-reflection support |
| R5-C09 | shear/stretch/projective/3D forms | Explicitly outside R5; legacy dispatch unchanged |
| R5-C10 | feature ON | One existing Locus V2 opt-in is sufficient for all supported forms |
| R5-C11 | feature OFF interactive | Typed unavailable result; no transformed V2 created |
| R5-C12 | feature OFF file loading | Supported persisted transformed locus reconstructs/preserves without enabling creation |
| R5-C13 | current host route audit | Basic factory, four 2D processors, dispatcher and four transform wrappers match the approved characterization; vector-at-point, rotate-text and circle-inversion routes are unchanged |

## 2. Durable identity and DAG provenance

| ID | Scenario | Required assertion |
|---|---|---|
| R5-I01 | ordinary nonidentity transform | Output has a new durable ID distinct from source |
| R5-I02 | zero vector / zero angle / `k=1` | Geometrically identical image still has a new output ID |
| R5-I03 | double reflection/full-turn composition | Coincident final image never reuses an ancestor ID |
| R5-I04 | dependency record | Exact direct IDs are source plus current vector/angle/center/axis/factor inputs; sorted/unique by existing contract |
| R5-I05 | label/rename | Source/output rename changes no semantic identity, address or geometry |
| R5-I06 | XML/construction reorder test fixture | No identity derived from XML position, construction index or label |
| R5-I07 | copy/remap | Existing copy provenance remaps source and transform dependencies exactly; copied output ID is new |
| R5-I08 | branch keys | Keys may be equal across source/output only under their distinct locus-ID namespaces |

## 3. Evaluation and semantic address preservation

Use line, circle/periodic, disconnected, open, empty and reconstructible
dependent-construction loci.

| ID | Scenario | Required assertion |
|---|---|---|
| R5-E01 | translation at several `(branch,u)` | Evaluated point equals source point plus current vector |
| R5-E02 | rotation about origin | Evaluated point equals the ordinary finite 2D rotation |
| R5-E03 | rotation about dynamic center | Evaluated point equals centered rotation and updates normally |
| R5-E04 | central reflection | Evaluated point equals `2P-sourcePoint` |
| R5-E05 | axial reflection | Evaluated point equals reflection in current finite line |
| R5-E06 | positive dilation | Evaluated point equals centered scale by `k` |
| R5-E07 | negative dilation | Geometry is correct; semantic traversal is not reversed |
| R5-E08 | source invalid address | Same invalid source status; transformation does not fabricate a point |
| R5-E09 | transformed arithmetic overflow | `NON_FINITE`; no stale coordinate or exception-published point |
| R5-E10 | style-only mutation | Source/output semantic revisions and downstream semantic results unchanged |

## 4. Domain, topology and periodic seam

| ID | Scenario | Required assertion |
|---|---|---|
| R5-D01 | finite closed component | Declared domain, valid component and endpoint inclusion exactly preserved |
| R5-D02 | open component | Open endpoints remain open; no inset/closure semantic rewrite |
| R5-D03 | disconnected components | Same component count/bounds; no geometric connector |
| R5-D04 | multiple semantic branches | Same branch keys under new locus ID; no merge from coincident images |
| R5-D05 | periodic source | Same provider canonicalization and half-open seam; equivalent endpoints remain one semantic address and one semantic phase frame |
| R5-D06 | reflection | Ambient orientation change does not alter branch parameter orientation or make Cartesian orientation phase/rank authority |
| R5-D07 | negative dilation | Parameter orientation and the semantic phase frame remain the source orientation |
| R5-D08 | empty domain | `EMPTY_DOMAIN`; no branch/point/sample fabricated |
| R5-D09 | source split/merge lineage | Current typed lineage is preserved; transform creates no false topology event |

## 5. Degeneration matrix

| ID | Input/state | Required assertion |
|---|---|---|
| R5-G01 | source temporarily undefined | Output/downstream results undefined; no stale semantic snapshot; recovery deterministic |
| R5-G02 | undefined/nonfinite vector | No valid output publication |
| R5-G03 | undefined/nonfinite angle | No valid output publication |
| R5-G04 | undefined/infinite/3D center | No valid 2D output publication |
| R5-G05 | undefined/nonfinite/zero-normal axis | No valid axial-reflection publication |
| R5-G06 | undefined/nonfinite factor | No valid dilation publication |
| R5-G07A | **Option A:** `k=0`, nonempty source domain | Valid semantic output; source domains/branches retained; `COLLAPSED_IMAGE` present |
| R5-G07B | **Option B:** `k=0`, nonempty source domain | Explicit unsupported/undefined transformed state; no valid or stale semantic snapshot |
| R5-G08A | **Option A:** `k=0`, source has invalid gap | Gap remains invalid; zero scale does not heal it |
| R5-G08B | **Option B:** factor changes `nonzero -> 0 -> nonzero` | Unsupported/undefined only at zero; deterministic recovery; no stale point, metric or token |
| R5-G09A | **Option A:** `k=0`, multi-branch source | Branch identities remain distinct even though images coincide |
| R5-G10A | **Option A:** later finite transform of collapsed output | Normal DAG result; collapsed property/zero metric retained |
| R5-G11 | factor crosses zero under the selected policy | Exact selected behavior; deterministic revisions/recovery; no stale point, metric or token |
| R5-G12 | temporarily undefined transform then recovery | No hidden snapshot/coordinate reuse |

## 6. Metric covariance

Test both total metric and partial metric between semantic points with rich value,
coverage, diagnostic and error evidence.

| ID | Scenario | Required assertion |
|---|---|---|
| R5-M01 | translation | Total length invariant within approved metric evidence |
| R5-M02 | rotation, origin/center | Total length invariant |
| R5-M03 | axial/central reflection | Total length invariant |
| R5-M04 | dilation `k>0` | Total length equals `abs(k)` times source |
| R5-M05 | dilation `k<0` | Total length equals `abs(k)` times source; no orientation sign leak |
| R5-M06A | **Option A:** `k=0` | Rich zero with typed `COLLAPSED_IMAGE` evidence according to the accepted collapsed-image metric contract |
| R5-M06B | **Option B:** `k=0` | No metric value is fabricated from the unsupported/undefined transformed locus |
| R5-M07 | corresponding partial addresses | Same invariance/scaling as total metric |
| R5-M08 | periodic wrapped partial route | Same wrap/seam policy; no connector segment |
| R5-M09 | disconnected/unbounded source | Existing coverage/unsupported/infinite status covaries truthfully |
| R5-M10 | style/zoom/DPI changes | Metric value/evidence and semantic revisions unchanged |

## 7. Point-on-Locus covariance

| ID | Scenario | Required assertion |
|---|---|---|
| R5-P01 | `Point[T(L),branch,u]` for each family | Geometry equals transformed source semantic point at same address |
| R5-P02 | source and transformed points | Distinct durable point identities; no coordinate identity |
| R5-P03 | dynamic transformation input | Point updates through transformed-locus dependency |
| R5-P04 | periodic seam address | Canonical address preserved; no duplicate point identity |
| R5-P05 | invalid/disappeared source address | Point becomes undefined and never jumps to another branch/address |
| R5-P06 | save/reopen | Exact source, transformed locus, branch and parameter dependencies retained |

## 8. Intersection covariance and token isolation

Use supported line, segment, ray, circle, conic, bounded function, regular
polynomial implicit and Locus V2 targets where their current capability applies.
All dynamic cases consume R4's deterministic current-snapshot selector,
including its intrinsic semantic phase/rank proof. The transformed source
domain, component lineage, periodic seam and semantic orientation drive a new
phase/rank in the transformed source-pair context. Prior transformation/root
history and Cartesian, solver/list, marker or UI order may not decide it or a
token.

| ID | Scenario | Required assertion |
|---|---|---|
| R5-X01 | `Intersect[T(L),X]` per target family | Ordinary rich pipeline consumes transformed evaluator; no type exception |
| R5-X02 | translate both operands | Finite/overlap geometry covaries within existing evidence |
| R5-X03 | rotate both operands | Geometric covariance; local/global status remains truthful |
| R5-X04 | reflect both operands | Geometric covariance; ambient orientation reversal does not reverse the source semantic phase frame or make transformed Cartesian order identity |
| R5-X05 | nonzero dilate both operands | Geometric covariance with residual/tolerance scaling handled by existing policy |
| R5-X06 | transformed query identity | New result/source-pair identity, newly derived intrinsic phase/rank and new exact tokens; no source selector/rank/token reuse |
| R5-X07 | coordinate/order perturbation | Selector phase/rank and tokens do not derive from Cartesian order, proximity, solver/list index, marker order or UI ordinal |
| R5-X08A | **Option A:** `k=0` target misses collapsed point | Zero finite solutions according to ordinary solver evidence |
| R5-X09A | **Option A:** `k=0` collapsed point lies on target | Existing overlap/non-isolated policy; no fabricated admissible isolated token |
| R5-X08B | **Option B:** `k=0` with any target | No intersection result is fabricated from the unsupported/undefined transformed locus |
| R5-X10 | topology transition under dynamic input | Existing R4 deterministic-selector and topology-evidence rules; ambiguity invalidates and no token jumps |
| R5-X11 | transformed-query path independence | Reach byte-identical final Construction state and durable IDs by direct, incremental, reverse and save/reopen transformation updates; current admissibility, token binding and point definedness are identical |
| R5-X12 | ordinary regular transformed motion | While the deterministic selector remains uniquely valid, materialized roots remain defined and continuous without previous-Cartesian or update-history authority |
| R5-X13 | source/transformed selector isolation | Geometrically covariant roots use distinct source-pair selector certificates and tokens; transformed phase/rank is deterministically recomputed from the preserved semantic frame |

## 9. Transformation closure and dynamic DAG

| ID | Scenario | Required assertion |
|---|---|---|
| R5-K01 | `Translate[Rotate[L,...],...]` | Correct composition and two normal parent edges |
| R5-K02 | `Reflect[Dilate[L,...],...]` | Correct composition, source addresses retained through both parents |
| R5-K03 | `Rotate[Reflect[L,...],...]` | Correct composition; ambient orientation never becomes parameter identity |
| R5-K04 | `Dilate[Translate[L,...],...]` | Correct composition and metric factor |
| R5-K05 | change vector/angle/center/axis/factor | Exactly normal DAG recomputation; all dependent semantic consumers update |
| R5-K06 | repeated equivalent update | Deterministic semantic comparison; no presentation-driven revision |
| R5-K07 | remove/undo/redo one transform | Ordinary lifecycle; no orphan identity/evaluator/cache state |

## 10. Persistence, copy and presentation

| ID | Scenario | Required assertion |
|---|---|---|
| R5-S01 | `.cedg` save/reopen for every family | Existing command spelling/inputs reconstruct semantic output and new durable ID |
| R5-S02 | downstream point/metric/intersection save/reopen | Dependencies, rich results and transformed-query phase/rank selectors/tokens remain current and distinct from source-query tokens |
| R5-S03 | copy/remap | Existing exact provenance rules; no label/coordinate fallback |
| R5-S04 | rename source/output/inputs | Reopen and recompute unchanged semantically |
| R5-S05 | undo/redo | Normal transform parent/output and downstream lifecycle |
| R5-S06 | initial style | Host transformation style convention applied once |
| R5-S07 | independent output style edit | Ordinary R2 persistence; zero semantic revision/identity effect |
| R5-S08 | XML/archive inspection | No render vertices, sampled point cloud, detached matrix or serialized callback; `app="classic"` unchanged |
| R5-S09 | Classic diagnostic preservation | Opens/recomputes/saves supported transformed locus without enabling creation |

## 11. Forbidden-authority and legacy regressions

| ID | Scenario | Required assertion |
|---|---|---|
| R5-N01 | reflection/translation/etc. implementation type audit | `GeoLocusV2` remains non-`Path` and does not adopt mutable transform interfaces |
| R5-N02 | render cache varied/absent | Identical semantic transform outputs |
| R5-N03 | viewport/zoom/DPI or marker/list order changed | Identical semantic points, metrics, intersection phase/rank selectors and tokens |
| R5-N04 | ordinary non-Locus transformations | Existing outputs, command routing, labels, styles and XML unchanged |
| R5-N05 | circle inversion | Existing supported non-Locus behavior unchanged; no Locus R5 claim |
| R5-N06 | legacy `GeoLocus` | No new overload/migration/behavior change |
| R5-N07 | 3D command processors | Existing 3D dispatch unchanged; no Locus V2 3D route |
| R5-N08 | feature policy | No new flag and no command-visibility regression for ordinary objects |

## 12. Determinism and evidence

Two clean focused runs over identical fixtures must emit an identical canonical
summary. The summary shall include scenario IDs/results, productive/test path
hashes, source fixture hashes, command counts and exact normalized evidence
hash. Timing, temporary paths, ZIP timestamps and environment noise are not
semantic evidence.

Future evidence belongs under:

```text
geocedg/validation/g9u0-r5/
```

Generated logs belong only under ignored `artifacts/g9u0-r5/`.

## 13. Required historical/composed authority

After focused and deterministic PASS, rerun without weakening:

- G9U0, G9U0-R1, G9U0-R2, G9U0-R3 and G9U0-R4;
- complete relevant G8 intersection and R4 token/admissibility authority;
- G7 metric and G6 semantic/topology authority;
- G9X1, G5 and relevant G9A identity/persistence;
- legacy/scientific Locus;
- ordinary transformation command and 3D dispatch regressions;
- relevant Checkstyle;
- `git diff --check` and `git diff --cached --check`; and
- full `tools/agent/verify.ps1` with terminal
  `All GeoCeDG verification gates passed.`

## 14. Manual author smoke plan

Prepare, but do not self-pass:

1. launch GeoCeDG with the one Locus V2 feature flag;
2. create/open one visible nontrivial Locus V2;
3. execute each supported existing command form;
4. vary vector, angle, centers, axis and positive/negative factor;
5. verify semantic points at matching branch/parameter addresses;
6. compare total and partial lengths;
7. intersect transformed loci with ordinary targets and materialize an
   admissible exact-token point where R4 permits;
8. reach the same regular transformed-query geometry by direct, incremental
   and partial-reverse input updates, then confirm the same intrinsic phase/rank
   selector and exact token binding;
9. compose at least three transformations;
10. set `k=0` and verify only the selected policy: Option A yields a valid
   source-domain-preserving collapsed locus and typed length zero without a
   fabricated isolated intersection token; Option B yields an explicit
   unsupported/undefined transformed state with no fabricated downstream value;
11. restore nonzero `k` and verify deterministic recovery under the selected
    policy;
12. edit ordinary styles and confirm semantic dependents do not update; and
13. save/reopen `.cedg`, then verify identities, styles and dynamic consumers.

The author alone records smoke PASS.

## 15. Exit condition

Automation may end only at:

```text
G9U0-R5 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false
```

R5 PASS and G9U1 authorization remain separate author decisions. R5 closeout
must prepare or supersede the definitive post-R5 G9U1 prompt, without executing
it, and retain the planned kernel-selector/exact-token-only candidate-marker
hit testing, create-one/create-all and opt-in visible frontend
auto-materialization with no UI/list/marker rank authority; professional
menu/tool, visual-identity, existing-host `Continuity = OFF` GeoCeDG product
invariant with Classic configurability, and `geocedg.brand.topbar` /
`geocedg.brand.startup` requirements.
