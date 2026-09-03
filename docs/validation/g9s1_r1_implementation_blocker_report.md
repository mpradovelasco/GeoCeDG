# G9S1-R1 implementation — scientific stop for author review

## Governance and scope

G9S1-R1 productive implementation was authorized under the author-approved D2
partial contract B. Disposition C is author accepted; its demonstrated monodromic
scope remains rich-only. **The phase is NOT PASS and implementation is not
technically complete.** Terminal state:
`IMPLEMENTATION IN PROGRESS — STOPPED FOR AUTHOR SCIENTIFIC REVIEW`.
`implementationAuthorized=true`, `implementationStarted=true`,
`selfApproved=false`, `authorApprovedPhase=false`, `passClaimed=false`,
`manualAuthorSmoke=NOT READY — SCIENTIFIC BLOCKER`.

This record invokes the implementation request's scientific stop condition:
the required native-knot positive control does not satisfy the C1 hypotheses
of the chosen rigorous certificate. No tolerance-based gluing, root merging,
coordinate matching or spline-semantic modification has been introduced.

Entry: `codex/g9s1-r1-spline-pair-materialization`, HEAD/main/origin/main/direct
remote main `109f077fc5e2a40bcde45d3271eb928ee66fdfcc`; empty index. The protected
G9U1 planning checkpoint remains
`00982e7e148a634cd57ed928f322774df267d5e3`. No commit/push/merge/tag was performed.

The prior 13-path D2 work remains preserved. Its historical reports, tests and
three evidence records were not rewritten. Living roadmap/traceability updates
are prospective, not edits to the negative mathematical findings. D2 trace
`101cba5816b57b48feb2bd9a2185841809115331fc11a6421ea78550b5797354` and D2 evidence
canonical-LF SHA-256
`21fe616f80bd511d4c0ca08a9e361c9800c4f518835113a72679b625de2f4f46` remain the
historical authority.

## Exact blocker: the native polynomial is not exactly glued

The new public regression uses ordinary commands, not injected coefficients:

```text
A=(-1,0)
B=(0,0)
C=(1,0)
S=SplineV2({A,B,C},3)
```

Complete pair reproduction adds:

```text
h=-0.1
E=(h,-1)
F=(h,0)
G=(h,1)
T=SplineV2({E,F,G},3)
R=Intersect(S,T)
```

The intended test then materializes the unique slot and updates the existing
numeric `h` through `[-0.05, 0, 0.05, 0.1, 0, -0.1]` using `setValue()` and
`updateCascade()`. It actually fails at the initial `h=-0.1` eligibility
assertion, before materialization or movement: the vertical source already
meets its internal knot. Both germ classes are UNRESOLVED; the bounded pass
visits 68 boxes and makes zero Krawczyk attempts. Thus the unexecuted movement
loop is not reported as measured evidence.

The unchanged published spline solve stores, for the varying coordinate, with
`e = 2^-52`:

```text
pL(u) =  e*u^3 + 2*u - 1
pR(u) = -e*u^3 + 4*e*u^2 + (2-2*e)*u - 1
internal knot u = 1/2
```

Exact dyadic evaluation of these **actual stored coefficients** gives:

| Evidence | Exact value |
|---|---|
| Left value at knot | `+2^-55` |
| Right value at knot | `-2^-55` |
| Right-minus-left value | `-2^-54` |
| Right-minus-left first derivative | `+2^-53` |

The represented pieces are not C0, hence not C1, at this join. This is not merely
replacement of a numerical box or projected rank. Left/right polynomial sign
tests at `nextDown(0.5)`, `0.5` and `nextUp(0.5)` exhibit separate one-sided real
zeros. Both sides have positive derivative there. Merging those preimages would
change the represented semantic source and can erase same-germ multiplicity.

The corresponding public horizontal/vertical pair therefore truthfully remains
UNRESOLVED/rich-only at the knot. The positive test
`nativeDyadicKnotCrossingKeepsCurrentSingletonSlot` remains failing and visible;
it has not been removed, skipped or weakened to manufacture acceptance.
The separate `nativeKnotStoredPolynomialJetDefectIsExactNotTolerance` test checks
the real coefficients with exact `BigDecimal(double)` arithmetic.

The certificate uses the C1 Krawczyk inclusion hypothesis, not a Newton residual
as a proof. Reference: Rump, *Verification methods: rigorous results using
floating-point arithmetic*, Acta Numerica 19 (2010), Theorem 13.3,
[primary text](https://www.tuhh.de/ti3/rump/intlab/ActaNumerica2010.pdf).
Historical D2 already distinguishes exact-model fixtures from floating native
knot gluing; see [D2 contract](../architecture/g9s1_r1_d2_pair_sheet_contract.md)
and [native atlas characterization](../architecture/g9s1_r1_symmetric_atlas_design.md).

## Implemented attempt retained, not accepted product authority

- Shared interval/Krawczyk proof and class-specific exhaustive coverage, guarded
  by native SplineV2 capability or supported captured R5 spline transforms.
  Generic non-Spline LocusV2 pairs do not acquire this authority.
- Outward IEEE-754 arithmetic fails closed on unsupported/nonfinite operations;
  proof uses original captured polynomial plus captured transforms, not rounded
  flattened rendering coefficients. Exact C0/C1 jet equality is required where
  a proof crosses a knot. Centered Taylor enclosure intersects Horner enclosure
  only on verified C1 charts.
- Symmetric source-associated branch/component/orientation/domain/parameterization
  and normalized germ selector; no u/v, box, list order or history in identity.
  Enclosing owner/constructive/topology context stays in the existing ledger.
- Discriminated pair-bearing ledger v5; scalar-only snapshots retain v4 and
  v1–v4 import. Pair quarantine differs from R4 periodic quarantine. Current
  address evidence is separate; exact two-source copy provenance is required.
- Same ordinary GeoPoint/exact token consumer. Current proof loss leaves a
  retained claim dormant; proven duplicate selector quarantines it. Same-slot
  unique current resolution reactivates it. **Semantic-slot reactivation is not
  proof of continuous physical-root passage through topology events.**
- One bounded certification pass per query, not one solve per point. Existing
  polynomial discovery remains; the proof independently verifies exclusions
  instead of treating floating broad-phase rejection as a certificate. Up to
  32,768 proof boxes and 16,384 Krawczyk attempts under the current pair budget;
  unresolved work fails closed. No performance acceptance is claimed yet.

Initial public DEV controls establish useful positives (ordinary crossing,
rank exchange, class-specific mixed multiplicity, transforms, active/dormant
recovery, XML, exact closure copy and undo/redo). These do not waive the native
knot failure or the remaining complete matrix. The Desktop native `.cedg` test
is written and compiled, but has not been executed; XML success is not native
archive acceptance. Pair XML active/dormant/recovery, source-associated closure
copy followed by reopen, and undo/redo passed in the selected shared tests.

## Validation chronology and remaining work

Durable run/inventory/hash details are in the
[blocker evidence](../../geocedg/validation/g9s1-r1/g9s1-r1-implementation-blocker-evidence.json).
Ignored raw logs under `artifacts/g9s1-r1-implementation/` are execution evidence,
not source authority.

1. DEV01 sandbox: compiler could not resolve existing classes. No Java change
   was made to compensate. Same command outside sandbox compiled normally.
2. DEV02: 88 tests, 5 failures. One test expected `v5` instead of actual `5|`;
   coverage precision and periodic representative assertions were characterized.
3. DEV03: 91 tests, 2 failures. Rigorous centered enclosure improved coverage;
   projected-rank motion and mixed-germ class isolation passed. Native knot
   hypothesis failure remained. Periodic local proof passed, but its test
   incorrectly required the approximate representative on one particular side
   of the canonical seam; corrected to canonical interval/enclosure semantics.
4. DEV04: 92 tests, one failure. The added exact-native-jet diagnostic passes;
   the unchanged native-knot positive remains failing.
5. DEV05/06: same 92 tests, same one failure, deterministic outcomes and selected
   scientific traces. Checkstyle subsequently identified one remaining import
   ordering warning; only import ordering was changed before final DEV07/08.
6. DEV07/08: final diagnostic A/B, 92 unique tests per execution, 91 pass and
   the same one native-knot failure; both commands exit 1. No skipped tests.
   Outcome SHA-256:
   `48f3e86e6c20bb6e11c4b32f64d86e76326dcd5adffbcf57dd7d33d9382332c8`.
   Selected scientific-trace SHA-256:
   `06ca7d4f2f97c820a40e8180f007478671491e024ca4c2a7fed05dafddb5fc54`.
   These hashes establish reproducible diagnostics, **not a green phase**.
7. Fake-first verification runtime tests: 112/112, exit 0; zero real Gradle
   executions inside that operational fixture suite. Checkstyle, parser and
   diff results, with raw log hashes, are separately recorded in the evidence.

Final diagnostic command (A/B differ only in ignored log directory):

```powershell
.\tools\agent\verify.ps1 -Level DEV -Module shared `
  -TestFilter 'org.geocedg.*G9S1R1*',
    'org.geocedg.common.kernel.locus.intersection.PiecewisePolynomialPairIntersectionCapability2DTest' `
  -KeepBuildOutputs -LogDirectory artifacts/g9s1-r1-implementation/dev-07
```

The final inventory contains 40 paths: 13 retained D2 paths (including two
prospectively updated living authorities), 13 productive shared-kernel Java
paths, four additional Java test paths, and ten additional operational/design/
evidence paths. The exact names and canonical-LF source hashes are recorded in
the evidence. No productive Desktop/web/profile, G9U1, author asset or generated
artifact path is included. The index remains empty.

`VERIFICATION_INFRASTRUCTURE_IMPACT=UPDATE_REQUIRED`: a phase registration and
focused verifier were started. Therefore PHASE, COMPOSED and FULL-CleanBuild are
still mandatory **before any completed candidate**; none is waived. They were
not launched to claim acceptance while the explicit scientific stop is active.
The new phase verifier/evidence integration is not yet a completed acceptance
authority. The complete historical/scientific/performance/native matrix remains
pending; DEV is non-acceptance evidence.

`BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: no new external dependency, runtime or
toolchain was added. Existing Java includes the interval primitives and exact
diagnostic arithmetic. No arbitrary bootstrap edit. `GUIDE_IMPACT=UPDATED`
remains required on resumption; final user/developer guidance and source registry
are not claimed complete at this stopped implementation checkpoint.

The new verifier deliberately still requires missing final phase scenarios,
evidence and hash manifest; no empty or invented acceptance artifacts have been
created. Source/parser checks and DEV are not described as a passing R1 static
acceptance gate. Historical full regression counts are not borrowed from prior
phases. The required native archive, complete transformed/knot/seam/lifecycle
matrix, performance acceptance, PHASE A/B, COMPOSED and FULL remain unexecuted.

## Required author disposition

Do not silently replace the stored spline polynomial by an ideal continuously
glued spline. The author must decide whether to authorize a bounded correction
of spline representation/certification semantics that establishes the intended
joint regularity, or explicitly restrict this partial milestone to genuinely
certified spans/joins while retaining native unproved knots as rich-only and
reconciling the requested knot acceptance. The former requires design/source
compatibility review; this task has not implemented it. A tolerance cleanup or
nearest-root merge is not a valid third option.

G9U1 remains unimplemented/unauthorized and its checkpoint unchanged.
The future reconciliation remains bounded: consume exact tokens only for the
certified singleton-germ Spline pair scope after R1 approval; repeated-germ,
monodromic and uncertified cases remain rich-only. No frontend proximity bypass.
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains **OPEN / TRACKED**, independently
of pair persistence. No R4 risk closure follows from these tests.
