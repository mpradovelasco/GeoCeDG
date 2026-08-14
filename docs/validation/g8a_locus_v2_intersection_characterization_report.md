# G8A — Locus V2 2D intersection characterization report

| Field | Result |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| Execution date | 2026-08-14 |
| Entry HEAD | `315aec011cdc719a41a9bdc352a4a10ea502df6e` |
| Branch | `feature/g8a-locus-v2-intersections-characterization` |
| G8 specification | **NORMATIVE / AUTHOR APPROVED** |
| ADR 0008 | **Accepted** |
| G8B | **AUTHORIZED / NOT STARTED** |
| Evidence bundle | [`g8a-intersection-characterization-evidence.json`](../../geocedg/validation/locus-v2/g8a/g8a-intersection-characterization-evidence.json) |
| Author closeout | [`g8a-author-closeout-evidence.json`](../../geocedg/validation/locus-v2/g8a/g8a-author-closeout-evidence.json) |
| Independent references | [`intersection-reference-values.json`](../../geocedg/validation/locus-v2/g8a/intersection-reference-values.json) |

G8A found a viable, bounded internal architecture for native intersections of
Locus V2 with line, segment, ray and circle targets. It also found that a
universal merge/split identity genealogy and generic evaluator-only
completeness are not defensible. The author approved D1–D17 on 2026-08-14,
made the G8 specification normative, accepted ADR 0008 and authorized G8B for
separate execution. No productive implementation exists in this report.

## 1. Entry gate and authority state

The repository root was
`C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG`. At entry,
`HEAD`, the current branch base and `origin/main` were all
`315aec011cdc719a41a9bdc352a4a10ea502df6e`; divergence was `0/0`, and the
tracked worktree was clean. The pinned upstream baseline remained GeoGebra
5.4.928.0 at `9b93256b7df401ff056c37b502d82df4d72b1522`.

The entry gate reproduced G6/G6R and G7, including G7A-R1/G7A/G7B. ADRs 0006
and 0007 were Accepted, their G6/G7 specifications were normative, G8 planning
was `PASS — AUTHOR APPROVED`, G8A was authorized, and G8B/G9/productive G8 had
not started. Locus V2 remained experimental, internal and disabled by default.
The 12 catalog references and two legacy pilot-model hashes used by G8A had no
mismatch.

The user named a plural-path prompt that does not exist:
`.github/prompts/tasks/g8a-locus-v2-intersections-characterization.prompt.md`.
The versioned canonical task is the singular-path
[`g8a-locus-v2-intersection-characterization.prompt.md`](../../.github/prompts/tasks/g8a-locus-v2-intersection-characterization.prompt.md),
SHA-256
`0f962a36b2cae76d2208371bd39e68fff8bf5cd1d9a7f3f9e9ca14afefef424b`,
which was executed.

## 2. Evidence produced

The saved test-private suite contains 65 tests:

| Evidence family | Tests | Result |
|---|---:|---|
| Target adapters and rich semantic axes | 13 | PASS |
| Solver, tangency, tolerance and completeness | 15 | PASS |
| Dynamic identity and genealogy | 12 | PASS |
| Branch/component topology and degeneration | 7 | PASS |
| Actual `GeoElement`/`AlgoElement` lifecycle | 10 | PASS |
| Functional counters and reduced scientific pilots | 8 | PASS |
| **Total** | **65** | **PASS; 0 failed/skipped** |

The probes compile against the real G6/G7 and GeoGebra classes. They are not a
mock replacement for `GeoLocusV2`, `GeoLine`, `GeoSegment`, `GeoRay`,
`GeoConic`, `GeoFunction`, `GeoImplicit`, `AlgoElement.OutputHandler` or the
Construction DAG. All G8 solver/result/identity classes remain in test source.

Independent references use CPython 3.12.13 and mpmath 1.4.1 at 80 decimal
digits. The generator reruns byte-for-byte with `--check`. It declares the
reduced focal and LSIM equations explicitly; no figure, screenshot or legacy
sample is a numeric oracle.

## 3. Geometric and family characterization

| Target | Actual authority | Closed G8A result | Author-approved disposition |
|---|---|---|---|
| Line | `GeoLine` homogeneous coefficients | normalized equation, derivative and complete finite/empty proofs characterized | G8B core |
| Segment | support line plus `GeoSegment.respectLimitedPath` | support incidence and finite membership remain separate; no clamping | G8B core |
| Ray | support line plus `GeoRay.respectLimitedPath` | included start and direction filtered independently | G8B core |
| Circle | `GeoConicND` matrix plus verified circle type | secant/tangent/empty, scaling, seam and residual evidence characterized | G8B core |
| Full conic | matrix, type and degeneration state | representation exists, but completeness/overlap/singularity varies by subtype | defer from minimum |
| Function | `value` plus an explicit interval when genuinely present | view bounds are inadmissible; discontinuity/unbounded completeness remains open | Level C defer |
| Implicit | native equation, derivatives and coefficients | valid authority exists, but component/singularity completeness is not closed | Level C defer |
| Locus–locus | two semantic evaluators and revisions | requires a separate two-parameter isolation/identity contract | Level C defer |

The `GeoFunction.setInterval` API retained an explicit semantic interval in the
probe. The attempted `Function(f,-1,1)` construction route did not expose
`hasInterval()` on the returned test object. That is an additional reason not
to promote function targets by nominal type alone.

## 4. Rich result and completeness

The test-private model validated six orthogonal axes:

1. computation status;
2. `COMPLETE` / `INCOMPLETE` / `NOT_ESTABLISHED` completeness;
3. finite/empty/overlap/infinite/unresolved geometry kind;
4. currentness;
5. support level; and
6. existing G6 `NumericGuarantee`.

Per-root residual, contact, multiplicity, domain location and identity status
remain separate. The model rejects `EMPTY` unless completeness is `COMPLETE`.
It accepts a verified finite subset as `INCOMPLETE` or `NOT_ESTABLISHED` and
prevents a derived point consumer from presenting it as the full intersection.

Overlap is a typed query-level result. A collapsed component on its target and
an identically coincident interval produced `OVERLAP` with zero arbitrary
point samples. Unknown multiplicity has no integer or NaN sentinel.

## 5. Solver hierarchy

| Strategy | Legitimate claim | Completeness boundary | Disposition |
|---|---|---|---|
| Target-specific analytic/factorization | exact capability/proof provenance; coordinates still independently verified | complete only when all roots or exclusion/overlap are proved | preferred first |
| Certified bracket/interval | certified simple root and bounded refinement | sign-changing brackets alone miss even roots | retain as one capability, never sole tangency method |
| Derivative-aware | established contact where derivative/order evidence supports it | convergence does not exclude other roots | combine with independent isolation/count evidence |
| Evaluator-only adaptive | floating-point verified candidates | exhaustiveness and empty are not established, especially unbounded domains | candidate discovery only |
| Conservative bounds/broad phase | safe pruning only with an independent coverage proof | omitted/unproved regions force incomplete/not-established | optional query-local accelerator |

Even and fourth-order roots were detected without sign changes. Where only an
evaluator sample encountered a small residual, contact and multiplicity stayed
undetermined. A nonfinite evaluation at a declared candidate produced
`INCOMPLETE + UNRESOLVED`, never false empty or stale success.

## 6. Measured tolerance evidence and approved normalization

The independent sweep measured:

- maximum normalized binary64 residual in the declared scale sweep:
  `2.0194839173657902e-28`;
- monotone-map binary64 round-trip error: `1.1102230246251565e-16`;
- near-tangent derivative magnitude for offset `1e-12`: `2e-6`; and
- a deliberately distinct clustered-root gap: `2e-8`.

The candidate policy is:

| Quantity | Candidate | Role |
|---|---:|---|
| semantic root interval | `1e-12` | refinement/localization |
| absolute normalized residual | `2e-12` | independent incidence verification |
| relative normalized residual | `2e-12` | scale-aware incidence verification |
| tangency threshold | `1e-10` | contact evidence, never completeness alone |
| semantic deduplication | `4e-12` | duplicate candidates in one preimage |
| semantic continuation | `1e-8` | prediction evidence only |
| coordinate verification | `4e-12` | secondary evaluated-coordinate consistency |

These measured values have different units and meanings. None is inherited
from G6 domain, G7 metric, kernel, render or pixel tolerances. The
machine-readable characterization evidence remains unchanged and records them
as G8A candidates.

At closeout the author accepted them as the source values for
`g8b-initial-normalized/v1`, subject to a mandatory normalization contract:

- a target adapter exposes a model-distance-equivalent residual where correct,
  otherwise a family-specific typed residual and matching tolerance;
- algebraic equation scaling cannot change the geometric decision;
- root, deduplication and continuation tolerances remain in declared provider
  parameter space, not Euclidean distance;
- the tangency threshold applies only to a normalized contact indicator, not a
  raw equation/parameter derivative; and
- coordinate tolerance remains verification evidence and never identity.

The raw number is used only when its normalized quantity matches the one
characterized; otherwise G8B must validate the normalized equivalent.

## 7. Dynamic identity result

The durable candidate consists of source-pair identity, constructive
intersection lineage, established branch lineage, topology context, an opaque
root token and an explicit continuation relation/status. Locus/target
revisions, component key, semantic/lifted parameter, isolating interval,
residual and solver certificate are revision-scoped evidence.

Identity continued under:

- ordinary motion with one unique semantic prediction;
- a known monotone reparameterization, including a derivative-degenerate cubic
  map while keeping contact guarantees separate;
- permitted orientation reversal with a known map; and
- a periodic seam using declared periodic semantics and a lifted parameter.

An isolating interval changed while the token remained the same. Thus it is
localization evidence, not fundamental identity. An unknown map, multiple
admissible semantic predecessors, or a replaced branch without unique G6
lineage produced `NOT_ESTABLISHED`, `AMBIGUOUS_CONTINUATION` or
`IDENTITY_DISCONTINUITY`; no coordinates or output slots were consulted.

### Merge/split hypothesis outcome

The universal genealogy hypothesis did not survive the symmetric and reverse
traces. For `2 -> 1 -> 2`, G8A could record a merge event with two candidate
parents and a split event with candidate children, but could not choose which
child inherits which former token in a symmetric case. Reverse traversal did
not make that choice canonical.

Author-approved narrow contract:

- create a topology-event token at a merge/split;
- retain explicit candidate parent/child sets;
- preserve a token only when a unique semantic continuation is independently
  established;
- use new tokens plus explicit ambiguity/discontinuity otherwise; and
- retain only the previous/current topology epochs for continuation work.

## 8. Lifecycle and first-class CeDG use

An immutable rich result was published atomically through a test-private,
nonnumeric `GeoElement` produced by a normal `AlgoElement`. Both source locus
and target were declared inputs. Input edits updated the result and a later
CeDG-style algorithm through the ordinary DAG while an identified token was
unambiguous.

Injected failure removed the prior point, published one current unresolved
snapshot and later recovered by a fresh DAG update. Copy/set did not import a
revision-bound payload or mutable solver state. Removal, label and bounded
`OutputHandler` behavior were exercised without adding XML or `GeoClass`.

The author accepts the rich Geo plus a required separate internal one-token
point consumer for G8B. It becomes undefined without retargeting when the token
is absent, stale or ambiguous and recovers only for the same current token. A
variable-size point list, public command and point-only authority are rejected.

## 9. Work and state

Query-local analytic work scaled exactly for 1/3/10/100 consumers:

```text
semantic evaluations     = 1 / 3 / 10 / 100
residual verifications   = 1 / 3 / 10 / 100
retained intersection state = 0
```

For ten consumers at semantic nesting depths 1/2/3, underlying evaluator calls
were exactly `10/20/30`. Whole-locus regenerations, render/legacy/viewport/
pixel/metric-index reads, stale entries and partial publications were all zero.
Removing half/all of 1/3/10/100 derived consumers retained no intersection
index or root history.

Candidate deterministic ceilings are 32768 semantic evaluations, 16384
semantic derivative evaluations, 32768 target evaluations, 8192 candidate
intervals, 8192 subdivisions, depth 40, 80 refinement iterations per
candidate, 1024 residual verifications, 512 candidates, 4096 continuation
comparisons, 256 published finite solutions, zero retained intersection-index
entries and two topology epochs. Exhaustion produced a typed unresolved result.
The author provisionally accepts these deliberately conservative ceilings as
initial G8B implementation defaults; wall-clock time is informational, not a
truth gate.

No measured evidence justifies a shared intersection owner for G8B. Query-local
state is therefore required. Any later owner requires new measurements and
a separate decision on complete key, immutable payload, capacity, eviction,
Construction lifecycle and cache-off equality.

## 10. Scientific pilots

The focal proxy used `F_mu(t)=(t,mu)` against the unit circle. It produced two
transverse roots at `mu=0.6`, one double tangent at `mu=1`, and complete empty
at `mu=1.2` from an analytic discriminant.

The reduced cone–cylinder LSIM topology proxy used two constructive branches
`F_sigma(t)=(t,sigma*(t^2-lambda))`, `sigma=±1`, against `y=0`. It produced
four leaves at `lambda=0.25`, two distinct branch preimages at the same tangent
coordinate for `lambda=0`, and complete empty on both branches at
`lambda=-0.25`.

These formulas preserve the scientific requirements of multileaf topology,
tangency and downstream constructive identity. They intentionally omit 3D
surface semantics. The CeDG papers and legacy models motivated the scenarios;
they did not supply root values or algorithms.

## 11. Exact G8B candidate and deferred scope

Author-approved minimum families: line, segment, ray and circle. Approved
architecture: immutable rich set, normal-DAG nonnumeric rich Geo, query-local
solver, explicit completeness, narrow semantic continuation and one required
token-selected internal point consumer.

Deferred: all nondegenerate/degenerate conics as a family, functions, implicit
curves, locus–locus, generic paths, public commands, dispatcher integration,
variable point arrays, XML/persistence, export, 3D/G9 and Python.

The exact proposed signatures and smallest edit set are in
[`locus_v2_intersection_api.md`](../developer/locus_v2_intersection_api.md).

## 12. Rejected strategies

- render, legacy sample, viewport, zoom, DPI or pixel authority;
- sign-change-only tangency;
- coordinate-nearest, label, creation order or output-slot identity;
- solver convergence as completeness evidence;
- arbitrary finite samples for overlap;
- G7 metric state as intersection state;
- an unmeasured shared/global cache;
- a generic implicit conversion layer; and
- universal merge/split child inheritance.

## 13. Author decision table

The author approved every decision below on 2026-08-14:

| ID | Evidence and alternatives | Final disposition | Resulting G8B constraint |
|---|---|---|---|
| D1 | 65 passing probes, independent references and closed package | **APPROVE** | `G8A = PASS — AUTHOR APPROVED` |
| D2 | Rich Geo lifecycle passed; private/point-only alternatives lose set truth | **APPROVE** | Immutable result plus normal-DAG nonnumeric rich Geo is authority |
| D3 | Token-selected downstream DAG consumer passed; incomplete projection refused | **APPROVE AND REQUIRE** | One internal token-selected point; no retarget, explicit undefined and same-token recovery |
| D4 | Five strategies showed root validity and exhaustiveness are independent | **APPROVE** | `COMPLETE/INCOMPLETE/NOT_ESTABLISHED` plus method evidence |
| D5 | Even/fourth-order contact defeats sign-only detection | **APPROVE** | Analytic/certified/normalized derivative-minimum hierarchy; unknown stays unknown |
| D6 | Coincident/collapsed solutions have no canonical finite sample | **APPROVE** | Typed overlap/infinite/unsupported-overlap semantics |
| D7 | Analytic/certified can prove coverage; evaluator-only cannot generally | **APPROVE** | Truthful capability hierarchy; broad phase never authority |
| D8 | Seven measured values have different dimensions/roles | **APPROVE WITH NORMALIZATION CONTRACT** | `g8b-initial-normalized/v1`; typed/model-distance residual, provider parameter units, normalized contact, coordinate verify only |
| D9 | Typed exhaustion and bounded repeated/nested traces | **APPROVE PROVISIONALLY** | Exact measured ceilings are initial defaults; wall clock informational |
| D10 | Known semantic maps preserved tokens while parameters/intervals changed | **APPROVE** | Durable token/lineage/topology context separated from all revision evidence |
| D11 | Symmetric/reverse traces cannot select canonical descendants | **APPROVE G8A RESULT** | No universal genealogy; topology events, candidate relations, ambiguity/discontinuity |
| D12 | Query-local traces retained zero entries; no measured shared benefit | **APPROVE** | No G7 state, global cache, shared owner or intersection index |
| D13 | Only four target families have a closed minimum contract | **APPROVE** | Line, segment, ray and circle required |
| D14 | Full conic/function/implicit/locus–locus contracts remain open | **APPROVE DEFERRAL** | All remain outside minimum G8B |
| D15 | Public/API/persistence boundaries were not needed or characterized | **APPROVE** | Internal only; no command, Path, XML, legacy/Classic, 3D/G9 or Python |
| D16 | Rich Geo may require a unique exhaustive classification | **APPROVE IF REQUIRED** | One append-only dedicated `GeoClass`; no broader type-system change |
| D17 | No contradiction in spec/ADR after D1–D16 | **APPROVE PROMOTION** | G8 spec normative/author-approved; ADR 0008 Accepted |

The separate
[`g8a-author-closeout-evidence.json`](../../geocedg/validation/locus-v2/g8a/g8a-author-closeout-evidence.json)
records these decisions and their derived G8B policy. The original
machine-readable characterization file remains byte-for-byte historical
measurement evidence and therefore retains its pre-review status fields.

## 14. Execution anomalies and classification

Three non-product anomalies occurred and were not hidden:

1. an initial Gradle invocation left a `-D` argument unquoted, so Gradle parsed
   it as a task; this was an invocation error, not a test failure;
2. one combined test/checkstyle invocation lost the test classpath and also hit
   `AccessDeniedException` under `build/reports`; separating tasks and using the
   managed Windows permission path restored the correct environment; and
3. the initial function-command fixture did not retain `hasInterval`; the probe
   was corrected to exercise the actual explicit `GeoFunction.setInterval`
   authority rather than changing productive code.

One early consumer-removal assertion exposed normal unlabeled predecessor
pruning. The final removal experiment labels the private rich source to keep it
alive while half/all consumers are removed, then removes the source explicitly.
This records actual DAG lifecycle rather than patching production behavior.

The closeout verifier also exposed three repository/environment issues before
the final green runs:

1. the prompt evidence recorded the byte SHA-256 while the first verifier draft
   compared it as canonicalized text; the verifier now compares the declared
   byte hash and retains canonicalized hashing for evidence manifests;
2. the first roadmap update replaced the unique `Última fase cerrada` row with
   an execution row; the normative G8 planning-closeout row was restored and a
   separate `Última fase ejecutada` row records G8A; and
3. the operational gate required the eleven new test-private Java files to be
   registered in `docs/upstream/modified-files.yml`. They are now individually
   registered as characterization-only additions. A sandboxed Conda check also
   required the normal managed Windows permission path; the identical check
   then passed without a repository change.

## 15. G8A execution verification evidence

All commands ran from the repository root. Build/runtime evidence is real Java
unless the row says static; generated build output was restored and logs remain
under the Windows temporary directory, outside version control.

| Command | Exit | Evidence and log |
|---|---:|---|
| `.\tools\agent\verify-g8a-intersections.ps1` | 0 | 65 test-private probes, independent 80-digit references, checkstyle, hashes, links and source boundary; `%TEMP%\geocedg-verify-g8a-intersections\` |
| `.\tools\agent\verify-operational.ps1` | 0 | static operational/schema/text/upstream boundary; 194 registered files |
| `.\tools\agent\verify-locus-v2.ps1` | 0 | real G6/G6R Java plus static laboratory contract; `%TEMP%\geocedg-verify-locus-v2\` |
| `.\tools\agent\verify-g7a-metrics.ps1` | 0 | 51 test-private G7A/R1 probes and independent references; `%TEMP%\geocedg-verify-g7a-metrics\` |
| `.\tools\agent\verify-g7b-metrics.ps1` | 0 | 62 productive plus 3 laboratory probes; `%TEMP%\geocedg-verify-g7b-metrics\` |
| `.\tools\agent\verify.ps1 -SkipBuild -LogDirectory "$env:TEMP\geocedg-verify-g8a-composed"` | 0 | canonical composed authority after the focused build gates; `%TEMP%\geocedg-verify-g8a-composed\` |
| `git diff --check` | 0 | invoked by the focused and composed authorities; repeated after final evidence hashing |

The G8A wrapper checks exact JUnit totals (65, zero failures/errors/skips),
independent-reference reproduction, evidence hashes, 15 Markdown documents,
phase/status strings, forbidden authority dependencies and zero productive
`src/main` changes. No wall-clock value is used as a geometric or pass/fail
budget.

## 16. Disposition

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
