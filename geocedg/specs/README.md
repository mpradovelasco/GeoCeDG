# GeoCeDG specifications

This directory contains durable GeoCeDG contracts. A specification owns
behavioral meaning; prompts and verification scripts reference it and must not
restate its geometric rules.

Current author-approved contracts include the application profile, controlled
legacy integration, Windows packaging, neutral 2D export, and the internal
Locus V2 semantics/metrics/intersections closed through G8. G9P-R1 refined the
spatial, public-surface, workspace, extended-DXF, documentation, and bundle
specifications without implementing them. Those six G9P specifications are
`NORMATIVE / AUTHOR APPROVED`; implementation authorization remains an
independent phase status. G9O1, G9A1–G9A3/the G9A track, G9U0, G9U0-R1,
G9X1, G9U0-R2, G9U0-R3, G9U0-R4, G9U0-R5 and G9S1 are
`PASS — AUTHOR APPROVED` within their recorded scopes. Historical R4 smoke 1 found two-root invalidation during regular
motion; historical smoke 2 found four finite roots that were initially not
materializable. The author subsequently authorized the narrow intrinsic
semantic phase/rank refinement in
[ADR 0017](../../docs/adr/0017-deterministic-intersection-phase-rank-identity.md).
Only repeated base-selector groups are ordered by pairwise-disjoint canonical
root intervals in the explicit oriented domain; solver/list/UI/coordinate
order remains non-authoritative. Orientation, domain kind, verified group
cardinality and intrinsic rank form one versioned durable selector context.

The third historical smoke passed the two-root case plus initial four-root
detection/selectors/tokens/materialization, then failed persistence during
ordinary regular motion. The fixed `component span / 256` guard was an
update-size bound, not topology evidence. The current correction uses a separate
adaptive intrinsic periodic phase-tube/cell certificate: ordinary regular motion
preserves tokens; insufficient/nonunique cyclic evidence durably quarantines,
unique offset zero releases/reactivates and proved unique nonzero
seam/monodromy permanently retires only the affected ranked group. Ledger v4 imports canonical phase-v3 and authentic
pre-phase v2/v1 state. A materialized claim ordinarily absent or non-current
remains dormant or periodically quarantined; the same existing `GeoPoint` is automatically reactivated by
kernel recomputation only when the same selector again resolves uniquely. This
does not create a new point. Future G9U1 markers and opt-in auto-materialization
remain frontend-only, consume current tokens only and have not been executed.
The adaptive correction and dormant-reactivation lifecycle subsequently passed
the final four-root and recurrence author re-smokes.

The current source-declared focused authority comprises 27 public-kernel, 28
ledger and 3 Desktop methods (58 total). Periodic `q`/`r` survives ledger
recompute/export-import/copy; native `.cedg` separately covers dormant
`2 -> 4 -> 2` same-point reactivation and reopen after reactivation, not a
periodic-quarantine native round trip. A historical replacement composed run
exposed the missing
localized value for `DETERMINISTIC_SELECTION_ESTABLISHED`; the bounded
base/English/Spanish bundle addition changes no semantic contract. The current
candidate inventory is 54 paths, 31 under `source/`. Protective checkpoint
`4ef2c9d` and its 50-method evidence predate the adaptive/dormant correction;
current A/B each pass 58/58 with historical closeout canonical SHA-256
`3e9ea0aa20d511f2828eae61e491c1b3b5d9cb86a0f02166503ee5093d6000fb`
and composed passes. R4 closes with `selfApproved=false`,
`authorApproved=true`, `passClaimed=true`; no smoke result is erased.
Retained risk `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` records the missing
dedicated native `.cedg` quarantine round trip. It is nonblocking for R4,
revisited by G9U1 and due for resolution or explicit author disposition by
global G9 closeout, without becoming an implicit R5 dependency.
`locus/locus-v2-similarity-transformations.md` is the G9U0-R5 **NORMATIVE —
DESIGN PASS — AUTHOR APPROVED** contract. Its implementation is also
**PASS — AUTHOR APPROVED**; Option A governs finite `k=0` as a valid
source-domain-preserving `COLLAPSED_IMAGE`. The shared-kernel implementation is
closed in that same author-approved state.
It routes all seven approved ordinary 2D transform forms to new first-class
`GeoLocusV2` DAG outputs, evaluates source-first at the unchanged semantic
address, preserves finite/unbounded domain classification while adding the
orthogonal collapsed-image property, and uses that property as the semantic
proof for exact-zero metric components without making invalid source gaps
valid. Axis/plane/spatial-center 3D routes fail closed, transformed rich
intersections receive new R4-context selectors/tokens, and construction
participation/redefinition rejected before the R5 transform's own publication
rolls back without R5 residue. R5 does not redefine upstream
retention of a successful nested subcommand after a later unrelated outer
failure. The author accepts slider/existing-object dynamic factor updates and
characterizes free-input `k=0.25` rejection as upstream G9A
`REDEFINE_CONTEXT_MISSING`, not an R5 semantic failure; R5 does not broaden G9A.
No G9U1 work is part of R5.

`curves/semantic-spline-2d.md` is the G9S1 **NORMATIVE — PASS — AUTHOR
APPROVED** contract. Accepted ADR 0018 selects Option B: a new experimental
`SplineV2` semantic parent publishes a new first-class `GeoLocusV2` with
explicit oriented domain, stable span/knot semantics and normal DAG
dependencies; Classic `Spline` remains unchanged. Existing Locus V2
Point/metric/intersection/token, R4 lifecycle, R5 transform and persistence
authorities are reused. For one-sided intersections against the supported
polynomial target families, provider-owned spline spans feed deterministic
polynomial root partition/refinement and locally isolated transverse roots may
enter the existing R4 selector/token/materialization lifecycle. The present
piecewise-polynomial Locus V2 x Locus V2 capability is deliberately rich-only:
its floating box subdivision and dual-Newton evidence do not establish a
symmetric certified unique pair selector, so it allocates no active ledger
token and materializes no point. The approved implementation does not invent
scholarly citations and does not close the periodic-quarantine risk.

`locus/locus-v2-point-interaction.md` is the G9U0-R6 **NORMATIVE — PASS —
AUTHOR APPROVED** contract. Accepted ADR 0019 keeps the inverse
interaction seam in the shared kernel: a transient geometric request produces
a typed set of semantic preimages, explicit selection creates or edits the
ordinary point's branch/component/parameter address, and forward evaluation
remains geometric authority. It does not make `GeoLocusV2` a generic `Path`,
does not persist click/pixel/render-sample data, and does not implement the
future Point tool, markers or workspace. The structural affine certificate
supplies complete finite-component coverage without fitting; evaluator-only
zero/one results remain unresolved because a narrow unsampled minimum is not
excluded. SplineV2 uses coherent paired x/y polynomials, O(1) captured
composition depth, linear similarity propagation and the shared 128-level
ceiling; floating coefficients do not claim exact arithmetic. Address mutation
reuses the O(N) host Construction snapshot/restore boundary. Stable role plus
structurally exclusive inputs govern auxiliary presentation and reopen
restoration, while exact persisted component lineage disambiguates shared
endpoints or fails closed. R5 transformed loci, periodic domains and `k=0`
`COLLAPSED_IMAGE` retain their accepted contracts. The validated matrix contains
72 scenarios and the focused suite passes 55/55 twice (52 shared-kernel plus 3
Desktop) with identical canonical summary SHA-256
`7aaed6a558bf6f86ec93a5b45eb74155d45e66b52b47c373a9ad32f43b156cc9`.
The author accepts the kernel test-host/API diagnostic surface;
`manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN` and
`kernelDiagnosticAcceptance = PASS`.

The independent G9U1 planning branch is protected at checkpoint
`857de6628489bda0b65a5ba5145e62ca0795fc32`; its checkpoint prompt canonical-LF
SHA-256 is
`2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322`.
R6 neither merges nor executes those 17 planning paths. G9U1 remains
unexecuted and requires post-R6 reconciliation plus separate execution
authorization. Retained risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains `OPEN / TRACKED`.

G9U1 remains unexecuted. Its definitive post-R6 reconciliation is **G9U1 DESIGN
PASS — AUTHOR APPROVED — POST-R6 RECONCILED**, while implementation remains
**NOT AUTHORIZED / NOT STARTED**; G9B and G9C remain
designed but not authorized. The candidate
`ui/g9u1-construction-interaction.md`,
`ui/application-profile-v2.candidate.schema.json` and
`ui/application-profile-v2.candidate.yml` are prospective planning authorities,
not the live product profile. They consume the published R6 typed inverse
resolver and interaction-owned semantic point creation/move operations; no
frontend inverse fallback, `Path` conversion or proximity identity is allowed.
The existing host Continuity setting remains off in GeoCeDG while Classic stays
configurable. Exact GGBScript forms such as `Point(L, branch, u)` remain the
scripting authority; pointer resolution is a UI interaction seam. The protected
pre-R6 checkpoint remains immutable and the canonical successor prompt is
`.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r6.prompt.md`.
G9U2 remains blocked and productive G10 remains unauthorized.

`locus/locus-v2-public-ui-exposure.md` is the bounded, author-approved G9U0-R3
implementation contract. R3 is `PASS — AUTHOR APPROVED`; its retained smoke
chronology includes the long-token width defect, bounded correction and passing
re-smoke. This additive authority leaves the hash-frozen G9U0 public-surface
spec unchanged and assigns graphical candidate markers prospectively to G9U1.

The G9U0-R2 contracts
`locus/locus-v2-presentation.md` and
`ui/native-document-identity.md` are **NORMATIVE / AUTHOR APPROVED** authorities.
Their implementation is `PASS — AUTHOR APPROVED`. The original R2-L11 smoke
failure remains historical evidence; the bounded derived-render correction,
automated authority and interactive author re-smoke all pass. `.cedg` is now the
native GeoCeDG document identity, while `.ggb` remains compatibility input and
the existing ZIP/XML plus `app="classic"` internals remain unchanged.

Every specification must state its status, version, authority, scope,
invariants, compatibility policy, validation evidence, and stop conditions.
Use `templates/specification-template.md` as the starting structure.
